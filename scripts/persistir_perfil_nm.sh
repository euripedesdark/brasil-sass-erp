#!/bin/bash
# Passa o perfil de rede do /run (volátil) para o /etc (persistente).
#
# O problema: o perfil "brasil-saas" foi criado pelo netplan, e por isso mora
# em /run/NetworkManager/system-connections/netplan-NM-<uuid>.nmconnection.
# O /run e' memoria — no reboot o arquivo some, o netplan nao esta mais
# instalado para recria-lo, e a maquina volta sem nenhum perfil de rede. O
# sintoma so apareceria no proximo reboot, que e' a hora errada para descobrir.
#
# A correcao e' gravar o mesmo perfil em /etc/NetworkManager/system-connections/,
# que e' onde o NM le no boot. O perfil em memoria continua valendo durante a
# operacao: o arquivo novo entra em vigor no proximo reload, sem derrubar a
# conexao que esta no ar.
export LC_ALL=C LANG=C

CONEXAO="${1:-brasil-saas}"
# O campo FILENAME so existe na saida de tabela. Pedir
# `nmcli -f FILENAME con show <nome>` da erro — o campo nao e' valido ai, e a
# lista de campos aceitos vem no meio da mensagem de erro, hiding o caminho
# real do arquivo. Entao a tabela e lida e a linha do perfil e separada.
ORIGEM=$(nmcli -t -f NAME,FILENAME con show 2>/dev/null \
         | awk -F: -v alvo="$CONEXAO" '$1 == alvo { print $2; exit }')
[ -z "$ORIGEM" ] && ORIGEM=$(nmcli -f NAME,FILENAME con show 2>/dev/null \
         | awk -v alvo="$CONEXAO" '$1 == alvo { $1=""; sub(/^[ \t]+/, ""); print; exit }')
DESTINO_DIR="/etc/NetworkManager/system-connections"
DESTINO="$DESTINO_DIR/$CONEXAO.nmconnection"

echo "--- perfil em memoria: $ORIGEM"
if [ -z "$ORIGEM" ] || [ ! -f "$ORIGEM" ]; then
    echo "ERRO: o perfil '$CONEXAO' nao tem arquivo em disco." >&2
    echo "     nmcli con show $CONEXAO | grep FILENAME" >&2
    exit 1
fi

echo "--- gerando o arquivo definitivo"
# Gerado a partir do perfil que esta ativo, para nao divergir em um campo so.
# O id e' um UUID novo porque dois arquivos com o mesmo id causam conflito de
# nome no NM.
{
    sed 's/^uuid=.*/'"$(cat /proc/sys/kernel/random/uuid)"'/' "$ORIGEM"
} > "$DESTINO.tmp"

# O NM so le o perfil se o arquivo for do dono root e modo 600. Com 644 ele
# ignora em silencio e a maquina fica sem rede no boot, sem mensagem.
chown root:root "$DESTINO.tmp"
chmod 600 "$DESTINO.tmp"
mv "$DESTINO.tmp" "$DESTINO"

echo "    escrito: $DESTINO ($(stat -c %a "$DESTINO"))"

echo "--- recarregando o NM (nao derruba a conexao ativa)"
nmcli connection reload 2>&1 | head -2

echo "--- conferindo o que o NM ve agora"
nmcli -f NAME,FILENAME,DEVICE,STATE con show 2>/dev/null | grep -E "NAME|$CONEXAO" | sed 's/^/    /'

echo "--- a rede continua de pe?"
ip -4 addr show enp3s0f1 2>/dev/null | grep -oE 'inet [0-9./]+' | sed 's/^/    /'
ip route get 1.1.1.1 2>/dev/null | head -1 | sed 's/^/    /'
getent hosts google.com >/dev/null 2>&1 && echo "    DNS resolve" || echo "    DNS NAO resolve"
