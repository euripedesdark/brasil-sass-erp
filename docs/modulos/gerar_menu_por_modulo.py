#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Gera os relatorios de menu por modulo em docs/modulos."""
import os, re, json
from collections import defaultdict

RAIZ = "/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP"
REACT = os.path.join(RAIZ, "src/main/resources/static/react/src")
DOCS = os.path.join(RAIZ, "docs/modulos")
d = json.load(open("/tmp/opencode/menus.json"))

TITULOS = {
    "cadastro": "Cadastros", "vendas": "Vendas", "compras": "Compras",
    "estoque": "Estoque", "financeiro": "Financeiro", "fiscal": "Fiscal",
    "producao": "Produção", "servicos": "Serviços", "rh": "Recursos Humanos",
    "bi": "Business Intelligence", "ia": "Inteligência Artificial",
    "core": "Core e Administração",
}
ORDEM = ["cadastro", "vendas", "compras", "estoque", "financeiro", "fiscal",
         "producao", "servicos", "rh", "bi", "ia", "core"]

app = open(os.path.join(REACT, "App.jsx"), encoding="utf-8").read()
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

# --- chamadas feitas por cada tela -------------------------------------------
_cache = {}


def _achados_em(arquivos):
    """Extrai os caminhos de API de um conjunto de arquivos."""
    src = "\n".join(open(a, encoding="utf-8", errors="replace").read() for a in arquivos)

    # Neutraliza ANTES de ler as constantes. Constante de URL costuma ser
    # `${ApiConfig.BASE_URL || ''}/api/financeiro/titulos`: o valor comeca com
    # "${", entao o regex que exige "/" inicial nao casa, e a constante ficava
    # fora de `bases` —levando o sufixo junto fora (/aprovacoes/pendentes).
    s2 = re.sub(r"\$\{\s*ApiConfig\.(?:API_BASE_URL|BASE_URL)\s*(\|\|(\s*'[^']*'\s*)?)*\}", "", src)

    bases = {}
    for mb in re.finditer(
        r"(?:const|let|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*['\"`](/[^'\"`]*)['\"`]", s2):
        bases[mb.group(1)] = mb.group(2)
    for mb in re.finditer(
        r"(?:const|let|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*ApiConfig\.(API_BASE_URL|BASE_URL)", s2):
        bases[mb.group(1)] = "/api" if mb.group(2) == "API_BASE_URL" else ""

    def norm(br):
        for k, v in bases.items():
            br = br.replace("${%s}" % k, v)
        br = re.sub(r"\$\{[^}]*\}", "*", br).split("?")[0]
        return re.sub(r"/+", "/", br).rstrip("/") or "/"

    achados = set()
    # (a) literais com /api
    for mt in re.finditer(r"['\"`]([^'\"`]*?/api[A-Za-z0-9_\-/{}$.?=&]*)['\"`]", s2):
        achados.add(norm(mt.group(1)))
    # (b) as proprias constantes de base. Alguns services montam
    # ${ApiConfig.API_BASE_URL}/vendas/pedidos, que depois da neutralizacao
    # fica "/vendas/pedidos" — sem "/api" no texto, entao (a) nao via. Sao
    # justamente as telas que passam pelo service.
    for nome, valor in bases.items():
        if valor.startswith("/"):
            achados.add(norm("${%s}" % nome))
            achados.add(norm(valor))
    # (c) templates que CONCATENAM a constante com o resto do caminho:
    # `${BASE_URL}/cadastro/marcas`. Sem isto, so a referencia solta era
    # expandida e o caminho inteiro nunca aparecia — por isso Marca,
    # UnidadeMedida e varias telas marcavam 0% cobertura.
    if bases:
        nomes = "|".join(re.escape(k) for k in bases)
        for mt in re.finditer(r"`([^`]*\$\{(?:%s)\}[^`]*)`" % nomes, s2):
            achados.add(norm(mt.group(1)))
    for mt in re.finditer(r"\.\s*(?:get|post|put|delete|patch)\s*\(\s*['\"`](/[^'\"`]*)['\"`]", s2, re.I):
        achados.add(norm(mt.group(1)))
    return achados


def chamadas_de_componente(comp):
    if comp in _cache:
        return _cache[comp]
    achados = set()
    caminho = imports.get(comp)
    if caminho:
        base = caminho[:-3] if caminho.endswith((".jsx", ".js")) else caminho
        arquivo = None
        for ext in (".jsx", ".js", ""):
            cand = os.path.join(REACT, base + ext)
            if os.path.isfile(cand):
                arquivo = cand
                break

        if arquivo:
            # A tela costuma delegar a API para um service. Ler so o .jsx
            # deixava de fora tudo que vai por service — a maioria das telas
            # de Financeiro, Compras e Fiscal — e fazia o modulo inteiro
            # aparecer com 0% de cobertura.
            arquivos = [arquivo]
            texto = open(arquivo, encoding="utf-8", errors="replace").read()
            for sm in re.finditer(r"^import\s+.*?from\s+['\"]([^'\"]+)['\"]", texto, re.M):
                rel = sm.group(1)
                # segue QUALQUER import local, nao so os de service. Antes a
                # galeria de imagem (components/shared/ImagensProduto) nao
                # entrava, e as duas rotas de imagem ficavam marcadas como
                # "sem tela" tendo tela.
                if not rel.startswith("."):
                    continue
                sb = rel[3:] if rel.startswith("./") else rel
                for ext in (".js", ".jsx"):
                    cand = os.path.normpath(os.path.join(os.path.dirname(arquivo), sb + ext))
                    if os.path.isfile(cand) and cand not in arquivos:
                        arquivos.append(cand)
            achados = _achados_em(arquivos)
    _cache[comp] = achados
    return achados


