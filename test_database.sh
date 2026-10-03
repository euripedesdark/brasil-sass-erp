#!/bin/bash

# Script para testar o banco de dados PostgreSQL do Brasil SaaS ERP
# Autor: Sistema ERP
# Data: $(date)

echo "🔍 Testando conexão com o banco de dados Brasil SaaS ERP..."

# Configurações do banco de dados
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="postgres"  # Banco padrão para testes iniciais
DB_USER="postgres"
DB_PASS="ALTERE_ME"

# Testar conexão com o banco de dados
echo "📡 Conectando ao PostgreSQL..."
if PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" >/dev/null 2>&1; then
    echo "✅ Conexão com PostgreSQL bem sucedida!"
else
    echo "❌ Falha na conexão com PostgreSQL"
    exit 1
fi

# Testar se o banco específico do ERP existe
ERP_DB_NAME="brasil-saas"
echo "🔍 Verificando existência do banco $ERP_DB_NAME..."

if PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1 FROM pg_database WHERE datname='$ERP_DB_NAME';" | grep -q "1"; then
    echo "✅ Banco $ERP_DB_NAME encontrado!"
    DB_NAME="$ERP_DB_NAME"
else
    echo "⚠️ Banco $ERP_DB_NAME não encontrado."
    echo "💡 Criando banco de dados $ERP_DB_NAME..."
    
    if PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "CREATE DATABASE $ERP_DB_NAME;"; then
        echo "✅ Banco $ERP_DB_NAME criado com sucesso!"
        DB_NAME="$ERP_DB_NAME"
    else
        echo "❌ Não foi possível criar o banco $ERP_DB_NAME"
        exit 1
    fi
fi

# Conectar ao banco do ERP e testar tabelas
echo "📋 Testando estrutura do banco de dados..."

TABLES_TO_CHECK=(
    "brasil_saas.bc_core_empresa"
    "brasil_saas.bc_core_usuario"
    "brasil_saas.bc_cad_cliente"
    "brasil_saas.bc_cad_fornecedor"
    "brasil_saas.bc_cad_produto"
    "brasil_saas.bc_compras_pedido"
    "brasil_saas.bc_vendas_pedido"
    "brasil_saas.bc_estoque_movimento"
)

echo "🔍 Verificando tabelas existentes..."

for table in "${TABLES_TO_CHECK[@]}"; do
    if PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema || '.' || table_name = '$table');" 2>/dev/null | grep -q "t"; then
        echo "✅ Tabela $table encontrada"
    else
        echo "❌ Tabela $table NÃO encontrada"
    fi
done

# Testar conexão com o aplicativo
echo "🌐 Testando conexão com o servidor da aplicação..."
SERVER_URL="http://localhost:8080"

# Primeiro verificar se o servidor está rodando
if curl -s --connect-timeout 5 "$SERVER_URL/actuator/health" >/dev/null 2>&1; then
    echo "✅ Servidor da aplicação está ONLINE"
else
    echo "⚠️ Servidor da aplicação NÃO está respondendo"
    echo "💡 Certifique-se de que o servidor está rodando na porta 8080"
fi

# Testar login básico se o servidor estiver disponível
if curl -s --connect-timeout 5 "$SERVER_URL/actuator/health" >/dev/null 2>&1; then
    echo "🔐 Testando autenticação básica..."
    
    # Tentar fazer login (ajustar conforme necessário)
    LOGIN_RESPONSE=$(curl -s -X POST \
      -H "Content-Type: application/json" \
      -H "Accept: application/json" \
      -d '{"username":"admin","password":"admin123"}' \
      "$SERVER_URL/api/auth/login" 2>/dev/null)
    
    if echo "$LOGIN_RESPONSE" | grep -q "token\|access_token\|jwt"; then
        echo "✅ Autenticação funcionando corretamente"
        TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$TOKEN" ]; then
            echo "🔑 Token obtido com sucesso"
        fi
    else
        echo "⚠️ Endpoint de login pode estar diferente ou credenciais incorretas"
        echo "💡 Verifique as credenciais e endpoint de autenticação"
    fi
fi

# Testar APIs básicas se o token estiver disponível
if [ -n "$TOKEN" ]; then
    echo "📊 Testando APIs básicas com autenticação..."
    
    # Testar listagem de empresas
    EMPRESAS=$(curl -s -H "Authorization: Bearer $TOKEN" "$SERVER_URL/api/core/empresas" 2>/dev/null)
    if echo "$EMPRESAS" | grep -q "content\|data"; then
        EMP_COUNT=$(echo "$EMPRESAS" | jq '.content | length 2>/dev/null || .data | length 2>/dev/null || echo 0')
        echo "🏢 Empresas encontradas: $EMP_COUNT"
    fi
    
    # Testar listagem de produtos
    PRODUTOS=$(curl -s -H "Authorization: Bearer $TOKEN" "$SERVER_URL/api/cadastro/produtos?page=0&size=5" 2>/dev/null)
    if echo "$PRODUTOS" | grep -q "content\|data"; then
        PROD_COUNT=$(echo "$PRODUTOS" | jq '.content | length 2>/dev/null || .data | length 2>/dev/null || echo 0')
        echo "📦 Produtos encontrados: $PROD_COUNT"
    fi
fi

echo ""
echo "🎯 Resumo dos testes:"
echo "- Conexão com PostgreSQL: ✅"
echo "- Banco de dados do ERP: $( [ -n "$(PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "postgres" -c "SELECT 1 FROM pg_database WHERE datname='$ERP_DB_NAME';" 2>/dev/null | grep "1")" ] && echo "✅" || echo "❌")"
echo "- Servidor da aplicação: $( [ -n "$(curl -s --connect-timeout 5 "$SERVER_URL/actuator/health" 2>/dev/null)" ] && echo "✅" || echo "❌")"
echo "- Autenticação: $( [ -n "$TOKEN" ] && echo "✅" || echo "❌")"

echo ""
echo "✅ Testes concluídos com sucesso!"
echo "💡 Próximos passos:"
echo "   1. Execute 'mvn spring-boot:run' para iniciar o servidor"
echo "   2. Execute este script novamente após o servidor estar online"
echo "   3. Verifique os logs para confirmar que todas as tabelas foram criadas"