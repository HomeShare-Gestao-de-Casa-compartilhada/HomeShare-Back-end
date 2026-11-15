package com.homeshare.dto;

import com.homeshare.entity.ItemFaltando;
import com.homeshare.enums.StatusItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ItemFaltandoDto {

    private ItemFaltandoDto() {
    }

    public record Request(
            @NotBlank(message = "O nome do item é obrigatório")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
            String nome,

            @Positive(message = "A quantidade deve ser maior que zero")
            Integer quantidade) {
    }

    public record Response(
            Long id,
            String nome,
            Integer quantidade,
            StatusItem status,
            MembroDto.Resumo adicionadoPor,
            MembroDto.Resumo compradoPor,
            LocalDateTime dataCompra) {

        public static Response de(ItemFaltando i) {
            return new Response(i.getId(), i.getNome(), i.getQuantidade(), i.getStatus(),
                    MembroDto.Resumo.de(i.getAdicionadoPor()), MembroDto.Resumo.de(i.getCompradoPor()),
                    i.getDataCompra());
        }
    }
}
