#!/bin/bash

# Script para testar conexão com o banco de dados PostgreSQL
# Usuário: postgres, Senha: ALTERE_ME

echo "🔍 Testando conexão com o banco de dados PostgreSQL..."
echo "👤 Usuário: postgres"
echo "🔒 Senha: ALTERE_ME"

# Configurações
DB_HOST="localhost"
DB_PORT="5432"
DB_USER="postgres"
DB_PASS="ALTERE_ME"

export PGPASSWORD="$DB_PASS"

echo ""
echo "📡 Testando conexão com PostgreSQL..."

# Testar conexão básica
if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "SELECT version();" >/dev/null 2>&1; then
    echo "✅ Conexão com PostgreSQL bem sucedida!"
else
    echo "❌ Falha na conexão com PostgreSQL"
    echo "💡 Verifique se o PostgreSQL está rodando e as credenciais estão corretas"
    exit 1
fi

echo ""
echo "📋 Verificando bancos de dados existentes..."

# Listar bancos de dados
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "\l" | head -20

echo ""
echo "🔧 Verificando extensões necessárias..."

# Verificar extensões comuns
EXTENSIONS=("uuid-ossp" "pgcrypto" "citext")
for ext in "${EXTENSIONS[@]}"; do
    RESULT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -tAc "SELECT 1 FROM pg_extension WHERE extname = '$ext';" 2>/dev/null)
    if [ "$RESULT" = "1" ]; then
        echo "✅ Extensão $ext: INSTALADA"
    else
        echo "⚠️ Extensão $ext: NÃO instalada"
    fi
done

echo ""
echo "🏠 Conectando ao banco 'postgres' para testes..."

# Testar operações básicas no banco postgres
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -c "
SELECT 
    current_database() as banco_atual,
    current_user as usuario_atual,
    version() as versao_postgres;
"

echo ""
echo "📊 Verificando tabelas do sistema Brasil SaaS ERP (se existirem)..."

# Tentar conectar ao banco do ERP (se existir)
ERP_DB="brasil-saas"
if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$ERP_DB';" 2>/dev/null | grep -q "1"; then
    echo "✅ Banco $ERP_DB encontrado!"
    
    echo ""
    echo "📋 Tabelas no esquema brasil_saas:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$ERP_DB" -c "
    SELECT table_schema, table_name 
    FROM information_schema.tables 
    WHERE table_schema = 'brasil_saas'
    ORDER BY table_name;"
else
    echo "ℹ️ Banco $ERP_DB não encontrado (isso é normal em instalações novas)"
    echo "💡 O banco será criado quando o sistema for iniciado pela primeira vez"
fi

echo ""
echo "✅ Teste de conexão com o banco de dados concluído!"
echo ""
echo "🔧 Comandos úteis para gerenciamento:"
echo "   - Conectar: psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres"
echo "   - Ver logs: sudo tail -f /var/log/postgresql/postgresql-*-main.log"
echo "   - Reiniciar: sudo systemctl restart postgresql"