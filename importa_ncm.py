import pandas as pd
import psycopg2
from psycopg2.extras import execute_batch
import warnings

# Ignora avisos de formatação do Excel
warnings.filterwarnings('ignore')

print("Lendo o arquivo Excel...")
# Pula as 4 primeiras linhas de cabeçalho inútil da planilha da Gecex
df = pd.read_excel('Tabela_NCM_Vigente_20260906.xlsx', skiprows=4)
df.columns = ['Codigo', 'Descricao', 'DataInicio', 'DataFim', 'AtoIni', 'NumAto', 'AnoAto']

# Filtra APENAS NCMs válidos (Exatamente formato XXXX.XX.XX)
df = df[df['Codigo'].astype(str).str.match(r'^\d{4}\.\d{2}\.\d{2}$')]

# Remove os hífens iniciais das descrições (Ex: "-- Outros" vira "Outros")
df['Descricao'] = df['Descricao'].str.replace(r'^-+ ', '', regex=True)

# Pega apenas as colunas que importam
dados = df[['Codigo', 'Descricao']].values.tolist()
print(f"{len(dados)} NCMs válidos encontrados. Inserindo no banco de dados...")

# CONECTA NO SEU BANCO DE DADOS (Ajuste a senha se necessário)
conn = psycopg2.connect(dbname="brasil-saas", user="postgres", password="ALTERE_ME", host="localhost")
cursor = conn.cursor()

# Insere em lote de forma muito rápida. O "ON CONFLICT DO NOTHING" evita duplicidade se rodar 2x
query = "INSERT INTO tncm (codigo, descricao) VALUES (%s, %s) ON CONFLICT (codigo) DO NOTHING;"
execute_batch(cursor, query, dados)

conn.commit()
cursor.close()
conn.close()

print("Importação concluída com sucesso!")
