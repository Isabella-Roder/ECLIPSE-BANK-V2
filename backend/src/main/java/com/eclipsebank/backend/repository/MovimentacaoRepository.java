package com.eclipsebank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.Movimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long>{
    
    List<Movimentacao> findByContaIdOrderByCriadaEmDesc(Long contaId);

    Optional<Movimentacao> findByCodigo(String codigo);
}
