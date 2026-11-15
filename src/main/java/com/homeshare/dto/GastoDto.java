package com.homeshare.dto;

import com.homeshare.entity.GastoFixo;
import com.homeshare.entity.GastoMorador;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class GastoDto {

    private GastoDto() {
    }

    public record ParcelaRequest(
            @NotNull(message = "Informe o morador da parcela")
            Long membroId,

            @NotNull(message = "Informe o valor da parcela")
            @DecimalMin(value = "0.00", message = "O valor da parcela não pode ser negativo")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido (máximo 2 casas decimais)")
            BigDecimal valor) {
    }

    /**
     * parcelas é opcional: se omitida, o valor total é dividido igualmente entre
     * todos os moradores ativos (os centavos que sobram vão para os primeiros).
     * mesReferencia: qualquer dia do mês; é guardado como dia 1.
     */
    public record CriarRequest(
            @NotBlank(message = "A descrição é obrigatória")
            @Size(max = 150, message = "A descrição deve ter no máximo 150 caracteres")
            String descricao,

            @NotNull(message = "O valor total é obrigatório")
            @DecimalMin(value = "0.01", message = "O valor total deve ser maior que zero")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido (máximo 2 casas decimais)")
            BigDecimal valorTotal,

            @NotNull(message = "O mês de referência é obrigatório")
            LocalDate mesReferencia,

            @Valid
            List<ParcelaRequest> parcelas) {
    }

    public record PagamentoRequest(
            @NotNull(message = "Informe se a parcela foi paga")
            Boolean pago) {
    }

    public record ParcelaResponse(
            Long id,
            MembroDto.Resumo membro,
            BigDecimal valor,
            boolean pago,
            LocalDateTime dataPagamento) {

        public static ParcelaResponse de(GastoMorador p) {
            return new ParcelaResponse(p.getId(), MembroDto.Resumo.de(p.getMembro()), p.getValorParcela(),
                    p.isPago(), p.getDataPagamento());
        }
    }

    public record Response(
            Long id,
            String descricao,
            BigDecimal valorTotal,
            LocalDate mesReferencia,
            BigDecimal totalPago,
            List<ParcelaResponse> parcelas) {

        public static Response de(GastoFixo g) {
            BigDecimal totalPago = g.getParcelas().stream()
                    .filter(GastoMorador::isPago)
                    .map(GastoMorador::getValorParcela)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            List<ParcelaResponse> parcelas = g.getParcelas().stream()
                    .map(ParcelaResponse::de)
                    .toList();
            return new Response(g.getId(), g.getDescricao(), g.getValorTotal(), g.getMesReferencia(),
                    totalPago, parcelas);
        }
    }

    /** Visão "minhas contas": uma linha por parcela do usuário logado. */
    public record MinhaParcelaResponse(
            Long parcelaId,
            Long gastoId,
            String descricao,
            LocalDate mesReferencia,
            BigDecimal valor,
            boolean pago,
            LocalDateTime dataPagamento) {

        public static MinhaParcelaResponse de(GastoMorador p) {
            GastoFixo g = p.getGastoFixo();
            return new MinhaParcelaResponse(p.getId(), g.getId(), g.getDescricao(), g.getMesReferencia(),
                    p.getValorParcela(), p.isPago(), p.getDataPagamento());
        }
    }
}
