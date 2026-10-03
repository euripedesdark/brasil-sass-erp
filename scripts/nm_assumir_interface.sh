#!/bin/bash
# Passa a interface do systemd-networkd para o NetworkManager, com rede de
# seguranca.
#
# O problema: o netplan estava com o renderer padrao, que e' o networkd. O
# NetworkManager esta instalado, ativo e habilitado, mas sem perfil — e
# enquanto o networkd segura a interface, o NM ve o device como "unmanaged":
#
#   Error: Connection activation failed: No suitable device found for this
#   connection (device wlp2s0 not available because profile is not compatible
#   with device (connection type is not "802-11-wireless"))
#
# Repara no wlp2s0: sem a interface amarrada no perfil, o NM tenta o primeiro
# device que ve, e o primeiro e' o do wifi. O perfil ja foi criado com
# connection.interface-name = enp3s0f1; o que falta e' o device sair de
# "unmanaged".
#
# A janela perigosa e' uma so: entre parar o networkd e o NM assumir, a
# interface fica sem dono e a maquina perde a rede. Por causa disso este
# script arma uma volta automatica antes de fazer qualquer coisa:
#
#   1. agenda um processo de fundo que, em 90 segundos, reativa o networkd
#   2. desativa o networkd
#   3. sobe o perfil do NM
#   4. confere
#   5. se deu certo, mata o processo de volta
#
# Se der errado, e' so esperar 90 segundos que a rede volta sozinha, sem ninguem
# precisar chegar na maquina.
#
# Rodar como root:
#   sudo bash scripts/nm_assumir_interface.sh

set -euo pipefail

IFACE="${BRASIL_SAAS_IFACE:-enp3s0f1}"
CONEXAO="${BRASIL_SAAS_CONEXAO:-brasil-saas}"
IP="${BRASIL_SAAS_IP:-<IP-DO-SERVIDOR>}"
MASCARA="${BRASIL_SAAS_MASCARA:-24}"
ESPERA=90

if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: rodar como root." >&2
    exit 1
fi

echo "==> antes"
nmcli -f DEVICE,TYPE,STATE,CONNECTION device status 2>/dev/null | grep -E "DEVICE|$IFACE" | head -2
systemctl is-active systemd-networkd 2>/dev/null | sed 's/^/    networkd: /'

# ---------------------------------------------------------------- volta automatica
# Um nohup que espera e reativa o networkd. Fica vivo mesmo se o script
# principal morrer, e por isso e' a rede de seguranca de verdade: nao depende
# do script terminar bem.
echo "==> armando a volta automatica em ${ESPERA}s"
cat > /root/voltar-networkd.sh <<'VOLTA'
#!/bin/bash
# Volta o networkd se o NetworkManager nao assumiu a interface.
sleep "$1"
systemctl enable --now systemd-networkd >/dev/null 2>&1
netplan apply >/dev/null 2>&1
systemctl restart NetworkManager >/dev/null 2>&1
VOLTA
chmod +x /root/voltar-networkd.sh
nohup setsid /root/voltar-networkd.sh "$ESPERA" >/dev/null 2>&1 &
VOLTA_PID=$!
echo "    processo de volta: PID $VOLTA_PID"

# ---------------------------------------------------------------- solta o networkd
echo "==> desativando o systemd-networkd"
systemctl disable --now systemd-networkd 2>&1 | head -2
systemctl is-active systemd-networkd >/dev/null 2>&1 && {
    echo "ERRO: o networkd continua ativo. Cancelando, nada foi mudado." >&2
    kill "$VOLTA_PID" 2>/dev/null || true
    exit 1
}
echo "    networkd parou"

# ---------------------------------------------------------------- NM assume
# Forcar o managed yes vem antes do con up: sem isso o NM ainda ve o device
# como unmanaged e a ativacao falha do mesmo jeito.
echo "==> NM assume a interface"
nmcli device set "$IFACE" managed yes 2>&1 | head -2
sleep 2
nmcli -f DEVICE,STATE device status 2>/dev/null | grep "$IFACE" | sed 's/^/    /'

nmcli con up "$CONEXAO" 2>&1 | head -3

# ---------------------------------------------------------------- confere
echo "==> conferindo"
sleep 6
FALHA=""
ip -4 addr show "$IFACE" 2>/dev/null | grep -q "inet $IP/$MASCARA" \
    || FALHA="a interface nao ficou com $IP/$MASCARA"
[ -z "$FALHA" ] && ! ip route get 1.1.1.1 2>/dev/null | grep -q "192.168.2.1" \
    && FALHA="a rota padrao nao esta mais pelo gateway 192.168.2.1"
[ -z "$FALHA" ] && ! getent hosts google.com >/dev/null 2>&1 \
    && FALHA="o DNS nao resolve google.com"

if [ -n "$FALHA" ]; then
    echo "ERRO: $FALHA" >&2
    echo "      A volta automatica roda em ${ESPERA}s. Nao faca mais nada, so espere." >&2
    exit 1
fi

# deu certo: cancela a volta
kill "$VOLTA_PID" 2>/dev/null || true
pkill -f '/root/voltar-networkd.sh' 2>/dev/null || true
rm -f /root/voltar-networkd.sh

echo
echo "    interface:  $IFACE"
echo "    endereco:   $IP/$MASCARA  (fixo, nao vem mais do DHCP)"
echo "    rede:       controlada pelo NetworkManager, perfil '$CONEXAO'"
echo "    networkd:   desativado"
echo
nmcli -f DEVICE,STATE,CONNECTION device status 2>/dev/null | grep -E "DEVICE|$IFACE" | head -2 | sed 's/^/    /'
echo
echo "Para conferir depois de um reboot:"
echo "  nmcli con show $CONEXAO | grep -E 'IP4.ADDRESS|IP4.GATEWAY|IP4.DNS'"
echo
echo "Se algum dia precisar voltar ao networkd:"
echo "  systemctl disable --now NetworkManager && systemctl enable --now systemd-networkd"
echo "  rm /etc/netplan/01-brasil-saas.yaml && netplan generate && netplan apply"
