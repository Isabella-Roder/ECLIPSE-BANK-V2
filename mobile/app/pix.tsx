import { Pressable, SafeAreaView, StyleSheet, Text, TextInput, Alert } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useState } from "react";

import { API_URL } from "@/config/api";
import { buscarTokenCsrf, obterTokenCsrf } from "@/config/csrf";

export default function PixScreen() {
    const roteador = useRouter();
    const parametros = useLocalSearchParams<{contaId: string, saldo: string, usuarioId: string}>();

    const [chave, setChave] = useState("");
    const [valor, setValor] = useState("");
    const [descricao, setDescricao] = useState("");
    const [carregando, setCarregando] = useState(false);

    async function enviarPix() {
        const valorNumerico = Number(valor.replace(",", "."));

        if (!chave.trim()) {
            Alert.alert("Atenção", "Informe a chave Pix.");
            return;
        }

        if (!Number.isFinite(valorNumerico) || valorNumerico <= 0) {
            Alert.alert("Atenção", "Informe um valor maior que zero.");
            return;
        }

        const dados = {
            chave: chave.trim(),
            valor: valorNumerico,
            descricao: descricao.trim() || null
        };

        try {
            setCarregando(true);

            await buscarTokenCsrf();

            const resposta = await fetch(`${API_URL}/contas/${parametros.contaId}/pix`, {
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
                const errosDosCampos = Object.values(corpo.campos || {});
                const mensagem = errosDosCampos[0] || corpo.mensagem || "Não foi possivel enviar o Pix.";

                throw new Error(String(mensagem));
            }

            roteador.replace({
                pathname: "/comprovante",
                params: {
                    usuarioId: parametros.usuarioId,
                    codigo: corpo.codigo
                }
            });
        } catch (erro) {
            const mensagem = erro instanceof Error ? erro.message : "Ocorreu um erro inesperado.";

            Alert.alert("Erro no Pix", mensagem);
        } finally {
            setCarregando(false);
        }
    }

    return(
        <SafeAreaView style={estilos.pagina}>
            <Pressable onPress={() => roteador.back()}>
                <Text style={estilos.voltar}>← Voltar</Text>
            </Pressable>

            <Text style={estilos.titulo}>Enviar Pix</Text>

            <Text style={estilos.saldo}>
                Saldo disponível: R$ {parametros.saldo}
            </Text>

            <Text style={estilos.rotulo}>Chave Pix</Text>

            <TextInput
                style={estilos.campo}
                placeholder="E-mail da conta de destino"
                placeholderTextColor="#777383"
                keyboardType="email-address"
                autoCapitalize="none"
                value={chave}
                onChangeText={setChave}
            />

            <Text style={estilos.rotulo}>Valor</Text>

            <TextInput
                style={estilos.campo}
                placeholder="0,00"
                placeholderTextColor="#777383"
                keyboardType="decimal-pad"
                value={valor}
                onChangeText={setValor}
            />

            <Text style={estilos.rotulo}>Descrição</Text>

            <TextInput
                style={[estilos.campo, estilos.campoDescricao]}
                placeholder="Ex.: pagamento do almoço"
                placeholderTextColor="#777383"
                maxLength={180}
                multiline
                value={descricao}
                onChangeText={setDescricao}
            />

            <Pressable style={[estilos.botao, carregando && estilos.botaoDesabilitado]} onPress={enviarPix} disabled={carregando}>
                <Text style={estilos.textoBotao}>
                    {carregando ? "Enviando..." : "Enviar Pix"}
                </Text>
            </Pressable>
        </SafeAreaView>
    );
}

const estilos = StyleSheet.create({
  pagina: {
    flex: 1,
    paddingHorizontal: 24,
    paddingVertical: 24,
    backgroundColor: "#0c0d15"
  },

  voltar: {
    color: "#b69cff",
    fontSize: 15,
    fontWeight: "700"
  },

  titulo: {
    marginTop: 28,
    color: "#ffffff",
    fontSize: 32,
    fontWeight: "800"
  },

  saldo: {
    marginTop: 12,
    color: "#97919f",
    fontSize: 16
  },

  rotulo: {
  marginTop: 24,
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

    campoDescricao: {
    minHeight: 90,
    paddingTop: 16,
    textAlignVertical: "top"
    },

    botao: {
    minHeight: 56,
    marginTop: 28,
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
