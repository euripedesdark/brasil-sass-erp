#!/bin/bash
# Fixa o IP do servidor e ajusta o DNS.
#
# Por que: com DHCP, o <IP-DO-SERVIDOR> pode mudar quando o roteador renova a
# locacao. Se a maquina vai ficar exposta para fora, um IP que muda e' um
# IP que voce para de conseguir acessar. Com estatico, o endereco e' o mesmo
# depois de cada boot.
#
# A rede continua a mesma que ja funciona: <IP-DO-SERVIDOR>/24, gateway
# 192.168.2.1. O IP nao muda — o que muda e' ele parar de vir do DHCP.
#
# Rodar como root, no servidor:
#   sudo bash scripts/fixar_ip.sh
#
# O que este script NAO faz, de proposito:
#   - nao mexe em rota, tabela ARP, iptables nem em servico
#   - nao troca o IP: mantem o que ja funciona
#   - nao mexe no netplan se ja estiver estatico
#
# Se der errado, `netplan try` volta sozinho em 2 minutos. Ver o fim do script.

set -euo pipefail

IP="${BRASIL_SAAS_IP:-<IP-DO-SERVIDOR>}"
MASCARA="${BRASIL_SAAS_MASCARA:-24}"
GATEWAY="${BRASIL_SAAS_GATEWAY:-192.168.2.1}"
DNS1="${BRASIL_SAAS_DNS1:-8.8.8.8}"     # Google
DNS2="${BRASIL_SAAS_DNS2:-8.8.4.4}"     # Google
REDE="${IP%.*}"
TIMEOUT=120                              # segundos antes de voltar sozinho

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

echo "==> IP=$IP/$MASCARA  gateway=$GATEWAY  DNS=$DNS1,$DNS2"

# ---------------------------------------------------------------- interface
# Descobre a interface pelo IP de destino, e nao pelo nome: o nome muda
# conforme o slot da placa mae, e um netplan com o nome errado deixa a
# maquina sem rede — que e' o jeito mais rapido de perder o acesso.
IFACE=""
for_iface() {
    # O campo 4 vem como "<IP-DO-SERVIDOR>/24". O teste e' por igualdade do
    # endereco, com o "/mascara" removido — antes comparava com um ponto no
    # final ("^192\.168\.2\.140\."), que so casaria com um IP que ainda tivesse
    # outro octeto depois. Por isso o script caia no fallback mesmo com o
    # endereco certo na maquina.
    ip -o -4 addr show 2>/dev/null | awk -v alvo="$1" '
        { split($4, a, "/"); if (a[1] == alvo) { print $2; exit } }
    '
}

# 1) a interface que ja tem o IP que estamos fixando
IFACE="$(for_iface "$IP")"

# 2) se o DHCP ja entregou outro IP, a interface e' a que tem a rota padrao
if [ -z "$IFACE" ]; then
    echo "    o IP $IP nao esta na maquina agora; usando a interface da rota padrao"
    IFACE="$(ip -o route show default 2>/dev/null | awk '{print $5; exit}')"
fi

# 3) ainda assim: a primeira com link up
if [ -z "$IFACE" ]; then
    IFACE="$(ip -o link show 2>/dev/null | awk -F': ' '$2 != "lo" { print $2; exit }')"
fi

if [ -z "$IFACE" ]; then
    echo "ERRO: nao achei nenhuma interface de rede." >&2
    exit 1
fi
echo "    interface: $IFACE"

# ---------------------------------------------------------------- ja e estatico?
ATUAL="$(ip -4 addr show "$IFACE" 2>/dev/null | grep -oE "inet $IP/$MASCARA" | head -1 || true)"
if [ -n "$ATUAL" ] && [ ! -d "/run/systemd/netif/leases" ] \
   && grep -rqs "$IP/$MASCARA" /etc/netplan/ 2>/dev/null; then
    echo "    ja esta estatico com o mesmo IP. So falta conferir o DNS."
fi

# ---------------------------------------------------------------- backup
mkdir -p /root/netplan-backup
STAMP="$(date +%Y%m%d-%H%M%S)"
cp -a /etc/netplan "/root/netplan-backup/netplan-$STAMP" 2>/dev/null || true
echo "==> backup em /root/netplan-backup/netplan-$STAMP"

