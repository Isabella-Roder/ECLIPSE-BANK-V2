package com.eclipsebank.backend.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.EmpresaCadastro;
import com.eclipsebank.backend.dto.EmpresaResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.EmpresaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public EmpresaController(EmpresaService empresaService, UsuarioAutenticado usuarioAutenticado) {
        this.empresaService = empresaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<EmpresaResposta> cadastrar(@Valid @RequestBody EmpresaCadastro dados, Authentication autenticacao) {
        Long usuarioId = usuarioAutenticado.obterId(autenticacao);

        EmpresaResposta empresa = empresaService.cadastrar(usuarioId, dados);
        URI localizacao = URI.create("/api/empresas/" + empresa.id());

        return ResponseEntity.created(localizacao).body(empresa);
    }
}
