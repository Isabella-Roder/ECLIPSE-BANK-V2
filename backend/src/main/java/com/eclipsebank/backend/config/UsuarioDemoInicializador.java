package com.eclipsebank.backend.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.dto.OperacaoValor;
import com.eclipsebank.backend.dto.UsuarioCadastro;
import com.eclipsebank.backend.dto.UsuarioResposta;
import com.eclipsebank.backend.repository.UsuarioRepository;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.MovimentacaoService;
import com.eclipsebank.backend.service.UsuarioService;

@Profile("demo")
@Component
public class UsuarioDemoInicializador implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final ContaService contaService;
    private final MovimentacaoService movimentacaoService;

    @Value("${DEMO_SENHA}")
    private String senhaDemo;

    public UsuarioDemoInicializador(UsuarioRepository usuarioRepository, UsuarioService usuarioService, ContaService contaService, MovimentacaoService movimentacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.contaService = contaService;
        this.movimentacaoService = movimentacaoService;
    }

    @Override
    public void run(String... args) {
        String email = "demo@eclipsebank.com";

        String cpf = "39823817008";

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        if (usuarioRepository.existsByCpf(cpf)) {
            return;
        }

        UsuarioCadastro dados = new UsuarioCadastro(
            "Nome Demo",
            null,
            cpf,
            email,
            senhaDemo,
            null,
            null
        );

        UsuarioResposta usuario = usuarioService.cadastrar(dados);

        ContaResposta conta = contaService.criarParaUsuario(usuario.id());

        movimentacaoService.depositar(conta.id(), new OperacaoValor(new BigDecimal("5000.00"), "Depósito inicial de demonstração"));
    }
}
