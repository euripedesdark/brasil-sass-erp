#!/bin/bash
# Brasil SaaS ERP - Graylog Monitor
# Script para enviar metricas e logs para o Graylog
# Integra com o service-watchdog para observabilidade completa

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

# Configuracoes Graylog
GRAYLOG_SERVER="${GRAYLOG_SERVER:-127.0.0.1}"
GRAYLOG_PORT="${GRAYLOG_PORT:-12201}"
GRAYLOG_ENABLED="${GRAYLOG_ENABLED:-true}"

# Arquivo de log
GRAYLOG_MONITOR_LOG="/var/log/brasil_saas/graylog-monitor.log"

# Intervalo de coleta (segundos)
COLLECT_INTERVAL="${COLLECT_INTERVAL:-60}"

# =============================================================================
# FUNCOES DE LOG
# =============================================================================

log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    local log_entry="[$timestamp] [$level] $message"
    
    echo -e "${log_entry}"
    echo "$log_entry" >> "$GRAYLOG_MONITOR_LOG"
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

# =============================================================================
# FUNCOES GRAYLOG
# =============================================================================

# Enviar mensagem GELF para Graylog
send_gelf() {
    local short_message=$1
    local full_message=$2
    local level=${3:-INFO}
    local timestamp=$(date +%s%3N)
    local host=$(hostname)
    
    if [ "$GRAYLOG_ENABLED" != "true" ]; then
        return
    fi
    
    # Verificar se nc esta disponivel
    if ! command -v nc &> /dev/null; then
        log_warn "netcat (nc) nao encontrado. Nao e possivel enviar para Graylog."
        return
    fi
    
    # Formatar JSON GELF
    local gelf_json=$(cat <<EOF
{
  "version": "1.1",
  "host": "$host",
  "short_message": "$short_message",
  "full_message": "$full_message",
  "timestamp": $timestamp,
  "_level": "$level",
  "_source": "brasil_saas-erp",
  "_type": "monitoring"
}
EOF
    )
    
    # Enviar para Graylog
    echo "$gelf_json" | nc -w 3 "$GRAYLOG_SERVER" "$GRAYLOG_PORT" 2>/dev/null
    
    if [ $? -ne 0 ]; then
        log_warn "Falha ao enviar para Graylog: $short_message"
    fi
}

# =============================================================================
# FUNCOES DE COLETA DE METRICAS
# =============================================================================

# Coleta metricas do sistema
collect_system_metrics() {
    log_info "Coletando metricas do sistema..."
    
    # CPU
    local cpu_usage=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')
    
    # Memoria
    local total_mem=$(free -m | awk '/Mem:/ {print $2}')
    local used_mem=$(free -m | awk '/Mem:/ {print $3}')
    local mem_usage=$((used_mem * 100 / total_mem))
    
    # Disco
    local disk_usage=$(df -h / | awk 'NR==2 {print $5}' | tr -d '%')
    
    # Enviar para Graylog
    send_gelf "System Metrics" "CPU: ${cpu_usage}%, Memory: ${mem_usage}%, Disk: ${disk_usage}%" "INFO"
    
    echo "CPU: ${cpu_usage}% | Memory: ${mem_usage}% | Disk: ${disk_usage}%"
}

# Coleta status de todos os servicos
collect_service_status() {
    log_info "Coletando status dos servicos..."
    
    # Lista de servicos
    local services=(
        "brasil_saas-erp-core"
        "brasil_saas-erp-cadastro"
        "brasil_saas-erp-financeiro"
        "brasil_saas-erp-vendas"
        "brasil_saas-erp-compras"
        "brasil_saas-erp-estoque"
        "brasil_saas-erp-fiscal"
        "brasil_saas-erp-rh"
        "brasil_saas-erp-ia"
        "brasil_saas-erp-servicos"
        "brasil_saas-erp-bi"
        "brasil_saas-erp-producao"
    )
    
    local active_count=0
    local inactive_count=0
    local status_json="["
    
    for service in "${services[@]}"; do
        local status=$(systemctl is-active "$service" 2>/dev/null || echo "inactive")
        local enabled=$(systemctl is-enabled "$service" 2>/dev/null || echo "disabled")
        
        if [ "$status" == "active" ]; then
            active_count=$((active_count + 1))
        else
            inactive_count=$((inactive_count + 1))
        fi
        
        status_json+="{\"service\":\"$service\",\"status\":\"$status\",\"enabled\":\"$enabled\"},"
    done
    
    # Remover ultma virgula
    status_json="${status_json%,}"
    status_json+="]"
    
    # Enviar para Graylog
    send_gelf "Service Status" "$status_json | Active: $active_count | Inactive: $inactive_count" "INFO"
    
    echo "Servicos Ativos: $active_count | Inativos: $inactive_count"
}

