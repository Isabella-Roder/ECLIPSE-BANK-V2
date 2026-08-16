const API_URL = `http://${window.location.hostname}:8080/api`;

const listaProduto = document.getElementById("lista-produtos-investimento");
const estadoCatalogo = document.getElementById("estado-catalogo");
const quantidadeProdutos = document.getElementById("quantidade-produtos");
const mensagem = document.getElementById("mensagem-investimentos");

const dialogoAplicacao = document.getElementById("dialog-aplicacao");
const formularioAplicacao = document.getElementById("form-aplicacao-investimento");
const produtoIdAplicacao = document.getElementById("produto-id-aplicacao");
const nomeProdutoAplicacao = document.getElementById("nome-produto-aplicacao");
const minimoProdutoAplicacao = document.getElementById("minimo-produto-aplicacao");
const valorAplicacao = document.getElementById("valor-aplicacao");
const mensagemAplicacao = document.getElementById("mensagem-aplicacao");
const botaoFecharAplicacao = document.getElementById("botao-fechar-aplicacao");
const botaoConfirmarAplicacao = document.getElementById("botao-confirmar-aplicacao");

const listaCarteira = document.getElementById("lista-carteira-investimentos");
const quantidadeAplicacoes = document.getElementById("quantidade-aplicacoes");

const dialogoResgate = document.getElementById("dialog-resgate");
const formularioResgate = document.getElementById("form-resgate-investimento");
const aplicacaoIdResgate = document.getElementById("aplicacao-id-resgate");
const nomeProdutoResgate = document.getElementById("nome-produto-resgate");
const saldoProdutoResgate = document.getElementById("saldo-produto-resgate");
const valorResgate = document.getElementById("valor-resgate");
const mensagemResgate = document.getElementById("mensagem-resgate");
const botaoFecharResgate = document.getElementById("botao-fechar-resgate");
const botaoConfirmarResgate = document.getElementById("botao-confirmar-resgate");

let contaAtual;

