package com.homeshare.service;

import com.homeshare.dto.GastoDto;
import com.homeshare.entity.GastoFixo;
import com.homeshare.entity.GastoMorador;
import com.homeshare.entity.Membro;
import com.homeshare.enums.StatusMembro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.GastoFixoRepository;
import com.homeshare.repository.GastoMoradorRepository;
import com.homeshare.repository.MembroRepository;
import com.homeshare.util.DivisaoDeValores;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoFixoRepository gastoRepository;
    private final GastoMoradorRepository parcelaRepository;
    private final MembroRepository membroRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    /**
     * O líder cadastra o gasto do mês e quem fica com cada parcela.
     * Sem "parcelas" no corpo, divide igualmente entre os moradores ativos.
     * Regra: a soma das parcelas tem de ser exatamente o valor total.
     */
    @Transactional
    public GastoDto.Response criar(Long casaId, Long usuarioId, GastoDto.CriarRequest req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);

        GastoFixo gasto = new GastoFixo();
        gasto.setDescricao(req.descricao().trim());
        gasto.setValorTotal(req.valorTotal());
        gasto.setMesReferencia(req.mesReferencia().withDayOfMonth(1));
        gasto.setCasa(lider.getCasa());

        if (req.parcelas() == null || req.parcelas().isEmpty()) {
            dividirEntreAtivos(gasto, casaId);
        } else {
            montarParcelasInformadas(gasto, casaId, req.parcelas());
        }

        gastoRepository.save(gasto);
        return GastoDto.Response.de(gasto);
    }

    /** Todos os moradores veem os gastos (transparência). mes no formato yyyy-MM, opcional. */
    @Transactional(readOnly = true)
    public List<GastoDto.Response> listar(Long casaId, Long usuarioId, String mes) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        List<GastoFixo> gastos;
        if (mes == null || mes.isBlank()) {
            gastos = gastoRepository.findByCasaIdOrderByMesReferenciaDescIdDesc(casaId);
        } else {
            YearMonth ym;
            try {
                ym = YearMonth.parse(mes.trim());
            } catch (DateTimeParseException e) {
                throw BusinessException.badRequest("Mês inválido. Use o formato yyyy-MM (ex.: 2026-11).");
            }
            gastos = gastoRepository.findByCasaIdAndMesReferenciaOrderByIdDesc(casaId, ym.atDay(1));
        }
        return gastos.stream().map(GastoDto.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public List<GastoDto.MinhaParcelaResponse> minhasParcelas(Long casaId, Long usuarioId) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        return parcelaRepository.findMinhas(casaId, ator.getId()).stream()
                .map(GastoDto.MinhaParcelaResponse::de)
                .toList();
    }

    /** O morador marca/desmarca a "checkbox" da própria parcela (o líder também pode corrigir). */
    @Transactional
    public GastoDto.ParcelaResponse marcarPagamento(Long casaId, Long usuarioId, Long parcelaId, boolean pago) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        GastoMorador parcela = parcelaRepository.findByIdAndGastoFixoCasaId(parcelaId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Parcela não encontrada."));

        boolean dona = parcela.getMembro().getId().equals(ator.getId());
        if (!dona && !acesso.ehLider(ator)) {
            throw BusinessException.forbidden("Você só pode marcar o pagamento da sua própria parcela.");
        }
        parcela.setPago(pago);
        parcela.setDataPagamento(pago ? LocalDateTime.now(clock) : null);
        return GastoDto.ParcelaResponse.de(parcela);
    }

    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long gastoId) {
        acesso.exigirLider(casaId, usuarioId);
        GastoFixo gasto = gastoRepository.findByIdAndCasaId(gastoId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Gasto não encontrado."));
        gastoRepository.delete(gasto);
    }

    private void dividirEntreAtivos(GastoFixo gasto, Long casaId) {
        List<Membro> ativos = membroRepository
                .findByCasaIdAndStatusOrderByDataEntradaAscIdAsc(casaId, StatusMembro.ATIVO);
        List<BigDecimal> valores = DivisaoDeValores.dividirIgualmente(gasto.getValorTotal(), ativos.size());
        for (int i = 0; i < ativos.size(); i++) {
            adicionarParcela(gasto, ativos.get(i), valores.get(i));
        }
    }

    private void montarParcelasInformadas(GastoFixo gasto, Long casaId, List<GastoDto.ParcelaRequest> parcelas) {
        Set<Long> vistos = new HashSet<>();
        BigDecimal soma = BigDecimal.ZERO;
        for (GastoDto.ParcelaRequest p : parcelas) {
            if (!vistos.add(p.membroId())) {
                throw BusinessException.badRequest("Cada morador só pode aparecer uma vez no mesmo gasto.");
            }
            Membro membro = acesso.buscarMembroAtivoDaCasa(p.membroId(), casaId);
            adicionarParcela(gasto, membro, p.valor());
            soma = soma.add(p.valor());
        }
        if (soma.compareTo(gasto.getValorTotal()) != 0) {
            throw BusinessException.unprocessable("A soma das parcelas (" + soma.toPlainString()
                    + ") precisa ser igual ao valor total (" + gasto.getValorTotal().toPlainString() + ").");
        }
    }

    private void adicionarParcela(GastoFixo gasto, Membro membro, BigDecimal valor) {
        GastoMorador parcela = new GastoMorador();
        parcela.setGastoFixo(gasto);
        parcela.setMembro(membro);
        parcela.setValorParcela(valor);
        parcela.setPago(false);
        gasto.getParcelas().add(parcela);
    }
}
