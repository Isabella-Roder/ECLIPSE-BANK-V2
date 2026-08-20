package com.eclipsebank.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.CompraCartao;
import com.eclipsebank.backend.dto.FaturaResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.CartaoService;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.FaturaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Faturas", description = "Faturas do cartão de crédito")
@RestController
@RequestMapping("/api/contas/{contaId}/cartoes/{cartaoId}/faturas")
public class FaturaController {

    private final FaturaService faturaService;
    private final ContaService contaService;
    private final CartaoService cartaoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public FaturaController(
        FaturaService faturaService,
        ContaService contaService,
        CartaoService cartaoService,
        UsuarioAutenticado usuarioAutenticado
    ) {
        this.faturaService = faturaService;
        this.contaService = contaService;
        this.cartaoService = cartaoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Operation(summary = "Lançar compra", description = "Lança a compra para a fatura")
    @PostMapping("/compras")
    public ResponseEntity<FaturaResposta> lancarCompra(
        @PathVariable Long contaId,
        @PathVariable Long cartaoId,
        @Valid @RequestBody CompraCartao dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        cartaoService.validarCartaoDaConta(contaId, cartaoId);

        FaturaResposta fatura = faturaService.lancarCompra(cartaoId, dados);

        URI localizacao = URI.create("/api/contas/" + contaId + "/cartoes/" + cartaoId + "/faturas/minha-fatura");

        return ResponseEntity.created(localizacao).body(fatura);
    }

    @Operation(summary = "Lista por cartao", description = "Lista fatura do cartao")
    @GetMapping("/minha-fatura")
    public ResponseEntity<List<FaturaResposta>> listarPorCartao(
        @PathVariable Long contaId,
        @PathVariable Long cartaoId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        cartaoService.validarCartaoDaConta(contaId, cartaoId);

        return ResponseEntity.ok(faturaService.listarPorCartao(cartaoId));
    }

    @Operation(summary = "Pagar", description = "Pagar fatura do cartão")
    @PostMapping("/{faturaId}/pagar")
    public ResponseEntity<FaturaResposta> pagar(
        @PathVariable Long contaId,
        @PathVariable Long cartaoId, 
        @PathVariable Long faturaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        cartaoService.validarCartaoDaConta(contaId, cartaoId);

        return ResponseEntity.ok(faturaService.pagar(contaId, cartaoId, faturaId));
    }

    @Operation(summary = "Fecha", description = "Fecha a fatura")
    @PatchMapping("/{faturaId}/fechar")
    public ResponseEntity<FaturaResposta> fechar(
        @PathVariable Long contaId,
        @PathVariable Long cartaoId,
        @PathVariable Long faturaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        cartaoService.validarCartaoDaConta(contaId, cartaoId);

        return ResponseEntity.ok(faturaService.fechar(cartaoId, faturaId));
    }
}
