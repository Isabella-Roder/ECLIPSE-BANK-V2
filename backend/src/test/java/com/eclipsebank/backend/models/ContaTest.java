package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusConta;
import com.eclipsebank.backend.exception.ConflitoException;
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

    @Test
    void deveBloquearContaAtiva() {
        Conta conta = new Conta();

        conta.bloquear();

        assertEquals(StatusConta.BLOQUEADA, conta.getStatus());
    }

    @Test
    void deveDesbloquearContaBloqueada() {
        Conta conta = new Conta();
        conta.bloquear();

        conta.desbloquear();

        assertEquals(StatusConta.ATIVA, conta.getStatus());
    }

    @Test
    void deveEncerrarContaComSaldoZerado() {
        Conta conta = new Conta();

        conta.encerrar();

        assertEquals(StatusConta.ENCERRADA, conta.getStatus());
    }

    @Test
    void deveRecusarEncerramentoQuandoHouverSaldo() {
        Conta conta = new Conta();
        conta.creditar(new BigDecimal("100.00"));

        assertThrows(ConflitoException.class, conta::encerrar);

        assertEquals(StatusConta.ATIVA, conta.getStatus());
    }

    @Test
    void deveRecusarDesbloqueioDeContaEncerrada() {
        Conta conta = new Conta();
        conta.encerrar();

        assertThrows(ConflitoException.class, conta::desbloquear);

        assertEquals(StatusConta.ENCERRADA, conta.getStatus());
    }

}
