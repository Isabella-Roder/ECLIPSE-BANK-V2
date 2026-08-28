from fastapi import FastAPI
import subprocess
import pandas as pd
from fastapi.responses import HTMLResponse

app = FastAPI()

@app.get("/")
def raiz() :
    return {"status": "ok"}

CAMINHO_H2_JAR = "/home/isabella/.m2/repository/com/h2database/h2/2.4.240/h2-2.4.240.jar"
URL_BANCO = "jdbc:h2:file:/home/isabella/Projetos/ECLIPSE-BANK-V2/backend/data/eclipsebank;AUTO_SERVER=TRUE"

def exportar_dados() :
    with open("export.sql") as arquvo :
        sql = arquvo.read()

    subprocess.run([
        "java", "-cp", CAMINHO_H2_JAR, "org.h2.tools.Shell",
        "-url", URL_BANCO, "-user", "sa", "-password", "",
        "-sql", sql
    ], check=True)

RECEITAS = ["DEPOSITO", "TRANSFERENCIA_RECEBIDA", "PIX_RECEBIDO", "RESGATE_INVESTIMENTO", 
    "PROVENTO_FII", "RESGATE_META_FINANCEIRA", "EMPRESTIMO_LIBERADO"]

@app.get("/relatorio", response_class=HTMLResponse)
def gerar_relatorio() :
    exportar_dados()

    df = pd.read_csv("dados/movimentacoes.csv")
    df["DIRECAO"] = df["TIPO"].apply(lambda tipo: "RECEITA" if tipo in RECEITAS else "DESPESA")

    resumo = df.groupby(["USUARIO_ID", "USUARIO_NOME", "DIRECAO"])["VALOR"].sum().unstack(fill_value=0)
    maiores = df.nlargest(5, "VALOR")[["USUARIO_NOME", "TIPO", "VALOR", "CRIADA_EM"]]

    return f"""
    <html><body>
    <h1>Relatório financeiro por usuário</h1>
    <h2>Receitas e despesas</h2>
    {resumo.to_html()}
    <h2>Maiores movimentações</h2>
    {maiores.to_html(index=False)}
    </body></html>
    """