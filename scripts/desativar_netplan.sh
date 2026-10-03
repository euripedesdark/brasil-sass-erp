#!/bin/bash
# Tira o netplan do caminho: a rede passa a ser configurada so pelo NM.
#
# Nao remove o pacote netplan.io, e' de proposito: ele e' dependencia de
# ubuntu-minimal, e o apt fica com o metapackage quebrado. O que atrapalha nao
# e' o pacote instalado, e' o netplan ter arquivo de configuracao. Sem
# arquivo, o NM e' o unico que fala da rede.
export LC_ALL=C LANG=C

echo "--- 1. remover os arquivos de configuracao do netplan"
mv /etc/netplan /etc/netplan.desativado 2>/dev/null \
    || rm -f /etc/netplan/*.yaml 2>/dev/null
ls -d /etc/netplan.desativado >/dev/null 2>&1 \
    && echo "    /etc/netplan renomeado para /etc/netplan.desativado (o original, para desfazer)" \
    || echo "    arquivos removidos"

echo "--- 2. conferir que nada mais invoca netplan"
for S in systemd-networkd NetworkManager-wait-online systemd-networkd-wait-online; do
    printf '    %-34s ' "$S"
    systemctl is-enabled "$S" 2>/dev/null | sed 's/^/habilitado: /'
done

echo "--- 3. o NM e' o dono agora"
nmcli -f NAME,DEVICE,STATE con show --active 2>/dev/null | sed 's/^/    /'

echo "--- 4. a rede funciona?"
ip -4 addr show enp3s0f1 2>/dev/null | grep -oE 'inet [0-9./]+' | sed 's/^/    /'
ip route get 1.1.1.1 2>/dev/null | head -1 | sed 's/^/    /'
getent hosts google.com >/dev/null 2>&1 && echo "    DNS resolve google.com" || echo "    DNS NAO resolve"

echo "--- 5. para desfazer"
echo "    mv /etc/netplan.desativado /etc/netplan && systemctl enable --now systemd-networkd"
