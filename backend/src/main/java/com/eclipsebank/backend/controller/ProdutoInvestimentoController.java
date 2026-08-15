package com.eclipsebank.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.ProdutoInvestimentoCadastro;
import com.eclipsebank.backend.dto.ProdutoInvestimentoResposta;
import com.eclipsebank.backend.service.ProdutoInvestimentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/investimentos/produtos")
public class ProdutoInvestimentoController {
    
    private final ProdutoInvestimentoService produtoInvestimentoService;

    public ProdutoInvestimentoController(ProdutoInvestimentoService produtoInvestimentoService) {
        this.produtoInvestimentoService = produtoInvestimentoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoInvestimentoResposta> cadastrar(@Valid @RequestBody ProdutoInvestimentoCadastro dados) {
        ProdutoInvestimentoResposta produto = produtoInvestimentoService.cadastrar(dados);

        URI localizacao = URI.create("/api/investimentos/produtos/" + produto.id());

        return ResponseEntity.created(localizacao).body(produto);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoInvestimentoResposta>> listarAtivos() {
        return ResponseEntity.ok(produtoInvestimentoService.listarAtivos());
    }
}
