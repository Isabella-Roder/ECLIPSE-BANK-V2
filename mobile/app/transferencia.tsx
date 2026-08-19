import { useState } from "react";
import { Alert, Pressable, SafeAreaView, ScrollView, StyleSheet, Text, TextInput } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";

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
      <ScrollView contentContainerStyle={estilos.conteudo} keyboardShouldPersistTaps="handled">
        <Pressable onPress={() => roteador.back()}>
          <Text style={estilos.voltar}>← Voltar</Text>
        </Pressable>

        <Text style={estilos.titulo}>Transferir</Text>
        <Text style={estilos.saldo}>Saldo disponível: R$ {parametros.saldo}</Text>

        <Text style={estilos.rotulo}>Agência de destino</Text>
        <TextInput style={estilos.campo} placeholder="0001" placeholderTextColor="#777383" keyboardType="number-pad" maxLength={4} value={agencia} onChangeText={setAgencia} />

        <Text style={estilos.rotulo}>Conta de destino</Text>
        <TextInput style={estilos.campo} placeholder="Número da conta" placeholderTextColor="#777383" keyboardType="number-pad" maxLength={12} value={numero} onChangeText={setNumero} />

        <Text style={estilos.rotulo}>Valor</Text>
        <TextInput style={estilos.campo} placeholder="0,00" placeholderTextColor="#777383" keyboardType="decimal-pad" value={valor} onChangeText={setValor} />

        <Text style={estilos.rotulo}>Descrição</Text>
        <TextInput style={[estilos.campo, estilos.descricao]} placeholder="Ex.: pagamento" placeholderTextColor="#777383" multiline maxLength={180} value={descricao} onChangeText={setDescricao} />

        <Pressable style={[estilos.botao, carregando && estilos.desabilitado]} onPress={transferir} disabled={carregando}>
          <Text style={estilos.textoBotao}>{carregando ? "Transferindo..." : "Confirmar transferência"}</Text>
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  pagina: { flex: 1, backgroundColor: "#0c0d15" },
  conteudo: { paddingHorizontal: 24, paddingVertical: 24, paddingBottom: 40 },
  voltar: { color: "#b69cff", fontSize: 15, fontWeight: "700" },
  titulo: { marginTop: 28, color: "#ffffff", fontSize: 32, fontWeight: "800" },
  saldo: { marginTop: 12, color: "#97919f", fontSize: 16 },
  rotulo: { marginTop: 22, marginBottom: 8, color: "#ded9e7", fontSize: 14, fontWeight: "700" },
  campo: { minHeight: 56, paddingHorizontal: 16, borderWidth: 1, borderColor: "#292431", borderRadius: 10, color: "#ffffff", backgroundColor: "#12131d", fontSize: 16 },
  descricao: { minHeight: 88, paddingTop: 16, textAlignVertical: "top" },
  botao: { minHeight: 56, marginTop: 28, alignItems: "center", justifyContent: "center", borderRadius: 10, backgroundColor: "#8b5cf6" },
  textoBotao: { color: "#ffffff", fontSize: 16, fontWeight: "800" },
  desabilitado: { opacity: 0.6 }
});
