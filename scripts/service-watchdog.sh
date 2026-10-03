#!/bin/bash
# Brasil SaaS ERP - Service Watchdog
# Monitora todos os servicos do ERP e reinicia automaticamente
# Caso nao consiga reiniciar, envia alerta para Graylog

set -o errexit
set -o nounset
set -o pipefail

# =============================================================================
# CONFIGURACOES
# =============================================================================

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Diretorio base do projeto
# O caminho do repositorio NAO e fixo.
#
# A versao anterior apontava para
#   /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP
# que e' o caminho DESTA maquina, e que nem existe aqui: o repo esta em
#   /home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP
#
# Em outra conta, outra pasta, ou com o OneDrive em outro lugar, o `cd` falhava
# e o script seguia rodando na pasta errada — sem mensagem, porque o `cd` sem
# `set -e` nao interrompe. Pior: as units systemd geradas gravavam o caminho
# fixo, entao o servico nao subia na maquina nova.
#
# O repositorio se localiza a partir do proprio script, que e' o unico ponto
# que muda junto com a copia de trabalho.
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Lista de todos os modulos (servicos)
declare -A SERVICE_MODULES=(
    ["core"]="brasil_saas-erp-core.service"
    ["cadastro"]="brasil_saas-erp-cadastro.service"
    ["financeiro"]="brasil_saas-erp-financeiro.service"
    ["vendas"]="brasil_saas-erp-vendas.service"
    ["compras"]="brasil_saas-erp-compras.service"
    ["estoque"]="brasil_saas-erp-estoque.service"
    ["fiscal"]="brasil_saas-erp-fiscal.service"
    ["rh"]="brasil_saas-erp-rh.service"
    ["ia"]="brasil_saas-erp-ia.service"
    ["servicos"]="brasil_saas-erp-servicos.service"
    ["bi"]="brasil_saas-erp-bi.service"
    ["producao"]="brasil_saas-erp-producao.service"
)

# Lista de modulos criticos (devem estar sempre ativos)
CRITICAL_MODULES=("core" "cadastro" "financeiro" "estoque")

# Configuracoes Graylog
GRAYLOG_SERVER="127.0.0.1"
GRAYLOG_PORT="12201"
GRAYLOG_ENABLED=true

# Configuracoes de alerta
MAX_RESTART_ATTEMPTS=3
RESTART_DELAY_SECONDS=10
CHECK_INTERVAL_SECONDS=30

# Arquivo de log do watchdog
WATCHDOG_LOG="/var/log/brasil_saas/brasil_saas-watchdog.log"
WATCHDOG_PID="/var/run/brasil_saas-watchdog.pid"

# =============================================================================
# FUNCOES DE LOG
# =============================================================================

log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    local log_entry="[$timestamp] [$level] $message"
    
    echo -e "${log_entry}"
    echo "$log_entry" >> "$WATCHDOG_LOG"
}

log_info() {
    log "INFO" "$1"
}

log_warn() {
    log "WARN" "$1"
}

log_error() {
    log "ERROR" "$1"
}

log_critical() {
    log "CRITICAL" "$1"
}

# =============================================================================
# FUNCOES DE GRAYLOG
# =============================================================================

send_to_graylog() {
    local level=$1
    local message=$2
    local timestamp=$(date +%s%3N)
    
    if [ "$GRAYLOG_ENABLED" != true ]; then
        return
    fi
    
    # Verificar se nc (netcat) esta disponivel
    if ! command -v nc &> /dev/null; then
        log_warn "netcat (nc) nao encontrado. Nao e possivel enviar para Graylog."
        return
    fi
    
    # Formatar mensagem GELF
    local gelf_message=$(printf '{"version":"1.1","host":"%s","short_message":"%s","timestamp":%s,"_level":%s}' \
        "$(hostname)" "$message" "$timestamp" "$level")
    
    # Enviar para Graylog
    echo "$gelf_message" | nc -w 3 "$GRAYLOG_SERVER" "$GRAYLOG_PORT" 2>/dev/null
    
    if [ $? -ne 0 ]; then
        log_warn "Falha ao enviar alerta para Graylog: $message"
    fi
}

# =============================================================================
# FUNCOES DE MONITORAMENTO
# =============================================================================

# Verificar se servico esta ativo
is_service_active() {
    local service_name=$1
    systemctl is-active "$service_name" 2>/dev/null | grep -q "active"
    return $?
}

# Verificar se servico esta habilitado
is_service_enabled() {
    local service_name=$1
    systemctl is-enabled "$service_name" 2>/dev/null | grep -q "enabled"
    return $?
}

# Contar tentativas de restart
get_restart_count() {
    local service_name=$1
    local key="restart_count_${service_name}"
    echo ${!key:-0}
}

# Incrementar contador de tentativas
increment_restart_count() {
    local service_name=$1
    local key="restart_count_${service_name}"
    local current=$(get_restart_count "$service_name")
    eval "restart_count_${service_name}=$((current + 1))"
}

