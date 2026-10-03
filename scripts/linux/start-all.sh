#!/bin/bash

# Brasil SaaS ERP - Start All Services
# Script inteligente que detecta usuário, cria diretórios, compila se necessário e inicia todos os serviços

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
SERVICE_USER="${SUDO_USER:-$(whoami)}"
BRAZILCLOUD_DIR="/usr/local/share/brasil_saas"
JAR_FILE="$BRAZILCLOUD_DIR/brasil_saas-erp.jar"
TARGET_JAR="$PROJECT_ROOT/target/brasil_saas-erp-1.0.0-SNAPSHOT.jar"

echo "=========================================="
echo "Brasil SaaS ERP - Iniciando todos os serviços"
echo "=========================================="
echo ""

# 1. Detectar usuário atual
echo "👤 Usuário detectado: $SERVICE_USER"

# 2. Criar diretório se não existir
if [ ! -d "$BRAZILCLOUD_DIR" ]; then
    echo "📁 Criando diretório $BRAZILCLOUD_DIR..."
    sudo mkdir -p "$BRAZILCLOUD_DIR"
    sudo chown "$SERVICE_USER:$SERVICE_USER" "$BRAZILCLOUD_DIR"
    echo "✓ Diretório criado com sucesso"
else
    echo "✓ Diretório $BRAZILCLOUD_DIR já existe"
fi

# 3. Verificar se o JAR existe, senão compilar
if [ ! -f "$JAR_FILE" ]; then
    echo "📦 JAR não encontrado em $JAR_FILE"
    
    if [ ! -f "$TARGET_JAR" ]; then
        echo "🔨 Compilando projeto Maven..."
        cd "$PROJECT_ROOT"
        mvn clean package -DskipTests -q
        echo "✓ Compilação concluída"
    else
        echo "✓ JAR compilado encontrado em $TARGET_JAR"
    fi
    
    echo "📋 Copiando JAR para $BRAZILCLOUD_DIR..."
    sudo cp "$TARGET_JAR" "$JAR_FILE"
    sudo chown "$SERVICE_USER:$SERVICE_USER" "$JAR_FILE"
    echo "✓ JAR copiado com sucesso"
else
    echo "✓ JAR já existe em $JAR_FILE"
fi

# 4. Corrigir arquivos .service para usar o usuário correto
echo "🔧 Ajustando arquivos de serviço para o usuário $SERVICE_USER..."
for service_file in /etc/systemd/system/brasil_saas-*.service; do
    if [ -f "$service_file" ]; then
        sudo sed -i "s/^User=.*/User=$SERVICE_USER/" "$service_file"
        # Comentar Group se existir para evitar erro
        # Remove linha Group= se existir
        sudo sed -i '/^Group=/d' "$service_file"
        # Remover MemoryLimit obsoleto
        sudo sed -i 's/^MemoryLimit=.*/#MemoryLimit=removed/' "$service_file"
    fi
done
echo "✓ Arquivos de serviço ajustados"

# 5. Recarregar systemd
echo "🔄 Recarregando daemon do systemd..."
sudo systemctl daemon-reload
echo "✓ Systemd recarregado"

# 6. Iniciar todos os serviços
echo ""
SERVICES=("brasil_saas-cadastro" "brasil_saas-financeiro" "brasil_saas-fiscal" "brasil_saas-vendas-compras" "brasil_saas-rh" "brasil_saas-relatorios" "brasil_saas-ia")

for service in "${SERVICES[@]}"; do
    echo "Iniciando serviço: $service..."
    if sudo systemctl start "$service"; then
        echo "✓ Serviço $service iniciado com sucesso"
    else
        echo "✗ Falha ao iniciar $service"
    fi
done

echo ""
echo "=========================================="
echo "Status dos serviços:"
echo "=========================================="
sleep 3
for service in "${SERVICES[@]}"; do
    status=$(sudo systemctl is-active "$service" 2>/dev/null || echo "unknown")
    echo "$status - $service"
done

echo ""
echo "Para verificar logs: journalctl -u brasil_saas-[nome-servico] -f"
echo "Para parar todos: $SCRIPT_DIR/stop-all.sh"
echo "=========================================="
