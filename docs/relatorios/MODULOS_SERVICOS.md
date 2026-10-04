> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Módulos e Serviços - Brasil SaaS ERP

## Visão Geral

O Brasil SaaS ERP foi estruturado em módulos independentes que podem ser executados como serviços do Linux (systemd). Cada módulo é um aplicativo Spring Boot que pode ser iniciado, parado e gerenciado independentemente.

## Estrutura de Módulos

| Módulo | Porta | Descrição | Status |
|--------|-------|-----------|--------|
| **core** | 8081 | Autenticação, Autorização, Configurações Base, Usuários, Empresas | ✅ Implementado |
| **shared** | N/A | Library - Classes compartilhadas entre módulos | ✅ Implementado |
| **cadastro** | 8082 | Pessoas, Produtos, Clientes, Fornecedores, Categorias, Marcas, Transportadoras, Unidades de Medida | ✅ Implementado |
| **financeiro** | 8083 | Contas a Pagar/Receber, Lançamentos, Conciliação Bancária | ✅ Implementado |
| **vendas** | 8084 | Pedidos, Orçamentos, Faturamento | ✅ Implementado |
| **compras** | 8085 | Pedidos de Compra, Cotações, Entrada de Notas | ✅ Implementado |
| **estoque** | 8086 | Controle de Saldos, Movimentações | ✅ Implementado |
| **fiscal** | 8087 | NF-e, NFS-e, CT-e, MDF-e, eSocial | ✅ Implementado |
| **rh** | 8088 | Funcionários, Folha de Pagamento, Comissões | ✅ Implementado |
| **ia** | 8089 | Inteligência Artificial Assistiva | ✅ Implementado |
| **servicos** | 8080 | Ordens de Serviço, Manutenção | ✅ Implementado |
| **producao** | 8090 | Ordem de Produção, Insumos, Processo Industrial | ✅ Implementado |
| **bi** | 8091 | Business Intelligence, Dashboards, Relatórios Gerenciais | ✅ Implementado |
| **integracoes** | N/A | Integrações com sistemas externos | 🔄 Em desenvolvimento |
| **portais** | N/A | Portais web especializados | 🔄 Em desenvolvimento |

## Arquitetura

```
brasil-saas-erp/
├── src/main/java/br/com/brasil_saas/
│   ├── core/              # Módulo Core (Serviço)
│   ├── shared/            # Library Shared (Não é serviço)
│   ├── cadastro/          # Módulo Cadastro (Serviço)
│   ├── financeiro/        # Módulo Financeiro (Serviço)
│   ├── vendas/            # Módulo Vendas (Serviço)
│   ├── compras/           # Módulo Compras (Serviço)
│   ├── estoque/           # Módulo Estoque (Serviço)
│   ├── fiscal/            # Módulo Fiscal (Serviço)
│   ├── rh/                # Módulo RH (Serviço)
│   ├── ia/                # Módulo IA (Serviço)
│   ├── servicos/          # Módulo Serviços (Serviço)
│   ├── producao/          # Módulo Produção (Serviço)
│   ├── bi/                # Módulo BI (Serviço)
│   ├── integracoes/       # Módulo Integrações
│   └── portais/           # Módulo Portais
├── modules/               # Módulos independentes (em desenvolvimento)
│   ├── core/
│   ├── cadastro/
│   └── ...
├── pom.xml                # Parent POM
├── systemd_units/         # Arquivos de serviço systemd
└── scripts/               # Scripts de automação
    ├── manage_services.sh  # Script mestre de gerenciamento
    ├── build_module.sh     # Build de módulo individual
    └── create_modules.sh   # Cria estrutura de módulos
```

## Pré-requisitos

1. **Java JDK 21** - Requerido para compilação e execução
2. **Maven 3.6+** - Para build dos módulos
3. **PostgreSQL** - Banco de dados relacional
4. **MongoDB** - Banco de dados para documentos e imagens
5. **Systemd** - Sistema de inicialização do Linux (para gerenciamento de serviços)

## Configuração do Banco de Dados

### PostgreSQL
- **Host**: localhost
- **Porta**: 5432
- **Database**: brasil-saas
- **Schema**: brasil-saas
- **Usuário**: sa (ou configure conforme seu ambiente)

