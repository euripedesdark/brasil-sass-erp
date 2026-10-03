#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Brasil SaaS ERP - Deploy Manager Unificado
Gerencia: Java Oracle 21 LTS, IPTables (Exclusivo), PostgreSQL, Redis, RabbitMQ, 
Fontes Symbol, Build Maven e Serviços Systemd para qualquer distro Linux.
"""

import os
import sys
import subprocess
import shutil
import time
from pathlib import Path

# Configurações Globais
PROJECT_ROOT = Path(__file__).resolve().parent.parent
INSTALL_DIR = Path("/usr/local/share/brasil_saas")
JAR_FILE = INSTALL_DIR / "brasil_saas-erp.jar"
JAVA_HOME = "/opt/oracle-jdk-21"

# Serviços do ERP (Portas)
SERVICES = {
    "cadastro": {"port": 8081, "desc": "Serviço de Cadastro"},
    "financeiro": {"port": 8082, "desc": "Serviço Financeiro"},
    "fiscal": {"port": 8083, "desc": "Serviço Fiscal"},
    "vendas-compras": {"port": 8084, "desc": "Serviço de Vendas e Compras"},
    "rh": {"port": 8085, "desc": "Serviço de RH"},
    "relatorios": {"port": 8086, "desc": "Serviço de Relatórios e BI"},
    "ia": {"port": 8087, "desc": "Serviço de IA Corporativa"},
}

# Portas de Infraestrutura baseadas no POM
INFRA_PORTS = [5432, 6379, 5672, 27017] # Postgres, Redis, RabbitMQ, MongoDB

class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

def log(msg, level="INFO"):
    color = Colors.OKCYAN
    if level == "ERROR": color = Colors.FAIL
    elif level == "SUCCESS": color = Colors.OKGREEN
    elif level == "WARN": color = Colors.WARNING
    print(f"{color}[{level}] {msg}{Colors.ENDC}")

def run_cmd(cmd, shell=True, check=False, **kwargs):
    try:
        result = subprocess.run(cmd, shell=shell, check=check, capture_output=True, text=True, **kwargs)
        return result.returncode == 0, result.stdout, result.stderr
    except subprocess.CalledProcessError as e:
        return False, e.stdout, e.stderr

def get_pkg_manager():
    """Detecta o gerenciador de pacotes do sistema para suportar qualquer Linux."""
    if shutil.which("apt-get"): return "apt", "DEBIAN_FRONTEND=noninteractive apt-get install -y", "apt-get purge -y"
    if shutil.which("dnf"): return "dnf", "dnf install -y", "dnf remove -y"
    if shutil.which("yum"): return "yum", "yum install -y", "yum remove -y"
    if shutil.which("zypper"): return "zypper", "zypper install -y", "zypper remove -y"
    if shutil.which("pacman"): return "pacman", "pacman -S --noconfirm", "pacman -Rns --noconfirm"
    return None, None, None

def install_java_oracle_21():
    log("=== GERENCIAMENTO JAVA ORACLE 21 LTS ===", "INFO")
    pkg_type, pkg_install, pkg_remove = get_pkg_manager()
    
    # 1. Remover TODAS as outras versões de Java violentamente
    log("Removendo versões conflitantes de Java (OpenJDK, Temurin, etc)...", "WARN")
    if pkg_type == "apt":
        run_cmd(f"{pkg_remove} openjdk-* eclipse-temurin-* oracle-java* java-common")
        run_cmd("apt-get autoremove -y")
    elif pkg_type in ["dnf", "yum"]:
        run_cmd(f"{pkg_remove} java-* openjdk-* temurin-*")
    
    run_cmd("rm -rf /usr/lib/jvm/*")
    run_cmd("rm -rf /opt/jdk* /opt/oracle-jdk*")
    if shutil.which("update-alternatives"):
        run_cmd("update-alternatives --remove-all java")
        run_cmd("update-alternatives --remove-all javac")

    # 2. Baixar e Instalar Oracle JDK 21
    log("Baixando Oracle JDK 21 LTS oficial...", "INFO")
    url = "https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz"
    
    if run_cmd(f"wget --no-check-certificate -O /tmp/jdk21.tar.gz {url}")[0] or run_cmd(f"curl -L -o /tmp/jdk21.tar.gz {url}")[0]:
        run_cmd("mkdir -p /opt")
        run_cmd("tar -xzf /tmp/jdk21.tar.gz -C /opt")
        
        # O tar extrai numa pasta tipo jdk-21.x.x, renomeamos para oracle-jdk-21
        extracted_dir = [d for d in os.listdir("/opt") if d.startswith("jdk-21")]
        if extracted_dir:
            run_cmd(f"mv /opt/{extracted_dir[0]} {JAVA_HOME}")
        run_cmd("rm -f /tmp/jdk21.tar.gz")
        log(f"Java instalado em {JAVA_HOME}", "SUCCESS")
    else:
        log("Falha no download do Oracle JDK 21.", "ERROR")
        return False

    # 3. Configurar Alternativas e Variáveis (Forçar como padrão)
    log("Configurando Java 21 como padrão absoluto do sistema...", "INFO")
    if shutil.which("update-alternatives"):
        run_cmd(f"update-alternatives --install /usr/bin/java java {JAVA_HOME}/bin/java 9999")
        run_cmd(f"update-alternatives --install /usr/bin/javac javac {JAVA_HOME}/bin/javac 9999")
        run_cmd(f"update-alternatives --set java {JAVA_HOME}/bin/java")
        run_cmd(f"update-alternatives --set javac {JAVA_HOME}/bin/javac")
    else:
        run_cmd(f"ln -sf {JAVA_HOME}/bin/java /usr/bin/java")
        run_cmd(f"ln -sf {JAVA_HOME}/bin/javac /usr/bin/javac")
    
    env_file = "/etc/profile.d/oracle_jdk.sh"
    with open(env_file, "w") as f:
        f.write(f"export JAVA_HOME={JAVA_HOME}\nexport PATH=$JAVA_HOME/bin:$PATH\n")
    run_cmd(f"chmod +x {env_file}")
    os.environ["JAVA_HOME"] = JAVA_HOME
    os.environ["PATH"] = f"{JAVA_HOME}/bin:{os.environ.get('PATH', '')}"
    
    success, out, err = run_cmd(f"{JAVA_HOME}/bin/java -version")
    log("Java 21 Oracle configurado com sucesso!", "SUCCESS")
    return True

def setup_firewall_iptables():
    log("=== CONFIGURANDO FIREWALL (EXCLUSIVAMENTE IPTABLES) ===", "INFO")
    pkg_type, pkg_install, pkg_remove = get_pkg_manager()
    
    # 1. Remover concorrentes
    log("Removendo firewalld, ufw e nftables para evitar conflitos...", "WARN")
    run_cmd("systemctl stop firewalld ufw")
    run_cmd("systemctl disable firewalld ufw")
    if pkg_type == "apt":
        run_cmd(f"{pkg_remove} firewalld ufw nftables")
        run_cmd(f"{pkg_install} iptables iptables-persistent")
    elif pkg_type in ["dnf", "yum"]:
        run_cmd(f"{pkg_remove} firewalld ufw")
        run_cmd(f"{pkg_install} iptables-services")
        run_cmd("systemctl enable iptables")
        run_cmd("systemctl start iptables")
    
    # 2. Limpar regras existentes
    log("Resetando regras do IPTables...")
    run_cmd("iptables -F")
    run_cmd("iptables -X")
    
    # 3. Regras Padrão
    run_cmd("iptables -P INPUT ACCEPT") # Permite acesso durante a configuração
    run_cmd("iptables -P FORWARD ACCEPT")
    run_cmd("iptables -P OUTPUT ACCEPT")
    
    run_cmd("iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT")
    run_cmd("iptables -A INPUT -i lo -j ACCEPT")
    run_cmd("iptables -A INPUT -p tcp --dport 22 -j ACCEPT") # SSH
    
    # 4. Portas dos Serviços ERP e Infraestrutura
    ports_to_open = [str(s['port']) for s in SERVICES.values()] + [str(p) for p in INFRA_PORTS]
    for p in ports_to_open:
        run_cmd(f"iptables -A INPUT -p tcp --dport {p} -j ACCEPT")
    
    # 5. Salvar Regras para Persistência
    log("Salvando regras do IPTables...", "INFO")
    if pkg_type == "apt":
        run_cmd("netfilter-persistent save")
    elif pkg_type in ["dnf", "yum"]:
        run_cmd("service iptables save")
    elif os.path.exists("/etc/iptables"):
        run_cmd("iptables-save > /etc/iptables/iptables.rules")
    else:
        run_cmd("iptables-save > /etc/iptables.rules")
        
    log(f"IPTables configurado e persistido. Portas abertas: {', '.join(ports_to_open)}", "SUCCESS")

def install_infrastructure():
    log("=== INSTALANDO INFRAESTRUTURA GLOBAL (BASEADO NO POM.XML) ===", "INFO")
    pkg_type, pkg_install, _ = get_pkg_manager()
    
    if not pkg_type:
        log("Gerenciador de pacotes não detectado. Instalação manual necessária.", "ERROR")
        return

    # Lista de pacotes baseada na distro
    packages = []
    if pkg_type == "apt":
        run_cmd("apt-get update")
        packages = [
            "wget", "curl", "git", "maven", "gcc", "make", 
            "postgresql", "postgresql-contrib",
            "redis-server", 
            "rabbitmq-server",
            "fontconfig", "fonts-symbola" # Fonte Symbol e lib de fontes para PDF
        ]
    elif pkg_type in ["dnf", "yum"]:
        packages = [
            "wget", "curl", "git", "maven", "gcc", "make",
            "postgresql-server", "postgresql-contrib",
            "redis", 
            "rabbitmq-server",
            "fontconfig", "gdouros-symbola-fonts"
        ]

    # Instalação dos pacotes
    log(f"Instalando pacotes via {pkg_type}...", "INFO")
    run_cmd(f"{pkg_install} {' '.join(packages)}")

    # Configuração do PostgreSQL
    log("Configurando PostgreSQL...", "INFO")
    if pkg_type in ["dnf", "yum"]:
        run_cmd("postgresql-setup --initdb")
    
    run_cmd("systemctl enable postgresql")
    run_cmd("systemctl start postgresql")
    
    # Aguarda o Postgres iniciar
    time.sleep(3)
    
    # Cria usuário e banco baseados no pom.xml (brasil_saas / ALTERE_ME)
    check_user_cmd = "sudo -u postgres psql -tAc \"SELECT 1 FROM pg_roles WHERE rolname='postgres'\""
    has_user, _, _ = run_cmd(check_user_cmd)
    
    # Configura a senha para postgres
    run_cmd("sudo -u postgres psql -c \"ALTER USER postgres WITH PASSWORD 'ALTERE_ME';\"")
    
    # Cria o database brasil_saas se não existir
    db_exist, _, _ = run_cmd("sudo -u postgres psql -lqt | cut -d \\| -f 1 | grep -qw brasil_saas")
    if not db_exist:
        run_cmd("sudo -u postgres createdb brasil_saas")
        log("Banco de dados 'brasil_saas' criado com sucesso.", "SUCCESS")
    
    # Inicializa serviços Redis e RabbitMQ
    log("Habilitando Redis e RabbitMQ...", "INFO")
    run_cmd("systemctl enable redis redis-server rabbitmq-server")
    run_cmd("systemctl start redis redis-server rabbitmq-server")
    
    # Atualiza cache de fontes para o JasperReports/OpenPDF
    run_cmd("fc-cache -fv")

    log("Infraestrutura global instalada com sucesso!", "SUCCESS")

def build_project():
    log("=== COMPILANDO PROJETO (MVN CLEAN PACKAGE) ===", "INFO")
    env = os.environ.copy()
    env["JAVA_HOME"] = JAVA_HOME
    env["PATH"] = f"{JAVA_HOME}/bin:{env.get('PATH', '')}"
    
    log("Limpando target e construindo... (pode demorar)")
    success, out, err = run_cmd("mvn clean package -DskipTests -B", cwd=str(PROJECT_ROOT), env=env)
    
    if success:
        log("Build realizado com sucesso!", "SUCCESS")
        return True
    else:
        log("Falha no build! Analise o log do Maven abaixo:", "ERROR")
        print("====== LOG DO MAVEN (STDOUT) ======")
        print(out)
        print("====== LOG DO SISTEMA (STDERR) ======")
        print(err)
        return False

def install_services():
    log("=== INSTALANDO SERVIÇOS SYSTEMD ===", "INFO")
    
    target_jar = None
    # Procura o JAR gerado no target
    target_dir = PROJECT_ROOT / "target"
    if target_dir.exists():
        jars = list(target_dir.glob("brasil_saas-erp-*.jar"))
        # Exclui javadoc/sources se existirem
        jars = [j for j in jars if not j.name.endswith("-javadoc.jar") and not j.name.endswith("-sources.jar")]
        if jars:
            target_jar = jars[0]

    if target_jar and target_jar.exists():
        log("Copiando JAR para diretório de instalação...")
        INSTALL_DIR.mkdir(parents=True, exist_ok=True)
        shutil.copy(target_jar, JAR_FILE)
    elif not JAR_FILE.exists():
        log("JAR não encontrado nem no target nem no diretório de instalação. Execute o Build primeiro.", "ERROR")
        return False

    user = os.environ.get("USER", "root")
    
    for name, info in SERVICES.items():
        service_name = f"brasil_saas-{name}"
        service_path = f"/etc/systemd/system/{service_name}.service"
        
        content = f"""[Unit]
