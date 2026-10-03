#!/usr/bin/env bash
# =============================================================================
# Brasil SaaS ERP — sobe o backend Java e só depois o React
# =============================================================================
#
#   ./subir-dev.sh              sobe os dois (Java espera ficar saudável)
#   ./subir-dev.sh --so-java    sobe o backend
#   ./subir-dev.sh --so-react   sobe so o frontend (backend ja de pe)
#   ./subir-dev.sh --parar      derruba os dois
#   ./subir-dev.sh --status     mostra o que esta de pe
#   ./subir-dev.sh --reiniciar  derruba e sobe de novo
#
# A ordem importa. O Vite faz proxy de /api para o backend (vite.config.js),
# entao subir o React antes deixaria a tela carregando e errando "Failed to
# fetch" em cada chamada ate o Java responder. Aqui o script espera o
# /actuator/health dizer UP antes de abrir a porta do Vite.
#
# O Vite e so para desenvolver. Em producao o build vai para static/dist e o
# proprio Spring serve — la nao ha segunda porta, e este script nao se aplica.
# =============================================================================
set -uo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$RAIZ"

REACT_DIR="$RAIZ/src/main/resources/static/react"
RUN_DIR="$RAIZ/.run"
JAVA_PID="$RUN_DIR/java.pid"
REACT_PID="$RUN_DIR/react.pid"
JAVA_LOG="$RUN_DIR/java.log"
REACT_LOG="$RUN_DIR/react.log"

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
# O Spring demora em contexto para subir tudo (Flyway, conexoes). 240s cobre
# banco grande sem passar vergonha logo depois do "iniciando".
TIMEOUT_JAVA="${TIMEOUT_JAVA:-240}"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

mkdir -p "$RUN_DIR"

# ---------------------------------------------------------------- utilidades

porta_ouvida() { ss -lnt 2>/dev/null | grep -qE "[:.]${1}\b"; }

pid_vivo() {
    local f="$1"
    [ -f "$f" ] || return 1
    local p; p="$(cat "$f" 2>/dev/null)"
    [ -n "$p" ] && kill -0 "$p" 2>/dev/null
}

# Derruba o GRUPO de processos, nao so o pid guardado.
#
# "npm run dev" e um invólucro: o vite real e filho dele. Matar so o pid do
# npm deixa o vite vivo segurando a porta 5173, e o proximo start falha com
# "porta ja ocupada" por um processo que o script nem sabe que existe. Por isso
# os processos sao lancados com setsid (grupo proprio) e aqui se usa kill com
# pid negativo, que alcanca o grupo inteiro.
mata() {
    local nome="$1" f="$2"
    if pid_vivo "$f"; then
        local p; p="$(cat "$f")"
        echo -e "${YELLOW}[$nome] derrubando (pid $p)...${NC}"
        kill -TERM -- "-$p" 2>/dev/null || kill -TERM "$p" 2>/dev/null
        for _ in $(seq 1 20); do
            pid_vivo "$f" || break
            sleep 0.5
        done
        if pid_vivo "$f"; then
            kill -KILL -- "-$p" 2>/dev/null || kill -KILL "$p" 2>/dev/null
        fi
        rm -f "$f"
        echo -e "${GREEN}[$nome] parado.${NC}"
    fi
}

