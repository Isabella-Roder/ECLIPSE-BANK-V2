import { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, SafeAreaView, StyleSheet, Text, View, Modal, TextInput } from "react-native";
import { API_URL } from "@/config/api";
import { buscarTokenCsrf, obterTokenCsrf } from "@/config/csrf";
import { useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { StatusBar } from "expo-status-bar";

type ProdutoInvestimento = {
    id: number;
    nome: string;
    codigo: string;
    tipo: string;
    valorMinimo: number;
    rentabilidadeAnualEstimada: number;
};

type AplicacaoInvestimento = {
    id: number;
    produtoId: number;
    produtoNome: string;
    produtoCodigo: string;
    produtoTipo: string;
    rentabilidadeAnualEstimada: number;
    valorAplicado: number;
    saldoInvestido: number;
    status: string;
    aplicadaEm: string;
}

export default function InvestimentoScreen() {

    const roteador = useRouter();

    const parametros = useLocalSearchParams<{contaId: string}>();

    const [produtos, setProdutos] = useState<ProdutoInvestimento[]>([]);
    const [aplicacoes, setAplicacoes] = useState<AplicacaoInvestimento[]>([]);

    const [produtoSelecionado, setProdutoSelecionado] = useState<ProdutoInvestimento | null>(null);
    const [valorAplicado, setValorAplicado] = useState("");

    const [aplicando, setAplicando] = useState(false);
    const [erroAplicacao, setErroAplicacao] = useState("");

    const [aplicacaoSelecionada, setAplicacaoSelecionada] = useState<AplicacaoInvestimento | null>(null);
    const [valorResgate, setValorResgate] = useState("");
    const [resgatando, setResgatando] = useState(false);
    const [erroResgate, setErroResgate] = useState("");

    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");

    function formatarDinheiro(valor:number) {
        return Number(valor).toLocaleString("pt-BR", {
            style: "currency",
            currency: "BRL"
        });
    }

    useEffect(() => {
        async function carregarInvestimentos() {
            try {
                setCarregando(true);
                setErro("");

                const [respostaProdutos, respostaCarteira] = await Promise.all([
                    fetch(`${API_URL}/investimentos/produtos`, {
                        credentials: "include"
                    }),
                    fetch(`${API_URL}/contas/${parametros.contaId}/investimentos/carteira`, {
                        credentials: "include"
                    })
                ]);

                const [corpoProdutos, corpoCarteira] = await Promise.all([
                    respostaProdutos.json(),
                    respostaCarteira.json()
                ]);

                if (!respostaProdutos.ok) {
                    throw new Error(corpoProdutos.mensagem || "Não foi possivel carregar o catálogo");
                }

                if (!respostaCarteira.ok) {
                    throw new Error(corpoCarteira.mensagem || "Não foi possivel carregar a carteira");
                }

                setProdutos(corpoProdutos);
                setAplicacoes(corpoCarteira);
            } catch (erro) {
                const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";
                setErro(mensagem);
            } finally {
                setCarregando(false);
            }
        }

        if (parametros.contaId) {
            carregarInvestimentos();
        }
    }, [parametros.contaId]);

    if (carregando) {
        return(
            <SafeAreaView style={estilos.centralizado}>
                <ActivityIndicator size="large" color="#8b5cf6" />
                <Text style={estilos.mensagem}>Carregando investimentos...</Text>
            </SafeAreaView>
        );
    }

    if (erro) {
        return (
            <SafeAreaView style={estilos.centralizado}>
                <Text style={estilos.erro}>{erro}</Text>

                <Pressable onPress={() => roteador.back()}>
                    <Text style={estilos.voltar}>← Voltar</Text>
                </Pressable>
            </SafeAreaView>
        );
    }

    function renderizarProduto({
        item
    }: {
        item: ProdutoInvestimento;
    }) {
        return (
            <View style={estilos.cartaoProduto}>
                <Text style={estilos.tipoProduto}>
                    {item.tipo.replaceAll("_", " ")}
                </Text>

                <Text style={estilos.nomeProduto}>
                    {item.nome}
                </Text>

                <Text style={estilos.codigoProduto}>
                    {item.codigo}
                </Text>

                <Text style={estilos.valorMinimo}>
                    A partir de {formatarDinheiro(item.valorMinimo)}
                </Text>

                <Text style={estilos.rentabilidade}>
                    {item.rentabilidadeAnualEstimada}% ao ano
                </Text>

                <Pressable style={estilos.botaoInvestir} onPress={() => abrirAplicacao(item)}>
                    <Text style={estilos.textoBotaoInvestir}>
                        Investir
                    </Text>
                </Pressable>
            </View>
        )
    }

    function abrirAplicacao(produto:ProdutoInvestimento) {
        setProdutoSelecionado(produto);
        setValorAplicado("");
    }

    function fecharAplicacao() {
        setProdutoSelecionado(null);
        setValorAplicado("");
        setErroAplicacao("");
    }

    async function realizarAplicacao() {
        if (!produtoSelecionado) {
            return;
        }

        const valor = Number(valorAplicado.replace(",", "."));

        if (!Number.isFinite(valor)) {
            setErroAplicacao("Informe um valor válido.");
            return;
        }

        if (valor < produtoSelecionado.valorMinimo) {
            setErroAplicacao(`O valor mínimo é ${formatarDinheiro(produtoSelecionado.valorMinimo)}`);
            return;
        }

        const dados = {
            produtoId: produtoSelecionado.id,
            valor
        }

        try {
            setAplicando(true);
            setErroAplicacao("");

            await buscarTokenCsrf();

            const resposta = await fetch(`${API_URL}/contas/${parametros.contaId}/investimentos/aplicacoes`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    "X-XSRF-TOKEN": obterTokenCsrf() ?? ""
                },
                body: JSON.stringify(dados)
            });

            const corpo = await resposta.json();

            if (!resposta.ok) {
                throw new Error(corpo.mensagem || "Não foi possivel realizar a aplicação.");
            }

            setAplicacoes((aplicacoesAtuais) => [
                corpo,
                ...aplicacoesAtuais
            ]);

            fecharAplicacao();
        } catch (erro) {
            const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";
            setErroAplicacao(mensagem);
        } finally {
            setAplicando(false);
        }
    }

    const totalInvestido = aplicacoes.reduce(
        (total, aplicacao) =>
            total + Number(aplicacao.saldoInvestido),
        0
    );

    function renderizarAplicacao(aplicacao: AplicacaoInvestimento) {
        const ativa = aplicacao.status === "ATIVA";

        return (
            <View key={aplicacao.id} style={estilos.cartaoAplicacao}>
                <Text style={[
                    estilos.statusAplicacao, 
                    ativa
                        ? estilos.statusAtiva
                        : estilos.statusResgatada
                ]}>
                    {aplicacao.status}
                </Text>

                <Text style={estilos.nomeAplicacao}>
                    {aplicacao.produtoNome}
                </Text>

                <Text style={estilos.codigoProduto}>
                    {aplicacao.produtoCodigo}
                </Text>

                <Text style={estilos.rotuloSaldoInvestido}>
                    SALDO INVESTIDO
                </Text>

                <Text style={estilos.saldoInvestido}>
                    {formatarDinheiro(aplicacao.saldoInvestido)}
                </Text>

                <Text style={estilos.valorOriginal}>
                    Aplicado:{" "}
                    {formatarDinheiro(aplicacao.valorAplicado)}
                </Text>

                <Text style={estilos.dataAplicacao}>
                    {new Date(aplicacao.aplicadaEm).toLocaleString("pt-BR")}
                </Text>

                {ativa ? (
                    <Pressable style={estilos.botaoResgatar} onPress={() => abrirResgate(aplicacao)}>
                        <Text style={estilos.textoBotaoResgatar}>
                            Resgatar
                        </Text>
                    </Pressable>
                ) : null}
                
            </View>
        );
    }

    function abrirResgate(aplicacao:AplicacaoInvestimento) {
        setAplicacaoSelecionada(aplicacao);
        setValorResgate("");
        setErroResgate("");
    }

    function fecharResgate() {
        setAplicacaoSelecionada(null);
        setValorResgate("");
        setErroResgate("");
    }

    async function realizarResgate() {
        if (!aplicacaoSelecionada) {
            return;
        }

        const valor = Number(valorResgate.replace(",", "."));

        if (!Number.isFinite(valor) || valor <= 0) {
            setErroResgate("Informe um valor válido.");
            return;
        }

        if (valor > aplicacaoSelecionada.saldoInvestido) {
            setErroResgate("O valor não pode ser maior que o saldo investido.");
            return;
        }

        try {
            setResgatando(true);
            setErroResgate("");

            await buscarTokenCsrf();

            const resposta = await fetch(`${API_URL}/contas/${parametros.contaId}/investimentos/${aplicacaoSelecionada.id}/resgates`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    "X-XSRF-TOKEN": obterTokenCsrf() ?? ""
                },
                body: JSON.stringify({
                    valor
                })
            });

            const corpo = await resposta.json();

            if (!resposta.ok) {
                throw new Error(corpo.mensagem || "Não foi possivel realizar o resgate.");
            }

            setAplicacoes((aplicacoesAtuais) => 
                aplicacoesAtuais.map((aplicacao) =>
                    aplicacao.id === corpo.id
                        ? corpo
                        : aplicacao));

            fecharResgate();
        } catch (erro) {
            const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";
            setErroResgate(mensagem);
        } finally {
            setResgatando(false);
        }
    }

    return (
        <SafeAreaView style={estilos.pagina}>
            <StatusBar style="light" />
            <View style={estilos.luzSuperior} />
            <View style={estilos.cabecalho}>
                <View style={estilos.topoPagina}>
                    <Pressable style={estilos.botaoVoltar} onPress={() => roteador.back()} accessibilityLabel="Voltar">
                        <Ionicons name="arrow-back" size={21} color="#c8b6ff" />
                    </Pressable>
                    <View style={estilos.identidade}>
                        <View style={estilos.simboloMarca}><View style={estilos.recorteMarca} /></View>
                        <Text style={estilos.marca}>ECLIPSE <Text style={estilos.marcaDestaque}>BANK</Text></Text>
                    </View>
                </View>

                <View style={estilos.apresentacao}>
                    <View style={estilos.iconeOperacao}><Ionicons name="trending-up-outline" size={27} color="#92e3c4" /></View>
                    <Text style={estilos.etiquetaPagina}>SEU DINHEIRO EM MOVIMENTO</Text>
                    <Text style={estilos.titulo}>Investimentos</Text>
                    <Text style={estilos.subtitulo}>Construa sua carteira e acompanhe sua evolução.</Text>
                </View>
            </View>

            <FlatList
            data={produtos}
            keyExtractor={(item) => String(item.id)}
            renderItem={renderizarProduto}
            contentContainerStyle={estilos.lista}
            ItemSeparatorComponent={() => (
                <View style={estilos.separador} />
            )}
            ListHeaderComponent={
                <View style={estilos.resumoCarteira}>
                    <Text style={estilos.rotuloResumo}>
                        MINHA CARTEIRA
                    </Text>

                    <Text style={estilos.totalInvestido}>
                        {formatarDinheiro(totalInvestido)}
                    </Text>

                    <Text style={estilos.textoResumo}>
                        {aplicacoes.length} aplicação(ões)
                    </Text>

                    <View style={estilos.listaAplicacoes}>
                        {aplicacoes.length === 0 ? (
                            <Text style={estilos.carteiraVazia}>
                                Você não possui investimentos.
                            </Text>
                        ) : (
                            aplicacoes.map(renderizarAplicacao)
                        )}
                    </View>

                    <Text style={estilos.tituloCatalogo}>
                        Produtos disponíveis
                    </Text>

                </View>
            }
            ListEmptyComponent={
                <Text style={estilos.listaVazia}>
                Nenhum produto disponível.
                </Text>
            }
            />

            <Modal
                visible={produtoSelecionado !== null}
                transparent
                animationType="slide"
                onRequestClose={fecharAplicacao}
            >

                <View style={estilos.fundoModal}>
                    <View style={estilos.conteudoModal}>
                        <Text style={estilos.rotuloResumo}>
                            NOVA APLICAÇÃO
                        </Text>

                        <Text style={estilos.tituloModal}>
                            Quanto você quer investir?
                        </Text>

                        <Text style={estilos.produtoModal}>
                            {produtoSelecionado?.nome}
                        </Text>

                        <Text style={estilos.minimoModal}>
                            Aplicação mínima: {" "}
                            {formatarDinheiro(
                                produtoSelecionado?.valorMinimo || 0
                            )}
                        </Text>

                        <TextInput
                            style={estilos.entradaValor}
                            value={valorAplicado}
                            onChangeText={setValorAplicado}
                            keyboardType="decimal-pad"
                            placeholder="0.00"
                            placeholderTextColor="#797380"
                        />

                        {erroAplicacao ? (
                            <Text style={estilos.erroModal}>{erroAplicacao}</Text>
                        ): null}

                        <Pressable style={[estilos.botaoInvestir, aplicando && estilos.botaoDesabilitado]} onPress={realizarAplicacao} disabled={aplicando}>
                            <Text style={estilos.textoBotaoInvestir}>
                                {aplicando ? "Aplicando..." : "Confirmar aplicação"}
                            </Text>
                        </Pressable>

                        <Pressable style={estilos.botaoCancelar} onPress={fecharAplicacao}>
                            <Text style={estilos.textoCancelar}>Cancelar</Text>
                        </Pressable>
                    </View>
                </View>
            </Modal>

            <Modal
                visible={aplicacaoSelecionada !== null}
                transparent
                animationType="slide"
                onRequestClose={fecharResgate}
            >

                <View style={estilos.fundoModal}>
                        <View style={estilos.conteudoModal}>
                            <Text style={estilos.rotuloResumo}>
                                RESGATE
                            </Text>

                            <Text style={estilos.tituloModal}>
                                Quanto você quer resgatar?
                            </Text>

                            <Text style={estilos.produtoModal}>
                                {aplicacaoSelecionada?.produtoNome}
                            </Text>

                            <Text style={estilos.minimoModal}>
                                Saldo investido:{" "}
                                {formatarDinheiro(aplicacaoSelecionada?.saldoInvestido || 0)}
                            </Text>

                            <TextInput
                                style={estilos.entradaValor}
                                value={valorResgate}
                                onChangeText={setValorResgate}
                                keyboardType="decimal-pad"
                                placeholder="0.00"
                                placeholderTextColor="#797380"
                            />

                            {erroResgate ? (
                                <Text style={estilos.erroModal}>
                                    {erroResgate}
                                </Text>
                            ) : null}

                            <Pressable
                                style={[
                                    estilos.botaoResgatar,
                                    resgatando && estilos.botaoDesabilitado
                                ]}
                                disabled={resgatando}
                                onPress={realizarResgate}
                            >
                                <Text style={estilos.textoBotaoResgatar}>
                                    {resgatando
                                        ? "Resgatando..."
                                        : "Confirmar resgate"}
                                </Text>
                            </Pressable>

                            <Pressable 
                                style={estilos.botaoCancelar}
                                onPress={fecharResgate}
                            >
                                <Text style={estilos.textoCancelar}>
                                    Cancelar
                                </Text>
                            </Pressable>
                        </View>
                </View>
            </Modal>
        </SafeAreaView>
    );
}

