package com.eclipsebank.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.EmpresaCadastro;
import com.eclipsebank.backend.dto.EmpresaResposta;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Empresa;
import com.eclipsebank.backend.models.Usuario;
import com.eclipsebank.backend.repository.EmpresaRepository;
import com.eclipsebank.backend.repository.UsuarioRepository;

@Service
public class EmpresaService {
    
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public EmpresaService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Empresa buscarEntidade(Long empresaId) {
        return empresaRepository.findById(empresaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa não encontrada com o ID: " + empresaId));
    }

    @Transactional
    public EmpresaResposta cadastrar(Long usuarioId, EmpresaCadastro dados) {
        Usuario usuarioResponsavel = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario não encontrado com o ID: " + usuarioId));

        if (empresaRepository.existsByCnpj(dados.cnpj())) {
            throw new ConflitoException("Já existe uma empresa cadastrada.");
        }

        Empresa empresa = new Empresa();
        empresa.setCnpj(dados.cnpj());
        empresa.setRazaoSocial(dados.razaoSocial());
        empresa.setNomeFantasia(dados.nomeFantasia());
        empresa.setUsuarioResponsavel(usuarioResponsavel);

        return EmpresaResposta.from(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public Long obterUsuarioResponsavelId(Long empresaId) {
        return empresaRepository.buscarUsuarioResponsavelIdPelaEmpresaId(empresaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empressa não encontrado com o ID: " + empresaId));
    }

    @Transactional(readOnly = true) 
    public List<EmpresaResposta> listarPorUsuarioResponsavel(Long usuarioId) {
        return empresaRepository
            .findByUsuarioResponsavelId(usuarioId)
            .stream()
            .map(EmpresaResposta::from)
            .toList();
    }
}
