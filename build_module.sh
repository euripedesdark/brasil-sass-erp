#!/bin/bash
# Script para build de um módulo específico do Brasil SaaS ERP

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para mostrar ajuda
show_help() {
    echo "Uso: $0 [MODULE_NAME] [OPTIONS]"
    echo ""
    echo "Argumentos:"
    echo "  MODULE_NAME   Nome do módulo (core, cadastro, financeiro, vendas, etc.)"
    echo ""
    echo "Opções:"
    echo "  --skip-tests    Pula os testes durante o build"
    echo "  --clean         Faz clean antes do build"
    echo "  --install       Instala o módulo no repositório local Maven"
    echo "  --help, -h      Mostra esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 core --clean --skip-tests"
    echo "  $0 financeiro --install"
    exit 0
}

# Parsing de argumentos
MODULE=""
SKIP_TESTS=false
CLEAN=false
INSTALL=false

for arg in "$@"; do
    case "$arg" in
        --skip-tests)
            SKIP_TESTS=true
            ;;
        --clean)
            CLEAN=true
            ;;
        --install)
            INSTALL=true
            ;;
        --help|-h)
            show_help
            ;;
        *)
            if [ -z "$MODULE" ]; then
                MODULE="$arg"
            else
                echo -e "${RED}Erro: Módulo não reconhecido ou argumentos inválidos${NC}"
                show_help
                exit 1
            fi
            ;;
    esac
done

if [ -z "$MODULE" ]; then
    echo -e "${RED}Erro: Nenhum módulo especificado${NC}"
    show_help
    exit 1
fi

# Verificar se o módulo existe
MODULE_DIR="modules/$MODULE"
if [ ! -d "$MODULE_DIR" ]; then
    echo -e "${RED}Erro: Módulo '$MODULE' não encontrado em $MODULE_DIR${NC}"
    exit 1
fi

# Verificar se o pom.xml existe
POM_FILE="$MODULE_DIR/pom.xml"
if [ ! -f "$POM_FILE" ]; then
    echo -e "${RED}Erro: pom.xml não encontrado em $POM_FILE${NC}"
    exit 1
fi

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}Build do módulo: $MODULE${NC}"
echo -e "${GREEN}=========================================${NC}"

# CD para o diretório do projeto
cd /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP

# Comando Maven
MVN_CMD="mvn"

if [ "$CLEAN" = true ]; then
    MVN_CMD="$MVN_CMD clean"
fi

if [ "$SKIP_TESTS" = true ]; then
    MVN_CMD="$MVN_CMD -DskipTests"
fi

if [ "$INSTALL" = true ]; then
    MVN_CMD="$MVN_CMD install"
else
    MVN_CMD="$MVN_CMD package"
fi

# Executar build
echo -e "${YELLOW}Executando: $MVN_CMD -pl modules/$MODULE -am${NC}"
$MVN_CMD -pl modules/$MODULE -am

if [ $? -eq 0 ]; then
    echo -e "${GREEN}"
    echo "=========================================="
    echo "Build do módulo '$MODULE' concluído com sucesso!"
    echo "=========================================="
    echo -e "${NC}"
    exit 0
else
    echo -e "${RED}"
    echo "=========================================="
    echo "Build do módulo '$MODULE' falhou!"
    echo "=========================================="
    echo -e "${NC}"
    exit 1
fi
