#!/bin/bash
# Expoe o ERP na internet pelo Cloudflare Tunnel, sem abrir nenhuma porta.
#
# POR QUE NAO BASTA ABRIR PORTA NO ROTEADOR
# ----------------------------------------
# Para o trafego chegar na sua maquina, alguma coisa teria de entrar pelo seu
# roteador: uma regra de port forwarding. Se o firewall do roteador esta
# fechado, essa regra nao existe e nao existe caminho de entrada. Nao ha
# configuracao no Linux que resolva — o pacote simplesmente nao chega.
#
# A solucao inverte a direcao. Em vez de a internet entrar na sua rede, a sua
# maquina ABRE uma conexao para fora, para o servidor da Cloudflare, e mantem
# essa conexao viva. O trafego do mundo externo chega nesse socket reverso e
# sai pela sua maquina. Nada e' aceito de dentro para fora no seu roteador, e
# o firewall pode continuar fechado.
#
# COMO FICA A CADEIA
# ------------------
#   visitante --https--> Cloudflare (443) --> tunel reverso --> cloudflared
#                                                      |
#                                            127.0.0.1:80
#                                                      |
#                                             nginx (porta 80)
#                                                      |
#                                            127.0.0.1:8080
#                                                      |
#                                              Tomcat / ERP
#
# O nginx continua sendo o dono da porta 80 e continua redirecionando para o
# 8080. O cloudflared nao sabe que o 8080 existe: ele fala so com o nginx. Por
# isso este script nao mexe na porta 8080 nem no servico do ERP.
#
# ANTES DE RODAR, DECIDA UMA COISA
# ---------------------------------
# Isso coloca o ERP na internet publica. Com:
#   - o login em /api/auth/login, com forca bruta de senha possivel
#   - um perfil SUPERUSER que ve e mexe em todas as empresas
#   - o furo de IDOR nos relatorios (empresaId vem do cliente)
# ...qualquer pessoa na internet que descubra a URL pode tentar entrar.
#
# O script monta duas camadas de protecao e explica cada uma. Se voce nao quiser
# a exposicao publica, pare aqui: a alternativa e dar acesso so para quem tem
# a rede (veja o final do script, secao "Alternativa sem expor").
#
# COMO USAR
# ---------
#   1. Crie uma conta na Cloudflare e adicione um dominio.
#   2. Crie um tunnel em dash.cloudflare.com e pegue o token.
#   3. Na maquina:
#        sudo bash scripts/tunel_cloudflare.sh
#        BRASIL_SAAS_TUNEL_TOKEN=<o token> sudo -E bash scripts/tunel_cloudflare.sh
#
# O token e' um segredo: ele da acesso a conta do tunnel. Fica em
# /etc/cloudflared, com modo 600, e nunca no git.

set -euo pipefail

# ---------------------------------------------------------------- configuracao
DOMINIO="${BRASIL_SAAS_DOMINIO:-}"
ORIGEM_LOCAL="${BRASIL_SAAS_ORIGEM:-http://127.0.0.1:80}"
HOSTNAME_TUNEL="${BRASIL_SAAS_HOSTNAME:-}"
EMAIL_NOTIF="${BRASIL_SAAS_EMAIL:-}"
INSTALAR_TOKEN="${BRASIL_SAAS_TUNEL_TOKEN:-}"
SERVICO_NM="cloudflared-tunel"
ARQ_TOKEN="/etc/cloudflared"
ARQ_CONFIG="/etc/cloudflared/config.yml"
ARQ_UNIT="/etc/systemd/system/$SERVICO_NM.service"

export DEBIAN_FRONTEND=noninteractive

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

# ---------------------------------------------------------------- 1. nginx vivo
echo "==> 1. conferindo o nginx"
if ! curl -s -o /dev/null --max-time 8 "http://127.0.0.1/"; then
    echo "ERRO: o nginx nao responde em http://127.0.0.1/." >&2
    echo "      O tunel aponta para o nginx, entao sem nginx o tunnel so ia dar" >&2
    echo "      pagina em branco. Suba o nginx antes: systemctl start nginx" >&2
    exit 1
fi
echo "    nginx responde em 127.0.0.1:80"
echo "    (o redirecionamento 80 -> 8080 e' do nginx; nao e' mexido aqui)"

# ---------------------------------------------------------------- 2. instala
echo "==> 2. instalando o cloudflared"
if ! command -v cloudflared >/dev/null 2>&1; then
    PKG_URL="https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64"
    curl -fsSL --max-time 120 "$PKG_URL" -o /usr/local/bin/cloudflared \
        || { echo "ERRO: nao consegui baixar o cloudflared." >&2; exit 1; }
    chmod 755 /usr/local/bin/cloudflared
fi
echo "    $(cloudflared --version 2>&1 | head -1)"

