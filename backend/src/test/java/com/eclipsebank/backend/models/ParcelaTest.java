package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusParcela;
import com.eclipsebank.backend.exception.ConflitoException;

public class ParcelaTest {
    
    @Test
    void devePagarQuandoStatusPENDENTE() {
        Parcela parcela = new Parcela();
        parcela.setStatus(StatusParcela.PENDENTE);

        parcela.pagar();

        assertEquals(StatusParcela.PAGA, parcela.getStatus());
        assertNotNull(parcela.getDataPagamento());
    }

    @Test
    void deveRecusarPagarQuandoStatusNaoPENDENTE() {
        Parcela parcela = new Parcela();
        parcela.setStatus(StatusParcela.PAGA);

        assertThrows(ConflitoException.class, () -> parcela.pagar());
    }
}
