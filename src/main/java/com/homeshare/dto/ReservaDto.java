package com.homeshare.dto;

import com.homeshare.entity.Reserva;
import com.homeshare.enums.StatusReserva;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class ReservaDto {

    private ReservaDto() {
    }

    public record Request(
            @NotNull(message = "O início é obrigatório")
            LocalDateTime inicio,

            @NotNull(message = "O fim é obrigatório")
            LocalDateTime fim) {
    }

    public record Response(
            Long id,
            Long espacoId,
            String espacoNome,
            MembroDto.Resumo reservadoPor,
            LocalDateTime inicio,
            LocalDateTime fim,
            StatusReserva status) {

        public static Response de(Reserva r) {
            return new Response(r.getId(), r.getEspaco().getId(), r.getEspaco().getNome(),
                    MembroDto.Resumo.de(r.getReservadoPor()), r.getInicio(), r.getFim(), r.getStatus());
        }
    }
}