def normaliza_backend(path):
    """{id} vira *, igual o frontend ja fazia.

    Sem isto, o caminho do frontend (/api/cadastro/categorias/*) nunca casava
    com o do backend (/api/cadastro/categorias/{id}), e telas que edittam e
    excluem apareciam como "sem tela"."""
    p = path.split("?")[0]
    p = re.sub(r"\{[^}]+\}", "*", p)
    return re.sub(r"/+", "/", p).rstrip("/") or "/"


def casa(alvo, backend):
    b = normaliza_backend(backend)
    if alvo == b: return True
    if b.endswith("/*"): return alvo.startswith(b[:-1])
    if "*" in b:
        pre, _, suf = b.partition("*")
        return alvo.startswith(pre) and alvo.endswith(suf)
    return False

todos_backends = set()
for ctrl, eps in d["controllers"].items():
    for e in eps: todos_backends.add(e.split(" ", 1)[1])

# --- gera um MD por modulo ----------------------------------------------------
def gera(mod):
    bloco = d["dados"][mod]
    linhas = []
    A = linhas.append
    A("# %s" % TITULOS[mod])
    A("")
    A("> Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e")
    A("> chamadas de API de cada tela. Os nomes das funções são descritivos e")
    A("> próprios do sistema.")
    A("")

    # menu
    A("## Menu")
    A("")
    if not bloco["menu"]:
        A("Sem item de menu neste módulo.")
    for g in bloco["menu"]:
        A("**%s**" % g["grupo"])
        A("")
        A("| Submenu | Rota | Tela |")
        A("|---|---|---|")
        for it in g["itens"]:
            comp = None
            for p, c in d["rotas"].items():
                if p.strip("/") == it["path"].strip("/") or ("/" + p) == it["path"]:
                    comp = c; break
            A("| %s | `%s` | %s |" % (it["rotulo"], it["path"], ("`%s`" % comp) if comp else "— **sem rota**"))
        A("")

    # telas x endpoints
    A("## Telas e o que cada uma acessa")
    A("")
    for g in bloco["menu"]:
        for it in g["itens"]:
            comp = None
            for p, c in d["rotas"].items():
                if p.strip("/") == it["path"].strip("/") or ("/" + p) == it["path"]:
                    comp = c; break
            if not comp: continue
            chamadas = chamadas_de_componente(comp)
            if chamadas:
                A("**%s** (`%s`) — %d chamada(s)" % (it["rotulo"], it["path"], len(chamadas)))
                A("")
                for c in sorted(chamadas): A("- `%s`" % c)
                A("")

    # controllers
    A("## Backend do módulo")
    A("")
    if not bloco["controllers"]:
        A("Sem controller.")
    total = 0
    for ctrl in sorted(bloco["controllers"]):
        eps = bloco["controllers"][ctrl]
        total += len(eps)
        A("### %s — %d endpoint(s)" % (ctrl, len(eps)))
        A("")
        A("| | | usado por alguma tela? |")
        A("|---|---|---|")
        for e in eps:
            verbo, path = e.split(" ", 1)
            usado = any(casa(c, path) for c in todas_chamadas()) if False else None
            A("| %s | `%s` | |" % (verbo, path))
        A("")
    A("**Total do módulo: %d endpoints.**" % total)
    A("")
    return "\n".join(linhas), total

def todas_chamadas():
    s = set()
    for c in set(v for v in d["rotas"].values() if v):
        s |= chamadas_de_componente(c)
    return s

# ------------------------------------------------------------------ principal
CHAMADAS_TODAS = todas_chamadas()

def cobertura():
    por_mod = {}
    for mod in ORDEM:
        bloco = d["dados"][mod]
        sem_acesso = []
        for ctrl, eps in bloco["controllers"].items():
            for e in eps:
                path = e.split(" ", 1)[1]
                if not any(casa(c, path) for c in CHAMADAS_TODAS):
                    sem_acesso.append(e)
        total = sum(len(v) for v in bloco["controllers"].values())
        por_mod[mod] = (sem_acesso, total, bloco)
    return por_mod

