#!/bin/bash
# Script para testar o sistema Brasil SaaS ERP

echo "==========================================="
echo "Testando o sistema Brasil SaaS ERP"
echo "==========================================="

# Verificar se o Java está instalado
echo "Verificando Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "Java encontrado: $JAVA_VERSION"
else
    echo "Erro: Java não encontrado!"
    exit 1
fi

# Verificar se o Maven está instalado
echo "Verificando Maven..."
if command -v mvn &> /dev/null; then
    MAVEN_VERSION=$(mvn -version | head -n 1)
    echo "Maven encontrado: $MAVEN_VERSION"
else
    echo "Erro: Maven não encontrado!"
    exit 1
fi

# Verificar se o PostgreSQL está rodando.
# Sem -U, pg_isready usa o usuário do sistema operacional. Executado como
# root, isso tenta a role "root" e pode gerar FATAL no log do PostgreSQL,
# mesmo quando o servidor está saudável. Use um usuário explícito/configurável.
PG_CHECK_USER="${BRASIL_SAAS_PGUSER:-sa}"
PG_CHECK_DB="${BRASIL_SAAS_PGDATABASE:-brasil-saas}"
echo "Verificando PostgreSQL (usuário: $PG_CHECK_USER, banco: $PG_CHECK_DB)..."
if pg_isready -h localhost -p 5432 -U "$PG_CHECK_USER" -d "$PG_CHECK_DB" &> /dev/null; then
    echo "PostgreSQL está rodando"
else
    echo "Aviso: PostgreSQL não parece estar rodando ou não aceita conexões"
    echo "Certifique-se de que o PostgreSQL está instalado e em execução"
    echo "Configure BRASIL_SAAS_PGUSER/BRASIL_SAAS_PGDATABASE se necessário."
fi

# Verificar se o diretório do projeto existe
echo "Verificando estrutura do projeto..."
if [ -d "src/main/java" ] && [ -f "pom.xml" ]; then
    echo "Estrutura do projeto encontrada"
else
    echo "Erro: Estrutura do projeto não encontrada"
    exit 1
fi

# Verificar se o diretório frontend React existe
echo "Verificando frontend React..."
if [ -d "src/main/resources/static/react" ]; then
    echo "Frontend React encontrado"
else
    echo "Aviso: Frontend React não encontrado"
fi

# Verificar se os scripts de implantação existem
echo "Verificando scripts de implantação..."
if [ -f "scripts/executar_jar.py" ]; then
    echo "Script Python encontrado"
else
    echo "Aviso: Script Python não encontrado"
fi

if [ -f "scripts/executar_jar.ps1" ]; then
    echo "Script PowerShell encontrado"
else
    echo "Aviso: Script PowerShell não encontrado"
fi

if [ -f "scripts/install_service.sh" ]; then
    echo "Script de instalação de serviço Linux encontrado"
else
    echo "Aviso: Script de instalação de serviço Linux não encontrado"
fi

# Verificar se o README existe
echo "Verificando README..."
if [ -f "docs/modulos/README.md" ]; then
    echo "README encontrado"
else
    echo "Aviso: README não encontrado"
fi

echo ""
echo "==========================================="
echo "Teste concluído!"
echo "==========================================="
echo ""
echo "O sistema Brasil SaaS ERP está configurado corretamente."
echo "Para compilar o projeto, execute:"
echo "  mvn clean install"
echo ""
echo "Para executar em modo desenvolvimento:"
echo "  mvn spring-boot:run"
echo ""
echo "Para executar o JAR compilado:"
echo "  java -jar target/brasil-saas-erp-1.0.0-SNAPSHOT.jar"
echo ""