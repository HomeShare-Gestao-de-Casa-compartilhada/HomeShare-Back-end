package com.homeshare.service;

import com.homeshare.dto.CasaDto;
import com.homeshare.dto.MembroDto;
import com.homeshare.entity.Casa;
import com.homeshare.entity.Membro;
import com.homeshare.entity.Usuario;
import com.homeshare.enums.StatusCasa;
import com.homeshare.enums.StatusMembro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.CasaRepository;
import com.homeshare.repository.MembroRepository;
import com.homeshare.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CasaService {

    private final CasaRepository casaRepository;
    private final MembroRepository membroRepository;
    private final UsuarioRepository usuarioRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    /** Quem cria a casa vira líder e primeiro morador automaticamente. */
    @Transactional
    public CasaDto.Response criar(Long usuarioId, CasaDto.Request req) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> BusinessException.unauthorized("Usuário não encontrado."));
        LocalDateTime agora = LocalDateTime.now(clock);

        Casa casa = new Casa();
        casa.setNome(req.nome().trim());
        casa.setCriador(usuario);
        casa.setLider(usuario);
        casa.setStatus(StatusCasa.ATIVA);
        casa.setDataCriacao(agora);
        casaRepository.save(casa);

        Membro membro = new Membro();
        membro.setUsuario(usuario);
        membro.setCasa(casa);
        membro.setStatus(StatusMembro.ATIVO);
        membro.setDataEntrada(agora);
        membroRepository.save(membro);

        return CasaDto.Response.de(casa, usuarioId, membro.getId());
    }

    /** Uma pessoa pode morar em mais de uma casa: lista todas as casas ativas dela. */
    @Transactional(readOnly = true)
    public List<CasaDto.Response> minhasCasas(Long usuarioId) {
        return membroRepository.findCasasDoUsuario(usuarioId, StatusMembro.ATIVO, StatusCasa.ATIVA).stream()
                .map(m -> CasaDto.Response.de(m.getCasa(), usuarioId, m.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CasaDto.Response buscar(Long casaId, Long usuarioId) {
        Membro membro = acesso.exigirMembroAtivo(casaId, usuarioId);
        return CasaDto.Response.de(membro.getCasa(), usuarioId, membro.getId());
    }

    @Transactional
    public CasaDto.Response renomear(Long casaId, Long usuarioId, CasaDto.Request req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);
        Casa casa = lider.getCasa();
        casa.setNome(req.nome().trim());
        return CasaDto.Response.de(casa, usuarioId, lider.getId());
    }

    @Transactional(readOnly = true)
    public List<MembroDto.Response> listarMembros(Long casaId, Long usuarioId) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        Long liderUsuarioId = ator.getCasa().getLider().getId();
        return membroRepository.findByCasaIdAndStatusOrderByDataEntradaAscIdAsc(casaId, StatusMembro.ATIVO).stream()
                .map(m -> MembroDto.Response.de(m, liderUsuarioId))
                .toList();
    }

    /**
     * Líder expulsa um morador. O morador perde o acesso na hora (status EXPULSO),
     * mas o histórico dele continua salvo (nada é apagado).
     */
    @Transactional
    public void expulsar(Long casaId, Long usuarioId, Long membroId) {
        acesso.exigirLider(casaId, usuarioId);
        Membro alvo = acesso.buscarMembroDaCasa(membroId, casaId);

        if (alvo.getUsuario().getId().equals(usuarioId)) {
            throw BusinessException.unprocessable(
                    "O líder não pode expulsar a si mesmo. Passe a liderança para outro morador antes de sair.");
        }
        if (alvo.getStatus() != StatusMembro.ATIVO) {
            throw BusinessException.unprocessable("Este morador já não faz parte da casa.");
        }
        alvo.setStatus(StatusMembro.EXPULSO);
        alvo.setDataSaida(LocalDateTime.now(clock));
    }

    /**
     * Morador sai da casa. Se quem sai é o líder, o morador ativo mais antigo assume;
     * se não sobrar ninguém, a casa é encerrada.
     */
    @Transactional
    public void sair(Long casaId, Long usuarioId) {
        // Trava a casa primeiro: evita corrida com troca de liderança ou outra saída simultânea.
        Casa casa = casaRepository.findByIdParaAtualizar(casaId)
                .orElseThrow(() -> BusinessException.notFound("Casa não encontrada."));
        Membro membro = acesso.exigirMembroAtivo(casaId, usuarioId);

        membro.setStatus(StatusMembro.SAIU);
        membro.setDataSaida(LocalDateTime.now(clock));

        if (casa.getLider().getId().equals(usuarioId)) {
            Optional<Membro> sucessor = membroRepository
                    .findFirstByCasaIdAndStatusAndUsuarioIdNotOrderByDataEntradaAscIdAsc(
                            casaId, StatusMembro.ATIVO, usuarioId);
            if (sucessor.isPresent()) {
                casa.setLider(sucessor.get().getUsuario());
            } else {
                casa.setStatus(StatusCasa.ENCERRADA);
            }
        }
    }

    /** O líder só pode ser trocado por ele mesmo (ou quando ele sai da casa). */
    @Transactional
    public CasaDto.Response transferirLideranca(Long casaId, Long usuarioId, Long novoLiderMembroId) {
        Casa casa = casaRepository.findByIdParaAtualizar(casaId)
                .orElseThrow(() -> BusinessException.notFound("Casa não encontrada."));
        Membro ator = acesso.exigirLider(casaId, usuarioId);

        Membro novoLider = acesso.buscarMembroDaCasa(novoLiderMembroId, casaId);
        if (novoLider.getStatus() != StatusMembro.ATIVO) {
            throw BusinessException.unprocessable("O novo líder precisa ser um morador ativo da casa.");
        }
        if (novoLider.getUsuario().getId().equals(usuarioId)) {
            throw BusinessException.unprocessable("Você já é o líder desta casa.");
        }
        casa.setLider(novoLider.getUsuario());
        return CasaDto.Response.de(casa, usuarioId, ator.getId());
    }
}
