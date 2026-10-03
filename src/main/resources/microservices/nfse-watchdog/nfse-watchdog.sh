#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Watchdog da NFS-e de São Paulo.
#
# O nginx já troca de upstream quando a conexão falha. O que ele não pega é a
# implementação que responde 200 e devolve o contrato errado — e é exatamente o
# que aconteceu em 26/09/2026: com a API Java fora, o Ruby emitiu a nota 29 com
# success=true, a prefeitura aceitou, e o ERP respondeu erro porque lia
# "sucesso" e o Ruby devolve "success" um nivel abaixo, dentro de "error".
#
# O nginx viu sucesso. O ERP quebrou. Por isso a checagem aqui é do CONTRATO, e
# não da porta: uma porta aberta nao prova que a implementação serve.
#
# O que este script faz, e o que ele deliberadamente nao faz:
#   - para a implementação quebrada e sobe a outra
#   - ESPERA a outra ficar saudavel antes de dar por resolvido
#   - mantem a quebrada em quarentena por um tempo, para nao ficar alternando
#   - nunca reinicia um serviço que ja esta de pe
#
# O sono depois de subir a outra importa: sem ele, o primeiro RPS depois da troca
# cai enquanto a JVM ainda esta bootando, e a pessoa ve timeout em vez de nota.
# -----------------------------------------------------------------------------
set -uo pipefail

# --- configuracao (ambiente sobrepoe) ----------------------------------------
PRIMARIA_SERVICO="${WATCHDOG_PRIMARIA_SERVICO:-nfse-sp-api}"
FALLBACK_SERVICO="${WATCHDOG_FALLBACK_SERVICO:-nfse-sp-bridge}"
INTERVALO="${WATCHDOG_INTERVALO:-15}"
TENTATIVAS_ANTES_DE_TROCAR="${WATCHDOG_TENTATIVAS:-2}"
ESPERA_SAUDE="${WATCHDOG_ESPERA_SAUDE:-90}"
QUARENTENA="${WATCHDOG_QUARENTENA:-300}"
IM_TESTE="${WATCHDOG_IM:-00000000000191}"
INTERVALO_PORTA="${WATCHDOG_PORTA:-5}"

# O intervalo e de 15s e nao de 30 porque o ciclo completo ja leva 15s de
# espera entre checagens mais o tempo da checagem, e uma emissao que cai no
# failover espera esse tempo. Medido: com a primaria fora do ar, a recuperacao
# levou 6s a partir do stop, e com o nginx ja estava resolvido em menos de 1s
# — o watchdog existe para o caso que o nginx nao pega, nao para ser mais rapido
# que ele.

ARQUIVO_ESTADO="${WATCHDOG_ESTADO:-/var/lib/brasil-saas/nfse-watchdog.estado}"
DIARIO="${WATCHDOG_LOG:-/var/log/brasil-saas/nfse-watchdog.log}"

mkdir -p "$(dirname "$ARQUIVO_ESTADO")" "$(dirname "$DIARIO")" 2>/dev/null || true

registrar() {
    printf '%s %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*" >> "$DIARIO"
}

# --- leitura do estado ------------------------------------------------------
# Formato: "marcador timestamp" em arquivo, uma linha. Simples de ler e de
# sobreviver a reinicio, que e o que importa num watchdog.
ler_estado() {
    local chave="$1" padrao="${2:-}"
    local linha
    linha=$(grep "^${chave} " "$ARQUIVO_ESTADO" 2>/dev/null | tail -1) || true
    [ -n "$linha" ] || { echo "$padrao"; return; }
    echo "${linha#* }"
}

gravar_estado() {
    local chave="$1" valor="$2"
    grep -v "^${chave} " "$ARQUIVO_ESTADO" 2>/dev/null > "${ARQUIVO_ESTADO}.tmp" || true
    printf '%s %s\n' "$chave" "$valor" >> "${ARQUIVO_ESTADO}.tmp"
    mv "${ARQUIVO_ESTADO}.tmp" "$ARQUIVO_ESTADO" 2>/dev/null || true
}

agora() { date +%s; }

em_quarentena() {
    local ate
    ate=$(ler_estado "quarentena_ate" "0")
    [ "$ate" -gt "$(agora)" ] 2>/dev/null
}

# --- a checagem de verdade --------------------------------------------------
# Porta aberta nao prova que serve. A unica forma de saber e perguntar algo que
# a implementacao precisaria resolver de verdade, e a consulta de CNPJ do
# prestador serve: ela nao emite nada, nao consome numero, e a resposta tem que
# trazer a inscricao municipal.
saudavel() {
    local porta="$1"
    local corpo
    corpo=$(curl -s -m 10 -X POST "http://127.0.0.1:${porta}/api/nfse-sp/consulta-cnpj" \
                 -H 'Content-Type: application/json' \
                 -d "{\"cnpj\":\"${IM_TESTE}\"}" 2>/dev/null)

    [ -n "$corpo" ] || return 1

    # O contrato e o mesmo nos dois lados, menos o nome do sucesso: a API Java
    # devolve "sucesso" e o bridge Ruby devolve "success". Aceitar os dois e o
    # que permite o watchdog ser justo com as implementacoes em vez de
    # privilegiar uma delas so porque ela foi escrita primeiro.
    echo "$corpo" | grep -q '"inscricao_municipal"' || return 1
    echo "$corpo" | grep -qE '"(sucesso|success)"[[:space:]]*:[[:space:]]*true' || return 1
    return 0
}

