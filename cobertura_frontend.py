#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Cobertura de frontend, do backend para a tela.

Responde: qual capacidade o backend expoe e o usuario nao tem como acessar.

A versao anterior falhou por so casar literais /api/... e tratar a constante
BASE de um service como se fosse a rota chamada. Aqui:

  1. os endpoints sao extraidos dos controllers (verb + path)
  2. as chamadas do frontend sao extraidas resolvendo a BASE de cada service
  3. as duas pontas sao normalizadas (template -> *, query fora, barra final)
  4. o resto e classificado por controller
"""
import os, re, json
from collections import defaultdict

RAIZ = os.environ.get("BRASIL_SAAS_RAIZ") or os.path.dirname(os.path.abspath(__file__))
JAVA = os.path.join(RAIZ, "src/main/java")
REACT = os.path.join(RAIZ, "src/main/resources/static/react/src")

# --------------------------------------------------------------- 1. backend
VERBS = {"GetMapping": "GET", "PostMapping": "POST", "PutMapping": "PUT",
         "DeleteMapping": "DELETE", "PatchMapping": "PATCH"}

controllers = {}
for base, _, arqs in os.walk(JAVA):
    for a in arqs:
        if not a.endswith("Controller.java"):
            continue
        src = open(os.path.join(base, a), encoding="utf-8", errors="replace").read()
        nome = a[:-len(".java")]
        m = re.search(r'@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]*)"', src)
        raiz = m.group(1) if m else ""
        eps = set()
        for mt in re.finditer(
                r'@(Get|Post|Put|Delete|Patch)Mapping(?:\(\s*(?:value\s*=\s*)?(?:"([^"]*)"|(\w+)\.(\w+)))?',
                src):
            verbo = VERBS[mt.group(1) + "Mapping"]
            sub = mt.group(2) or ""
            if not sub and mt.group(3):
                continue  # constante, resolvida na parte do frontend
            full = (raiz.rstrip("/") + "/" + sub.lstrip("/")).rstrip("/") or "/"
            eps.add((verbo, full))
        controllers[nome] = eps

# --------------------------------------------------------------- 2. frontend
chamadas = defaultdict(set)   # path normalizado -> set(arquivos)

# a) literais /api/... e /api
padrao_api = re.compile(r"""['"`](/api[A-Za-z0-9_\-/{}$.]*)['"`]""")
# b) metodo do axios:  api.get('/x')  /  axios.post(`/y`)
padrao_metodo = re.compile(
    r"\.\s*(get|post|put|delete|patch)\s*\(\s*['\"`](/[^'\"`]*)['\"`]", re.I)
# c) template com interpolacao:  `${BASE}/foo/${id}`
TOKEN = re.compile(r"""'[^']*'|"[^"]*"|`[^`]*`|[A-Za-z_$][\w$.]*(?:\([^()]*\))?""")
_TK = TOKEN.pattern
CADEIA = re.compile(r"\b([A-Za-z_]\w*)\s*\+\s*((?:" + _TK + r")(?:\s*\+\s*(?:" + _TK + r"))*)")
CADEIA_LIT = re.compile(r"""(['"])(/api[^'"]*)\1\s*\+\s*((?:""" + _TK + r""")(?:\s*\+\s*(?:""" + _TK + r"""))*)""")
padrao_template = re.compile(
    r"\.\s*(get|post|put|delete|patch)\s*\(\s*`([^`]*)`", re.I)

