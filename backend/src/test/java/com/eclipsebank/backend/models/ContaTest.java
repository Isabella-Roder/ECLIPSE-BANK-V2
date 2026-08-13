package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusConta;
import com.eclipsebank.backend.exception.ContaIndisponivelException;
import com.eclipsebank.backend.exception.SaldoInsuficienteException;

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

    @Test
    void deveRecusarDebitoQuandoSaldoForInsuficiente() {
        Conta conta = new Conta();

        conta.creditar(new BigDecimal("50.00"));

        assertThrows(SaldoInsuficienteException.class, () -> conta.debitar(new BigDecimal("100.00")));

        assertEquals(new BigDecimal("50.00"), conta.getSaldo());
    }

    @Test
    void deveRecusarCreditoComValorZero() {
        Conta conta = new Conta();

        assertThrows(IllegalArgumentException.class, () -> conta.creditar(BigDecimal.ZERO));
    }

    @Test
    void deveRecusarDebitoComValorNegativo() {
        Conta conta = new Conta();

        assertThrows(IllegalArgumentException.class, () -> conta.debitar(new BigDecimal("-10.00")));

        assertEquals(BigDecimal.ZERO, conta.getSaldo());
    }

    @Test
    void deveRecusarCreditoComValorNulo() {
        Conta conta = new Conta();

        assertThrows(IllegalArgumentException.class, () -> conta.creditar(null));

        assertEquals(BigDecimal.ZERO, conta.getSaldo());
    }

    @Test
    void deveRecusarCreditoQuandoContaEstiverBloqueada() {
        Conta conta = new Conta();
        conta.setStatus(StatusConta.BLOQUEADA);

        assertThrows(ContaIndisponivelException.class, () -> conta.creditar(new BigDecimal("100.00")));

        assertEquals(BigDecimal.ZERO, conta.getSaldo());
    }

    @Test
    void deveRecusarDebitoQuandoContaEstiverBloqueada() {
        Conta conta = new Conta();
        conta.creditar(new BigDecimal("100.00"));
        conta.setStatus(StatusConta.BLOQUEADA);

        assertThrows(ContaIndisponivelException.class, () -> conta.debitar(new BigDecimal("50.00")));

        assertEquals(new BigDecimal("100.00"), conta.getSaldo());
    }

}
