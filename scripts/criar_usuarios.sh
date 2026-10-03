#!/bin/bash
# Cria usuarios pela API, e nao por SQL.
#
# Por que nao por SQL: a senha precisa de BCrypt, e o hash so sai do proprio
# aplicativo. Montar o BCrypt na mao exigiria o mesmo gerador, na mesma versao
# de custo, e qualquer divergencia faz o login recusar a senha sem mensagem.
# Indo pela API, o ERP grava o hash que ele mesmo sabe ler.
#
# Depois que o jar novo estiver no ar, o caminho preferivel e a tela de
# Usuarios. Este script existe para quando a tela nao esta acessivel — mas
# exige o jar novo rodando, porque as rotas de criar usuario nao existiam
# antes.
#
# Como usar:
#   bash scripts/criar_usuarios.sh
#
# Para a senha: BRASIL_SAAS_SENHA_FELIPE e BRASIL_SAAS_SENHA_SRVCLOUD, ou
# BRASIL_SAAS_SENHA_PADRAO para os dois. Essas variaveis estao no arquivo
# .senhas.usuarios, que NAO esta no git de proposito.
#
# Como usar:
#   source ./.senhas.usuarios
#   BRASIL_SAAS_SENHA_ADMIN='a senha do euripedes' bash scripts/criar_usuarios.sh

set -euo pipefail

HOST="${BRASIL_SAAS_HOST:-http://127.0.0.1:8080}"
ADMIN="${BRASIL_SAAS_ADMIN:-euripedes}"
SENHA_ADMIN="${BRASIL_SAAS_SENHA_ADMIN:?a senha do admin e obrigatoria}"
SENHA_PADRAO="${BRASIL_SAAS_SENHA_PADRAO:-TrocaEssa#2026}"

export LC_ALL=C LANG=C

echo "==> autenticando como $ADMIN em $HOST"
TOKEN=$(curl -s --max-time 20 -X POST "$HOST/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$ADMIN\",\"password\":\"$SENHA_ADMIN\"}" \
  | grep -oE '"accessToken":"[^"]+' | cut -d'"' -f4 || true)

if [ -z "$TOKEN" ]; then
  echo "ERRO: nao consegui token. O usuario existe? A senha esta certa?" >&2
  exit 1
fi
echo "    ok"

# criar <username> <nome> <email> <perfil> <senha>
criar() {
  local usuario="$1" nome="$2" email="$3" perfil="$4" senha="$5"

  echo "==> criando $usuario (perfil $perfil)"
  local corpo="{\"username\":\"$usuario\",\"nome\":\"$nome\",\"email\":\"$email\",\"senha\":\"$senha\",\"perfil\":\"$perfil\",\"ativo\":true}"
  local resposta
  resposta=$(curl -s -w '\n%{http_code}' --max-time 30 -X POST "$HOST/api/superadmin/usuarios" \
    -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "$corpo" || true)

  local codigo="${resposta##*$'\n'}" corpo_resp="${resposta%$'\n'*}"
  case "$codigo" in
    200|201) echo "    criado (id $(printf '%s' "$corpo_resp" | grep -oE '"id":[0-9]+' | head -1 | cut -d: -f2))" ;;
    409)     echo "    ja existe, pulando" ;;
    *)       echo "    FALHOU ($codigo): $corpo_resp" >&2; return 1 ;;
  esac
}

criar "felipe-ti" "Felipe TI"        "felipe.ti@srvcloud.cloud"  "ADMIN" "${SENHA_FELIPE:-$SENHA_PADRAO}"
criar "srvcloud"  "Servidor SRVCLOUD" "srvcloud@srvcloud.cloud"  "ADMIN" "${SENHA_SRVCLOUD:-$SENHA_PADRAO}"

echo
echo "Listando para conferir:"
curl -s --max-time 20 "$HOST/api/superadmin/usuarios" \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -c "
import json, sys
try:
    d = json.load(sys.stdin)
except Exception as e:
    print('  nao deu para ler a lista:', e)
    raise SystemExit(1)
for u in (d if isinstance(d, list) else d.get('data', [])):
    perfis = ','.join(p.get('nome', '') for p in (u.get('perfis') or [])) or 'SEM PERFIL'
    print('  %-16s %-12s empresa=%-5s ativo=%-6s %s' % (
        u.get('username'), (u.get('nome') or '')[:12],
        u.get('empresaId'), u.get('ativo'), perfis))
"