# Resetar contador de tentativas
reset_restart_count() {
    local service_name=$1
    local key="restart_count_${service_name}"
    eval "restart_count_${service_name}=0"
}

# Reiniciar servico
restart_service() {
    local service_name=$1
    local module=$2
    
    log_info "Tentando reiniciar servico: $service_name ($module)"
    
    sudo systemctl restart "$service_name" 2>/dev/null
    
    # Aguardar alguns segundos
    sleep "$RESTART_DELAY_SECONDS"
    
    # Verificar se reiniciou com sucesso
    if is_service_active "$service_name"; then
        log_info "Servico $service_name reiniciado com sucesso"
        reset_restart_count "$service_name"
        send_to_graylog "INFO" "Servico $service_name reiniciado automaticamente"
        return 0
    else
        log_warn "Falha ao reiniciar servico: $service_name"
        return 1
    fi
}

# =============================================================================
# FUNCAO PRINCIPAL DE MONITORAMENTO
# =============================================================================

monitor_services() {
    log_info "=========================================="
    log_info "Iniciando monitoramento de servicos"
    log_info "=========================================="
    
    for module in "${!SERVICE_MODULES[@]}"; do
        local service_name="${SERVICE_MODULES[$module]}"
        local is_critical=false
        
        # Verificar se modulo e critico
        for crit_module in "${CRITICAL_MODULES[@]}"; do
            if [ "$module" == "$crit_module" ]; then
                is_critical=true
                break
            fi
        done
        
        # Verificar se servico esta ativo
        if is_service_active "$service_name"; then
            reset_restart_count "$service_name"
            log_info "✓ $module ($service_name) - ATIVO"
        else
            # Servico esta inativo
            local restart_count=$(get_restart_count "$service_name")
            
            if [ "$restart_count" -ge "$MAX_RESTART_ATTEMPTS" ]; then
                # Maximo de tentativas alcancado
                local level="WARNING"
                [ "$is_critical" == true ] && level="CRITICAL"
                
                log_error "ALERTA $level: Servico $module ($service_name) nao responde apos $MAX_RESTART_ATTEMPTS tentativas"
                send_to_graylog "$level" "Brasil SaaS ERP - Servico $module ($service_name) CAIDO. Tentativas de restart: $restart_count"
                
                # Resetar contador para proxima verificacao
                reset_restart_count "$service_name"
            else
                # Tentar reiniciar
                increment_restart_count "$service_name"
                if restart_service "$service_name" "$module"; then
                    log_info "Servico $module ($service_name) reiniciado com sucesso"
                else
                    log_warn "Tentativa $(get_restart_count "$service_name") de restart falhou para $module"
                fi
            fi
        fi
    done
    
    log_info "Monitoramento concluido. Proximo check em $CHECK_INTERVAL_SECONDS segundos"
}

# =============================================================================
# FUNCAO DE HEALTH CHECK
# =============================================================================

check_health() {
    local all_healthy=true
    
    log_info "=========================================="
    log_info "Verificando health de todos os servicos"
    log_info "=========================================="
    
    for module in "${!SERVICE_MODULES[@]}"; do
        local service_name="${SERVICE_MODULES[$module]}"
        
        if is_service_active "$service_name"; then
            log_info "✓ $module - ATIVO"
        else
            log_error "✗ $module - INATIVO"
            all_healthy=false
        fi
    done
    
    if [ "$all_healthy" == true ]; then
        log_info "Todos os servicos estao saudaveis"
        return 0
    else
        log_error "Alguns servicos estao com problemas"
        return 1
    fi
}

# =============================================================================
# FUNCOES DE CONTROLE
# =============================================================================

start_watchdog() {
    # Verificar se ja esta rodando
    if [ -f "$WATCHDOG_PID" ]; then
        local pid=$(cat "$WATCHDOG_PID")
        if ps -p "$pid" > /dev/null 2>&1; then
            log_warn "Watchdog ja esta rodando (PID: $pid)"
            return 1
        else
            # Processo morreu, remover PID file
            rm -f "$WATCHDOG_PID"
        fi
    fi
    
    # Criar diretorio de logs
    mkdir -p "$(dirname "$WATCHDOG_LOG")"
    
    # Salvar PID
    echo $$ > "$WATCHDOG_PID"
    
    log_info "Watchdog iniciado (PID: $$)"
    
    # Loop infinito de monitoramento
    while true; do
        monitor_services
        sleep "$CHECK_INTERVAL_SECONDS"
    done
}

stop_watchdog() {
    if [ -f "$WATCHDOG_PID" ]; then
        local pid=$(cat "$WATCHDOG_PID")
        if ps -p "$pid" > /dev/null 2>&1; then
            kill "$pid"
            rm -f "$WATCHDOG_PID"
            log_info "Watchdog parado (PID: $pid)"
            return 0
        else
            log_warn "Watchdog nao esta rodando (PID: $pid)"
            rm -f "$WATCHDOG_PID"
            return 1
        fi
    else
        log_warn "Nenhum watchdog em execucao"
        return 1
    fi
}