const estilos = StyleSheet.create({
    pagina: {
        flex: 1,
        paddingHorizontal: 22,
        paddingTop: 18,
        backgroundColor: "#0c0d15"
    },
    luzSuperior: { position: "absolute", top: -170, right: -120, width: 340, height: 340, borderRadius: 170, backgroundColor: "rgba(139,92,246,.13)" },
    topoPagina: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
    botaoVoltar: { width: 42, height: 42, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: "rgba(182,156,255,.2)", borderRadius: 21, backgroundColor: "rgba(139,92,246,.08)" },
    identidade: { flexDirection: "row", alignItems: "center", gap: 9 },
    simboloMarca: { width: 31, height: 31, alignItems: "center", justifyContent: "center", overflow: "hidden", borderRadius: 16, backgroundColor: "#8b5cf6" },
    recorteMarca: { width: 25, height: 25, marginLeft: 13, borderRadius: 13, backgroundColor: "#17131f" },
    marca: { color: "#ffffff", fontSize: 10, fontWeight: "800", letterSpacing: 1.4 },
    marcaDestaque: { color: "#b69cff" },
    apresentacao: { marginTop: 30 },
    iconeOperacao: { width: 52, height: 52, alignItems: "center", justifyContent: "center", marginBottom: 18, borderWidth: 1, borderColor: "rgba(118,221,183,.18)", borderRadius: 17, backgroundColor: "rgba(69,201,151,.09)" },
    etiquetaPagina: { color: "#76ddb7", fontSize: 9, fontWeight: "800", letterSpacing: 1.7 },
    
    titulo: {
        marginTop: 8,
        color: "#ffffff",
        fontSize: 34,
        fontWeight: "800",
        letterSpacing: -1
    },
    centralizado: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    backgroundColor: "#0c0d15"
    },

    mensagem: {
    marginTop: 12,
    color: "#ded9e7",
    fontSize: 15
    },

    erro: {
    marginBottom: 20,
    color: "#ff8c8c",
    fontSize: 16,
    textAlign: "center"
    },

    voltar: {
    color: "#b69cff",
    fontSize: 15,
    fontWeight: "700"
    },
    cabecalho: {
    marginBottom: 24
    },

    subtitulo: {
    marginTop: 8,
    color: "#97919f",
    fontSize: 15
    },

    lista: {
    paddingBottom: 32
    },

    resumoCarteira: {
    marginBottom: 20,
    padding: 23,
    borderWidth: 1,
    borderColor: "rgba(182,156,255,.22)",
    borderRadius: 21,
    backgroundColor: "#191525",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 14 },
    shadowOpacity: .24,
    shadowRadius: 22,
    elevation: 8
    },

    rotuloResumo: {
    color: "#b69cff",
    fontSize: 11,
    fontWeight: "800",
    letterSpacing: 1.5
    },

    numeroResumo: {
    marginTop: 10,
    color: "#ffffff",
    fontSize: 34,
    fontWeight: "800"
    },

    textoResumo: {
    color: "#97919f",
    fontSize: 13
    },

    tituloCatalogo: {
    marginTop: 30,
    color: "#ffffff",
    fontSize: 20,
    fontWeight: "800"
    },

    cartaoProduto: {
    padding: 21,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,.07)",
    borderRadius: 19,
    backgroundColor: "#12131d"
    },

    tipoProduto: {
    color: "#b69cff",
    fontSize: 11,
    fontWeight: "800"
    },

    nomeProduto: {
    marginTop: 14,
    color: "#ffffff",
    fontSize: 21,
    fontWeight: "800"
    },

    codigoProduto: {
    marginTop: 4,
    color: "#797380",
    fontSize: 12
    },

    valorMinimo: {
    marginTop: 20,
    color: "#ded9e7",
    fontSize: 14,
    fontWeight: "700"
    },

    rentabilidade: {
    marginTop: 6,
    color: "#76ddb7",
    fontSize: 13,
    fontWeight: "700"
    },

    botaoInvestir: {
    minHeight: 48,
    marginTop: 20,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 10,
    backgroundColor: "#8b5cf6"
    },

    textoBotaoInvestir: {
    color: "#ffffff",
    fontSize: 15,
    fontWeight: "800"
    },

    separador: {
    height: 12
    },

    listaVazia: {
    paddingVertical: 40,
    color: "#97919f",
    textAlign: "center"
    },

    fundoModal: {
    flex: 1,
    justifyContent: "flex-end",
    backgroundColor: "rgba(5, 6, 11, 0.78)"
    },

    conteudoModal: {
    padding: 24,
    paddingBottom: 36,
    borderTopWidth: 1,
    borderTopColor: "rgba(182,156,255,.18)",
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    backgroundColor: "#171824"
    },

    tituloModal: {
    marginTop: 8,
    color: "#ffffff",
    fontSize: 26,
    fontWeight: "800"
    },

    produtoModal: {
    marginTop: 24,
    color: "#b69cff",
    fontSize: 18,
    fontWeight: "800"
    },

    minimoModal: {
    marginTop: 6,
    color: "#97919f",
    fontSize: 13
    },

    entradaValor: {
    minHeight: 58,
    marginTop: 22,
    paddingHorizontal: 18,
    borderWidth: 1,
    borderColor: "#34294b",
    borderRadius: 12,
    color: "#ffffff",
    backgroundColor: "#0d0e16",
    fontSize: 22,
    fontWeight: "700"
    },

    botaoCancelar: {
    minHeight: 48,
    marginTop: 10,
    alignItems: "center",
    justifyContent: "center"
    },

    textoCancelar: {
    color: "#97919f",
    fontSize: 15,
    fontWeight: "700"
    },

    erroModal: {
    marginTop: 14,
    color: "#ff8c8c",
    fontSize: 14
    },

    botaoDesabilitado: {
    opacity: 0.55
    },

    totalInvestido: {
    marginTop: 10,
    color: "#ffffff",
    fontSize: 30,
    fontWeight: "800"
    },

    listaAplicacoes: {
    gap: 12,
    marginTop: 22
    },

    carteiraVazia: {
    paddingVertical: 20,
    color: "#97919f",
    textAlign: "center"
    },

    cartaoAplicacao: {
    padding: 18,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,.07)",
    borderRadius: 17,
    backgroundColor: "#0d0e16"
    },

    statusAplicacao: {
    alignSelf: "flex-start",
    paddingHorizontal: 9,
    paddingVertical: 5,
    borderRadius: 20,
    fontSize: 10,
    fontWeight: "800"
    },

    statusAtiva: {
    color: "#76ddb7",
    backgroundColor: "#123b30"
    },

    statusResgatada: {
    color: "#ff93ad",
    backgroundColor: "#421b28"
    },

    nomeAplicacao: {
    marginTop: 14,
    color: "#ffffff",
    fontSize: 18,
    fontWeight: "800"
    },

    rotuloSaldoInvestido: {
    marginTop: 20,
    color: "#797380",
    fontSize: 10,
    fontWeight: "800",
    letterSpacing: 1
    },

    saldoInvestido: {
    marginTop: 5,
    color: "#ffffff",
    fontSize: 25,
    fontWeight: "800"
    },

    valorOriginal: {
    marginTop: 7,
    color: "#aaa4b1",
    fontSize: 12
    },

    dataAplicacao: {
    marginTop: 12,
    color: "#797380",
    fontSize: 11
    },

    botaoResgatar: {
        minHeight: 48,
        marginTop: 18,
        alignItems: "center",
        justifyContent: "center",
        borderWidth: 1,
        borderColor: "#397d66",
        borderRadius: 10,
        backgroundColor: "#123b30"
        },

        textoBotaoResgatar: {
        color: "#76ddb7",
        fontSize: 15,
        fontWeight: "800"
    }
});
