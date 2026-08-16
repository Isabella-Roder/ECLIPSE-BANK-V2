package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CalculadoraProventoFiiTest {

    @Test
    void deveCalcularProventoMensalDoFundoImobiliario() {
        CalculadoraProventoFii calculadora = new CalculadoraProventoFii();

        BigDecimal provento = calculadora.calcular(
            new BigDecimal("10.000000"),
            new BigDecimal("0.80")
        );

        assertEquals(new BigDecimal("8.00"), provento);
    }
}
