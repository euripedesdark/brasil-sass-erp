#!/bin/bash
# Confere e finaliza a configuracao de rede gerida pelo NetworkManager.
export LC_ALL=C LANG=C
echo "--- volta automatica cancelada"
pkill -f voltar-networkd.sh 2>/dev/null
rm -f /root/voltar-networkd.sh

echo "--- o perfil sobe sozinho no boot?"
echo "    autoconnect: $(nmcli -g connection.autoconnect con show brasil-saas 2>/dev/null)"

echo "--- o perfil tem IP estatico de verdade?"
echo "    ipv4.method:   $(nmcli -g ipv4.method con show brasil-saas 2>/dev/null)"
echo "    ipv4.addresses:$(nmcli -g ipv4.addresses con show brasil-saas 2>/dev/null)"
echo "    ipv4.gateway:  $(nmcli -g ipv4.gateway con show brasil-saas 2>/dev/null)"
echo "    ipv4.dns:      $(nmcli -g ipv4.dns con show brasil-saas 2>/dev/null)"

echo "--- netplan ainda e necessario para alguma coisa?"
apt-cache rdepends netplan.io 2>/dev/null | grep -v '^netplan.io' | grep -v '^$' | sed 's/^/    /' | head -5
echo "    (vazio acima = nada depende dele)"
