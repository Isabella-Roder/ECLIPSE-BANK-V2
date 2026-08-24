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

import com.eclipsebank.backend.dto.EmpresaCadastro;
import com.eclipsebank.backend.dto.EmpresaResposta;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Empresa;
import com.eclipsebank.backend.models.Usuario;
import com.eclipsebank.backend.repository.EmpresaRepository;
import com.eclipsebank.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private EmpresaService empresaService;

    @BeforeEach
    void preparar() {
        empresaService = new EmpresaService(empresaRepository, usuarioRepository);
    }

    @Test
    void deveCadastrarEmpresaVinculadaAoUsuarioResponsavel() {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(empresaRepository.existsByCnpj("12345678000190")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        EmpresaCadastro dados = new EmpresaCadastro("12345678000190", "Empresa Teste LTDA", "Empresa Teste");

        EmpresaResposta resposta = empresaService.cadastrar(1L, dados);

        assertEquals("12345678000190", resposta.cnpj());
        assertEquals("Empresa Teste LTDA", resposta.razaoSocial());
    }

    @Test
    void deveRecusarCadastrarQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        EmpresaCadastro dados = new EmpresaCadastro("12345678000190", "Empresa Teste LTDA", "Empresa Teste");

        assertThrows(RecursoNaoEncontradoException.class, () -> empresaService.cadastrar(1L, dados));
    }

    @Test
    void deveRecusarCadastrarQuandoCnpjJaExiste() {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(empresaRepository.existsByCnpj("12345678000190")).thenReturn(true);

        EmpresaCadastro dados = new EmpresaCadastro("12345678000190", "Empresa Teste LTDA", "Empresa Teste");

        assertThrows(ConflitoException.class, () -> empresaService.cadastrar(1L, dados));
    }

    @Test
    void deveObterUsuarioResponsavelId() {
        when(empresaRepository.buscarUsuarioResponsavelIdPelaEmpresaId(1L)).thenReturn(Optional.of(9L));

        Long usuarioResponsavelId = empresaService.obterUsuarioResponsavelId(1L);

        assertEquals(9L, usuarioResponsavelId);
    }

    @Test
    void deveRecusarObterUsuarioResponsavelQuandoEmpresaNaoExiste() {
        when(empresaRepository.buscarUsuarioResponsavelIdPelaEmpresaId(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> empresaService.obterUsuarioResponsavelId(1L));
    }
}
