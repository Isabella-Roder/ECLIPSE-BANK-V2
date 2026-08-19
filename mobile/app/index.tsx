import { SafeAreaView, StyleSheet, Text, TextInput, Pressable, Alert, ScrollView, View } from "react-native";
import { useState } from "react";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { StatusBar } from "expo-status-bar";

import { API_URL } from "../config/api";
import { buscarTokenCsrf, obterTokenCsrf } from "@/config/csrf";

export default function Index() {

  const roteador = useRouter();

  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");

  const [carregando, setCarregando] = useState(false);

  async function entrar() {

    if (email.trim() === "" || senha.trim() === "") {
      Alert.alert("Atenção", "Preencha o e-mail e a senha.");
      return;
    }

    const dados = {
      email: email.trim(),
      senha: senha
    }

    try {
      setCarregando(true);

      await buscarTokenCsrf();

      const resposta = await fetch(`${API_URL}/usuarios/login`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": obterTokenCsrf() ?? ""
        },
        body: JSON.stringify(dados)
      });

      const textoResposta = await resposta.text();
      const corpo = textoResposta ? JSON.parse(textoResposta) : {};

      if (!resposta.ok) {
        const errosDosCampos = Object.values(corpo.campos || {});
        const mensagem = errosDosCampos[0] || corpo.mensagem || "Não foi possivel entrar.";

        throw new Error(String(mensagem));
      }

      roteador.replace({
        pathname: "/conta",
        params: {
          usuarioId: String(corpo.id)
        }
      });

    } catch (erro) {
      const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";
      Alert.alert("Erro ao entrar", mensagem);
    } finally {
      setCarregando(false);
    }
  }

  return (
    <SafeAreaView style={estilos.pagina}>
      <StatusBar style="light" />
      <View style={estilos.luzSuperior} />
      <View style={estilos.orbitaFundo} />

      <ScrollView contentContainerStyle={estilos.conteudo} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        <View style={estilos.identidade}>
          <View style={estilos.simboloMarca}><View style={estilos.recorteMarca} /></View>
          <Text style={estilos.marca}>ECLIPSE <Text style={estilos.marcaDestaque}>BANK</Text></Text>
        </View>

        <View style={estilos.apresentacao}>
          <Text style={estilos.etiqueta}>BEM-VINDA DE VOLTA</Text>
          <Text style={estilos.titulo}>Seu banco em uma nova órbita.</Text>
          <Text style={estilos.subtitulo}>Entre com seus dados para continuar cuidando dos seus planos.</Text>
        </View>

        <View style={estilos.formulario}>
          <Text style={estilos.tituloFormulario}>Acesse sua conta</Text>
          <Text style={estilos.textoFormulario}>Use o e-mail e a senha cadastrados.</Text>

          <Text style={estilos.rotulo}>E-mail</Text>
          <View style={estilos.caixaCampo}>
            <Ionicons name="mail-outline" size={20} color="#81798b" />
            <TextInput style={estilos.campo} placeholder="voce@exemplo.com" placeholderTextColor="#777383" keyboardType="email-address" autoCapitalize="none" value={email} onChangeText={setEmail} />
          </View>

          <Text style={estilos.rotuloSenha}>Senha</Text>
          <View style={estilos.caixaCampo}>
            <Ionicons name="lock-closed-outline" size={20} color="#81798b" />
            <TextInput style={estilos.campo} placeholder="Digite sua senha" placeholderTextColor="#777383" secureTextEntry value={senha} onChangeText={setSenha} />
          </View>

          <Pressable style={({ pressed }) => [estilos.botao, carregando && estilos.botaoDesabilitado, pressed && estilos.botaoPressionado]} onPress={entrar} disabled={carregando}>
            <Text style={estilos.textoBotao}>{carregando ? "Entrando..." : "Entrar na conta"}</Text>
            <Ionicons name="arrow-forward" size={21} color="#ffffff" />
          </Pressable>
        </View>

        <Text style={estilos.rodape}>SEGURANÇA E SIMPLICIDADE EM CADA MOVIMENTO</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  pagina: {
    flex: 1,
    backgroundColor: "#0c0d15"
  },
  conteudo: { flexGrow: 1, paddingHorizontal: 25, paddingTop: 28, paddingBottom: 34 },
  luzSuperior: { position: "absolute", top: -150, right: -130, width: 360, height: 360, borderRadius: 180, backgroundColor: "rgba(139,92,246,.16)" },
  orbitaFundo: { position: "absolute", top: 90, right: -170, width: 330, height: 330, borderWidth: 1, borderColor: "rgba(182,156,255,.08)", borderRadius: 165 },
  identidade: { flexDirection: "row", alignItems: "center", gap: 12 },
  simboloMarca: { width: 40, height: 40, alignItems: "center", justifyContent: "center", overflow: "hidden", borderWidth: 1, borderColor: "rgba(255,255,255,.2)", borderRadius: 20, backgroundColor: "#8b5cf6" },
  recorteMarca: { width: 32, height: 32, marginLeft: 16, borderRadius: 16, backgroundColor: "#17131f" },
  marca: { color: "#ffffff", fontSize: 12, fontWeight: "800", letterSpacing: 1.8 },
  marcaDestaque: { color: "#b69cff" },
  apresentacao: { marginTop: 54, marginBottom: 31 },
  etiqueta: { color: "#b69cff", fontSize: 9, fontWeight: "800", letterSpacing: 2 },
  titulo: { maxWidth: 340, marginTop: 12, color: "#ffffff", fontSize: 37, fontWeight: "800", lineHeight: 44, letterSpacing: -1.4 },
  subtitulo: { maxWidth: 330, marginTop: 14, color: "#918b99", fontSize: 15, lineHeight: 23 },
  formulario: { padding: 22, borderWidth: 1, borderColor: "rgba(255,255,255,.08)", borderRadius: 22, backgroundColor: "rgba(16,17,25,.96)", shadowColor: "#000", shadowOffset: { width: 0, height: 16 }, shadowOpacity: .26, shadowRadius: 25, elevation: 9 },
  tituloFormulario: { color: "#ffffff", fontSize: 19, fontWeight: "800" },
  textoFormulario: { marginTop: 5, marginBottom: 5, color: "#716b79", fontSize: 12 },
  rotulo: { marginTop: 20, marginBottom: 8, color: "#ded9e7", fontSize: 13, fontWeight: "700" },
  rotuloSenha: { marginTop: 18, marginBottom: 8, color: "#ded9e7", fontSize: 13, fontWeight: "700" },
  caixaCampo: { minHeight: 57, flexDirection: "row", alignItems: "center", gap: 10, paddingHorizontal: 15, borderWidth: 1, borderColor: "#292431", borderRadius: 12, backgroundColor: "#0d0e16" },
  campo: { flex: 1, minHeight: 55, color: "#ffffff", fontSize: 15 },
  botao: { minHeight: 58, flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 25, paddingHorizontal: 20, borderRadius: 13, backgroundColor: "#8b5cf6", shadowColor: "#8b5cf6", shadowOffset: { width: 0, height: 10 }, shadowOpacity: .25, shadowRadius: 18, elevation: 7 },
  textoBotao: { color: "#ffffff", fontSize: 15, fontWeight: "800" },
  botaoDesabilitado: { opacity: .6 },
  botaoPressionado: { opacity: .78, transform: [{ scale: .99 }] },
  rodape: { marginTop: 25, color: "#504b56", fontSize: 8, fontWeight: "800", letterSpacing: 1.4, textAlign: "center" }
});