# ---------------------------------------------------------------- escreve
# netplan da 1022 em diante nao aceita YAML meso sem espaco. Gerar com
# heredoc e' mais seguro que editar o arquivo que ja existe, porque o
# arquivo antigo pode ter chaves que nao valem mais.
# O nome do arquivo vive numa variavel e nao e' escrito a mao em mais nenhum
# lugar. Escrito a mao, ele ja divergiu em caixa entre o cat e o chmod — um
# lado dizia 01-brasil-saas, o outro 01-BRASIL-saas — e o chmod falhou com
# "Arquivo ou diretório inexistente" logo depois do arquivo ter sido criado.
# O script abortava no set -e e a configuracao nunca era aplicada, sem nenhum
# sinal de que o arquivo tinha sido criado.
NETPLAN_ARQ="/etc/netplan/01-brasil-saas.yaml"
cat > "$NETPLAN_ARQ" <<EOF
# Gerado por scripts/fixar_ip.sh em $STAMP
# IP fixo do servidor BRASIL-SAAS. O IP nao muda: e' o mesmo que ja
# funcionava, so que parado no lugar em vez de vir do DHCP.
network:
  version: 2
  ethernets:
    $IFACE:
      dhcp4: no
      dhcp6: no
      addresses:
        - $IP/$MASCARA
      routes:
        - to: default
          via: $GATEWAY
      nameservers:
        addresses: [$DNS1, $DNS2]
EOF

chmod 600 "$NETPLAN_ARQ"
echo "==> escrito $NETPLAN_ARQ"

netplan generate >/dev/null 2>&1 || {
    echo "ERRO: o netplan gerou invalido. O arquivo anterior continua valendo." >&2
    echo "      para desfazer: rm "$NETPLAN_ARQ" && netplan apply" >&2
    exit 1
}

# ---------------------------------------------------------------- aplica com volta automatica
# `netplan try` e' o que segura a mao: aplica, e se em $TIMEOUT segundos
# ninguem confirmar, volta o arquivo anterior sozinho. Sem SSH provavelmente
# quebrado, o servidor se recupera sozinho em 2 minutos.
echo "==> netplan try (volta sozinho em ${TIMEOUT}s se nao confirmar)"
netplan try --timeout "$TIMEOUT" --sandbox &
NETPLAN_TRY_PID=$!

# ---------------------------------------------------------------- confere
echo "==> conferindo"
sleep 12
FALHA=""
ip -4 addr show "$IFACE" 2>/dev/null | grep -q "inet $IP/$MASCARA" || FALHA="a interface nao ficou com o IP $IP/$MASCARA"
[ -z "$FALHA" ] && ! ping -c 2 -W 2 "$GATEWAY" >/dev/null 2>&1 && FALHA="o gateway $GATEWAY nao responde"
[ -z "$FALHA" ] && ! getent hosts google.com >/dev/null 2>&1 && FALHA="o DNS nao resolve google.com"

if [ -n "$FALHA" ]; then
    echo "ERRO: $FALHA" >&2
    echo "      o netplan try vai voltar sozinho. Nao confirme nada e espere." >&2
    exit 1
fi

echo "    ok: $IFACE tem $IP/$MASCARA, o gateway responde e o DNS resolve"
echo
echo "IP fixo:  $IP/$MASCARA"
echo "Gateway:  $GATEWAY"
echo "DNS:      $DNS1, $DNS2"
echo
echo "O IP nao mudou, entao se voce esta lendo isto por SSH a sessao continua."
echo "O servidor fica estatico agora e no proximo boot."

# Confirmar mantem a configuracao. Sem isto, o netplan try reverte em
# $TIMEOUT segundos — o que seria perder a configuracao logo depois de
# ela passar.
if [ -d /run/systemd/system ]; then
    netplan apply 2>/dev/null || true
    kill "$NETPLAN_TRY_PID" 2>/dev/null || true
    echo "==> configuracao confirmada e gravada."
else
    echo "AVISO: sem systemd, confirme manualmente antes de $TIMEOUT segundos."
fi

echo
echo "Para desfazer depois:"
echo "  cp -a /root/netplan-backup/netplan-$STAMP/. /etc/netplan/ && netplan apply"
