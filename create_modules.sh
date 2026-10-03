#!/bin/bash
# Script para criar a estrutura de módulos do Brasil SaaS ERP

set -e

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Lista de módulos a serem criados
MODULES=("cadastro" "financeiro" "vendas" "compras" "estoque" "fiscal" "rh" "ia" "servicos")

# Portas para cada módulo (evitando conflitos)
declare -A PORTS
PORTS=(
    ["cadastro"]="8082"
    ["financeiro"]="8083"
    ["vendas"]="8084"
    ["compras"]="8085"
    ["estoque"]="8086"
    ["fiscal"]="8087"
    ["rh"]="8088"
    ["ia"]="8089"
    ["servicos"]="8080"
)

# Descrições dos módulos
declare -A DESCRIPTIONS
DESCRIPTIONS=(
    ["cadastro"]="Módulo de Cadastro - Pessoas, Produtos, Clientes, Fornecedores"
    ["financeiro"]="Módulo Financeiro - Contas a Pagar/Receber, Lançamentos, Conciliação"
    ["vendas"]="Módulo de Vendas - Pedidos, Orçamentos, Faturamento"
    ["compras"]="Módulo de Compras - Pedidos de Compra, Fornecedores"
    ["estoque"]="Módulo de Estoque - Controle de Saldos, Movimentações"
    ["fiscal"]="Módulo Fiscal - NF-e, NFS-e, CT-e, MDF-e, eSocial"
    ["rh"]="Módulo de RH - Funcionários, Folha de Pagamento, Comissões"
    ["ia"]="Módulo de IA - Inteligência Artificial Assistiva"
    ["servicos"]="Módulo de Serviços - Ordens de Serviço, Manutenção"
)

# Nomes dos JARs
declare -A JAR_NAMES
JAR_NAMES=(
    ["cadastro"]="brasil-saas-erp-cadastro"
    ["financeiro"]="brasil-saas-erp-financeiro"
    ["vendas"]="brasil-saas-erp-vendas"
    ["compras"]="brasil-saas-erp-compras"
    ["estoque"]="brasil-saas-erp-estoque"
    ["fiscal"]="brasil-saas-erp-fiscal"
    ["rh"]="brasil-saas-erp-rh"
    ["ia"]="brasil-saas-erp-ia"
    ["servicos"]="brasil-saas-erp-servicos"
)

# Pacotes base para scan de entidades e repositórios
declare -A BASE_PACKAGES
BASE_PACKAGES=(
    ["cadastro"]="br.com.brasil_saas.cadastro"
    ["financeiro"]="br.com.brasil_saas.financeiro"
    ["vendas"]="br.com.brasil_saas.vendas"
    ["compras"]="br.com.brasil_saas.compras"
    ["estoque"]="br.com.brasil_saas.estoque"
    ["fiscal"]="br.com.brasil_saas.fiscal"
    ["rh"]="br.com.brasil_saas.rh"
    ["ia"]="br.com.brasil_saas.ia"
    ["servicos"]="br.com.brasil_saas.servicos"
)

# Dependências adicionais por módulo
declare -A EXTRA_DEPENDENCIES
EXTRA_DEPENDENCIES=(
    ["fiscal"]="nfe"
    ["ia"]="openai"
)

cd /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN} Criando estrutura de módulos${NC}"
echo -e "${GREEN}=========================================${NC}"

for MODULE in "${MODULES[@]}"; do
    MODULE_DIR="modules/$MODULE"
    
    echo -e "${YELLOW}Processando módulo: $MODULE${NC}"
    
    # Criar estrutura de diretórios
    mkdir -p "$MODULE_DIR/src/main/java/br/com/brasil_saas/$MODULE/{controller,service,repository,model,config,dto,mapper,event,exception,integration,report,validation}"
    mkdir -p "$MODULE_DIR/src/main/resources"
    mkdir -p "$MODULE_DIR/src/test/java/br/com/brasil_saas/$MODULE"
    mkdir -p "$MODULE_DIR/src/test/resources"
    
    # Criar pom.xml
    cat > "$MODULE_DIR/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>br.com.brasil_saas</groupId>
        <artifactId>brasil-saas-erp-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom-parent.xml</relativePath>
    </parent>

    <artifactId>brasil-saas-erp-$MODULE</artifactId>
    <name>Brasil SaaS ERP - ${DESCRIPTIONS[$MODULE]} Module</name>
    <description>${DESCRIPTIONS[$MODULE]}</description>

    <dependencies>
        <!-- ============ WEB ============ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- ============ SEGURANÇA ============ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- ============ BANCO ============ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- ============ CORE (Dependência do módulo core) ============ -->
        <dependency>
            <groupId>br.com.brasil_saas</groupId>
            <artifactId>brasil-saas-erp-core</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!-- ============ SHARED ============ -->
        <dependency>
            <groupId>br.com.brasil_saas</groupId>
            <artifactId>brasil-saas-erp-shared</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!-- ============ Lombok ============ -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- ============ MapStruct ============ -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>

        <!-- ============ OpenAPI ============ -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>

        <!-- ============ PDF ============ -->
        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
        </dependency>
EOF

    # Adicionar dependências específicas do módulo
    if [ -n "${EXTRA_DEPENDENCIES[$MODULE]:-}" ]; then
        for DEP in ${EXTRA_DEPENDENCIES[$MODULE]}; do
            case "$DEP" in
                "nfe")
                    cat >> "$MODULE_DIR/pom.xml" <<EOF
        <!-- ============ NF-e ============ -->
        <dependency>
            <groupId>br.com.swconsultoria</groupId>
            <artifactId>java-nfe</artifactId>
        </dependency>
