package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class ContaTest {
    
    @Test
    void deveCreditarValorNoSaldo() {
        Conta conta = new Conta();

        conta.creditar(new BigDecimal("150.00"));

        assertEquals(
            new BigDecimal("150.00"),
            conta.getSaldo()
        );
    }

    @Test
    void deveDebitarValorDoSaldo() {
        Conta conta = new Conta();

        conta.creditar(new BigDecimal("200.00"));
        conta.debitar(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), conta.getSaldo());
    }

}
