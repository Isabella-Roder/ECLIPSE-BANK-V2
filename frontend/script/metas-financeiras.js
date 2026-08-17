const API_URL = `http://${window.location.hostname}:8080/api`;

const botaoNovaMeta = document.getElementById("botao-nova-meta");
const quantidadeMetas = document.getElementById("quantidade-metas");
const listaMetasFinanceiras = document.getElementById("lista-metas-financeiras");
const estadoMetas = document.getElementById("estado-metas");
const mensagem = document.getElementById("mensagem-metas");

const dialogoNovaMeta = document.getElementById("dialog-nova-meta");
const formNovaMeta = document.getElementById("form-nova-meta");
const botaoFecharNovaMeta = document.getElementById("botao-fechar-nova-meta");
const nomeNovaMeta = document.getElementById("nome-nova-meta");
const valorAlvoNovaMeta = document.getElementById("valor-alvo-nova-meta");
const prazoNovaMeta = document.getElementById("prazo-nova-meta");
const mensagemNovaMeta = document.getElementById("mensagem-nova-meta");
const botaoConfirmarNovaMeta = document.getElementById("botao-confirmar-nova-meta");

const dialogoAporteMeta = document.getElementById("dialog-aporte-meta");
const formAporteMeta = document.getElementById("form-aporte-meta");
const botaoFecharAporteMeta = document.getElementById("botao-fechar-aporte-meta");
const metaIdAporte = document.getElementById("meta-id-aporte");
const nomeMetaAporte = document.getElementById("nome-meta-aporte");
const progressoMetaAporte = document.getElementById("progresso-meta-aporte");
const valorAporteMeta = document.getElementById("valor-aporte-meta");
const mensagemAporteMeta = document.getElementById("mensagem-aporte-meta");
const botaoConfirmarAporteMeta = document.getElementById("botao-confirmar-aporte-meta");

const dialogoResgateMeta = document.getElementById("dialog-resgate-meta");
const formResgateMeta = document.getElementById("form-resgate-meta");
const botaoFecharResgateMeta = document.getElementById("botao-fechar-resgate-meta");
const metaIdResgate = document.getElementById("meta-id-resgate");
const nomeMetaResgate = document.getElementById("nome-meta-resgate");
const progressoMetaResgate = document.getElementById("progresso-meta-resgate");
const valorResgateMeta = document.getElementById("valor-resgate-meta");
const mensagemResgateMeta = document.getElementById("mensagem-resgate-meta");
const botaoConfirmarResgateMeta = document.getElementById("botao-confirmar-resgate-meta");

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

        contaAtual = conta;
        return true;
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return false;
    }
}

async function iniciarPagina() {
    const usuario = await carregarSessao();

    if (!usuario) {
        return;
    }

    const contaCarregada = await carregaConta(usuario.id);

    if (!contaCarregada) {
        return;
    }

    await Promise.all([
        
    ])
}