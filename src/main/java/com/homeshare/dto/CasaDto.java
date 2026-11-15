package com.homeshare.dto;

import com.homeshare.entity.Casa;
import com.homeshare.enums.StatusCasa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class CasaDto {

    private CasaDto() {
    }

    public record Request(
            @NotBlank(message = "O nome da casa é obrigatório")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
            String nome) {
    }

    public record TransferirLiderancaRequest(
            @NotNull(message = "Informe o morador que será o novo líder")
            Long novoLiderMembroId) {
    }

    public record Response(
            Long id,
            String nome,
            StatusCasa status,
            LocalDateTime dataCriacao,
            Long liderUsuarioId,
            String liderNome,
            boolean souLider,
            Long meuMembroId) {

        public static Response de(Casa casa, Long usuarioLogadoId, Long meuMembroId) {
            Long liderId = casa.getLider().getId();
            return new Response(casa.getId(), casa.getNome(), casa.getStatus(), casa.getDataCriacao(),
                    liderId, casa.getLider().getNome(), liderId.equals(usuarioLogadoId), meuMembroId);
        }
    }
}
