#!/bin/bash
# Brasil SaaS ERP - Setup Production SSL Configuration
# Configura HTTPS/SSL para todos os modulos do ERP

set -o errexit
set -o nounset
set -o pipefail

# =============================================================================
# CONFIGURACOES
# =============================================================================

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Diretorio base
# O caminho do repositorio NAO e fixo.
#
# A versao anterior apontava para
#   /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP
# que e' o caminho DESTA maquina, e que nem existe aqui: o repo esta em
#   /home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP
#
# Em outra conta, outra pasta, ou com o OneDrive em outro lugar, o `cd` falhava
# e o script seguia rodando na pasta errada — sem mensagem, porque o `cd` sem
# `set -e` nao interrompe. Pior: as units systemd geradas gravavam o caminho
# fixo, entao o servico nao subia na maquina nova.
#
# O repositorio se localiza a partir do proprio script, que e' o unico ponto
# que muda junto com a copia de trabalho.
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Configuracoes SSL
KEYSTORE_DIR="/etc/brasil_saas/keystore"
KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD:-brasil-saas2026}"
CERT_VALIDITY_DAYS=3650
CERT_ALIAS="brasil_saas-erp"
CERT_CN="brasil_saas-erp.local"
CERT_ORG="Brasil SaaS ERP"
CERT_CITY="Sao Paulo"
CERT_STATE="SP"
CERT_COUNTRY="BR"

# =============================================================================
# FUNCOES DE LOG
# =============================================================================

log() {
    local level=$1
    local message=$2
    echo -e "[${level}] ${message}"
}

log_info() {
    log "${GREEN}INFO${NC}" "$1"
}

log_warn() {
    log "${YELLOW}WARN${NC}" "$1"
}

log_error() {
    log "${RED}ERROR${NC}" "$1"
}

log_header() {
    echo -e "\n${BLUE}==========================================${NC}"
    echo -e "${BLUE} $1${NC}"
    echo -e "${BLUE}==========================================${NC}\n"
}

# =============================================================================
# FUNCOES DE CONFIGURACAO SSL
# =============================================================================

# Gerar keystore Java
generate_keystore() {
    log_header "Gerando Keystore Java"
    
    # Criar diretorio
    sudo mkdir -p "$KEYSTORE_DIR"
    sudo chmod 750 "$KEYSTORE_DIR"
    
    # Gerar keystore com certificado auto-assinado
    keytool -genkeypair \
        -alias "$CERT_ALIAS" \
        -keyalg RSA \
        -keysize 4096 \
        -validity "$CERT_VALIDITY_DAYS" \
        -keystore "$KEYSTORE_DIR/brasil_saas-erp.p12" \
        -storepass "$KEYSTORE_PASSWORD" \
        -keypass "$KEYSTORE_PASSWORD" \
        -dname "CN=$CERT_CN, OU=IT, O=$CERT_ORG, L=$CERT_CITY, ST=$CERT_STATE, C=$CERT_COUNTRY" \
        -storetype PKCS12
    
    # Copiar keystore para todos os modulos
    local modules=("core" "cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos" "bi" "producao")
    
    for module in "${modules[@]}"; do
        sudo cp "$KEYSTORE_DIR/brasil_saas-erp.p12" "$BASE_DIR/modules/$module/src/main/resources/keystore/"
        sudo chmod 640 "$BASE_DIR/modules/$module/src/main/resources/keystore/brasil_saas-erp.p12"
    done
    
    # Copiar para o modulo principal
    sudo mkdir -p "$BASE_DIR/src/main/resources/keystore"
    sudo cp "$KEYSTORE_DIR/brasil_saas-erp.p12" "$BASE_DIR/src/main/resources/keystore/"
    sudo chmod 640 "$BASE_DIR/src/main/resources/keystore/brasil_saas-erp.p12"
    
    log_info "Keystore gerado e distribuido para todos os modulos"
}

