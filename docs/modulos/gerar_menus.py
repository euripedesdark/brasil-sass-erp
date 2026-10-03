#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Relatorio de menus por funcao.

Cruza quatro fontes e mostra, para cada item de menu:
   menu > submenu > rota > tela > endpoint

e depois o balanco backend x frontend, para dar para ver o que falta.

Nomenclatura propria: nao usa nome de nenhum produto comercial de terceiros.
"""
import os, re, json
from collections import defaultdict

RAIZ = "/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP"
REACT = os.path.join(RAIZ, "src/main/resources/static/react/src")
JAVA = os.path.join(RAIZ, "src/main/java")
DOCS = os.path.join(RAIZ, "docs/modulos")

# ---------------------------------------------------------------- 1. o menu
layout = open(os.path.join(REACT, "components/Layout.jsx"), encoding="utf-8").read()

# o bloco do dashboard tem item(...) solto; os grupos sao { label, items: [...] }
menu = []
for bloco in re.finditer(r"label:\s*'([^']+)',(.*?)\n\s*\},\n", layout, re.S):
    grupo, corpo = bloco.group(1), bloco.group(2)
    itens = re.findall(r"item\(\s*'([^']*)'\s*,\s*'[^']*'\s*,\s*'([^']*)'", corpo)
    if itens:
        menu.append({"grupo": grupo, "itens": [{"rotulo": r, "path": p} for r, p in itens]})

# dashboard e itens soltos
for m in re.finditer(r"item\(\s*'([^']*)'\s*,\s*'[^']*'\s*,\s*'([^']*)'\)", layout):
    r, p = m.group(1), m.group(2)
    if not any(i["path"] == p for g in menu for i in g["itens"]):
        menu.append({"grupo": "Geral", "itens": [{"rotulo": r, "path": p}]})

# ---------------------------------------------------------------- 2. rotas -> tela
app = open(os.path.join(REACT, "App.jsx"), encoding="utf-8").read()
rotas = {}
for trecho in re.split(r'<Route\b', app)[1:]:
    fim = trecho.find('>')
    if fim == -1: continue
    cab = trecho[:fim]
    mp = re.search(r'path="([^"]*)"', cab)
    me = re.search(r'element=\{\s*<([A-Za-z0-9_]+)', cab)
    if mp:
        rotas[mp.group(1)] = me.group(1) if me else None

imports = {}
for m in re.finditer(r"^import\s+(?:(\*\s+as\s+\w+)|(\{[^}]*\})|(\w+))\s+from\s+['\"]([^'\"]+)['\"]", app, re.M):
    destino = m.group(4)
    if m.group(3):                      # import X from '...'
        imports[m.group(3)] = destino
    elif m.group(2):                    # import { X, Y } from '...'
        for nome in m.group(2)[1:-1].split(","):
            nome = nome.split(" as ")[-1].strip()
            if nome:
                imports[nome] = destino
    elif m.group(1):                    # import * as X from '...'
        imports[m.group(1).split()[-1]] = destino

def arquivo_do_componente(nome):
    if not nome: return None
    caminho = imports.get(nome)
    if not caminho: return None
    base = caminho.replace("./", "").replace("../", "")
    for candidato in (
        os.path.join(REACT, caminho),
        os.path.join(REACT, "components", caminho.split("/")[-1]),
    ):
        if os.path.isfile(candidato):
            return os.path.relpath(candidato, RAIZ)
    return None

# ---------------------------------------------------------------- 3. endpoints
controllers = {}
for base, _, arqs in os.walk(JAVA):
    for a in arqs:
        if not a.endswith("Controller.java"): continue
        src = open(os.path.join(base, a), encoding="utf-8", errors="replace").read()
        nome = a[:-len(".java")]
        m = re.search(r'@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]*)"', src)
        raiz = m.group(1) if m else ""
        eps = set()
        for mt in re.finditer(
            r'@(Get|Post|Put|Delete|Patch)Mapping(?:\(\s*(?:value\s*=\s*)?(?:"([^"]*)"|(\w+)\.(\w+)))?', src):
            verbo = {"Get": "GET", "Post": "POST", "Put": "PUT",
                     "Delete": "DELETE", "Patch": "PATCH"}[mt.group(1)]
            sub = mt.group(2) or ""
            if not sub and mt.group(3): continue
            full = (raiz.rstrip("/") + "/" + sub.lstrip("/")).rstrip("/") or "/"
            eps.add((verbo, full))
        controllers[nome] = eps

# ---------------------------------------------------------------- 4. modulo por path
def modulo_de(path):
    if not path: return "core"
    p = path.strip("/").split("/")[0]
    mapa = {"cadastro": "cadastro", "vendas": "vendas", "compras": "compras",
            "estoque": "estoque", "financeiro": "financeiro", "fiscal": "fiscal",
            "producao": "producao", "servicos": "servicos", "rh": "rh",
            "bi": "bi", "ia": "ia", "relatorios": "relatorios",
            "ordens-servico": "servicos",
            "admin": "core", "perfil": "core", "configurar-empresa": "core"}
    return mapa.get(p, "core")

# "relatorios" e modulo proprio: /relatorios e item de menu, com tela propria.
# Antes caia dentro de bi e somava 21 endpoints la, escondendo o tamanho real
# de cada um dos dois.
MODULOS = ["cadastro", "vendas", "compras", "estoque", "financeiro", "fiscal",
           "producao", "servicos", "rh", "bi", "relatorios", "ia", "core"]

TITULOS = {
    "cadastro": "Cadastros", "vendas": "Vendas", "compras": "Compras",
    "estoque": "Estoque", "financeiro": "Financeiro", "fiscal": "Fiscal",
    "producao": "Produção", "servicos": "Serviços", "rh": "Recursos Humanos",
    "bi": "Business Intelligence", "relatorios": "Relatórios",
    "ia": "Inteligência Artificial", "core": "Core e Administração",
}

# ---------------------------------------------------------------- 5. chamadas de cada tela
def chamadas_de(caminho_rel):
    if not caminho_rel: return set()
    abs_ = os.path.join(RAIZ, caminho_rel)
    if not os.path.isfile(abs_): return set()
    src = open(abs_, encoding="utf-8", errors="replace").read()
    achados = set()
    bases = {}
    for mb in re.finditer(
        r"(?:const|let|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*['\"`](/[^'\"`]*)['\"`]", src):
        bases[mb.group(1)] = mb.group(2)
    for mb in re.finditer(
        r"(?:const|let|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*ApiConfig\.(API_BASE_URL|BASE_URL)", src):
        bases[mb.group(1)] = "/api" if mb.group(2) == "API_BASE_URL" else ""
    src2 = re.sub(r"\$\{\s*(?:ApiConfig\.)?(?:API_BASE_URL|BASE_URL)\s*(\|\|(\s*'[^']*'\s*)?)*\}", "", src)

    def norm(bruto):
        for k, v in bases.items():
            bruto = bruto.replace("${%s}" % k, v)
        bruto = re.sub(r"\$\{[^}]*\}", "*", bruto).split("?")[0]
        return re.sub(r"/+", "/", bruto).rstrip("/") or "/"

    for mt in re.finditer(r"['\"`]([^'\"`]*?/api[A-Za-z0-9_\-/{}$.?=&]*)['\"`]", src2):
        achados.add(norm(mt.group(1)))
    for mt in re.finditer(r"\.\s*(?:get|post|put|delete|patch)\s*\(\s*['\"`](/[^'\"`]*)['\"`]", src2, re.I):
        achados.add(norm(mt.group(2)))
    return achados

def casa(alvo, backend):
    if alvo == backend: return True
    if backend.endswith("/*"): return alvo.startswith(backend[:-1])
    if "*" in backend:
        pre, _, suf = backend.partition("*")
        return alvo.startswith(pre) and alvo.endswith(suf)
    return False

# ---------------------------------------------------------------- 6. monta
dados = {m: {"menu": [], "controllers": {}} for m in MODULOS}

for g in menu:
    mod = modulo_de(g["itens"][0]["path"] if g["itens"] else "")
    dados[mod]["menu"].append(g)

# controllers por modulo, pelo prefixo do path
for nome, eps in controllers.items():
    prefixos = sorted({p.split("/")[2] for _, p in eps if p.count("/") >= 2})
    mods = {modulo_de("/" + x) for x in prefixos} or {"core"}
    for m in mods:
        dados[m]["controllers"][nome] = sorted(eps)

# stats
print("=" * 90)
print("RESUMO")
print("=" * 90)
total_ep = sum(len(e) for e in controllers.values())
print("grupos de menu   : %d" % len(menu))
print("itens de menu    : %d" % sum(len(g["itens"]) for g in menu))
print("rotas declaradas : %d" % len(rotas))
print("controllers      : %d" % len(controllers))
print("endpoints        : %d" % total_ep)
print()
for m in MODULOS:
    itens = sum(len(g["itens"]) for g in dados[m]["menu"])
    eps = sum(len(e) for c in dados[m]["controllers"].values() for e in c)
    if itens or eps:
        print("  %-26s menu=%-3d controllers=%-3d endpoints=%d" % (TITULOS[m], itens, len(dados[m]["controllers"]), eps))

json.dump({
    "menu": menu, "rotas": rotas,
    "controllers": {k: sorted("%s %s" % e for e in v) for k, v in controllers.items()},
    "dados": {m: {"menu": dados[m]["menu"],
                  "controllers": {k: ["%s %s" % e for e in v] for k, v in dados[m]["controllers"].items()}}
              for m in MODULOS},
    "arquivos": {n: arquivo_do_componente(n) for n in set(rotas.values()) if n},
}, open("/tmp/opencode/menus.json", "w"), indent=1, ensure_ascii=False)
print()
print("json em /tmp/opencode/menus.json")
