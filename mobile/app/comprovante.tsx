import { useEffect, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";

import { API_URL } from "@/config/api";

type Movimentacao = {
  codigo: string;
  tipo: string;
  valor: number;
  saldoResultante: number;
  descricao: string | null;
  criadaEm: string;
};

export default function ComprovanteScreen() {
  const roteador = useRouter();
  const parametros = useLocalSearchParams<{ codigo: string; usuarioId: string }>();
  const [movimentacao, setMovimentacao] = useState<Movimentacao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  useEffect(() => {
    async function carregar() {
      if (!parametros.codigo) {
        setErro("Código do comprovante não informado.");
        setCarregando(false);
        return;
      }

      try {
        const resposta = await fetch(
          `${API_URL}/movimentacoes/${parametros.codigo}`,
          { credentials: "include" }
        );

        const corpo = await resposta.json();

        if (!resposta.ok) {
          throw new Error(corpo.mensagem || "Não foi possível carregar o comprovante.");
        }

        setMovimentacao(corpo);
      } catch (erro) {
        setErro(erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.");
      } finally {
        setCarregando(false);
      }
    }

    carregar();
  }, [parametros.codigo]);

  const dinheiro = (valor: number) => Number(valor).toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL"
  });

  function voltar() {
    roteador.replace({
      pathname: "/conta",
      params: { usuarioId: parametros.usuarioId }
    });
  }

  if (carregando) {
    return (
      <SafeAreaView style={estilos.centralizado}>
        <ActivityIndicator size="large" color="#8b5cf6" />
        <Text style={estilos.mensagem}>Gerando comprovante...</Text>
      </SafeAreaView>
    );
  }

  if (erro || !movimentacao) {
    return (
      <SafeAreaView style={estilos.centralizado}>
        <Text style={estilos.tituloErro}>Comprovante indisponível</Text>
        <Text style={estilos.erro}>{erro}</Text>
        <Pressable style={estilos.botao} onPress={voltar}>
          <Text style={estilos.textoBotao}>Voltar para a conta</Text>
        </Pressable>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={estilos.pagina}>
      <ScrollView contentContainerStyle={estilos.conteudo}>
        <View style={estilos.cabecalho}>
          <View style={estilos.icone}><Text style={estilos.check}>✓</Text></View>
          <Text style={estilos.etiqueta}>
            {movimentacao.tipo.startsWith("PIX") ? "PIX REALIZADO" : "TRANSFERÊNCIA REALIZADA"}
          </Text>
          <Text style={estilos.titulo}>Operação concluída</Text>
          <Text style={estilos.valor}>{dinheiro(movimentacao.valor)}</Text>
        </View>

        <View style={estilos.cartao}>
          <Dado rotulo="Operação" valor={movimentacao.tipo.replaceAll("_", " ")} />
          <View style={estilos.divisor} />
          <Dado rotulo="Descrição" valor={movimentacao.descricao || "Sem descrição"} />
          <View style={estilos.divisor} />
          <Dado rotulo="Data e hora" valor={new Date(movimentacao.criadaEm).toLocaleString("pt-BR")} />
          <View style={estilos.divisor} />
          <Dado rotulo="Saldo após a operação" valor={dinheiro(movimentacao.saldoResultante)} destaque />
        </View>

        <View style={estilos.codigoBox}>
          <Text style={estilos.rotulo}>Código do comprovante</Text>
          <Text selectable style={estilos.codigo}>{movimentacao.codigo}</Text>
        </View>

        <Pressable style={estilos.botao} onPress={voltar}>
          <Text style={estilos.textoBotao}>Voltar para a conta</Text>
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

function Dado({ rotulo, valor, destaque = false }: { rotulo: string; valor: string; destaque?: boolean }) {
  return <View style={estilos.linha}>
    <Text style={estilos.rotulo}>{rotulo}</Text>
    <Text style={destaque ? estilos.destaque : estilos.dado}>{valor}</Text>
  </View>;
}

const estilos = StyleSheet.create({
  pagina: { flex: 1, backgroundColor: "#0c0d15" },
  conteudo: { paddingHorizontal: 24, paddingTop: 38, paddingBottom: 34 },
  centralizado: { flex: 1, alignItems: "center", justifyContent: "center", padding: 24, backgroundColor: "#0c0d15" },
  cabecalho: { alignItems: "center" },
  icone: { width: 66, height: 66, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: "#70d9b3", borderRadius: 33, backgroundColor: "#14251f" },
  check: { color: "#76ddb7", fontSize: 32, fontWeight: "800" },
  etiqueta: { marginTop: 22, color: "#b69cff", fontSize: 12, fontWeight: "800", letterSpacing: 2 },
  titulo: { marginTop: 8, color: "#ffffff", fontSize: 25, fontWeight: "800", textAlign: "center" },
  valor: { marginTop: 14, color: "#ffffff", fontSize: 38, fontWeight: "800" },
  cartao: { marginTop: 34, padding: 20, borderWidth: 1, borderColor: "#292431", borderRadius: 16, backgroundColor: "#12131d" },
  linha: { gap: 7 },
  rotulo: { color: "#797380", fontSize: 12 },
  dado: { color: "#ded9e7", fontSize: 15, fontWeight: "700" },
  destaque: { color: "#76ddb7", fontSize: 16, fontWeight: "800" },
  divisor: { height: 1, marginVertical: 16, backgroundColor: "#25212c" },
  codigoBox: { marginTop: 16, padding: 18, borderRadius: 14, backgroundColor: "#17131f" },
  codigo: { marginTop: 8, color: "#b69cff", fontSize: 13, lineHeight: 20 },
  botao: { minHeight: 56, marginTop: 24, alignItems: "center", justifyContent: "center", paddingHorizontal: 22, borderRadius: 10, backgroundColor: "#8b5cf6" },
  textoBotao: { color: "#ffffff", fontSize: 16, fontWeight: "800" },
  mensagem: { marginTop: 16, color: "#ded9e7", fontSize: 16 },
  tituloErro: { color: "#ffffff", fontSize: 22, fontWeight: "800" },
  erro: { marginTop: 10, color: "#ff8c8c", fontSize: 15, textAlign: "center" }
});
