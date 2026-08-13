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
const listaMovimentacoes = document.getElementById("lista-movimentacoes");
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
        await carregarExtrato(corpo.id);
        mensagem.textContent = "";

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

async function carregarExtrato(contaId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaId}/extrato`);

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
    saldoConta.textContent = formatarSaldo(conta.saldo);
    agenciaConta.textContent = conta.agencia;
    numeroConta.textContent = conta.numero;
}

function preencherExtrato(movimentacoes) {
    listaMovimentacoes.innerHTML = "";

    if (movimentacoes.length === 0) {
        listaMovimentacoes.textContent = "Nenhuma movimentação recente.";
        return;
    }

    movimentacoes.slice(0, 5).forEach((movimentacao) => {
        const item = document.createElement("div");

        const credito = 
            movimentacao.tipo === "DEPOSITO" ||
            movimentacao.tipo === "TRANSFERENCIA_RECEBIDA" ||
            movimentacao.tipo === "PIX_RECEBIDO";

        const sinal = credito ? "+" : "-";

        const data = new Date(movimentacao.criadaEm).toLocaleString("pt-BR");

        item.className = "item-movimentacao";

        item.innerHTML = `
            <div>
                <strong>${movimentacao.tipo}</strong>
                <span>${movimentacao.descricao || "Sem descrição"}</span>
            </div>

            <div>
                <strong>
                    ${sinal} ${formatarSaldo(movimentacao.valor)}
                </strong>
                <span>${data}</span>
        `;

        listaMovimentacoes.appendChild(item);
    });
}

botaoSair.addEventListener("click", () => {
    sessionStorage.removeItem("clienteLogado");
    localStorage.removeItem("clienteLogado");
    window.location.href = "login.html";
});

carregarOuCriarConta();