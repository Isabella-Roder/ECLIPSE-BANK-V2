const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const nomeUsuario = document.getElementById("nome-usuario");
const saldoAtual = document.getElementById("saldo-atual");
const filtroTipo = document.getElementById("filtro-tipo");
const dataInicial = document.getElementById("data-inicial");
const dataFinal = document.getElementById("data-final");
const botaoLimpar = document.getElementById("botao-limpar-filtros");
const quantidade = document.getElementById("quantidade-movimentacoes");
const listaExtrato = document.getElementById("lista-extrato");
const mensagem = document.getElementById("mensagem-extrato");
const botaoExportarCsv = document.getElementById("botao-exportar-csv");
const botaoImprimirExtrato = document.getElementById("botao-imprimir-extrato");

let movimentacoes = [];
let movimentacoesExibidas = [];

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

async function carregarConta(usuarioId) {
    try {
        mensagem.textContent = "Carregando conta...";

        const resposta = await fetch(`${API_URL}/contas/usuario/${usuarioId}`, {
            credentials: "include"
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao carregar conta");
        }

        saldoAtual.textContent = formatarDinheiro(corpo.saldo);

        await carregarExtrato(corpo.id);
        mensagem.textContent = "";
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

async function carregarExtrato(contaId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaId}/extrato`, {
            credentials: "include"
        });

        if (!resposta.ok) {
            const erro = await resposta.text();
            throw new Error(erro|| "Erro ao carregar extrato");
        }

        movimentacoes = await resposta.json();
        renderizarExtrato(movimentacoes);

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarExtrato(lista) {
    movimentacoesExibidas = [...lista];
    listaExtrato.replaceChildren();
    quantidade.textContent = `${lista.length} Movimentação(ões)`;

    if (lista.length === 0) {
        const estadoExtrato = document.createElement("p");
        estadoExtrato.className = "estado-extrato";
        estadoExtrato.textContent = "Nenhuma movimentação encontrada.";
        listaExtrato.appendChild(estadoExtrato);
        return;
    }

    lista.forEach((movimentacao) => {
        const linha = document.createElement("article");

        const credito = movimentacao.tipo === "DEPOSITO" ||
            movimentacao.tipo === "TRANSFERENCIA_RECEBIDA"||
            movimentacao.tipo === "PIX_RECEBIDO"||
            movimentacao.tipo === "RESGATE_INVESTIMENTO";

        const sinal = credito ? "+" : "-";
        const classeTipo = credito ? "credito" : "debito";

        const data = new Date(movimentacao.criadaEm).toLocaleString("pt-BR");

        linha.className = `linha-extrato ${classeTipo}`;

        const dadosMovimentacao = document.createElement("div");
        dadosMovimentacao.className = "dados-movimentacao";

        const tipoMovimentacao = document.createElement("span");
        tipoMovimentacao.className = "tipo-movimentacao";
        tipoMovimentacao.textContent = movimentacao.tipo;

        const descricaoMovimentacao = document.createElement("strong");
        descricaoMovimentacao.textContent = movimentacao.descricao || "Sem descrição";

        const dataMovimentacao = document.createElement("span");
        dataMovimentacao.textContent = data;

        dadosMovimentacao.append(
            tipoMovimentacao,
            descricaoMovimentacao,
            dataMovimentacao
        );

        const valoresMovimentacao = document.createElement("div");
        valoresMovimentacao.className = "valores-movimentacao";

        const valorMovimentacao = document.createElement("strong");
        valorMovimentacao.textContent = `${sinal} ${formatarDinheiro(movimentacao.valor)}`;

        const saldoResultante = document.createElement("span");
        saldoResultante.textContent = `Saldo: ${formatarDinheiro(movimentacao.saldoResultante)}`;

        const linkComprovante = document.createElement("a");
        const parametros = new URLSearchParams({ codigo: movimentacao.codigo });
        linkComprovante.className = "link-comprovante";
        linkComprovante.href = `comprovante.html?${parametros.toString()}`;
        linkComprovante.textContent = "Ver comprovante";

        valoresMovimentacao.append(
            valorMovimentacao,
            saldoResultante,
            linkComprovante
        );

        linha.append(dadosMovimentacao, valoresMovimentacao);

        listaExtrato.appendChild(linha);
    });
}

function protegerFormulaCsv(valor) {
    const texto = String(valor ?? "");

    return /^[=+\-@]/.test(texto) ? `'${texto}` : texto;
}

function escaparCampoCsv(valor) {
    const textoSeguro = protegerFormulaCsv(valor).replaceAll('"', '""');

    return `"${textoSeguro}"`;
}

function exportarExtratoCsv() {
    if (movimentacoesExibidas.length === 0) {
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Não há movimentações para exportar.";
        return;
    }

    const cabecalho = [
        "Data",
        "Tipo",
        "Descrição",
        "Valor",
        "Saldo resultante",
        "Status",
        "Código"
    ];

    const linhas = movimentacoesExibidas.map((movimentacao) => [
        new Date(movimentacao.criadaEm).toLocaleString("pt-BR"),
        movimentacao.tipo,
        movimentacao.descricao || "Sem descrição",
        Number(movimentacao.valor).toFixed(2).replace(".", ","),
        Number(movimentacao.saldoResultante).toFixed(2).replace(".", ","),
        movimentacao.status,
        movimentacao.codigo
    ]);

    const conteudoCsv = [cabecalho, ...linhas]
        .map((linha) => linha.map(escaparCampoCsv).join(";"))
        .join("\n");

    const arquivo = new Blob(["\uFEFF", conteudoCsv], {
        type: "text/csv;charset=utf-8"
    });
    const url = URL.createObjectURL(arquivo);
    const link = document.createElement("a");
    const dataArquivo = new Date().toISOString().slice(0, 10);

    link.href = url;
    link.download = `extrato-eclipse-bank-${dataArquivo}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);

    mensagem.className = "mensagem-operacao sucesso";
    mensagem.textContent = "Extrato exportado em CSV com sucesso.";
}

function aplicarFiltros() {
    let resultado = movimentacoes;

    if (filtroTipo.value !== "TODAS") {
        resultado = resultado.filter((movimentacao) => {
            return movimentacao.tipo === filtroTipo.value;
        });
    }

    if (dataInicial.value) {
        resultado = resultado.filter((movimentacao) => {
            const dataMovimentacao = movimentacao.criadaEm.slice(0, 10);

            return dataMovimentacao >= dataInicial.value;
        });
    }

    if (dataFinal.value) {
        resultado = resultado.filter((movimentacao) => {
            const dataMovimentacao = movimentacao.criadaEm.slice(0, 10);

            return dataMovimentacao <= dataFinal.value;
        })
    }

    renderizarExtrato(resultado);
}

filtroTipo.addEventListener("change", aplicarFiltros);

dataInicial.addEventListener("change", aplicarFiltros);

dataFinal.addEventListener("change", aplicarFiltros);

botaoLimpar.addEventListener("click", () => {
    filtroTipo.value = "TODAS";
    dataInicial.value = "";
    dataFinal.value = "";

    renderizarExtrato(movimentacoes);
});

botaoExportarCsv.addEventListener("click", exportarExtratoCsv);

botaoImprimirExtrato.addEventListener("click", () => {
    if (movimentacoesExibidas.length === 0) {
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Não há movimentações para imprimir.";
        return;
    }

    window.print();
});

async function iniciarPagina() {
    const resposta = await fetch(`${API_URL}/usuarios/sessao`, {
        credentials: "include"
    });

    if (!resposta.ok) {
        window.location.href = "login.html";
        return;
    }

    const usuario = await resposta.json();
    nomeUsuario.textContent = `Olá, ${usuario.nome}`;
    await carregarConta(usuario.id);
}

iniciarPagina();
