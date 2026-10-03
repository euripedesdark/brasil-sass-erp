#!/bin/bash
# Expoe o ERP na internet por Tailscale Funnel, sem dominio e sem abrir porta.
#
# POR QUE TAILSCALE E NAO CLOUDFLARE NESTE CASO
# ---------------------------------------------
# O Cloudflare Tunnel "de verdade" exige um dominio seu apontado para a
# Cloudflare. Sem dominio proprio, sobra so o Quick Tunnel
# (trycloudflare.com), que da uma URL ALEATORIA: muda a cada reinicio da
# maquina. Isso nao serve para um ERP, porque o endereco que voce fechou com
# o cliente, o que esta no DNS, o que o usuario digita — tudo mudaria a cada
# reboot. O ngrok no plano gratis tem o mesmo problema.
#
# O Tailscale Funnel nao usa dominio: ele usa o *.ts.net, que a propria
# Tailscale registra e assina com certificado. O endereco fica estavel,
# o HTTPS e' de verdade, e nada e' aceito de dentro para fora no seu
# roteador — entao o firewall pode continuar fechado.
#
# COMO FICA A CADEIA
# ------------------
#   visitante --https--> Tailscale (443) --> tailscaled (funnel)
#                                                   |
#                                          127.0.0.1:80
#                                                   |
#                                              nginx (porta 80)
#                                                   |
#                                          127.0.0.1:8080
#                                                   |
#                                                   ERP
#
# O tailscaled fala so com o nginx. Ele nao sabe que o 8080 existe, e nao
# precisa saber: o nginx continua sendo o dono da porta 80 e continua
# redirecionando para o 8080. Nada da arquitetura que ja funciona muda.
#
# O Funnel so publica 443, 8443 e 10000. Por isso o escutavel e' o 443 e o
# destino e' a 80 — que e' exatamente o seu caso, ja que o nginx esta na 80.
#
# O QUE PRECISA DE VOCE (nao da para adivinhar)
# ---------------------------------------------
#   1. Uma conta no Tailscale: https://login.tailscale.com
#   2. Autorizar esta maquina: ele vai abrir uma URL de confirmacao
#   3. Habilitar o Funnel na politica da tailnet (o script mostra o trecho)
#
# O passo 3 e' o que casi com a maioria das pessoas. A partir de 2024 a
# Tailscale exige que o Funnel seja liberado explicitamente no arquivo de
# politica da tailnet, senao o comando responde que o Funnel esta desativado.
#
# ANTES DE RODAR: o que isso abre para a internet esta em discussao no final
# do script. Rodando este script a URL publica passa a responder.
#
# COMO USAR
# ---------
#   sudo bash scripts/tunel_tailscale.sh          # instala e sobe
#   sudo bash scripts/tunel_tailscale.sh --so-instalar   # so a instalacao
#   sudo bash scripts/tunel_tailscale.sh --diagnostico   # so conferir

set -euo pipefail

DOMINIO_FUNNEL="${BRASIL_SAAS_DOMINIO:-}"
PORTA_FUNNEL="${BRASIL_SAAS_PORTA_FUNNEL:-443}"
PORTA_LOCAL="${BRASIL_SAAS_PORTA_LOCAL:-80}"
EMAIL_NOTIF="${BRASIL_SAAS_EMAIL:-}"
MODO="${1:-}"

export DEBIAN_FRONTEND=noninteractive

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

# ---------------------------------------------------------------- funcoes
conferir() {
    local FALHA=""
    command -v tailscale >/dev/null 2>&1 || FALHA="o tailscale nao esta instalado"
    systemctl is-active tailscaled >/dev/null 2>&1 || FALHA="o tailscaled nao esta rodando"
    [ -z "$FALHA" ] && tailscale status >/dev/null 2>&1 || FALHA="esta maquina ainda nao foi autorizada na tailnet"

    echo "--- diagnostico"
    echo "    tailscale:  $(tailscale version 2>/dev/null | head -1 || echo 'nao instalado')"
    echo "    tailscaled: $(systemctl is-active tailscaled 2>/dev/null || echo parado)"
    echo "    autorizado: $(tailscale status --json 2>/dev/null | grep -o '"BackendState": *"[A-Za-z]*"' | cut -d'"' -f4 || echo 'nao sei')"
    echo "    tailscale serve/funnel:"
    tailscale serve status 2>/dev/null | sed 's/^/      /' | head -8 || echo "      (nada configurado)"
    echo "    nginx em 127.0.0.1:$PORTA_LOCAL: $(curl -s -o /dev/null -w '%{http_code}' --max-time 6 "http://127.0.0.1:$PORTA_LOCAL/" || echo 'sem resposta')"
    [ -n "$FALHA" ] && { echo "    ATENCAO: $FALHA"; return 0; }
    return 0
}

