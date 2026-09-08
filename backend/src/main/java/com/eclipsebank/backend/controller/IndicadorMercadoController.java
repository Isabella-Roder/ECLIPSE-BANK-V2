package com.eclipsebank.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsebank.backend.dto.CambioResposta;
import com.eclipsebank.backend.dto.IndicadorMercadoResposta;
import com.eclipsebank.backend.service.CambioService;
import com.eclipsebank.backend.service.IndicadorMercadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag (name = "Indicadores de mercado", description = "Dados públicos do Banco Central")
@RestController
@RequestMapping("/api/mercado")
public class IndicadorMercadoController {
    
    private final IndicadorMercadoService indicadorMercadoService;
    private final CambioService cambioService;

    public IndicadorMercadoController(IndicadorMercadoService indicadorMercadoService, CambioService cambioService) {
        this.indicadorMercadoService = indicadorMercadoService;
        this.cambioService = cambioService;
    }

    @Operation(summary = "Selic", description = "Retorna a taxa Selic mais recente")
    @GetMapping("/selic")
    public ResponseEntity<IndicadorMercadoResposta> buscarSelic() {
        return ResponseEntity.ok(indicadorMercadoService.buscarSelic());
    }

    @Operation(summary = "CDI", description = "Retorna a taxa CDI mais recente") 
    @GetMapping("/cdi")
    public ResponseEntity<IndicadorMercadoResposta> buscarCDI() {
        return ResponseEntity.ok(indicadorMercadoService.buscarCDI());
    }

    @Operation(summary = "IPCA", description = "Retorna a taxa IPCA mais recente")
    @GetMapping("/ipca")
    public ResponseEntity<IndicadorMercadoResposta> buscarIPCA() {
        return ResponseEntity.ok(indicadorMercadoService.buscarIPCA());
    }

    @Operation(summary = "Câmbio", description = "Retorna a cotação da moeda em reais")
    @GetMapping("/cambio/{moeda}")
    public ResponseEntity<CambioResposta> buscarCambio(
        @PathVariable String moeda
    ) {
        return ResponseEntity.ok(cambioService.buscarCotacao(moeda));
    }
}
