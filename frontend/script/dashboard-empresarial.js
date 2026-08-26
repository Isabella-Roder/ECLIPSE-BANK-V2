const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const nomeEmpresa = document.getElementById("nome-empresa-dashboard");
const botaoSair = document.getElementById("botao-sair-empresa");
const botaoOcultarSaldo = document.getElementById("botao-ocultar-saldo-empresa");
const saldoConta = document.getElementById("saldo-conta-empresa");
const agenciaConta = document.getElementById("agencia-conta-empresa");
const numeroConta = document.getElementById("numero-conta-empresa");
const cnpjConta = document.getElementById("cnpj-dashboard-empresa");
const recebimentosMes = document.getElementById("recebimentos-mes-empresa");
const pagamentosMes = document.getElementById("pagamentos-mes-empresa");
const resultadoMes = document.getElementById("resultado-mes-empresa");
const listaMovimentacoes = document.getElementById("lista-movimentacoes-empresa");
const nomeFantasia = document.getElementById("nome-fantasia-dashboard");
const razaoSocial = document.getElementById("razao-social-dashboard");
const responsavel = document.getElementById("responsavel-dashboard");
const statusConta = document.getElementById("status-conta-dashboard");
const mensagem = document.getElementById("mensagem-dashboard-empresa");

let contaAtual;
let saldoAtual = 0;
let saldoOculto = false;

const tiposDeEntrada = new Set([
    "DEPOSITO",
    "TRANSFERENCIA_RECEBIDA",
    "PIX_RECEBIDO",
    "RESGATE_INVESTIMENTO",
    "RESGATE_META_FINANCEIRA",
    "PROVENTO_FII",
    "EMPRESTIMO_LIBERADO"
]);

function lerCookie(nome) {
    const valor = document.cookie
        .split("; ")
        .find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function formatarCnpj(valor) {
    return String(valor)
        .replace(/\D/g, "")
        .replace(/^(\d{2})(\d)/, "$1.$2")
        .replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3")
        .replace(/\.(\d{3})(\d)/, ".$1/$2")
        .replace(/(\/\d{4})(\d)/, "$1-$2");
}

function formatarTipo(tipo) {
    return tipo
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/^\w/, letra => letra.toUpperCase());
}

function alterarMensagem(texto = "", tipo = "") {
    mensagem.className = tipo
        ? `mensagem-operacao ${tipo}`
        : "mensagem-operacao";
    mensagem.textContent = texto;
}

async function buscarJson(url) {
    const resposta = await fetch(url, {
        credentials: "include"
    });

    if (resposta.status === 401) {
        window.location.href = "login.html";
        return null;
    }

    const corpo = await resposta.json();

    if (!resposta.ok) {
        throw new Error(corpo.mensagem || "Não foi possível carregar os dados.");
    }

    return corpo;
}

async function carregarSessao() {
    return buscarJson(`${API_URL}/usuarios/sessao`);
}

async function carregarEmpresas() {
    return buscarJson(`${API_URL}/empresas/minhas-empresas`);
}

async function carregarContaEmpresarial(empresaId) {
    return buscarJson(`${API_URL}/contas/empresa/${empresaId}`);
}

async function carregarExtrato(contaId) {
    return buscarJson(`${API_URL}/contas/${contaId}/extrato`);
}

function selecionarEmpresa(empresas) {
    if (empresas.length === 0) {
        return null;
    }

    const parametros = new URLSearchParams(window.location.search);
    const empresaIdInformado = Number(parametros.get("empresaId"));

    if (!empresaIdInformado) {
        return empresas[0];
    }

    const empresaSelecionada = empresas.find(
        empresa => empresa.id === empresaIdInformado
    );

    if (!empresaSelecionada) {
        throw new Error("A empresa selecionada não pertence ao usuário autenticado.");
    }

    return empresaSelecionada;
}

function preencherEmpresa(empresa, usuario) {
    const nomeExibido = empresa.nomeFantasia || empresa.razaoSocial;

    nomeEmpresa.textContent = nomeExibido;
    nomeFantasia.textContent = empresa.nomeFantasia || "Não informado";
    razaoSocial.textContent = empresa.razaoSocial;
    responsavel.textContent = usuario.nome;
    cnpjConta.textContent = formatarCnpj(empresa.cnpj);
}

function preencherConta(conta) {
    contaAtual = conta;
    saldoAtual = Number(conta.saldo);

    agenciaConta.textContent = conta.agencia;
    numeroConta.textContent = conta.numero;
    statusConta.textContent = conta.status;

    atualizarVisibilidadeSaldo();
}

