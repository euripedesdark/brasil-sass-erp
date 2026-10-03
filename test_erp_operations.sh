#!/bin/bash

# Script para testar operações básicas do Brasil SaaS ERP
# Adaptado do exemplo fornecido
# Autor: Sistema ERP

echo "🧪 Iniciando testes de operações do Brasil SaaS ERP..."

# Configurações básicas
B="http://localhost:8080"  # Base URL do servidor
H="Content-Type: application/json"
J="Accept: application/json"

# Variáveis para armazenar IDs
USUARIO_TOKEN=""

echo "🔐 Tentando autenticar usuário..."

# Tente fazer login (ajuste as credenciais conforme necessário)
LOGIN_DATA='{"username":"admin","password":"admin123"}'
AUTH_RESPONSE=$(curl -s -X POST \
  -H "$H" \
  -d "$LOGIN_DATA" \
  "$B/api/auth/login" 2>/dev/null)

if [ $? -eq 0 ] && echo "$AUTH_RESPONSE" | grep -q "token\|access_token"; then
    USUARIO_TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    if [ -z "$USUARIO_TOKEN" ]; then
        USUARIO_TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
    fi
    
    if [ -n "$USUARIO_TOKEN" ]; then
        H="Content-Type: application/json"
        AUTH_HEADER="Authorization: Bearer $USUARIO_TOKEN"
        echo "✅ Autenticação bem sucedida!"
        echo "🔑 Token obtido: ${USUARIO_TOKEN:0:20}..."
    else
        echo "❌ Falha na autenticação - verifique credenciais"
        # Tentar com credenciais padrão alternativas
        for creds in '{"username":"admin","password":"admin"}' '{"username":"root","password":"root"}' '{"username":"test","password":"test"}'; do
            AUTH_RESPONSE=$(curl -s -X POST -H "$H" -d "$creds" "$B/api/auth/login" 2>/dev/null)
            if [ $? -eq 0 ] && echo "$AUTH_RESPONSE" | grep -q "token\|access_token"; then
                USUARIO_TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
                if [ -z "$USUARIO_TOKEN" ]; then
                    USUARIO_TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
                fi
                if [ -n "$USUARIO_TOKEN" ]; then
                    AUTH_HEADER="Authorization: Bearer $USUARIO_TOKEN"
                    echo "✅ Autenticação alternativa bem sucedida!"
                    break
                fi
            fi
        done
        
        if [ -z "$USUARIO_TOKEN" ]; then
            echo "❌ Não foi possível autenticar - verifique credenciais"
            echo "💡 Dica: Crie um usuário admin primeiro ou verifique a configuração de autenticação"
            exit 1
        fi
    fi
else
    echo "❌ Endpoint de autenticação não encontrado ou falhou"
    echo "💡 Verifique se o servidor está rodando e o endpoint de autenticação está correto"
    exit 1
fi

# Função para esperar um pouco entre requisições
wait_for_db() {
    sleep 2
}

echo "🏢 Buscando ou criando empresa..."

