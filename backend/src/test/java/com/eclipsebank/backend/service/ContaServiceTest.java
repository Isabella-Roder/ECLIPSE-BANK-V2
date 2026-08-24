package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.eclipsebank.backend.dto.ContaResposta;
import com.eclipsebank.backend.enums.TipoConta;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Empresa;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.EmpresaRepository;
import com.eclipsebank.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    private ContaService contaService;

    @BeforeEach
    void preparar() {
        contaService = new ContaService(contaRepository, usuarioRepository, empresaRepository);
    }

    private Empresa criarEmpresaAtiva() {
        Empresa empresa = new Empresa();
        ReflectionTestUtils.setField(empresa, "id", 1L);
        empresa.setCnpj("12345678000190");
        empresa.setRazaoSocial("Empresa Teste LTDA");
        empresa.setAtiva(true);

        return empresa;
    }

    @Test
    void deveCriarContaParaEmpresaComTipoPessoaJuridica() {
        Empresa empresa = criarEmpresaAtiva();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(contaRepository.existsByEmpresaId(1L)).thenReturn(false);
        when(contaRepository.existsByAgenciaAndNumero(any(), any())).thenReturn(false);
        when(contaRepository.save(any(Conta.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        ContaResposta resposta = contaService.criarParaEmpresa(1L);

        assertEquals(TipoConta.PESSOA_JURIDICA, resposta.tipo());
        assertEquals(1L, resposta.empresaId());
    }

    @Test
    void deveRecusarCriarContaQuandoEmpresaNaoExiste() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> contaService.criarParaEmpresa(1L));
    }

    @Test
    void deveRecusarCriarContaQuandoEmpresaEstaInativa() {
        Empresa empresa = criarEmpresaAtiva();
        empresa.setAtiva(false);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertThrows(ConflitoException.class, () -> contaService.criarParaEmpresa(1L));
    }

    @Test
    void deveRecusarCriarContaQuandoEmpresaJaPossuiConta() {
        Empresa empresa = criarEmpresaAtiva();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(contaRepository.existsByEmpresaId(1L)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> contaService.criarParaEmpresa(1L));
    }
}
