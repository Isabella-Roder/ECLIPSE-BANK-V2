import { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, SafeAreaView, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { StatusBar } from "expo-status-bar";

import { API_URL } from "@/config/api";

type Movimentacao = {
    id: number;
    codigo: string;
    contaId: number;
    tipo: string;
    status: string;
    valor: number;
    saldoResultante: number;
    descricao: string | null;
    criadaEm: string;
};

export default function ExtratoScreen() {
    const roteador = useRouter();

    const parametros = useLocalSearchParams<{ contaId: string}>();

    const [movimentacoes, setMovimentacoes] = useState<Movimentacao[]>([]);

    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState("");

    function formatarDinheiro(valor:number) {
        return Number(valor).toLocaleString("pt-BR", {
            style: "currency",
            currency: "BRL"
        });
    }

    function movimentacoesEhCredito(tipo:string) {
        return tipo === "DEPOSITO" ||
        tipo === "RESGATE_INVESTIMENTO" ||
        tipo.endsWith("_RECEBIDA");
    }

    function renderizarMovimentacao({
            item
        }: {
            item: Movimentacao;
        }) {
            const credito = movimentacoesEhCredito(item.tipo);
            const sinal = credito ? "+" : "-";

            const tipoFormatado = item.tipo.replaceAll("_", " ");

            const dataFormatada = new Date(item.criadaEm).toLocaleString("pt-BR");

            return (
                <View style={estilos.movimentacao}>
                    <View style={[estilos.iconeMovimentacao, credito ? estilos.iconeCredito : estilos.iconeDebito]}>
                        <Ionicons name={credito ? "arrow-down" : "arrow-up"} size={19} color={credito ? "#76ddb7" : "#ff93ad"} />
                    </View>
                    <View style={estilos.dadosMovimentacao}>
                        <View style={estilos.topoMovimentacao}>
                            <View style={estilos.textosMovimentacao}>
                                <Text style={estilos.tipoMovimentacao}>{tipoFormatado}</Text>
                                <Text style={estilos.descricao}>{item.descricao || "Sem descrição"}</Text>
                            </View>
                            <Text style={[estilos.valor, credito ? estilos.credito : estilos.debito]}>
                                {sinal} {formatarDinheiro(item.valor)}
                            </Text>
                        </View>

                        <View style={estilos.rodapeMovimentacao}>
                            <Text style={estilos.data}>{dataFormatada}</Text>
                            <Text style={estilos.saldoResultante}>
                                Saldo: {formatarDinheiro(item.saldoResultante)}
                            </Text>
                        </View>
                    </View>
                </View>
            );
        }
    useEffect(() => {

        async function carregarExtrato() {
            try {
                setCarregando(true);
                setErro("");

                const resposta = await fetch(`${API_URL}/contas/${parametros.contaId}/extrato`, {
                    credentials: "include"
                });

                const corpo = await resposta.json();

                if (!resposta.ok) {
                    throw new Error(corpo.mensagem || "Não foi possivel carregar extrato");
                }

                setMovimentacoes(corpo);
            } catch (erro) {
                const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";

                setErro(mensagem);
            } finally {
                setCarregando(false);
            }
        }

        if (parametros.contaId) {
            carregarExtrato();
        }
    }, [parametros.contaId]);

    if (carregando) {
        return (
            <SafeAreaView style={estilos.centralizado}>
                <ActivityIndicator size="large" color="#8b5cf6"/>
                <Text style={estilos.mensagem}>
                    Carregando extrato...
                </Text>
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

    return(
        <SafeAreaView style={estilos.pagina}>
            <StatusBar style="light" />
            <View style={estilos.luzSuperior} />
            <View style={estilos.cabecalho}>
                <Pressable style={estilos.botaoVoltar} onPress={() => roteador.back()} accessibilityLabel="Voltar">
                    <Ionicons name="arrow-back" size={21} color="#c8b6ff" />
                </Pressable>

                <View style={estilos.apresentacao}>
                    <Text style={estilos.etiqueta}>MOVIMENTAÇÕES</Text>
                    <Text style={estilos.titulo}>Seu extrato</Text>
                    <Text style={estilos.subtitulo}>Acompanhe cada movimento da sua conta.</Text>
                </View>
            </View>

            <FlatList
                data={movimentacoes}
                keyExtractor={(item) => String(item.id)}
                renderItem={renderizarMovimentacao}
                contentContainerStyle={estilos.lista}
                ItemSeparatorComponent={() => (
                    <View style={estilos.separador}/>
                )}
                ListEmptyComponent={
                    <Text style={estilos.listaVazia}>
                        Nenhuma movimentação encontrada.
                    </Text>
                }
            />
        </SafeAreaView>
    )
}

const estilos = StyleSheet.create({
  pagina: {
    flex: 1,
    paddingHorizontal: 24,
    paddingTop: 18,
    backgroundColor: "#0c0d15"
  },
  luzSuperior: { position: "absolute", top: -170, right: -120, width: 340, height: 340, borderRadius: 170, backgroundColor: "rgba(139,92,246,.13)" },
  cabecalho: { marginBottom: 25 },
  botaoVoltar: { width: 42, height: 42, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: "rgba(182,156,255,.2)", borderRadius: 21, backgroundColor: "rgba(139,92,246,.08)" },
  voltar: { color: "#b69cff", fontSize: 15, fontWeight: "700" },
  apresentacao: { marginTop: 31 },
  etiqueta: { color: "#b69cff", fontSize: 9, fontWeight: "800", letterSpacing: 1.8 },
  titulo: { marginTop: 8, color: "#ffffff", fontSize: 34, fontWeight: "800", letterSpacing: -1 },
  subtitulo: { marginTop: 8, color: "#85808d", fontSize: 14 },

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

lista: {
  paddingBottom: 35
},

movimentacao: {
  flexDirection: "row",
  alignItems: "flex-start",
  gap: 13,
  padding: 17,
  borderWidth: 1,
  borderColor: "rgba(255,255,255,.07)",
  borderRadius: 17,
  backgroundColor: "#12131d"
},

iconeMovimentacao: { width: 42, height: 42, alignItems: "center", justifyContent: "center", borderWidth: 1, borderRadius: 13 },
iconeCredito: { borderColor: "rgba(118,221,183,.18)", backgroundColor: "rgba(69,201,151,.09)" },
iconeDebito: { borderColor: "rgba(255,147,173,.18)", backgroundColor: "rgba(201,54,92,.08)" },

dadosMovimentacao: {
  flex: 1,
  gap: 12
},

topoMovimentacao: { flexDirection: "row", justifyContent: "space-between", gap: 10 },
textosMovimentacao: { flex: 1, gap: 4 },
rodapeMovimentacao: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: 8 },

tipoMovimentacao: {
  color: "#b69cff",
  fontSize: 9,
  fontWeight: "800",
  letterSpacing: .7
},

descricao: {
  color: "#ffffff",
  fontSize: 15,
  fontWeight: "700"
},

data: {
  color: "#797380",
  fontSize: 12
},

valor: {
  fontSize: 14,
  fontWeight: "800"
},

credito: {
  color: "#76ddb7"
},

debito: {
  color: "#ff93ad"
},

saldoResultante: {
  color: "#797380",
  fontSize: 11
},

separador: {
  height: 12
},

listaVazia: {
  marginTop: 50,
  color: "#97919f",
  fontSize: 15,
  textAlign: "center"
}
});