# reescreve o gerador de modulo usando a cobertura ja calculada
def gera_md(mod, sem_acesso, total):
    bloco = d["dados"][mod]
    L = []
    A = L.append
    A("# %s" % TITULOS[mod])
    A("")
    A("Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as")
    A("chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.")
    A("")
    A("| | |")
    A("|---|---|")
    A("| Itens de menu | %d |" % sum(len(g["itens"]) for g in bloco["menu"]))
    A("| Controllers | %d |" % len(bloco["controllers"]))
    A("| Endpoints | %d |" % total)
    A("| Endpoints sem tela | %d |" % len(sem_acesso))
    A("")
    A("---")
    A("")
    A("## 1. Menu")
    A("")
    if not bloco["menu"]:
        A("Este módulo não tem item de menu.")
        A("")
    for g in bloco["menu"]:
        A("### %s" % g["grupo"])
        A("")
        A("| Submenu | Rota | Componente |")
        A("|---|---|---|")
        for it in g["itens"]:
            comp = None
            for p, c in d["rotas"].items():
                if p.strip("/") == it["path"].strip("/") or ("/" + p) == it["path"]:
                    comp = c; break
            A("| %s | `%s` | %s |" % (it["rotulo"], it["path"],
                ("`%s`" % comp) if comp else "**sem rota**"))
        A("")

    A("## 2. Função por função: tela e endpoint")
    A("")
    for g in bloco["menu"]:
        for it in g["itens"]:
            comp = None
            for p, c in d["rotas"].items():
                if p.strip("/") == it["path"].strip("/") or ("/" + p) == it["path"]:
                    comp = c; break
            A("### %s" % it["rotulo"])
            A("")
            A("- **Rota:** `%s`" % it["path"])
            A("- **Componente:** %s" % ("`%s`" % comp if comp else "**sem rota registrada**"))
            if comp:
                ch = chamadas_de_componente(comp)
                if ch:
                    A("- **Endpoints usados:** %d" % len(ch))
                    for c in sorted(ch): A("  - `%s`" % c)
                else:
                    A("- **Endpoints usados:** nenhum identificado (tela pode estar mock ou só ler por outro caminho)")
            A("")

    A("## 3. Backend do módulo")
    A("")
    for ctrl in sorted(bloco["controllers"]):
        eps = bloco["controllers"][ctrl]
        A("### %s" % ctrl)
        A("")
        A("| | Endpoint | Acesso por tela |")
        A("|---|---|---|")
        for e in eps:
            verbo, path = e.split(" ", 1)
            A("| %s | `%s` | %s |" % (verbo, path, "—" if e in sem_acesso else "sim"))
        A("")
    return "\n".join(L)

cob = cobertura()
os.makedirs(DOCS, exist_ok=True)
for mod in ORDEM:
    sem_acesso_lista, total, _ = cob[mod]
    sem_acesso = sem_acesso_lista
    if not d["dados"][mod]["menu"] and total == 0: continue
    conteudo = gera_md(mod, sem_acesso, total)
    with open(os.path.join(DOCS, "%s.md" % mod), "w", encoding="utf-8") as f:
        f.write(conteudo + "\n")
    print("  %-14s itens=%-3d endpoints=%-4d sem_tela=%d" % (
        mod, sum(len(g["itens"]) for g in d["dados"][mod]["menu"]), total, len(sem_acesso)))

# ------------------------------------------------------------------ indice
tot_ep = sum(v[1] for v in cob.values())
tot_sem = sum(len(v[0]) for v in cob.values())
L = []
A = L.append
A("# Mapa de módulos — menu, função e cobertura")
A("")
A("Uma visão por módulo do que existe no menu, o que está em código no backend e o")
A("que tem tela. Serve para comparar a abrangência: o número de **endpoints sem tela**")
A("é a medida direta do buraco.")
A("")
A("| Módulo | Itens de menu | Controllers | Endpoints | Sem tela | Cobertura |")
A("|---|---:|---:|---:|---:|---:|")
for mod in ORDEM:
    sem_acesso, total, bloco = cob[mod]
    sem_acesso = len(sem_acesso)
    itens = sum(len(g["itens"]) for g in bloco["menu"])
    if not itens and not total: continue
    cob_pct = 100 - round(100 * sem_acesso / total) if total else 100
    A("| [%s](%s.md) | %d | %d | %d | %d | %d%% |" % (
        TITULOS[mod], mod, itens, len(bloco["controllers"]), total, sem_acesso, cob_pct))
A("| **Total** | **%d** | **%d** | **%d** | **%d** | **%d%%** |" % (
    sum(sum(len(g["itens"]) for g in cob[m][2]["menu"]) for m in ORDEM),
    sum(len(cob[m][2]["controllers"]) for m in ORDEM),
    tot_ep, tot_sem, 100 - round(100 * tot_sem / tot_ep)))
A("")
A("> `Sem tela` = endpoint que existe no backend e nenhuma tela chama. É a lista")
A("> do que o operador não consegue fazer pelo sistema.")
A("")
with open(os.path.join(DOCS, "docs/modulos/README.md"), "w", encoding="utf-8") as f:
    f.write("\n".join(L) + "\n")
print()
print("indice: docs/modulos/README.md")
print("total: %d endpoints, %d sem tela" % (tot_ep, tot_sem))