# Configurar application-prod.yml para um modulo
configure_module_ssl() {
    local module=$1
    local port=$2
    local context_path=$3
    
    log_info "Configurando SSL para modulo: $module (porta: $port)"
    
    local config_file="$BASE_DIR/modules/$module/src/main/resources/application-prod.yml"
    
    if [ ! -f "$config_file" ]; then
        log_warn "Arquivo de configuracao nao encontrado: $config_file"
        return
    fi
    
    # Backup
    cp "$config_file" "${config_file}.bak"
    
    # Adicionar configuracoes SSL
    cat >> "$config_file" << EOF

# SSL Configuration
server:
  port: $port
  servlet:
    context-path: $context_path
  ssl:
    enabled: true
    key-store: classpath:keystore/brasil_saas-erp.p12
    key-store-password: \${KEYSTORE_PASSWORD:$KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: $CERT_ALIAS

# HTTPS Redirect
spring:
  security:
    require-ssl: true
EOF
    
    log_info "SSL configurado para modulo $module"
}

# Configurar SSL para todos os modulos
configure_all_modules_ssl() {
    log_header "Configurando SSL para Todos os Modulos"
    
    # Definir portas e context paths
    declare -A module_ports=(
        ["core"]="8081,/api"
        ["cadastro"]="8082,/api"
        ["financeiro"]="8083,/api"
        ["vendas"]="8084,/api"
        ["compras"]="8085,/api"
        ["estoque"]="8086,/api"
        ["fiscal"]="8087,/api"
        ["rh"]="8088,/api"
        ["ia"]="8089,/api"
        ["servicos"]="8080,/api"
        ["bi"]="8091,/api"
        ["producao"]="8090,/api"
    )
    
    for module in "${!module_ports[@]}"; do
        IFS=',' read -r port context_path <<< "${module_ports[$module]}"
        configure_module_ssl "$module" "$port" "$context_path"
    done
    
    # Configurar modulo principal
    configure_module_ssl "main" "8080" "/"
    
    log_info "SSL configurado para todos os modulos"
}

# Configurar Nginx como reverse proxy
configure_nginx() {
    log_header "Configurando Nginx como Reverse Proxy"
    
    # Instalar Nginx
    if ! command -v nginx &> /dev/null; then
        log_info "Instalando Nginx..."
        sudo apt-get update -qq
        sudo apt-get install -y nginx -qq
    else
        log_info "Nginx ja esta instalado"
    fi
    
    # Criar configuracao Nginx
    cat > /tmp/brasil_saas-erp.conf << 'EOF'
# Brasil SaaS ERP - Nginx Configuration
upstream brasil_saas_core {
    server 127.0.0.1:8081;
}

upstream brasil_saas_cadastro {
    server 127.0.0.1:8082;
}

upstream brasil_saas_financeiro {
    server 127.0.0.1:8083;
}

upstream brasil_saas_vendas {
    server 127.0.0.1:8084;
}

upstream brasil_saas_compras {
    server 127.0.0.1:8085;
}

upstream brasil_saas_estoque {
    server 127.0.0.1:8086;
}

upstream brasil_saas_fiscal {
    server 127.0.0.1:8087;
}

upstream brasil_saas_rh {
    server 127.0.0.1:8088;
}

upstream brasil_saas_ia {
    server 127.0.0.1:8089;
}

upstream brasil_saas_servicos {
    server 127.0.0.1:8080;
}

upstream brasil_saas_bi {
    server 127.0.0.1:8091;
}

upstream brasil_saas_producao {
    server 127.0.0.1:8090;
}

# HTTPS Server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name brasil_saas-erp.local;
    
    # SSL Configuration
    ssl_certificate /etc/brasil_saas/ssl/brasil_saas-erp.crt;
    ssl_certificate_key /etc/brasil_saas/ssl/brasil_saas-erp.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Security Headers
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    
    # Proxy configurations
    location /core/ {
        proxy_pass http://brasil_saas_core/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /cadastro/ {
        proxy_pass http://brasil_saas_cadastro/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /financeiro/ {
        proxy_pass http://brasil_saas_financeiro/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /vendas/ {
        proxy_pass http://brasil_saas_vendas/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /compras/ {
        proxy_pass http://brasil_saas_compras/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /estoque/ {
        proxy_pass http://brasil_saas_estoque/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /fiscal/ {
        proxy_pass http://brasil_saas_fiscal/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /rh/ {
        proxy_pass http://brasil_saas_rh/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /ia/ {
        proxy_pass http://brasil_saas_ia/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /servicos/ {
        proxy_pass http://brasil_saas_servicos/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /bi/ {
        proxy_pass http://brasil_saas_bi/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /producao/ {
        proxy_pass http://brasil_saas_producao/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Static files (frontend)
    location / {
        root /opt/brasil_saas-erp/frontend;
        try_files $uri $uri/ /index.html;
    }
    
    # Health check
    location /health {
        return 200 'OK';
        add_header Content-Type text/plain;
    }
}

# HTTP to HTTPS redirect
server {
    listen 80;
    listen [::]:80;
    server_name brasil_saas-erp.local;
    return 301 https://\$host\$request_uri;
}
EOF
    
    # Copiar configuracao
    sudo cp /tmp/brasil_saas-erp.conf /etc/nginx/sites-available/brasil_saas-erp
    sudo ln -sf /etc/nginx/sites-available/brasil_saas-erp /etc/nginx/sites-enabled/brasil_saas-erp
    
    # Testar configuracao
    sudo nginx -t
    
    # Reiniciar Nginx
    sudo systemctl restart nginx
    sudo systemctl enable nginx
    
    log_info "Nginx configurado como reverse proxy"
}

# Gerar certificado SSL para Nginx
generate_nginx_ssl() {
    log_header "Gerando Certificado SSL para Nginx"
    
    # Criar diretorio
    sudo mkdir -p /etc/brasil_saas/ssl
    
    # Gerar certificado auto-assinado
    sudo openssl req -x509 -nodes -days "$CERT_VALIDITY_DAYS" \
        -newkey rsa:4096 \
        -keyout /etc/brasil_saas/ssl/brasil_saas-erp.key \
        -out /etc/brasil_saas/ssl/brasil_saas-erp.crt \
        -subj "/C=$CERT_COUNTRY/ST=$CERT_STATE/L=$CERT_CITY/O=$CERT_ORG/CN=$CERT_CN"
    
    # Definir permissoes
    sudo chmod 600 /etc/brasil_saas/ssl/brasil_saas-erp.key
    sudo chmod 644 /etc/brasil_saas/ssl/brasil_saas-erp.crt
    
    # Reiniciar Nginx para carregar certificado
    sudo systemctl restart nginx
    
    log_info "Certificado SSL gerado para Nginx"
}

# Configurar logs JSON para producao
configure_json_logs() {
    log_header "Configurando Logs JSON para Producao"
    
    # Criar configuracao de logback para producao
    cat > /tmp/logback-spring-prod.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="br.com.brasil_saas.core.config.JsonLayout">
                <timestampFormat>yyyy-MM-dd'T'HH:mm:ss.SSSX</timestampFormat>
                <includeContext>true</includeContext>
                <includeMdc>true</includeMdc>
            </layout>
        </encoder>
    </appender>
    
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/brasil_saas/brasil_saas-erp-prod.json</file>
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="br.com.brasil_saas.core.config.JsonLayout">
                <timestampFormat>yyyy-MM-dd'T'HH:mm:ss.SSSX</timestampFormat>
                <includeContext>true</includeContext>
                <includeMdc>true</includeMdc>
            </layout>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/brasil_saas/brasil_saas-erp-prod.%d{yyyy-MM-dd}.json</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="JSON"/>
        <appender-ref ref="JSON_FILE"/>
    </root>
</configuration>
EOF
    
    # Copiar para todos os modulos
    local modules=("core" "cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos" "bi" "producao")
    
    for module in "${modules[@]}"; do
        sudo cp /tmp/logback-spring-prod.xml "$BASE_DIR/modules/$module/src/main/resources/logback-spring-prod.xml"
    done
    
    # Copiar para modulo principal
    sudo cp /tmp/logback-spring-prod.xml "$BASE_DIR/src/main/resources/logback-spring-prod.xml"
    
    log_info "Logs JSON configurados para todos os modulos"
}

# Configurar HikariCP para producao
configure_hikaricp() {
    log_header "Configurando HikariCP para Producao"
    
    # Criar configuracao otimizada do HikariCP
    cat > /tmp/hikari-config.yml << 'EOF'
# HikariCP Configuration for Production
spring:
  datasource:
    hikari:
      connection-timeout: 30000
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1200000
      pool-name: BrasilSaasHikariPool
      auto-commit: false
      connection-test-query: SELECT 1
      leak-detection-threshold: 60000
      
      # Cache settings
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
EOF
    
    # Adicionar configuracao aos arquivos application-prod.yml
    local modules=("core" "cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos" "bi" "producao")
    
    for module in "${modules[@]}"; do
        local config_file="$BASE_DIR/modules/$module/src/main/resources/application-prod.yml"
        if [ -f "$config_file" ]; then
            cat /tmp/hikari-config.yml >> "$config_file"
        fi
    done
    
    # Adicionar ao modulo principal
    cat /tmp/hikari-config.yml >> "$BASE_DIR/src/main/resources/application-prod.yml"
    
    log_info "HikariCP configurado para producao"
}

# Configurar Rate Limiting
configure_rate_limiting() {
    log_header "Configurando Rate Limiting"
    
    # Criar configuracao de rate limiting
    cat > /tmp/rate-limit-config.yml << 'EOF'
# Rate Limiting Configuration
spring:
  cloud:
    gateway:
      routes:
        - id: brasil_saas-erp
          uri: no://
          predicates:
            - Path=/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                redis-rate-limiter.requestedTokens: 1
                key-resolver: "#{@remoteAddrKeyResolver}"
EOF
    
    # Adicionar configuracao aos arquivos application-prod.yml
    local modules=("core" "cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos" "bi" "producao")
    
    for module in "${modules[@]}"; do
        local config_file="$BASE_DIR/modules/$module/src/main/resources/application-prod.yml"
        if [ -f "$config_file" ]; then
            cat /tmp/rate-limit-config.yml >> "$config_file"
        fi
    done
    
    log_info "Rate Limiting configurado"
}

# =============================================================================
# FUNCAO PRINCIPAL
# =============================================================================

# Mudar para diretorio base
cd "$BASE_DIR" 2>/dev/null || {
    log_error "Diretorio base nao encontrado: $BASE_DIR"
    exit 1
}

# Verificar se esta rodando como root
if [ "$EUID" -ne 0 ]; then
    log_error "Este script requer privilégios de root"
    exit 1
fi

# Parsing de argumentos
COMMAND="${1:-all}"

case "$COMMAND" in
    all)
        log_header "INICIANDO CONFIGURACAO SSL COMPLETA"
        
        generate_keystore
        configure_all_modules_ssl
        configure_nginx
        generate_nginx_ssl
        configure_json_logs
        configure_hikaricp
        configure_rate_limiting
        
        log_header "CONFIGURACAO SSL CONCLUIDA"
        log_info "Todos os modulos estao configurados para HTTPS/SSL"
        ;;
    keystore)
        generate_keystore
        ;;
    modules)
        configure_all_modules_ssl
        ;;
    nginx)
        configure_nginx
        generate_nginx_ssl
        ;;
    logs)
        configure_json_logs
        ;;
    hikaricp)
        configure_hikaricp
        ;;
    rate-limit)
        configure_rate_limiting
        ;;
    test)
        log_header "Testando Configuracoes SSL"
        
        # Testar conexao HTTPS
        if curl -k -I https://brasil_saas-erp.local 2>/dev/null | grep -q "200 OK"; then
            log_info "Conexao HTTPS esta funcionando"
        else
            log_error "Conexao HTTPS falhou"
            exit 1
        fi
        
        # Testar todos os endpoints
        local endpoints=(
            "https://brasil_saas-erp.local/core/health"
            "https://brasil_saas-erp.local/cadastro/health"
            "https://brasil_saas-erp.local/financeiro/health"
        )
        
        for endpoint in "${endpoints[@]}"; do
            if curl -k -I "$endpoint" 2>/dev/null | grep -q "200 OK"; then
                log_info "Endpoint $endpoint: OK"
            else
                log_error "Endpoint $endpoint: FAILED"
            fi
        done
        ;;
    --help|-h|help)
        echo "Uso: $0 [COMMAND]"
        echo ""
        echo "Comandos:"
        echo "  all           - Configurar tudo (Keystore, Modulos, Nginx, Logs, HikariCP)"
        echo "  keystore      - Gerar keystore Java"
        echo "  modules       - Configurar SSL para todos os modulos"
        echo "  nginx         - Configurar Nginx como reverse proxy"
        echo "  logs          - Configurar logs JSON"
        echo "  hikaricp      - Configurar HikariCP para producao"
        echo "  rate-limit    - Configurar rate limiting"
        echo "  test          - Testar configuracoes SSL"
        echo ""
        exit 0
        ;;
    *)
        log_error "Comando nao reconhecido: $COMMAND"
        echo "Use $0 --help para ver opcoes"
        exit 1
        ;;
esac

exit 0