### MongoDB
- **Host**: localhost
- **Porta**: 27017
- **Database**: brasil-saas
- **Usuário**: admin
- **Senha**: ${MONGODB_PASSWORD} (ou configure conforme seu ambiente)

## Build dos Módulos

### Build de um módulo específico

```bash
./scripts/build_module.sh <nome-do-modulo> [OPCOES]

Opções:
  --clean         Executa 'mvn clean' antes do build
  --skip-tests    Pula os testes durante o build
  --install       Instala o módulo no repositório local Maven

Exemplos:
  ./scripts/build_module.sh core --clean --skip-tests
  ./scripts/build_module.sh financeiro --install
```

### Build de todos os módulos

```bash
./scripts/manage_services.sh build-all [OPCOES]

Opções:
  --clean         Executa 'mvn clean' antes do build
  --skip-tests    Pula os testes durante o build
  --install       Instala os módulos no repositório local Maven

Exemplo:
  ./scripts/manage_services.sh build-all --clean --skip-tests
```

**Nota**: Os módulos são buildados na seguinte ordem para respeitar dependências:
1. shared (library)
2. core
3. cadastro
4. financeiro
5. vendas
6. compras
7. estoque
8. fiscal
9. rh
10. ia
11. servicos
12. producao
13. bi

## Gerenciamento de Serviços

### Script de Gerenciamento

O script `scripts/manage_services.sh` fornece uma interface unificada para gerenciar todos os serviços:

```bash
./scripts/manage_services.sh [COMANDO] [MÓDULO] [OPÇÕES]

Comandos disponíveis:
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
  ./scripts/manage_services.sh build core --clean --skip-tests
  ./scripts/manage_services.sh start financeiro
  ./scripts/manage_services.sh install cadastro
  ./scripts/manage_services.sh status fiscal
  ./scripts/manage_services.sh list
  ./scripts/manage_services.sh start-all
```

### Gerenciamento Manual via Systemd

#### Instalar um serviço

```bash
# Copiar arquivo de serviço para systemd
sudo cp systemd_units/brasil-saas-erp-<modulo>.service /etc/systemd/system/

# Recarregar systemd
sudo systemctl daemon-reload

# Habilitar serviço (para iniciar automaticamente no boot)
sudo systemctl enable brasil-saas-erp-<modulo>

# Iniciar serviço
sudo systemctl start brasil-saas-erp-<modulo>

# Verificar status
sudo systemctl status brasil-saas-erp-<modulo>
```

#### Exemplo: Instalar módulo Core

```bash
sudo cp systemd_units/brasil-saas-erp-core.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable brasil-saas-erp-core
sudo systemctl start brasil-saas-erp-core
```

#### Desinstalar um serviço

```bash
# Parar serviço
sudo systemctl stop brasil-saas-erp-<modulo>

# Desabilitar serviço
sudo systemctl disable brasil-saas-erp-<modulo>

# Remover arquivo de serviço
sudo rm /etc/systemd/system/brasil-saas-erp-<modulo>.service

# Recarregar systemd
sudo systemctl daemon-reload
```

## Endpoints dos Módulos

### Core (Porta 8081)
- `POST /api/auth/login` - Login e autenticação JWT
- `POST /api/auth/logout` - Logout
- `GET /api/core/empresas` - Listar empresas
- `POST /api/core/empresas` - Criar empresa
- `GET /api/core/usuarios` - Listar usuários
- `POST /api/core/usuarios` - Criar usuário
- `POST /api/core/empresas/logo` - Upload de logo da empresa
- `GET /api/core/empresas/{id}/logo` - Download de logo da empresa
- `POST /api/core/usuarios/foto` - Upload de foto do usuário
- `GET /api/core/usuarios/{id}/foto` - Download de foto do usuário

