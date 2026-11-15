package com.homeshare.service;

import com.homeshare.dto.AvisoDto;
import com.homeshare.entity.Aviso;
import com.homeshare.entity.Membro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.AvisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Quadro de avisos: o líder publica, todos leem. */
@Service
@RequiredArgsConstructor
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    @Transactional
    public AvisoDto.Response publicar(Long casaId, Long usuarioId, AvisoDto.Request req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);
        Aviso aviso = new Aviso();
        aviso.setRecado(req.recado().trim());
        aviso.setDataPublicacao(LocalDateTime.now(clock));
        aviso.setAutor(lider);
        aviso.setCasa(lider.getCasa());
        avisoRepository.save(aviso);
        return AvisoDto.Response.de(aviso);
    }

    @Transactional(readOnly = true)
    public List<AvisoDto.Response> listar(Long casaId, Long usuarioId) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        return avisoRepository.findByCasaIdOrderByDataPublicacaoDescIdDesc(casaId).stream()
                .map(AvisoDto.Response::de)
                .toList();
    }

    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long avisoId) {
        acesso.exigirLider(casaId, usuarioId);
        Aviso aviso = avisoRepository.findByIdAndCasaId(avisoId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Aviso não encontrado."));
        avisoRepository.delete(aviso);
    }
}
