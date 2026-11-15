package com.homeshare.dto;

import com.homeshare.entity.Contato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ContatoDto {

    private ContatoDto() {
    }

    public record Request(
            @NotBlank(message = "O nome é obrigatório")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
            String nome,

            @NotBlank(message = "A função é obrigatória (ex.: encanador, dono da casa)")
            @Size(max = 60, message = "A função deve ter no máximo 60 caracteres")
            String funcao,

            @NotBlank(message = "O telefone é obrigatório")
            @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
            @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$", message = "Telefone inválido")
            String telefone) {
    }

    public record Response(Long id, String nome, String funcao, String telefone) {

        public static Response de(Contato c) {
            return new Response(c.getId(), c.getNome(), c.getFuncao(), c.getTelefone());
        }
    }
}