### Cadastro (Porta 8082)
- `GET /api/cadastro/pessoas` - Listar pessoas
- `POST /api/cadastro/pessoas` - Criar pessoa
- `GET /api/cadastro/clientes` - Listar clientes
- `POST /api/cadastro/clientes` - Criar cliente
- `GET /api/cadastro/fornecedores` - Listar fornecedores
- `POST /api/cadastro/fornecedores` - Criar fornecedor
- `GET /api/cadastro/produtos` - Listar produtos
- `POST /api/cadastro/produtos` - Criar produto
- `GET /api/cadastro/categorias` - Listar categorias
- `POST /api/cadastro/categorias` - Criar categoria
- `POST /api/cadastro/clientes/logo` - Upload de logo do cliente
- `GET /api/cadastro/clientes/{id}/logo` - Download de logo do cliente
- `POST /api/cadastro/fornecedores/logo` - Upload de logo do fornecedor
- `GET /api/cadastro/fornecedores/{id}/logo` - Download de logo do fornecedor

### Financeiro (Porta 8083)
- `GET /api/financeiro/titulos` - Listar títulos (contas a pagar/receber)
- `POST /api/financeiro/titulos` - Criar título
- `GET /api/financeiro/lancamentos` - Listar lançamentos
- `POST /api/financeiro/lancamentos` - Criar lançamento
- `GET /api/financeiro/contas-bancarias` - Listar contas bancárias
- `POST /api/financeiro/contas-bancarias` - Criar conta bancária
- `GET /api/financeiro/plano-contas` - Listar plano de contas
- `POST /api/financeiro/plano-contas` - Criar conta no plano
- `GET /api/financeiro/centro-custo` - Listar centros de custo
- `POST /api/financeiro/centro-custo` - Criar centro de custo
- `GET /api/financeiro/extrato/{idConta}` - Extrato bancário
- `POST /api/financeiro/baixar` - Baixar título
- `GET /api/financeiro/conciliacao` - Conciliação bancária
- `GET /api/financeiro/fluxo-caixa` - Projeção de fluxo de caixa

### Vendas (Porta 8084)
- `GET /api/vendas/pedidos` - Listar pedidos de venda
- `POST /api/vendas/pedidos` - Criar pedido de venda
- `GET /api/vendas/pedidos/{id}` - Obter pedido por ID
- `PUT /api/vendas/pedidos/{id}` - Atualizar pedido
- `DELETE /api/vendas/pedidos/{id}` - Cancelar pedido
- `GET /api/vendas/orcamentos` - Listar orçamentos
- `POST /api/vendas/orcamentos` - Criar orçamento
- `POST /api/vendas/pedidos/{id}/faturar` - Faturar pedido
- `GET /api/vendas/condicoes-pagamento` - Listar condições de pagamento

### Compras (Porta 8085)
- `GET /api/compras/pedidos` - Listar pedidos de compra
- `POST /api/compras/pedidos` - Criar pedido de compra
- `GET /api/compras/pedidos/{id}` - Obter pedido por ID
- `PUT /api/compras/pedidos/{id}` - Atualizar pedido
- `DELETE /api/compras/pedidos/{id}` - Cancelar pedido
- `GET /api/compras/cotacoes` - Listar cotações
- `POST /api/compras/cotacoes` - Criar cotação

### Estoque (Porta 8086)
- `GET /api/estoque/saldos` - Consultar saldos de estoque
- `GET /api/estoque/saldos/{idProduto}` - Saldo de um produto
- `GET /api/estoque/movimentacoes` - Histórico de movimentações
- `POST /api/estoque/movimentacoes` - Registrar movimentação
- `GET /api/estoque/ajustes` - Listar ajustes de estoque
- `POST /api/estoque/ajustes` - Criar ajuste de estoque
- `POST /api/estoque/entrada` - Entrada de estoque (pela compra)
- `POST /api/estoque/saida` - Saída de estoque (pela venda)

### Fiscal (Porta 8087)
- `POST /api/fiscal/nfe/emitir` - Emitir NF-e
- `GET /api/fiscal/nfe/{chave}` - Consultar NF-e por chave
- `POST /api/fiscal/nfe/cancelar` - Cancelar NF-e
- `POST /api/fiscal/nfe/inutilizar` - Inutilizar numeração
- `GET /api/fiscal/nfe/status` - Status do serviço SEFAZ
- `POST /api/fiscal/nfse/emitir` - Emitir NFS-e
- `GET /api/fiscal/nfse/{numero}` - Consultar NFS-e
- `POST /api/fiscal/esocial/enviar` - Enviar evento eSocial
- `GET /api/fiscal/esocial/{protocolo}` - Consultar protocolo eSocial

