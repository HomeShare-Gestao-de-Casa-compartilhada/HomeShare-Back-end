package com.homeshare.service;

import com.homeshare.dto.EventoDto;
import com.homeshare.entity.Evento;
import com.homeshare.entity.Membro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private static final LocalDate DATA_MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate DATA_MAXIMA = LocalDate.of(2999, 12, 31);

    private final EventoRepository eventoRepository;
    private final CasaAcessoService acesso;

    @Transactional
    public EventoDto.Response criar(Long casaId, Long usuarioId, EventoDto.Request req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);
        Evento evento = new Evento();
        preencher(evento, req);
        evento.setCriadoPor(lider);
        evento.setCasa(lider.getCasa());
        eventoRepository.save(evento);
        return EventoDto.Response.de(evento);
    }

    /** Calendário da casa: todos os moradores veem. de/ate são opcionais. */
    @Transactional(readOnly = true)
    public List<EventoDto.Response> listar(Long casaId, Long usuarioId, LocalDate de, LocalDate ate) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        LocalDate inicio = de != null ? de : DATA_MINIMA;
        LocalDate fim = ate != null ? ate : DATA_MAXIMA;
        return eventoRepository.findByCasaIdAndDataBetweenOrderByDataAscHorarioAsc(casaId, inicio, fim).stream()
                .map(EventoDto.Response::de)
                .toList();
    }

    @Transactional
    public EventoDto.Response atualizar(Long casaId, Long usuarioId, Long eventoId, EventoDto.Request req) {
        acesso.exigirLider(casaId, usuarioId);
        Evento evento = buscar(eventoId, casaId);
        preencher(evento, req);
        return EventoDto.Response.de(evento);
    }

    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long eventoId) {
        acesso.exigirLider(casaId, usuarioId);
        eventoRepository.delete(buscar(eventoId, casaId));
    }

    private Evento buscar(Long eventoId, Long casaId) {
        return eventoRepository.findByIdAndCasaId(eventoId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Evento não encontrado."));
    }

    private void preencher(Evento evento, EventoDto.Request req) {
        evento.setTitulo(req.titulo().trim());
        evento.setData(req.data());
        evento.setHorario(req.horario());
        evento.setDescricao(req.descricao().trim());
    }
}
