#!/bin/bash

# Script para verificar o MongoDB e as credenciais fornecidas

echo "🔍 Verificando conexão com o MongoDB..."
echo "👤 Usuário: admin"
echo "🔒 Senha: ${MONGODB_PASSWORD}"
echo "🌐 Host: localhost:27017"
echo "🗄️ Banco: brasil-saas"

# Testar conexão com o MongoDB usando mongosh ou mongo
if command -v mongosh &> /dev/null; then
    echo "✅ Mongosh encontrado"
    
    # Testar conexão
    echo "📡 Testando conexão..."
    RESULT=$(mongosh "mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin" --eval "db.runCommand({ping:1})" 2>/dev/null)
    
    if echo "$RESULT" | grep -q "ok.*1"; then
        echo "✅ Conexão com MongoDB bem sucedida!"
        echo "📊 Informações do banco:"
        mongosh "mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin" --eval "
            db.getName();
            db.stats();
        " 2>/dev/null
    else
        echo "❌ Falha na conexão com MongoDB"
        echo "💡 Verifique se o MongoDB está rodando e as credenciais estão corretas"
    fi
elif command -v mongo &> /dev/null; then
    echo "✅ Mongo encontrado"
    
    # Testar conexão com o mongo antigo
    RESULT=$(mongo "mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin" --eval "db.runCommand({ping:1})" 2>/dev/null)
    
    if echo "$RESULT" | grep -q "ok.*1"; then
        echo "✅ Conexão com MongoDB bem sucedida!"
        echo "📊 Informações do banco:"
        mongo "mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin" --eval "
            db.getName();
            db.stats();
        " 2>/dev/null
    else
        echo "❌ Falha na conexão com MongoDB"
        echo "💡 Verifique se o MongoDB está rodando e as credenciais estão corretas"
    fi
else
    echo "⚠️ Nenhum cliente MongoDB encontrado (mongosh ou mongo)"
    echo "💡 Instale o MongoDB Shell para testar a conexão"
fi

echo ""
echo "📁 Coleções importantes:"
if command -v mongosh &> /dev/null; then
    mongosh "mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin" --eval "
        console.log('Coleções existentes:');
        db.getMongo().getDBNames().forEach(function(dbName) {
            if(dbName === 'brasil-saas') {
                console.log('Banco:', dbName);
                db.getCollectionNames().forEach(function(collName) {
                    var count = db[collName].countDocuments();
                    console.log('  - ' + collName + ' (' + count + ' documentos)');
                });
            }
        });
    " 2>/dev/null
elif command -v mongo &> /dev/null; then
    mongo "mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin" --eval "
        print('Coleções existentes:');
        db.getMongo().getDBNames().forEach(function(dbName) {
            if(dbName === 'brasil-saas') {
                print('Banco:', dbName);
                db.getCollectionNames().forEach(function(collName) {
                    var count = db[collName].count();
                    print('  - ' + collName + ' (' + count + ' documentos)');
                });
            }
        });
    " 2>/dev/null
fi

echo ""
echo "🔧 Comandos úteis para gerenciamento MongoDB:"
echo "   - Conectar: mongosh \"mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin\""
echo "   - Ver coleções: show collections"
echo "   - Ver documentos: db.imagens.find().limit(5)"

echo ""
echo "✅ Verificação do MongoDB concluída!"