# Coleta metricas Java/JVM
collect_jvm_metrics() {
    log_info "Coletando metricas JVM..."
    
    # Lista de aplicacoes Java
    local java_procs=$(pgrep -f "java.*brasil_saas" 2>/dev/null || echo "")
    
    if [ -z "$java_procs" ]; then
        log_warn "Nenhum processo Java encontrado"
        return
    fi
    
    for pid in $java_procs; do
        local jar_file=$(ps -p "$pid" -o cmd= | grep -oP 'brasil_saas-[^.]+\.jar' | head -1)
        local module=$(echo "$jar_file" | sed 's/brasil_saas-//;s/\.jar//' | sed 's/-/_/g')
        
        if [ -n "$jar_file" ]; then
            # Memoria
            local jvm_mem=$(jstat -gc "$pid" 2>/dev/null | tail -1 | awk '{print $3 + $4 + $8 + $10}')
            local jvm_mem_mb=$((jvm_mem / 1024))
            
            # Threads
            local threads=$(jstack "$pid" 2>/dev/null | grep -c "^\"" || echo "0")
            
            send_gelf "JVM Metrics" "Module: $module | Memory: ${jvm_mem_mb}MB | Threads: $threads" "INFO"
            
            echo "JVM $module: Memory=${jvm_mem_mb}MB, Threads=$threads"
        fi
    done
}

# Coleta logs de erro recentes
collect_error_logs() {
    log_info "Verificando logs de erro..."
    
    # Diretorio de logs
    local log_dir="/var/log/brasil_saas"
    
    if [ ! -d "$log_dir" ]; then
        log_warn "Diretorio de logs nao encontrado: $log_dir"
        return
    fi
    
    # Procurar por erros nos ultimos 5 minutos
    local error_count=$(grep -r "ERROR\|Exception\|Error" "$log_dir" 2>/dev/null | grep -c "$(date '+%Y-%m-%d %H:%M' --date='5 minutes ago')" || echo "0")
    
    if [ "$error_count" -gt 0 ]; then
        send_gelf "Error Alert" "$error_count erros encontrados nos ultimos 5 minutos" "ERROR"
        log_error "$error_count erros encontrados nos ultimos 5 minutos"
    fi
    
    echo "Erros recentes: $error_count"
}

# Coleta metricas do banco de dados
collect_db_metrics() {
    log_info "Coletando metricas do banco de dados..."
    
    # Verificar se pg_isready esta disponivel
    if command -v pg_isready &> /dev/null; then
        local db_status=$(pg_isready -h localhost -p 5432 -U postgres 2>&1 || echo "down")
        
        if [ "$db_status" == "localhost:5432 - accepting connections" ]; then
            # Contar conexoes
            local connections=$(psql -h localhost -p 5432 -U postgres -t -c "SELECT count(*) FROM pg_stat_activity;" 2>/dev/null || echo "0")
            
            # Tamanho do banco
            local db_size=$(psql -h localhost -p 5432 -U postgres -t -c "SELECT pg_size_pretty(pg_database_size('brasil_saas'));" 2>/dev/null || echo "0")
            
            send_gelf "Database Metrics" "Status: UP | Connections: $connections | Size: $db_size" "INFO"
            echo "Database: UP | Connections: $connections | Size: $db_size"
        else
            send_gelf "Database Alert" "PostgreSQL esta DOWN: $db_status" "CRITICAL"
            log_error "PostgreSQL esta DOWN: $db_status"
        fi
    else
        log_warn "pg_isready nao encontrado. Nao e possivel verificar PostgreSQL."
    fi
}

