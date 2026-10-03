# Scripts e Estrutura de Módulos - Brasil SaaS ERP

## Estrutura Atual

O projeto Brasil SaaS ERP está organizado da seguinte forma:

```
brasil_saas-erp/
├── src/main/java/br/com/brasil_saas/  # Código fonte principal (MONÓLITO FUNCIONANDO)
│   ├── core/         # Módulo Core
│   ├── cadastro/     # Módulo Cadastro
│   ├── financeiro/   # Módulo Financeiro
│   ├── vendas/       # Módulo Vendas
│   ├── compras/      # Módulo Compras
│   ├── estoque/      # Módulo Estoque
│   ├── fiscal/       # Módulo Fiscal
│   ├── rh/          # Módulo RH
│   ├── ia/          # Módulo IA
│   └── servicos/     # Módulo Serviços
├── modules/          # Estrutura para módulos independentes (EM DESENVOLVIMENTO)
│   ├── core/         # Módulo Core (cópia do src)
│   ├── shared/       # Módulo Shared
│   ├── cadastro/     # Módulo Cadastro (cópia do src)
│   └── ...
├── systemd_units/    # Arquivos de serviço systemd (PRONTOS)
│   ├── brasil_saas-erp-core.service
│   ├── brasil_saas-erp-cadastro.service
│   └── ...
├── scripts/          # Scripts de automação
│   ├── manage_services.sh    # Gerenciamento de serviços
│   ├── build_module.sh        # Build de módulo individual
│   ├── create_modules.sh      # Cria estrutura de módulos
│   └── build_standalone.sh    # Build standalone
└── pom.xml           # POM principal (MONÓLITO - FUNCIONANDO)
```

## Status Atual

✅ **FUNCIONANDO**:
- **Monólito principal** - Build com `mvn compile` ou `mvn package` funciona
- **Systemd units** - Arquivos de serviço criados para todos os módulos
- **Estrutura de módulos** - Pastas criadas em `modules/` com código copiado
- **Scripts de gerenciamento** - `manage_services.sh` pronto para uso

⚠️ **EM DESENVOLVIMENTO**:
- Build multi-módulo Maven (pom-multimodule.xml)
- Build standalone de cada módulo

## Como Usar AGORA

### Opção 1: Usar o Monólito (RECOMENDADO)

O projeto já funciona como monólito. Para compilar e executar:

```bash
# Compilar
cd /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP
mvn clean package -DskipTests

# Executar
java -jar target/brasil_saas-erp-1.0.0-SNAPSHOT.jar

# ou com profile
java -jar target/brasil_saas-erp-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Opção 2: Instalar como Serviço Systemd

Para instalar o sistema completo como um único serviço:

```bash
# Copiar arquivo de serviço
sudo cp systemd_units/brasil_saas-erp.service /etc/systemd/system/

# Recarregar systemd
sudo systemctl daemon-reload

# Habilitar serviço
sudo systemctl enable brasil_saas-erp

# Iniciar serviço
sudo systemctl start brasil_saas-erp

# Verificar status
sudo systemctl status brasil_saas-erp

# Ver logs
sudo journalctl -u brasil_saas-erp -f
```

### Opção 3: Serviços por Módulo (EM DESENVOLVIMENTO)

Os arquivos de serviço para cada módulo já estão criados em `systemd_units/`:

- `brasil_saas-erp-core.service` (porta 8081)
- `brasil_saas-erp-cadastro.service` (porta 8082)
- `brasil_saas-erp-financeiro.service` (porta 8083)
- `brasil_saas-erp-vendas.service` (porta 8084)
- `brasil_saas-erp-compras.service` (porta 8085)
- `brasil_saas-erp-estoque.service` (porta 8086)
- `brasil_saas-erp-fiscal.service` (porta 8087)
- `brasil_saas-erp-rh.service` (porta 8088)
- `brasil_saas-erp-ia.service` (porta 8089)
- `brasil_saas-erp-servicos.service` (porta 8080)

Para instalar todos os serviços:

```bash
cd /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP

# Instalar todos os serviços
for service in systemd_units/brasil_saas-erp-*.service; do
    sudo cp "$service" /etc/systemd/system/
done

# Recarregar e habilitar
sudo systemctl daemon-reload
for service in brasil_saas-erp-core brasil_saas-erp-cadastro brasil_saas-erp-financeiro; do
    sudo systemctl enable "$service"
    sudo systemctl start "$service"
done
```

**ATENÇÃO**: Para usar os serviços por módulo, você precisa:
1. Buildar cada módulo individualmente
2. Gerar JARs separados
3. Configurar o banco de dados compartilhado

## Scripts Disponíveis

### manage_services.sh

Script mestre para gerenciar todos os módulos e serviços:

```bash
./scripts/manage_services.sh [COMANDO] [MÓDULO] [OPÇÕES]

Comandos:
  build [MODULE] [--clean] [--skip-tests] [--install]   - Build de um módulo
  build-all [--clean] [--skip-tests] [--install]        - Build de todos os módulos
  start [MODULE]                                           - Inicia um serviço
  stop [MODULE]                                            - Para um serviço
  restart [MODULE]                                         - Reinicia um serviço
  status [MODULE]                                          - Mostra status de um serviço
  install [MODULE]                                         - Instala serviço no systemd
  uninstall [MODULE]                                       - Remove serviço do systemd
  list                                                   - Lista todos os módulos
  list-services                                          - Lista serviços instalados
  start-all                                              - Inicia todos os serviços
  stop-all                                               - Para todos os serviços
  restart-all                                            - Reinicia todos os serviços

