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
const saldoAtual = document.getElementById("saldo-atual");
const filtroTipo = document.getElementById("filtro-tipo");
const dataInicial = document.getElementById("data-inicial");
const dataFinal = document.getElementById("data-final");
const botaoLimpar = document.getElementById("botao-limpar-filtros");
const quantidade = document.getElementById("quantidade-movimentacoes");
const listaExtrato = document.getElementById("lista-extrato");
const mensagem = document.getElementById("mensagem-extrato");

let movimentacoes = [];

nomeUsuario.textContent = `Olá, ${usuario.nome}`;

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

async function carregarConta() {
    try {
        mensagem.textContent = "Carregando conta...";

        const resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`);

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
        const resposta = await fetch(`${API_URL}/contas/${contaId}/extrato`);

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
    listaExtrato.innerHTML = "";
    quantidade.textContent = `${lista.length} Movimentação(ões)`;

    if (lista.length === 0) {
        listaExtrato.innerHTML = `
            <p class="estado-extrato">
                Nenhuma movimentação encontrada.
            </p>
        `;
        return;
    }

    lista.forEach((movimentacao) => {
        const linha = document.createElement("article");

        const credito = movimentacao.tipo === "DEPOSITO" ||
            movimentacao.tipo === "TRANSFERENCIA_RECEBIDA";

        const sinal = credito ? "+" : "-";
        const classeTipo = credito ? "credito" : "debito";

        const data = new Date(movimentacao.criadaEm).toLocaleString("pt-BR");

        linha.className = `linha-extrato ${classeTipo}`;

        linha.innerHTML = `
            <div class="dados-movimentacao">
                <span class="tipo-movimentacao">
                    ${movimentacao.tipo}
                </span>

                <strong>
                    ${movimentacao.descricao || "Sem descrição"}
                </strong>

                <span>${data}</span>
            </div>

            <div class="valores-movimentacao">
                <strong>
                    ${sinal} ${formatarDinheiro(movimentacao.valor)}
                </strong>

                <span>
                    Saldo: ${formatarDinheiro(
                        movimentacao.saldoResultante
                    )}
                </span>

                <a class="link-comprovante" href="comprovante.html?codigo=${movimentacao.codigo}">
                    Ver comprovante
                </a>
            </div>
        `;

        listaExtrato.appendChild(linha);
    });
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

carregarConta();