### RH (Porta 8088)
- `GET /api/rh/funcionarios` - Listar funcionários
- `POST /api/rh/funcionarios` - Criar funcionário
- `GET /api/rh/cargos` - Listar cargos
- `POST /api/rh/cargos` - Criar cargo
- `GET /api/rh/folha/{competencia}` - Folha de pagamento por competência
- `POST /api/rh/folha/gerar` - Gerar folha de pagamento
- `GET /api/rh/comissoes` - Listar comissões
- `POST /api/rh/funcionarios/foto` - Upload de foto do funcionário
- `GET /api/rh/funcionarios/{id}/foto` - Download de foto do funcionário

### IA (Porta 8089)
- `POST /api/ia/chat` - Chat com IA assistiva
- `POST /api/ia/analise` - Análise de dados
- `GET /api/ia/alertas` - Listar alertas da IA
- `POST /api/ia/recomendacoes` - Solicitar recomendações
- `GET /api/ia/historico` - Histórico de interações com IA

### Produção (Porta 8090)
- `GET /api/producao/ordens` - Listar ordens de produção
- `POST /api/producao/ordens` - Criar ordem de produção
- `GET /api/producao/ordens/{id}` - Obter OP por ID
- `PUT /api/producao/ordens/{id}` - Atualizar OP
- `POST /api/producao/ordens/{id}/iniciar` - Iniciar produção
- `POST /api/producao/ordens/{id}/concluir` - Concluir produção
- `GET /api/producao/insumos` - Listar insumos
- `POST /api/producao/insumos` - Registrar consumo de insumos
- `GET /api/producao/processos` - Listar processos industriais

### BI (Porta 8091)
- `GET /api/bi/dashboards` - Listar dashboards disponíveis
- `GET /api/bi/kpis` - Indicadores de desempenho (KPIs)
- `GET /api/bi/relatorios` - Relatórios gerenciais
- `POST /api/bi/analises` - Análises personalizadas
- `GET /api/bi/metricas/vendas` - Métricas de vendas
- `GET /api/bi/metricas/financeiro` - Métricas financeiras
- `GET /api/bi/metricas/producao` - Métricas de produção

### Serviços (Porta 8080)
- `GET /api/servicos/ordens` - Listar ordens de serviço
- `POST /api/servicos/ordens` - Criar ordem de serviço
- `GET /api/servicos/ordens/{id}` - Obter OS por ID
- `PUT /api/servicos/ordens/{id}` - Atualizar OS
- `POST /api/servicos/ordens/{id}/fechar` - Fechar ordem de serviço
- `GET /api/servicos/ordens/{id}/pdf` - Gerar PDF da OS
- `POST /api/servicos/ordens/{id}/enviar-email` - Enviar OS por email

## Dependências entre Módulos

```
┌─────────────┐
│   shared     │  (Library - Não é serviço)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    core      │  (Porta 8081)
└──────┬──────┘
       │
       ├───▶ cadastro (8082)
       │
       ├───▶ financeiro (8083)
       │
       ├───▶ vendas (8084)
       │
       ├───▶ compras (8085)
       │
       ├───▶ estoque (8086)
       │
       ├───▶ fiscal (8087)
       │
       ├───▶ rh (8088)
       │
       ├───▶ ia (8089)
       │
       ├───▶ servicos (8080)
       │
       ├───▶ producao (8090)
       │
       └─────▶ bi (8091)
```

## Comunicação entre Módulos

Os módulos se comunicam via:
1. **REST API** - Chamadas HTTP entre módulos
2. **Banco de Dados Compartilhado** - PostgreSQL com schema `brasil-saas`
3. **MongoDB** - Para armazenamento de documentos e imagens
4. **Eventos** - Sistema de eventos assíncronos (futuro)

### Exemplo de Chamada entre Módulos

Do módulo **vendas** para **financeiro** (ao faturar um pedido):

