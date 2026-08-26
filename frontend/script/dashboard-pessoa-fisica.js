const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const nomeUsuario = document.getElementById("nome-usuario-pessoal");
const botaoSair = document.getElementById("botao-sair-pessoal");
const botaoOcultarSaldo = document.getElementById("botao-ocultar-saldo-pessoal");
const saldoConta = document.getElementById("saldo-conta-pessoal");
const agenciaConta = document.getElementById("agencia-conta-pessoal");
const numeroConta = document.getElementById("numero-conta-pessoal");
const statusConta = document.getElementById("status-conta-pessoal");
const entradasMes = document.getElementById("entradas-mes-pessoal");
const saidasMes = document.getElementById("saidas-mes-pessoal")
const invetido = document.getElementById("investido-pessoal");
const tituloAcoes = document.getElementById("titulo-acoes-pessoais");
const tituloAtividade = document.getElementById("titulo-atividade-pessoal");
const listaMovimentacoes = document.getElementById("lista-movimentacoes-pessoal");
const estadoMovimentacoes = document.getElementById("estado-movimentacoes-pessoal");
const tituloProgresso = document.getElementById("titulo-progresso-pessoal");
const nomeMetaDestaque = document.getElementById("nome-meta-destaque");
const valorMetaDestaque = document.getElementById("valor-meta-destaque");
const progressoMeta = document.getElementById("progresso-meta-destaque");
const mensagem = document.getElementById("mensagem-dashboard-pessoal");

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
    const valor = document.cookie.split("; ").find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
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
        throw new Error(corpo.mensagem || "Não foi possivel carregar os dados.");
    }

    return corpo;
}

async function carregarSessao() {
    return buscarJson(`${API_URL}/usuarios/sessao`);
}

async function carregarConta(usuarioId) {
    return buscarJson(`${API_URL}/contas/usuario/${usuarioId}`);
}

async function carregarExtrato(contaId) {
    return buscarJson(`${API_URL}/contas/${contaId}/extrato`);
}

async function carregarCarteira(contaId) {
    return buscarJson(`${API_URL}/contas/${contaId}/investimentos/carteira`);
}

async function carregarMetas(contaId) {
    return buscarJson(`${API_URL}/contas/${contaId}/metas-financeiras/minhas-metas`);
}

function preencherUsuario(usuario) {
    nomeUsuario.textContent = `Olá, ${usuario.nome}`;
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

    botaoOcultarSaldo.textContent = saldoOculto
        ? "Mostrar"
        : "Ocultar";

    botaoOcultarSaldo.setAttribute("aria-pressed", String(saldoOculto));

    botaoOcultarSaldo.setAttribute(
        "aria-label",
        saldoOculto
            ? "Mostrar saldo da conta"
            : "Ocultar saldo da conta"
    );
}

function movimentacaoEhEntrada(movimentacao) {
    return tiposDeEntrada.has(movimentacao.tipo);
}

function movimentacaoEhDoMesAtual(movimentacao) {
    const dataMovimentacao = new Date(movimentacao.criadaEm);
    const hoje = new Date();

    return(
        dataMovimentacao.getMonth() === hoje.getMonth()
        && dataMovimentacao.getFullYear() === hoje.getFullYear()
    );
}

function calcularResumoMensal(movimentacoes) {
    const movimentacaoDoMes = movimentacoes.filter(
        movimentacaoEhDoMesAtual
    );

    const totalEntradas = movimentacaoDoMes.filter(movimentacaoEhEntrada)
        .reduce((total, movimentacao) => 
            total + Number(movimentacao.valor),
            0
        );

    const totalSaidas = movimentacaoDoMes
        .filter(movimentacao => !movimentacaoEhEntrada(movimentacao))
        .reduce((total, movimentacao) => 
            total + Number(movimentacao.valor),
            0
        );

    entradasMes.textContent = formatarDinheiro(totalEntradas);
    saidasMes.textContent = formatarDinheiro(totalSaidas);
}