function atualizarVisibilidadeSaldo() {
    saldoConta.textContent = saldoOculto
        ? "R$ ••••••"
        : formatarDinheiro(saldoAtual);

    botaoOcultarSaldo.textContent = saldoOculto ? "Mostrar" : "Ocultar";
    botaoOcultarSaldo.setAttribute("aria-pressed", String(saldoOculto));
    botaoOcultarSaldo.setAttribute(
        "aria-label",
        saldoOculto
            ? "Mostrar saldo da conta empresarial"
            : "Ocultar saldo da conta empresarial"
    );
}

function movimentacaoEhEntrada(movimentacao) {
    return tiposDeEntrada.has(movimentacao.tipo);
}

function movimentacaoEhDoMesAtual(movimentacao) {
    const dataMovimentacao = new Date(movimentacao.criadaEm);
    const hoje = new Date();

    return dataMovimentacao.getMonth() === hoje.getMonth()
        && dataMovimentacao.getFullYear() === hoje.getFullYear();
}

function calcularIndicadores(movimentacoes) {
    const movimentacoesDoMes = movimentacoes.filter(
        movimentacao => movimentacaoEhDoMesAtual(movimentacao)
            && movimentacao.status === "CONCLUIDA"
    );

    const totalRecebimentos = movimentacoesDoMes
        .filter(movimentacaoEhEntrada)
        .reduce(
            (total, movimentacao) => total + Number(movimentacao.valor),
            0
        );

    const totalPagamentos = movimentacoesDoMes
        .filter(movimentacao => !movimentacaoEhEntrada(movimentacao))
        .reduce(
            (total, movimentacao) => total + Number(movimentacao.valor),
            0
        );

    recebimentosMes.textContent = formatarDinheiro(totalRecebimentos);
    pagamentosMes.textContent = formatarDinheiro(totalPagamentos);
    resultadoMes.textContent = formatarDinheiro(
        totalRecebimentos - totalPagamentos
    );
}

function renderizarMovimentacoes(movimentacoes) {
    listaMovimentacoes.replaceChildren();

    if (movimentacoes.length === 0) {
        const estadoVazio = document.createElement("p");
        estadoVazio.textContent = "Nenhuma movimentação empresarial encontrada.";
        listaMovimentacoes.appendChild(estadoVazio);
        return;
    }

    movimentacoes.slice(0, 5).forEach((movimentacao) => {
        const item = document.createElement("article");
        item.className = "item-movimentacao";

        const dados = document.createElement("div");

        const tipo = document.createElement("strong");
        tipo.textContent = formatarTipo(movimentacao.tipo);

        const descricao = document.createElement("span");
        descricao.textContent = movimentacao.descricao || "Sem descrição";

        dados.append(tipo, descricao);

        const valores = document.createElement("div");

        const valor = document.createElement("strong");
        const sinal = movimentacaoEhEntrada(movimentacao) ? "+" : "-";
        valor.textContent = `${sinal} ${formatarDinheiro(movimentacao.valor)}`;

        const data = document.createElement("span");
        data.textContent = new Date(movimentacao.criadaEm).toLocaleString("pt-BR");

        valores.append(valor, data);
        item.append(dados, valores);
        listaMovimentacoes.appendChild(item);
    });
}

async function sair() {
    try {
        botaoSair.disabled = true;
        botaoSair.textContent = "Saindo...";

        await fetch(`${API_URL}/usuarios/logout`, {
            method: "POST",
            credentials: "include",
            headers: {
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });
    } catch (erro) {
        console.error("Erro ao encerrar sessão:", erro);
    } finally {
        window.location.href = "login.html";
    }
}

botaoOcultarSaldo.addEventListener("click", () => {
    saldoOculto = !saldoOculto;
    atualizarVisibilidadeSaldo();
});

botaoSair.addEventListener("click", sair);

async function iniciarPagina() {
    try {
        alterarMensagem("Carregando painel empresarial...");

        const usuario = await carregarSessao();
        if (!usuario) return;

        const empresas = await carregarEmpresas();
        if (!empresas) return;

        const empresa = selecionarEmpresa(empresas);

        if (!empresa) {
            window.location.href = "cadastro-empresa.html";
            return;
        }

        preencherEmpresa(empresa, usuario);

        const conta = await carregarContaEmpresarial(empresa.id);
        if (!conta) return;

        preencherConta(conta);

        const movimentacoes = await carregarExtrato(conta.id);
        if (!movimentacoes) return;

        calcularIndicadores(movimentacoes);
        renderizarMovimentacoes(movimentacoes);
        alterarMensagem();
    } catch (erro) {
        console.error(erro);
        alterarMensagem(`Erro: ${erro.message}`, "erro");
    }
}

iniciarPagina();
