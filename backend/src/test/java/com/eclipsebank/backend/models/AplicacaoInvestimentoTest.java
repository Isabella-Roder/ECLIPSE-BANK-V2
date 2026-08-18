package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.exception.ConflitoException;

public class AplicacaoInvestimentoTest {

    @Test
    void deveRecusarResgateQuandoValorForMaiorQueSaldoInvestido() {
        AplicacaoInvestimento aplicacao = new AplicacaoInvestimento();
        aplicacao.aplicar(new BigDecimal("100.00"));

        assertThrows(ConflitoException.class, () -> aplicacao.resgatar(new BigDecimal("200.00")));
    }

    @Test
    void deveRecusarResgateDeAplicacaoJaResgatada() {
        AplicacaoInvestimento aplicacao = new AplicacaoInvestimento();
        aplicacao.aplicar(new BigDecimal("100.00"));
        aplicacao.resgatar(new BigDecimal("100.00"));

        assertThrows(ConflitoException.class, () -> aplicacao.resgatar(new BigDecimal("10.00")));
    }

    @Test
    void deveRecusarSegundaAplicacaoNaMesmaInstancia() {
        AplicacaoInvestimento aplicacao = new AplicacaoInvestimento();
        aplicacao.aplicar(new BigDecimal("100.00"));

        assertThrows(ConflitoException.class, () -> aplicacao.aplicar(new BigDecimal("50.00")));
    }

    @Test
    void deveRecusarResgateComValorNuloOuNaoPositivo() {
        AplicacaoInvestimento aplicacao = new AplicacaoInvestimento();
        aplicacao.aplicar(new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> aplicacao.resgatar(null));
        assertThrows(IllegalArgumentException.class, () -> aplicacao.resgatar(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> aplicacao.resgatar(new BigDecimal("-10.00")));
    }
}
