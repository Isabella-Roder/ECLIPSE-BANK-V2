package com.eclipsebank.backend.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Usuario;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.UsuarioRepository;

@Service
public class ContaService {
    
    private static final String AGENCIA_PADRAO = "0001";
    private static final int LIMITE_TENTATIVA = 20;

    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecureRandom secureRandom;

    public ContaService(ContaRepository contaRepository, UsuarioRepository usuarioRepository) {
        this.contaRepository = contaRepository;
        this.usuarioRepository = usuarioRepository;
        this.secureRandom = new SecureRandom();
    }

    private String gerarNumeroUnico() {
        for (int tentativa = 0; tentativa < LIMITE_TENTATIVA; tentativa++) {
            String numero = String.format(
                "%08d", 
            secureRandom.nextInt(100_000_000));

            if (!contaRepository.existsByAgenciaAndNumero(AGENCIA_PADRAO, numero)) {
                return numero;
            }
        }

        throw new IllegalStateException("Não foi possivel gerar um número de conta único");
    }

    private Conta buscarEntidade(Long id) {
        return contaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID:" + id));
    }

    @Transactional(readOnly = true)
    public Long obterUsuarioIdDono(Long contaId) {
        return contaRepository.buscarUsuarioIdPelaContaId(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID: " + contaId));
    }

    @Transactional
    public ContaResposta criarParaUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario não encontrado com o ID: " + usuarioId));

        if (!usuario.getAtivo()) {
            throw new ConflitoException("Não é possivel criar conta para usuario inativo.");
        }

        if (contaRepository.existsByUsuarioId(usuarioId)) {
            throw new ConflitoException("O usuário já possui uma conta");
        }

        Conta conta = new Conta();
        conta.setAgencia(AGENCIA_PADRAO);
        conta.setNumero(gerarNumeroUnico());
        conta.setUsuario(usuario);

        return ContaResposta.from(contaRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public ContaResposta buscarPorId(Long id) {
        return ContaResposta.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public ContaResposta buscarPorUsuario(Long usuarioId) {
        Conta conta = contaRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada para o usuário: " + usuarioId));

        return ContaResposta.from(conta);
    }

    @Transactional(readOnly = true)
    public ContaResposta buscarPorAgenciaENumero(String agencia, String numero) {
        Conta conta = contaRepository.findByAgenciaAndNumero(agencia, numero)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));

        return ContaResposta.from(conta);
    }

    @Transactional
    public ContaResposta bloquear(Long contaId) {
        Conta conta = buscarEntidade(contaId);

        conta.bloquear();

        return ContaResposta.from(conta);
    }

    @Transactional
    public ContaResposta desbloquear(Long contaId) {
        Conta conta = buscarEntidade(contaId);

        conta.desbloquear();

        return ContaResposta.from(conta);
    }

    @Transactional
    public ContaResposta encerrar(Long contaId) {
        Conta conta = buscarEntidade(contaId);

        conta.encerrar();

        return ContaResposta.from(conta);
    }
}