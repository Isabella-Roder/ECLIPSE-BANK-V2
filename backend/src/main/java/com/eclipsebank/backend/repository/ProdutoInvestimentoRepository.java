package com.eclipsebank.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.ProdutoInvestimento;

public interface ProdutoInvestimentoRepository extends JpaRepository<ProdutoInvestimento, Long> {
    
    boolean existsByCodigoIgnoreCase(String codigo);

    List<ProdutoInvestimento> findByAtivoTrueOrderByNomeAsc();
}
