const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const formulario = document.getElementById("form-cadastro-empresa");
const cnpj = document.getElementById("cnpj-empresa");
const razaoSocial = document.getElementById("razao-social-empresa");
const nomeFantasia = document.getElementById("nome-fantasia-empresa");
const mensagem = document.getElementById("mensagem-cadastro-empresa");
const botaoCadastrar = document.getElementById("botao-cadastrar-empresa");
const resultado = document.getElementById("resultado-conta-empresarial");
const statusConta = document.getElementById("status-conta-empresarial");
const nomeEmpresaResultado = document.getElementById("nome-empresa-resultado");
const identificacaoConta = document.getElementById("identificacao-conta-empresarial");
const cnpjEmpresaResultado = document.getElementById("cnpj-empresa-resultado");

let empresaCriada;

function lerCookie(nome) {
    const valor = document.cookie.split("; ").find(linha => linha.startsWith(nome + "="));

    return valor ? decodeURIComponent(valor.split("=")[1]) : null;
}

function alterarMensagem(texto = "", tipo = "") {
    mensagem.className = tipo
        ? `mensagem-operacao ${tipo}`
        : "mensagem-operacao";
    mensagem.textContent = texto;
}

function formatarCnpj(valor) {
    return valor
        .replace(/\D/g, "")
        .replace(/^(\d{2})(\d)/, "$1.$2")
        .replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3")
        .replace(/\.(\d{3})(\d)/, ".$1/$2")
        .replace(/(\/\d{4})(\d)/, "$1-$2");
}

async function validarSessao() {
    try {
        const resposta = await fetch(`${API_URL}/usuarios/sessao`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return false;
        }

        if (!resposta.ok) {
            throw new Error("Não foi possível validar a sessão.");
        }

        return true;
    } catch (erro) {
        console.error(erro);
        alterarMensagem(`Erro: ${erro.message}`, "erro");
        return false;
    }
}

async function cadastrarEmpresa(dados) {
    const resposta = await fetch(`${API_URL}/empresas`, {
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
        throw new Error(errosDosCampos[0] || corpo.mensagem || "Erro ao cadastrar empresa.");
    }

    return corpo;
}

async function criarContaEmpresarial(empresaId) {
    const resposta = await fetch(`${API_URL}/contas/empresa/${empresaId}`, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
        }
    });

    const corpo = await resposta.json();

    if (!resposta.ok) {
        throw new Error(corpo.mensagem || "Erro ao abrir conta empresarial.");
    }

    return corpo;
}

function exibirContaCriada(empresa, conta) {
    statusConta.textContent = conta.status;
    nomeEmpresaResultado.textContent = empresa.nomeFantasia || empresa.razaoSocial;
    identificacaoConta.textContent = `Agência ${conta.agencia} · Conta ${conta.numero}`;
    cnpjEmpresaResultado.textContent = `CNPJ ${formatarCnpj(empresa.cnpj)}`;

    formulario.closest("section").hidden = true;
    resultado.hidden = false;
    resultado.scrollIntoView({ behavior: "smooth", block: "start" });
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const cnpjSemFormatacao = cnpj.value.replace(/\D/g, "");

    if (cnpjSemFormatacao.length !== 14) {
        alterarMensagem("Informe os 14 números do CNPJ.", "erro");
        cnpj.focus();
        return;
    }

    if (!razaoSocial.value.trim()) {
        alterarMensagem("Informe a razão social.", "erro");
        razaoSocial.focus();
        return;
    }

    const dados = {
        cnpj: cnpjSemFormatacao,
        razaoSocial: razaoSocial.value.trim(),
        nomeFantasia: nomeFantasia.value.trim() || null
    };

    try {
        botaoCadastrar.disabled = true;
        botaoCadastrar.textContent = empresaCriada
            ? "Abrindo conta..."
            : "Cadastrando empresa...";
        alterarMensagem();

        if (!empresaCriada) {
            empresaCriada = await cadastrarEmpresa(dados);
        }

        const conta = await criarContaEmpresarial(empresaCriada.id);

        exibirContaCriada(empresaCriada, conta);
    } catch (erro) {
        console.error(erro);

        const orientacao = empresaCriada
            ? " A empresa foi cadastrada; clique novamente para tentar abrir a conta."
            : "";

        alterarMensagem(`Erro: ${erro.message}${orientacao}`, "erro");
    } finally {
        botaoCadastrar.disabled = false;
        botaoCadastrar.innerHTML = 'Cadastrar e abrir conta <span aria-hidden="true">→</span>';
    }
});

validarSessao();
