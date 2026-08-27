package com.eclipsebank.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.dto.RegistroAuditoriaResposta;
import com.eclipsebank.backend.dto.UsuarioResposta;
import com.eclipsebank.backend.service.AuditoriaService;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.UsuarioService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioService usuarioService;
    private final ContaService contaService;
    private final AuditoriaService auditoriaService;

    public AdminController(
        UsuarioService usuarioService,
        ContaService contaService,
        AuditoriaService auditoriaService
    ) {
        this.usuarioService = usuarioService;
        this.contaService = contaService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResposta>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/contas")
    public ResponseEntity<List<ContaResposta>> listarContas() {
        return ResponseEntity.ok(contaService.listarTodas());
    }

    @PatchMapping("/contas/{id}/bloqueio")
    public ResponseEntity<ContaResposta> bloquearConta(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(contaService.bloquear(id));
    }

    @PatchMapping("/contas/{id}/desbloqueio")
    public ResponseEntity<ContaResposta> desbloquearConta(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(contaService.desbloquear(id));
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<RegistroAuditoriaResposta>> listarAuditoria() {
        return ResponseEntity.ok(auditoriaService.listar());
    }
}
