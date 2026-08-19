package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.eclipsebank.backend.dto.AplicacaoInvestimentoRequisicao;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.models.Usuario;
import com.eclipsebank.backend.repository.AplicacaoInvestimentoRepository;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;
import com.eclipsebank.backend.repository.UsuarioRepository;

@SpringBootTest
class AplicacaoInvestimentoRollbackTest {

    @Autowired
    private AplicacaoInvestimentoService aplicacaoInvestimentoService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoInvestimentoRepository produtoInvestimentoRepository;

    @Autowired
    private AplicacaoInvestimentoRepository aplicacaoInvestimentoRepository;

    @MockitoBean
    private MovimentacaoRepository movimentacaoRepository;

    private Long contaId;
    private Long produtoId;

    @BeforeEach
    void preparar() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Rollback");
        usuario.setCpf("11111111111");
        usuario.setEmail("rollback@teste.com");
        usuario.setSenhaHash("hash-fake-para-teste");
        usuario = usuarioRepository.save(usuario);

        Conta conta = new Conta();
        conta.setAgencia("0001");
        conta.setNumero("99999999");
        conta.setUsuario(usuario);
        conta = contaRepository.save(conta);
        conta.creditar(new BigDecimal("1000.00"));
        conta = contaRepository.save(conta);
        contaId = conta.getId();

        ProdutoInvestimento produto = new ProdutoInvestimento();
        produto.setNome("CDB Teste Rollback");
        produto.setCodigo("CDBRB");
        produto.setTipo(TipoInvestimento.RENDA_FIXA);
        produto.setValorMinimo(new BigDecimal("50.00"));
        produto.setRentabilidadeAnualEstimada(new BigDecimal("10.00"));
        produto = produtoInvestimentoRepository.save(produto);
        produtoId = produto.getId();

        doThrow(new RuntimeException("Falha simulada ao registrar a movimentação"))
            .when(movimentacaoRepository).save(any());
    }

    @Test
    void deveDesfazerDebitoEAplicacaoQuandoRegistroDaMovimentacaoFalhar() {
        AplicacaoInvestimentoRequisicao dados =
            new AplicacaoInvestimentoRequisicao(produtoId, new BigDecimal("200.00"));

        assertThrows(
            RuntimeException.class,
            () -> aplicacaoInvestimentoService.aplicar(contaId, dados)
        );

        Conta contaAposFalha = contaRepository.findById(contaId).orElseThrow();

        assertEquals(0, new BigDecimal("1000.00").compareTo(contaAposFalha.getSaldo()));
        assertTrue(aplicacaoInvestimentoRepository.findByContaIdOrderByAplicadaEmDesc(contaId).isEmpty());
    }
}