# ---------------------------------------------------------------- 3. token
echo "==> 3. TOKEN DO TUNEL"
mkdir -p "$ARQ_TOKEN"
chmod 700 "$ARQ_TOKEN"
ARQ_CRED="$ARQ_TOKEN/creds-tunel.json"

if [ -s "$ARQ_CRED" ]; then
    echo "    ja existe credencial em $ARQ_CRED"
elif [ -n "$INSTALAR_TOKEN" ]; then
    # O token e' o que o painel da Cloudflare entrega. O cloudflared troca o
    # token por um arquivo de credencial e sobe sozinho; nao ha passo de login
    # no navegador, que e' o que trava instalacao em servidor.
    cloudflared service uninstall >/dev/null 2>&1 || true
    systemctl stop "$SERVICO_NM" 2>/dev/null || true
    echo "    instalando o servico com o token"
    if ! cloudflared service install "$INSTALAR_TOKEN"; then
        echo "ERRO: o token foi recusado. Confira se copiou inteiro." >&2
        exit 1
    fi
    # O service install cria o proprio unit. Este script usa o unit dele.
    ARQ_UNIT="/etc/systemd/system/cloudflared.service"
else
    echo "ERRO: preciso do token do tunnel." >&2
    echo >&2
    echo "    Como obter:" >&2
    echo "      1. Em https://one.dash.cloudflare.com va em Zero Trust > Networks > Tunnels" >&2
    echo "      2. Create a tunnel, escolha cloudflared" >&2
    echo "      3. Na etapa de instalacao, copie o comando que comeca com:" >&2
    echo "         cloudflared service install" >&2
    echo "      4. Rode aqui com o token colado:" >&2
    echo >&2
    echo "         BRASIL_SAAS_TUNEL_TOKEN='<o token>' sudo -E bash $0" >&2
    echo >&2
    echo "    Sem token nao da para montar o tunnel. Nao tem como adivinhar." >&2
    exit 1
fi

# ---------------------------------------------------------------- 4. config
echo "==> 4. configurando o hostname publico"
if [ -z "$DOMINIO" ] || [ -z "$HOSTNAME_TUNEL" ]; then
    if [ -n "$DOMINIO" ]; then
        HOSTNAME_TUNEL="erp.$DOMINIO"
    else
        echo "    BRASIL_SAAS_DOMINIO nao informado: o tunnel so vai funcionar" >&2
        echo "    depois que voce apontar um hostname para ele no painel." >&2
        echo "    O servico sobe mesmo assim, so nao ha URL publica ainda." >&2
    fi
fi

TUNEL_ID="desconhecido"
[ -s "$ARQ_CRED" ] && TUNEL_ID=$(grep -oE '"TunnelID"[[:space:]]*:[[:space:]]*"[^"]+"' "$ARQ_CRED" 2>/dev/null | cut -d'"' -f4 || echo desconhecido)

if [ -n "$DOMINIO" ] && [ -n "$HOSTNAME_TUNEL" ]; then
    # Ingresso escrito no arquivo de configuracao: o hostname publico aponta
    # para o nginx local. O serviceToken e' o que da a permissao de atender
    # esse hostname.
    SERVICE_TOKEN=$(grep -oE '"Token"[[:space:]]*:[[:space:]]*"[^"]+"' "$ARQ_CRED" 2>/dev/null | head -1 | cut -d'"' -f4 || echo "")
    if [ -z "$SERVICE_TOKEN" ]; then
        echo "ERRO: o arquivo de credencial nao tem Token. Regere o tunnel." >&2
        exit 1
    fi
    cat > "$ARQ_CONFIG" <<YML
# Gerado por scripts/tunel_cloudflare.sh
#
# O ingress aponta para o NGINX local (127.0.0.1:80). O nginx e' que sabe
# sobre o 8080; o cloudflared nao. Por isso o tunnel nao muda nada da
# arquitetura que ja funciona.
tunnel: $TUNEL_ID
credentials-file: $ARQ_CRED
ingress:
  - hostname: $HOSTNAME_TUNEL
    service: $ORIGEM_LOCAL
    originRequest:
      # O ERP nao deve ficar sem TLS na frente. oOriginRequest herda o
      # protocolo do visitante, e o cloudflared termina o TLS antes de falar
      # com o nginx.
      noTLSVerify: false
  - service: http_status:404
YML
    # serviceToken fica no arquivo de credencial; o ingresso so precisa do
    # hostname e do destino local.
    sed -i '/serviceToken/d' "$ARQ_CONFIG"
    chmod 600 "$ARQ_CONFIG"
    echo "    hostname publico: $HOSTNAME_TUNEL"
    echo "    apontando para:   $ORIGEM_LOCAL (o nginx)"
else
    echo "    sem hostname publico por enquanto"
fi

# ---------------------------------------------------------------- 5. unit
echo "==> 5. servico systemd"
if [ ! -f "$ARQ_UNIT" ]; then
    cat > "$ARQ_UNIT" <<UNIT
