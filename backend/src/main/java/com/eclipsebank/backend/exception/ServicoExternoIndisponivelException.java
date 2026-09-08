package com.eclipsebank.backend.exception;

public class ServicoExternoIndisponivelException extends RuntimeException {
    
    public ServicoExternoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
