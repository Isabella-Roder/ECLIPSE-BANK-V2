package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusCartao;
import com.eclipsebank.backend.exception.ConflitoException;

public class CartaoTest {
    
    @Test
    void deveBloquearCartaoAtivo() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.ATIVO);

        cartao.bloquear();

        assertEquals(StatusCartao.BLOQUEADO, cartao.getStatus());
    }

    @Test
    void deveRecusarBloquearCartaoJaBloqueado() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.BLOQUEADO);

        assertThrows(ConflitoException.class, () -> cartao.bloquear());
    }

    @Test
    void deveRecusarBloquearCartaoJaCancelado() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.CANCELADO);

        assertThrows(ConflitoException.class, () -> cartao.bloquear());
    }

    @Test
    void deveDesbloquearCartaoBloqueado() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.BLOQUEADO);

        cartao.desbloquear();

        assertEquals(StatusCartao.ATIVO, cartao.getStatus());
    }

    @Test
    void deveRecusarDesbloquearCartaoJaAtivo() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.ATIVO);

        assertThrows(ConflitoException.class, () -> cartao.desbloquear());
    }

    @Test
    void deveRecusarDesbloquearCartaoJaCancelado() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.CANCELADO);

        assertThrows(ConflitoException.class, () -> cartao.desbloquear());
    }

    @Test
    void deveCancelarCartaoAtivo() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.ATIVO);

        cartao.cancelar();

        assertEquals(StatusCartao.CANCELADO, cartao.getStatus());
    }

    @Test
    void deveCancelarCartaoBloqueado() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.BLOQUEADO);

        cartao.cancelar();

        assertEquals(StatusCartao.CANCELADO, cartao.getStatus());
    }

    @Test
    void deveRecusarCancecalarCartaoJaCancelado() {
        Cartao cartao = new Cartao();
        cartao.setStatus(StatusCartao.CANCELADO);

        assertThrows(ConflitoException.class, () -> cartao.cancelar());
    }
}
