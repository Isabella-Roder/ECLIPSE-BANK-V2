import { useState } from "react";
import { Alert, Pressable, SafeAreaView, ScrollView, StyleSheet, Text, TextInput, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { StatusBar } from "expo-status-bar";

import { API_URL } from "@/config/api";
import { buscarTokenCsrf, obterTokenCsrf } from "@/config/csrf";

export default function TransferenciaScreen() {
  const roteador = useRouter();
  const parametros = useLocalSearchParams<{ contaId: string; usuarioId: string; saldo: string }>();
  const [agencia, setAgencia] = useState("");
  const [numero, setNumero] = useState("");
  const [valor, setValor] = useState("");
  const [descricao, setDescricao] = useState("");
  const [carregando, setCarregando] = useState(false);

  async function transferir() {
    const valorNumerico = Number(valor.replace(",", "."));

    if (!agencia.trim() || !numero.trim()) {
      Alert.alert("Atenção", "Informe a agência e a conta de destino.");
      return;
    }

    if (!Number.isFinite(valorNumerico) || valorNumerico <= 0) {
      Alert.alert("Atenção", "Informe um valor maior que zero.");
      return;
    }

    try {
      setCarregando(true);

      await buscarTokenCsrf();

      const resposta = await fetch(`${API_URL}/contas/${parametros.contaId}/transferencias`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": obterTokenCsrf() ?? ""
        },
        body: JSON.stringify({
          agenciaDestino: agencia.trim(),
          numeroDestino: numero.trim(),
          valor: valorNumerico,
          descricao: descricao.trim() || null
        })
      });

      const corpo = await resposta.json();

      if (!resposta.ok) {
        const erros = Object.values(corpo.campos || {});
        throw new Error(String(erros[0] || corpo.mensagem || "Não foi possível transferir."));
      }

      roteador.replace({
        pathname: "/comprovante",
        params: { codigo: corpo.codigo, usuarioId: parametros.usuarioId }
      });
    } catch (erro) {
      Alert.alert("Erro na transferência", erro instanceof Error ? erro.message : "Erro inesperado.");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <SafeAreaView style={estilos.pagina}>
      <StatusBar style="light" />
      <View style={estilos.luzSuperior} />

      <ScrollView contentContainerStyle={estilos.conteudo} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        <View style={estilos.cabecalho}>
          <Pressable style={estilos.botaoVoltar} onPress={() => roteador.back()} accessibilityLabel="Voltar">
            <Ionicons name="arrow-back" size={21} color="#c8b6ff" />
          </Pressable>

          <View style={estilos.identidade}>
            <View style={estilos.simboloMarca}><View style={estilos.recorteMarca} /></View>
            <Text style={estilos.marca}>ECLIPSE <Text style={estilos.marcaDestaque}>BANK</Text></Text>
          </View>
        </View>

        <View style={estilos.apresentacao}>
          <View style={estilos.iconeOperacao}>
            <Ionicons name="swap-horizontal-outline" size={28} color="#c8b6ff" />
          </View>
          <Text style={estilos.etiqueta}>TRANSFERÊNCIA BANCÁRIA</Text>
          <Text style={estilos.titulo}>Transferir</Text>
          <Text style={estilos.subtitulo}>Envie dinheiro para outra conta Eclipse Bank.</Text>
        </View>

        <View style={estilos.cartaoSaldo}>
          <View>
            <Text style={estilos.rotuloSaldo}>SALDO DISPONÍVEL</Text>
            <Text style={estilos.saldo}>R$ {parametros.saldo}</Text>
          </View>
          <Ionicons name="wallet-outline" size={25} color="#8f8699" />
        </View>

        <View style={estilos.formulario}>
          <View style={estilos.cabecalhoFormulario}>
            <Text style={estilos.etiquetaFormulario}>CONTA DE DESTINO</Text>
            <Text style={estilos.passo}>01</Text>
          </View>

          <View style={estilos.linhaCampos}>
            <View style={estilos.grupoAgencia}>
              <Text style={estilos.rotulo}>Agência</Text>
              <View style={estilos.caixaCampo}>
                <Ionicons name="business-outline" size={19} color="#81798b" />
                <TextInput style={estilos.campo} placeholder="0001" placeholderTextColor="#777383" keyboardType="number-pad" maxLength={4} value={agencia} onChangeText={setAgencia} />
              </View>
            </View>

            <View style={estilos.grupoConta}>
              <Text style={estilos.rotulo}>Conta</Text>
              <View style={estilos.caixaCampo}>
                <Ionicons name="card-outline" size={19} color="#81798b" />
                <TextInput style={estilos.campo} placeholder="Número" placeholderTextColor="#777383" keyboardType="number-pad" maxLength={12} value={numero} onChangeText={setNumero} />
              </View>
            </View>
          </View>

          <Text style={estilos.rotulo}>Valor</Text>
          <View style={estilos.caixaCampo}>
            <Text style={estilos.prefixo}>R$</Text>
            <TextInput style={[estilos.campo, estilos.campoValor]} placeholder="0,00" placeholderTextColor="#777383" keyboardType="decimal-pad" value={valor} onChangeText={setValor} />
          </View>

          <Text style={estilos.rotulo}>Descrição <Text style={estilos.opcional}>· opcional</Text></Text>
          <View style={[estilos.caixaCampo, estilos.caixaDescricao]}>
            <Ionicons name="create-outline" size={20} color="#81798b" style={estilos.iconeDescricao} />
            <TextInput style={[estilos.campo, estilos.descricao]} placeholder="Ex.: pagamento" placeholderTextColor="#777383" multiline maxLength={180} value={descricao} onChangeText={setDescricao} />
          </View>

          <Pressable style={({ pressed }) => [estilos.botao, carregando && estilos.desabilitado, pressed && estilos.botaoPressionado]} onPress={transferir} disabled={carregando}>
            <Text style={estilos.textoBotao}>{carregando ? "Transferindo..." : "Continuar transferência"}</Text>
            <Ionicons name="arrow-forward" size={21} color="#ffffff" />
          </Pressable>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  pagina: { flex: 1, backgroundColor: "#0c0d15" },
  conteudo: { paddingHorizontal: 22, paddingTop: 18, paddingBottom: 42 },
  luzSuperior: { position: "absolute", top: -170, right: -120, width: 340, height: 340, borderRadius: 170, backgroundColor: "rgba(139,92,246,.13)" },
  cabecalho: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  botaoVoltar: { width: 42, height: 42, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: "rgba(182,156,255,.2)", borderRadius: 21, backgroundColor: "rgba(139,92,246,.08)" },
  identidade: { flexDirection: "row", alignItems: "center", gap: 9 },
  simboloMarca: { width: 31, height: 31, alignItems: "center", justifyContent: "center", overflow: "hidden", borderRadius: 16, backgroundColor: "#8b5cf6" },
  recorteMarca: { width: 25, height: 25, marginLeft: 13, borderRadius: 13, backgroundColor: "#17131f" },
  marca: { color: "#ffffff", fontSize: 10, fontWeight: "800", letterSpacing: 1.4 },
  marcaDestaque: { color: "#b69cff" },
  apresentacao: { marginTop: 34 },
  iconeOperacao: { width: 54, height: 54, alignItems: "center", justifyContent: "center", marginBottom: 20, borderWidth: 1, borderColor: "rgba(182,156,255,.2)", borderRadius: 17, backgroundColor: "rgba(139,92,246,.1)" },
  etiqueta: { color: "#b69cff", fontSize: 9, fontWeight: "800", letterSpacing: 1.8 },
  titulo: { marginTop: 8, color: "#ffffff", fontSize: 34, fontWeight: "800", letterSpacing: -1 },
  subtitulo: { maxWidth: 310, marginTop: 9, color: "#85808d", fontSize: 14, lineHeight: 21 },
  cartaoSaldo: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 25, padding: 18, borderWidth: 1, borderColor: "rgba(255,255,255,.07)", borderRadius: 16, backgroundColor: "#12131d" },
  rotuloSaldo: { color: "#77717e", fontSize: 9, fontWeight: "800", letterSpacing: 1.2 },
  saldo: { marginTop: 5, color: "#ffffff", fontSize: 20, fontWeight: "800" },
  formulario: { marginTop: 17, padding: 21, borderWidth: 1, borderColor: "rgba(255,255,255,.07)", borderRadius: 21, backgroundColor: "#101119" },
  cabecalhoFormulario: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: 3 },
  etiquetaFormulario: { color: "#8b5cf6", fontSize: 9, fontWeight: "800", letterSpacing: 1.6 },
  passo: { color: "#4f4a55", fontSize: 11, fontWeight: "800" },
  linhaCampos: { flexDirection: "row", gap: 11 },
  grupoAgencia: { width: "37%" },
  grupoConta: { flex: 1 },
  rotulo: { marginTop: 20, marginBottom: 8, color: "#ded9e7", fontSize: 13, fontWeight: "700" },
  opcional: { color: "#716b79", fontSize: 11, fontWeight: "500" },
  caixaCampo: { minHeight: 57, flexDirection: "row", alignItems: "center", gap: 9, paddingHorizontal: 14, borderWidth: 1, borderColor: "#292431", borderRadius: 12, backgroundColor: "#0d0e16" },
  campo: { flex: 1, minWidth: 0, minHeight: 55, color: "#ffffff", fontSize: 15 },
  prefixo: { color: "#b69cff", fontSize: 13, fontWeight: "800" },
  campoValor: { fontSize: 19, fontWeight: "700" },
  caixaDescricao: { minHeight: 96, alignItems: "flex-start" },
  iconeDescricao: { marginTop: 17 },
  descricao: { minHeight: 94, paddingTop: 16, textAlignVertical: "top" },
  botao: { minHeight: 58, flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 26, paddingHorizontal: 20, borderRadius: 13, backgroundColor: "#8b5cf6", shadowColor: "#8b5cf6", shadowOffset: { width: 0, height: 10 }, shadowOpacity: .25, shadowRadius: 18, elevation: 7 },
  textoBotao: { color: "#ffffff", fontSize: 15, fontWeight: "800" },
  desabilitado: { opacity: .6 },
  botaoPressionado: { opacity: .78, transform: [{ scale: .99 }] }
});