if [ "$MODO" = "--diagnostico" ]; then
    conferir
    exit 0
fi

# ---------------------------------------------------------------- 1. nginx
echo "==> 1. conferindo o nginx em 127.0.0.1:$PORTA_LOCAL"
CODIGO=$(curl -s -o /dev/null -w '%{http_code}' --max-time 8 "http://127.0.0.1:$PORTA_LOCAL/" || echo 000)
if [ "$CODIGO" = "000" ]; then
    echo "ERRO: o nginx nao responde em 127.0.0.1:$PORTA_LOCAL." >&2
    echo "      O funnel aponta para o nginx. Sem nginx, a URL publica fica em" >&2
    echo "      branco. Suba antes: systemctl start nginx" >&2
    exit 1
fi
echo "    respondeu HTTP $CODIGO"
echo "    (o 80 -> 8080 e' do nginx; este script nao mexe nisso)"

# ---------------------------------------------------------------- 2. instala
echo "==> 2. instalando o tailscale"
if ! command -v tailscale >/dev/null 2>&1; then
    if ! curl -fsSL --max-time 120 https://tailscale.com/install.sh | sh; then
        echo "ERRO: a instalacao falhou." >&2
        echo "      Alternativa por pacote:" >&2
        echo "        curl -fsSL https://pkgs.tailscale.com/stable/ubuntu/noble.noarmor.gpg |" >&2
        echo "          sudo tee /usr/share/keyrings/tailscale-archive-keyring.gpg >/dev/null" >&2
        echo "        echo 'deb [signed-by=/usr/share/keyrings/tailscale-archive-keyring.gpg]" >&2
        echo "          https://pkgs.tailscale.com/stable/ubuntu noble main' |" >&2
        echo "          sudo tee /etc/apt/sources.list.d/tailscale.list" >&2
        echo "        sudo apt-get update && sudo apt-get install -y tailscale" >&2
        exit 1
    fi
fi
systemctl enable --now tailscaled >/dev/null 2>&1
echo "    $(tailscale version 2>/dev/null | head -1)"

# ---------------------------------------------------------------- 3. autoriza
echo "==> 3. autorizando esta maquina"
ESTADO=$(tailscale status --json 2>/dev/null | grep -o '"BackendState": *"[A-Za-z]*"' | cut -d'"' -f4)
if [ "$ESTADO" = "Running" ]; then
    echo "    ja autorizada"
else
    echo "    Preciso que voce abra o link abaixo no navegador e clique em"
    echo "    'Connect'. Sem isso a maquina nao entra na tailnet:"
    echo
    tailscale up --timeout=8s 2>&1 | grep -oE 'https://login\.tailscale\.com/[a-zA-Z0-9/._-]+' | head -1 | sed 's/^/      /'
    echo
    echo "    Depois de autorizar, rode este script de novo."
    exit 1
fi

# ---------------------------------------------------------------- 4. funcao do host
echo "==> 4. nome publico da maquina"
if [ -n "$DOMINIO_FUNNEL" ]; then
    tailscale set --hostname="$DOMINIO_FUNNEL" 2>/dev/null && echo "    hostname definido: $DOMINIO_FUNNEL"
else
    HOST=$(tailscale status --json 2>/dev/null | grep -o '"DNSName": *"[^"]*"' | head -1 | cut -d'"' -f4)
    HOST="${HOST%.}"
    echo "    usando o nome automatico: $HOST"
fi

