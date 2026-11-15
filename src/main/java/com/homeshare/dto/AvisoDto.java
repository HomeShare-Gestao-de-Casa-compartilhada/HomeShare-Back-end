package com.homeshare.dto;

import com.homeshare.entity.Aviso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AvisoDto {

    private AvisoDto() {
    }

    public record Request(
            @NotBlank(message = "O recado é obrigatório")
            @Size(max = 500, message = "O recado deve ter no máximo 500 caracteres")
            String recado) {
    }

    public record Response(Long id, String recado, LocalDateTime dataPublicacao, MembroDto.Resumo autor) {

        public static Response de(Aviso a) {
            return new Response(a.getId(), a.getRecado(), a.getDataPublicacao(), MembroDto.Resumo.de(a.getAutor()));
        }
    }
}
