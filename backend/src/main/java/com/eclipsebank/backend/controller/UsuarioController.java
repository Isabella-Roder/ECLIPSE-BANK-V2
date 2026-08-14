package com.eclipsebank.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.LoginRequisicao;
import com.eclipsebank.backend.dto.UsuarioAtualizacao;
import com.eclipsebank.backend.dto.UsuarioCadastro;
import com.eclipsebank.backend.dto.UsuarioResposta;
import com.eclipsebank.backend.exception.CredenciaisInvalidasException;
import com.eclipsebank.backend.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    private final SecurityContextRepository securityContextRepository;

    public UsuarioController(UsuarioService usuarioService, SecurityContextRepository securityContextRepository) {
        this.usuarioService = usuarioService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping
    public ResponseEntity<UsuarioResposta> cadastrar(@Valid @RequestBody UsuarioCadastro dados) {
        UsuarioResposta usuario = usuarioService.cadastrar(dados);
        URI localizacao = URI.create("/api/usuarios/" + usuario.id());

        return ResponseEntity.created(localizacao).body(usuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResposta>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResposta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResposta> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioAtualizacao dados) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResposta> login(@Valid @RequestBody LoginRequisicao dados, HttpServletRequest requisicao, HttpServletResponse resposta) {
        UsuarioResposta usuario = usuarioService.login(dados);

        HttpSession sessionAnterior = requisicao.getSession(false);

        if (sessionAnterior != null) {
            sessionAnterior.invalidate();
        }

        SimpleGrantedAuthority autoridade = new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name());

        Authentication autenticacao = UsernamePasswordAuthenticationToken.authenticated(
            usuario.id(),
            null,
            List.of(autoridade)
        );

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);

        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, requisicao, resposta);

        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/sessao")
    public ResponseEntity<UsuarioResposta> buscarSessao(Authentication autenticacao) {
        if (
            autenticacao == null ||
            !autenticacao.isAuthenticated() ||
            !(autenticacao.getPrincipal() instanceof Long usuarioId)
        ) {
            throw new CredenciaisInvalidasException();
        }

        return ResponseEntity.ok(usuarioService.buscarPorId(usuarioId));
    }

}
