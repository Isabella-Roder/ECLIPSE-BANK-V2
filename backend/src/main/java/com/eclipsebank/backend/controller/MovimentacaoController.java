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

import com.eclipsebank.backend.dto.MovimentacaoResposta;
import com.eclipsebank.backend.dto.OperacaoValor;
import com.eclipsebank.backend.dto.TransferenciaRequisicao;
import com.eclipsebank.backend.service.MovimentacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas/{contaId}")
public class MovimentacaoController {
    
    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping("/depositos")
    public ResponseEntity<MovimentacaoResposta> depositar(@PathVariable Long contaId, @Valid @RequestBody OperacaoValor dados) {
        MovimentacaoResposta movimentacao = movimentacaoService.depositar(contaId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @PostMapping("/saques")
    public ResponseEntity<MovimentacaoResposta> sacar(@PathVariable Long contaId, @Valid @RequestBody OperacaoValor dados) {
        MovimentacaoResposta movimentacao = movimentacaoService.sacar(contaId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @PostMapping("/transferencias")
    public ResponseEntity<MovimentacaoResposta> transferir(@PathVariable Long contaOrigemId, @Valid @RequestBody TransferenciaRequisicao dados) {
        MovimentacaoResposta movimentacao = movimentacaoService.transferir(contaOrigemId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @GetMapping("/extrato")
    public ResponseEntity<List<MovimentacaoResposta>> listarExtrato(@PathVariable Long contaId) {
        return ResponseEntity.ok(movimentacaoService.listarExtrato(contaId));
    }
}
