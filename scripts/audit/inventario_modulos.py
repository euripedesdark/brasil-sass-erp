#!/usr/bin/env python3
"""Inventario reproduzivel de evidencias; nao calcula conclusao funcional ou cobertura."""
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODULES = {
    "fiscal": "Fiscal", "financeiro": "Financeiro", "cadastro": "Cadastro",
    "producao": "Producao", "compras": "Compras", "estoque": "Estoque",
    "vendas": "Vendas", "contabilidade": "Contabilidade", "wms": "WMS",
}
java = ROOT / "src/main/java/br/com/brasil_saas"
tests = ROOT / "src/test/java/br/com/brasil_saas"
sql = [(p, p.read_text(errors="replace").lower()) for p in
       sorted((ROOT / "src/main/resources/db").rglob("*.sql"))
       if re.match(r"[VB]\d+__", p.name)]
sha = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip()
print("# Evidencias dos modulos na main — 9 de outubro de 2026\n")
print(f"Base inspecionada: `{sha}`.\n")
print("Contagem de arquivos Java proprios por pacote; testes fora do pacote nao entram nesta coluna. "
      "Interfaces e implementacoes sao arquivos distintos. Referencia SQL nao prova que uma migration pode "
      "ser aplicada nem que uma integracao externa funciona. Nenhum numero representa percentual de conclusao.\n")
print("| Modulo | Java | Entidades | Controllers | Services anotados | Arquivos de teste no pacote | Migrations que referenciam tabelas |")
print("|---|---:|---:|---:|---:|---:|---:|")
details = []
for key, name in MODULES.items():
    paths = sorted((java / key).rglob("*.java"))
    contents = [(p, p.read_text()) for p in paths]
    tables = {t.lower() for _, s in contents for t in
              re.findall(r'@Table\s*\(\s*name\s*=\s*"([^"]+)"', s)}
    migrations = [p for p, s in sql if any(re.search(r"\b" + re.escape(t) + r"\b", s) for t in tables)]
    testpaths = sorted((tests / key).rglob("*Test.java"))
    counts = [len(paths), sum(bool(re.search(r"@Entity\b", s)) for _, s in contents),
              sum(bool(re.search(r"@(?:RestController|Controller)\b", s)) for _, s in contents),
              sum(bool(re.search(r"@Service\b", s)) for _, s in contents), len(testpaths), len(migrations)]
    print("| " + name + " | " + " | ".join(map(str, counts)) + " |")
    details.append((name, [p.relative_to(ROOT) for p in testpaths],
                    [p.relative_to(ROOT) for p in migrations]))
print("\nOs testes globais, contratos e pacotes transversais, como enterprise, podem validar estes modulos. "
      "Ausencia de teste dentro de um pacote nao comprova ausencia de testes ou funcionalidade.\n")
for index, (name, testpaths, migrations) in enumerate(details):
    print(f"## {name}\n")
    print("Testes encontrados no pacote:\n")
    if testpaths:
        for p in testpaths: print(f"- `{p}`")
    else: print("Sem arquivo de teste neste pacote; conferir testes globais e transversais.")
    print("\nMigrations com referencia a tabelas do modulo:\n")
    for p in migrations: print(f"- `{p}`")
    if not migrations: print("Sem referencia identificada pelo criterio textual.")
    if index < len(details) - 1: print()
