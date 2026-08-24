package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.eclipsebank.backend.dto.EmprestimoResposta;
import com.eclipsebank.backend.dto.EmprestimoSolicitacao;
import com.eclipsebank.backend.dto.ParcelaResposta;
import com.eclipsebank.backend.enums.StatusEmprestimo;
import com.eclipsebank.backend.enums.StatusParcela;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Emprestimo;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.models.Parcela;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.EmprestimoRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.ParcelaRepository;

@ExtendWith(MockitoExtension.class)
public class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ParcelaRepository parcelaRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    private EmprestimoService emprestimoService;

    @BeforeEach
    void preparar() {
        emprestimoService = new EmprestimoService(
            emprestimoRepository,
            contaRepository,
            parcelaRepository,
            movimentacaoRepository
        );
    }

    @Test
    void deveSolicitarEmprestimoComValorTotalEParcelasCorretas() {
        Conta conta = new Conta();

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(emprestimoRepository.save(any(Emprestimo.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));
        when(parcelaRepository.save(any(Parcela.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        EmprestimoSolicitacao dados = new EmprestimoSolicitacao(
            new BigDecimal("1000.00"),
            new BigDecimal("10.00"),
            5
        );

        EmprestimoResposta resposta = emprestimoService.solicitar(1L, dados);

        assertEquals(new BigDecimal("1100.00"), resposta.valorTotal());
        verify(parcelaRepository, times(5)).save(any(Parcela.class));
    }

    @Test
    void deveRecusarSolicitarQuandoContaNaoExiste() {
        when(contaRepository.findById(1L)).thenReturn(Optional.empty());

        EmprestimoSolicitacao dados = new EmprestimoSolicitacao(
            new BigDecimal("1000.00"),
            new BigDecimal("10.00"),
            5
        );

        assertThrows(RecursoNaoEncontradoException.class, () -> emprestimoService.solicitar(1L, dados));
    }

    @Test
    void deveListarEmprestimosPorConta() {
        Conta conta = new Conta();
        ReflectionTestUtils.setField(conta, "id", 1L);

        Emprestimo emprestimo = new Emprestimo();
        ReflectionTestUtils.setField(emprestimo, "id", 10L);
        emprestimo.setConta(conta);
        emprestimo.setValorSolicitado(new BigDecimal("1000.00"));
        emprestimo.setTaxaJuros(new BigDecimal("10.00"));
        emprestimo.setQuantidadeParcelas(5);
        emprestimo.setValorTotal(new BigDecimal("1100.00"));

        when(emprestimoRepository.findByContaId(1L)).thenReturn(List.of(emprestimo));

        List<EmprestimoResposta> resposta = emprestimoService.listarPorConta(1L);

        assertEquals(1, resposta.size());
        assertEquals(10L, resposta.getFirst().id());
        assertEquals(new BigDecimal("1100.00"), resposta.getFirst().valorTotal());
    }

    @Test
    void deveAprovarECreditarValorNaConta() {
        Conta conta = new Conta();
        ReflectionTestUtils.setField(conta, "id", 1L);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setConta(conta);
        emprestimo.setValorSolicitado(new BigDecimal("1000.00"));
        emprestimo.setStatus(StatusEmprestimo.SOLICITADO);

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimo));

        EmprestimoResposta resposta = emprestimoService.aprovar(1L, 10L);

        assertEquals(StatusEmprestimo.APROVADO, resposta.status());
        assertEquals(new BigDecimal("1000.00"), conta.getSaldo());
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }

    @Test
    void deveRecusarAprovarEmprestimoDeOutraConta() {
        Conta contaDoEmprestimo = new Conta();
        ReflectionTestUtils.setField(contaDoEmprestimo, "id", 2L);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setConta(contaDoEmprestimo);

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimo));

        assertThrows(RecursoNaoEncontradoException.class, () -> emprestimoService.aprovar(1L, 10L));
    }

    @Test
    void devePagarParcelaEDebitarConta() {
        Conta conta = new Conta();
        ReflectionTestUtils.setField(conta, "id", 1L);
        conta.creditar(new BigDecimal("500.00"));

        Emprestimo emprestimo = new Emprestimo();
        ReflectionTestUtils.setField(emprestimo, "id", 10L);
        emprestimo.setConta(conta);
        emprestimo.setStatus(StatusEmprestimo.EM_ANDAMENTO);

        Parcela parcela = new Parcela();
        parcela.setEmprestimo(emprestimo);
        parcela.setNumero(1);
        parcela.setValor(new BigDecimal("100.00"));
        parcela.setStatus(StatusParcela.PENDENTE);

        Parcela outraParcelaPendente = new Parcela();
        outraParcelaPendente.setEmprestimo(emprestimo);
        outraParcelaPendente.setNumero(2);
        outraParcelaPendente.setStatus(StatusParcela.PENDENTE);

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimo));
        when(parcelaRepository.findById(20L)).thenReturn(Optional.of(parcela));
        when(parcelaRepository.findByEmprestimoId(10L))
            .thenReturn(List.of(parcela, outraParcelaPendente));

        ParcelaResposta resposta = emprestimoService.pagarParcela(1L, 10L, 20L);

        assertEquals(StatusParcela.PAGA, resposta.status());
        assertEquals(new BigDecimal("400.00"), conta.getSaldo());
        assertEquals(StatusEmprestimo.EM_ANDAMENTO, emprestimo.getStatus());
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }

    @Test
    void deveQuitarEmprestimoQuandoUltimaParcelaForPaga() {
        Conta conta = new Conta();
        ReflectionTestUtils.setField(conta, "id", 1L);
        conta.creditar(new BigDecimal("500.00"));

        Emprestimo emprestimo = new Emprestimo();
        ReflectionTestUtils.setField(emprestimo, "id", 10L);
        emprestimo.setConta(conta);
        emprestimo.setStatus(StatusEmprestimo.EM_ANDAMENTO);

        Parcela ultimaParcela = new Parcela();
        ultimaParcela.setEmprestimo(emprestimo);
        ultimaParcela.setNumero(2);
        ultimaParcela.setValor(new BigDecimal("100.00"));
        ultimaParcela.setStatus(StatusParcela.PENDENTE);

        Parcela parcelaJaPaga = new Parcela();
        parcelaJaPaga.setEmprestimo(emprestimo);
        parcelaJaPaga.setNumero(1);
        parcelaJaPaga.setStatus(StatusParcela.PAGA);

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimo));
        when(parcelaRepository.findById(20L)).thenReturn(Optional.of(ultimaParcela));
        when(parcelaRepository.findByEmprestimoId(10L))
            .thenReturn(List.of(ultimaParcela, parcelaJaPaga));

        emprestimoService.pagarParcela(1L, 10L, 20L);

        assertEquals(StatusEmprestimo.QUITADO, emprestimo.getStatus());
    }

    @Test
    void deveRecusarPagarParcelaDeOutroEmprestimo() {
        Emprestimo emprestimoDaParcela = mock(Emprestimo.class);
        when(emprestimoDaParcela.getId()).thenReturn(2L);

        Conta conta = new Conta();
        ReflectionTestUtils.setField(conta, "id", 1L);

        Emprestimo emprestimoInformado = mock(Emprestimo.class);
        when(emprestimoInformado.getConta()).thenReturn(conta);

        Parcela parcela = new Parcela();
        parcela.setEmprestimo(emprestimoDaParcela);

        when(emprestimoRepository.findById(10L)).thenReturn(Optional.of(emprestimoInformado));
        when(parcelaRepository.findById(20L)).thenReturn(Optional.of(parcela));

        assertThrows(RecursoNaoEncontradoException.class, () -> emprestimoService.pagarParcela(1L, 10L, 20L));
    }
}
