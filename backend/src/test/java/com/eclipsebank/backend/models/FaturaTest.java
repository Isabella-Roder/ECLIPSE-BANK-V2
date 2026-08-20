package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusFatura;
import com.eclipsebank.backend.exception.ConflitoException;

public class FaturaTest {
    
    @Test
    void deveLancarCompraFaturaAtiva() {
        Fatura fatura = new Fatura();
        fatura.setStatus(StatusFatura.ABERTA);

        fatura.lancarCompra(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), fatura.getValorTotal());
    }

    @Test
    void deveRecusarLancarCompraFaturaNaoAtiva() {
        Fatura fatura = new Fatura();
        fatura.setStatus(StatusFatura.FECHADA);

        assertThrows(ConflitoException.class, () -> fatura.lancarCompra(new BigDecimal("100.00")));
    }

    @Test
    void deveRecusarLancarCompraValorNulo() {
        Fatura fatura = new Fatura();

        assertThrows(IllegalArgumentException.class, () -> fatura.lancarCompra(null));
    }

    @Test
    void devaRecusarLancarCompraValorZero() {
        Fatura fatura = new Fatura();

        assertThrows(IllegalArgumentException.class, () -> fatura.lancarCompra(new BigDecimal("0.0")));
    }

    @Test
    void deveRecusarLancarCompraValorNegativo() {
        Fatura fatura = new Fatura();

        assertThrows(IllegalArgumentException.class, () -> fatura.lancarCompra(new BigDecimal("-100.00")));
    }

    @Test
    void deveFecharFaturaAtiva() {
        Fatura fatura = new Fatura();
        fatura.setStatus(StatusFatura.ABERTA);

        fatura.fechar();

        assertEquals(StatusFatura.FECHADA, fatura.getStatus());
    }

    @Test
    void deveRecusarFecharFatunaNaoAtiva() {
        Fatura fatura = new Fatura();
        fatura.setStatus(StatusFatura.PAGA);

        assertThrows(ConflitoException.class, () -> fatura.fechar());
    }

    @Test
    void devePagarFaturaFechada() {
        Fatura fatura = new Fatura();
        fatura.setStatus(StatusFatura.FECHADA);

        fatura.pagar();

        assertEquals(StatusFatura.PAGA, fatura.getStatus());
    }

    @Test
    void deveRecusarPagarFaturaNaoFechada() {
        Fatura fatura = new Fatura();
        fatura.setStatus(StatusFatura.ABERTA);

        assertThrows(ConflitoException.class, () -> fatura.pagar());
    }
}
