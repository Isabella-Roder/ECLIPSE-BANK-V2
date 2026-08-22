const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

let contaAtual;
let emprestimoAtual;

const formSolicitarEmprestimo = document.getElementById("form-solicitar-emprestimo");
const valorSolicitado = document.getElementById("valor-solicitado");
const taxaJuros = document.getElementById("taxa-juros");
const quantidadeParcelas = document.getElementById("quantidade-parcelas");
const valorTotalSimulacao = document.getElementById("valor-total-simulacao");
const valorParcelaSimulacao = document.getElementById("valor-parcela-simulacao");
const mensagem = document.getElementById("mensagem-emprestimo");
const botaoSolicitar = document.getElementById("botao-solicitar-emprestimo");
const resultadoEmprestimo = document.getElementById("resultado-emprestimo");
const statusEmprestimo = document.getElementById("status-emprestimo");
const identificadorEmprestimo = document.getElementById("identificador-emprestimo");
const valorSolicitadoResultado = document.getElementById("valor-solicitado-resultado");
const detalhesEmprestimo = document.getElementById("detalhes-emprestimo");
const botaoAprovar = document.getElementById("botao-aprovar-emprestimo");

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

function alterarMensagem(texto = "", tipo = "") {
    mensagem.className = tipo
        ? `mensagem-operacao ${tipo}`
        : "mensagem-operacao";
    mensagem.textContent = texto;
}

function obterDadosSimulacao() {
    return {
        valorSolicitado: obterValorMonetario(valorSolicitado),
        taxaJuros: Number(taxaJuros.value),
        quantidadeParcelas: Number(quantidadeParcelas.value)
    };
}

function calcularSimulacao() {
    const dados = obterDadosSimulacao();

    if (
        !Number.isFinite(dados.valorSolicitado)
        || dados.valorSolicitado <= 0
        || !Number.isFinite(dados.taxaJuros)
        || dados.taxaJuros <= 0
        || !Number.isInteger(dados.quantidadeParcelas)
        || dados.quantidadeParcelas <= 0
    ) {
        valorTotalSimulacao.textContent = "Total: R$ 0,00";
        valorParcelaSimulacao.textContent = "Parcela: R$ 0,00";
        return;
    }

    const juros = dados.valorSolicitado * (dados.taxaJuros / 100);
    const valorTotal = dados.valorSolicitado + juros;
    const valorParcela = valorTotal / dados.quantidadeParcelas;

    valorTotalSimulacao.textContent = `Total: ${formatarDinheiro(valorTotal)}`;
    valorParcelaSimulacao.textContent = `${dados.quantidadeParcelas}x de ${formatarDinheiro(valorParcela)}`;
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
            throw new Error(usuario.mensagem || "Erro ao carregar sessão.");
        }

        return usuario;
    } catch (erro) {
        console.error(erro);
        return null;
    }
}

async function carregarConta(usuarioId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuarioId}`, {
            credentials: "include"
        });

        const conta = await resposta.json();

        if (!resposta.ok) {
            throw new Error(conta.mensagem || "Erro a carregar conta.");
        }

        return conta;
    } catch (erro) {
        console.error(erro);
        return null;
    }
}

async function solicitar(dados) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/emprestimos`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            },
            body: JSON.stringify(dados)
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao solicitar.");
        }

        console.log("solicitado");
        return corpo;
    } catch (erro) {
        console.error(erro);
        throw erro;
    }
}

async function aprovar(emprestimoId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/emprestimos/${emprestimoId}/aprovar`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao aprovar.");
        }

        return corpo;
    } catch (erro) {
        console.error(erro);
        throw erro;
    }
}

async function pagarParcela(emprestimoId, parcelaId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/emprestimos/${emprestimoId}/parcelas/${parcelaId}/pagar`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao pagar parcela.");
        }

        return corpo;
    } catch (erro) {
        console.error(erro);
        throw erro;
    }
}

function exibirEmprestimo(emprestimo) {
    emprestimoAtual = emprestimo;

    identificadorEmprestimo.textContent = `Empréstimo #${emprestimo.id}`;
    statusEmprestimo.textContent = emprestimo.status.replaceAll("_", " ");
    statusEmprestimo.dataset.status = emprestimo.status;
    valorSolicitadoResultado.textContent = formatarDinheiro(emprestimo.valorSolicitado);
    detalhesEmprestimo.textContent = `${emprestimo.quantidadeParcelas} parcelas · Total de ${formatarDinheiro(emprestimo.valorTotal)} · Juros de ${Number(emprestimo.taxaJuros).toLocaleString("pt-BR")}%`;

    const podeAprovar = emprestimo.status === "SOLICITADO";
    botaoAprovar.hidden = !podeAprovar;
    resultadoEmprestimo.hidden = false;
}

formSolicitarEmprestimo.addEventListener("input", calcularSimulacao);

formSolicitarEmprestimo.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const dados = obterDadosSimulacao();

    if (!Number.isFinite(dados.valorSolicitado) || dados.valorSolicitado <= 0) {
        alterarMensagem("Informe um valor maior que zero.", "erro");
        valorSolicitado.focus();
        return;
    }

    if (!Number.isFinite(dados.taxaJuros) || dados.taxaJuros <= 0) {
        alterarMensagem("Informe uma taxa de juros maior que zero.", "erro");
        taxaJuros.focus();
        return;
    }

    if (!Number.isInteger(dados.quantidadeParcelas) || dados.quantidadeParcelas <= 0) {
        alterarMensagem("Informe uma quantidade válida de parcelas.", "erro");
        quantidadeParcelas.focus();
        return;
    }

    try {
        botaoSolicitar.disabled = true;
        botaoSolicitar.textContent = "Solicitando...";
        alterarMensagem();

        const emprestimo = await solicitar(dados);

        exibirEmprestimo(emprestimo);
        alterarMensagem("Empréstimo solicitado com sucesso.", "sucesso");
        resultadoEmprestimo.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (erro) {
        alterarMensagem(`Erro: ${erro.message}`, "erro");
    } finally {
        botaoSolicitar.disabled = false;
        botaoSolicitar.innerHTML = 'Solicitar empréstimo <span aria-hidden="true">→</span>';
    }
});

botaoAprovar.addEventListener("click", async () => {
    if (!emprestimoAtual || !confirm("Deseja aprovar o empréstimo e liberar o valor na conta?")) {
        return;
    }

    try {
        botaoAprovar.disabled = true;
        botaoAprovar.textContent = "Aprovando...";
        alterarMensagem();

        const emprestimo = await aprovar(emprestimoAtual.id);

        exibirEmprestimo(emprestimo);
        alterarMensagem("Empréstimo aprovado e valor liberado na conta.", "sucesso");
    } catch (erro) {
        alterarMensagem(`Erro: ${erro.message}`, "erro");
    } finally {
        botaoAprovar.disabled = false;
        botaoAprovar.innerHTML = 'Aprovar e liberar valor <span aria-hidden="true">→</span>';
    }
});

async function iniciarPagina() {
    try {
        const usuario = await carregarSessao();
        if (!usuario) return;

        contaAtual = await carregarConta(usuario.id);

        if (!contaAtual) {
            alterarMensagem("Não foi possível carregar a conta.", "erro");
        }
    } catch (erro) {
        console.error(erro);
        alterarMensagem(`Erro: ${erro.message}`, "erro");
    }
}

iniciarPagina();
