const API_URL = "http://localhost:8080/api";

const usuarioSalvo = 
    sessionStorage.getItem("clienteLogado") ||
    localStorage.getItem("clienteLogado");

if (!usuarioSalvo) {
    window.location.href = "login.html";
    throw new Error("Usuário não encontrado");
}

const usuario = JSON.parse(usuarioSalvo);

const formulario = document.getElementById("form-deposito");
const valor = document.getElementById("valor");
const descricao = document.getElementById("descricao");
const conta = document.getElementById("conta-destino");
const mensagem = document.getElementById("mensagem-operacao");
const btnConfirmar = document.getElementById("botao-confirmar");

let contaId;

async function carregarConta() {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`);

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Não foi possivel carregar conta");
        }

        contaId = corpo.id;
        conta.textContent = `Agencia ${corpo.agencia} . Conta ${corpo.numero}`;
    } catch (erro) {
        console.error(erro);

        conta.textContent = "Conta indísponivel";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!contaId) {
        return;
    }

    if (!valor.value) {
        mensagem.textContent = "Coloque um valor para depositar.";
        return;
    }

    const dados = {
        valor: Number(valor.value),
        descricao: descricao.value.trim() || null
    };

    try {
        btnConfirmar.disabled = true;
        btnConfirmar.textContent = "Depositando...";

        const resposta = await fetch(`${API_URL}/contas/${contaId}/depositos`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            const erroDoscampos = Object.values(corpo.campos || {});
            const textoErro = erroDoscampos[0] || corpo.mensagem || "Erro ao depositar.";

            throw new Error(textoErro);
        }

        mensagem.textContent = "Deposito realizado com sucesso.";

        formulario.reset();

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        btnConfirmar.disabled = false;
        btnConfirmar.textContent = "Confirmar depósito";
    }
});

carregarConta();