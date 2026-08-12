package com.eclipsebank.backend.exception;

public class ContaIndisponivelException extends RuntimeException {
    
    public ContaIndisponivelException() {
        super("A conta não está disponivel para movimentações");
    }
}
