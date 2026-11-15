package com.homeshare.dto;

import com.homeshare.entity.Espaco;
import com.homeshare.enums.TipoEspaco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class EspacoDto {

    private EspacoDto() {
    }

    public record Request(
            @NotBlank(message = "O nome do espaço é obrigatório")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
            String nome,

            @NotNull(message = "O tipo do espaço é obrigatório")
            TipoEspaco tipo) {
    }

    public record Response(Long id, String nome, TipoEspaco tipo) {

        public static Response de(Espaco e) {
            return new Response(e.getId(), e.getNome(), e.getTipo());
        }
    }
}
