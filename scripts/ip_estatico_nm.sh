#!/bin/bash
# Passa a rede do servidor para o NetworkManager, com IP fixo.
#
# Por que NM e nao so netplan: o netplan deste servidor usa o renderer padrao,
# que e' o systemd-networkd. O NetworkManager esta instalado, ativo e
# habilitado, mas sem nenhum perfil — e enquanto o networkd segura a
# interface, o NM ve o device como "unmanaged" e recusa a ativacao:
#
#   Error: Connection activation failed: No suitable device found for this
#   connection (device wlp2s0 not available because profile is not compatible
#   with device (connection type is not "802-11-wireless"))
#
# Repara no wlp2s0: sem interface amarra, o NM tries usar a primeira que
# encontra, e a primeira e' a do wifi. Com a interface amarrada e o renderer
# trocado, ele usa a certa.
#
# O IP nao muda: <IP-DO-SERVIDOR> ja e' o de hoje. O que muda e' ele parar de vir
# do DHCP, que e' o que faz o endereco mudar quando o roteador renova a
# locacao — e ai comecam as horas perdidas tentando acessar a maquina.
#
# Rodar como root:
#   sudo bash scripts/ip_estatico_nm.sh
#
# A rede nao e derrubada no meio: o perfil novo e' criado antes de qualquer
# mudanca, e o IP continua o mesmo durante a troca. Se der problema, o
# backup em /root/nm-backup/ desfaz.

set -euo pipefail

IFACE="${BRASIL_SAAS_IFACE:-enp3s0f1}"
IP="${BRASIL_SAAS_IP:-<IP-DO-SERVIDOR>}"
MASCARA="${BRASIL_SAAS_MASCARA:-24}"
GATEWAY="${BRASIL_SAAS_GATEWAY:-192.168.2.1}"
DNS1="${BRASIL_SAAS_DNS1:-8.8.8.8}"
DNS2="${BRASIL_SAAS_DNS2:-8.8.4.4}"
CONEXAO="${BRASIL_SAAS_CONEXAO:-brasil-saas}"

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

command -v nmcli >/dev/null 2>&1 || { echo "ERRO: nmcli nao instalado." >&2; exit 1; }
[ -d "/sys/class/net/$IFACE" ] || {
    echo "ERRO: a interface $IFACE nao existe nesta maquina." >&2
    echo "      As interfaces aqui sao: $(ls /sys/class/net | grep -v lo | tr '\n' ' ')" >&2
    exit 1
}

# ---------------------------------------------------------------- backup
mkdir -p /root/nm-backup
STAMP="$(date +%Y%m%d-%H%M%S)"
nmcli -t -f NAME,UUID,TYPE con show > "/root/nm-backup/conexoes-$STAMP.txt" 2>/dev/null || true
cp -a /etc/netplan "/root/nm-backup/netplan-$STAMP" 2>/dev/null || true
echo "==> backup em /root/nm-backup ($STAMP)"

# ---------------------------------------------------------------- netplan -> NM
# O renderer e' o que decide quem controla a interface. Sem esta linha, o
# netplan usa o networkd e o NM nunca ve o device.
#
# O nome do arquivo e' uma variavel e nao aparece escrito em mais nenhum
# lugar: escrito a mao, ja divergiu em caixa entre o cat e o chmod e o script
# morreu depois de criar o arquivo, parecendo que nada tinha acontecido.
NETPLAN_ARQ="/etc/netplan/01-brasil-saas.yaml"
cat > "$NETPLAN_ARQ" <<EOF
# Gerado por scripts/ip_estatico_nm.sh em $STAMP
# O renderer e' NetworkManager: e' o NM que passa a controlar a interface e a
# guardar o perfil. Sem ele, o networkd segura o device, o NM ve como
# "unmanaged" e recusa a ativacao.
network:
  version: 2
  renderer: NetworkManager
  ethernets:
    $IFACE:
      dhcp4: no
      dhcp6: no
      addresses: [$IP/$MASCARA]
      routes:
        - to: default
          via: $GATEWAY
      nameservers:
        addresses: [$DNS1, $DNS2]
EOF
chmod 600 "$NETPLAN_ARQ"
netplan generate 2>/dev/null \
    || { echo "ERRO: netplan generate falhou; o arquivo anterior continua valendo." >&2; exit 1; }
echo "==> netplan com renderer NetworkManager"

# ---------------------------------------------------------------- perfil no NM
# Criar ou corrigir o perfil. `nmcli con mod` em perfil que nao existe falha, e
# `nmcli con add` em perfil que existe tambem — entao o script escolhe os dois
# caminhos em vez de assumir um.
if nmcli -t -f NAME con show 2>/dev/null | grep -qx "$CONEXAO"; then
    echo "==> perfil '$CONEXAO' ja existe; corrigindo"
    # O "nmcli" precisa ficar fora da variavel: se a variavel guardasse
    # "con mod brasil-saas" e fosse expandida como comando, o shell tentaria
    # executar "con", que nao existe, e o script morria em
    # "con: command not found". A variavel guarda so os argumentos.
    ALVO="mod $CONEXAO"
else
    echo "==> criando o perfil '$CONEXAO'"
    ALVO="add type ethernet ifname $IFACE con-name $CONEXAO"
fi

# interface-name e' o que impede o NM de escolher o wlp2s0. Sem esta linha, a
# ativacao tenta o wifi e falha.
nmcli con $ALVO \
    connection.interface-name "$IFACE" \
    ipv4.method manual \
    ipv4.addresses "$IP/$MASCARA" \
    ipv4.gateway "$GATEWAY" \
    ipv4.dns "$DNS1 $DNS2" \
    ipv4.never-default no \
    ipv6.method disabled \
    2>&1 | head -3

# ---------------------------------------------------------------- sobe
echo "==> ativar"
nmcli con up "$CONEXAO" 2>&1 | head -3

# ---------------------------------------------------------------- confere
echo "==> conferindo"
FALHA=""
sleep 5
ip -4 addr show "$IFACE" 2>/dev/null | grep -q "inet $IP/$MASCARA" || FALHA="a interface nao ficou com $IP/$MASCARA"
[ -z "$FALHA" ] && ! ip route get 1.1.1.1 2>/dev/null | grep -q "$GATEWAY" \
    && FALHA="a rota padrao nao esta passando pelo gateway $GATEWAY"
[ -z "$FALHA" ] && ! getent hosts google.com >/dev/null 2>&1 \
    && FALHA="o DNS nao resolve google.com"

if [ -n "$FALHA" ]; then
    echo "ERRO: $FALHA" >&2
    echo "      O IP anterior era $IP tambem, entao nada mudou de endereco." >&2
    echo "      Para desfazer: rm $NETPLAN_ARQ && netplan generate && netplan apply" >&2
    exit 1
fi

echo
echo "    interface:  $IFACE"
echo "    endereco:   $IP/$MASCARA  (fixo, nao vem mais do DHCP)"
echo "    gateway:    $GATEWAY"
echo "    DNS:        $DNS1, $DNS2"
echo "    controlado por: NetworkManager, perfil '$CONEXAO'"
echo
echo "Para conferir depois:"
echo "  nmcli con show $CONEXAO | grep -E 'IP4|GATEWAY|DNS'"
echo "  nmcli device status | grep $IFACE"
echo
echo "Para desfazer:"
echo "  rm $NETPLAN_ARQ && netplan generate && netplan apply"
