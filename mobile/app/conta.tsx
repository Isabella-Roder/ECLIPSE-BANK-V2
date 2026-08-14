import { useEffect, useState } from "react";
import { ActivityIndicator, Pressable, SafeAreaView, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";

import { API_URL } from "../config/api";

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

            let resposta = await fetch(`${API_URL}/contas/usuario/${parametros.usuarioId}`);

            if (resposta.status === 404) {
                resposta = await fetch(`${API_URL}/contas/usuario/${parametros.usuarioId}`, {
                    method: "POST"
                });
            }

            const corpo = await resposta.json();

            if (!resposta.ok) {
                throw new Error(corpo.mensagem || "Não foi possivel carregar a conta.");
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
            <View style={estilos.cabecalho}>
                <View>
                    <Text style={estilos.marca}>ECLIPSE BANK</Text>
                    <Text style={estilos.saudacao}>
                        Olá, {conta.titular}
                    </Text>
                </View>

                <Pressable onPress={sair}>
                    <Text style={estilos.sair}>Sair</Text>
                </Pressable>
            </View>

            <View style={estilos.cartao}>
                <Text style={estilos.rotuloSaldo}>Saldo disponível</Text>

                <Text style={estilos.saldo}>{formatarDinheiro(conta.saldo)}</Text>

                <View style={estilos.dadosConta}>
                    <View>
                        <Text style={estilos.rotuloDado}>Agência</Text>
                        <Text style={estilos.valorDado}>{conta.agencia}</Text>
                    </View>

                    <View>
                        <Text style={estilos.rotuloDado}>Conta</Text>
                        <Text style={estilos.valorDado}>{conta.numero}</Text>
                    </View>
                </View>
            </View>

            <Text style={estilos.tituloAcoes}>O que deseja fazer?</Text>

            <View style={estilos.acoes}>
                <Pressable style={estilos.acao} onPress={() => {
                    roteador.push({
                        pathname: "/pix",
                        params: {
                            contaId: String(conta.id),
                            saldo: String(conta.saldo),
                            usuarioId: String(conta.usuarioId)
                        }
                    })
                }}>
                    <Text style={estilos.icone}>↗</Text>
                    <Text style={estilos.textoAcao}>Pix</Text>
                </Pressable>

                <Pressable style={estilos.acao} onPress={() => {
                    roteador.push({
                        pathname: "/transferencia",
                        params: {
                            contaId: String(conta.id),
                            saldo: String(conta.saldo),
                            usuarioId: String(conta.usuarioId)
                        }
                    });
                }}>
                    <Text style={estilos.icone}>⇄</Text>
                    <Text style={estilos.textoAcao}>Transferir</Text>
                </Pressable>

                <Pressable style={estilos.acao} onPress={() => {
                    roteador.push({
                        pathname: "/extrato",
                        params: {
                            contaId: String(conta.id)
                        }
                    });
                }}>
                    <Text style={estilos.icone}>▤</Text>
                    <Text style={estilos.textoAcao}>Extrato</Text>
                </Pressable>
            </View>
        </SafeAreaView>
    )
}

const estilos = StyleSheet.create({
  pagina: {
    flex: 1,
    paddingHorizontal: 24,
    paddingVertical: 24,
    backgroundColor: "#0c0d15"
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

  marca: {
    color: "#b69cff",
    fontSize: 13,
    fontWeight: "800",
    letterSpacing: 2
  },

  saudacao: {
    marginTop: 8,
    color: "#ffffff",
    fontSize: 22,
    fontWeight: "700"
  },

  sair: {
    color: "#b69cff",
    fontSize: 15,
    fontWeight: "700"
  },

  cartao: {
    marginTop: 36,
    padding: 24,
    borderWidth: 1,
    borderColor: "#34294b",
    borderRadius: 18,
    backgroundColor: "#17131f"
  },

  rotuloSaldo: {
    color: "#97919f",
    fontSize: 14
  },

  saldo: {
    marginTop: 8,
    color: "#ffffff",
    fontSize: 34,
    fontWeight: "800"
  },

  dadosConta: {
    flexDirection: "row",
    gap: 40,
    marginTop: 32
  },

  rotuloDado: {
    color: "#797380",
    fontSize: 12
  },

  valorDado: {
    marginTop: 4,
    color: "#ded9e7",
    fontSize: 15,
    fontWeight: "700"
  },

  tituloAcoes: {
    marginTop: 36,
    marginBottom: 16,
    color: "#ffffff",
    fontSize: 18,
    fontWeight: "700"
  },

  acoes: {
    flexDirection: "row",
    gap: 12
  },

  acao: {
    flex: 1,
    minHeight: 96,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: "#292431",
    borderRadius: 14,
    backgroundColor: "#12131d"
  },

  icone: {
    color: "#b69cff",
    fontSize: 24,
    fontWeight: "700"
  },

  textoAcao: {
    marginTop: 8,
    color: "#ffffff",
    fontSize: 13,
    fontWeight: "700"
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
