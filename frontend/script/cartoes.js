const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const botaoNovoCartao = document.getElementById("botao-novo-cartao");
const quantidadeCartoes = document.getElementById("quantidade-cartoes");
const listaCartoes = document.getElementById("lista-cartoes");
const estadoCartoes = document.getElementById("estado-cartoes");
const mensagem = document.getElementById("mensagem-cartoes");

const dialogoNovoCartao = document.getElementById("dialog-novo-cartao");
const formNovoCartao = document.getElementById("form-novo-cartao");
const botaoFecharNovoCartao = document.getElementById("botao-fechar-novo-cartao");
const tipoNovoCartao = document.getElementById("tipo-novo-cartao");
const mensagemNovoCartao = document.getElementById("mensagem-novo-cartao");
const botaoConfirmarNovoCartao = document.getElementById("botao-confirmar-novo-cartao");

let contaAtual;

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

function mascararNumero(numero) {
    return "**** **** **** " + numero.slice(-4);
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
            throw new Error(usuario.mensagem || "Erro ao carregar sessao.");
        }

        return usuario;
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return null;
    }
}

async function carregaConta(usuarioId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/usuario/${usuarioId}`, {
            credentials: "include"
        });

        const conta = await resposta.json();

        if (!resposta.ok) {
            throw new Error(conta.mensagem || "Erro ao carregar conta.");
        }

        return conta;
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return null;
    }
}

async function carregarMeusCartoes() {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/meus-cartoes`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const meusCartoes = await resposta.json();

        if (!resposta.ok) {
            throw new Error(meusCartoes.mensagem || "Erro ao carregar meus cartões");
        }

        quantidadeCartoes.textContent = `${meusCartoes.length} cartão(ões)`;
        renderizarMeusCartoes(meusCartoes);

        mensagem.className = "mensagem-operacao sucesso"
        mensagem.textContent = "Meus cartões carregados com sucesso.";

    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarMeusCartoes(cartoes) {
    listaCartoes.replaceChildren();

    if (cartoes.length === 0) {
        estadoCartoes.textContent = "Nenhum cartão disponivel.";
        listaCartoes.appendChild(estadoCartoes);
        return;
    }

    cartoes.forEach((cartao) => {
        const card = document.createElement("article");
        card.className = "cartao-item";

        const titular = document.createElement("p")
        titular.className = "cartao-titular";
        titular.textContent = cartao.titular;

        const tipoTexto = document.createElement("p");
        tipoTexto.className = "cartao-tipo";
        tipoTexto.textContent = cartao.tipo === "CREDITO" ? "Crédito" : "Débito";

        const numero = document.createElement("p");
        numero.className = "cartao-numero";
        numero.textContent = mascararNumero(cartao.numero);

        const status = document.createElement("p");
        status.className = "cartao-status";
        status.textContent = cartao.status;

        if (cartao.tipo === "CREDITO") {
            const limite = document.createElement("strong");
            limite.className = "cartao-limite";
            limite.textContent = formatarDinheiro(cartao.limite);
            card.appendChild(limite);

            const botaoFaturas = document.createElement("button");
            botaoFaturas.className = "botao-faturas";
            botaoFaturas.type = "button";
            botaoFaturas.textContent = "Ver faturas";

            botaoFaturas.addEventListener("click", () => {
                window.location.href = `faturas.html?cartaoId=${encodeURIComponent(cartao.id)}`;
            });

            card.appendChild(botaoFaturas);
        }

        if (cartao.status === "ATIVO") {
            const botaoBloquear = document.createElement("button");
            botaoBloquear.className = "botao-bloquear";
            botaoBloquear.type = "button";
            botaoBloquear.textContent = "Bloquear cartão";

            botaoBloquear.addEventListener("click", () => {
                if (!confirm("Deseja bloquear este cartão?")) {
                    return;
                }

                bloquearCartao(cartao.id);
            });

            const botaoCancelar = document.createElement("button");
            botaoCancelar.className = "botao-cancelar";
            botaoCancelar.type = "button";
            botaoCancelar.textContent = "Cancelar cartão";

            botaoCancelar.addEventListener("click", () => {
                if (!confirm("Deseja cancelar este cartão?")) {
                    return;
                }

                cancelarCartao(cartao.id);
            });

            card.append(botaoBloquear, botaoCancelar);
        }

        if (cartao.status === "BLOQUEADO") {
            const botaoDesbloquear = document.createElement("button");
            botaoDesbloquear.className = "botao-desbloquear";
            botaoDesbloquear.type = "button";
            botaoDesbloquear.textContent = "Desbloquear cartão";

            botaoDesbloquear.addEventListener("click", () => {
                if (!confirm("Deseja desbloquear o cartão?")) {
                    return;
                }

                desbloquearCartao(cartao.id);
            });

            const botaoCancelar = document.createElement("button");
            botaoCancelar.className = "botao-cancelar";
            botaoCancelar.type = "button";
            botaoCancelar.textContent = "Cancelar cartão";

            botaoCancelar.addEventListener("click", () => {
                if (!confirm("Deseja cancelar o cartão?")) {
                    return;
                }

                cancelarCartao(cartao.id);
            });

            card.append(botaoDesbloquear, botaoCancelar);
        }

        card.append(
            titular,
            tipoTexto,
            numero,
            status
        )

        listaCartoes.appendChild(card);
    });
}

async function bloquearCartao(cartaoId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/bloquear`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            },
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem);
        }

        await carregarMeusCartoes();
    } catch(erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

async function desbloquearCartao(cartaoId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/desbloquear`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao desbloquear conta.");
        }

        await carregarMeusCartoes();
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

async function cancelarCartao(cartaoId) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/cancelar`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao cancelar cartão.");
        }

        await carregarMeusCartoes();
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function abrirNovoCartao() {
    dialogoNovoCartao.showModal();
}

function fecharNovoCartao() {
    dialogoNovoCartao.close();
}

formNovoCartao.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!tipoNovoCartao.value) {
        mensagemNovoCartao.textContent = "Preencha o tipo do cartão.";
        return;
    }

    const dados = {
        tipo: tipoNovoCartao.value.trim()
    };

    console.log("botao foi apertado");

    try {
        botaoConfirmarNovoCartao.disabled = true;
        botaoConfirmarNovoCartao.textContent = "Criando...";
        mensagemNovoCartao.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/criar`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            },
            body: JSON.stringify(dados)
        });

        if (resposta.status === 401) {
            window.location.href = "login.html"
            return;
        }

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro a criar cartao");
        }

        mensagemNovoCartao.className = "mensagem-operacao sucesso";
        mensagemNovoCartao.textContent = "Cartao criado com sucesso.";

        await carregarMeusCartoes();
    } catch (erro) {
        console.error(erro);
        mensagemNovoCartao.className = "mensagem-operacao erro";
        mensagemNovoCartao.textContent = "Erro: " + erro.message;
    } finally {
        botaoConfirmarNovoCartao.disabled = false;
        botaoConfirmarNovoCartao.innerHTML = `
            Criar cartao
        `;
    }
})

// Funções que faltam:
// - carregarMeusCartoes()   -> busca GET /contas/{contaId}/cartoes/meus-cartoes e chama renderizarCartoes
// - renderizarCartoes(lista) -> monta os cards no #lista-cartoes
// - acionarCartao(cartaoId, acao) -> PATCH /contas/{contaId}/cartoes/{cartaoId}/{acao} (bloquear/desbloquear/cancelar)
// - listener do botaoNovoCartao (abre o dialog)
// - listener do botaoFecharNovoCartao (fecha o dialog)
// - listener do submit do formNovoCartao (POST /contas/{contaId}/cartoes/criar)
// - iniciarPagina() -> carrega sessão, conta, e os cartões; chamar no fim do arquivo

botaoNovoCartao.addEventListener("click", abrirNovoCartao);

botaoFecharNovoCartao.addEventListener("click", fecharNovoCartao);

async function iniciarPagina() {
    const usuario = await carregarSessao();
    if (!usuario) return;

    contaAtual = await carregaConta(usuario.id);
    if (!contaAtual) return;

    await carregarMeusCartoes();
}

iniciarPagina();
