package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class CalculadoraProventoFii {

    public BigDecimal calcular(
        BigDecimal quantidadeCotas,
        BigDecimal proventoPorCota
    ) {
        validarValorPositivo(quantidadeCotas, "A quantidade de cotas");
        validarValorPositivo(proventoPorCota, "O provento por cota");

        return quantidadeCotas
            .multiply(proventoPorCota)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private void validarValorPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                campo + " deve ser maior que zero."
            );
        }
    }
}
