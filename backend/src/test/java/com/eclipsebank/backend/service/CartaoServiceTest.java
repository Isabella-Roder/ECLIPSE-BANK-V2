package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eclipsebank.backend.dto.CartaoCadastro;
import com.eclipsebank.backend.dto.CartaoResposta;
import com.eclipsebank.backend.enums.StatusCartao;
import com.eclipsebank.backend.enums.TipoCartao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Cartao;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.repository.CartaoRepository;
import com.eclipsebank.backend.repository.ContaRepository;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {
    
    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private ContaRepository contaRepository;

    private CartaoService cartaoService;

    @BeforeEach
    void preparar() {
        cartaoService = new CartaoService(cartaoRepository, contaRepository);
    }

    @Test
    void deveCadastrarCartaoDeDebito() {
        Conta conta = new Conta();
        CartaoCadastro dados = new CartaoCadastro(TipoCartao.DEBITO);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(cartaoRepository.save(any(Cartao.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        CartaoResposta resposta = cartaoService.cadastrar(1L, dados);

        assertEquals(TipoCartao.DEBITO, resposta.tipo());
    }

    @Test
    void deveCadastrarCartaoDeCreditoComLimite() {
        Conta conta = new Conta();
        CartaoCadastro dados = new CartaoCadastro(TipoCartao.CREDITO);

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(cartaoRepository.save(any(Cartao.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        CartaoResposta resposta = cartaoService.cadastrar(1L, dados);

        assertEquals(new BigDecimal("1000.00"), resposta.limite());
    }

    @Test
    void deveRecusarCadastroQuandoContaNaoExistir() {
        CartaoCadastro dados = new CartaoCadastro(TipoCartao.DEBITO);

        when(contaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> cartaoService.cadastrar(1L, dados));
    }

    @Test
    void deveBloquearCartaoDaConta() {
        Conta conta = mock(Conta.class);
        when(conta.getId()).thenReturn(1L);

        Cartao cartao = new Cartao();
        cartao.setConta(conta);
        cartao.setStatus(StatusCartao.ATIVO);

        when(cartaoRepository.findById(10L)).thenReturn(Optional.of(cartao));

        CartaoResposta resposta = cartaoService.bloquear(1L, 10L);

        assertEquals(StatusCartao.BLOQUEADO, resposta.status());
    }

    @Test
    void deveRecusarBloquearCartaoDeOutraConta() {
        Conta conta = mock(Conta.class);
        when(conta.getId()).thenReturn(2L);

        Cartao cartao = new Cartao();
        cartao.setConta(conta);
        cartao.setStatus(StatusCartao.ATIVO);

        when(cartaoRepository.findById(10L)).thenReturn(Optional.of(cartao));

        assertThrows(RecursoNaoEncontradoException.class, () -> cartaoService.bloquear(1L, 10L));
    }

    @Test
    void deveDesbloquearCartaoDaConta() {
        Conta conta = mock(Conta.class);
        when(conta.getId()).thenReturn(1L);

        Cartao cartao = new Cartao();
        cartao.setConta(conta);
        cartao.setStatus(StatusCartao.BLOQUEADO);

        when(cartaoRepository.findById(10L)).thenReturn(Optional.of(cartao));

        CartaoResposta resposta = cartaoService.desbloquear(1L, 10L);

        assertEquals(StatusCartao.ATIVO, resposta.status());
    }

    @Test
    void deveCancelarCartaoDaConta() {
        Conta conta = mock(Conta.class);
        when(conta.getId()).thenReturn(1L);

        Cartao cartao = new Cartao();
        cartao.setConta(conta);
        cartao.setStatus(StatusCartao.ATIVO);

        when(cartaoRepository.findById(10L)).thenReturn(Optional.of(cartao));

        CartaoResposta resposta = cartaoService.cancelar(1L, 10L);

        assertEquals(StatusCartao.CANCELADO, resposta.status());
    }

    @Test
    void deveListarCartoesDaConta() {
        Conta conta = mock(Conta.class);
        when(conta.getId()).thenReturn(1L);

        Cartao cartao = new Cartao();
        cartao.setConta(conta);
        cartao.setTipo(TipoCartao.DEBITO);

        when(cartaoRepository.findByContaId(1L)).thenReturn(List.of(cartao));

        List<CartaoResposta> resposta = cartaoService.listarPorConta(1L);

        assertEquals(1, resposta.size());
        assertEquals(TipoCartao.DEBITO, resposta.get(0).tipo());
    }
}
