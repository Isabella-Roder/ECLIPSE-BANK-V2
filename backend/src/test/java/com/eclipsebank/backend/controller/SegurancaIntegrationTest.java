package com.eclipsebank.backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.models.Usuario;
import com.eclipsebank.backend.repository.UsuarioRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SegurancaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long usuarioId;

    @BeforeEach
    void preparar() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Seguranca");
        usuario.setCpf("22222222222");
        usuario.setEmail("seguranca@teste.com");
        usuario.setSenhaHash(passwordEncoder.encode("senha12345"));
        usuario = usuarioRepository.save(usuario);
        usuarioId = usuario.getId();
    }

    private Authentication autenticacaoDoUsuario() {
        return UsernamePasswordAuthenticationToken.authenticated(
            usuarioId,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }

    // --- CSRF ---

    @Test
    void deveRecusarLoginSemTokenCsrf() throws Exception {
        String corpo = objectMapper.writeValueAsString(
            new Object() {
                public final String email = "seguranca@teste.com";
                public final String senha = "senha12345";
            }
        );

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isForbidden());
    }

    @Test
    void deveAceitarLoginComTokenCsrfValido() throws Exception {
        String corpo = objectMapper.writeValueAsString(
            new Object() {
                public final String email = "seguranca@teste.com";
                public final String senha = "senha12345";
            }
        );

        mockMvc.perform(post("/api/usuarios/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isOk());
    }

    @Test
    void deveRecusarOperacaoAutenticadaSemTokenCsrf() throws Exception {
        mockMvc.perform(post("/api/contas/usuario/" + usuarioId)
                .with(authentication(autenticacaoDoUsuario())))
            .andExpect(status().isForbidden());
    }

    @Test
    void deveAceitarOperacaoAutenticadaComTokenCsrfValido() throws Exception {
        mockMvc.perform(post("/api/contas/usuario/" + usuarioId)
                .with(authentication(autenticacaoDoUsuario()))
                .with(csrf()))
            .andExpect(status().isCreated());
    }

    // --- Sessão ausente/expirada ---

    @Test
    void deveRecusarAcessoSemSessaoValida() throws Exception {
        mockMvc.perform(get("/api/contas/usuario/" + usuarioId))
            .andExpect(status().isUnauthorized());
    }

    // --- Força bruta (linha de base: sem rate limiting ainda) ---

    @Test
    void deveManterMensagemGenericaEmTentativasRepetidasDeLoginInvalido() throws Exception {
        String corpoInvalido = objectMapper.writeValueAsString(
            new Object() {
                public final String email = "seguranca@teste.com";
                public final String senha = "senhaErrada";
            }
        );

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            mockMvc.perform(post("/api/usuarios/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpoInvalido))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(
                    org.hamcrest.Matchers.containsString("E-mail ou senha inválidos")
                ));
        }
    }
}
