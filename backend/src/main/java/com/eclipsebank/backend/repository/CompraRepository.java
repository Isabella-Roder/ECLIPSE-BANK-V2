package com.eclipsebank.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.Compra;

public interface CompraRepository extends JpaRepository<Compra, Long>{
    
    List<Compra> findByFaturaId(Long faturaId);
}
