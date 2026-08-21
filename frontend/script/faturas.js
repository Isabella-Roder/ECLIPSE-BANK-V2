const API_URL = window.location.port && window.location.port !== "8080"
    ? `http://${window.location.hostname}:8080/api`
    : "/api";

const parametros = new URLSearchParams(window.location.search);
const cartaoId = parametros.get("cartaoId");

const cartaoSelecionado = document.getElementById("cartao-selecionado");
const quantidadeFaturas = document.getElementById("quantidade-faturas");
const listaFaturas = document.getElementById("lista-faturas");
const estadoFaturas = document.getElementById("estado-faturas");
const mensagem = document.getElementById("mensagem-faturas");

const formCompra = document.getElementById("form-compra");
const descricaoCompra = document.getElementById("descricao-compra");
const categoriaCompra = document.getElementById("categoria-compra");
const campoCategoriaPersonalizada = document.getElementById("campo-categoria-personalizada");
const categoriaPersonalizadaCompra = document.getElementById("categoria-personalizada-compra");
const valorCompra = document.getElementById("valor-compra");
const botaoLancarCompra = document.getElementById("botao-lancar-compra");

let contaAtual;

function lercookie(nome) {
    const cookie = document.cookie.split("; ").find(item => item.startsWith(nome + "="));

    return cookie
        ? decodeURIComponent(cookie.split("=")[1])
        : null;
}

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function formatarData(data) {
    if (!data) {
        return "Não informada";
    }

    return new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR");
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
            throw new Error(usuario.mensagem || "Erro ao carregar sessão.");
        }

        return usuario;
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
        return null;
    }
}

async function carregarConta(usuarioId) {
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
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
        return null
    }
}

async function carregarFaturas() {
    try {
        mensagem.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/faturas/minha-fatura`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const faturas = await resposta.json();

        if (!resposta.ok) {
            throw new Error(faturas.mensagem || "Erro ao carregar faturas.");
        }

        renderizarFaturas(faturas);
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarFaturas(faturas) {
    listaFaturas.replaceChildren();

    quantidadeFaturas.textContent = `${faturas.length} fatura(s)`;

    cartaoSelecionado.textContent = `Cartão selecionado: ${cartaoId}`;

    if (faturas.length === 0) {
        estadoFaturas.textContent = "Este cartao ainda não possui faturas.";
        listaFaturas.appendChild(estadoFaturas);
        return;
    }

    faturas.forEach((fatura) => {
        const card = document.createElement("article");
        card.className = "cartao-item";

        const referencia = document.createElement("p");
        referencia.className = "cartao-tipo";
        referencia.textContent = fatura.mesReferencia;

        const status = document.createElement("p");
        status.className = "cartao-status";
        status.textContent = fatura.status;

        const valor = document.createElement("strong");
        valor.className = "cartao-numero";
        valor.textContent = formatarDinheiro(fatura.valorTotal);

        const vencimento = document.createElement("p");
        vencimento.className = "cartao-titular";
        vencimento.textContent = `Vencimento: ${formatarData(fatura.dataVencimento)}`;

        card.append(
            referencia,
            status,
            valor,
            vencimento
        );

        if (fatura.status === "ABERTA") {
            const botaoFechar = document.createElement("button");
            botaoFechar.className = "botao-fatura";
            botaoFechar.type = "button";
            botaoFechar.textContent = "Fechar fatura";

            botaoFechar.addEventListener("click", () => {
                fecharFatura(fatura.id);
            });

            card.appendChild(botaoFechar);
        }

        if (fatura.status === "FECHADA") {
            const botaoPagar = document.createElement("button");
            botaoPagar.className = "botao-fatura";
            botaoPagar.type = "button";
            botaoPagar.textContent = "Pagar fatura";

            botaoPagar.addEventListener("click", () => {
                pagarFatura(fatura.id);
            });

            card.appendChild(botaoPagar);
        }

        listaFaturas.appendChild(card);
    });
}

async function lancarCompra(valor, descricao, categoria, categoriaPersonalizada) {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/faturas/compras`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lercookie("XSRF-TOKEN")
            },
            body: JSON.stringify({
                valor: valor,
                descricao: descricao || null,
                categoria: categoria,
                categoriaPersonalizada: categoria === "OUTROS" ? categoriaPersonalizada : null
            })
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao lançar compra.");
        }

        await carregarFaturas();
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

async function fecharFatura(faturaId) {
    if (!confirm("Deseja fechar esta fatura?")) {
        return;
    }

    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/faturas/${faturaId}/fechar`, {
            method: "PATCH",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lercookie("XSRF-TOKEN")
            }
        });

        const corpo = await resposta.json();

        if (!resposta.ok) {
            throw new Error(corpo.mensagem || "Erro ao fechar fatura.");
        }

        await carregarFaturas();
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

async function pagarFatura(faturaId) {
    if (!confirm("Deseja pagar esta fatura usando o saldo da conta?")) {
        return;
    }

    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/cartoes/${cartaoId}/faturas/${faturaId}/pagar`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lercookie("XSRF-TOKEN")
            }
        });

        const fatura = await resposta.json();

        if (!resposta.ok) {
            throw new Error(fatura.mensagem || "Erro ao pagar fatura.");
        }

        mensagem.className = "mensagem-operacao sucesso"
        mensagem.textContent = "Fatura paga com sucesso.";

        await carregarFaturas();
    } catch(erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

categoriaCompra.addEventListener("change", () => {
    const selecionouOutros = categoriaCompra.value === "OUTROS";

    campoCategoriaPersonalizada.hidden = !selecionouOutros;
    categoriaPersonalizadaCompra.required = selecionouOutros;

    if (!selecionouOutros) {
        categoriaPersonalizadaCompra.value = "";
    }
});

formCompra.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const valor = obterValorMonetario(valorCompra);

    if (!Number.isFinite(valor) || valor <= 0) {
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Informe um valor valido.";
        return;
    }

    if (categoriaCompra.value === "OUTROS" && !categoriaPersonalizadaCompra.value) {
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Informe a categoria personalizada.";
        return;
    }

    botaoLancarCompra.disabled = true;
    botaoLancarCompra.textContent = "Lançando...";

    await lancarCompra(
        valor,
        descricaoCompra.value,
        categoriaCompra.value,
        categoriaPersonalizadaCompra.value
    );

    valorCompra.value = "";
    descricaoCompra.value = "";
    categoriaCompra.selectedIndex = 0;
    categoriaPersonalizadaCompra.value = "";
    categoriaPersonalizadaCompra.required = false;
    campoCategoriaPersonalizada.hidden = true;
    botaoLancarCompra.disabled = false;
    botaoLancarCompra.innerHTML = `
        Lançar compra
        <span aria-hidden="true">→</span>
    `;
});

async function iniciarPagina() {
    try {
        if (!cartaoId) {
            throw new Error("O ID do cartão não foi informado.");
        }

        const usuario = await carregarSessao();

        if (!usuario) {
            return;
        }

        contaAtual = await carregarConta(usuario.id);

        await carregarFaturas();
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

iniciarPagina();
