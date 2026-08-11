package com.eclipsebank.backend.exception;

import java.time.Instant;
import java.util.Map;

public record ErroResposta(
    Instant timestamp,
    int status,
    String erro,
    String mensagem,
    String caminho,
    Map<String, String> campos
) {
}
