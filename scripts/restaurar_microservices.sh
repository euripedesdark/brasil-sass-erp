#!/usr/bin/env bash
# Restaura as pastas de microservices a partir do manifesto de hashes.
#
# POR QUE ISTO E UM SCRIPT E NAO UM "git clone" E PRONTO
#
# So 3 das 23 pastas tem commit registrado. Das outras 20 o historico foi
# perdido e nao ha commit nenhum. Um clone dessas 20 traz o HEAD de hoje, que
# pode ser outra versao do material com que o ERP foi testado -- e uma
# biblioteca de imposto que mudou de comportamento aparece como "a API antes
# funcionava", que e o tipo de bug que se procura semanas.
#
# Este script faz tres coisas, nesta ordem, e a ordem importa:
#
#   1. baixa (usando o commit quando existe; HEAD quando nao existe)
#   2. apaga o .git, porque o material e referencia e nao entra no git do ERP
#   3. VERIFICA o SHA-256 de cada arquivo contra o manifesto
#
# O passo 3 e o que o script existe para fazer. Sem ele, um clone que trouxe a
# versao errada entraria em producao em silencio. Com ele, o script diz que
# NAO bate, quantos arquivos divergem, e o operador decide.
#
# Nao apaga nada sem --limpar. Restaurar por cima de pasta existente faz o
# script pular o que ja esta la, e um "ja existe" silencioso e exatamente o
# tipo de coisa que esconde material desatualizado.
#
# USO
#   ./restaurar_microservices.sh                    # restaura o que falta
#   ./restaurar_microservices.sh --verificar        # so confere, nao baixa
#   ./restaurar_microservices.sh --forcar           # rebaixa tudo
#   ./restaurar_microservices.sh --limpar           # apaga tudo antes
#   ./restaurar_microservices.sh --so nfe,sped-mdfe # so essas pastas
#   ./restaurar_microservices.sh --destino /opt/x   # restaura em outro lugar
#   ./restaurar_microservices.sh --sem-verificar     # rapido, sem confere
#   ./restaurar_microservices.sh --jobs 4             # downloads em paralelo

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_RAIZ="$(cd "$SCRIPT_DIR/.." && pwd)"

DESTINO="${REPO_RAIZ}/src/main/resources/microservices"
MANIFESTO=""
VERIFICAR=1
FORCAR=0
LIMPAR=0
JOBS=4
SO_PASTAS=""

CORES=0
if [ -t 1 ]; then CORES=1; fi
R=$'\033[31m'; V=$'\033[32m'; A=$'\033[33m'; C=$'\033[36m'; N=$'\033[0m'; B=$'\033[1m'
[ "$CORES" = "1" ] || { R=""; V=""; A=""; C=""; N=""; B=""; }

info()  { echo "${C}$*${N}"; }
ok()    { echo "  ${V}ok${N}    $*"; }
aviso() { echo "  ${A}aviso${N}  $*"; }
erro()  { echo "  ${R}erro${N}  $*"; }
passo() { echo; echo "${B}$*${N}"; }

# ---------------------------------------------------------------------------
while [ $# -gt 0 ]; do
  case "$1" in
    --verificar)    VERIFICAR=0 ;;
    --sem-verificar) VERIFICAR=1 ;;
    --forcar)       FORCAR=1 ;;
    --limpar)       LIMPAR=1 ;;
    --jobs)         JOBS="$2"; shift ;;
    --destino)      DESTINO="$2"; shift ;;
    --manifesto)    MANIFESTO="$2"; shift ;;
    --so)           SO_PASTAS="$2"; shift ;;
    -h|--help)      sed -n '2,40p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) erro "opcao desconhecida: $1"; exit 2 ;;
  esac
  shift
done

[ -n "$MANIFESTO" ] || MANIFESTO="${REPO_RAIZ}/src/main/resources/microservices/MANIFESTO-MICROSERVICES.json"

if ! command -v python3 >/dev/null 2>&1; then
  erro "python3 nao encontrado: o manifesto e lido com ele"
  exit 1
fi
if [ ! -f "$MANIFESTO" ]; then
  erro "manifesto nao encontrado: $MANIFESTO"
  erro "gerar com: python3 scripts/gerar_manifesto.py"
  exit 1
fi

# ---------------------------------------------------------------------------
# O Python le o JSON. Shell nao le JSON sem dependencia, e nao vale levar
# jq como requisito para um script de instalacao.
lista_pastas() {
  python3 - "$MANIFESTO" <<'PY'
import json, sys
m = json.load(open(sys.argv[1], encoding="utf-8"))
for nome in sorted(m["_pastas"]):
    print(nome)
PY
}

campo() {
  # campo <pasta> <chave>  ->  valor, ou vazio
  python3 - "$MANIFESTO" "$1" "$2" <<'PY'
import json, sys
m = json.load(open(sys.argv[1], encoding="utf-8"))
e = m["_pastas"].get(sys.argv[2], {})
v = e.get(sys.argv[3], "")
if isinstance(v, (dict, list)):
    v = json.dumps(v, ensure_ascii=False)
print(v if v is not None else "")
PY
}