Exemplos:
  ./scripts/manage_services.sh list
  ./scripts/manage_services.sh status core
  ./scripts/manage_services.sh install financeiro
  ./scripts/manage_services.sh start-all
```

### build_module.sh

Script para build de um módulo específico:

```bash
./scripts/build_module.sh <nome-do-modulo> [OPÇÕES]

Opções:
  --clean         Executa 'mvn clean' antes do build
  --skip-tests    Pula os testes durante o build
  --install       Instala o módulo no repositório local Maven

Exemplo:
  ./scripts/build_module.sh core --clean --skip-tests
```

## O que foi Feito

### 1. Estrutura de Módulos

Criada pasta `modules/` com subpastas para cada módulo:
- core
- shared
- cadastro
- financeiro
- vendas
- compras
- estoque
- fiscal
- rh
- ia
- servicos

Cada módulo tem:
- `src/main/java/br/com/brasil_saas/<modulo>/` - Código fonte (copiado do src principal)
- `src/main/resources/` - Recursos
- `pom.xml` - Arquivo POM (em desenvolvimento)

### 2. Classes Main para Cada Módulo

Criadas classes principal para cada módulo:
- `CoreApplication.java`
- `CadastroApplication.java`
- `FinanceiroApplication.java`
- etc.

Cada classe é anotada com:
- `@SpringBootApplication`
- `@EnableJpaAuditing`
- `@EnableScheduling`
- `@EnableJpaRepositories`
- `@EntityScan`

### 3. Systemd Units

Criados arquivos de serviço para o systemd:
- Cada serviço tem Porta única
- Dependências configuradas (PostgreSQL, MongoDB, etc.)
- Variáveis de ambiente configuradas
- Restart automático em caso de falha

### 4. Arquivos de Configuração

Cada módulo tem:
- `application.yml` - Configuração Spring Boot
- Portas únicas para evitar conflitos
- Configuração de banco de dados
- Configuração JWT

### 5. Documentação

- `docs/relatorios/MODULOS_SERVICOS.md` - Documentação completa
- `docs/infra/README_MODULES.md` - Este arquivo
- Atualizações nos arquivos existentes

## Próximos Passos (Para você, Eurípedes)

### 1. Testar o Monólito

Verifique se o build principal funciona:

```bash
cd /home/euripedes/OneDrive/python/projetos-leno/JAVA/BRASIL-SAAS-ERP
mvn clean compile -DskipTests
```

Se funcionar, você pode usar o monólito normalmente.

### 2. Instalar como Serviço

Instale o sistema como serviço:

```bash
sudo cp systemd_units/brasil_saas-erp.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable brasil_saas-erp
sudo systemctl start brasil_saas-erp
```

### 3. Configurar Módulos Independentes (OPCIONAL)

Se você quiser modularizar o sistema:

#### a. Finalizar pom.xml dos módulos
- Corrigir dependências
- Adicionar repositórios necessários
- Testar build de cada módulo

#### b. Configurar Banco de Dados Compartilhado
- Todos os módulos usam o mesmo PostgreSQL e MongoDB
- Configurar usuário e permissões

#### c. Testar Comunicação entre Módulos
- Configurar autenticação JWT compartilhada
- Testar chamadas REST entre módulos

### 4. Configurar Firewall

Habilitar portas no firewall:

```bash
# Para UFW
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089; do
    sudo ufw allow $port
done
```

### 5. Testar End-to-End

- Acessar cada módulo na sua porta
- Testar autenticação
- Testar operações CRUD
- Verificar logs

## Solução de Problemas

### Erro: "Cannot find symbol"

Isso significa que uma classe está faltando. Verifique:
1. Se o pom.xml do módulo tem todas as dependências necessárias
2. Se o código foi copiado corretamente
3. Se as classes shared estão acessíveis

### Erro: "Non-resolvable parent POM"

O Maven não encontrou o POM parent. Verifique:
1. O arquivo `pom-multimodule.xml` existe
2. O path relativo está correto
3. O parent foi instalado no repositório local

### Erro: Conexão com banco

Verifique:
1. PostgreSQL está rodando: `sudo systemctl status postgresql`
2. MongoDB está rodando: `sudo systemctl status mongodb`
3. Credenciais estão corretas no `application.yml`

## Arquivos Importantes

- `pom.xml` - POM principal do monólito (FUNCIONANDO)
- `pom-multimodule.xml` - POM parent para multi-módulo (EM DESENVOLVIMENTO)
- `pom-parent.xml` - POM parent alternativo (CRIADO)
- `systemd_units/*.service` - Arquivos de serviço systemd (PRONTOS)
- `scripts/*.sh` - Scripts de automação (PRONTOS)
- `docs/relatorios/MODULOS_SERVICOS.md` - Documentação completa (CRIADO)

## Resumo

✅ **PRONTO**:
- Estrutura de módulos criada
- Systemd units para cada módulo
- Scripts de gerenciamento
- Documentação
- Monólito funcionando

⚠️ **EM DESENVOLVIMENTO**:
- Build multi-módulo Maven
- Dependências entre módulos
- Build standalone

🎯 **RECOMENDAÇÃO**: Use o monólito por enquanto. A estrutura de módulos está pronta para quando você quiser modularizar.

## Contato

Para dúvidas ou problemas, revise:
1. Este arquivo (docs/infra/README_MODULES.md)
2. docs/relatorios/MODULOS_SERVICOS.md
3. Os scripts em `scripts/`

Se precisar de ajuda adicional, entre em contato!
