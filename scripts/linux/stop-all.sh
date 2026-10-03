#!/bin/bash
# Brasil SaaS ERP - Script para parar todos os serviços
# Uso: ./stop-all.sh

SERVICES=("cadastro" "financeiro" "fiscal" "vendas-compras" "rh" "relatorios" "ia")

echo "=========================================="
echo "Brasil SaaS ERP - Parando todos os serviços"
echo "=========================================="

for service in "${SERVICES[@]}"; do
    echo "Parando serviço: brasil_saas-$service..."
    sudo systemctl stop brasil_saas-$service
    if [ $? -eq 0 ]; then
        echo "✓ Serviço brasil_saas-$service parado com sucesso"
    else
        echo "✗ Falha ao parar brasil_saas-$service"
    fi
    sleep 1
done

echo ""
echo "=========================================="
echo "Status dos serviços:"
echo "=========================================="
for service in "${SERVICES[@]}"; do
    sudo systemctl is-active brasil_saas-$service
done

echo ""
echo "Para iniciar todos: ./start-all.sh"
