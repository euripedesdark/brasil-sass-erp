import subprocess
import os

DB = "SYSFLUXO"
USER = "postgres"
OUT = "/home/euripedes/OneDrive/python/projetos-leno/SYSFLUXO-JAVA/bd"
PASSWORD = "ALTERE_ME"

tables = [
    "fcaixa", "fcentrocusto", "fcfo", "fcondicao", "fcusto", "fdatas", "fdia",
    "fdocumento", "fempresa", "fempresa_vinculo", "fextrato", "ffinanceiro",
    "ffuncionario", "fitem", "flan", "fmov", "fmovimento", "fmovimento_item",
    "fnota", "fnota_item", "fpessoa", "fproduto", "fproduto_ecommerce",
    "fproduto_fiscal", "fproduto_imagem", "fproduto_kit", "fproduto_movimento",
    "fproduto_variacao", "frecibo", "ftipo", "ftipopagamento", "fusuario",
    "fvenda", "fvenda_item", "gparametro", "tcnae_servico", "tissqn",
    "tmunicipio", "tncm", "tservico_lc116"
]

# Define a senha como variável de ambiente
os.environ["PGPASSWORD"] = PASSWORD

for t in tables:
    csv_path = f"{OUT}/{t}.csv"
    cmd = f"\\copy {t} TO '{csv_path}' CSV HEADER"
    print(f"Exportando {t} para {csv_path}...")
    subprocess.run(["psql", "-U", USER, "-d", DB, "-c", cmd])
