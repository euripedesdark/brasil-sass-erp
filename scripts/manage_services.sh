#!/bin/bash
# Script para gerenciar todos os serviços do Brasil SaaS ERP

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Lista de todos os módulos
ALL_MODULES=("core" "shared" "cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos")

# Portas dos módulos
declare -A MODULE_PORTS
MODULE_PORTS=(
    ["core"]="8081"
    ["shared"]="N/A"
    ["cadastro"]="8082"
    ["financeiro"]="8083"
    ["vendas"]="8084"
    ["compras"]="8085"
    ["estoque"]="8086"
    ["fiscal"]="8087"
    ["rh"]="8088"
    ["ia"]="8089"
    ["servicos"]="8080"
)

# Descrições
declare -A MODULE_DESC
MODULE_DESC=(
    ["core"]="Core - Autenticação e Autorização"
    ["shared"]="Shared - Classes Compartilhadas (Library)"
    ["cadastro"]="Cadastro - Pessoas, Produtos, Clientes"
    ["financeiro"]="Financeiro - Contas, Lançamentos"
    ["vendas"]="Vendas - Pedidos e Orçamentos"
    ["compras"]="Compras - Pedidos de Compra"
    ["estoque"]="Estoque - Controle de Saldos"
    ["fiscal"]="Fiscal - NF-e, NFS-e, eSocial"
    ["rh"]="RH - Funcionários e Folha"
    ["ia"]="IA - Inteligência Artificial"
    ["servicos"]="Serviços - Ordens de Serviço"
)

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
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Função para mostrar ajuda
show_help() {
    echo "Uso: $0 [COMMAND] [MODULE] [OPTIONS]"
    echo ""
    echo "Comandos:"
    echo "  build [MODULE] [--clean] [--skip-tests] [--install]  - Build de um módulo"
    echo "  build-all [--clean] [--skip-tests] [--install]       - Build de todos os módulos"
    echo "  start [MODULE]                                      - Inicia um serviço"
    echo "  stop [MODULE]                                       - Para um serviço"
    echo "  restart [MODULE]                                    - Reinicia um serviço"
    echo "  status [MODULE]                                     - Status de um serviço"
    echo "  install [MODULE]                                    - Instala serviço no systemd"
    echo "  uninstall [MODULE]                                  - Remove serviço do systemd"
    echo "  list                                              - Lista todos os módulos"
    echo "  list-services                                     - Lista serviços instalados"
    echo "  start-all                                         - Inicia todos os serviços"
    echo "  stop-all                                          - Para todos os serviços"
    echo "  restart-all                                       - Reinicia todos os serviços"
    echo ""
    exit 0
}

# Função para listar módulos
list_modules() {
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Módulos do Brasil SaaS ERP${NC}"
    echo -e "${GREEN}=========================================${NC}"
    printf "%-20s %-10s %s\n" "MÓDULO" "PORTA" "DESCRIÇÃO"
    echo "-------------------------------------------"
    for MODULE in "${ALL_MODULES[@]}"; do
        PORT="${MODULE_PORTS[$MODULE]}"
        DESC="${MODULE_DESC[$MODULE]}"
        printf "%-20s %-10s %s\n" "$MODULE" "$PORT" "$DESC"
    done
    echo ""
}

# Função para listar serviços instalados
list_services() {
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Serviços Brasil SaaS ERP Instalados${NC}"
    echo -e "${GREEN}=========================================${NC}"
    for MODULE in "${ALL_MODULES[@]}"; do
        SERVICE_FILE="/etc/systemd/system/brasil_saas-erp-$MODULE.service"
        if [ -f "$SERVICE_FILE" ]; then
            STATUS=$(systemctl is-active brasil_saas-erp-$MODULE 2>/dev/null || echo "inactive")
            ENABLED=$(systemctl is-enabled brasil_saas-erp-$MODULE 2>/dev/null || echo "disabled")
            printf "%-20s %-12s %-12s %s\n" "$MODULE" "$STATUS" "$ENABLED" "(${MODULE_DESC[$MODULE]})"
        fi
    done
    echo ""
}

