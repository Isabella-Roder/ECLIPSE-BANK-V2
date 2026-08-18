const API_URL = `http://${window.location.hostname}:8080/api`;

const botaoNovaMeta = document.getElementById("botao-nova-meta");
const quantidadeMetas = document.getElementById("quantidade-metas");
const listaMetasFinanceiras = document.getElementById("lista-metas-financeiras");
const estadoMetas = document.getElementById("estado-metas");
const mensagem = document.getElementById("mensagem-metas");

const dialogoNovaMeta = document.getElementById("dialog-nova-meta");
const formNovaMeta = document.getElementById("form-nova-meta");
const botaoFecharNovaMeta = document.getElementById("botao-fechar-nova-meta");
const nomeNovaMeta = document.getElementById("nome-nova-meta");
const valorAlvoNovaMeta = document.getElementById("valor-alvo-nova-meta");
const prazoNovaMeta = document.getElementById("prazo-nova-meta");
const mensagemNovaMeta = document.getElementById("mensagem-nova-meta");
const botaoConfirmarNovaMeta = document.getElementById("botao-confirmar-nova-meta");

const dialogoAporteMeta = document.getElementById("dialog-aporte-meta");
const formAporteMeta = document.getElementById("form-aporte-meta");
const botaoFecharAporteMeta = document.getElementById("botao-fechar-aporte-meta");
const metaIdAporte = document.getElementById("meta-id-aporte");
const nomeMetaAporte = document.getElementById("nome-meta-aporte");
const progressoMetaAporte = document.getElementById("progresso-meta-aporte");
const valorAporteMeta = document.getElementById("valor-aporte-meta");
const mensagemAporteMeta = document.getElementById("mensagem-aporte-meta");
const botaoConfirmarAporteMeta = document.getElementById("botao-confirmar-aporte-meta");

const dialogoResgateMeta = document.getElementById("dialog-resgate-meta");
const formResgateMeta = document.getElementById("form-resgate-meta");
const botaoFecharResgateMeta = document.getElementById("botao-fechar-resgate-meta");
const metaIdResgate = document.getElementById("meta-id-resgate");
const nomeMetaResgate = document.getElementById("nome-meta-resgate");
const progressoMetaResgate = document.getElementById("progresso-meta-resgate");
const valorResgateMeta = document.getElementById("valor-resgate-meta");
const mensagemResgateMeta = document.getElementById("mensagem-resgate-meta");
const botaoConfirmarResgateMeta = document.getElementById("botao-confirmar-resgate-meta");

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

function formatarData(data) {
    return new Date(data).toLocaleString("pt-BR");
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

        contaAtual = conta;
        return true;
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return false;
    }
}

