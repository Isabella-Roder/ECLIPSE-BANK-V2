package com.eclipsebank.backend.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.ContaService;

@RestController
@RequestMapping("/api/contas")
public class ContaController {
    
    private final ContaService contaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public ContaController(ContaService contaService, UsuarioAutenticado usuarioAutenticado) {
        this.contaService = contaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<ContaResposta> criarParaUsuario(@PathVariable Long usuarioId, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, usuarioId);
        
        ContaResposta conta = contaService.criarParaUsuario(usuarioId);
        URI localizacao = URI.create("/api/contas/" + conta.id());

        return ResponseEntity.created(localizacao).body(conta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResposta> buscarPorId(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ContaResposta> buscarPorUsuario(
        @PathVariable Long usuarioId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(
            autenticacao,
            usuarioId
        );

        return ResponseEntity.ok(
            contaService.buscarPorUsuario(usuarioId)
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<ContaResposta> buscarPorAgencieENumero(
        @RequestParam String agencia,
        @RequestParam String numero
    ) {
        return ResponseEntity.ok(contaService.buscarPorAgenciaENumero(agencia, numero));
    }

    @PatchMapping("/{id}/bloqueio")
    public ResponseEntity<ContaResposta> bloquear(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.bloquear(id));
    }

    @PatchMapping("/{id}/desbloqueio")
    public ResponseEntity<ContaResposta> desbloquear(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.desbloquear(id));
    }

    @PatchMapping("/{id}/encerramento")
    public ResponseEntity<ContaResposta> encerrar(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.encerrar(id));
    }
}