# Função para build de um módulo
build_module() {
    local MODULE=$1
    local CLEAN=$2
    local SKIP_TESTS=$3
    local INSTALL=$4
    
    if [ ! -d "modules/$MODULE" ]; then
        echo -e "${RED}Erro: Módulo '$MODULE' não encontrado${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}Buildando módulo: $MODULE${NC}"
    
    local MVN_CMD="mvn"
    [ "$CLEAN" = true ] && MVN_CMD="$MVN_CMD clean"
    [ "$SKIP_TESTS" = true ] && MVN_CMD="$MVN_CMD -DskipTests"
    [ "$INSTALL" = true ] && MVN_CMD="$MVN_CMD install" || MVN_CMD="$MVN_CMD package"
    
    $MVN_CMD -pl modules/$MODULE -am -q
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}  ✓ Build do módulo '$MODULE' concluído${NC}"
    else
        echo -e "${RED}  ✗ Build do módulo '$MODULE' falhou${NC}"
        exit 1
    fi
}

# Função para build de todos os módulos
build_all() {
    local CLEAN=$1
    local SKIP_TESTS=$2
    local INSTALL=$3
    
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Build de Todos os Módulos${NC}"
    echo -e "${GREEN}=========================================${NC}"
    
    # Build na ordem correta (shared e core primeiro)
    local ORDERED_MODULES=("shared" "core" "cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos")
    
    for MODULE in "${ORDERED_MODULES[@]}"; do
        build_module "$MODULE" "$CLEAN" "$SKIP_TESTS" "$INSTALL"
    done
    
    echo -e "${GREEN}"
    echo "=========================================="
    echo "Build de todos os módulos concluído!"
    echo "=========================================="
    echo -e "${NC}"
}

# Função para instalar serviço
install_service() {
    local MODULE=$1
    
    if [ "$MODULE" = "shared" ]; then
        echo -e "${YELLOW}Shared é uma library, não um serviço. Ignorando...${NC}"
        return
    fi
    
    local SERVICE_FILE="systemd_units/brasil_saas-erp-$MODULE.service"
    local TARGET_FILE="/etc/systemd/system/brasil_saas-erp-$MODULE.service"
    
    if [ ! -f "$SERVICE_FILE" ]; then
        echo -e "${RED}Erro: Arquivo de serviço '$SERVICE_FILE' não encontrado${NC}"
        exit 1
    fi
    
    echo -e "${YELLOW}Instalando serviço: $MODULE${NC}"
    
    # Copiar arquivo de serviço
    sudo cp "$SERVICE_FILE" "$TARGET_FILE"
    
    # Recarregar systemd
    sudo systemctl daemon-reload
    
    # Habilitar serviço
    sudo systemctl enable brasil_saas-erp-$MODULE
    
    # Iniciar serviço
    sudo systemctl start brasil_saas-erp-$MODULE
    
    echo -e "${GREEN}  ✓ Serviço '$MODULE' instalado e iniciado${NC}"
}

# Função para desinstalar serviço
uninstall_service() {
    local MODULE=$1
    
    if [ "$MODULE" = "shared" ]; then
        echo -e "${YELLOW}Shared é uma library, não um serviço. Ignorando...${NC}"
        return
    fi
    
    local TARGET_FILE="/etc/systemd/system/brasil_saas-erp-$MODULE.service"
    
    if [ ! -f "$TARGET_FILE" ]; then
        echo -e "${YELLOW}Serviço '$MODULE' não está instalado${NC}"
        return
    fi
    
    echo -e "${YELLOW}Desinstalando serviço: $MODULE${NC}"
    
    # Parar serviço
    sudo systemctl stop brasil_saas-erp-$MODULE 2>/dev/null || true
    
    # Desabilitar serviço
    sudo systemctl disable brasil_saas-erp-$MODULE 2>/dev/null || true
    
    # Remover arquivo
    sudo rm -f "$TARGET_FILE"
    
    # Recarregar systemd
    sudo systemctl daemon-reload
    
    echo -e "${GREEN}  ✓ Serviço '$MODULE' desinstalado${NC}"
}

# Função para iniciar serviço
start_service() {
    local MODULE=$1
    
    if [ "$MODULE" = "shared" ]; then
        echo -e "${YELLOW}Shared é uma library, não um serviço. Ignorando...${NC}"
        return
    fi
    
    echo -e "${YELLOW}Iniciando serviço: $MODULE${NC}"
    sudo systemctl start brasil_saas-erp-$MODULE
    echo -e "${GREEN}  ✓ Comando enviado${NC}"
}

