package com.eclipsebank.backend.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.eclipsebank.backend.dto.ProdutoInvestimentoCadastro;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;
import com.eclipsebank.backend.service.ProdutoInvestimentoService;

@Component
public class ProdutoInvestimentoInicializador implements CommandLineRunner{
    
    private final ProdutoInvestimentoRepository produtoInvestimentoRepository;
    private final ProdutoInvestimentoService produtoInvestimentoService;

    public ProdutoInvestimentoInicializador(
        ProdutoInvestimentoRepository produtoInvestimentoRepository,
        ProdutoInvestimentoService produtoInvestimentoService
    ) {
        this.produtoInvestimentoRepository = produtoInvestimentoRepository;
        this.produtoInvestimentoService = produtoInvestimentoService;
    }

    private void cadastrarSeNaoExistir(
        String nome,
        String codigo,
        TipoInvestimento tipo,
        String valorMinimo,
        String rentabilidade
    ){
        if (produtoInvestimentoRepository.existsByCodigoIgnoreCase(codigo)) {
            return;
        }

        ProdutoInvestimentoCadastro dados = new ProdutoInvestimentoCadastro(
            nome,
            codigo,
            tipo,
            new BigDecimal(valorMinimo),
            new BigDecimal(rentabilidade),
            null,
            null
        );

        produtoInvestimentoService.cadastrar(dados);
    }

    @Override
    public void run(String... args) {
        cadastrarSeNaoExistir(
            "CDB Eclipse",
            "CDBELC",
            TipoInvestimento.RENDA_FIXA,
            "100.00",
            "12.50"
        );

        cadastrarSeNaoExistir(
            "Fundo Órbita",
            "FORBITA",
            TipoInvestimento.FUNDO,
            "50.00",
            "10.80"
        );

        cadastrarSeNaoExistir(
            "Ação Eclipse",
            "ACAOECL",
            TipoInvestimento.ACAO,
            "25.00",
            "15.00"
        );

        cadastrarSeNaoExistir(
            "ETF Horizonte",
            "ETFHORIZ",
            TipoInvestimento.ETF,
            "75.00",
            "13.20"
        );

        cadastrarSeNaoExistir(
            "Cripto Lunar",
            "CRIPLUN",
            TipoInvestimento.CRIPTOMOEDA,
            "10.00",
            "20.00"
        );
    }
}