# Nao exigir status "UP" para considerar o app de pe.
#
# Em dev o indicador de mail fica DOWN (nao ha SMTP em localhost:25), e isso
# derruba o status geral do actuator para DOWN, com HTTP 503. Mas o app esta
# inteiro: banco UP, mongo UP, disco UP. E o proprio 503 e a prova de que o
# Tomcat esta servindo -- so existe resposta se o Spring MVC estiver roteando.
# Exigir "UP" faria o script esperar ate o timeout com o backend funcionando.
#
# 401/403 = actuator protegido pelo Spring Security; a porta de pe basta.
#
# saida: 0 = app no ar | 1 = ainda nao
DEGRADADO=""
java_saudavel() {
    DEGRADADO=""
    local corpo http

    # Sem o -f de proposito: em 503 o curl descarta o corpo, e e justamente no
    # corpo que vem a lista de qual componente caiu.
    corpo="$(curl -sS -m 3 "http://localhost:${BACKEND_PORT}/actuator/health" 2>/dev/null)"
    http="$(curl -s -o /dev/null -m 3 -w '%{http_code}' \
        "http://localhost:${BACKEND_PORT}/actuator/health" 2>/dev/null)"

    case "$http" in
        200|503)
            # O status do topo e o do actuator, nao o de qualquer componente: um
            # grep por "status":"UP" solto casa com o do banco e daria UP num
            # app com o mail caido. Por isso o parse e feito com o python.
            printf '%s' "$corpo" | python3 -c '
import json, sys
try:
    d = json.load(sys.stdin)
except Exception:
    print("|"); sys.exit(0)
comps = d.get("components") or {}
ruins = [k for k, v in comps.items() if v.get("status") in ("DOWN", "OUT_OF_SERVICE")]
print("|".join(ruins))
' 2>/dev/null > "$RUN_DIR/.health"
            DEGRADADO="$(tr '|' ' ' < "$RUN_DIR/.health" | sed 's/  */ /g; s/^ //; s/ $//')"
            return 0
            ;;
        401|403)
            porta_ouvida "$BACKEND_PORT" && return 0
            ;;
    esac
    return 1
}

# ---------------------------------------------------------------- subindo

subir_java() {
    if pid_vivo "$JAVA_PID"; then
        echo -e "${GREEN}[JAVA] ja esta no ar (pid $(cat "$JAVA_PID")).${NC}"
        return 0
    fi
    if porta_ouvida "$BACKEND_PORT"; then
        echo -e "${RED}[JAVA] a porta ${BACKEND_PORT} ja esta ocupada por outro processo.${NC}"
        echo -e "     Feche o que estiver escutando ali, ou rode com BACKEND_PORT=<outra> ${NC}"
        return 1
    fi
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}[JAVA] maven nao encontrado no PATH.${NC}"
        return 1
    fi

    echo -e "${BLUE}[JAVA] subindo o backend na ${BACKEND_PORT}...${NC}"
    : > "$JAVA_LOG"
    # setsid: grupo de processos proprio, para o --parar derrubar a arvore toda
    setsid mvn -B -q spring-boot:run >> "$JAVA_LOG" 2>&1 &
    echo $! > "$JAVA_PID"

    local espera=0
    while [ "$espera" -lt "$TIMEOUT_JAVA" ]; do
        if ! pid_vivo "$JAVA_PID"; then
            echo -e "${RED}[JAVA] o processo morreu no boot. Ultimas linhas do log:${NC}"
            tail -30 "$JAVA_LOG" | sed 's/^/     /'
            rm -f "$JAVA_PID"
            return 1
        fi
        if java_saudavel; then
            echo -e "${GREEN}[JAVA] de pe em ${espera}s (pid $(cat "$JAVA_PID")).${NC}"
            if [ -n "$DEGRADADO" ]; then
                echo -e "${YELLOW}       com componente(s) fora do ar: ${DEGRADADO}${NC}"
                echo -e "${YELLOW}       isso NAO impede o uso; em dev o mail cai por falta de SMTP local.${NC}"
            fi
            return 0
        fi
        sleep 2
        espera=$((espera + 2))
        [ $((espera % 30)) -eq 0 ] && echo -e "${YELLOW}      ...${espera}s/${TIMEOUT_JAVA}s${NC}"
    done

    echo -e "${RED}[JAVA] nao ficou saudavel em ${TIMEOUT_JAVA}s. Ultimas linhas:${NC}"
    tail -30 "$JAVA_LOG" | sed 's/^/     /'
    return 1
}