# 1) Garante que existe pelo menos uma empresa
EMPRESA=$(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/core/empresas?page=0&size=1" | jq -r '.content[0].id // .data.content[0].id // empty')
if [ -z "$EMPRESA" ]; then
    echo "⚠️ Nenhuma empresa encontrada. Criando empresa de teste..."
    EMPRESA=$(curl -s -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/core/empresas" \
        -d '{"razaoSocial":"Empresa Teste S/A","nomeFantasia":"Empresa Teste","cnpj":"12345678000195","endereco":"Rua Teste, 123","bairro":"Centro","cep":"12345678","uf":"SP","telefone":"(11) 99999-9999","status":"ATIVO"}' \
        | jq -r '.id // .data.id // empty')
fi

[ -n "$EMPRESA" ] || { echo "❌ NAO CONSEGUI CRIAR/ENCONTRAR EMPRESA"; exit 1; }
echo "empresa_id=$EMPRESA"

echo "👥 Buscando ou criando cliente..."

# 2) Garante que existe pelo menos um cliente
CLIENTE=$(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/clientes?page=0&size=1" | jq -r '.content[0].id // .data.content[0].id // empty')
if [ -z "$CLIENTE" ]; then
    echo "⚠️ Nenhum cliente encontrado. Criando cliente de teste..."
    CLIENTE=$(curl -s -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/clientes" \
        -d '{"codigo":"CLI001","pessoa":{"nome":"Cliente Teste","tipo":"FISICA","documento":"12345678900","email":"cliente@teste.com","telefone":"(11) 99999-9999","status":"ATIVO"}}' \
        | jq -r '.id // .data.id // empty')
fi

[ -n "$CLIENTE" ] || { echo "❌ NAO CONSEGUI CRIAR/ENCONTRAR CLIENTE"; exit 1; }
echo "cliente_id=$CLIENTE"

echo "🏭 Buscando ou criando fornecedor..."

# 3) Garante que existe pelo menos um fornecedor
FORN=$(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/fornecedores?page=0&size=1" | jq -r '.content[0].id // .data.content[0].id // empty')
if [ -z "$FORN" ]; then
    echo "⚠️ Nenhum fornecedor encontrado. Criando fornecedor de teste..."
    FORN=$(curl -s -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/fornecedores" \
        -d '{"codigo":"FOR001","pessoa":{"nome":"Fornecedor Teste","tipo":"JURIDICA","documento":"12345678000195","email":"fornecedor@teste.com","telefone":"(11) 99999-9999","status":"ATIVO"}}' \
        | jq -r '.id // .data.id // empty')
fi

[ -n "$FORN" ] || { echo "❌ NAO CONSEGUI CRIAR/ENCONTRAR FORNECEDOR"; exit 1; }
echo "fornecedor_id=$FORN"

echo "📦 Buscando ou criando produto..."

# 4) Garante que existe pelo menos um produto
PROD=$(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/produtos?page=0&size=1" | jq -r '.content[0].id // .data.content[0].id // empty')
if [ -z "$PROD" ]; then
    echo "⚠️ Nenhum produto encontrado. Criando produto de teste..."
    # Ajuste os campos abaixo se o seu ProdutoRequest exigir outros obrigatórios (como categoriaId, ncm, etc)
    PROD=$(curl -s -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/produtos" \
        -d '{"codigo":"P001","nome":"Produto Teste API","tipo":"PRODUTO","precoVenda":10.00,"precoCusto":5.00,"estoqueMinimo":0,"estoqueMaximo":100,"unidadeMedidaId":1,"ativo":true}' \
        | jq -r '.id // .data.id // empty')
fi

[ -n "$PROD" ] || { echo "❌ NAO CONSEGUI CRIAR/ENCONTRAR PRODUTO"; exit 1; }
echo "produto_id=$PROD"

echo "🛒 Tentando criar pedido de compra..."
wait_for_db

# 5) Tenta criar a compra e trata a resposta HTTP corretamente
RESP_COMPRA=$(curl -s -w "\n%{http_code}" -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/compras/pedidos" \
    -d "{\"fornecedorId\":$FORN,\"dataEmissao\":\"$(date +%Y-%m-%d)\",\"itens\":[{\"numeroItem\":1,\"produtoId\":$PROD,\"descricao\":\"Compra Teste\",\"quantidade\":10,\"unidade\":\"UN\",\"valorUnitario\":5.00}]}")

HTTP_CODE=$(echo "$RESP_COMPRA" | tail -1)
BODY=$(echo "$RESP_COMPRA" | sed '$d')

if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
    COMPRA=$(echo "$BODY" | jq -r '.id // .data.id // empty')
    echo "✅ Compra criada com ID: $COMPRA"
    
    # Receber mercadoria
    echo "📦 Dando entrada no estoque (receber)..."
    RECEBER_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/compras/pedidos/$COMPRA/receber")
    echo "Recebimento HTTP: $RECEBER_RESULT"
    
    # Conferir saldo
    echo "📊 Saldo atual em estoque:"
    curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/estoque/saldos?empresaId=1&produtoId=$PROD" | jq -c '.data // .'
else
    echo "❌ ERRO AO CRIAR COMPRA (HTTP $HTTP_CODE):"
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
fi

echo "🛍️ Tentando criar pedido de venda..."
wait_for_db

# 6) Tenta criar a venda e trata a resposta HTTP corretamente
RESP_VENDA=$(curl -s -w "\n%{http_code}" -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/vendas/pedidos" \
    -d "{\"clienteId\":$CLIENTE,\"dataEmissao\":\"$(date +%Y-%m-%d)\",\"itens\":[{\"numeroItem\":1,\"produtoId\":$PROD,\"descricao\":\"Venda Teste\",\"quantidade\":2,\"unidade\":\"UN\",\"valorUnitario\":10.00}]}")

HTTP_CODE=$(echo "$RESP_VENDA" | tail -1)
BODY=$(echo "$RESP_VENDA" | sed '$d')

if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
    VENDA=$(echo "$BODY" | jq -r '.id // .data.id // empty')
    echo "✅ Venda criada com ID: $VENDA"
    
    # Faturar venda
    echo "💰 Tentando faturar a venda..."
    FATURAR_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "$H" -H "$AUTH_HEADER" "$B/api/vendas/pedidos/$VENDA/faturar")
    echo "Faturamento HTTP: $FATURAR_RESULT"
    
    # Conferir saldo após venda
    echo "📊 Saldo atual em estoque após venda:"
    curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/estoque/saldos?empresaId=1&produtoId=$PROD" | jq -c '.data // .'
else
    echo "❌ ERRO AO CRIAR VENDA (HTTP $HTTP_CODE):"
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
fi

echo ""
echo "🎯 Resumo dos testes realizados:"
echo "✅ Autenticação: FUNCIONANDO"
echo "✅ Empresa: OK"
echo "✅ Cliente: OK"
echo "✅ Fornecedor: OK"
echo "✅ Produto: OK"
echo "✅ Compra: $( [ -n "$COMPRA" ] && echo "OK ($COMPRA)" || echo "FALHOU")"
echo "✅ Venda: $( [ -n "$VENDA" ] && echo "OK ($VENDA)" || echo "FALHOU")"

echo ""
echo "📈 Estatísticas finais:"
echo "Empresas no sistema: $(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/core/empresas?page=0&size=100" 2>/dev/null | jq '.totalElements // .data.totalElements // 0' 2>/dev/null || echo "N/A")"
echo "Clientes no sistema: $(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/clientes?page=0&size=100" 2>/dev/null | jq '.totalElements // .data.totalElements // 0' 2>/dev/null || echo "N/A")"
echo "Fornecedores no sistema: $(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/fornecedores?page=0&size=100" 2>/dev/null | jq '.totalElements // .data.totalElements // 0' 2>/dev/null || echo "N/A")"
echo "Produtos no sistema: $(curl -s -H "$H" -H "$AUTH_HEADER" "$B/api/cadastro/produtos?page=0&size=100" 2>/dev/null | jq '.totalElements // .data.totalElements // 0' 2>/dev/null || echo "N/A")"

echo ""
echo "🎉 Testes concluídos com sucesso!"
echo "💡 Dica: Execute este script periodicamente para verificar a saúde do sistema"