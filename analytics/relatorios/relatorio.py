import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("dados/movimentacoes.csv")
df["CRIADA_EM"] = pd.to_datetime(df["CRIADA_EM"])

RECEITAS = ["DEPOSITO", "TRANSFERENCIA_RECEBIDA", "PIX_RECEBIDO", "RESGATE_INVESTIMENTO",
    "PROVENTO_FII", "RESGATE_META_FINANCEIRA", "EMPRESTIMO_LIBERADO"]

df["DIRECAO"] = df["TIPO"].apply(lambda tipo: "RECEITA" if tipo in RECEITAS else "DESPESA")

resumo = df.groupby(["USUARIO_ID", "USUARIO_NOME", "DIRECAO"])["VALOR"].sum().unstack(fill_value=0)

saldo_medio = df.groupby(["USUARIO_ID", "USUARIO_NOME"])["SALDO_RESULTANTE"].mean()

maiores = df.nlargest(5, "VALOR")[["USUARIO_NOME", "TIPO", "VALOR", "CRIADA_EM"]]

print(df.head())
print(df.dtypes)
print(df["DIRECAO"].value_counts())

print(resumo)

print(saldo_medio)
print(maiores)

despesas_por_tipo = df[df["DIRECAO"] == "DESPESA"].groupby("TIPO")["VALOR"].sum().sort_values(ascending=False)

despesas_por_tipo.plot(kind="bar")
plt.title("Total gasto por tipo de movimentação")
plt.ylabel("R$")
plt.tight_layout()
plt.savefig("grafico_despesas.png")

resumo_html = resumo.to_html()
maiores_html = maiores.to_html(index=False)

html = f"""
<html>
<head><meta charset="utf-8"><title>Relatório financeiro</title></head>
<body>
<h1>Relatório financeiro por usuário</h1>

<h2>Receitas e despesas por usuário</h2>
{resumo_html}

<h2>Maiores movimentações</h2>
{maiores_html}

<h2>Gastos por tipo</h2>
<img src="grafico_despesas.png" alt="Gráfico de despesas por tipo">
</body>
</html>
"""

with open("relatorio.html", "w", encoding="utf-8") as arquivo:
    arquivo.write(html)