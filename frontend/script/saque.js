const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const formulario = document.getElementById("form-saque");
const valor = document.getElementById("valor");
const descricao = document.getElementById("descricao");
const mensagem = document.getElementById("mensagem-operacao");
const btnConfirmar = document.getElementById("botao-confirmar");

function lerCookie(nome) {
    const valor = document.cookie.split("; ").find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

let contaId;

function formatarValor(valor) {
    if (!valor) {
        return "";
    }

    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

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

        renderizarValorDisponivel(corpo);

    } catch (erro) {
        console.error(erro);    
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarValorDisponivel(conta) {
    document.getElementById("saldo-disponivel").textContent = formatarValor(conta.saldo);
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!contaId) {
        return;
    }

    if (!valor.value) {
        mensagem.textContent = "Coloque um valor para sacar";
        return;
    }

    const dados = {
        valor: Number(valor.value),
        descricao: descricao.value.trim() || null
    };

    try {
        btnConfirmar.disabled = true;
        btnConfirmar.textContent = "Sacando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/saques`, {
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
            const erroDosCampos = Object.values(corpo.campos || {});
            const textoErro = erroDosCampos[0] || corpo.mensagem || "Erro ao sacar";

            throw new Error(textoErro);
        }

        mensagem.className = "mensagem-operacao sucesso";
        mensagem.textContent = "Saque realizado com sucesso";

        document.getElementById("saldo-disponivel").textContent = formatarValor(corpo.saldoResultante);

        formulario.reset();

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        btnConfirmar.disabled = false;
        btnConfirmar.textContent = "Confirmar saque"
    }
});

document.getElementById("saldo-disponivel").addEventListener("input", formatarValor);

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
