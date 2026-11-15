package com.homeshare.dto;

import com.homeshare.entity.Usuario;

import java.time.LocalDateTime;

public final class UsuarioDto {

    private UsuarioDto() {
    }

    public record Response(Long id, String nome, String email, LocalDateTime dataCadastro) {

        public static Response de(Usuario u) {
            return new Response(u.getId(), u.getNome(), u.getEmail(), u.getDataCadastro());
        }
    }
}
