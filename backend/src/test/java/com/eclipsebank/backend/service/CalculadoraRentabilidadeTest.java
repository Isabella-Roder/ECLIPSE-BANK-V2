package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CalculadoraRentabilidadeTest {
    
    @Test
    void deveCalcularRentabilidadeDeUmAnoCompleto() {
        CalculadoraRentabilidade calculadora = new CalculadoraRentabilidade();

        BigDecimal saldoAtual = calculadora.calcularSaldoAtual(new BigDecimal("1000.00"), new BigDecimal("12.00"), 365);

        assertEquals(new BigDecimal("1120.00"), saldoAtual);
    }

    @Test
    void deveCalcularRentabilidadeProporcionalAosDias() {
        CalculadoraRentabilidade calculadora = new CalculadoraRentabilidade();

        BigDecimal saldoAtual = calculadora.calcularSaldoAtual(new BigDecimal("1000.00"), new BigDecimal("12.00"), 30);

        assertEquals(new BigDecimal("1009.86"), saldoAtual);
    }

    @Test
    void deveRecusarQuantidadeDeDiasNegativa() {
        CalculadoraRentabilidade calculadora = new CalculadoraRentabilidade();

        assertThrows(IllegalArgumentException.class, () -> calculadora.calcularSaldoAtual(new BigDecimal("1000.00"), new BigDecimal("12.00"), -1));
    }
}
