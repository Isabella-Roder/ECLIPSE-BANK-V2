package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class CalculadoraRentabilidade {
    
    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal DIAS_NO_ANO = new BigDecimal("365");

    public BigDecimal calcularSaldoAtual(
        BigDecimal valorAplicado,
        BigDecimal taxaAnual,
        long dias
    ) {

        if (dias < 0) {
            throw new IllegalArgumentException("A quantidade de dias não pode ser negativa");
        }

        BigDecimal taxaDecimal = taxaAnual.divide(CEM, 10, RoundingMode.HALF_UP);

        BigDecimal periodo = BigDecimal.valueOf(dias).divide(DIAS_NO_ANO, 10, RoundingMode.HALF_UP);

        BigDecimal rendimento = valorAplicado.multiply(taxaDecimal).multiply(periodo);

        return valorAplicado.add(rendimento).setScale(2, RoundingMode.HALF_UP);
    }

}
