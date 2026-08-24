package com.eclipsebank.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.EmprestimoResposta;
import com.eclipsebank.backend.dto.EmprestimoSolicitacao;
import com.eclipsebank.backend.dto.ParcelaResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.EmprestimoService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas/{contaId}/emprestimos")
public class EmprestimoController {
    
    private final EmprestimoService emprestimoService;
    private final ContaService contaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public EmprestimoController(
        EmprestimoService emprestimoService,
        ContaService contaService,
        UsuarioAutenticado usuarioAutenticado
    ) {
        this.emprestimoService = emprestimoService;
        this.contaService = contaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<EmprestimoResposta> solicitar(
        @PathVariable Long contaId,
        @Valid @RequestBody EmprestimoSolicitacao dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        return ResponseEntity.status(HttpStatus.CREATED).body(emprestimoService.solicitar(contaId, dados));
    }

    @GetMapping
    public ResponseEntity<List<EmprestimoResposta>> listarPorConta(
        @PathVariable Long contaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        return ResponseEntity.ok(emprestimoService.listarPorConta(contaId));
    }

    @PatchMapping("/{emprestimoId}/aprovar")
    public ResponseEntity<EmprestimoResposta> aprovar(
        @PathVariable Long contaId,
        @PathVariable Long emprestimoId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        return ResponseEntity.ok(emprestimoService.aprovar(contaId, emprestimoId));
    }

    @PostMapping("/{emprestimoId}/parcelas/{parcelaId}/pagar")
    public ResponseEntity<ParcelaResposta> pagarParcela(
        @PathVariable Long contaId,
        @PathVariable Long emprestimoId,
        @PathVariable Long parcelaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        return ResponseEntity.ok(emprestimoService.pagarParcela(contaId, emprestimoId, parcelaId));
    }

    @GetMapping("/{emprestimoId}/parcelas")
    public ResponseEntity<List<ParcelaResposta>> listarParcelas(
        @PathVariable Long contaId,
        @PathVariable Long emprestimoId,
        Authentication authentication
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(authentication, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(emprestimoService.listarParcelas(contaId, emprestimoId));
    }
}
