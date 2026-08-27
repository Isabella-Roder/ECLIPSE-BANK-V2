package com.eclipsebank.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eclipsebank.backend.dto.LoginRequisicao;
import com.eclipsebank.backend.dto.UsuarioAtualizacao;
import com.eclipsebank.backend.dto.UsuarioCadastro;
import com.eclipsebank.backend.dto.UsuarioResposta;
import com.eclipsebank.backend.enums.AcaoAuditoria;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.CredenciaisInvalidasException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Usuario;
import com.eclipsebank.backend.repository.UsuarioRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("E-mail já cadastrado.");
        }
    }

    private void validarCpfDisponivel(String cpf) {
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new ConflitoException("CPF já cadastrado.");
        }
    }

    private String normalizarEmail(String email) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        if (emailNormalizado.isBlank()) {
            throw new IllegalArgumentException("O e-mail não pode estar vazio.");
        }

        return emailNormalizado;
    }

    private String normalizarCpf(String cpf) {
        String somenteNumeros = cpf.replaceAll("\\D", "");

        if (somenteNumeros.length() != 11) {
            throw new IllegalArgumentException("O CPF deve possuir 11 números");
        }

        return somenteNumeros;
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }

        return telefone.replaceAll("\\D", "");
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    @Transactional
    public UsuarioResposta cadastrar(UsuarioCadastro dados) {
        String email = normalizarEmail(dados.email());
        String cpf = normalizarCpf(dados.cpf());

        validarEmailDisponivel(email);
        validarCpfDisponivel(cpf);

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setNomeSocial(normalizarTextoOpcional(dados.nomeSocial()));
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(dados.senha()));
        usuario.setTelefone(normalizarTelefone(dados.telefone()));
        usuario.setDataNascimento(dados.dataNascimento());

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioResposta.from(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResposta> listar() {
        return usuarioRepository.findAll().stream()
            .map(UsuarioResposta::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResposta buscarPorId(Long id) {
        return UsuarioResposta.from(buscarEntidade(id));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> 
                new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + id)
            );
    }

    @Transactional
    public UsuarioResposta atualizar(Long id, UsuarioAtualizacao dados) {
        Usuario usuario = buscarEntidade(id);

        if (dados.nome() != null) {
            if (dados.nome().isBlank()) {
                throw new IllegalArgumentException("O nome não pode estar vazio.");
            }

            usuario.setNome(dados.nome().trim());
        }

        if (dados.email() != null) {
            String novoEmail = normalizarEmail(dados.email());

            if (!novoEmail.equals(usuario.getEmail())) {
                validarEmailDisponivel(novoEmail);
                usuario.setEmail(novoEmail);
            }
        }

        if (dados.telefone() != null) {
            usuario.setTelefone(normalizarTelefone(dados.telefone()));
        }

        if (dados.dataNascimento() != null) {
            usuario.setDataNascimento(dados.dataNascimento());
        }

        return UsuarioResposta.from(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(false);
    }
    
    @Transactional
    public UsuarioResposta login(LoginRequisicao dados) {
        String email = normalizarEmail(dados.email());

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        boolean credenciaisValidas = usuario != null
            && usuario.getAtivo()
            && passwordEncoder.matches(dados.senha(), usuario.getSenhaHash());

        if (!credenciaisValidas) {
            Long usuarioId = usuario != null ? usuario.getId() : null;
            auditoriaService.registrar(usuarioId, AcaoAuditoria.LOGIN_FALHA, "Tentativa de login: " + email);
            throw new CredenciaisInvalidasException();
        }

        auditoriaService.registrar(usuario.getId(), AcaoAuditoria.LOGIN_SUCESSO, null);
        return UsuarioResposta.from(usuario);
    }
}