for base, _, arqs in os.walk(REACT):
    if "node_modules" in base:
        continue
    for a in arqs:
        if not a.endswith((".js", ".jsx")):
            continue
        caminho = os.path.join(base, a)
        rel = os.path.relpath(caminho, REACT)
        src = open(caminho, encoding="utf-8", errors="replace").read()

        # BASE_URL e '' (o axios ja resolve a origem). Varios services montam a
        # URL como `${ApiConfig.BASE_URL || ''}/api/...`; sem neutralizar isso
        # aqui, a constante nao vira literal e o endpoint inteiro aparecia como
        # "sem tela" — foi o caso do TituloService, que tem tela e service.
        # Services com `const BASE_URL = ApiConfig.API_BASE_URL` (Folha, EntradaNota...):
        # ai BASE_URL vale '/api', e apagar o prefixo escondia todas as chamadas.
        if re.search(r"(?:const|let|var)\s+(?:BASE_URL|API_BASE_URL)\s*=\s*(?:ApiConfig\.)?API_BASE_URL", src):
            src = re.sub(r"\$\{\s*(?:ApiConfig\.)?(?:API_BASE_URL|BASE_URL)\s*\}", "/api", src)
            src = re.sub(r"\b(?:ApiConfig\.)?(?:API_BASE_URL|BASE_URL)\s*\+\s*(?=['\"`])", "'/api' + ", src)
        src = re.sub(r"\$\{\s*ApiConfig\.BASE_URL\s*(\|\|(\s*'[^']*'\s*)?)*\}", "", src)
        src = re.sub(r"\$\{\s*(?:ApiConfig\.)?(?:API_BASE_URL|BASE_URL)\s*(\|\|(\s*'[^']*'\s*)?)*\}", "", src)

        # resolve as constantes de base do proprio arquivo
        bases = {}
        # `const BASE = ApiConfig.API_BASE_URL` (CargoService e varios outros):
        # a constante vem de membro, nao de literal, entao ficava sem valor e o
        # endpoint inteiro aparecia como "sem tela".
        for mb in re.finditer(
                r"(?:const|let|var)\s+([A-Z_][A-Z0-9_]*)\s*=\s*ApiConfig\.(API_BASE_URL|BASE_URL)", src):
            bases[mb.group(1)] = "/api" if mb.group(2) == "API_BASE_URL" else ""
        for mb in re.finditer(
                r"(?:const|let|var)\s+([A-Za-z_]\w*)\s*=\s*['\"`](/[^'\"`]*)['\"`]", src):
            bases[mb.group(1)] = mb.group(2)

        # c) apiFetch(`...`) e QUALQUER template que contenha /api.
        # Antes o Dashboard (que usa apiFetch com template e query) nao era
        # accounted, e /api/bi/indicadores/dashboard aparecia como "sem tela".
        for mt in re.finditer(r"['\"`]([^'\"`]*?/api[A-Za-z0-9_\-/{}$.?=&]*)['\"`]", src):
            bruto = mt.group(1)
            for nome_const, valor in bases.items():
                bruto = bruto.replace("${%s}" % nome_const, valor)
            if '/api' in bruto:
                chamadas[bruto].add(rel)

        for m in padrao_api.finditer(src):
            chamadas[m.group(1)].add(rel)
        for m in padrao_metodo.finditer(src):
            caminho_api = m.group(2)
            if caminho_api.startswith("/api"):
                chamadas[caminho_api].add(rel)
        for m in padrao_template.finditer(src):
            bruto = m.group(2)
            for nome_const, valor in bases.items():
                bruto = bruto.replace("${%s}" % nome_const, valor)
            if re.match(r"^/api", bruto):
                chamadas[bruto].add(rel)

        # e) qualquer template iniciado por ${CONST}: apiFetch(`${BASE}/x/${id}`)
        for mt in re.finditer(r"`\$\{(\w+)\}([^`]*)`", src):
            if mt.group(1) in bases:
                resto = re.sub(r"\$\{[^}]*\}", "*", mt.group(2))
                full = bases[mt.group(1)] + resto
                if full.startswith("/api"):
                    chamadas[full].add(rel)

        # f) literal /api iniciando a cadeia: '/api/x/' + r.id + '/acao'
        for ml in CADEIA_LIT.finditer(src):
            caminho_cat = ml.group(2)
            for tk in TOKEN.findall(ml.group(3)):
                if tk[0] in "'\"":
                    caminho_cat += tk[1:-1]
                elif tk[0] == "`":
                    caminho_cat += re.sub(r"\$\{[^}]*\}", "*", tk[1:-1])
                else:
                    caminho_cat += "*"
            chamadas[caminho_cat].add(rel)

        # d) concatenacao:  BASE + '/lancamentos/' + id + '/lancar'
        #    O script so resolvia ${BASE}; telas que montam a URL com '+'
        #    (Contabilidade, WMS, Projetos...) apareciam como "sem tela".
        for mc in CADEIA.finditer(src):
            nome, resto = mc.group(1), mc.group(2)
            if nome not in bases:
                continue
            caminho_cat = bases[nome]
            for tk in TOKEN.findall(resto):
                if tk[0] in "'\"":
                    caminho_cat += tk[1:-1]
                elif tk[0] == "`":
                    caminho_cat += re.sub(r"\$\{[^}]*\}", "*", tk[1:-1])
                else:
                    caminho_cat += "*"
            if caminho_cat.startswith("/api"):
                chamadas[caminho_cat].add(rel)

