package com.eclipsebank.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.MovimentacaoResposta;
import com.eclipsebank.backend.service.MovimentacaoService;

@RestController
@RequestMapping("/api/movimentacoes")
public class ComprovanteController {
    
    private final MovimentacaoService movimentacaoService;

    public ComprovanteController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<MovimentacaoResposta> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(movimentacaoService.buscarPorCodigo(codigo));
    }
}
