#!/usr/bin/env python3
"""
Le tabela de PDF que so existe como imagem.

Motivo: a documentacao do AGILIBlue traz, nas secoes 4 e 5 do manual da Nota
Tecnica SE/CGNFS-e 007/2026, as tabelas de valores validos de
`CodigoSituacaoTributaria` e `CodigoTipoRetencao` **inteiramente como imagem**.
A extracao de texto nao pega. Mas o PDF tem as palavras posicionadas, e o
posicionamento e' a informacao que o texto corrido perde.

O que o script faz, e por que cada parte e' necessaria:

1. Renderiza a pagina em 400 DPI, em tons de cinza. A 300 DPI o `tesseract`
   le a coluna de codigos com erro; em cinza o antialias do PDF some e o
   contraste do texto preto sobre a grade da tabela improves.
2. Pede o TSV do tesseract, que traz a caixa de cada palavra. E' isso que
   permite separar colunas.
3. Agrupa as palavras por linha, pela coordenada vertical.
4. Separa as colunas por um vao na coordenada horizontal. A tabela do AGILIBlue
   tem **duas colunas de codigo** — a negativa e a positiva — e o `--psm 6`
   achata as duas em uma linha só, perdendo a negativa. Sem os caixas, nao ha
   como reconstituir isso.
5. Junta o codigo com a descricao na mesma linha.

Uso:
    scripts/ocr-tabela-pdf.py <pdf> <primeira-pagina> <ultima-pagina> [coluna-x]

Argumentos:
    pdf             caminho do PDF
    1 14            paginas, intervalo
    180             (opcional) x, em pixel, onde a coluna de codigo acaba.
                    Sem este valor o script detecta o vao sozinho.
"""

import subprocess
import sys
import csv
import os
import tempfile
import shutil
from collections import defaultdict


def renderizar(pdf, pagina, dpi, cinza=True):
    """A pagina vira PNG em alta resolucao."""
    flags = ["-png", "-r", str(dpi)]
    if cinza:
        flags.append("-gray")
    with tempfile.TemporaryDirectory() as tmp:
        antes = set(os.listdir(tmp))
        subprocess.run(
            ["pdftoppm", "-q", "-f", str(pagina), "-l", str(pagina)] + flags
            + [pdf, os.path.join(tmp, "pg")],
            check=True,
        )
        novos = [f for f in os.listdir(tmp) if f not in antes]
        if not novos:
            raise SystemExit("pdftoppm nao gerou nada para a pagina %d" % pagina)
        destino = os.path.join(tmp, "pagina.png")
        shutil.move(os.path.join(tmp, novos[0]), destino)
        # copia para fora antes do TemporaryDirectory sumir
        saida = tempfile.mktemp(suffix=".png")
        shutil.copy(destino, saida)
        return saida


def tesseract_tsv(png, lang="por"):
    """O TSV: uma linha por palavra, com a caixa."""
    base = png[:-4] if png.endswith(".png") else png
    subprocess.run(
        [
            "tesseract", png, base, "-l", lang,
            "--oem", "1",          # LSTM: bem melhor em documento digitalizado
            "--psm", "6",          # bloco uniforme: e' uma tabela
            "-c", "tessedit_create_tsv=1",
            "-c", "preserve_interword_spaces=1",
        ],
        check=True,
        capture_output=True,
    )
    return base + ".tsv"


def palavras(tsv):
    """(texto, left, top, width, conf), na ordem de leitura."""
    with open(tsv, encoding="utf-8") as f:
        for linha in csv.DictReader(f, delimiter="\t", quoting=csv.QUOTE_NONE):
            txt = (linha.get("text") or "").strip()
            if not txt:
                continue
            try:
                conf = float(linha.get("conf") or -1)
                left = int(linha["left"])
                top = int(linha["top"])
                width = int(linha["width"])
            except (ValueError, KeyError):
                continue
            yield txt, left, top, width, conf


def linhas(ws, tol=12):
    """Agrupa por linha, pela coordenada vertical."""
    por_linha = defaultdict(list)
    for w in ws:
        por_linha[w[2] // tol].append(w)
    for chave in sorted(por_linha):
        yield sorted(por_linha[chave], key=lambda w: w[1])


def vao_das_colunas(linha, min_vao=60):
    """
    Onde ha separacao entre colunas, em x.

    A coluna do codigo e' estreita e o texto comeca mais a direita. O vao e'
    grande em relacao ao espaco entre palavras dentro de uma frase, entao o
    limiar separa coluna de palavra.
    """
    posicoes = [(w[1], w[1] + w[3], w[0]) for w in linha if w[4] >= 30]
    if len(posicoes) < 2:
        return []
    posicoes.sort()
    vaos = []
    for (e1, d1, _), (e2, _, _) in zip(posicoes, posicoes[1:]):
        if e2 - d1 >= min_vao:
            vaos.append((d1 + e2) // 2)
    return vaos


def reconstruir(linha, cortes):
    """
    Devolve a linha como colunas, usando os cortes em x.

    Sem cortes, devolve uma coluna so — que e' o que o `--psm 6` do tesseract
    entrega, e o que perde a coluna de codigo negativa.
    """
    if not cortes:
        return [" ".join(w[0] for w in linha)]
    colunas = [[] for _ in range(len(cortes) + 1)]
    for w in linha:
        if w[4] < 30:
            continue
        centro = w[1] + w[3] // 2
        i = 0
        while i < len(cortes) and centro > cortes[i]:
            i += 1
        colunas[i].append(w[0])
    return [" ".join(c) for c in colunas]


def principal(pdf, primeira, ultima, corte_x=None, dpi=400, lang="por"):
    for pagina in range(primeira, ultima + 1):
        png = renderizar(pdf, pagina, dpi)
        try:
            ws = list(palavras(tesseract_tsv(png, lang)))
            print("=== pagina %d ===" % pagina)
            for linha in linhas(ws):
                cortes = [corte_x] if corte_x else vao_das_colunas(linha)
                colunas = reconstruir(linha, cortes)
                colunas = [c for c in colunas if c.strip()]
                if not colunas:
                    continue
                print(" | ".join(colunas))
        finally:
            if os.path.exists(png):
                os.remove(png)
            tsv = png[:-4] + ".tsv"
            if os.path.exists(tsv):
                os.remove(tsv)


if __name__ == "__main__":
    if len(sys.argv) < 4:
        print(__doc__)
        raise SystemExit(1)
    corte = int(sys.argv[4]) if len(sys.argv) > 4 else None
    principal(
        sys.argv[1],
        int(sys.argv[2]),
        int(sys.argv[3]),
        corte_x=corte,
    )
