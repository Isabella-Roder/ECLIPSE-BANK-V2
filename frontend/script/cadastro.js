const API_URL = `http://${window.location.hostname}:8080/api`;

const formulario = document.querySelector("#form-cadastro");
const nome = document.querySelector("#nome");
const nomeSocial = document.querySelector("#nome-social");
const cpf = document.querySelector("#cpf");
const dataNascimento = document.querySelector("#data-nascimento");
const telefone = document.querySelector("#telefone");
const email = document.querySelector("#email");
const senha = document.querySelector("#senha");
const termos = document.querySelector("#termos");
const mensagem = document.querySelector("#mensagem-formulario");
const btnCriar = document.querySelector(".botao-principal");

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!nome.value.trim() || !cpf.value.trim() || !dataNascimento || !email.value.trim() || !senha.value) {
        mensagem.className = "mensagem-formulario erro";
        mensagem.textContent = "Preencha todos os campos obrigatórios.";
        return;
    }

    if (!termos.checked) {
        mensagem.textContent = "Acente os termos para continuar.";
        return;
    }

    const dados = {
        nome: nome.value,
        nomeSocial: nomeSocial.value || null,
        cpf: cpf.value,
        email: email.value,
        senha: senha.value,
        telefone: telefone.value || null,
        dataNascimento: dataNascimento.value
    };

    try {
        btnCriar.disabled = true;
        btnCriar.textContent = "Criando...";

        const resposta = await fetch(`${API_URL}/usuarios`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            const errosDosCampos = Object.values(corpo.campos || {});
            const textoErro = errosDosCampos[0] || corpo.mensagem || "Erro ao criar conta.";

            throw new Error(textoErro);
        }


        mensagem.className = "mensagem-formulario sucesso";
        mensagem.textContent = "Conta criada com sucesso.";

        formulario.reset();

        setTimeout(() => {
            window.location.href = "login.html";
        }, 750);
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-formulario erro";
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        btnCriar.disabled = false;
        btnCriar.innerHTML = `
            Criar minha conta
            <span aria-hidden="true">→</span>
        `;
    }
})