EOF
                    ;;
                "openai")
                    cat >> "$MODULE_DIR/pom.xml" <<EOF
        <!-- ============ OpenAI ============ -->
        <dependency>
            <groupId>com.theokanning.openai-gpt3-java</groupId>
            <artifactId>service</artifactId>
            <version>0.12.0</version>
        </dependency>
EOF
                    ;;
            esac
        done
    fi

    cat >> "$MODULE_DIR/pom.xml" <<EOF
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <mainClass>br.com.brasil_saas.${MODULE}.${MODULE^}.Application</mainClass>
                    <layout>ZIP</layout>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF
    
    # Criar classe Main
    CLASS_NAME="${MODULE^}"
    cat > "$MODULE_DIR/src/main/java/br/com/brasil_saas/$MODULE/${CLASS_NAME}Application.java" <<EOF
package br.com.brasil_saas.$MODULE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "br.com.brasil_saas.$MODULE",
    "br.com.brasil_saas.core",
    "br.com.brasil_saas.shared"
})
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableScheduling
@EnableJpaRepositories(
    basePackages = {
        "br.com.brasil_saas.$MODULE.repository",
        "br.com.brasil_saas.core.repository",
        "br.com.brasil_saas.shared.repository"
    }
)
@EntityScan(
    basePackages = {
        "br.com.brasil_saas.$MODULE.model",
        "br.com.brasil_saas.core.model",
        "br.com.brasil_saas.shared.model"
    }
)
public class ${CLASS_NAME}Application {

    public static void main(String[] args) {
        SpringApplication.run(${CLASS_NAME}Application.class, args);
    }
}
EOF
    
    # Criar application.yml
    cat > "$MODULE_DIR/src/main/resources/application.yml" <<EOF
spring:
  application:
    name: brasil-saas-erp-$MODULE
  profiles:
    active: dev

  datasource:
    url: "jdbc:postgresql://localhost:5432/brasil-saas?currentSchema=brasil-saas&sslmode=verify-ca&sslrootcert=\${BRASIL_SAAS_CERTS_DIR:/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP/certs/pki}/ca.crt&sslcert=\${BRASIL_SAAS_CERTS_DIR:/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP/certs/pki}/issued/sa.crt&sslkey=\${BRASIL_SAAS_CERTS_DIR:/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP/certs/pki}/private/sa.pk8"
    username: sa
    driver-class-name: org.postgresql.Driver
    hikari:
      pool-name: Brasil SaaS${CLASS_NAME}HikariPool
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000

  jpa:
    show-sql: true
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        default_schema: brasil_saas

  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: brasil-saas
    default-schema: brasil_saas
    baseline-on-migrate: true
    validate-on-migrate: true

  jackson:
    time-zone: America/Sao_Paulo
    date-format: yyyy-MM-dd HH:mm:ss
    default-property-inclusion: non_null

  data:
    mongodb:
      uri: "mongodb://admin:\${MONGODB_PASSWORD:ALTERE_ME}@localhost:27017/brasil_saas?authSource=admin"

server:
  port: ${PORTS[$MODULE]}
  error:
    include-message: always

brasil_saas:
  jwt:
    secret: \${JWT_SECRET:brasil-saas-erp-$MODULE-secret-key-minimo-32-bytes-0123456789}
    expiration-ms: 3600000
    refresh-expiration-ms: 604800000
  imagens:
    diretorio-entrada: \${IMAGENS_ENTRADA_DIR:src/main/resources/static/images}
    monitoramento-intervalo-ms: 5000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
EOF
    
    # Criar systemd service
    cat > "systemd_units/brasil-saas-erp-$MODULE.service" <<EOF
[Unit]
Description=Brasil SaaS ERP ${DESCRIPTIONS[$MODULE]} Module
After=network.target postgresql.service mongodb.service braslcloud-erp-core.service

[Service]
User=euripedes
WorkingDirectory=/home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP
ExecStart=/usr/bin/java -jar modules/$MODULE/target/${JAR_NAMES[$MODULE]}-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JWT_SECRET=brasil-saas-erp-$MODULE-ALTERE_ME_32_BYTES_OU_MAIS
Environment=MONGODB_PASSWORD=ALTERE_ME
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF
    
    echo -e "${GREEN}  ✓ Módulo $MODULE criado com sucesso${NC}"
done

echo -e "${GREEN}"
echo "=========================================="
echo "Todos os módulos foram criados!"
echo "=========================================="
echo -e "${NC}"

# Tornar scripts executáveis
chmod +x scripts/build_module.sh
chmod +x scripts/create_modules.sh

echo -e "${YELLOW}"
echo "Para buildar um módulo:"
echo "  ./scripts/build_module.sh <nome-do-modulo> --clean --skip-tests"
echo ""
echo "Para instalar um serviço:"
echo "  sudo cp systemd_units/brasil-saas-erp-<modulo>.service /etc/systemd/system/"
echo "  sudo systemctl daemon-reload"
echo "  sudo systemctl enable brasil-saas-erp-<modulo>"
echo "  sudo systemctl start brasil-saas-erp-<modulo>"
echo -e "${NC}"
