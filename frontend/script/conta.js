const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const nomeUsuario = document.getElementById("nome-usuario");
const saldoConta = document.getElementById("saldo-conta");
const agenciaConta = document.getElementById("agencia-conta");
const numeroConta = document.getElementById("numero-conta");
const listaMovimentacoes = document.getElementById("lista-movimentacoes");
const mensagem = document.getElementById("mensagem-conta");
const botaoSair = document.getElementById("botao-sair");
const botaoOcultarSaldo = document.getElementById("botao-ocultar-saldo");

function lerCookie(nome) {
    const valor = document.cookie.split("; ").find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

let usuario;
let saldoAtual = 0;
let saldoOculto = false;

function formatarSaldo(saldo) {
    return Number(saldo).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

async function carregarSessao() {
    try {
        const resposta = await fetch(`${API_URL}/usuarios/sessao`, {
            credentials: "include"
        });

        if (!resposta.ok) {
            window.location.href = "login.html";
            return;
        }

        usuario = await resposta.json();
        nomeUsuario.textContent = `Olá, ${usuario.nome}`;
        await carregarOuCriarConta();
    } catch (erro) {
        console.error(erro);
        window.location.href = "login.html"
    }
}

async function carregarOuCriarConta() {
    try {
        mensagem.textContent = "Carregando sua conta...";

        let resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`, {
            credentials: "include"
        });

        if (resposta.status === 404) {
            resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
                }
            });
        }

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Não foi possivel carregar sua conta.");
        }

        preencherConta(corpo);
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

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao carregar extrato");
        }

        preencherExtrato(corpo);
    } catch (erro) {
        console.error(erro);
        listaMovimentacoes.textContent = "Não foi possivel carregar as movimentações";
    }
}

function preencherConta(conta) {
    saldoAtual = conta.saldo;
    atualizarVisibilidadeSaldo();
    agenciaConta.textContent = conta.agencia;
    numeroConta.textContent = conta.numero;
}

function atualizarVisibilidadeSaldo() {
    saldoConta.textContent = saldoOculto
        ? "R$ ••••••"
        : formatarSaldo(saldoAtual);

    saldoConta.classList.toggle("saldo-oculto", saldoOculto);
    botaoOcultarSaldo.textContent = saldoOculto ? "Mostrar" : "Ocultar";
    botaoOcultarSaldo.setAttribute("aria-pressed", String(saldoOculto));
    botaoOcultarSaldo.setAttribute(
        "aria-label",
        saldoOculto ? "Mostrar saldo da conta" : "Ocultar saldo da conta"
    );
}

function preencherExtrato(movimentacoes) {
    listaMovimentacoes.replaceChildren();

    if (movimentacoes.length === 0) {
        listaMovimentacoes.textContent = "Nenhuma movimentação recente.";
        return;
    }

    movimentacoes.slice(0, 5).forEach((movimentacao) => {
        const item = document.createElement("div");

        const credito = 
            movimentacao.tipo === "DEPOSITO" ||
            movimentacao.tipo === "TRANSFERENCIA_RECEBIDA" ||
            movimentacao.tipo === "PIX_RECEBIDO" ||
            movimentacao.tipo === "RESGATE_INVESTIMENTO";

        const sinal = credito ? "+" : "-";

        const data = new Date(movimentacao.criadaEm).toLocaleString("pt-BR");

        item.className = "item-movimentacao";

        const dadosMovimentacao = document.createElement("div");
        const tipoMovimentacao = document.createElement("strong");
        const descricaoMovimentacao = document.createElement("span");

        tipoMovimentacao.textContent = movimentacao.tipo;
        descricaoMovimentacao.textContent = movimentacao.descricao || "Sem descrição";
        dadosMovimentacao.append(tipoMovimentacao, descricaoMovimentacao);

        const valoresMovimentacao = document.createElement("div");
        const valorMovimentacao = document.createElement("strong");
        const dataMovimentacao = document.createElement("span");

        valorMovimentacao.textContent = `${sinal} ${formatarSaldo(movimentacao.valor)}`;
        dataMovimentacao.textContent = data;
        valoresMovimentacao.append(valorMovimentacao, dataMovimentacao);

        item.append(dadosMovimentacao, valoresMovimentacao);

        listaMovimentacoes.appendChild(item);
    });
}

botaoOcultarSaldo.addEventListener("click", () => {
    saldoOculto = !saldoOculto;
    atualizarVisibilidadeSaldo();
});

botaoSair.addEventListener("click", async () => {
    botaoSair.disabled = true;
    botaoSair.textContent = "Saindo...";

    try {
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
})

carregarSessao();
