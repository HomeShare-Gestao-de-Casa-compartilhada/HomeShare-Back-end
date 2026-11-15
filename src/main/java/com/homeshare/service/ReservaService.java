package com.homeshare.service;

import com.homeshare.dto.ReservaDto;
import com.homeshare.entity.Espaco;
import com.homeshare.entity.Membro;
import com.homeshare.entity.Reserva;
import com.homeshare.enums.StatusReserva;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.EspacoRepository;
import com.homeshare.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private static final LocalDateTime INICIO_MINIMO = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime FIM_MAXIMO = LocalDateTime.of(2999, 12, 31, 23, 59);

    private final ReservaRepository reservaRepository;
    private final EspacoRepository espacoRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    @Transactional
    public ReservaDto.Response criar(Long casaId, Long usuarioId, Long espacoId, ReservaDto.Request req) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);

        if (!req.fim().isAfter(req.inicio())) {
            throw BusinessException.badRequest("O fim da reserva deve ser depois do início.");
        }
        if (req.inicio().isBefore(LocalDateTime.now(clock))) {
            throw BusinessException.badRequest("Não é possível reservar um horário que já passou.");
        }

        // Trava o espaço até o fim da transação: a checagem de conflito + o insert viram uma operação só.
        Espaco espaco = espacoRepository.findByIdAndCasaIdParaAtualizar(espacoId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Espaço não encontrado."));

        long conflitos = reservaRepository.contarConflitos(espacoId, StatusReserva.ATIVA, req.inicio(), req.fim());
        if (conflitos > 0) {
            throw BusinessException.conflict("Já existe uma reserva ativa para este espaço nesse horário.");
        }

        Reserva reserva = new Reserva();
        reserva.setEspaco(espaco);
        reserva.setReservadoPor(ator);
        reserva.setInicio(req.inicio());
        reserva.setFim(req.fim());
        reserva.setStatus(StatusReserva.ATIVA);
        reservaRepository.save(reserva);
        return ReservaDto.Response.de(reserva);
    }

    /** Reservas ativas da casa; filtros opcionais por espaço e período. */
    @Transactional(readOnly = true)
    public List<ReservaDto.Response> listar(Long casaId, Long usuarioId, Long espacoId,
                                            LocalDateTime de, LocalDateTime ate) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        LocalDateTime inicio = de != null ? de : INICIO_MINIMO;
        LocalDateTime fim = ate != null ? ate : FIM_MAXIMO;
        return reservaRepository.listar(casaId, StatusReserva.ATIVA, espacoId, inicio, fim).stream()
                .map(ReservaDto.Response::de)
                .toList();
    }

    /** Quem reservou ou o líder podem cancelar. */
    @Transactional
    public void cancelar(Long casaId, Long usuarioId, Long reservaId) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        Reserva reserva = reservaRepository.findByIdAndEspacoCasaId(reservaId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Reserva não encontrada."));

        boolean dono = reserva.getReservadoPor().getId().equals(ator.getId());
        if (!dono && !acesso.ehLider(ator)) {
            throw BusinessException.forbidden("Apenas quem fez a reserva ou o líder pode cancelá-la.");
        }
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw BusinessException.conflict("Esta reserva já foi cancelada.");
        }
        reserva.setStatus(StatusReserva.CANCELADA);
    }
}
