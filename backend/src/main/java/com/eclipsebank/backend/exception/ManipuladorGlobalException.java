package com.eclipsebank.backend.exception;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ManipuladorGlobalException {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ManipuladorGlobalException.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(
        RecursoNaoEncontradoException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.NOT_FOUND,
            "Recurso não encontrado",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(
        ConflitoException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.CONFLICT,
            "Conflito",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarArgumentoInvalido(
        IllegalArgumentException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.BAD_REQUEST,
            "Dados inválidos",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(erro ->
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage())
        );

        return criarResposta(
            HttpStatus.BAD_REQUEST,
            "Erro de validação",
            "Um ou mais campos estão inválidos",
            request,
            campos
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.BAD_REQUEST,
            "JSON inválido",
            "Não foi possível interpretar os dados enviados",
            request,
            Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error("Erro inesperado na requisição {}", request.getRequestURI(), exception);

        return criarResposta(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno",
            "Ocorreu um erro inesperado",
            request,
            Map.of()
        );
    }

    private ResponseEntity<ErroResposta> criarResposta(
        HttpStatus status,
        String erro,
        String mensagem,
        HttpServletRequest request,
        Map<String, String> campos
    ) {
        ErroResposta resposta = new ErroResposta(
            Instant.now(),
            status.value(),
            erro,
            mensagem,
            request.getRequestURI(),
            campos
        );

        return ResponseEntity.status(status).body(resposta);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> tratarCredenciaisInvalidas(
        CredenciaisInvalidasException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.UNAUTHORIZED,
            "Não autorizado",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler({
        SaldoInsuficienteException.class,
        ContaIndisponivelException.class
    })
    public ResponseEntity<ErroResposta> tratarRegraBancaria(
        RuntimeException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Operação não permitida",
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> tratarAcessoNegado(
        AccessDeniedException exception,
        HttpServletRequest request
    ) {
        return criarResposta(
            HttpStatus.FORBIDDEN,
            "Acesso negado",
            "Você não possui permissão para acessar este recurso",
            request,
            Map.of()
        );
    }
}
