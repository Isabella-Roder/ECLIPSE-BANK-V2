const API_URL = "http://localhost:8080/api";

const formulario = document.querySelector("#form-login");
const email = document.querySelector("#email");
const senha = document.querySelector("#senha");
const manterConectado = document.querySelector("#manter-conectado");
const mensagem = document.querySelector(".mensagem-login");
const btnLogin = document.querySelector(".botao-login");

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    
    if (!email.value.trim() || !senha.value) {
        mensagem.className = "mensagem-login erro";
        mensagem.textContent = "Preencha os campos obrigatórios";
        return;
    }

    const dados = {
        email: email.value,
        senha: senha.value
    };

    try {
        btnLogin.disabled = true;
        btnLogin.textContent = "Entrando...";

        const resposta = await fetch(`${API_URL}/usuarios/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            const errosDosCampos = Object.values(corpo.campos || {});
            const textoErro = errosDosCampos[0] || corpo.mensagem || "Erro ao entrar na conta.";

            throw new Error(textoErro);
        }

        formulario.reset();

        const armazenamento = manterConectado.checked
            ? localStorage
            : sessionStorage

        armazenamento.setItem("clienteLogado", JSON.stringify(corpo));

        mensagem.className = "mensagem-login sucesso";
        mensagem.textContent = "Login realizado com sucesso.";

        setTimeout(() => {
            // adicionar a direção para a home
        }, 750);
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-login erro";
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        btnLogin.disabled = false;
        btnLogin.innerHTML = `
            Entrar na minha conta
            <span aria-hidden="true">→</span>
        `;
    }
})