```bash
# Chamada do serviço de vendas para criar título financeiro
curl -X POST http://localhost:8083/api/financeiro/titulos \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Pedido de Venda #123",
    "valor": 1500.00,
    "dataVencimento": "2026-10-19",
    "clienteId": 456,
    "tipo": "RECEBER"
  }'
```

## Configurações de Segurança

### Autenticação JWT
Todos os módulos compartilham o mesmo sistema de autenticação JWT. O módulo **core** é responsável por:
- Emitir tokens JWT
- Validar tokens
- Gerenciar sessões

### Configuração do JWT

Em cada módulo, no arquivo `application.yml`:

```yaml
brasil-saas:
  jwt:
    secret: ${JWT_SECRET:brasil-saas-erp-<modulo>-secret-key-minimo-32-bytes-0123456789}
    expiration-ms: 3600000  # 1 hora
    refresh-expiration-ms: 604800000  # 7 dias
```

**Importante**: Todos os módulos devem usar a MESMA secret key para que os tokens sejam válidos entre os módulos.

### Variáveis de Ambiente

Variáveis comuns a todos os módulos:

```bash
# PostgreSQL
export DATABASE_URL="jdbc:postgresql://localhost:5432/brasil-saas?currentSchema=brasil-saas"
export DATABASE_USERNAME="sa"
export DATABASE_PASSWORD="<sua-senha>"

# MongoDB
export MONGODB_URI="mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil-saas?authSource=admin"

# JWT
export JWT_SECRET="ALTERE_ME_32_BYTES_OU_MAIS"

# Imagens
export IMAGENS_ENTRADA_DIR="/path/para/imagens"
```

## Monitoramento

### Endpoints de Health Check

Cada módulo expõe endpoints de monitoramento:

- `GET /actuator/health` - Health check básico
- `GET /actuator/info` - Informações do módulo
- `GET /actuator/metrics` - Métricas do módulo

### Logs

Os logs de cada módulo podem ser visualizados via:

```bash
# Logs de um módulo específico
sudo journalctl -u brasil-saas-erp-<modulo> -f

# Logs de todos os módulos
sudo journalctl -u brasil-saas-erp-* -f

# Logs históricos
sudo journalctl -u brasil-saas-erp-<modulo> --since "2026-09-19" --until "2026-09-20"
```

## Solução de Problemas

### Erro: Porta já em uso

Verifique qual processo está usando a porta:

```bash
sudo lsof -i :8081
sudo netstat -tulnp | grep 8081
```

Matar o processo:

```bash
sudo kill -9 <PID>
```

### Erro: Conexão com PostgreSQL

Verifique se o PostgreSQL está rodando:

```bash
sudo systemctl status postgresql
```

Teste a conexão:

```bash
psql -h localhost -p 5432 -U sa -d brasil-saas
```

### Erro: Conexão com MongoDB

Verifique se o MongoDB está rodando:

```bash
sudo systemctl status mongodb
```

Teste a conexão:

```bash
mongo --host localhost --port 27017 -u admin -p ${MONGODB_PASSWORD} --authenticationDatabase admin
```

### Erro: Módulo não compila

Verifique as dependências:

```bash
cd modules/<modulo>
mvn clean compile -X
```

O flag `-X` mostra logs detalhados do Maven.

### Erro: Dependência não encontrada

Se o módulo **shared** ou **core** não for encontrado durante o build:

```bash
# Primeiro faça build do módulo shared e core
./scripts/build_module.sh shared --install
./scripts/build_module.sh core --install

# Depois faça build do módulo dependente
./scripts/build_module.sh <modulo> --install
```

## Boas Práticas

1. **Build na ordem correta**: Sempre build `shared` e `core` primeiro
2. **Usar o script de gerenciamento**: Prefira usar `scripts/manage_services.sh` para evitar erros
3. **Verificar logs**: Sempre verifique os logs após iniciar um serviço
4. **Configurar variáveis de ambiente**: Use um arquivo `.env` ou configure no systemd
5. **Backup do banco**: Faça backup antes de atualizações grandes
6. **Testar em desenvolvimento**: Testar cada módulo individualmente antes de deploy em produção

