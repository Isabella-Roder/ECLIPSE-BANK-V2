import { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, SafeAreaView, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";

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
        return tipo === "DEPOSITO" || tipo.endsWith("_RECEBIDA");
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
                    <View style={estilos.dadosMovimentacao}>
                        <Text style={estilos.tipoMovimentacao}>
                            {tipoFormatado}
                        </Text>

                        <Text style={estilos.descricao}>
                            {item.descricao || "Sem descrição"}
                        </Text>

                        <Text style={estilos.data}>
                            {dataFormatada}
                        </Text>

                        <View style={estilos.valoresMovimentacao}>
                            <Text style={[estilos.valor, credito ? estilos.credito : estilos.debito]}>
                                {sinal} {formatarDinheiro(item.valor)}
                            </Text>

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

                const resposta = await fetch(`${API_URL}/contas/${parametros.contaId}/extrato`);

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
            <View style={estilos.cabecalho}>
                <Pressable onPress={() => roteador.back()}>
                    <Text style={estilos.voltar}>← Voltar</Text>
                </Pressable>

                <Text style={estilos.titulo}>Extrato</Text>
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
    paddingVertical: 24,
    backgroundColor: "#0c0d15"
  },

  cabecalho: {
    gap: 16,
    marginBottom: 28
  },

  voltar: {
    color: "#b69cff",
    fontSize: 15,
    fontWeight: "700"
  },

  titulo: {
    color: "#ffffff",
    fontSize: 32,
    fontWeight: "800"
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

lista: {
  paddingBottom: 30
},

movimentacao: {
  flexDirection: "row",
  justifyContent: "space-between",
  gap: 18,
  padding: 18,
  borderWidth: 1,
  borderColor: "#292431",
  borderRadius: 14,
  backgroundColor: "#12131d"
},

dadosMovimentacao: {
  flex: 1,
  gap: 5
},

valoresMovimentacao: {
  alignItems: "flex-end",
  gap: 6
},

tipoMovimentacao: {
  color: "#b69cff",
  fontSize: 11,
  fontWeight: "800"
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
  fontSize: 15,
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