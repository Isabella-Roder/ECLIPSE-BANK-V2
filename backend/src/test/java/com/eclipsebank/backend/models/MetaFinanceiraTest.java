package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusMetaFinanceira;
import com.eclipsebank.backend.exception.ConflitoException;

public class MetaFinanceiraTest {
    
    @Test
    void deveConcluirMetaQuandoValorAtualAtingirValorAlvo() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        meta.aportar(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), meta.getValorAtual());
        assertEquals(StatusMetaFinanceira.CONCLUIDA, meta.getStatus());
        assertNotNull(meta.getConcluidaEm());
    }

    @Test
    void deveAumentarValorAtualSemAtingirValorAtual() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        meta.aportar(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), meta.getValorAtual());
    }

    @Test
    void deveRecusarAporteComValorZero() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        assertThrows(IllegalArgumentException.class, () -> meta.aportar(new BigDecimal("0.00")));
    }

    @Test 
    void deveRecusarAporteComValorNulo() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        assertThrows(IllegalArgumentException.class, () -> meta.aportar(null));
    }

    @Test
    void deveRecusarAporteComValorNegativo() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        assertThrows(IllegalArgumentException.class, () -> meta.aportar(new BigDecimal("-500.00")));
    }

    @Test
    void deveRecusarAporteMetaConcluida() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.CONCLUIDA);

        assertThrows(ConflitoException.class, () -> meta.aportar(new BigDecimal("500.00")));
    }

    @Test
    void deveResgatarDiminuirValorAtual() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        meta.aportar(new BigDecimal("1000.00"));

        meta.resgatar(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), meta.getValorAtual());
    }

    @Test
    void deveRecusarResgatarMaisDoQueValorAtual() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        meta.aportar(new BigDecimal("1000.00"));

        assertThrows(ConflitoException.class, () -> meta.resgatar(new BigDecimal("1500.00")));
    }

    @Test
    void deveRecusarResgatarValorComNulo() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        assertThrows(IllegalArgumentException.class, () -> meta.resgatar(null));
    }

    @Test
    void deveRecusarResgatarValorComZero() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        assertThrows(IllegalArgumentException.class, () -> meta.resgatar(new BigDecimal("0.00")));
    }

    @Test
    void deveRecusarResgatarValorComNegativo() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        assertThrows(IllegalArgumentException.class, () -> meta.resgatar(new BigDecimal("-500.00")));
    }

    @Test
    void deveRecusarResgatarMetaNaoEM_ANDAMENTO() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1500.00"));
        meta.setStatus(StatusMetaFinanceira.CONCLUIDA);

        assertThrows(ConflitoException.class, () -> meta.resgatar(new BigDecimal("10.00")));
    }
}
