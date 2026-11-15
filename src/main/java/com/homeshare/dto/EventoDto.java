package com.homeshare.dto;

import com.homeshare.entity.Evento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public final class EventoDto {

    private EventoDto() {
    }

    public record Request(
            @NotBlank(message = "O título é obrigatório")
            @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
            String titulo,

            @NotNull(message = "A data é obrigatória")
            LocalDate data,

            @NotNull(message = "O horário é obrigatório")
            LocalTime horario,

            @NotBlank(message = "A descrição é obrigatória")
            @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
            String descricao) {
    }

    public record Response(
            Long id,
            String titulo,
            LocalDate data,
            LocalTime horario,
            String descricao,
            MembroDto.Resumo criadoPor) {

        public static Response de(Evento e) {
            return new Response(e.getId(), e.getTitulo(), e.getData(), e.getHorario(), e.getDescricao(),
                    MembroDto.Resumo.de(e.getCriadoPor()));
        }
    }
}
