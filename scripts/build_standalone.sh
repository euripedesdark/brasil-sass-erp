#!/bin/bash
# Script para build de módulos standalone (independentes)

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

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
    echo "Uso: $0 [MODULE] [OPTIONS]"
    echo ""
    echo "Argumentos:"
    echo "  MODULE      Nome do módulo (core, cadastro, financeiro, vendas, etc.)"
    echo ""
    echo "Opções:"
    echo "  --clean         Executa 'mvn clean' antes do build"
    echo "  --skip-tests    Pula os testes durante o build"
    echo "  --install       Instala o módulo no repositório local Maven"
    echo "  --help, -h      Mostra esta ajuda"
    echo ""
    exit 0
}

# Parsing de argumentos
MODULE="$1"
OPTIONS="${@:2}"

if [ -z "$MODULE" ] || [ "$MODULE" = "--help" ] || [ "$MODULE" = "-h" ]; then
    show_help
fi

# Verificar se o módulo existe
MODULE_DIR="modules/$MODULE"
if [ ! -d "$MODULE_DIR" ]; then
    echo -e "${RED}Erro: Módulo '$MODULE' não encontrado em $MODULE_DIR${NC}"
    exit 1
fi

# Cores dos módulos
MODULE_COLORS=(
    "core:${GREEN}"
    "shared:${YELLOW}"
    "cadastro:${GREEN}"
    "financeiro:${GREEN}"
    "vendas:${GREEN}"
    "compras:${GREEN}"
    "estoque:${GREEN}"
    "fiscal:${GREEN}"
    "rh:${GREEN}"
    "ia:${GREEN}"
    "servicos:${GREEN}"
)

# Obter cor do módulo (default: GREEN)
COLOR=${GREEN}
for item in "${MODULE_COLORS[@]}"; do
    IFS=':' read -r m c <<< "$item"
    if [ "$m" = "$MODULE" ]; then
        COLOR="$c"
        break
    fi
done

# Processar opções
CLEAN=false
SKIP_TESTS=false
INSTALL=false

for opt in $OPTIONS; do
    case "$opt" in
        --clean) CLEAN=true ;;
        --skip-tests) SKIP_TESTS=true ;;
        --install) INSTALL=true ;;
        --help|-h) show_help ;;
        *) echo -e "${RED}Opção não reconhecida: $opt${NC}"; show_help ;;
    esac
done

echo -e "${COLOR}=========================================="
echo "  Build Standalone do Módulo: $MODULE"
echo "==========================================${NC}"

# Criar diretório de build temporário
BUILD_DIR="/tmp/brasil_saas-erp-$MODULE-$(date +%s)"
mkdir -p "$BUILD_DIR/src/main/java/br/com/brasil_saas/$MODULE"
mkdir -p "$BUILD_DIR/src/main/resources"

# Copiar arquivos do módulo
if [ -d "$MODULE_DIR/src" ]; then
    echo -e "${YELLOW}Copiando código fonte...${NC}"
    cp -r "$MODULE_DIR/src/"* "$BUILD_DIR/src/"
else
    echo -e "${RED}Erro: Nenhum código fonte encontrado em $MODULE_DIR/src${NC}"
    exit 1
fi

# Copiar pom.xml
if [ -f "$MODULE_DIR/pom.xml" ]; then
    echo -e "${YELLOW}Copiando pom.xml...${NC}"
    cp "$MODULE_DIR/pom.xml" "$BUILD_DIR/pom.xml"
else
    echo -e "${RED}Erro: pom.xml não encontrado em $MODULE_DIR${NC}"
    exit 1
fi

# Verificar se o pom.xml tem parent e remover (para build standalone)
sed -i '/<parent>/,/<\/parent>/d' "$BUILD_DIR/pom.xml"

# Adicionar groupId e version se não existirem
if ! grep -q "<groupId>" "$BUILD_DIR/pom.xml"; then
    sed -i '/<artifactId>/i\    <groupId>br.com.brasil_saas</groupId>' "$BUILD_DIR/pom.xml"
fi
if ! grep -q "<version>" "$BUILD_DIR/pom.xml"; then
    sed -i '/<artifactId>/a\    <version>1.0.0-SNAPSHOT</version>' "$BUILD_DIR/pom.xml"
fi

# Adicionar properties se não existirem
if ! grep -q "<properties>" "$BUILD_DIR/pom.xml"; then
    sed -i '/<modelVersion>/a\
    <properties>\
        <java.version>21</java.version>\
        <maven.compiler.source>21</maven.compiler.source>\
        <maven.compiler.target>21</maven.compiler.target>\
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\
    </properties>' "$BUILD_DIR/pom.xml"
fi

# CD para o diretório de build
cd "$BUILD_DIR"

# Comando Maven
MVN_CMD="mvn"
[ "$CLEAN" = true ] && MVN_CMD="$MVN_CMD clean"
[ "$SKIP_TESTS" = true ] && MVN_CMD="$MVN_CMD -DskipTests"
[ "$INSTALL" = true ] && MVN_CMD="$MVN_CMD install" || MVN_CMD="$MVN_CMD package"

echo -e "${YELLOW}Executando: $MVN_CMD${NC}"
$MVN_CMD -q

if [ $? -eq 0 ]; then
    echo -e "${COLOR}"
    echo "=========================================="
    echo "  Build do módulo '$MODULE' concluído!"
    echo "  JAR: $BUILD_DIR/target/*.jar"
    echo "==========================================${NC}"
    
    # Copiar JAR de volta para o módulo
    if [ -f "$BUILD_DIR/target/*.jar" ]; then
        mkdir -p "$MODULE_DIR/target"
        cp "$BUILD_DIR/target/"*.jar "$MODULE_DIR/target/"
        echo -e "${GREEN}  JAR copiado para: $MODULE_DIR/target/${NC}"
    fi
    
    # Limpar diretório temporário
    rm -rf "$BUILD_DIR"
    exit 0
else
    echo -e "${RED}"
    echo "=========================================="
    echo "  Build do módulo '$MODULE' falhou!"
    echo "==========================================${NC}"
    rm -rf "$BUILD_DIR"
    exit 1
fi