status_watchdog() {
    if [ -f "$WATCHDOG_PID" ]; then
        local pid=$(cat "$WATCHDOG_PID")
        if ps -p "$pid" > /dev/null 2>&1; then
            log_info "Watchdog esta rodando (PID: $pid)"
            return 0
        else
            log_warn "Watchdog esta registrado mas nao esta rodando (PID: $pid)"
            return 1
        fi
    else
        log_info "Watchdog nao esta rodando"
        return 1
    fi
}

# =============================================================================
# FUNCAO DE INSTALACAO COMO SERVICO SYSTEMd
# =============================================================================

install_watchdog_service() {
    local service_file="/etc/systemd/system/brasil_saas-watchdog.service"
    
    cat > /tmp/brasil_saas-watchdog.service <<EOF
[Unit]
Description=Brasil SaaS ERP - Service Watchdog
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=$BASE_DIR
ExecStart=/bin/bash $BASE_DIR/scripts/service-watchdog.sh start
Restart=always
RestartSec=30

[Install]
WantedBy=multi-user.target
EOF
    
    sudo cp /tmp/brasil_saas-watchdog.service "$service_file"
    sudo systemctl daemon-reload
    sudo systemctl enable brasil_saas-watchdog
    sudo systemctl start brasil_saas-watchdog
    
    log_info "Servico watchdog instalado e iniciado"
}

uninstall_watchdog_service() {
    local service_file="/etc/systemd/system/brasil_saas-watchdog.service"
    
    sudo systemctl stop brasil_saas-watchdog 2>/dev/null || true
    sudo systemctl disable brasil_saas-watchdog 2>/dev/null || true
    sudo rm -f "$service_file"
    sudo systemctl daemon-reload
    
    log_info "Servico watchdog desinstalado"
}

# =============================================================================
# FUNCOES DE ALERTA ESTENDIDO
# =============================================================================

send_alert() {
    local service_name=$1
    local module=$2
    local status=$3
    local attempts=$4
    
    local message="Brasil SaaS ERP ALERT: Servico $module ($service_name) - Status: $status, Tentativas: $attempts"
    
    # Enviar para Graylog
    send_to_graylog "CRITICAL" "$message"
    
    # Log local
    log_critical "$message"
}

# =============================================================================
# FUNCAO DE NOTIFICACAO DE STATUS
# =============================================================================

notify_status() {
    local message="Brasil SaaS ERP Status: $(date '+%Y-%m-%d %H:%M:%S')"
    local active_count=0
    local inactive_count=0
    
    for module in "${!SERVICE_MODULES[@]}"; do
        local service_name="${SERVICE_MODULES[$module]}"
        if is_service_active "$service_name"; then
            active_count=$((active_count + 1))
        else
            inactive_count=$((inactive_count + 1))
        fi
    done
    
    message="$message | Ativos: $active_count | Inativos: $inactive_count"
    send_to_graylog "INFO" "$message"
    log_info "$message"
}

# =============================================================================
# MAIN
# =============================================================================

# Mudar para diretorio base
cd "$BASE_DIR" 2>/dev/null || {
    echo -e "${RED}Erro: Diretorio base nao encontrado: $BASE_DIR${NC}"
    exit 1
}

# Verificar se esta rodando como root para operacoes systemctl
if [ "$EUID" -ne 0 ] && [[ "$1" =~ (start|stop|restart|install|uninstall) ]]; then
    echo -e "${RED}Este comando requer privilégios de root${NC}"
    exit 1
fi

# Parsing de argumentos
COMMAND="${1:-status}"
MODULE="$2"

case "$COMMAND" in
    start)
        start_watchdog
        ;;
    stop)
        stop_watchdog
        ;;
    restart)
        stop_watchdog
        sleep 2
        start_watchdog
        ;;
    status)
        status_watchdog
        ;;
    check)
        check_health
        ;;
    monitor)
        monitor_services
        ;;
    install)
        install_watchdog_service
        ;;
    uninstall)
        uninstall_watchdog_service
        ;;
    notify)
        notify_status
        ;;
    --help|-h|help)
        echo "Uso: $0 [COMMAND]"
        echo ""
        echo "Comandos:"
        echo "  start       - Inicia o watchdog em background"
        echo "  stop        - Para o watchdog"
        echo "  restart     - Reinicia o watchdog"
        echo "  status      - Mostra status do watchdog"
        echo "  check       - Verifica health de todos os servicos"
        echo "  monitor     - Executa uma vez o monitoramento"
        echo "  install     - Instala como servico systemd"
        echo "  uninstall   - Desinstala o servico systemd"
        echo "  notify      - Envia notificacao de status para Graylog"
        echo ""
        exit 0
        ;;
    *)
        echo -e "${RED}Comando nao reconhecido: $COMMAND${NC}"
        echo "Use $0 --help para ver opcoes"
        exit 1
        ;;
esac

exit 0
