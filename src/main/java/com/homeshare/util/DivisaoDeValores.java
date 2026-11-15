package com.homeshare.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class DivisaoDeValores {

    private DivisaoDeValores() {
    }

    /**
     * Divide um valor em partes iguais, em centavos, sem perder nem criar dinheiro.
     * Os centavos que sobram vão um a um para as primeiras partes.
     * Ex.: 100,00 / 3 -> [33,34 ; 33,33 ; 33,33]  (a soma é sempre exatamente o total).
     */
    public static List<BigDecimal> dividirIgualmente(BigDecimal total, int partes) {
        if (partes <= 0) {
            throw new IllegalArgumentException("O número de partes deve ser positivo.");
        }
        long centavos = total.movePointRight(2).longValueExact();
        long base = centavos / partes;
        long resto = centavos % partes;

        List<BigDecimal> resultado = new ArrayList<>(partes);
        for (int i = 0; i < partes; i++) {
            long parte = base + (i < resto ? 1 : 0);
            resultado.add(BigDecimal.valueOf(parte, 2));
        }
        return resultado;
    }
}
