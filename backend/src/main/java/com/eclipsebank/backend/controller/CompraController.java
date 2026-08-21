package com.eclipsebank.backend.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.CompraCadastro;
import com.eclipsebank.backend.dto.CompraResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.CartaoService;
import com.eclipsebank.backend.service.CompraService;
import com.eclipsebank.backend.service.ContaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Compras", description = "Compras realizadas com o cartao para a fatura")
@RestController
@RequestMapping("/api/contas/{contaId}/cartoes/{cartaoId}/faturas")
public class CompraController {
    
    private final CompraService compraService;
    private final ContaService contaService;
    private final CartaoService cartaoService;
    private final UsuarioAutenticado usuarioAutenticado;

    public CompraController(
        CompraService compraService,
        ContaService contaService,
        CartaoService cartaoService,
        UsuarioAutenticado usuarioAutenticado
    ) {
        this.compraService = compraService;
        this.contaService = contaService;
        this.cartaoService = cartaoService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Operation(summary = "Lançar compra", description = "Lança a compra para a fatura")
    @PostMapping("/compras")
    public ResponseEntity<CompraResposta> lancarCompra(
        @PathVariable Long contaId,
        @PathVariable Long cartaoId,
        @Valid @RequestBody CompraCadastro dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        cartaoService.validarCartaoDaConta(contaId, cartaoId);

        CompraResposta compra = compraService.lancarCompra(cartaoId, dados);

        URI localizacao = URI.create("/api/contas/" + contaId + "/cartoes/" + cartaoId + "/faturas/minha-fatura");

        return ResponseEntity.created(localizacao).body(compra);
    }
}
