#!/bin/bash
echo "Instalando Python 3 e pip no Linux..."
sudo apt update
sudo apt install -y python3 python3-pip
echo "Python instalado. Execute: python3 deploy_manager.py"
