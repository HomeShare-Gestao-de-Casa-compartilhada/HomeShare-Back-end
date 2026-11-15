package com.homeshare.service;

import com.homeshare.dto.EspacoDto;
import com.homeshare.entity.Espaco;
import com.homeshare.entity.Membro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.EspacoRepository;
import com.homeshare.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacoService {

    private final EspacoRepository espacoRepository;
    private final ReservaRepository reservaRepository;
    private final CasaAcessoService acesso;

    @Transactional
    public EspacoDto.Response criar(Long casaId, Long usuarioId, EspacoDto.Request req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);
        Espaco espaco = new Espaco();
        espaco.setNome(req.nome().trim());
        espaco.setTipo(req.tipo());
        espaco.setCasa(lider.getCasa());
        espacoRepository.save(espaco);
        return EspacoDto.Response.de(espaco);
    }

    @Transactional(readOnly = true)
    public List<EspacoDto.Response> listar(Long casaId, Long usuarioId) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        return espacoRepository.findByCasaIdOrderByNomeAsc(casaId).stream()
                .map(EspacoDto.Response::de)
                .toList();
    }

    @Transactional
    public EspacoDto.Response atualizar(Long casaId, Long usuarioId, Long espacoId, EspacoDto.Request req) {
        acesso.exigirLider(casaId, usuarioId);
        Espaco espaco = buscar(espacoId, casaId);
        espaco.setNome(req.nome().trim());
        espaco.setTipo(req.tipo());
        return EspacoDto.Response.de(espaco);
    }

    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long espacoId) {
        acesso.exigirLider(casaId, usuarioId);
        Espaco espaco = buscar(espacoId, casaId);
        if (reservaRepository.existsByEspacoId(espacoId)) {
            throw BusinessException.conflict(
                    "Este espaço possui reservas registradas e não pode ser removido. Renomeie-o, se preferir.");
        }
        espacoRepository.delete(espaco);
    }

    private Espaco buscar(Long espacoId, Long casaId) {
        return espacoRepository.findByIdAndCasaId(espacoId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Espaço não encontrado."));
    }
}
