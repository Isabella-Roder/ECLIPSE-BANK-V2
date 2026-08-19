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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Catálogo de Investimentos", description = "Cadastro e listagem de produtos de investimento disponíveis")
@RestController
@RequestMapping("/api/investimentos/produtos")
public class ProdutoInvestimentoController {

    private final ProdutoInvestimentoService produtoInvestimentoService;

    public ProdutoInvestimentoController(ProdutoInvestimentoService produtoInvestimentoService) {
        this.produtoInvestimentoService = produtoInvestimentoService;
    }

    @Operation(summary = "Cadastrar produto", description = "Cadastra um novo produto de investimento no catálogo (restrito a administradores)")
    @PostMapping
    public ResponseEntity<ProdutoInvestimentoResposta> cadastrar(@Valid @RequestBody ProdutoInvestimentoCadastro dados) {
        ProdutoInvestimentoResposta produto = produtoInvestimentoService.cadastrar(dados);

        URI localizacao = URI.create("/api/investimentos/produtos/" + produto.id());

        return ResponseEntity.created(localizacao).body(produto);
    }

    @Operation(summary = "Listar produtos ativos", description = "Lista os produtos de investimento ativos em ordem alfabética")
    @GetMapping
    public ResponseEntity<List<ProdutoInvestimentoResposta>> listarAtivos() {
        return ResponseEntity.ok(produtoInvestimentoService.listarAtivos());
    }
}