Description=Brasil SaaS ERP - {info['desc']}
After=network.target postgresql.service rabbitmq-server.service redis.service
Documentation=https://github.com/euripedesdark/BRASIL-SAAS-ERP

[Service]
Type=simple
User={user}
WorkingDirectory={INSTALL_DIR}
Environment=JAVA_HOME={JAVA_HOME}
Environment=PATH={JAVA_HOME}/bin:/usr/bin:/bin
ExecStart={JAVA_HOME}/bin/java -jar {JAR_FILE} --spring.profiles.active=servico-{name}
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal
SyslogIdentifier={service_name}

# Limites
LimitNOFILE=65535
MemoryMax=2G
CPUQuota=80%

[Install]
WantedBy=multi-user.target
"""
        with open(service_path, "w") as f:
            f.write(content)
        
        log(f"Arquivo {service_name}.service criado.")
    
    run_cmd("systemctl daemon-reload")
    for name in SERVICES.keys():
        run_cmd(f"systemctl enable brasil_saas-{name}")
    
    log("Serviços instalados e habilitados para boot.", "SUCCESS")
    return True

def manage_service(action, specific=None):
    targets = [specific] if specific else list(SERVICES.keys())
    for name in targets:
        svc = f"brasil_saas-{name}"
        log(f"{action.upper()} {svc}...")
        run_cmd(f"systemctl {action} {svc}")
        if action == "status":
            run_cmd(f"systemctl status {svc} --no-pager -l")

def show_menu():
    while True:
        print("\n" + "="*65)
        print(f"{Colors.BOLD}BRASIL-SAAS ERP - DEPLOY MANAGER UNIFICADO EXTREMO{Colors.ENDC}")
        print("="*65)
        print("1. Instalação Completa (Java 21 + Infra POM + FW Iptables + Build + Serviços)")
        print("2. Apenas Instalar Oracle JDK 21 LTS e Definir Padrão")
        print("3. Apenas Instalar Infraestrutura Global (Postgres, Redis, Fonts...)")
        print("4. Apenas Configurar Firewall (Exclusivo IPTables)")
        print("5. Apenas Compilar Projeto (Maven)")
        print("6. Apenas Instalar Serviços Systemd")
        print("-" * 65)
        print("7. Iniciar Todos os Serviços")
        print("8. Parar Todos os Serviços")
        print("9. Reiniciar Todos os Serviços")
        print("10. Status dos Serviços")
        print("-" * 65)
        print("0. Sair")
        print("="*65)
        
        choice = input("Escolha uma opção: ")
        
        if choice == '1':
            install_java_oracle_21()
            setup_firewall_iptables()
            install_infrastructure()
            if build_project():
                install_services()
                manage_service("start")
                log("INSTALAÇÃO COMPLETA FINALIZADA!", "SUCCESS")
            else:
                log("Instalação interrompida devido a erro no build.", "ERROR")
                
        elif choice == '2':
            install_java_oracle_21()
        elif choice == '3':
            install_infrastructure()
        elif choice == '4':
            setup_firewall_iptables()
        elif choice == '5':
            build_project()
        elif choice == '6':
            install_services()
        elif choice == '7':
            manage_service("start")
        elif choice == '8':
            manage_service("stop")
        elif choice == '9':
            manage_service("restart")
        elif choice == '10':
            manage_service("status")
        elif choice == '0':
            print("Saindo...")
            sys.exit(0)
        else:
            log("Opção inválida!", "ERROR")

if __name__ == "__main__":
    if os.geteuid() != 0:
        print("ERRO: Este script deve ser executado como ROOT (sudo).")
        sys.exit(1)
    
    show_menu()
