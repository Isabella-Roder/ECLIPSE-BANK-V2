const API_URL = "http://localhost:8080/api";

const usuarioSalvo = 
    sessionStorage.getItem("clienteLogado") ||
    localStorage.getItem("clienteLogado");

if (!usuarioSalvo) {
    window.location.href = "login.html";
    throw new Error("Usuário não encontrado");
}

const usuario = JSON.parse(usuarioSalvo);

const nomeUsuario = document.getElementById("nome-usuario");
const saldoConta = document.getElementById("saldo-conta");
const agenciaConta = document.getElementById("agencia-conta");
const numeroConta = document.getElementById("numero-conta");
const mensagem = document.getElementById("mensagem-conta");
const botaoSair = document.getElementById("botao-sair");

nomeUsuario.textContent = `Olá, ${usuario.nome}`;

function formatarSaldo(saldo) {
    return Number(saldo).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

async function carregarOuCriarConta() {
    try {
        mensagem.textContent = "Carregando sua conta...";

        let resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`);

        if (resposta.status === 404) {
            resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`, {
                method: "POST"
            });
        }

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Não foi possivel carregar sua conta.");
        }

        preencherConta(corpo);
        mensagem.textContent = "";

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function preencherConta(conta) {
    saldoConta.textContent = formatarSaldo(conta.saldo);
    agenciaConta.textContent = conta.agencia;
    numeroConta.textContent = conta.numero;
}

botaoSair.addEventListener("click", () => {
    sessionStorage.removeItem("clienteLogado");
    localStorage.removeItem("clienteLogado");
    window.location.href = "login.html";
});

carregarOuCriarConta();