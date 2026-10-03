#!/usr/bin/env python3
"""
Brasil SaaS ERP — Conversor de tabelas oficiais para carga via COPY.

Entradas:
  - seed/ncm/Tabela_NCM_Vigente_*.xlsx   (colunas: codigo, descricao, dt_ini, dt_fim, ato, numero, ano)
  - seed/issqn/<UF>.xlsx ou <UF>.csv     (colunas: codigo_ibge, uf, nome_municipio,
                                           codigo_servico, incidencia, aliquota, dt_ini, dt_fim)

Saídas (seed/processed/):
  - ncm_oficial.csv        codigo;descricao
  - issqn_oficial.csv      codigo_servico;aliquota;cod_ibge;uf;municipio;vigencia;dt_fim
  - municipios_oficial.csv codigo_ibge;nome;uf

Decisões documentadas:
  * NCM: só itens de 8 dígitos (capítulos/posições/subposições são cabeçalho);
    pontos removidos p/ caber em bc_fis_ncm.codigo VARCHAR(8);
    linhas de cabeçalho repetido "Tabela NCM" ignoradas.
  * ISSQN: sem coluna descricao no oficial -> descricao resolvida no SQL via LC 116;
    incidencia ignorada (duplica codigo_servico); aliquota em PERCENTUAL (5 = 5%);
    dt_ini -> vigencia; linhas com dt_fim < hoje são descartadas (vigência expirada).
  * Municípios: distinct(codigo_ibge, nome_municipio, uf) do próprio ISSQN.
  * CEP: não processado aqui (preenchimento sob demanda via API ViaCEP, Fase 7).
"""

import csv
import re
import sys
import unicodedata
from pathlib import Path

import pandas as pd

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent
SEED_DIR = PROJECT_ROOT / "src" / "main" / "resources" / "db" / "seed"
OUT_DIR = SEED_DIR / "processed"

NCM_ITEM = re.compile(r"^\d{8}$")
DATA_ISO = re.compile(r"^\d{4}-\d{2}-\d{2}")
DATA_BR = re.compile(r"^(\d{2})/(\d{2})/(\d{4})")


def sem_acento(s: str) -> str:
    return unicodedata.normalize("NFKD", str(s)).encode("ascii", "ignore").decode()


def limpa_descricao(s) -> str:
    if s is None or (isinstance(s, float) and pd.isna(s)):
        return ""
    return re.sub(r"^[-–—\s\.]+", "", str(s).strip()).strip()


def so_digitos(s) -> str:
    if s is None or (isinstance(s, float) and pd.isna(s)):
        return ""
    return re.sub(r"\D", "", str(s))


def norm_codigo_ibge(v) -> str:
    """4300034 | 4300034.0 | '4300034' -> '4300034'"""
    if v is None or (isinstance(v, float) and pd.isna(v)):
        return ""
    if isinstance(v, float):
        return str(int(v))
    return so_digitos(v)


def norm_data(v) -> str:
    """'2026-01-01T00:00:00' | '01/01/2026' | NaT -> 'YYYY-MM-DD' ou ''"""
    if v is None or (isinstance(v, float) and pd.isna(v)):
        return ""
    t = str(v).strip()
    if not t or t.lower() in ("nat", "none", "nan"):
        return ""
    m = DATA_ISO.match(t)
    if m:
        return t[:10]
    m = DATA_BR.match(t)
    if m:
        return f"{m.group(3)}-{m.group(2)}-{m.group(1)}"
    return ""


def norm_aliquota(v) -> str:
    if v is None or (isinstance(v, float) and pd.isna(v)):
        return ""
    t = str(v).strip().replace("%", "").replace(",", ".")
    try:
        return f"{float(t):.2f}"
    except ValueError:
        return ""


