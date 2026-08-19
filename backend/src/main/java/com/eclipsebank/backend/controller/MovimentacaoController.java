package com.eclipsebank.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.MovimentacaoResposta;
import com.eclipsebank.backend.dto.OperacaoValor;
import com.eclipsebank.backend.dto.PixRequisicao;
import com.eclipsebank.backend.dto.TransferenciaRequisicao;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.MovimentacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Movimentações", description = "Depósito, saque, transferência, Pix e extrato da conta")
@RestController
@RequestMapping("/api/contas/{contaId}")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;
    private final ContaService contaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public MovimentacaoController(MovimentacaoService movimentacaoService, ContaService contaService, UsuarioAutenticado usuarioAutenticado) {
        this.movimentacaoService = movimentacaoService;
        this.contaService = contaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Operation(summary = "Depositar", description = "Registra um depósito e credita o valor na conta")
    @PostMapping("/depositos")
    public ResponseEntity<MovimentacaoResposta> depositar(@PathVariable Long contaId, @Valid @RequestBody OperacaoValor dados, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        MovimentacaoResposta movimentacao = movimentacaoService.depositar(contaId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @Operation(summary = "Sacar", description = "Registra um saque e debita o valor da conta")
    @PostMapping("/saques")
    public ResponseEntity<MovimentacaoResposta> sacar(@PathVariable Long contaId, @Valid @RequestBody OperacaoValor dados, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        MovimentacaoResposta movimentacao = movimentacaoService.sacar(contaId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @Operation(summary = "Transferir", description = "Transfere valor para outra conta pela agência e número")
    @PostMapping("/transferencias")
    public ResponseEntity<MovimentacaoResposta> transferir(@PathVariable Long contaId, @Valid @RequestBody TransferenciaRequisicao dados, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        MovimentacaoResposta movimentacao = movimentacaoService.transferir(contaId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @Operation(summary = "Enviar Pix", description = "Envia um Pix para a chave de e-mail informada")
    @PostMapping("/pix")
    public ResponseEntity<MovimentacaoResposta> fazerPix(@PathVariable Long contaId, @Valid @RequestBody PixRequisicao dados, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        MovimentacaoResposta movimentacao = movimentacaoService.fazerPix(contaId, dados);

        URI localizacao = URI.create("/api/movimentacoes/" + movimentacao.codigo());

        return ResponseEntity.created(localizacao).body(movimentacao);
    }

    @Operation(summary = "Listar extrato", description = "Lista o extrato de movimentações da conta")
    @GetMapping("/extrato")
    public ResponseEntity<List<MovimentacaoResposta>> listarExtrato(@PathVariable Long contaId, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));
        
        return ResponseEntity.ok(movimentacaoService.listarExtrato(contaId));
    }
}
