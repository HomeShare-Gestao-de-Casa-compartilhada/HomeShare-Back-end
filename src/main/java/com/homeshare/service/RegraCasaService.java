package com.homeshare.service;

import com.homeshare.dto.RegraCasaDto;
import com.homeshare.entity.Membro;
import com.homeshare.entity.RegraCasa;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.RegraCasaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Regras da casa: o líder escreve, todos leem. */
@Service
@RequiredArgsConstructor
public class RegraCasaService {

    private final RegraCasaRepository regraRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    @Transactional
    public RegraCasaDto.Response criar(Long casaId, Long usuarioId, RegraCasaDto.Request req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);
        RegraCasa regra = new RegraCasa();
        regra.setCasa(lider.getCasa());
        preencher(regra, req);
        regraRepository.save(regra);
        return RegraCasaDto.Response.de(regra);
    }

    @Transactional(readOnly = true)
    public List<RegraCasaDto.Response> listar(Long casaId, Long usuarioId) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        return regraRepository.findByCasaIdOrderByTituloAsc(casaId).stream()
                .map(RegraCasaDto.Response::de)
                .toList();
    }

    @Transactional
    public RegraCasaDto.Response atualizar(Long casaId, Long usuarioId, Long regraId, RegraCasaDto.Request req) {
        acesso.exigirLider(casaId, usuarioId);
        RegraCasa regra = buscar(regraId, casaId);
        preencher(regra, req);
        return RegraCasaDto.Response.de(regra);
    }

    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long regraId) {
        acesso.exigirLider(casaId, usuarioId);
        regraRepository.delete(buscar(regraId, casaId));
    }

    private RegraCasa buscar(Long regraId, Long casaId) {
        return regraRepository.findByIdAndCasaId(regraId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Regra não encontrada."));
    }

    private void preencher(RegraCasa regra, RegraCasaDto.Request req) {
        regra.setTitulo(req.titulo().trim());
        regra.setTexto(req.texto().trim());
        regra.setDataAtualizacao(LocalDateTime.now(clock));
    }
}