function formatarDinheiro(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

async function carregarSessao() {
    try {
        const respostaSessao = await fetch(`${API_URL}/usuarios/sessao`, {
            credentials: "include"
        });

        if (respostaSessao.status === 401) {
            window.location.href = "login.html";
            return null;
        }

        const usuario = await respostaSessao.json();

        if (!respostaSessao.ok) {
            throw new Error(usuario.mensagem || "Erro ao carregar sessão.");
        }

        return usuario
        
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = "Erro: " + erro.message;
        return null;
    }
}

async function carregarConta(usuarioId) {
    try {
        const respostaConta = await fetch(`${API_URL}/contas/usuario/${usuarioId}`, {
            credentials: "include"
        });

        const conta = await respostaConta.json();

        if (!respostaConta.ok) {
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

async function carregarProdutos() {
    try {
        const resposta = await fetch(`${API_URL}/investimentos/produtos`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const produtos = await resposta.json();

        if (!resposta.ok) {
            throw new Error(produtos.mensagem || "Não foi possivel carregar o catálogo de produtos");
        }

        console.log(produtos);

        renderizarProdutos(produtos);

        quantidadeProdutos.textContent = `${produtos.length} produto(s)`;
    } catch (erro) {
        console.error(erro);
        estadoCatalogo.textContent = "";
        mensagem.textContent = "Erro: " + erro.message;
    }
}

function renderizarProdutos(produtos) {
    listaProduto.replaceChildren();

    if (produtos.length === 0) {
        const estadoVazio = document.createElement("p");
        estadoVazio.textContent = "Nenhum produto disponível.";
        listaProduto.appendChild(estadoVazio);
        return;
    }

    produtos.forEach((produto) => {
        const cartao = document.createElement("article");
        cartao.className = "cartao-investimento";

        const tipo = document.createElement("span");
        tipo.className = "tipo-investimento";
        tipo.textContent = produto.tipo.replaceAll("_", " ");

        const nome = document.createElement("h3");
        nome.textContent = produto.nome;

        const codigo = document.createElement("p");
        codigo.className = "codigo-investimento";
        codigo.textContent = produto.codigo;

        const valorMinimo = document.createElement("strong");
        valorMinimo.textContent = `A partir de ${formatarDinheiro(produto.valorMinimo)}`;

        const rentabilidade = document.createElement("span")
        rentabilidade.className = "rentabilidade-investimento";
        rentabilidade.textContent = `${produto.rentabilidadeAnualEstimada}% ao ano`;

        const botaoInvestir = document.createElement("button");
        botaoInvestir.className = "botao-investir";
        botaoInvestir.type = "button";
        botaoInvestir.textContent = "Investir";

        cartao.append(
            tipo,
            nome,
            codigo,
            valorMinimo,
            rentabilidade,
            botaoInvestir
        );

        botaoInvestir.addEventListener("click", () => {
            abrirAplicacao(produto);
        });

        listaProduto.appendChild(cartao);
    });
}

async function carregarCarteira() {
    try {
        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/investimentos/carteira`, {
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const aplicacoes = await resposta.json();

        if (!resposta.ok) {
            throw new Error(aplicacoes.mensagem || "Não foi possível carregar a carteira.");
        }

        renderizarCarteira(aplicacoes)

        quantidadeAplicacoes.textContent = `${aplicacoes.length} aplicação(ões)`;
    } catch (erro) {
        console.error(erro);

        listaCarteira.replaceChildren();

        const mensagemErro = document.createElement("p");
        mensagemErro.id = "estado-carteira";
        mensagemErro.textContent = "Erro: " + erro.message;

        listaCarteira.appendChild(mensagemErro);
    }
}

function renderizarCarteira(aplicacoes) {
    listaCarteira.replaceChildren();

    if (aplicacoes.length === 0) {
        const estadoVazio = document.createElement("p");
        estadoVazio.id = "estado-carteira";
        estadoVazio.textContent = "Você ainda não possui investimentos.";
        listaCarteira.appendChild(estadoVazio);
        return;
    }

    aplicacoes.forEach((aplicacao) => {
        const cartao = document.createElement("article");
        cartao.className = "cartao-aplicacao";

        const status = document.createElement("span");
        status.className = `status-aplicacao ${aplicacao.status.toLowerCase()}`;
        status.textContent = aplicacao.status;

        const nome = document.createElement("h3")
        nome.textContent = aplicacao.produtoNome;

        const codigo = document.createElement("p");
        codigo.className = "codigo-investimento";
        codigo.textContent = aplicacao.produtoCodigo;

        const saldo = document.createElement("strong");
        saldo.className = "saldo-investido";
        saldo.textContent = formatarDinheiro(aplicacao.saldoAtualEstimado);

        const valorOriginal = document.createElement("span");
        valorOriginal.className = "valor-original-aplicacao";
        valorOriginal.textContent = `Aplicado: ${formatarDinheiro(aplicacao.valorAplicado)}`;

        const data = document.createElement("time");
        data.dateTime = aplicacao.aplicadaEm;
        data.textContent = new Date(aplicacao.aplicadaEm).toLocaleString("pt-BR");

        cartao.append(
            status,
            nome,
            codigo,
            saldo,
            valorOriginal,
            data
        );

        if (aplicacao.status === "ATIVA") {
            const botaoResgatar = document.createElement("button");
            botaoResgatar.className = "botao-resgatar";
            botaoResgatar.type = "button";
            botaoResgatar.textContent = "Resgatar";

            botaoResgatar.addEventListener("click", () => {
                abrirResgate(aplicacao);
            });

            cartao.appendChild(botaoResgatar);
        }

        if (aplicacao.produtoTipo === "FUNDO_IMOBILIARIO" && aplicacao.status === "ATIVA") {
            const botaoProvento = document.createElement("button");
            botaoProvento.type = "button";
            botaoProvento.className = "botao-provento";
            botaoProvento.textContent = "Receber provento";

            botaoProvento.addEventListener("click", () => {
                receberProvento(aplicacao, botaoProvento);
            });

            cartao.appendChild(botaoProvento);
        }

        listaCarteira.appendChild(cartao);
    });
}

async function receberProvento(aplicacao, botao) {
    if (!confirm(`Receber o provento mensal de ${aplicacao.produtoNome}?`)) {
        return;
    }

    try {
        botao.disabled = true;
        botao.textContent = "Recebendo...";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/investimentos/${aplicacao.id}/proventos`, {
            method: "POST",
            credentials: "include"
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const provento = await resposta.json();

        if (!resposta.ok) {
            throw new Error(provento.mensagem || "Não foi possivel receber o provento.");
        }

        mensagem.className = "mensagem-operacao sucesso";
        mensagem.textContent = `Provento de ${formatarDinheiro(provento.valor)} recebido com sucesso.`;

        await carregarCarteira();
    } catch (erro) {
        console.error(erro);
        mensagem.className = "mensagem-operacao erro";
        mensagem.textContent = "Erro: " + erro.message;
    } finally {
        botao.disabled = false;
        botao.textContent = "Receber provento";
    }
}

function abrirResgate(aplicacao) {
    aplicacaoIdResgate.value = aplicacao.id;
    nomeProdutoResgate.textContent = aplicacao.produtoNome;

    saldoProdutoResgate.textContent = `Saldo investido: ${formatarDinheiro(aplicacao.saldoAtualEstimado)}`;

    valorResgate.max = aplicacao.saldoAtualEstimado;
    valorResgate.value = "";
    mensagemResgate.textContent = "";

    dialogoResgate.showModal();
    valorResgate.focus();
}

function abrirAplicacao(produto) {
    produtoIdAplicacao.value = produto.id;
    nomeProdutoAplicacao.textContent = produto.nome;

    minimoProdutoAplicacao.textContent = `Aplicação mínima: ${formatarDinheiro(produto.valorMinimo)}`;

    valorAplicacao.min = produto.valorMinimo;
    valorAplicacao.value = "";
    mensagemAplicacao.textContent = "";

    dialogoAplicacao.showModal();
    valorAplicacao.focus();
}

botaoFecharAplicacao.addEventListener("click", () => {
    dialogoAplicacao.close();
});

botaoFecharResgate.addEventListener("click", () => {
    dialogoResgate.close();
});

formularioAplicacao.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    
    const dados = {
        produtoId: Number(produtoIdAplicacao.value),
        valor: Number(valorAplicacao.value)
    };

    try {
        botaoConfirmarAplicacao.disabled = true;
        botaoConfirmarAplicacao.textContent = "Aplicando...";
        mensagemAplicacao.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/investimentos/aplicacoes`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const aplicacao = await resposta.json();

        if (!resposta.ok) {
            throw new Error(aplicacao.mensagem || "Não foi possivel realizar a aplicação.");
        }

        dialogoAplicacao.close();

        mensagem.className = "mensagem-operacao sucesso";
        mensagem.textContent = `Aplicação em ${aplicacao.produtoNome} realizada com sucesso.`;
        console.log(aplicacao);
        await carregarCarteira();
    } catch (erro) {
        console.error(erro);
        mensagemAplicacao.className = "mensagem-operacao erro";
        mensagemAplicacao.textContent = "Erro: " + erro.message;
    } finally {
        botaoConfirmarAplicacao.disabled = false;
        botaoConfirmarAplicacao.innerHTML = `
            Confirmar aplicação
            <span aria-hidden="true">→</span>
        `;
    }
});

formularioResgate.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    if (!confirm("Deseja resgatar o seu investimento?")) {
        return;
    }
    
    const dados = {
        valor: Number(valorResgate.value)
    };

    try {
        botaoConfirmarResgate.disabled = true;
        botaoConfirmarResgate.textContent = "Resgatando...";
        mensagemResgate.textContent = "";

        const resposta = await fetch(`${API_URL}/contas/${contaAtual.id}/investimentos/${aplicacaoIdResgate.value}/resgates`, {
            method: "POST",
            credentials: "include",
            headers: {
                "content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (resposta.status === 401) {
            window.location.href = "login.html";
            return;
        }

        const aplicacao = await resposta.json();

        if (!resposta.ok) {
            throw new Error(aplicacao.mensagem || "Não foi possivel realizar o resgate.");
        }

        dialogoResgate.close();

        mensagem.className = "mensagem-operacao sucesso";
        mensagem.textContent = `Resgate de ${aplicacao.produtoNome} realizado com sucesso.`;

        await carregarCarteira();
    } catch (erro) {
        console.error(erro);

        mensagemResgate.className = "mensagem-operacao erro";
        mensagemResgate.textContent = "Erro: " + erro.message;
    } finally {
        botaoConfirmarResgate.disabled = false;
        botaoConfirmarResgate.innerHTML = `
            Confirmar resgate
            <span aria-hidden="true">→</span>
        `;
    }
})

async function iniciarPagina() {
    const usuario = await carregarSessao();

    if (!usuario) {
        return;
    }

    const contaCarregada = await carregarConta(usuario.id);

    if (!contaCarregada) {
        return;
    }

    await Promise.all([
        carregarProdutos(),
        carregarCarteira()
    ]);
}

iniciarPagina();