function renderizarMovimentacoes(movimentacoes) {
    listaMovimentacoes.replaceChildren();

    if (movimentacoes.length === 0) {
        const estadoVazio = document.createElement("p")
        estadoVazio.textContent = "Nenhuma movimentação encontrada.";
        listaMovimentacoes.appendChild(estadoVazio);
        return;
    }

    movimentacoes.slice(0, 5)
        .forEach(movimentacao => {
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

            const sinal = movimentacaoEhEntrada(movimentacao)
                ? "+"
                : "-";

            valor.textContent = `${sinal} ${formatarDinheiro(movimentacao.valor)}`;

            const data = document.createElement("span");

            data.textContent = new Date(
                movimentacao.criadaEm
            ).toLocaleString("pt-BR");

            valores.append(valor, data);
            item.append(dados, valores);

            listaMovimentacoes.appendChild(item);
        });
}

function preencherCarteira(aplicacoes) {
    const aplicacoesAtivas = aplicacoes.filter(
        aplicacao => aplicacao.status === "ATIVA"
    );

    const totalInvestido = aplicacoesAtivas.reduce(
        (total, aplicacao) =>
            total + Number(aplicacao.saldoAtualEstimado),
            0
    );

    invetido.textContent = formatarDinheiro(totalInvestido);
}

function preencherMetaDestaque(metas) {
    const metaEmAndamento = metas.find(
        meta => meta.status === "EM_ANDAMENTO"
    );

    if (!metaEmAndamento) {
        nomeMetaDestaque.textContent = "Nenhuma meta em andamento";
        valorMetaDestaque.textContent = "R$ 0,00 de R$ 0,00";
        progressoMeta.style.width = "0%";

        progressoMeta.parentElement.setAttribute(
            "aria-valuenow",
            "0"
        );

        return;
    }

    const valorAtual = Number(metaEmAndamento.valorAtual);
    const valorAlvo = Number(metaEmAndamento.valorAlvo);

    const porcentagem = valorAlvo > 0
        ? Math.min((valorAtual / valorAlvo) * 100, 100)
        : 0;

    nomeMetaDestaque.textContent = metaEmAndamento.nome;

    valorMetaDestaque.textContent = `${formatarDinheiro(valorAtual)} de ` + formatarDinheiro(valorAlvo);

    progressoMeta.style.width = `${porcentagem}%`;

    progressoMeta.parentElement.setAttribute(
        "aria-valuenow",
        String(Math.round(porcentagem))
    );
}

async function carregarDadosSecundarios(contaId) {
    const resultados = await Promise.allSettled([
        carregarExtrato(contaId),
        carregarCarteira(contaId),
        carregarMetas(contaId)
    ]);

    const resultadoExtrato = resultados[0];
    const resultadoCarteira = resultados[1];
    const resultadoMetas = resultados[2];

    if (resultadoExtrato.status === "fulfilled") {
        const movimentacoes = resultadoExtrato.value;

        calcularResumoMensal(movimentacoes);
        renderizarMovimentacoes(movimentacoes);
    } else {
        console.error(resultadoExtrato.reason);
    }

    if (resultadoCarteira.status === "fulfilled") {
        preencherCarteira(resultadoCarteira.value);
    } else {
        console.error(resultadoCarteira.reason);
    }

    if (resultadoMetas.status === "fulfilled") {
        preencherMetaDestaque(resultadoMetas.value);
    } else {
        console.error(resultadoMetas.reason);
    }
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
        console.error("Erro ao encerrar sessão: " , erro);
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
        alterarMensagem("carregando painel...");

        const usuario = await carregarSessao();

        if (!usuario) {
            return;
        }

        preencherUsuario(usuario);

        const conta = await carregarConta(usuario.id);

        if (!conta) {
            return;
        }

        preencherConta(conta);

        await carregarDadosSecundarios(conta.id);

        alterarMensagem();
    } catch (erro) {
        console.error(erro);
        alterarMensagem(`Erro: ${erro.message}`, "erro");
    }
}

iniciarPagina();
