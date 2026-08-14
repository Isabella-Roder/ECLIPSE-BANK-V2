package com.eclipsebank.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
    public ResponseEntity<UsuarioResposta> login(@Valid @RequestBody LoginRequisicao dados, HttpServletRequest requisicao) {
        UsuarioResposta usuario = usuarioService.login(dados);

        HttpSession sessionAnterior = requisicao.getSession(false);

        if (sessionAnterior != null) {
            sessionAnterior.invalidate();
        }

        HttpSession novaSessao = requisicao.getSession(true);
        novaSessao.setAttribute("usuarioId", usuario.id());

        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/sessao")
    public ResponseEntity<UsuarioResposta> buscarSessao(HttpSession sessao) {
        Long usuarioId = (Long) sessao.getAttribute("usuarioId");

        if (usuarioId == null) {
            throw new CredenciaisInvalidasException();
        }

        return ResponseEntity.ok(usuarioService.buscarPorId(usuarioId));
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpSession sessao) {
        sessao.invalidate();

        return ResponseEntity.noContent().build();
    }
}
