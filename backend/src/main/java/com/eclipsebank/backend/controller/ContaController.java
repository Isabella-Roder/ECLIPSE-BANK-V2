package com.eclipsebank.backend.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.security.UsuarioAutenticado;
import com.eclipsebank.backend.service.ContaService;
import com.eclipsebank.backend.service.EmpresaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Contas", description = "Criação, consulta e gerenciamento de contas bancárias")
@RestController
@RequestMapping("/api/contas")
public class ContaController {
    
    private final ContaService contaService;
    private final EmpresaService empresaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public ContaController(ContaService contaService, EmpresaService empresaService, UsuarioAutenticado usuarioAutenticado) {
        this.contaService = contaService;
        this.empresaService = empresaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Operation(summary = "Criar conta", description = "Cria a conta bancária do usuário autenticado, caso ele ainda não possua uma")
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<ContaResposta> criarParaUsuario(@PathVariable Long usuarioId, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, usuarioId);
        
        ContaResposta conta = contaService.criarParaUsuario(usuarioId);
        URI localizacao = URI.create("/api/contas/" + conta.id());

        return ResponseEntity.created(localizacao).body(conta);
    }

    @Operation(summary = "Buscar conta por ID", description = "Retorna os dados da conta do proprietário autenticado a partir do seu ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContaResposta> buscarPorId(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @Operation(summary = "Buscar conta por usuário", description = "Retorna a conta vinculada ao usuário autenticado")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ContaResposta> buscarPorUsuario(
        @PathVariable Long usuarioId,
        Authentication autenticacao
    ) {
        usuarioAutenticado.validarAcessoAoUsuario(
            autenticacao,
            usuarioId
        );

        return ResponseEntity.ok(
            contaService.buscarPorUsuario(usuarioId)
        );
    }

    @Operation(summary = "Buscar conta por agência e número", description = "Localiza uma conta pela combinação de agência e número, usada para validar a conta de destino em transferências")
    @GetMapping("/buscar")
    public ResponseEntity<ContaResposta> buscarPorAgencieENumero(
        @RequestParam String agencia,
        @RequestParam String numero
    ) {
        return ResponseEntity.ok(contaService.buscarPorAgenciaENumero(agencia, numero));
    }

    @Operation(summary = "Bloquear conta", description = "Bloqueia a conta do proprietário autenticado, impedindo novas movimentações")
    @PatchMapping("/{id}/bloqueio")
    public ResponseEntity<ContaResposta> bloquear(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.bloquear(id));
    }

    @Operation(summary = "Desbloquear conta", description = "Reativa uma conta bloqueada do proprietário autenticado")
    @PatchMapping("/{id}/desbloqueio")
    public ResponseEntity<ContaResposta> desbloquear(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.desbloquear(id));
    }

    @Operation(summary = "Encerrar conta", description = "Encerra a conta do proprietário autenticado; exige que o saldo esteja zerado")
    @PatchMapping("/{id}/encerramento")
    public ResponseEntity<ContaResposta> encerrar(@PathVariable Long id, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, contaService.obterUsuarioIdDono(id));
        
        return ResponseEntity.ok(contaService.encerrar(id));
    }

    @Operation(summary = "Criar conta para empresa", description = "Cria uma conta empresarial com usuario responsavel")
    @PostMapping("/empresa/{empresaId}")
    public ResponseEntity<ContaResposta> criarParaEmpresa(@PathVariable Long empresaId, Authentication autenticacao) {
        usuarioAutenticado.validarAcessoAoUsuario(autenticacao, empresaService.obterUsuarioResponsavelId(empresaId));

        ContaResposta resposta = contaService.criarParaEmpresa(empresaId);
        URI localizacao = URI.create("/api/contas/" + resposta.id());

        return ResponseEntity.created(localizacao).body(resposta);
    }
}