subir_react() {
    if pid_vivo "$REACT_PID"; then
        echo -e "${GREEN}[REACT] ja esta no ar (pid $(cat "$REACT_PID")).${NC}"
        return 0
    fi
    if porta_ouvida "$FRONTEND_PORT"; then
        echo -e "${RED}[REACT] a porta ${FRONTEND_PORT} ja esta ocupada.${NC}"
        return 1
    fi
    if [ ! -d "$REACT_DIR/node_modules" ]; then
        echo -e "${YELLOW}[REACT] node_modules ausente; rodando npm install (pode demorar)...${NC}"
        (cd "$REACT_DIR" && npm install) || return 1
    fi

    echo -e "${BLUE}[REACT] subindo o Vite na ${FRONTEND_PORT}...${NC}"
    : > "$REACT_LOG"
    setsid bash -c "cd '$REACT_DIR' && exec npm run dev -- --port '$FRONTEND_PORT'" \
        >> "$REACT_LOG" 2>&1 &
    echo $! > "$REACT_PID"

    local espera=0
    while [ "$espera" -lt 60 ]; do
        if ! pid_vivo "$REACT_PID"; then
            echo -e "${RED}[REACT] o processo morreu. Log:${NC}"
            tail -20 "$REACT_LOG" | sed 's/^/     /'
            rm -f "$REACT_PID"
            return 1
        fi
        if porta_ouvida "$FRONTEND_PORT"; then
            echo -e "${GREEN}[REACT] de pe em ${espera}s (pid $(cat "$REACT_PID")).${NC}"
            return 0
        fi
        sleep 1
        espera=$((espera + 1))
    done

    echo -e "${RED}[REACT] nao abriu a porta ${FRONTEND_PORT} em 60s. Log:${NC}"
    tail -20 "$REACT_LOG" | sed 's/^/     /'
    return 1
}

status() {
    echo ""
    echo -e "${BLUE}--- Brasil SaaS ERP ---${NC}"
    if pid_vivo "$JAVA_PID"; then
        echo -e "  backend  ${GREEN}no ar${NC}   http://localhost:${BACKEND_PORT}   (pid $(cat "$JAVA_PID"))"
    elif porta_ouvida "$BACKEND_PORT"; then
        echo -e "  backend  ${YELLOW}ouvido${NC}    :${BACKEND_PORT}, mas fora deste script"
    else
        echo -e "  backend  ${RED}parado${NC}"
    fi
    if pid_vivo "$REACT_PID"; then
        echo -e "  frontend ${GREEN}no ar${NC}   http://localhost:${FRONTEND_PORT}  (pid $(cat "$REACT_PID"))"
    elif porta_ouvida "$FRONTEND_PORT"; then
        echo -e "  frontend ${YELLOW}ouvido${NC}    :${FRONTEND_PORT}, mas fora deste script"
    else
        echo -e "  frontend ${RED}parado${NC}"
    fi
    echo -e "  logs     $RUN_DIR"
    echo ""
}

# ---------------------------------------------------------------- main

COMANDO="${1:-subir}"

case "$COMANDO" in
    --parar|parar|stop)
        mata "REACT" "$REACT_PID"
        mata "JAVA"  "$JAVA_PID"
        status
        ;;
    --status|status)
        status
        ;;
    --so-java|so-java)
        subir_java && status
        ;;
    --so-react|so-react)
        subir_react && status
        ;;
    --reiniciar|reiniciar|restart)
        mata "REACT" "$REACT_PID"
        mata "JAVA"  "$JAVA_PID"
        echo ""
        subir_java && subir_react && status
        ;;
    subir|"")
        if ! subir_java; then
            echo -e "${RED}O React nao sobe sem o backend: o Vite faz proxy de /api para :${BACKEND_PORT}.${NC}"
            exit 1
        fi
        # So agora, com o Java saudavel, abre o frontend.
        subir_react && status
        ;;
    -h|--help|help)
         sed -n '2,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
        ;;
    *)
        echo -e "${RED}Opcao desconhecida: $COMANDO${NC}"
         sed -n '2,20p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
        exit 1
        ;;
esac