async function carregarMinhasMetas() {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/metas-financeiras/minhas-metas`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const minhasMetas = await resposta.json();

        if (!resposta.ok) {
            throw new Error(minhasMetas.mensagem || "Erro ao carregar minhas metas.");
        }

        renderizarMinhasMetas(minhasMetas);

        quantidadeMetas.textContent = `${minhasMetas.length} minha(s) meta(s)`;
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarMinhasMetas(metas) {
    listaMetasFinanceiras.replaceChildren();

    if (metas.length === 0) {
        estadoMetas.textContent = "Você ainda não possui metas financeiras.";
        listaMetasFinanceiras.appendChild(estadoMetas);
        return;
    }

    metas.forEach((meta) => {
        const cartao = document.createElement("article");
        cartao.className = "cartao-meta-financeira";

        const status = document.createElement("span");
        status.className = `status-meta-financeira ${meta.status.toLowerCase()}`;
        status.textContent = meta.status;

        const nome = document.createElement("h3");
        nome.textContent = meta.nome;

        const valorAlvo = document.createElement("strong");
        valorAlvo.className = "valor-alvo";
        valorAlvo.textContent = formatarDinheiro(meta.valorAlvo);

        const valorAtual = document.createElement("span");
        valorAtual.className = "valor-atual";
        valorAtual.textContent = formatarDinheiro(meta.valorAtual);

        const prazo = document.createElement("strong");
        prazo.className = "prazo-meta-financeira";
        prazo.textContent = formatarData(meta.prazo);

        const porcentagem = Math.min((meta.valorAtual / meta.valorAlvo) * 100, 100);

        const barraProgresso = document.createElement("div");
        barraProgresso.className = "barra-progresso-meta";

        const preenchimento = document.createElement("span");
        preenchimento.style.width = `${porcentagem}%`;

        barraProgresso.appendChild(preenchimento);

        const criadaEm = document.createElement("time");
        criadaEm.dateTime = meta.criadaEm;
        criadaEm.textContent = formatarData(meta.criadaEm);

        const atualizadaEm = document.createElement("time");
        atualizadaEm.dateTime = meta.atualizadaEm;
        atualizadaEm.textContent = formatarData(meta.atualizadaEm);

        if (meta.status === "CONCLUIDA") {
            const concluidaEm = document.createElement("time");
            concluidaEm.dateTime = meta.concluidaEm;
            concluidaEm.textContent = formatarData(meta.concluidaEm);
            cartao.appendChild(concluidaEm);
        }

        if (meta.status === "EM_ANDAMENTO") {
            const botaoAportar = document.createElement("button");
            botaoAportar.className = "botao-aplicar";
            botaoAportar.type = "button";
            botaoAportar.textContent = "Aportar";

            botaoAportar.addEventListener("click", () => {
                abrirAporte(meta);
            });

            const botaoResgatar = document.createElement("button");
            botaoResgatar.className = "botao-resgatar";
            botaoResgatar.type = "button";
            botaoResgatar.textContent = "Resgatar";

            botaoResgatar.addEventListener("click", () => {
                abrirResgate(meta);
            });

            cartao.append(botaoAportar, botaoResgatar);
        }

        cartao.append(
            status,
            nome,
            valorAlvo,
            valorAtual,
            prazo,
            criadaEm,
            atualizadaEm,
            barraProgresso
        )

        listaMetasFinanceiras.appendChild(cartao);
    });

}

function abrirAporte(meta) {
    metaIdAporte.value = meta.id;
    nomeMetaAporte.textContent = meta.nome;

    progressoMetaAporte.textContent = `Progresso da meta ${formatarDinheiro(meta.valorAlvo)}`;

    valorAporteMeta.max = meta.valorAlvo;
    valorAporteMeta.value = "";
    mensagemAporteMeta.textContent = "";

    dialogoAporteMeta.showModal();
    valorAporteMeta.focus();
}

function abrirResgate(meta) {
    metaIdResgate.value = meta.id;
    nomeMetaResgate.textContent = meta.nome;

    progressoMetaResgate.textContent = `Progresso da meta ${formatarDinheiro(meta.valorAlvo)}`;

    valorResgateMeta.max = meta.valorAlvo;
    valorResgateMeta.value = "";
    mensagemResgateMeta.textContent = "";

    dialogoResgateMeta.showModal();
    valorResgateMeta.focus();
}

botaoNovaMeta.addEventListener("click", () => {
    dialogoNovaMeta.showModal();

});

formNovaMeta.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!nomeNovaMeta.value.trim() || !valorAlvoNovaMeta.value || !prazoNovaMeta.value) {
        mensagemNovaMeta.textContent = "Preencha os campos obrigatórios.";
        return;
    }

    const dados = {
        nome: nomeNovaMeta.value.trim(),
        valorAlvo: Number(valorAlvoNovaMeta.value),
        prazo: new Date(prazoNovaMeta.value)
    };

    try {
        botaoConfirmarNovaMeta.disabled = true;
        botaoConfirmarNovaMeta.textContent = "Criando...";
        mensagemNovaMeta.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/metas-financeiras/criar`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            },
            body: JSON.stringify(dados)
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const meta = await resposta.json();

        if (!resposta.ok) {
            throw new Error(meta.mensagem || "Erro ao criar meta.");
        }

        dialogoNovaMeta.close();

        mensagemNovaMeta.className = "mensagem-operacao sucesso";
        mensagemNovaMeta.textContent = "Nova meta criada com sucesso.";
        await carregarMinhasMetas();

    }catch (erro) {
        console.error(erro);
        mensagemNovaMeta.className = "mensagem-operacao erro";
        mensagemNovaMeta.textContent = "Erro: " + erro.message;
    } finally {
        botaoConfirmarNovaMeta.disabled = false;
        botaoConfirmarNovaMeta.innerHTML = `
            Criar meta
            <span aria-hidden="true">→</span>
        `;
    }
});

