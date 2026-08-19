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

import com.eclipsebank.backend.dto.AplicacaoInvestimentoRequisicao;
import com.eclipsebank.backend.dto.AplicacaoInvestimentoResposta;
import com.eclipsebank.backend.dto.PosicaoConsolidadaResposta;
import com.eclipsebank.backend.dto.ResgateInvestimentoRequisicao;
import com.eclipsebank.backend.dto.ProventoFiiResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.AplicacaoInvestimentoService;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.ProventoFiiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Investimentos", description = "Aplicação, resgate, proventos e posição consolidada da carteira de investimentos")
@RestController
@RequestMapping("/api/contas/{contaId}/investimentos")
public class AplicacaoInvestimentoController {
    
    private final AplicacaoInvestimentoService aplicacaoInvestimentoService;
    private final ProventoFiiService proventoFiiService;
    private final ContaService contaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public AplicacaoInvestimentoController(
        AplicacaoInvestimentoService aplicacaoInvestimentoService,
        ProventoFiiService proventoFiiService,
        ContaService contaService,
        UsuarioAutenticado usuarioAutenticado
    ) {
        this.aplicacaoInvestimentoService = aplicacaoInvestimentoService;
        this.proventoFiiService = proventoFiiService;
        this.contaService = contaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Operation(summary = "Aplicar em investimento", description = "Debita a conta e registra uma nova aplicação no produto escolhido")
    @PostMapping("/aplicacoes")
    public ResponseEntity<AplicacaoInvestimentoResposta> aplicar(
        @PathVariable Long contaId,
        @Valid @RequestBody AplicacaoInvestimentoRequisicao dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        AplicacaoInvestimentoResposta aplicacao = aplicacaoInvestimentoService.aplicar(contaId, dados);
        URI localizacao = URI.create("/api/contas/" + contaId + "/investimentos/carteira");

        return ResponseEntity.created(localizacao).body(aplicacao);
    }

    @Operation(summary = "Listar carteira", description = "Lista todas as aplicações de investimento da conta")
    @GetMapping("/carteira")
    public ResponseEntity<List<AplicacaoInvestimentoResposta>> listarCarteira(
        @PathVariable Long contaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(aplicacaoInvestimentoService.listarPorConta(contaId));
    }

    @Operation(summary = "Resgatar investimento", description = "Resgata parcial ou totalmente uma aplicação, creditando o valor na conta")
    @PostMapping("/{aplicacaoId}/resgates")
    public ResponseEntity<AplicacaoInvestimentoResposta> resgatar(
        @PathVariable Long contaId,
        @PathVariable Long aplicacaoId,
        @Valid @RequestBody ResgateInvestimentoRequisicao dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(aplicacaoInvestimentoService.resgatar(contaId, aplicacaoId, dados));
    }

    @Operation(summary = "Receber provento", description = "Credita na conta o provento mensal de uma aplicação em fundo imobiliário")
    @PostMapping("/{aplicacaoId}/proventos")
    public ResponseEntity<ProventoFiiResposta> pagarProvento(
        @PathVariable Long contaId,
        @PathVariable Long aplicacaoId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(proventoFiiService.pagar(contaId, aplicacaoId));
    }

    @Operation(summary = "Listar posição consolidada", description = "Retorna a posição consolidada por produto, com valor aplicado, valor atual e rentabilidade")
    @GetMapping("/posicao")
    public ResponseEntity<List<PosicaoConsolidadaResposta>> listarPosicaoConsolidada(
        @PathVariable Long contaId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(aplicacaoInvestimentoService.listarPosicaoConsolidada(contaId));
    }
}