## Ambientes

### Desenvolvimento
- Use `application-dev.yml`
- Habilite logs detalhados
- Desabilite autenticação JWT (opcional)

### Homologação
- Use `application-hom.yml`
- Habilite autenticação JWT
- Configure conexões seguras com banco

### Produção
- Use `application-prod.yml`
- Habilite autenticação JWT
- Configure SSL/TLS
- Configure logs para arquivo
- Habilite monitoramento

## Scripts Úteis

### Verificar status de todos os serviços

```bash
./scripts/manage_services.sh list-services
```

### Reiniciar todos os serviços

```bash
./scripts/manage_services.sh restart-all
```

### Build e deploy completo

```bash
# 1. Build de todos os módulos
./scripts/manage_services.sh build-all --clean --skip-tests --install

# 2. Instalar todos os serviços
for module in core cadastro financeiro vendas compras estoque fiscal rh ia servicos; do
    ./scripts/manage_services.sh install $module
done

# 3. Iniciar todos os serviços
./scripts/manage_services.sh start-all
```

## Requisitos de Hardware

| Módulo | CPU | Memória | Armazenamento |
|--------|-----|---------|--------------|
| Core | 1 vCPU | 1 GB | 500 MB |
| Cadastro | 1 vCPU | 1 GB | 500 MB |
| Financeiro | 2 vCPU | 2 GB | 1 GB |
| Vendas | 1 vCPU | 1 GB | 500 MB |
| Compras | 1 vCPU | 1 GB | 500 MB |
| Estoque | 1 vCPU | 1 GB | 500 MB |
| Fiscal | 2 vCPU | 2 GB | 2 GB |
| RH | 1 vCPU | 1 GB | 500 MB |
| IA | 2 vCPU | 4 GB | 2 GB |
| Serviços | 1 vCPU | 1 GB | 500 MB |

**Total mínimo para todos os módulos**: 4 vCPUs, 8 GB RAM, 10 GB Armazenamento

## Segurança

### Firewall

Habilite as portas dos módulos no firewall:

```bash
# Para UFW
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089; do
    sudo ufw allow $port
done

# Para firewalld
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089; do
    sudo firewall-cmd --permanent --add-port=$port/tcp
    sudo firewall-cmd --reload
done
```

### SSL/TLS

Configure SSL/TLS para cada módulo em produção. Adicione ao `application-prod.yml`:

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore/brasil-saas-erp.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12
```

### Backup

Faça backup regular dos:
1. Banco de dados PostgreSQL
2. Banco de dados MongoDB
3. Certificados digitais
4. Arquivos de configuração

Exemplo de backup do PostgreSQL:

```bash
pg_dump -h localhost -p 5432 -U sa -d brasil-saas > backup_brasil-saas_$(date +%Y%m%d).sql
```

Exemplo de backup do MongoDB:

```bash
mongodump --host localhost --port 27017 --username admin --password ${MONGODB_PASSWORD} --authenticationDatabase admin --out backup_mongodb_$(date +%Y%m%d)
```

## Atualizações

Para atualizar um módulo:

1. Pare o serviço:
   ```bash
   sudo systemctl stop brasil-saas-erp-<modulo>
   ```

2. Faça backup do JAR atual:
   ```bash
   cp modules/<modulo>/target/*.jar modules/<modulo>/target/backup/
   ```

3. Faça build do módulo:
   ```bash
   ./scripts/build_module.sh <modulo> --clean --skip-tests
   ```

4. Inicie o serviço:
   ```bash
   sudo systemctl start brasil-saas-erp-<modulo>
   ```

5. Verifique os logs:
   ```bash
   sudo journalctl -u brasil-saas-erp-<modulo> -f
   ```

## Contribuições

1. Siga a estrutura de módulos existente
2. Adicione novos endpoints à documentação
3. Atualize os arquivos de migração do Flyway
4. Mantenha compatibilidade com as APIs existentes
5. Escreva testes para novas funcionalidades

## Licença

Este projeto é propriedade da Brasil SaaS ERP e está protegido por direitos autorais.

## Contato

Para dúvidas ou suporte, entre em contato com a equipe de desenvolvimento.
