package com.eclipsebank.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eclipsebank.backend.models.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    @Query("select e.usuarioResponsavel.id from Empresa e where e.id = :empresaId")
    Optional<Long> buscarUsuarioResponsavelIdPelaEmpresaId(@Param("empresaId") Long empresaId);
}
