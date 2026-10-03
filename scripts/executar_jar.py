#!/usr/bin/env python3
"""
Script para executar o Brasil SaaS ERP
"""
import subprocess
import sys
import os
from pathlib import Path

def run_erp_jar():
    jar_file = Path("target/brasil_saas-erp-1.0.0-SNAPSHOT.jar")
    
    if not jar_file.exists():
        print(f"Erro: Arquivo {jar_file} não encontrado!")
        sys.exit(1)
    
    print(f"Executando {jar_file}...")
    try:
        process = subprocess.Popen([
            "java", "-jar", str(jar_file),
            "--spring.profiles.active=prod"
        ])
        
        process.wait()
    except KeyboardInterrupt:
        print("\nParando o servidor...")
        process.terminate()
        process.wait()

if __name__ == "__main__":
    run_erp_jar()