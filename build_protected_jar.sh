#!/bin/bash
# Script para construir o JAR protegido do Brasil SaaS ERP

echo "==========================================="
echo "Construindo o JAR protegido do Brasil SaaS ERP"
echo "==========================================="

# Verificar se o Maven está disponível
if ! command -v mvn &> /dev/null; then
    echo "Erro: Maven não encontrado!"
    exit 1
fi

# Compilar o projeto
echo "Compilando o projeto..."
mvn clean compile -DskipTests

if [ $? -ne 0 ]; then
    echo "Erro na compilação do projeto!"
    exit 1
fi

# Construir o JAR
echo "Construindo o JAR..."
mvn package -DskipTests

if [ $? -ne 0 ]; then
    echo "Erro na construção do JAR!"
    exit 1
fi

# Verificar se o JAR foi criado
JAR_FILE="target/brasil-saas-erp-1.0.0-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Erro: JAR não encontrado em $JAR_FILE"
    exit 1
fi

echo "JAR criado com sucesso: $JAR_FILE"

# Criar diretório para o JAR protegido
mkdir -p protected

# Copiar o JAR para o diretório protegido
cp "$JAR_FILE" protected/

echo "JAR copiado para o diretório protegido."

echo ""
echo "==========================================="
echo "Construção do JAR protegido concluída!"
echo "==========================================="

# Mostrar informações sobre o JAR
JAR_FILE_PATH="protected/brasil-saas-erp-1.0.0-SNAPSHOT.jar"

JAR_SIZE=$(du -h "$JAR_FILE_PATH" | cut -f1)
echo "Tamanho do JAR: $JAR_SIZE"
echo "Localização: $JAR_FILE_PATH"

echo ""
echo "Para executar o sistema:"
echo "  java -jar $JAR_FILE_PATH"
echo ""
echo "Para executar com perfil de produção:"
echo "  java -jar $JAR_FILE_PATH --spring.profiles.active=prod"
echo ""
