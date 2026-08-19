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

import com.eclipsebank.backend.dto.CartaoCadastro;
import com.eclipsebank.backend.dto.CartaoResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.CartaoService;
import com.eclipsebank.backend.service.ContaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Cartões", description = "Cartão de crédito e débito")
@RestController
@RequestMapping("/api/contas/{contaId}/cartoes")
public class CartaoController {
    
    private final CartaoService cartaoService;
    private final ContaService contaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public CartaoController(CartaoService cartaoService, ContaService contaService, UsuarioAutenticado usuarioAutenticado) {
        this.cartaoService = cartaoService;
        this.contaService = contaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Operation(summary =  "Criar cartão", description = "Cria um cartão para a conta")
    @PostMapping("criar")
    public ResponseEntity<CartaoResposta> cadastrar(
        @PathVariable Long contaId,
        @Valid @RequestBody CartaoCadastro dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        CartaoResposta cartao = cartaoService.cadastrar(contaId, dados);

        URI localizacao = URI.create("/api/contas/" + contaId + "/cartoes/meus-cartoes");

        return ResponseEntity.created(localizacao).body(cartao);
    }

    @Operation(summary = "Bloqueia o cartão", description = "Bloqueia o cartão do usuario")
    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<CartaoResposta> bloquear(
        @PathVariable Long contaId,
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(cartaoService.bloquear(contaId, id));
    }

    @Operation(summary = "Desbloqueia o cartão", description = "Desbloqueia o cartão do usuario")
    @PatchMapping("/{id}/desbloquear")
    public ResponseEntity<CartaoResposta> desbloquear(
        @PathVariable Long contaId,
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(cartaoService.desbloquear(contaId, id));
    }

    @Operation(summary = "Cancela o cartão", description = "Cancela o cartão do usuario")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CartaoResposta> cancelar(
        @PathVariable Long contaId,
        @PathVariable Long id,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(cartaoService.cancelar(contaId, id));
    }

    @Operation(summary = "Lista por conta", description = "Lista cartão pela conta do usuario")
    @GetMapping("/meus-cartoes")
    public ResponseEntity<List<CartaoResposta>> listarPorConta(
        @PathVariable Long contaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(cartaoService.listarPorConta(contaId));
    }

}