formAporteMeta.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!valorAporteMeta.value) {
        mensagemAporteMeta.textContent = "Preencha o valor para aporte.";
        return;
    }

    const dados = {
        metaId: Number(metaIdAporte.value),
        valor: Number(valorAporteMeta.value)
    };

    try {
        botaoConfirmarAporteMeta.disabled = true;
        botaoConfirmarAporteMeta.textContent = "Aportando...";
        mensagemAporteMeta.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/metas-financeiras/${metaIdAporte.value}/aportar`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": lerCookie("XSRF-TOKEN")
            },
            body: JSON.stringify(dados)
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const aporte = await resposta.json();

        if (!resposta.ok) {
            throw new Error(aporte.mensagem || "Erro ao aportar na meta");
        }

        mensagemAporteMeta.className = "mensagem-operacao sucesso";
        mensagemAporteMeta.textContent = `Valor aportado em ${aporte.nome} com sucesso.`;
        dialogoAporteMeta.close();
        await carregarMinhasMetas();
    } catch (erro) {
        console.error(erro);
        mensagemAporteMeta.className = "mensagem-operacao erro";
        mensagemAporteMeta.textContent = "Erro: " + erro.message;
    } finally {
        botaoConfirmarAporteMeta.disabled = false;
        botaoConfirmarAporteMeta.innerHTML = `
            Confirmar aporte
            <span aria-hidden="true">→</span>
        `;
    }
});

formResgateMeta.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!valorResgateMeta.value) {
        mensagemResgateMeta.textContent = "Preencha o valor para resgate.";
        return;
    }

    const dados = {
        metaId: Number(metaIdResgate.value),
        valor: Number(valorResgateMeta.value)
    };

    try {
        botaoConfirmarResgateMeta.disabled = true;
        botaoConfirmarResgateMeta.textContent = "Resgatando...";
        mensagemResgateMeta.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/metas-financeiras/${metaIdResgate.value}/resgatar`, {
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

        const resgate = await resposta.json();

        if (!resposta.ok) {
            throw new Error(resgate.mensagem || "Erro ao resgatar.");
        }

        mensagemResgateMeta.className = "mensagem-operacao sucesso";
        mensagemResgateMeta.textContent = `Valor resgatado da meta: ${resgate.nome} com sucesso.`;
        dialogoResgateMeta.close();
        await carregarMinhasMetas();
    } catch (erro) {
        console.error(erro);
        mensagemResgateMeta.className = "mensagem-operacao erro";
        mensagemResgateMeta.textContent = "Erro: " + erro.message;
    } finally {
        botaoConfirmarResgateMeta.disabled = false;
        botaoConfirmarResgateMeta.innerHTML = `
            Confirmar resgate
            <span aria-hidden="true">→</span>
        `;
    }
})

botaoFecharNovaMeta.addEventListener("click", () => {
    dialogoNovaMeta.close();
});

botaoFecharAporteMeta.addEventListener("click", () => {
    dialogoAporteMeta.close();
});

botaoFecharResgateMeta.addEventListener("click", () => {
    dialogoResgateMeta.close();
});

async function iniciarPagina() {
    const usuario = await carregarSessao();

    if (!usuario) {
        return;
    }

    const contaCarregada = await carregaConta(usuario.id);

    if (!contaCarregada) {
        return;
    }
        await carregarMinhasMetas();
}

iniciarPagina();