const API_URL = "http://localhost:8080/api";

const usuarioSalvo = 
    sessionStorage.getItem("clienteLogado") ||
    localStorage.getItem("clienteLogado");

if (!usuarioSalvo) {
    window.location.href = "login.html";
    throw new Error("Usuário não encontrado");
} 

const usuario = JSON.parse(usuarioSalvo);

const formulario = document.getElementById("form-saque");
const valor = document.getElementById("valor");
const descricao = document.getElementById("descricao");
const mensagem = document.getElementById("mensagem-operacao");
const btnConfirmar = document.getElementById("botao-confirmar");

let contaId;

async function carregarConta() {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuario.id}`);

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
    document.getElementById("valor-disponivel").innerHTML `
        <strong>${conta.saldo}</strong>
    `;
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
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            const erroDosCampos = Object.values(corpo.mensagem || {});
            const textoErro = erroDosCampos[0] || corpo.mensagem || "Erro ao sacar";

            throw new Error(textoErro);
        }

        mensagem.textContent = "Saque realizado com sucesso";

        formulario.reset();

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        btnConfirmar.disabled = false;
        btnConfirmar.textContent = "Confirmar saque"
    }
})

carregarConta();