# Função para parar serviço
stop_service() {
    local MODULE=$1
    
    if [ "$MODULE" = "shared" ]; then
        echo -e "${YELLOW}Shared é uma library, não um serviço. Ignorando...${NC}"
        return
    fi
    
    echo -e "${YELLOW}Parando serviço: $MODULE${NC}"
    sudo systemctl stop brasil_saas-erp-$MODULE
    echo -e "${GREEN}  ✓ Comando enviado${NC}"
}

# Função para reiniciar serviço
restart_service() {
    local MODULE=$1
    
    if [ "$MODULE" = "shared" ]; then
        echo -e "${YELLOW}Shared é uma library, não um serviço. Ignorando...${NC}"
        return
    fi
    
    echo -e "${YELLOW}Reiniciando serviço: $MODULE${NC}"
    sudo systemctl restart brasil_saas-erp-$MODULE
    echo -e "${GREEN}  ✓ Comando enviado${NC}"
}

# Função para status do serviço
status_service() {
    local MODULE=$1
    
    if [ "$MODULE" = "shared" ]; then
        echo -e "${YELLOW}Shared é uma library, não um serviço.${NC}"
        return
    fi
    
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Status do Serviço: $MODULE${NC}"
    echo -e "${GREEN}=========================================${NC}"
    sudo systemctl status brasil_saas-erp-$MODULE --no-pager
    echo ""
}

# Funções para todos os serviços
start_all_services() {
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Iniciando Todos os Serviços${NC}"
    echo -e "${GREEN}=========================================${NC}"
    
    for MODULE in "${ALL_MODULES[@]}"; do
        [ "$MODULE" = "shared" ] && continue
        start_service "$MODULE"
    done
    
    echo -e "${GREEN}  ✓ Todos os serviços iniciais solicitados${NC}"
}

stop_all_services() {
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Parando Todos os Serviços${NC}"
    echo -e "${GREEN}=========================================${NC}"
    
    # Parar na ordem inversa
    for (( i=${#ALL_MODULES[@]}-1; i>=0; i-- )); do
        MODULE=${ALL_MODULES[$i]}
        [ "$MODULE" = "shared" ] && continue
        stop_service "$MODULE"
    done
    
    echo -e "${GREEN}  ✓ Todos os serviços foram solicitados para parar${NC}"
}

restart_all_services() {
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN} Reiniciando Todos os Serviços${NC}"
    echo -e "${GREEN}=========================================${NC}"
    
    for MODULE in "${ALL_MODULES[@]}"; do
        [ "$MODULE" = "shared" ] && continue
        restart_service "$MODULE"
    done
    
    echo -e "${GREEN}  ✓ Todos os serviços foram solicitados para reiniciar${NC}"
}

# Parsing de argumentos
COMMAND="$1"
MODULE="$2"
OPTIONS="${@:3}"

case "$COMMAND" in
    build)
        CLEAN=false
        SKIP_TESTS=false
        INSTALL=false
        
        for opt in $OPTIONS; do
            case "$opt" in
                --clean) CLEAN=true ;;
                --skip-tests) SKIP_TESTS=true ;;
                --install) INSTALL=true ;;
            esac
        done
        
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        
        build_module "$MODULE" "$CLEAN" "$SKIP_TESTS" "$INSTALL"
        ;;
    build-all)
        CLEAN=false
        SKIP_TESTS=false
        INSTALL=false
        
        for opt in $OPTIONS; do
            case "$opt" in
                --clean) CLEAN=true ;;
                --skip-tests) SKIP_TESTS=true ;;
                --install) INSTALL=true ;;
            esac
        done
        
        build_all "$CLEAN" "$SKIP_TESTS" "$INSTALL"
        ;;
    start)
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        start_service "$MODULE"
        ;;
    stop)
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        stop_service "$MODULE"
        ;;
    restart)
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        restart_service "$MODULE"
        ;;
    status)
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        status_service "$MODULE"
        ;;
    install)
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        install_service "$MODULE"
        ;;
    uninstall)
        if [ -z "$MODULE" ]; then
            echo -e "${RED}Erro: Módulo não especificado${NC}"
            show_help
            exit 1
        fi
        uninstall_service "$MODULE"
        ;;
    list)
        list_modules
        ;;
    list-services)
        list_services
        ;;
    start-all)
        start_all_services
        ;;
    stop-all)
        stop_all_services
        ;;
    restart-all)
        restart_all_services
        ;;
    --help|-h|help)
        show_help
        ;;
    *)
        echo -e "${RED}Comando não reconhecido: $COMMAND${NC}"
        show_help
        exit 1
        ;;
esac