quer() {
  [ -z "$SO_PASTAS" ] && return 0
  case ",$SO_PASTAS," in *",$1,"*) return 0 ;; *) return 1 ;; esac
}

limpar_pasta() {
  local alvo="$DESTINO/$1"
  [ -e "$alvo" ] || return 0
  # Guarda o que nao vem de repositorio externo: documento e codigo nosso.
  local salvos=()
  for item in LEIA-ME.md BRASIL-SAAS.md O-SERVICO-EM-PRODUCAO.md INVENTARIO.md; do
    [ -f "$alvo/$item" ] && salvos+=("$item")
  done
  local tmp; tmp="$(mktemp -d)"
  for s in "${salvos[@]:-}"; do
    [ -n "$s" ] && cp -a "$alvo/$s" "$tmp/" 2>/dev/null
  done
  rm -rf "$alvo"
  mkdir -p "$alvo"
  for s in "${salvos[@]:-}"; do
    [ -n "$s" ] && cp -a "$tmp/$s" "$alvo/" 2>/dev/null
  done
  rm -rf "$tmp"
  [ ${#salvos[@]} -gt 0 ] && info "$1: preservado ${#salvos[@]} doc(s) local(is)"
  return 0
}

baixar_repo() {
  local pasta="$1" url="$2" commit="$3" alvo="$DESTINO/$1"
  local log; log="$(mktemp)"

  if [ -n "$commit" ] && [ "$commit" != "None" ]; then
    info "$pasta: baixando no commit $commit"
    if ! git clone --quiet "$url" "$alvo" >"$log" 2>&1; then
      erro "$pasta: clone falhou"; sed 's/^/        /' "$log" | head -4; rm -f "$log"; return 1
    fi
    if ! git -C "$alvo" checkout --quiet "$commit" >"$log" 2>&1; then
      aviso "$pasta: commit $commit nao encontrado; ficou no HEAD"
      git -C "$alvo" checkout --quiet "$(git -C "$alvo" rev-parse HEAD)" >/dev/null 2>&1
    fi
  else
    aviso "$pasta: SEM commit conhecido; baixa o HEAD e a verificacao decide se serve"
    if ! git clone --quiet --depth 1 "$url" "$alvo" >"$log" 2>&1; then
      erro "$pasta: clone falhou"; sed 's/^/        /' "$log" | head -4; rm -f "$log"; return 1
    fi
  fi

  # O material e referencia, nao entra no git do ERP. O .git vai fora.
  if [ -d "$alvo/.git" ]; then
    git -C "$alvo" rev-parse HEAD 2>/dev/null | sed 's/^/        baixado: /'
    rm -rf "$alvo/.git"
  fi
  rm -f "$log"
  return 0
}

verificar_pasta() {
  python3 - "$MANIFESTO" "$DESTINO/$1" "$1" <<'PY'
import hashlib, json, os, sys

manifesto, alvo, nome = sys.argv[1], sys.argv[2], sys.argv[3]
m = json.load(open(manifesto, encoding="utf-8"))
esperado = m["_pastas"][nome]["hashes"]

if not os.path.isdir(alvo):
    print("AUSENTE"); sys.exit(0)

def sha(caminho):
    h = hashlib.sha256()
    with open(caminho, "rb") as f:
        for pedaco in iter(lambda: f.read(1 << 20), b""):
            h.update(pedaco)
    return h.hexdigest()

iguais, diferentes, faltando = 0, [], []
for rel, quer in esperado.items():
    caminho = os.path.join(alvo, rel)
    if not os.path.isfile(caminho):
        faltando.append(rel); continue
    try:
        if sha(caminho) == quer:
            iguais += 1
        else:
            diferentes.append(rel)
    except OSError:
        faltando.append(rel)

extra = 0
for raiz, dirs, nomes in os.walk(alvo):
    dirs[:] = [d for d in dirs if d != ".git"]
    for n in nomes:
        rel = os.path.relpath(os.path.join(raiz, n), alvo)
        if rel not in esperado:
            extra += 1

print("%d %d %d %d" % (iguais, len(diferentes), len(faltando), extra))
if diferentes[:3]:
    for d in diferentes[:3]:
        print("DIF\t%s" % d)
if faltando[:3]:
    for f in faltando[:3]:
        print("FALTA\t%s" % f)
PY
}

# ---------------------------------------------------------------------------
passo "Restaurar microservices"
info "destino:  $DESTINO"
info "manifesto: $MANIFESTO"
info "total de pastas: $(lista_pastas | wc -l | tr -d ' ')"

[ "$LIMPAR" = "1" ] && passo "Limpando (--limpar)"

mkdir -p "$DESTINO"

# As externas vao em paralelo; o resto e pequeno e sequencial.
# Declaradas vazias de proposito: com `set -u` e um filtro --so que nao
# seleciona nenhuma pasta de um grupo, um array sem atribuicao estoura o
# shell com "variavel nao associada". Foi o que aconteceu no primeiro teste.
externas=(); oficiais=(); nossas=()
while IFS= read -r p; do
  quer "$p" || continue
  t="$(campo "$p" tipo)"
  case "$t" in
    repositorio-externo) externas+=("$p") ;;
    oficial-sem-repo)    oficiais+=("$p") ;;
    *)                   nossas+=("$p") ;;
  esac