# ---------------------------------------------------------------- 5. liga o funnel
echo "==> 5. ligando o funnel na $PORTA_FUNNEL -> 127.0.0.1:$PORTA_LOCAL"
if ! tailscale funnel --bg "$PORTA_FUNNEL" 2>/tmp/funnel.err; then
    echo "ERRO: o funnel nao ligou. A mensagem do tailscale:" >&2
    sed 's/^/      /' /tmp/funnel.err >&2
    echo >&2
    cat <<DICA
      As duas causas mais comuns:

      1) O Funnel esta desativado na politica da tailnet. Em 2024 a Tailscale
         passou a exigir liberacao explicita. Abra, no painel, em
         https://login.tailscale.com/admin/policies e acrescente ao ACL:

             "nodeAttrs": [
               {
                 "target": ["autogroup:member"],
                 "attr":   ["funnel"]
               }
             ]

         E salve. Sem isso a resposta e "funnel is disabled on your tailnet".

      2) Voce esta numa versao antiga. Atualize:

             sudo apt-get update && sudo apt-get install -y --only-upgrade tailscale
DICA
    exit 1
fi

# ---------------------------------------------------------------- 6. confere
echo "==> 6. conferindo"
URL=$(tailscale funnel status 2>/dev/null | grep -oE 'https://[a-zA-Z0-9.-]+\.ts\.net' | head -1)
tailscale serve status 2>/dev/null | sed 's/^/    /' | head -6

if [ -z "$URL" ]; then
    echo "    o funnel subiu, mas nao achei a URL em 'funnel status'."
    echo "    O endereco completo esta em: https://login.tailscale.com/admin/dns"
    exit 0
fi

echo
echo "    URL publica: $URL"
echo "    porta publica: $PORTA_FUNNEL   destino local: 127.0.0.1:$PORTA_LOCAL (nginx)"
echo

echo "    Teste de dentro, para ver se o tunnel esta inteiro:"
echo "      curl -I $URL"
echo
echo "    O endereco e' estavel: nao muda quando a maquina reinicia. Nao e' como"
echo "    o ngrok gratis nem o trycloudflare, que trocam de URL a cada boot."
echo

# ---------------------------------------------------------------- 7. avisar
cat <<'AVISO'

==> O QUE ACABOU DE ABRIR PARA A INTERNET

O ERP agora responde num endereco publico, e qualquer pessoa com o link e um
navegador chega nele. Nao precisa de Tailscale, nao precisa instalar nada.
E assim que precisa ser, se varias pessoas sao para usar.

Antes de mandar o link para alguem, confira estas tres coisas:

  1. O login aceita tentativas ilimitadas
     /api/auth/login nao trava. O banco bloqueia por tentativas, mas veja em
     quantas e se ha usuario com contador alto:
         SELECT username, tentativas_login, bloqueado_ate
           FROM brasil_saas.bc_core_usuario
          WHERE tentativas_login > 0 OR bloqueado_ate IS NOT NULL;

  2. Quem tem SUPERUSER ve e mexe em todas as empresas
         SELECT u.username, p.nome
           FROM brasil_saas.bc_core_usuario u
           JOIN brasil_saas.bc_core_usuario_perfil up ON up.usuario_id = u.id
           JOIN brasil_saas.bc_core_perfil p ON p.id = up.perfil_id
          WHERE p.nome IN ('SUPERUSER','SUPERADMIN');

  3. O furo de IDOR dos relatorios
     Os controllers de relatorio recebem empresaId do cliente e concatenam no
     SQL. Quem chamar pode pedir dados de outra empresa. Isso ja existia
     antes desta exposicao, mas o alcance passa a ser a internet inteira.

Sobre criptografia: o tailscaled termina o TLS, e o trecho do tailscaled ate
o nginx (127.0.0.1) nao criptografa porque nao sai da maquina.

Se quiser reduzir quem chega no ERP sem mudar a URL, o caminho e por um
autenticador na frente — por exemplo Cloudflare Access, que e' gratuito e
faz login antes de entregar a pagina. Com isso a URL publica continua
existem, mas ninguem ve o ERP sem se identificar antes.

Uma coisa que o Funnel NAO faz, e que vale saber: ele so publica para fora o
que voce apontar. Quem tem Tailscale na sua tailnet enxerga a rede local
inteira, mas quem chega so pelo link publico do Funnel enxerga apenas o
servico publicado — no seu caso, so o nginx.

AVISO

echo "Feito."
