package com.eclipsebank.backend.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.eclipsebank.backend.enums.StatusEmprestimo;
import com.eclipsebank.backend.exception.ConflitoException;

public class EmprestimoTest {
    
    @Test
    void deveAprovarQuandoStatusSOLICITADO() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setStatus(StatusEmprestimo.SOLICITADO);

        emprestimo.aprovar();

        assertEquals(StatusEmprestimo.APROVADO, emprestimo.getStatus());
    }

    @Test
    void deveRecusarQuandoStatusNaoSOLICITADO() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setStatus(StatusEmprestimo.INADIMPLENTE);

        assertThrows(ConflitoException.class, () -> emprestimo.aprovar());
    }

    @Test
    void deveIniciarQuandoStatusAPROVADO() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setStatus(StatusEmprestimo.APROVADO);

        emprestimo.iniciar();

        assertEquals(StatusEmprestimo.EM_ANDAMENTO, emprestimo.getStatus());
    }

    @Test
    void deveRecusarIniciarQuandoStatusNaoAPROVADO() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setStatus(StatusEmprestimo.SOLICITADO);

        assertThrows(ConflitoException.class, () -> emprestimo.iniciar());
    }
}
