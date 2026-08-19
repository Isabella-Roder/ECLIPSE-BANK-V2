const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const identificacaoConta = document.getElementById("identificacao-conta");
const statusConta = document.getElementById("status-conta");
const botaoBloquear = document.getElementById("botao-bloquear-conta");
const botaoDesbloquear = document.getElementById("botao-desbloquear-conta");
const confirmarEncerramento = document.getElementById("confirmar-encerramento");
const botaoEncerrar = document.getElementById("botao-encerrar-conta");
const mensagem = document.getElementById("mensagem-gerenciamento");

function lerCookie(nome) {
    const valor = document.cookie.split("; ").find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

let contaId;

async function carregarConta(usuarioId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuarioId}`, {
            credentials: "include"
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao carregar conta");
        }

        contaId = corpo.id;
        renderizarConta(corpo);
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarConta(conta) {
    identificacaoConta.textContent = conta.numero;
    statusConta.textContent = conta.status;
}

async function bloquearConta() {
    if (!confirm("Deseja realmente bloquear sua conta?")) {
        return;
    }

    try {
        botaoBloquear.disabled = true;
        botaoBloquear.textContent = "Bloqueando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/bloqueio`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao bloquear a conta");
        }

        mensagem.textContent = "Conta bloqueada com sucesso.";
        renderizarConta(corpo);

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        botaoBloquear.disabled = false;
        botaoBloquear.textContent = "Bloquear conta";
    }
}

async function desbloquearConta() {
    if (!confirm("Deseja desbloquear a sua conta?")) {
        return;
    }

    try {
        botaoDesbloquear.disabled = true;
        botaoDesbloquear.textContent = "Desbloqueando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/desbloqueio`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao desbloquear conta");
        }

        mensagem.textContent = "Conta desbloqueada com sucesso";
        renderizarConta(corpo);
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        botaoDesbloquear.disabled = false;
        botaoDesbloquear.textContent = "Desbloquear conta";
    }
}

function verificarEncerramento() {
    if (!confirmarEncerramento.checked) {
        mensagem.textContent = "Marque a caixinha para concordar em encerrar";
        botaoEncerrar.disabled = true;
        return;
    } else {
        botaoEncerrar.disabled = false;
    }
}

async function encerrarConta() {

    if (!confirm("Deseja realmente encerrar a sua conta?")) {
        return;
    }

    try {
        botaoEncerrar.disabled = true;
        botaoEncerrar.textContent = "Encerrando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/encerramento`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao encerrar a conta");
        }

        mensagem.textContent = "Conta encerrada com sucesso.";

        setTimeout(() => {
            window.location.href = "login.html";
        }, 750);
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

botaoBloquear.addEventListener("click", bloquearConta);

botaoDesbloquear.addEventListener("click", desbloquearConta);

confirmarEncerramento.addEventListener("change", verificarEncerramento);

botaoEncerrar.addEventListener("click", encerrarConta);

async function iniciarPagina() {
    const resposta = await fetch(`${API_URL}/usuarios/sessao`, {
        credentials: "include"
    });

    if (!resposta.ok) {
        window.location.href = "login.html";
        return;
    }

    const usuario = await resposta.json();
    await carregarConta(usuario.id);
}

iniciarPagina();