porta_de() {
    case "$1" in
        "$PRIMARIA_SERVICO")  echo "${WATCHDOG_PORTA_PRIMARIA:-4568}" ;;
        "$FALLBACK_SERVICO") echo "${WATCHDOG_PORTA_FALLBACK:-4569}" ;;
        *) echo "" ;;
    esac
}

parar()  { systemctl stop  "$1" 2>/dev/null; }
subir()  { systemctl start "$1" 2>/dev/null; }
ativo()  { systemctl is-active --quiet "$1" 2>/dev/null; }

esperar_saudavel() {
    # Sem este sleep, o primeiro RPS depois da troca cai com a JVM ainda
    # subindo, e o resultado e timeout em vez de nota emitida.
    local porta="$1" limite="$2" decorrido=0
    while [ "$decorrido" -lt "$limite" ]; do
        if saudavel "$porta"; then
            return 0
        fi
        sleep "$INTERVALO_PORTA"
        decorrido=$((decorrido + INTERVALO_PORTA))
    done
    return 1
}

# --- laco -------------------------------------------------------------------
registrar "watchdog iniciado: primaria=${PRIMARIA_SERVICO} fallback=${FALLBACK_SERVICO} intervalo=${INTERVALO}s"

falhas_primaria=0
falhas_fallback=0

while true; do
    porta_primaria=$(porta_de "$PRIMARIA_SERVICO")
    porta_fallback=$(porta_de "$FALLBACK_SERVICO")

    # 1. primaria saudavel? e o caminho normal, entao sai rapido.
    if ! em_quarentena && saudavel "$porta_primaria"; then
        falhas_primaria=0
        # Se a primaria voltou, garante que ela esta de pe. E o unico lugar que
        # faz isso: recuperacao e aqui, nao no nginx.
        if ! ativo "$PRIMARIA_SERVICO"; then
            registrar "primaria respondeu mas o servico nao esta ativo; subindo"
            subir "$PRIMARIA_SERVICO"
        fi
        sleep "$INTERVALO"
        continue
    fi

    # 2. primaria nao respondeu. A distincao que evita o loop: ela esta no ar e
    #    quebrada, ou esta fora do ar?
    #
    #    Fora do ar e caso de RECUPERACAO: tenta subir. Parar de novo um servico
    #    que ja esta parado e nao-op, e repetir isso a cada ciclo e exatamente o
    #    restart storm que passou 8,6 horas sem ninguem ver. Se subir nao
    #    resolve, ai sim e caso de troca.
    #
    #    No ar e quebrada e caso de TROCA: para e joga o fallback.
    if ! em_quarentena; then
        falhas_primaria=$((falhas_primaria + 1))
        if [ "$falhas_primaria" -ge "$TENTATIVAS_ANTES_DE_TROCAR" ]; then
            falhas_primaria=0

            if ! ativo "$PRIMARIA_SERVICO"; then
                registrar "PRIMARIA ${PRIMARIA_SERVICO} esta fora do ar. Tentando recuperar."
                subir "$PRIMARIA_SERVICO"
                if esperar_saudavel "$porta_primaria" "$ESPERA_SAUDE"; then
                    registrar "PRIMARIA ${PRIMARIA_SERVICO} recuperada."
                else
                    registrar "PRIMARIA ${PRIMARIA_SERVICO} nao recuperou em ${ESPERA_SAUDE}s. Isolando."
                    gravar_estado "quarentena_ate" "$(( $(agora) + QUARENTENA ))"
                    if ! ativo "$FALLBACK_SERVICO"; then
                        registrar "subindo o fallback ${FALLBACK_SERVICO}"
                        subir "$FALLBACK_SERVICO"
                    fi
                    esperar_saudavel "$porta_fallback" "$ESPERA_SAUDE" \
                        && registrar "FALLBACK ${FALLBACK_SERVICO} saudavel apos o isolamento." \
                        || registrar "FALLBACK ${FALLBACK_SERVICO} NAO ficou saudavel em ${ESPERA_SAUDE}s."
                fi
            else
                registrar "PRIMARIA ${PRIMARIA_SERVICO} no ar e nao responde. Trocando."
                parar "$PRIMARIA_SERVICO"
                gravar_estado "quarentena_ate" "$(( $(agora) + QUARENTENA ))"

                if ! ativo "$FALLBACK_SERVICO"; then
                    registrar "subindo o fallback ${FALLBACK_SERVICO}"
                    subir "$FALLBACK_SERVICO"
                fi

                if esperar_saudavel "$porta_fallback" "$ESPERA_SAUDE"; then
                    registrar "FALLBACK ${FALLBACK_SERVICO} saudavel apos a troca."
                else
                    registrar "FALLBACK ${FALLBACK_SERVICO} NAO ficou saudavel em ${ESPERA_SAUDE}s."
                fi
            fi
        fi
    fi

    # 3. o fallback tambem esta ruim? ai nao ha o que fazer alem de registrar.
    # Registrar e o que importa: um watchdog silencioso e a causa dos 2024
    # reinicios que ninguem viu.
    if ! saudavel "$porta_fallback"; then
        falhas_fallback=$((falhas_fallback + 1))
        if [ "$falhas_fallback" -ge "$TENTATIVAS_ANTES_DE_TROCAR" ]; then
            registrar "FALLBACK ${FALLBACK_SERVICO} tambem nao respondeu (${falhas_fallback}x). Sem implementacao saudavel."
            falhas_fallback=0
        fi
    else
        falhas_fallback=0
    fi

    sleep "$INTERVALO"
done
