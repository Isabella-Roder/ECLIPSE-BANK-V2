const API_URL = `http://${window.location.hostname}:8080/api`;

const formulario = document.querySelector("#form-login");
const email = document.querySelector("#email");
const senha = document.querySelector("#senha");
const mensagem = document.querySelector(".mensagem-login");
const btnLogin = document.querySelector(".botao-login");

function lerCookie(nome) {
    const valor = document.cookie.split("; ").find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

fetch(`${API_URL}/usuarios/sessao`, { credentials: "include" });

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
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
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

        mensagem.className = "mensagem-login sucesso";
        mensagem.textContent = "Login realizado com sucesso.";

        setTimeout(() => {
            window.location.href = "conta.html"
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
