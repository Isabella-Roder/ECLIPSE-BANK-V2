import { useEffect, useState } from "react";
import { ActivityIndicator, Pressable, SafeAreaView, ScrollView, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { StatusBar } from "expo-status-bar";

import { API_URL } from "../config/api";
import { buscarTokenCsrf, obterTokenCsrf } from "../config/csrf";

type Conta = {
    id: number;
    agencia: string;
    numero: string;
    saldo: number;
    status: string;
    usuarioId: number;
    titular: string;
};

export default function ContaScreen() {
    const roteador = useRouter();
    const parametros = useLocalSearchParams<{ usuarioId: string}>();

    const [conta, setConta] = useState<Conta | null>(null);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");

    useEffect(() => {
        carregarConta();
    }, []);

    async function carregarConta() {
        try {
            setCarregando(true);
            setErro("");

            let resposta = await fetch(`${API_URL}/contas/usuario/${parametros.usuarioId}`, {
                credentials: "include"
            });

            if (resposta.status === 404) {
                await buscarTokenCsrf();

                resposta = await fetch(`${API_URL}/contas/usuario/${parametros.usuarioId}`, {
                    method: "POST",
                    credentials: "include",
                    headers: {
                        "X-XSRF-TOKEN": obterTokenCsrf() ?? ""
                    }
                });
            }

            const textoResposta = await resposta.text();
            const corpo = textoResposta ? JSON.parse(textoResposta) : {};

            if (!resposta.ok) {
                const mensagem = resposta.status === 401
                    ? "Sua sessão não foi reconhecida. Entre novamente."
                    : corpo.mensagem || "Não foi possivel carregar a conta.";

                throw new Error(mensagem);
            }

            setConta(corpo);
        } catch (erro) {
            const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";
            setErro(mensagem);
        } finally {
            setCarregando(false);
        }
    }

    function formatarDinheiro(valor:number) {
        return Number(valor).toLocaleString("pt-BR", {
            style: "currency",
            currency: "BRL"
        });
    }

    function sair() {
        roteador.replace("/");
    }

    if (carregando) {
        return (
            <SafeAreaView style={estilos.centralizado}>
                <ActivityIndicator size="large" color="#8b5cf6" />
                <Text style={estilos.mensagem}>Carregando sua conta...</Text>
            </SafeAreaView>
        );
    }

    if (erro || !conta) {
        return (
            <SafeAreaView style={estilos.centralizado}>
                <Text style={estilos.erro}>{erro}</Text>

                <Pressable style={estilos.botao} onPress={carregarConta}>
                    <Text style={estilos.textoBotao}>Tentar novamente</Text>
                </Pressable>
            </SafeAreaView>
        );
    }

    return(
        <SafeAreaView style={estilos.pagina}>
            <StatusBar style="light" />
            <View style={estilos.luzSuperior} />

            <ScrollView
                showsVerticalScrollIndicator={false}
                contentContainerStyle={estilos.conteudo}
            >
                <View style={estilos.cabecalho}>
                    <View style={estilos.identidade}>
                        <View style={estilos.simboloMarca}>
                            <View style={estilos.recorteMarca} />
                        </View>

                        <Text style={estilos.marca}>
                            ECLIPSE <Text style={estilos.marcaDestaque}>BANK</Text>
                        </Text>
                    </View>

                    <Pressable
                        style={({ pressed }) => [estilos.botaoSair, pressed && estilos.pressionado]}
                        onPress={sair}
                        accessibilityLabel="Sair da conta"
                    >
                        <Ionicons name="log-out-outline" size={20} color="#b69cff" />
                    </Pressable>
                </View>

                <View style={estilos.apresentacao}>
                    <Text style={estilos.etiqueta}>SUA CONTA</Text>
                    <Text style={estilos.saudacao}>Olá, {conta.titular}</Text>
                    <Text style={estilos.subtitulo}>Seu dinheiro, seus planos, no seu ritmo.</Text>
                </View>

                <View style={estilos.cartao}>
                    <View style={estilos.orbitaExterna} />
                    <View style={estilos.orbitaInterna} />

                    <View style={estilos.topoCartao}>
                        <Text style={estilos.rotuloSaldo}>SALDO DISPONÍVEL</Text>
                        <View style={estilos.estadoConta}>
                            <View style={estilos.pontoAtivo} />
                            <Text style={estilos.textoEstado}>{conta.status}</Text>
                        </View>
                    </View>

                    <Text style={estilos.saldo}>{formatarDinheiro(conta.saldo)}</Text>

                    <View style={estilos.dadosConta}>
                        <View>
                            <Text style={estilos.rotuloDado}>AGÊNCIA</Text>
                            <Text style={estilos.valorDado}>{conta.agencia}</Text>
                        </View>

                        <View style={estilos.separadorDado} />

                        <View>
                            <Text style={estilos.rotuloDado}>CONTA</Text>
                            <Text style={estilos.valorDado}>{conta.numero}</Text>
                        </View>
                    </View>
                </View>

                <View style={estilos.cabecalhoSecao}>
                    <View>
                        <Text style={estilos.etiquetaSecao}>ACESSO RÁPIDO</Text>
                        <Text style={estilos.tituloAcoes}>O que deseja fazer?</Text>
                    </View>
                    <View style={estilos.linhaSecao} />
                </View>

                <View style={estilos.acoes}>
                    <Pressable style={({ pressed }) => [estilos.acao, pressed && estilos.acaoPressionada]} onPress={() => {
                        roteador.push({
                            pathname: "/pix",
                            params: {
                                contaId: String(conta.id),
                                saldo: String(conta.saldo),
                                usuarioId: String(conta.usuarioId)
                            }
                        })
                    }}>
                        <View style={estilos.caixaIcone}>
                            <Ionicons name="paper-plane-outline" size={23} color="#c8b6ff" />
                        </View>
                        <Text style={estilos.textoAcao}>Pix</Text>
                        <Text style={estilos.descricaoAcao}>Envie em segundos</Text>
                    </Pressable>

                    <Pressable style={({ pressed }) => [estilos.acao, pressed && estilos.acaoPressionada]} onPress={() => {
                        roteador.push({
                            pathname: "/transferencia",
                            params: {
                                contaId: String(conta.id),
                                saldo: String(conta.saldo),
                                usuarioId: String(conta.usuarioId)
                            }
                        });
                    }}>
                        <View style={estilos.caixaIcone}>
                            <Ionicons name="swap-horizontal-outline" size={24} color="#c8b6ff" />
                        </View>
                        <Text style={estilos.textoAcao}>Transferir</Text>
                        <Text style={estilos.descricaoAcao}>Entre contas</Text>
                    </Pressable>

                    <Pressable style={({ pressed }) => [estilos.acao, pressed && estilos.acaoPressionada]} onPress={() => {
                        roteador.push({
                            pathname: "/extrato",
                            params: {
                                contaId: String(conta.id)
                            }
                        });
                    }}>
                        <View style={estilos.caixaIcone}>
                            <Ionicons name="receipt-outline" size={23} color="#c8b6ff" />
                        </View>
                        <Text style={estilos.textoAcao}>Extrato</Text>
                        <Text style={estilos.descricaoAcao}>Veja seu histórico</Text>
                    </Pressable>

                    <Pressable style={({ pressed }) => [estilos.acao, estilos.acaoDestaque, pressed && estilos.acaoPressionada]} onPress={() => {
                        roteador.push({
                            pathname: "/investimentos",
                            params: {
                                contaId: String(conta.id)
                            }
                        });
                    }}>
                        <View style={[estilos.caixaIcone, estilos.caixaIconeDestaque]}>
                            <Ionicons name="trending-up-outline" size={24} color="#92e3c4" />
                        </View>
                        <Text style={estilos.textoAcao}>Investir</Text>
                        <Text style={estilos.descricaoAcao}>Faça crescer</Text>
                    </Pressable>
                </View>

                <View style={estilos.rodapePainel}>
                    <View style={estilos.simboloRodape} />
                    <Text style={estilos.textoRodape}>Eclipse Bank · sua vida financeira em movimento</Text>
                </View>
            </ScrollView>
        </SafeAreaView>
    )
}

const estilos = StyleSheet.create({
  pagina: {
    flex: 1,
    backgroundColor: "#0c0d15"
  },

  conteudo: {
    paddingHorizontal: 22,
    paddingTop: 18,
    paddingBottom: 38
  },

  luzSuperior: {
    position: "absolute",
    top: -170,
    right: -120,
    width: 340,
    height: 340,
    borderRadius: 170,
    backgroundColor: "rgba(139, 92, 246, 0.13)"
  },

  centralizado: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    backgroundColor: "#0c0d15"
  },

  mensagem: {
    marginTop: 16,
    color: "#ded9e7",
    fontSize: 16
  },

  erro: {
    marginBottom: 20,
    color: "#ff8c8c",
    fontSize: 16,
    textAlign: "center"
  },

  cabecalho: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between"
  },

  identidade: {
    flexDirection: "row",
    alignItems: "center",
    gap: 11
  },

  simboloMarca: {
    width: 38,
    height: 38,
    alignItems: "center",
    justifyContent: "center",
    overflow: "hidden",
    borderWidth: 1,
    borderColor: "rgba(255, 255, 255, 0.2)",
    borderRadius: 19,
    backgroundColor: "#8b5cf6"
  },

  recorteMarca: {
    width: 30,
    height: 30,
    marginLeft: 15,
    borderRadius: 15,
    backgroundColor: "#17131f"
  },

  marca: {
    color: "#ffffff",
    fontSize: 12,
    fontWeight: "800",
    letterSpacing: 1.7
  },

  marcaDestaque: {
    color: "#b69cff"
  },

  botaoSair: {
    width: 42,
    height: 42,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: "rgba(182, 156, 255, 0.2)",
    borderRadius: 21,
    backgroundColor: "rgba(139, 92, 246, 0.08)"
  },

  pressionado: {
    opacity: 0.65,
    transform: [{ scale: 0.96 }]
  },

  apresentacao: {
    marginTop: 34,
    marginBottom: 23
  },

  etiqueta: {
    color: "#b69cff",
    fontSize: 10,
    fontWeight: "800",
    letterSpacing: 2.2
  },

  saudacao: {
    marginTop: 9,
    color: "#ffffff",
    fontSize: 29,
    fontWeight: "800",
    letterSpacing: -0.8
  },

  subtitulo: {
    marginTop: 7,
    color: "#85808d",
    fontSize: 14,
    lineHeight: 21
  },

  cartao: {
    position: "relative",
    minHeight: 225,
    overflow: "hidden",
    padding: 25,
    borderWidth: 1,
    borderColor: "rgba(182, 156, 255, 0.25)",
    borderRadius: 24,
    backgroundColor: "#191525",
    shadowColor: "#000000",
    shadowOffset: { width: 0, height: 18 },
    shadowOpacity: 0.32,
    shadowRadius: 25,
    elevation: 12
  },

  orbitaExterna: {
    position: "absolute",
    right: -72,
    bottom: -85,
    width: 210,
    height: 210,
    borderWidth: 1,
    borderColor: "rgba(182, 156, 255, 0.13)",
    borderRadius: 105
  },

  orbitaInterna: {
    position: "absolute",
    right: -28,
    bottom: -42,
    width: 125,
    height: 125,
    borderWidth: 20,
    borderColor: "rgba(139, 92, 246, 0.06)",
    borderRadius: 63
  },

  topoCartao: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between"
  },

  rotuloSaldo: {
    color: "#aaa4b1",
    fontSize: 10,
    fontWeight: "800",
    letterSpacing: 1.4
  },

  estadoConta: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: "rgba(118, 221, 183, 0.2)",
    borderRadius: 20,
    backgroundColor: "rgba(69, 201, 151, 0.08)"
  },

  pontoAtivo: {
    width: 5,
    height: 5,
    borderRadius: 3,
    backgroundColor: "#76ddb7"
  },

  textoEstado: {
    color: "#92e3c4",
    fontSize: 9,
    fontWeight: "800",
    letterSpacing: 0.8
  },

  saldo: {
    marginTop: 25,
    color: "#ffffff",
    fontSize: 38,
    fontWeight: "800",
    letterSpacing: -1.3
  },

  dadosConta: {
    flexDirection: "row",
    alignItems: "center",
    gap: 24,
    marginTop: 31
  },

  separadorDado: {
    width: 1,
    height: 28,
    backgroundColor: "rgba(255, 255, 255, 0.1)"
  },

  rotuloDado: {
    color: "#716b79",
    fontSize: 9,
    fontWeight: "700",
    letterSpacing: 1.2
  },

  valorDado: {
    marginTop: 5,
    color: "#ded9e7",
    fontSize: 14,
    fontWeight: "700"
  },

  cabecalhoSecao: {
    marginTop: 37,
    marginBottom: 17
  },

  etiquetaSecao: {
    color: "#8b5cf6",
    fontSize: 9,
    fontWeight: "800",
    letterSpacing: 1.8
  },

  tituloAcoes: {
    marginTop: 6,
    color: "#ffffff",
    fontSize: 21,
    fontWeight: "800",
    letterSpacing: -0.4
  },

  linhaSecao: {
    width: 42,
    height: 2,
    marginTop: 13,
    backgroundColor: "#8b5cf6"
  },

  acoes: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 12
  },

  acao: {
    width: "48%",
    minHeight: 150,
    justifyContent: "space-between",
    padding: 18,
    borderWidth: 1,
    borderColor: "rgba(255, 255, 255, 0.07)",
    borderRadius: 19,
    backgroundColor: "#12131d"
  },

  acaoDestaque: {
    borderColor: "rgba(118, 221, 183, 0.17)",
    backgroundColor: "#111b1a"
  },

  acaoPressionada: {
    opacity: 0.78,
    transform: [{ scale: 0.98 }]
  },

  caixaIcone: {
    width: 47,
    height: 47,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: "rgba(182, 156, 255, 0.18)",
    borderRadius: 15,
    backgroundColor: "rgba(139, 92, 246, 0.1)"
  },

  caixaIconeDestaque: {
    borderColor: "rgba(118, 221, 183, 0.18)",
    backgroundColor: "rgba(69, 201, 151, 0.09)"
  },

  textoAcao: {
    marginTop: 19,
    color: "#ffffff",
    fontSize: 16,
    fontWeight: "800"
  },

  descricaoAcao: {
    marginTop: 4,
    color: "#77717e",
    fontSize: 11,
    lineHeight: 16
  },

  rodapePainel: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 9,
    marginTop: 30,
    paddingTop: 22,
    borderTopWidth: 1,
    borderTopColor: "rgba(255, 255, 255, 0.06)"
  },

  simboloRodape: {
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: "#8b5cf6"
  },

  textoRodape: {
    color: "#5f5a66",
    fontSize: 10
  },

  botao: {
    minHeight: 52,
    paddingHorizontal: 24,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 10,
    backgroundColor: "#8b5cf6"
  },

  textoBotao: {
    color: "#ffffff",
    fontSize: 16,
    fontWeight: "800"
  }
});