# --------------------------------------------------------------------------- NCM
def processar_ncm() -> int:
    arquivos = sorted((SEED_DIR / "ncm").glob("Tabela_NCM_Vigente_*.xlsx"))
    if not arquivos:
        print("❌ Nenhum Tabela_NCM_Vigente_*.xlsx em", SEED_DIR / "ncm")
        return 0

    xlsx = arquivos[-1]
    print(f"📦 NCM: lendo {xlsx.name} (header=None)")
    df = pd.read_excel(xlsx, header=None, dtype=str)

    linhas, vistos = [], set()
    for _, row in df.iterrows():
        c0 = row.get(0)
        if c0 is None or pd.isna(c0):
            continue
        c0 = str(c0).strip()
        if not c0 or c0.lower().startswith("tabela"):      # cabeçalho repetido
            continue
        cod = so_digitos(c0)
        if not NCM_ITEM.match(cod):                        # capítulo/posição/subposição
            continue
        desc = limpa_descricao(row.get(1))
        if not desc or cod in vistos:
            continue
        vistos.add(cod)
        linhas.append((cod, desc))

    with open(OUT_DIR / "ncm_oficial.csv", "w", newline="", encoding="utf-8") as f:
        csv.writer(f, delimiter=";").writerows(linhas)

    print(f"✅ NCM: {len(linhas)} itens de 8 dígitos -> processed/ncm_oficial.csv")
    return len(linhas)


# -------------------------------------------------------------------------- ISSQN
def ler_issqn(caminho: Path):
    if caminho.suffix.lower() == ".xlsx":
        df = pd.read_excel(caminho, dtype=str)
    else:
        texto = None
        for enc in ("utf-8-sig", "utf-8", "latin-1", "cp1252"):
            try:
                texto = caminho.read_text(encoding=enc)
                break
            except (UnicodeDecodeError, OSError):
                continue
        if texto is None or not texto.strip():
            return None
        delim = ";" if ";" in texto.splitlines()[0] else ("," if "," in texto.splitlines()[0] else "\t")
        linhas = [ln for ln in csv.reader(texto.splitlines(), delimiter=delim) if ln and any(c.strip() for c in ln)]
        if not linhas:
            return None
        df = pd.DataFrame(linhas[1:], columns=linhas[0])
    df.columns = [sem_acento(c).lower().strip() for c in df.columns]
    return df


def processar_issqn():
    issqn_dir = SEED_DIR / "issqn"
    arquivos = sorted(list(issqn_dir.glob("*.xlsx")) + list(issqn_dir.glob("*.csv")))
    if not arquivos:
        print("❌ Nenhum arquivo de UF em", issqn_dir)
        return 0, 0

    out, municipios, total = [], {}, 0
    for caminho in arquivos:
        uf_arq = caminho.stem.upper()
        df = ler_issqn(caminho)
        if df is None or df.empty:
            print(f"  ⚠️  {uf_arq}: vazio/ilegível — pulado")
            continue
        faltam = [c for c in ("codigo_ibge", "codigo_servico") if c not in df.columns]
        if faltam:
            print(f"  ⚠️  {uf_arq}: colunas ausentes {faltam} — pulado")
            continue

        n = 0
        for _, r in df.iterrows():
            cod = str(r.get("codigo_servico") or "").strip()
            ibge = norm_codigo_ibge(r.get("codigo_ibge"))
            if not cod or not ibge:
                continue
            uf = str(r.get("uf") or uf_arq).strip().upper()[:2]
            mun = limpa_descricao(r.get("nome_municipio"))
            out.append((
                cod,
                norm_aliquota(r.get("aliquota")),
                ibge, uf, mun,
                norm_data(r.get("dt_ini")),
                norm_data(r.get("dt_fim")),
            ))
            if ibge not in municipios and mun:
                municipios[ibge] = (mun, uf)
            n += 1
        total += n
        print(f"  → {uf_arq}: {n} linhas")

    with open(OUT_DIR / "issqn_oficial.csv", "w", newline="", encoding="utf-8") as f:
        csv.writer(f, delimiter=";").writerows(out)

    with open(OUT_DIR / "municipios_oficial.csv", "w", newline="", encoding="utf-8") as f:
        csv.writer(f, delimiter=";").writerows(
            (ibge, nome, uf) for ibge, (nome, uf) in sorted(municipios.items())
        )

    print(f"✅ ISSQN: {total} linhas -> processed/issqn_oficial.csv")
    print(f"✅ Municípios: {len(municipios)} -> processed/municipios_oficial.csv")
    return total, len(municipios)


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    print("=" * 64)
    print("Brasil SaaS ERP — conversor de tabelas oficiais")
    print("=" * 64)
    n_ncm = processar_ncm()
    n_iss, n_mun = processar_issqn()
    print("-" * 64)
    print(f"Resumo: NCM={n_ncm} | ISSQN={n_iss} | Municípios={n_mun}")
    print("Próximo passo: psql -U postgres -d brasil_saas -v ON_ERROR_STOP=1 -f scripts/seed/carregar_oficiais.sql")
    if n_ncm == 0 and n_iss == 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
