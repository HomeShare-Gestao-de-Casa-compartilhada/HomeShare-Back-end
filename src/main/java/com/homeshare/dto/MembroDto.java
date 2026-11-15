package com.homeshare.dto;

import com.homeshare.entity.Membro;
import com.homeshare.enums.StatusMembro;

import java.time.LocalDateTime;

public final class MembroDto {

    private MembroDto() {
    }

    /** Versão enxuta, usada dentro de outras respostas (autor, responsável...). */
    public record Resumo(Long id, String nome) {

        public static Resumo de(Membro m) {
            if (m == null) {
                return null;
            }
            return new Resumo(m.getId(), m.getUsuario().getNome());
        }
    }

    public record Response(
            Long membroId,
            Long usuarioId,
            String nome,
            StatusMembro status,
            LocalDateTime dataEntrada,
            boolean lider) {

        public static Response de(Membro m, Long liderUsuarioId) {
            Long usuarioId = m.getUsuario().getId();
            return new Response(m.getId(), usuarioId, m.getUsuario().getNome(), m.getStatus(),
                    m.getDataEntrada(), usuarioId.equals(liderUsuarioId));
        }
    }
}
