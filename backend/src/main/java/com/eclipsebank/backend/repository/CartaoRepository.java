package com.eclipsebank.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.Cartao;

public interface CartaoRepository extends JpaRepository<Cartao, Long>{
    
    List<Cartao> findByContaId(Long contaId);

    boolean existsByNumero(String numero);
}
