const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const botaoNovoCartao = document.getElementById("botao-novo-cartao");
const quantidadeCartoes = document.getElementById("quantidade-cartoes");
const listaCartoes = document.getElementById("lista-cartoes");
const estadoCartoes = document.getElementById("estado-cartoes");
const mensagem = document.getElementById("mensagem-cartoes");

const dialogoNovoCartao = document.getElementById("dialog-novo-cartao");
const formNovoCartao = document.getElementById("form-novo-cartao");
const botaoFecharNovoCartao = document.getElementById("botao-fechar-novo-cartao");
const tipoNovoCartao = document.getElementById("tipo-novo-cartao");
const mensagemNovoCartao = document.getElementById("mensagem-novo-cartao");
const botaoConfirmarNovoCartao = document.getElementById("botao-confirmar-novo-cartao");

let contaAtual;

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

function mascararNumero(numero) {
    return "**** **** **** " + numero.slice(-4);
}

async function carregarSessao() {
    try {
        const resposta = await fetch(`${API_URL}/usuarios/sessao`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return null;
        }

        const usuario = await resposta.json();

        if (!resposta.ok) {
            throw new Error(usuario.mensagem || "Erro ao carregar sessao.");
        }

        return usuario;
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return null;
    }
}

async function carregaConta(usuarioId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuarioId}`, {
            credentials: "include"
        });

        const conta = await resposta.json();

        if (!resposta.ok) {
            throw new Error(conta.mensagem || "Erro ao carregar conta.");
        }

        return conta;
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return null;
    }
}

// TODO: você escreve daqui pra baixo, com as dicas do chat.
// Funções que faltam:
// - carregarMeusCartoes()   -> busca GET /contas/{contaId}/cartoes/meus-cartoes e chama renderizarCartoes
// - renderizarCartoes(lista) -> monta os cards no #lista-cartoes
// - acionarCartao(cartaoId, acao) -> PATCH /contas/{contaId}/cartoes/{cartaoId}/{acao} (bloquear/desbloquear/cancelar)
// - listener do botaoNovoCartao (abre o dialog)
// - listener do botaoFecharNovoCartao (fecha o dialog)
// - listener do submit do formNovoCartao (POST /contas/{contaId}/cartoes/criar)
// - iniciarPagina() -> carrega sessão, conta, e os cartões; chamar no fim do arquivo

async function iniciarPagina() {
    const usuario = await carregarSessao();
    if (!usuario) return;

    contaAtual = await carregaConta(usuario.id);
    if (!contaAtual) return;

    await carregarMeusCartoes();
}

iniciarPagina();