# Coleta metricas do Actuator (Spring Boot)
collect_actuator_metrics() {
    log_info "Coletando metricas do Actuator..."
    
    # Lista de endpoints actuator
    local endpoints=(
        "http://localhost:8081/actuator/health"
        "http://localhost:8082/actuator/health"
        "http://localhost:8083/actuator/health"
        "http://localhost:8084/actuator/health"
        "http://localhost:8085/actuator/health"
        "http://localhost:8086/actuator/health"
        "http://localhost:8087/actuator/health"
        "http://localhost:8088/actuator/health"
        "http://localhost:8089/actuator/health"
        "http://localhost:8090/actuator/health"
        "http://localhost:8091/actuator/health"
    )
    
    local up_count=0
    local down_count=0
    local health_json="["
    
    for endpoint in "${endpoints[@]}"; do
        local port=$(echo "$endpoint" | grep -oP '\d+' | tail -1)
        local module="unknown"
        
        case "$port" in
            8081) module="core" ;;
            8082) module="cadastro" ;;
            8083) module="financeiro" ;;
            8084) module="vendas" ;;
            8085) module="compras" ;;
            8086) module="estoque" ;;
            8087) module="fiscal" ;;
            8088) module="rh" ;;
            8089) module="ia" ;;
            8090) module="producao" ;;
            8091) module="bi" ;;
        esac
        
        local status=$(curl -s -o /dev/null -w "%{http_code}" "$endpoint" 2>/dev/null || echo "000")
        
        if [ "$status" == "200" ]; then
            up_count=$((up_count + 1))
            local health=$(curl -s "$endpoint" 2>/dev/null | jq -r '.status' 2>/dev/null || echo "unknown")
            health_json+="{\"module\":\"$module\",\"port\":$port,\"status\":\"UP\",\"health\":\"$health\"},"
        else
            down_count=$((down_count + 1))
            health_json+="{\"module\":\"$module\",\"port\":$port,\"status\":\"DOWN\"},"
        fi
    done
    
    # Remover ultima virgula
    health_json="${health_json%,}"
    health_json+="]"
    
    send_gelf "Actuator Health" "$health_json | UP: $up_count | DOWN: $down_count" "INFO"
    
    echo "Actuator: UP=$up_count | DOWN=$down_count"
}

# =============================================================================
# FUNCAO PRINCIPAL
# =============================================================================

# Criar diretorio de logs
mkdir -p "$(dirname "$GRAYLOG_MONITOR_LOG")"

# Mudar para diretorio base
cd "$BASE_DIR" 2>/dev/null || {
    echo -e "${RED}Erro: Diretorio base nao encontrado: $BASE_DIR${NC}"
    exit 1
}

# Parsing de argumentos
COMMAND="${1:-collect}"

case "$COMMAND" in
    collect)
        # Coletar todas as metricas
        log_info "=========================================="
        log_info "Iniciando coleta de metricas para Graylog"
        log_info "=========================================="
        
        collect_system_metrics
        echo ""
        
        collect_service_status
        echo ""
        
        collect_jvm_metrics
        echo ""
        
        collect_error_logs
        echo ""
        
        collect_db_metrics
        echo ""
        
        collect_actuator_metrics
        echo ""
        
        log_info "Coleta de metricas concluida"
        ;;
    system)
        collect_system_metrics
        ;;
    services)
        collect_service_status
        ;;
    jvm)
        collect_jvm_metrics
        ;;
    errors)
        collect_error_logs
        ;;
    db|database)
        collect_db_metrics
        ;;
    health|actuator)
        collect_actuator_metrics
        ;;
    continuous)
        log_info "Iniciando monitoramento continuo (Ctrl+C para parar)..."
        while true; do
            echo ""
            echo "=========================================="
            echo "Coleta: $(date '+%Y-%m-%d %H:%M:%S')"
            echo "=========================================="
            
            collect_system_metrics
            collect_service_status
            collect_jvm_metrics
            collect_error_logs
            collect_db_metrics
            collect_actuator_metrics
            
            sleep "$COLLECT_INTERVAL"
        done
        ;;
    test)
        # Teste de conexao com Graylog
        send_gelf "Test Message" "Graylog Monitor esta funcionando" "INFO"
        log_info "Mensagem de teste enviada para Graylog"
        ;;
    --help|-h|help)
        echo "Uso: $0 [COMMAND]"
        echo ""
        echo "Comandos:"
        echo "  collect        - Coletar todas as metricas uma vez"
        echo "  system        - Coletar metricas do sistema"
        echo "  services      - Coletar status dos servicos"
        echo "  jvm          - Coletar metricas JVM"
        echo "  errors       - Verificar logs de erro"
        echo "  db|database  - Coletar metricas do banco"
        echo "  health       - Coletar metricas do Actuator"
        echo "  continuous    - Monitoramento continuo"
        echo "  test         - Enviar mensagem de teste"
        echo ""
        echo "Variaveis de ambiente:"
        echo "  GRAYLOG_SERVER  - Servidor Graylog (default: 127.0.0.1)"
        echo "  GRAYLOG_PORT    - Porta Graylog (default: 12201)"
        echo "  GRAYLOG_ENABLED - Ativar/desativar (default: true)"
        echo "  COLLECT_INTERVAL - Intervalo de coleta (default: 60)"
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
