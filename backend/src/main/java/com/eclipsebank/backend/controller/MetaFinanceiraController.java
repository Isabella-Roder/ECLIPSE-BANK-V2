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

import com.eclipsebank.backend.dto.MetaFinanceiraCadastro;
import com.eclipsebank.backend.dto.MetaFinanceiraRequisicao;
import com.eclipsebank.backend.dto.MetaFinanceiraResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.MetaFinanceiraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas/{contaId}/metas-financeiras")
public class MetaFinanceiraController {
    
    private final MetaFinanceiraService metaFinanceiraService;
    private final UsuarioAutenticado usuarioAutenticado;
    private final ContaService contaService;

    public MetaFinanceiraController(
        MetaFinanceiraService metaFinanceiraService,
        UsuarioAutenticado usuarioAutenticado,
        ContaService contaService
    ) {
        this.metaFinanceiraService = metaFinanceiraService;
        this.usuarioAutenticado = usuarioAutenticado;
        this.contaService = contaService;
    }

    @PostMapping("/criar")
    public ResponseEntity<MetaFinanceiraResposta> criar(@PathVariable Long contaId, @Valid @RequestBody MetaFinanceiraCadastro dados, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        MetaFinanceiraResposta meta = metaFinanceiraService.criar(contaId, dados);
        URI localizacao = URI.create("/api/contas/" + contaId + "/metas-financeiras/minhas-metas");

        return ResponseEntity.created(localizacao).body(meta);
    }

    @GetMapping("/minhas-metas")
    public ResponseEntity<List<MetaFinanceiraResposta>> listarMinhasMetas(@PathVariable Long contaId, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(metaFinanceiraService.listarPorConta(contaId));
    }

    @PostMapping("/{metaId}/aportar")
    public ResponseEntity<MetaFinanceiraResposta> aportar(
        @PathVariable Long contaId,
        @PathVariable Long metaId,
        @Valid @RequestBody MetaFinanceiraRequisicao dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(metaFinanceiraService.aportar(contaId, metaId, dados));
    }

    @PostMapping("/{metaId}/resgatar")
    public ResponseEntity<MetaFinanceiraResposta> resgatar(
        @PathVariable Long contaId,
        @PathVariable Long metaId,
        @Valid @RequestBody MetaFinanceiraRequisicao dados,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(contaId));

        return ResponseEntity.ok(metaFinanceiraService.resgatar(contaId, metaId, dados));
    }

}