done < <(lista_pastas)

# --- externas -------------------------------------------------------------
if [ ${#externas[@]} -gt 0 ]; then
  passo "Repositorios externos (${#externas[@]})"
  for p in "${externas[@]}"; do
    if [ "$FORCAR" = "1" ] || [ "$LIMPAR" = "1" ]; then
      limpar_pasta "$p"
    elif [ -d "$DESTINO/$p" ]; then
      info "$p: ja existe, pulando (use --forcar para rebaixar)"
      continue
    fi
    url="$(campo "$p" url)"
    commit="$(campo "$p" commit_conhecido)"
    [ -n "$url" ] || { erro "$p: sem URL no manifesto"; continue; }
    baixar_repo "$p" "$url" "$commit" &
    while [ "$(jobs -rp | wc -l | tr -d ' ')" -ge "$JOBS" ]; do sleep 0.2; done
  done
  wait
fi

# --- sem repositorio ------------------------------------------------------
if [ ${#officiais[@]} -gt 0 ]; then
  passo "Material oficial, sem repositorio (${#officiais[@]})"
  for p in "${officiais[@]}"; do
    if [ -d "$DESTINO/$p" ] && [ "$FORCAR" != "1" ] && [ "$LIMPAR" != "1" ]; then
      info "$p: ja existe"
    else
      aviso "$p: NAO E REPOSITORIO. Tem que baixar na mao:"
    fi
    detalhe="$(campo "$p" detalhe)"
    echo "$detalhe" | python3 -c "
import json, sys
try:
    d = json.loads(sys.stdin.read())
    for k, v in d.items():
        print('           %-12s %s' % (k + ':', v))
except Exception:
    pass
"
  done
  aviso "Estas duas nao tem git porque sao documento oficial. Nao tem como automatizar."
fi

# --- codigo nosso ---------------------------------------------------------
if [ ${#nossas[@]} -gt 0 ]; then
  passo "Codigo do Brasil SaaS (${#nossas[@]}) - vive no git do ERP"
  for p in "${nossas[@]}"; do
    commits="$(campo "$p" detalhe)"
    info "$p: $commits"
    aviso "$p: se a pasta sumiu, restoring do git do ERP:"
    echo "           git checkout <commit> -- src/main/resources/microservices/$p"
  done
fi

# --- verificacao ----------------------------------------------------------
if [ "$VERIFICAR" = "1" ]; then
  passo "Verificacao por SHA-256"
  declare -A RES=( [bate]=0 [diverge]=0 [ausente]=0 [faltando]=0 )
  while IFS= read -r p; do
    quer "$p" || continue
    r="$(verificar_pasta "$p" 2>/dev/null | head -1)"
    if [ -z "$r" ]; then
      erro "$p: nao verificavel"; continue
    fi
    if [ "$r" = "AUSENTE" ]; then
      aviso "$p: ausente"; RES[ausente]=$(( ${RES[ausente]} + 1 )); continue
    fi
    set -- $r
    ig="$1"; di="$2"; fa="$3"; ex="$4"
    if [ "$di" = "0" ] && [ "$fa" = "0" ]; then
      ok "$p: $ig arquivos, bate com o manifesto"
      RES[bate]=$(( ${RES[bate]} + 1 ))
    else
      erro "$p: $di diferente(s), $fa faltando(s), $ig ok"
      RES[diverge]=$(( ${RES[diverge]} + 1 ))
      verificar_pasta "$p" 2>/dev/null | grep -E '^(DIF|FALTA)' | head -3 | sed 's/^/          /'
    fi
  done < <(lista_pastas)

  passo "Resumo"
  echo "  ${V}${RES[bate]} bate(s)${N}   ${A}${RES[diverge]} diverge(n)${N}   ${R}${RES[ausente]} ausente(s)${N}"
  if [ "${RES[diverge]}" != "0" ] || [ "${RES[ausente]}" != "0" ]; then
    echo
    aviso "Divergencia NAO e erro do script: e o repositorio ter mudado depois da copia."
    aviso "Para as pastas sem commit conhecido isso e esperado. Olhe o diff antes de aceitar."
    echo
    info "Para fixar uma pasta, copie a versao boa por cima e regenere o manifesto:"
    info "  python3 scripts/gerar_manifesto.py"
  fi
else
  passo "Verificacao pulada (--sem-verificar)"
fi

passo "Fim"
info "As pastas oficiais continuam de mao: PL_MDFe_300b e NFSe-SaoPaulo-SP."
info "O pom.xml exclui microservices/** do empacotamento; nada disso vai para o JAR."
