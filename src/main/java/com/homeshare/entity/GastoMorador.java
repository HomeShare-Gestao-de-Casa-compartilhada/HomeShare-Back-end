package com.homeshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Parcela de um gasto fixo atribuída a um morador. */
@Entity
@Table(name = "gasto_morador", uniqueConstraints = @UniqueConstraint(
        name = "uq_gasto_morador", columnNames = {"gasto_fixo_id", "membro_id"}))
@Getter
@Setter
@NoArgsConstructor
public class GastoMorador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gasto_fixo_id", nullable = false)
    private GastoFixo gastoFixo;

    @Column(name = "valor_parcela", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorParcela;

    @Column(name = "pago", nullable = false)
    private boolean pago;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
}
