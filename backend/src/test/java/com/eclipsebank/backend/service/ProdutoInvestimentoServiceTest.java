package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eclipsebank.backend.dto.ProdutoInvestimentoCadastro;
import com.eclipsebank.backend.dto.ProdutoInvestimentoResposta;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoInvestimentoServiceTest {
    
    @Mock
    private ProdutoInvestimentoRepository produtoInvestimentoRepository;

    private ProdutoInvestimentoService produtoInvestimentoService;

    @BeforeEach
    void preparar() {
        produtoInvestimentoService = new ProdutoInvestimentoService(produtoInvestimentoRepository);
    }

    @Test
    void deveRecusarCadastroQuandoCodigoJaExistir() {
        ProdutoInvestimentoCadastro dados = new ProdutoInvestimentoCadastro(
            "CDB de teste",
            " cdb01 ",
            TipoInvestimento.RENDA_FIXA,
            new BigDecimal("100.00"),
            new BigDecimal("12.50")
        );

        when(produtoInvestimentoRepository.existsByCodigoIgnoreCase("CDB01")).thenReturn(true);

        assertThrows(
            ConflitoException.class,
            () -> produtoInvestimentoService.cadastrar(dados)
        );

        verify(produtoInvestimentoRepository, never()).save(any(ProdutoInvestimento.class));
    }

    @Test
    void deveCadastrarProdutoNormalizandoNomeCodigo() {
        ProdutoInvestimentoCadastro dados = new ProdutoInvestimentoCadastro(
            " CDB de teste ",
            " cdb01 ",
            TipoInvestimento.RENDA_FIXA,
            new BigDecimal("100.00"),
            new BigDecimal("12.50")
        );

        when(produtoInvestimentoRepository.save(any(ProdutoInvestimento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ProdutoInvestimentoResposta resposta = produtoInvestimentoService.cadastrar(dados);

        ArgumentCaptor<ProdutoInvestimento> captor = ArgumentCaptor.forClass(ProdutoInvestimento.class);

        verify(produtoInvestimentoRepository).save(captor.capture());

        ProdutoInvestimento produtoSalvo = captor.getValue();

        assertEquals("CDB de teste", produtoSalvo.getNome());
        assertEquals("CDB01", produtoSalvo.getCodigo());
        assertEquals(TipoInvestimento.RENDA_FIXA, produtoSalvo.getTipo());
        assertEquals(new BigDecimal("100.00"), resposta.valorMinimo());
    }

    @Test
    void deveListarProdutoAtivosEmOrdemRecebidaDoRepository() {
        ProdutoInvestimento primeiroProduto = new ProdutoInvestimento();
        primeiroProduto.setNome("CDB Eclipse");
        primeiroProduto.setCodigo("CDB01");
        primeiroProduto.setTipo(TipoInvestimento.RENDA_FIXA);
        primeiroProduto.setValorMinimo(new BigDecimal("100.00"));
        primeiroProduto.setRentabilidadeAnualEstimada(new BigDecimal("12.50"));

        ProdutoInvestimento segundoProduto = new ProdutoInvestimento();
        segundoProduto.setNome("ETF Global");
        segundoProduto.setCodigo("ETF01");
        segundoProduto.setTipo(TipoInvestimento.ETF);
        segundoProduto.setValorMinimo(new BigDecimal("50.00"));
        segundoProduto.setRentabilidadeAnualEstimada(new BigDecimal("8.00"));

        when(produtoInvestimentoRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(primeiroProduto, segundoProduto));

        List<ProdutoInvestimentoResposta> resposta = produtoInvestimentoService.listarAtivos();

        assertEquals(2, resposta.size());
        assertEquals("CDB Eclipse", resposta.get(0).nome());
        assertEquals("ETF Global", resposta.get(1).nome());

        verify(produtoInvestimentoRepository).findByAtivoTrueOrderByNomeAsc();
    }
}
