package com.homeshare.dto;

import com.homeshare.entity.Convite;
import com.homeshare.enums.StatusConvite;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public final class ConviteDto {

    private ConviteDto() {
    }

    /** validadeHoras é opcional; se omitido, usa o padrão do application.yaml. */
    public record CriarRequest(
            @Positive(message = "A validade deve ser positiva")
            Integer validadeHoras) {
    }

    public record Response(
            Long id,
            String codigo,
            LocalDateTime validade,
            StatusConvite status,
            boolean expirado,
            boolean usado,
            boolean valido,
            LocalDateTime dataCriacao,
            MembroDto.Resumo geradoPor,
            MembroDto.Resumo usadoPor,
            LocalDateTime dataUso,
            String link) {

        public static Response de(Convite c, LocalDateTime agora, String link) {
            boolean expirado = !c.getValidade().isAfter(agora);
            boolean usado = c.getUsadoPor() != null;
            boolean valido = c.getStatus() == StatusConvite.ATIVO && !expirado && !usado;
            return new Response(c.getId(), c.getCodigo(), c.getValidade(), c.getStatus(), expirado, usado, valido,
                    c.getDataCriacao(), MembroDto.Resumo.de(c.getGeradoPor()),
                    MembroDto.Resumo.de(c.getUsadoPor()), c.getDataUso(), link);
        }
    }

    /** Pré-visualização para quem recebeu o código: mostra a qual casa o convite pertence. */
    public record PreviaResponse(String casaNome, LocalDateTime validade, boolean valido) {
    }
}
