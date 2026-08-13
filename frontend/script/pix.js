const API_URL = "http://localhost:8080/api";

const usuarioSalvo = 
    sessionStorage.getItem("clienteLogado") ||
    localStorage.getItem("clienteLogado");

if (!usuarioSalvo) {
    window.location.href = "login.html";
    throw new Error("Usuário não encontrado");
}

const usuario = JSON.parse(usuarioSalvo);

const formulario = document.getElementById("form-pix");
const chavePix = document.getElementById("chave-pix");
const valor = document.getElementById("valor");
const descricao = document.getElementById("descricao");
const saldoDisponivel = document.getElementById("saldo-disponivel");
const contaOrigem = document.getElementById("conta-origem");
const mensagem = document.getElementById("mensagem-operacao");
const btnConfirmar = document.getElementById("botao-confirmar");

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

async function carregarConta() {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`);

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
        valor: Number(valor.value),
        descricao: descricao.value.trim() || null
    }

    try {
        btnConfirmar.disabled = true;
        btnConfirmar.textContent = "Enviando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/pix`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
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

carregarConta();
