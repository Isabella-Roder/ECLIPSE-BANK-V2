package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.CartaoCadastro;
import com.eclipsebank.backend.dto.CartaoResposta;
import com.eclipsebank.backend.enums.TipoCartao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Cartao;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.repository.CartaoRepository;
import com.eclipsebank.backend.repository.ContaRepository;

@Service
public class CartaoService {

    private static final int LIMITE_TENTATIVA = 20;
    
    private final CartaoRepository cartaoRepository;
    private final ContaRepository contaRepository;
    private final SecureRandom secureRandom;

    public CartaoService(CartaoRepository cartaoRepository, ContaRepository contaRepository) {
        this.cartaoRepository = cartaoRepository;
        this.contaRepository = contaRepository;
        this.secureRandom = new SecureRandom();
    }

    private String gerarNumeroUnico() {
        for (int tentativa = 0; tentativa < LIMITE_TENTATIVA; tentativa++) {
            String numero = String.format(
                "%016d",
                secureRandom.nextLong(10_000_000_000_000_000L)
            );

            if (!cartaoRepository.existsByNumero(numero)) {
                return numero;
            }
        }

        throw new IllegalArgumentException("Não foi possivel gerar um numero de cartão único.");
    }

    private Cartao buscarCartaoDaConta(Long cartaoId, Long contaId) {
        Cartao cartao = cartaoRepository.findById(cartaoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cartão não encontrado com o ID: " + cartaoId));

        if (!cartao.getConta().getId().equals(contaId)) {
            throw new RecursoNaoEncontradoException("Cartão não encontrado com o ID: " + cartaoId);
        }

        return cartao;
    }

    public void validarCartaoDaConta(Long contaId, Long cartaoId) {
        buscarCartaoDaConta(cartaoId, contaId);
    }

    @Transactional
    public CartaoResposta cadastrar(Long contaId, CartaoCadastro dados) {
        Conta conta = contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID: " + contaId));

        Cartao cartao = new Cartao();
        cartao.setConta(conta);
        cartao.setTitular(conta.getUsuario().getNomeSocial() != null
            ? conta.getUsuario().getNomeSocial()
            : conta.getUsuario().getNome()
        );
        cartao.setNumero(gerarNumeroUnico());
        cartao.setTipo(dados.tipo());

        if (dados.tipo() == TipoCartao.CREDITO) {
            cartao.setLimite(new BigDecimal("1000.00"));
        }

        return CartaoResposta.from(cartaoRepository.save(cartao));
    }

    @Transactional
    public CartaoResposta bloquear(Long contaId, Long cartaoId) {
        Cartao cartao = buscarCartaoDaConta(cartaoId, contaId);
        cartao.bloquear();

        return CartaoResposta.from(cartao);
    }

    @Transactional
    public CartaoResposta desbloquear(Long contaId, Long cartaoId) {
        Cartao cartao = buscarCartaoDaConta(cartaoId, contaId);
        cartao.desbloquear();

        return CartaoResposta.from(cartao);
    }

    @Transactional
    public CartaoResposta cancelar(Long contaId, Long cartaoId) {
        Cartao cartao = buscarCartaoDaConta(cartaoId, contaId);
        cartao.cancelar();

        return CartaoResposta.from(cartao);
    }

    @Transactional(readOnly = true)
    public List<CartaoResposta> listarPorConta(Long contaId) {
        return cartaoRepository.findByContaId(contaId).stream()
            .map(CartaoResposta::from).toList();
    }

}
