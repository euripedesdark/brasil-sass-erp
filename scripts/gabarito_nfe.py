#!/usr/bin/env python3
"""Prova a leitura das duas NFe reais de ~/Downloads, comparando com o XML.

Este script NAO e teste do codigo Java. E o gabarito: extrai os mesmos campos
em Python, direto do XML, e devolve um JSON que o Java tem que reproduzir
identico. A comparacao campo a campo e feita pelo resultado do endpoint.

Por que um gabarito em Python e nao "olhei e acreditei": o leitor tem 200
linhas de parser de XML com namespace, prefixo opcional, GZIP e aninhamento.
Confiar no olho em parser e o jeito classico de o parser passar em um caso e
falhar em outro. Aqui o gabarito vem de uma segunda implementacao, com regex
simples, e os dois tem que concordar.
"""
import json
import os
import re
import subprocess
import sys

DOWNLOADS = "/home/euripedes/Downloads"
ARQUIVOS = [
    "35260341068753000116550080006464051177461672.xml",
    "35260561412110008997650550000153511149915393-nfe.xml",
]

# O que o Java tem que trazer, e de onde.
CAMPOS_ITEM = ["numeroItem", "cProd", "ean", "descricao", "ncm", "cest",
               "cfop", "unidade", "quantidade", "valorUnitario", "valorTotal"]
CAMPOS_CAB = ["chave", "numero", "serie", "emissao", "emitenteCnpj",
              "emitenteNome", "destinatarioCnpj", "destinatarioNome",
              "naturezaOperacao", "cfopNota", "valorTotal"]


def tag(xml, nome):
    m = re.search(r"<(?:[\w.-]+:)?" + nome + r"\b[^>]*>(.*?)</(?:[\w.-]+:)?" + nome + r">",
                  xml, re.S)
    if not m:
        return None
    return re.sub(r"<[^>]+>", "", m.group(1)).strip() or None


def bloco(xml, nome):
    m = re.search(r"<" + nome + r"\b[^>]*>", xml)
    if not m:
        return None
    i, nivel = m.end(), 1
    abre = re.compile(r"<" + nome + r"\b[^>]*>")
    fecha = re.compile(r"</" + nome + r">")
    while nivel > 0:
        a = abre.search(xml, i)
        f = fecha.search(xml, i)
        if not a and not f:
            return None
        if a and (not f or a.start() < f.start()):
            nivel += 1
            i = a.end()
        else:
            nivel -= 1
            i = f.end()
            if nivel == 0:
                return xml[m.end():f.start()]


def gabarito(caminho):
    bruto = open(caminho, "rb").read()
    if bruto[:2] == b"\x1f\x8b":
        import gzip
        bruto = gzip.decompress(bruto)
    xml = bruto.decode("utf-8", errors="replace")

    inf = bloco(xml, "infNFe") or xml
    emit = bloco(inf, "emit") or ""
    dest = bloco(inf, "dest")

    chave = tag(xml, "chNFe")
    if not chave or len(chave) != 44:
        digitos = re.sub(r"\D", "", os.path.basename(caminho))
        chave = digitos if len(digitos) == 44 else None

    itens = []
    for det in re.findall(r"<det\b[^>]*>(.*?)</det>", xml, re.S):
        qtd = tag(det, "qCom") or tag(det, "qUnCom") or "0"
        unit = tag(det, "vUnCom") or tag(det, "vUnNCom") or "0"
        total = tag(det, "vProd")
        itens.append({
            "numeroItem": int(tag(det, "nItem") or len(itens) + 1),
            "cProd": tag(det, "cProd"),
            "ean": tag(det, "cEAN"),
            "descricao": tag(det, "xProd"),
            "ncm": tag(det, "NCM"),
            "cest": tag(det, "CEST"),
            "cfop": tag(det, "CFOP"),
            "unidade": tag(det, "uCom"),
            "quantidade": float(qtd.replace(",", ".")),
            "valorUnitario": float(unit.replace(",", ".")),
            "valorTotal": float(total.replace(",", ".")) if total else
                           round(float(qtd) * float(unit), 2),
        })

    dest_cnpj = None
    if dest:
        dest_cnpj = tag(dest, "CNPJ") or tag(dest, "CPF")

    return {
        "chave": chave,
        "numero": tag(inf, "nNF"),
        "serie": tag(inf, "serie"),
        "emissao": (tag(inf, "dhEmi") or "")[:10] or None,
        "emitenteCnpj": re.sub(r"\D", "", tag(emit, "CNPJ") or ""),
        "emitenteNome": tag(emit, "xNome") or tag(emit, "xFant"),
        "destinatarioCnpj": re.sub(r"\D", "", dest_cnpj or ""),
        "destinatarioNome": tag(dest, "xNome") if dest else None,
        "naturezaOperacao": tag(inf, "natOp"),
        "cfopNota": itens[0]["cfop"] if itens else None,
        "valorTotal": float((tag(inf, "vNF") or "0").replace(",", ".")),
        "itens": itens,
    }


def main():
    resultado = {}
    for nome in ARQUIVOS:
        caminho = os.path.join(DOWNLOADS, nome)
        if not os.path.exists(caminho):
            print("  %s nao existe" % nome)
            continue
        g = gabarito(caminho)
        resultado[nome] = g
        print("=== %s ===" % nome)
        for c in CAMPOS_CAB:
            print("  %-20s %s" % (c, g[c]))
        for i in itens if (itens := g["itens"]) else []:
            print("    item %d: %s" % (i["numeroItem"], json.dumps(
                {k: i[k] for k in CAMPOS_ITEM}, ensure_ascii=False)))
        print()

    with open("/tmp/opencode/gabarito_nfe.json", "w", encoding="utf-8") as f:
        json.dump(resultado, f, ensure_ascii=False, indent=1)
    print("  gabarito salvo em /tmp/opencode/gabarito_nfe.json")
    print("  %d nota(s)" % len(resultado))
    return 0


if __name__ == "__main__":
    sys.exit(main())
