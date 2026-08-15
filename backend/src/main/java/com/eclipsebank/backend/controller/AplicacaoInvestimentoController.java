package com.eclipsebank.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.AplicacaoInvestimentoRequisicao;
import com.eclipsebank.backend.dto.AplicacaoInvestimentoResposta;
import com.eclipsebank.backend.dto.ResgateInvestimentoRequisicao;
import com.eclipsebank.backend.service.AplicacaoInvestimentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas/{contaId}/investimentos")
public class AplicacaoInvestimentoController {
    
    private final AplicacaoInvestimentoService aplicacaoInvestimentoService;

    public AplicacaoInvestimentoController(AplicacaoInvestimentoService aplicacaoInvestimentoService) {
        this.aplicacaoInvestimentoService = aplicacaoInvestimentoService;
    }

    @PostMapping("/aplicacoes")
    public ResponseEntity<AplicacaoInvestimentoResposta> aplicar(@PathVariable Long contaId, @Valid @RequestBody AplicacaoInvestimentoRequisicao dados) {
        AplicacaoInvestimentoResposta aplicacao = aplicacaoInvestimentoService.aplicar(contaId, dados);

        URI localizacao = URI.create("/api/contas/" + contaId + "/investimentos/carteira");

        return ResponseEntity.created(localizacao).body(aplicacao);
    }

    @GetMapping("/carteira")
    public ResponseEntity<List<AplicacaoInvestimentoResposta>> listarCarteira(@PathVariable Long contaId) {
        return ResponseEntity.ok(aplicacaoInvestimentoService.listarPorConta(contaId));
    }

    @PostMapping("/{aplicacaoId}/resgates")
    public ResponseEntity<AplicacaoInvestimentoResposta> resgatar(
        @PathVariable Long contaId,
        @PathVariable Long aplicacaoId,
        @Valid @RequestBody ResgateInvestimentoRequisicao dados
    ) {
        return ResponseEntity.ok(aplicacaoInvestimentoService.resgatar(contaId, aplicacaoId, dados));
    }
}