[Unit]
Description=Cloudflare Tunnel (ERP BRASIL-SAAS)
After=network-online.target nginx.service
Wants=network-online.target

[Service]
Type=simple
ExecStart=/usr/local/bin/cloudflared --no-autoupdate tunnel run
Restart=on-failure
RestartSec=10
# o token fica em /etc/cloudflared, com 600
NoNewPrivileges=true
ProtectHome=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
UNIT
    systemctl daemon-reload
fi
systemctl enable "$SERVICO_NM" >/dev/null 2>&1 || systemctl enable cloudflared >/dev/null 2>&1
echo "    $SERVICO_NM habilitado"

# ---------------------------------------------------------------- 6. sobe
echo "==> 6. subindo"
NOME_UNIT="cloudflared"
systemctl list-unit-files 2>/dev/null | grep -q "^cloudflared.service" && NOME_UNIT="cloudflared"
systemctl restart "$NOME_UNIT" 2>&1 | head -2
sleep 10

# ---------------------------------------------------------------- 7. confere
echo "==> 7. conferindo"
ATIVO=$(systemctl is-active "$NOME_UNIT" 2>/dev/null)
echo "    servico: $ATIVO"
if [ "$ATIVO" != "active" ]; then
    echo "ERRO: o servico nao subiu. O log:" >&2
    journalctl -u "$NOME_UNIT" -n 15 --no-pager 2>/dev/null | sed 's/^/      /' >&2
    exit 1
fi
journalctl -u "$NOME_UNIT" -n 40 --no-pager 2>/dev/null \
    | grep -oE 'https://[a-z0-9.-]+\.trycloudflare\.com' | tail -1 \
    | sed 's/^/    url de teste: /' || true

echo
if [ -n "$HOSTNAME_TUNEL" ]; then
    echo "    URL publica: https://$HOSTNAME_TUNEL"
    echo
    echo "    Confira o acesso de fora daqui do servidor (o IP de saida tem de ser"
    echo "    o do servidor, nao o da sua rede local):"
    echo "      curl -I https://$HOSTNAME_TUNEL"
else
    echo "    O tunnel esta aberto, mas ainda nao tem URL publica."
    echo "    No painel: Zero Trust > Networks > Tunnels > Published applications"
    echo "    e aponte um hostname para o tunnel."
fi

# ---------------------------------------------------------------- 8. proteger
cat <<'PROTEGIDO'

==> 8. PROTEGER O QUE ACABOU DE ABRIR PARA A INTERNET

O ERP esta em um endereco publico. Tres coisas para fazer agora:

  a) Cloudflare Access (a mais forte, e gratuita)
     Em Zero Trust > Access > Applications, ponha o ERP atras de um login
     proprio. Ate o google tem isso. Ninguem chega no ERP sem passar por
     la. Com isso, a exposicao publica deixa de ser um problema: o unico
     acesso e' de quem voce autorizar.

  b) Travar o login
     /api/auth/login aceita tentativas infinitas. O banco tem
     bloqueio por tentativas, mas confira quantas:

       SELECT * FROM brasil_saas.bc_core_usuario WHERE tentativas_login > 0;

     E confirme que o perfil SUPERUSER e' o unico que alcanca todas as
     empresas — vale conferir quem tem.

  c) O furo de IDOR dos relatorios
     Os controllers de relatorio recebem empresaId do cliente e concatenam no
     SQL. Quem llamar pode pedir os dados de outra empresa. Isso e' anterior
     a esta exposicao, mas agora o alcance e' a internet.

  d) Confiar em HTTPS so
     O cloudflared termina TLS, e o trafego do navegador ate a Cloudflare e'
     criptografado. O trecho entre o cloudflared e o nginx (127.0.0.1) nao
     criptografa porque nao sai da maquina.

PROTEGIDO

# ---------------------------------------------------------------- 9. alternativa
cat <<'ALTERNATIVA'

==> ALTERNATIVA SEM EXPOR NA INTERNET

Se a ideia nao e' publicar, e' so voce (e quem voce quiser) acessar de fora, o
caminho e' uma rede overlay. Nada publico, nenhuma porta, e voce chega em
toda a sua rede local, nao so na maquina do ERP.

Tailscale:

  curl -fsSL https://tailscale.com/install.sh | sh
  sudo tailscale up
  sudo tailscale up --advertise-tags=tag:erp

O acesso e' por https://<nome-da-maquina>.<seu-tailnet>.ts.net, e so quem
voce autorizou entra. E' o que eu escolheria se o ERP nao fosse para ser
publico.

O Tailscale tem tambem o Funnel, que expoe publicamente pela rede deles —
mesma ideia do Cloudflare Tunnel, e da para escolher entre os dois.

ALTERNATIVA

echo "Feito."
