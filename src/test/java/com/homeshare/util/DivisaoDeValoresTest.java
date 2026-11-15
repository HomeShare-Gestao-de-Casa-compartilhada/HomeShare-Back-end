package com.homeshare.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivisaoDeValoresTest {

    @Test
    void divideSemPerderCentavos() {
        List<BigDecimal> partes = DivisaoDeValores.dividirIgualmente(new BigDecimal("100.00"), 3);

        assertThat(partes).containsExactly(
                new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33"));
        assertThat(partes.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void divisaoExataMantemPartesIguais() {
        List<BigDecimal> partes = DivisaoDeValores.dividirIgualmente(new BigDecimal("90.00"), 3);

        assertThat(partes).allMatch(p -> p.compareTo(new BigDecimal("30.00")) == 0);
    }

    @Test
    void umaParteRecebeOTotal() {
        assertThat(DivisaoDeValores.dividirIgualmente(new BigDecimal("0.01"), 1))
                .containsExactly(new BigDecimal("0.01"));
    }

    @Test
    void rejeitaZeroPartes() {
        assertThatThrownBy(() -> DivisaoDeValores.dividirIgualmente(new BigDecimal("10.00"), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
