package com.homeshare.service;

import com.homeshare.entity.Membro;
import com.homeshare.enums.StatusCasa;
import com.homeshare.enums.StatusMembro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Porteiro da aplicação: toda ação dentro de uma casa passa por aqui.
 * Garante a regra "o morador só vê a casa em que ele mora" e as permissões do líder.
 */
@Service
@RequiredArgsConstructor
public class CasaAcessoService {

    private final MembroRepository membroRepository;

    /** Retorna o vínculo do usuário com a casa, desde que ele seja morador ATIVO de uma casa ATIVA. */
    @Transactional(readOnly = true)
    public Membro exigirMembroAtivo(Long casaId, Long usuarioId) {
        Membro membro = membroRepository.findByCasaIdAndUsuarioId(casaId, usuarioId)
                .filter(Membro::isAtivo)
                .orElseThrow(() -> BusinessException.forbidden("Você não é morador ativo desta casa."));
        if (membro.getCasa().getStatus() != StatusCasa.ATIVA) {
            throw BusinessException.forbidden("Esta casa foi encerrada.");
        }
        return membro;
    }

    /** Igual ao anterior, mas exige que o usuário seja o líder da casa. */
    @Transactional(readOnly = true)
    public Membro exigirLider(Long casaId, Long usuarioId) {
        Membro membro = exigirMembroAtivo(casaId, usuarioId);
        if (!ehLider(membro)) {
            throw BusinessException.forbidden("Apenas o líder da casa pode realizar esta ação.");
        }
        return membro;
    }

    public boolean ehLider(Membro membro) {
        return membro.getCasa().getLider().getId().equals(membro.getUsuario().getId());
    }

    /** Busca um membro (de qualquer status) pelo id, garantindo que ele pertence à casa. */
    @Transactional(readOnly = true)
    public Membro buscarMembroDaCasa(Long membroId, Long casaId) {
        return membroRepository.findByIdAndCasaId(membroId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Morador não encontrado nesta casa."));
    }

    /** Busca um membro que precisa estar ATIVO na casa (ex.: responsável por tarefa, dono de parcela). */
    @Transactional(readOnly = true)
    public Membro buscarMembroAtivoDaCasa(Long membroId, Long casaId) {
        Membro membro = buscarMembroDaCasa(membroId, casaId);
        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw BusinessException.unprocessable("O morador informado não está mais ativo nesta casa.");
        }
        return membro;
    }
}
