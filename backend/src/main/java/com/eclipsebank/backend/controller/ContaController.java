package com.eclipsebank.backend.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.service.ContaService;

@RestController
@RequestMapping("/api/contas")
public class ContaController {
    
    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<ContaResposta> criarParaUsuario(@PathVariable Long usuarioId) {
        ContaResposta conta = contaService.criarParaUsuario(usuarioId);
        URI localizacao = URI.create("/api/contas/" + conta.id());

        return ResponseEntity.created(localizacao).body(conta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResposta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ContaResposta> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(contaService.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ContaResposta> buscarPorAgencieENumero(
        @RequestParam String agencia,
        @RequestParam String numero
    ) {
        return ResponseEntity.ok(contaService.buscarPorAgenciaENumero(agencia, numero));
    }
}
