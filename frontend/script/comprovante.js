const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const parametros = new URLSearchParams(window.location.search);
const codigo = parametros.get("codigo");

const descricao = document.getElementById("descricao-comprovante");
const tipo = document.getElementById("tipo-movimentacao");
const valor = document.getElementById("valor-movimentacao");
const data = document.getElementById("data-movimentacao");
const status = document.getElementById("status-movimentacao");
const saldo = document.getElementById("saldo-resultante");
const contaId = document.getElementById("conta-id");
const codigoMovimentacao = document.getElementById("codigo-movimentacao");
const mensagem = document.getElementById("mensagem-comprovante");
const botaoImprimir = document.getElementById("botao-imprimir");

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

async function carregarComprovante() {
    try {
        const sessao = await fetch(`${API_URL}/usuarios/sessao`, {
            credentials: "include"
        });

        if (!sessao.ok) {
            window.location.href = "login.html";
            return;
        }

        const resposta = await fetch(`${API_URL}/movimentacoes/${codigo}`, {
            credentials: "include"
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao carregar comprovante");
        }

        renderizarComprovante(corpo);

        mensagem.textContent = "Comprovante carregado com sucesso";
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarComprovante(comprovante) {
    descricao.textContent = comprovante.descricao || "Sem descrição";
    tipo.textContent = comprovante.tipo;
    valor.textContent = formatarDinheiro(comprovante.valor);
    data.textContent = new Date(comprovante.criadaEm).toLocaleString("pt-BR");
    status.textContent = comprovante.status;
    saldo.textContent = formatarDinheiro(comprovante.saldoResultante);
    contaId.textContent = comprovante.contaId;
    codigoMovimentacao.textContent = comprovante.codigo
}

botaoImprimir.addEventListener("click", () => {
    window.print();
})

carregarComprovante();
