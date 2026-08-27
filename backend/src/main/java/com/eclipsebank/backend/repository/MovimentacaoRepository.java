package com.eclipsebank.backend.repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.models.Movimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long>{
    
    List<Movimentacao> findByContaIdOrderByCriadaEmDesc(Long contaId);

    Optional<Movimentacao> findByCodigo(String codigo);

    long countByContaIdAndTipo(Long contaId, TipoMovimentacao tipo);

    @Query("select avg(m.valor) from Movimentacao m where m.conta.id = :contaId and m.tipo = :tipo")
    BigDecimal calcularValorMedioPorContaETipo(@Param("contaId") Long contaId, @Param("tipo") TipoMovimentacao tipo);

}