# as constantes de base tambem contam, por causa dos services que usam
# axios.get(API_URL) sem template
for base, _, arqs in os.walk(REACT):
    if "node_modules" in base:
        continue
    for a in arqs:
        if not a.endswith((".js", ".jsx")):
            continue
        caminho = os.path.join(base, a)
        rel = os.path.relpath(caminho, REACT)
        src = open(caminho, encoding="utf-8", errors="replace").read()
        for mb in re.finditer(
                r"(?:const|let|var)\s+([A-Za-z_]\w*)\s*=\s*['\"`](/api[^'\"`]*)['\"`]", src):
            chamadas[mb.group(2)].add(rel + " (constante)")

# --------------------------------------------------------------- 3. normalizar
def norm(p):
    p = p.split("?")[0]
    p = re.sub(r"\$\{[^}]*\}", "*", p)   # ${id} -> *
    p = re.sub(r"\{[^}]*\}", "*", p)      # {id}  -> *
    if not p.startswith("/api"):
        p = "/api" + p if p.startswith("/") else p
    p = re.sub(r"/+", "/", p).rstrip("/") or "/"
    return p

def casa(alvo, backend):
    """o alvo do frontend casa com o path do backend?"""
    if alvo == backend:
        return True
    b = backend
    if b.endswith("/*"):
        return alvo.startswith(b[:-1])
    if "*" in b:
        pre, _, suf = b.partition("*")
        return alvo.startswith(pre) and alvo.endswith(suf) and len(alvo) >= len(pre) + len(suf)
    return False

front_norm = {}
for paths, arqs in chamadas.items():
    front_norm.setdefault(norm(paths), set()).update(arqs)

# --------------------------------------------------------------- 4. cobertura
print("=" * 92)
print("CAPACIDADE DO BACKEND SEM ACESSO NO FRONTEND")
print("=" * 92)
print("%d controllers, %d endpoints | %d caminhos distintos chamados pelo frontend"
      % (len(controllers), sum(len(v) for v in controllers.values()), len(front_norm)))
print()

sem_acesso, com_acesso = [], []
for ctrl in sorted(controllers):
    eps = controllers[ctrl]
    fora = [e for e in eps
            if not any(casa(norm(e[1]), alvo) for alvo in front_norm)]
    dentro = [e for e in eps if e not in fora]
    if fora:
        sem_acesso.append((ctrl, dentro, fora))
    if dentro:
        com_acesso.append((ctrl, dentro))

total_sem = sum(len(f) for _, _, f in sem_acesso)
total_com = sum(len(d) for _, d in com_acesso)
print("controllers com pelo menos 1 endpoint sem tela : %d" % len(sem_acesso))
print("endpoints sem tela                            : %d de %d"
      % (total_sem, total_sem + total_com))
print()

for ctrl, dentro, fora in sorted(sem_acesso, key=lambda x: -len(x[2])):
    print("### %s  —  %d/%d endpoints sem acesso" % (ctrl, len(fora), len(fora) + len(dentro)))
    for v, p in sorted(fora):
        print("      %-7s %s" % (v, p))
    print()

json.dump(
    {"controllers": {c: sorted("%s %s" % e for e in eps) for c, eps in controllers.items()},
     "sem_acesso": {c: ["%s %s" % e for e in f] for c, _, f in sem_acesso},
     "total_sem_acesso": total_sem,
     "total_endpoints": total_sem + total_com},
    open(os.path.join(os.environ.get("TMPDIR", "/tmp"), "cobertura.json"), "w"), indent=1, ensure_ascii=False)
