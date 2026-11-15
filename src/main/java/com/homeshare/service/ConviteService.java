package com.homeshare.service;

import com.homeshare.config.AppProperties;
import com.homeshare.dto.CasaDto;
import com.homeshare.dto.ConviteDto;
import com.homeshare.entity.Casa;
import com.homeshare.entity.Convite;
import com.homeshare.entity.Membro;
import com.homeshare.entity.Usuario;
import com.homeshare.enums.StatusCasa;
import com.homeshare.enums.StatusConvite;
import com.homeshare.enums.StatusMembro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.ConviteRepository;
import com.homeshare.repository.MembroRepository;
import com.homeshare.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConviteService {

    /** Sem 0/O/1/I para o código não ser confundido ao ser digitado. */
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_CODIGO = 10;
    private static final int MAX_TENTATIVAS_CODIGO = 5;

    private final ConviteRepository conviteRepository;
    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;
    private final CasaAcessoService acesso;
    private final AppProperties props;
    private final Clock clock;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ConviteDto.Response criar(Long casaId, Long usuarioId, ConviteDto.CriarRequest req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);

        int horas = (req != null && req.validadeHoras() != null)
                ? req.validadeHoras()
                : props.convite().validadePadraoHoras();
        if (horas > props.convite().validadeMaximaHoras()) {
            throw BusinessException.badRequest(
                    "A validade máxima de um convite é de " + props.convite().validadeMaximaHoras() + " horas.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        Convite convite = new Convite();
        convite.setCodigo(gerarCodigoUnico());
        convite.setValidade(agora.plusHours(horas));
        convite.setStatus(StatusConvite.ATIVO);
        convite.setDataCriacao(agora);
        convite.setGeradoPor(lider);
        convite.setCasa(lider.getCasa());
        conviteRepository.save(convite);

        return ConviteDto.Response.de(convite, agora);
    }

    @Transactional(readOnly = true)
    public List<ConviteDto.Response> listar(Long casaId, Long usuarioId) {
        acesso.exigirLider(casaId, usuarioId);
        LocalDateTime agora = LocalDateTime.now(clock);
        return conviteRepository.findByCasaIdOrderByDataCriacaoDesc(casaId).stream()
                .map(c -> ConviteDto.Response.de(c, agora))
                .toList();
    }

    @Transactional
    public void cancelar(Long casaId, Long usuarioId, Long conviteId) {
        acesso.exigirLider(casaId, usuarioId);
        Convite convite = conviteRepository.findByIdAndCasaId(conviteId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Convite não encontrado."));
        if (convite.getUsadoPor() != null) {
            throw BusinessException.unprocessable("Este convite já foi utilizado e não pode ser cancelado.");
        }
        convite.setStatus(StatusConvite.CANCELADO);
    }

    /** Mostra a quem recebeu o código em qual casa ele vai entrar (antes de confirmar). */
    @Transactional(readOnly = true)
    public ConviteDto.PreviaResponse previa(String codigo) {
        Convite convite = conviteRepository.findByCodigo(normalizarCodigo(codigo))
                .orElseThrow(() -> BusinessException.notFound("Convite não encontrado."));
        LocalDateTime agora = LocalDateTime.now(clock);
        boolean valido = conviteValido(convite, agora) && convite.getCasa().getStatus() == StatusCasa.ATIVA;
        return new ConviteDto.PreviaResponse(convite.getCasa().getNome(), convite.getValidade(), valido);
    }

    /**
     * Entra na casa usando o código. Convite de uso único.
     * Quem já morou (SAIU/EXPULSO) volta reativando a mesma linha de membro.
     */
    @Transactional
    public CasaDto.Response entrar(Long usuarioId, String codigo) {
        // Trava a linha do convite: dois usuários com o mesmo código não passam ao mesmo tempo.
        Convite convite = conviteRepository.findByCodigoParaAtualizar(normalizarCodigo(codigo))
                .orElseThrow(() -> BusinessException.notFound("Convite não encontrado."));

        LocalDateTime agora = LocalDateTime.now(clock);
        Casa casa = convite.getCasa();
        if (!conviteValido(convite, agora) || casa.getStatus() != StatusCasa.ATIVA) {
            throw BusinessException.gone("Este convite expirou, foi cancelado ou já foi utilizado.");
        }

        Optional<Membro> existente = membroRepository.findByCasaIdAndUsuarioId(casa.getId(), usuarioId);
        Membro membro;
        if (existente.isPresent()) {
            membro = existente.get();
            if (membro.isAtivo()) {
                throw BusinessException.conflict("Você já mora nesta casa.");
            }
            membro.setStatus(StatusMembro.ATIVO);
            membro.setDataEntrada(agora);
            membro.setDataSaida(null);
        } else {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> BusinessException.unauthorized("Usuário não encontrado."));
            membro = new Membro();
            membro.setUsuario(usuario);
            membro.setCasa(casa);
            membro.setStatus(StatusMembro.ATIVO);
            membro.setDataEntrada(agora);
        }
        membroRepository.saveAndFlush(membro);

        convite.setUsadoPor(membro);
        convite.setDataUso(agora);

        return CasaDto.Response.de(casa, usuarioId, membro.getId());
    }

    /** Convite válido: status ATIVO, validade no futuro e ainda não usado. */
    private boolean conviteValido(Convite convite, LocalDateTime agora) {
        return convite.getStatus() == StatusConvite.ATIVO
                && convite.getUsadoPor() == null
                && convite.getValidade().isAfter(agora);
    }

    private String gerarCodigoUnico() {
        for (int i = 0; i < MAX_TENTATIVAS_CODIGO; i++) {
            StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
            for (int j = 0; j < TAMANHO_CODIGO; j++) {
                sb.append(ALFABETO.charAt(secureRandom.nextInt(ALFABETO.length())));
            }
            String codigo = sb.toString();
            if (!conviteRepository.existsByCodigo(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de convite único.");
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
