import { SafeAreaView, StyleSheet, Text, TextInput, Pressable, Alert } from "react-native";
import { useState } from "react";
import { useRouter } from "expo-router";

const API_URL = "http://192.168.0.104:8080/api";

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

      const resposta = await fetch(`${API_URL}/usuarios/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(dados)
      });

      const corpo = await resposta.json();

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
      <Text style={estilos.marca}>Eclipse Bank</Text>

      <Text style={estilos.titulo}>
        Seu banco em uma nova órbita.
      </Text>

      <Text style={estilos.subtitulo}>
        Entre com os dados cadastrados na sua conta.
      </Text>

      <Text style={estilos.rotulo}>E-mail</Text>

      <TextInput
        style={estilos.campo}
        placeholder="voce@exemplo.com"
        placeholderTextColor="#777383"
        keyboardType="email-address"
        autoCapitalize="none"
        value={email}
        onChangeText={setEmail}
      />

      <Text style={estilos.rotuloSenha}>Senha</Text>

      <TextInput
        style={estilos.campo}
        placeholder="Digite sua senha"
        placeholderTextColor="#777383"
        secureTextEntry
        value={senha}
        onChangeText={setSenha}
      />

      <Pressable style={[estilos.botao, carregando && estilos.botaoDesabilitado]} onPress={entrar} disabled={carregando}>
        <Text style={estilos.textoBotao}>{carregando ? "Entrando..." : "Entrar"}</Text>
      </Pressable>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  pagina: {
    flex: 1,
    paddingHorizontal: 28,
    paddingVertical: 24,
    backgroundColor: "#0c0d15"
  },

  marca: {
    color: "#b69cff",
    fontSize: 16,
    fontWeight: "800",
    letterSpacing: 3
  },

  titulo: {
    marginTop: 80,
    color: "#ffffff",
    fontSize: 30,
    fontWeight: "500",
    lineHeight: 44
  },

  subtitulo: {
    marginTop: 12,
    marginBottom: 36,
    color: "#97919f",
    fontSize: 16,
    lineHeight: 24
  },

  rotulo: {
    marginBottom: 8,
    color: "#ded9e7",
    fontSize: 14,
    fontWeight: "700"
  },

  campo: {
    minHeight: 56,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: "#292431",
    borderRadius: 10,
    color: "#ffffff",
    backgroundColor: "#12131d",
    fontSize: 16
  },

  rotuloSenha: {
    marginTop: 18,
    marginBottom: 8,
    color: "#ded9ef",
    fontSize: 14,
    fontWeight: "700"
  },

  botao: {
    minHeight: 56,
    marginTop: 24,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 10,
    backgroundColor: "#8b5cf6"
  },

  textoBotao: {
    color: "#ffffff",
    fontSize: 16,
    fontWeight: "800"
  },

  botaoDesabilitado: {
    opacity: 0.6
  }
});