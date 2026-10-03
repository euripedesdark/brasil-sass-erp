#!/bin/bash
# Script para instalar o Brasil SaaS ERP como serviço no Linux

SERVICE_NAME="brasil_saas-erp"
JAR_FILE="target/brasil_saas-erp-1.0.0-SNAPSHOT.jar"
SERVICE_FILE="/etc/systemd/system/$SERVICE_NAME.service"

if [ ! -f "$JAR_FILE" ]; then
    echo "Erro: Arquivo $JAR_FILE não encontrado!"
    exit 1
fi

# Criar arquivo de serviço
sudo tee $SERVICE_FILE > /dev/null <<EOF
[Unit]
Description=Brasil SaaS ERP Service
After=network.target

[Service]
Type=simple
User=$(whoami)
ExecStart=/usr/bin/java -jar $JAR_FILE --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Recarregar systemd e iniciar o serviço
sudo systemctl daemon-reload
sudo systemctl enable $SERVICE_NAME
sudo systemctl start $SERVICE_NAME

echo "Serviço $SERVICE_NAME instalado e iniciado com sucesso!"
echo "Para verificar o status: sudo systemctl status $SERVICE_NAME"
echo "Para ver os logs: sudo journalctl -u $SERVICE_NAME -f"