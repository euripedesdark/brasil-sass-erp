#!/usr/bin/env python3
"""Confere o que o ERP GRAVOU contra o gabarito, direto do Postgres.

Mais forte que comparar o plano: o plano prova que a leitura funciona, mas
nao prova que a gravacao guardou o que a leitura viu. Um servico que le o
desconto e grava a soma dos itens.erraria aqui e passaria la.

Compara a nota com a tabela bc_fis_nfe e a linha com bc_fis_nfe_item.
"""
import json
import subprocess
import sys

GABARITO = "/tmp/opencode/gabarito_nfe.json"
gab = json.load(open(GABARITO, encoding="utf-8"))

SQL = """
select n.id, n.numero, n.serie, n.status, n.chave_acesso,
       n.valor_produtos, n.valor_desconto, n.valor_frete, n.valor_icms,
       n.valor_ipi, n.valor_pis, n.valor_cofins, n.valor_total,
       i.numero_item, i.codigo_produto, coalesce(i.codigo_barras,'') as ean,
       i.ncm, coalesce(i.cest,'') as cest, i.cfop,
       i.quantidade, i.valor_unitario, i.valor_total as item_total,
       (i.uuid is not null) as tem_uuid, (i.created_at is not null) as tem_created
  from brasil_saas.bc_fis_nfe n
  join brasil_saas.bc_fis_nfe_item i on i.nfe_id = n.id
 where n.chave_acesso = '%s'
 order by i.numero_item
"""


def psql(chave):
    r = subprocess.run(
        ["psql", "-h", "localhost", "-U", "postgres", "-d", "brasil_saas",
         "-tA", "-F", "|", "-c", SQL % chave],
        capture_output=True, text=True,
        env={"PGPASSWORD": "ALTERE_ME", "PATH": "/usr/bin:/bin"})
    linhas = [l for l in r.stdout.strip().split("\n") if l]
    if not linhas:
        return None
    return linhas[0].split("|")


ok = True
n = 0


def confere(caminho, esperado, obtido):
    global ok, n
    n += 1
    if str(esperado) != str(obtido):
        ok = False
        print("  %-26s %-16s -> %-16s  <<< DIVERGE" % (caminho, esperado, obtido))
    else:
        print("  %-26s %-16s ok" % (caminho, esperado))


print("=" * 74)
for arquivo, g in gab.items():
    r = psql(g["chave"])
    print("=== %s ===" % arquivo)
    if not r:
        ok = False
        print("  NAO ESTA NO BANCO — a importacao nao gravou esta nota")
        continue
    (nfe_id, numero, serie, status, chave, vprod, vdesc, vfrete, vicms,
     vipi, vpis, vcofins, vtotal, nitem, cprod, ean, ncm, cest, cfop,
     qtd, vunit, vitem, tem_uuid, tem_created) = r

    confere("nfe.status", "IMPORTADA_XML", status)
    confere("nfe.numero", g["numero"], numero)
    confere("nfe.serie", g["serie"], serie)
    # O vProd da prefeitura e a soma dos vProd dos itens. Sao o mesmo numero
    # na primeira nota; na segunda, 377.24 contra um vNF de 293.98.
    confere("nfe.valor_produtos", round(sum(i["valorTotal"] for i in g["itens"]), 2),
            float(vprod))
    # O vNF da prefeitura. Em XML sem desconto e vDesc=0, entao vDesc e
    # exatamente a diferenca entre a soma e o vNF.
    confere("nfe.valor_total", g["valorTotal"], float(vtotal))
    confere("nfe.valor_desconto",
            round(sum(i["valorTotal"] for i in g["itens"]) - g["valorTotal"], 2),
            float(vdesc))

    i = g["itens"][0]
    confere("item.numero_item", i["numeroItem"], nitem)
    confere("item.codigo_produto", i["cProd"], cprod)
    # SEM GTIN tem que ter virado vazio, e nao "SEM GTIN".
    if i["ean"] and not i["ean"].isdigit():
        confere("item.ean (SEM GTIN -> vazio)", "", ean)
    else:
        confere("item.ean", i["ean"], ean)
    confere("item.ncm", i["ncm"], ncm)
    confere("item.cest", i["cest"] or "", cest)
    confere("item.cfop", i["cfop"], cfop)
    confere("item.quantidade", i["quantidade"], float(qtd))
    confere("item.valor_unitario", i["valorUnitario"], float(vunit))
    confere("item.valor_total", i["valorTotal"], float(vitem))
    confere("item.uuid presente", "t", tem_uuid)
    confere("item.created_at presente", "t", tem_created)
    print()

print("=" * 74)
print("  %d campos conferidos no banco" % n)
print("  RESULTADO: %s" % ("a gravacao bate com o gabarito" if ok else "DIVERGENCIA"))
sys.exit(0 if ok else 1)
