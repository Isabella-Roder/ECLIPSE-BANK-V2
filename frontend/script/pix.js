const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const formulario = document.getElementById("form-pix");
const chavePix = document.getElementById("chave-pix");
const valor = document.getElementById("valor");
const descricao = document.getElementById("descricao");
const saldoDisponivel = document.getElementById("saldo-disponivel");
const contaOrigem = document.getElementById("conta-origem");
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
        renderizarConta(corpo);
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarConta(conta) {
    saldoDisponivel.textContent = formatarValor(conta.saldo);
    contaOrigem.textContent = `Agência ${conta.agencia} · Numero ${conta.numero}`;
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!contaId) {
        return;
    }

    if (!chavePix.value.trim() || !valor.value) {
        mensagem.textContent = "Preencha os campos obrigatórios"
        return;
    }

    const dados = {
        chave: chavePix.value.trim(),
        valor: obterValorMonetario(valor),
        descricao: descricao.value.trim() || null
    }

    try {
        btnConfirmar.disabled = true;
        btnConfirmar.textContent = "Enviando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/pix`, {
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
            const textoErro = erroDosCampos[0] || corpo.mensagem || "Erro ao enviar pix";

            throw new Error(textoErro);
        }


        mensagem.classList = "mensagem-operacao sucesso";
        mensagem.textContent = "Pix enviado com sucesso";

        saldoDisponivel.textContent = formatarValor(corpo.saldoResultante);

        formulario.reset();
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        btnConfirmar.disabled = false;
        btnConfirmar.textContent = "Confirmar pix";
    }
})

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
