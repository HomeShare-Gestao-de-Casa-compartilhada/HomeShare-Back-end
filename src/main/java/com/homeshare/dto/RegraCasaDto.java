package com.homeshare.dto;

import com.homeshare.entity.RegraCasa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class RegraCasaDto {

    private RegraCasaDto() {
    }

    public record Request(
            @NotBlank(message = "O título é obrigatório")
            @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
            String titulo,

            @NotBlank(message = "O texto da regra é obrigatório")
            @Size(max = 1000, message = "O texto deve ter no máximo 1000 caracteres")
            String texto) {
    }

    public record Response(Long id, String titulo, String texto, LocalDateTime dataAtualizacao) {

        public static Response de(RegraCasa r) {
            return new Response(r.getId(), r.getTitulo(), r.getTexto(), r.getDataAtualizacao());
        }
    }
}
