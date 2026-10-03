# Brasil SaaS ERP - Arquitetura Modular por Serviços

## Visão Geral

O Brasil SaaS ERP agora utiliza uma arquitetura de **Monolito Modular executado como Serviços Independentes**. Esta abordagem permite que cada módulo do ERP rode como um processo separado, garantindo isolamento de falhas e escalabilidade seletiva.

## Vantagens da Arquitetura

✅ **Isolamento de Falhas**: Se o serviço fiscal cair, o financeiro continua operando  
✅ **Deploy Independente**: Cada serviço pode ser reiniciado sem afetar os outros  
✅ **Escalabilidade Seletiva**: Aloque mais recursos para serviços críticos  
✅ **Código Preservado**: Nenhum arquivo foi movido das pastas originais  
✅ **Build Único**: Um único JAR gera todos os serviços via Spring Profiles  
✅ **Gerenciamento Nativo**: Systemd no Linux, Scripts Windows para desenvolvimento  

## Serviços Disponíveis

| Serviço | Porta | Responsabilidade | Memória Máxima | Status |
|---------|-------|-----------------|----------------|--------|
| **Cadastro** | 8081 | Clientes, Fornecedores, Produtos, Marcas, Transportadoras, Categorias, Unidades de Medida | 2GB | ✅ Implementado |
| **Financeiro** | 8082 | Contas a Pagar/Receber, Conciliação, Orçamento, Fluxo de Caixa | 2GB | ✅ Implementado |
| **Fiscal** | 8083 | NF-e, NFC-e, CT-e, MDF-e, NFS-e, EFD ICMS, EFD Contribuições, eSocial, SPED | 3GB | ✅ Implementado |
| **Vendas & Compras** | 8084 | Pedidos de Venda, Compras, Estoque, Faturamento, Entrada de Notas | 2GB | ✅ Implementado |
| **RH** | 8085 | Funcionários, Departamento Pessoal, eSocial, Folha de Pagamento, Comissões | 2GB | ✅ Implementado |
| **Relatórios & BI** | 8086 | Dashboards, Analytics, Relatórios Gerenciais, KPIs | 3GB | ✅ Implementado |
| **IA Corporativa** | 8087 | Spring AI, OpenAI, RAG, Assistente Corporativo | 4GB | ✅ Implementado |
| **Produção Industrial** | 8090 | Ordem de Produção, Insumos, Processo Industrial, Consumo | 3GB | ✅ Implementado |

## Estrutura de Arquivos

```
/workspace
├── src/main/resources/
│   └── application.yml          # Configuração com 8 perfis Spring (7 serviços + produção)
├── scripts/
│   ├── linux/
│   │   ├── start-all.sh         # Inicia todos os serviços
│   │   ├── stop-all.sh          # Para todos os serviços
│   │   ├── brasil-saas-cadastro.service      # Systemd Cadastro
│   │   ├── brasil-saas-financeiro.service    # Systemd Financeiro
│   │   ├── brasil-saas-fiscal.service        # Systemd Fiscal
│   │   ├── brasil-saas-vendas-compras.service # Systemd Vendas/Compras
│   │   ├── brasil-saas-rh.service            # Systemd RH
│   │   ├── brasil-saas-relatorios.service    # Systemd Relatórios/BI
│   │   ├── brasil-saas-ia.service            # Systemd IA
│   │   └── brasil-saas-producao.service      # Systemd Produção
│   └── windows/
│       ├── start-all.bat        # Inicia todos os serviços (Windows)
│       ├── stop-all.bat         # Para todos os serviços (Windows)
│       ├── brasil-saas-cadastro.bat
│       ├── brasil-saas-financeiro.bat
│       ├── brasil-saas-fiscal.bat
│       ├── brasil-saas-vendas-compras.bat
│       ├── brasil-saas-rh.bat
│       ├── brasil-saas-relatorios.bat
│       ├── brasil-saas-ia.bat
│       └── brasil-saas-producao.bat
└── target/
    └── brasil-saas-erp-1.0.0-SNAPSHOT.jar  # JAR único
```

## Instalação e Configuração

### Linux (Produção)

#### 1. Preparar Ambiente

```bash
# Criar usuário e diretórios
sudo useradd -r -s /bin/false brasil-saas
sudo mkdir -p /opt/brasil-saas
sudo mkdir -p /var/log/brasil-saas
sudo chown -R brasil-saas:brasil-saas /opt/brasil-saas
sudo chown -R brasil-saas:brasil-saas /var/log/brasil-saas

# Copiar JAR para produção
cp target/brasil-saas-erp-1.0.0-SNAPSHOT.jar /opt/brasil-saas/brasil-saas-erp.jar

# Copiar arquivos de serviço systemd
sudo cp scripts/linux/*.service /etc/systemd/system/

# Recarregar systemd
sudo systemctl daemon-reload
```

#### 2. Habilitar e Iniciar Serviços

```bash
# Habilitar inicialização automática
sudo systemctl enable brasil-saas-cadastro
sudo systemctl enable brasil-saas-financeiro
sudo systemctl enable brasil-saas-fiscal
sudo systemctl enable brasil-saas-vendas-compras
sudo systemctl enable brasil-saas-rh
sudo systemctl enable brasil-saas-relatorios
sudo systemctl enable brasil-saas-ia

# Iniciar serviços individualmente
sudo systemctl start brasil-saas-cadastro
sudo systemctl start brasil-saas-financeiro
sudo systemctl start brasil-saas-fiscal
# ... etc

# OU iniciar todos de uma vez
cd scripts/linux
./start-all.sh
```

#### 3. Comandos de Gerenciamento

```bash
# Verificar status de um serviço
sudo systemctl status brasil-saas-financeiro

# Parar um serviço específico
sudo systemctl stop brasil-saas-fiscal

# Reiniciar um serviço
sudo systemctl restart brasil-saas-cadastro

# Ver logs em tempo real
journalctl -u brasil-saas-financeiro -f

# Parar todos os serviços
cd scripts/linux
./stop-all.sh
```

### Windows (Desenvolvimento)

#### 1. Preparar Ambiente

```cmd
REM Criar diretório
mkdir C:\brasil-saas
mkdir C:\brasil-saas\scripts

REM Copiar JAR
copy target\brasil-saas-erp-1.0.0-SNAPSHOT.jar C:\brasil-saas\brasil-saas-erp.jar

REM Copiar scripts
copy scripts\windows\*.bat C:\brasil-saas\scripts\
```

#### 2. Executar Serviços

```cmd
cd C:\brasil-saas\scripts

REM Iniciar um serviço específico
brasil-saas-financeiro.bat

REM Iniciar todos os serviços
start-all.bat

REM Parar todos os serviços
stop-all.bat
```

## Como Funciona o Isolamento

Cada serviço é uma instância JVM separada rodando o mesmo JAR, mas com:

1. **Profile Spring diferente**: Ativa apenas os beans do módulo específico
2. **Porta exclusiva**: Evita conflito de bindings
3. **Logging segmentado**: Logs de outros módulos são desativados (OFF)
4. **Recursos independentes**: Heap memory e CPU quota configurados por serviço

### Exemplo de Profile (application.yml)

```yaml
---
spring:
  config:
    activate:
      on-profile: servico-financeiro
server:
  port: 8082
logging:
  level:
    br.com.brasil_saas.financeiro: DEBUG    # Módulo ativo
    br.com.brasil_saas.fiscal: OFF          # Outros desativados
    br.com.brasil_saas.vendas: OFF
```

## Monitoramento

### Health Check por Serviço

```bash
curl http://localhost:8081/actuator/health  # Cadastro
curl http://localhost:8082/actuator/health  # Financeiro
curl http://localhost:8083/actuator/health  # Fiscal
# ... etc
```

### Métricas

```bash
curl http://localhost:8082/actuator/metrics  # Métricas do Financeiro
```

## Troubleshooting

### Serviço não inicia

```bash
# Verificar logs
journalctl -u brasil-saas-financeiro -n 50

# Verificar se porta está em uso
sudo netstat -tlnp | grep 8082

# Verificar se PostgreSQL está rodando
sudo systemctl status postgresql
```

### Memory Leak Suspeito

```bash
# Monitorar uso de memória
watch -n 1 'ps aux | grep brasil-saas'

# Reiniciar serviço
sudo systemctl restart brasil-saas-financeiro
```

### Logs de Erro

```bash
# Buscar erros nos últimos 10 minutos
journalctl -u brasil-saas-fiscal --since "10 minutes ago" | grep ERROR
```

## Próximos Passos (Roadmap)

- [ ] Implementar API Gateway (Spring Cloud Gateway)
- [ ] Adicionar Service Discovery (Eureka/Consul)
- [ ] Configurar Circuit Breaker (Resilience4j)
- [ ] Centralizar logs (ELK Stack ou Graylog)
- [ ] Monitoramento distribuído (Zipkin/Jaeger)
- [ ] Containerização Docker + Kubernetes
- [ ] CI/CD pipeline para deploy independente

## Notas Importantes

⚠️ **PostgreSQL**: Todos os serviços compartilham o mesmo banco de dados. Garanta que o PostgreSQL esteja rodando antes de iniciar qualquer serviço.

⚠️ **Certificados Digitais**: O serviço Fiscal requer acesso aos certificados A1/A3. Configure as variáveis de ambiente apropriadas.

⚠️ **OpenAI API Key**: O serviço de IA requer a variável `OPENAI_API_KEY` configurada.

⚠️ **Memória Total**: A soma das memórias máximas de todos os serviços é ~21GB (incluindo Produção). Ajuste conforme disponibilidade do servidor.

## Suporte

Documentação completa: https://github.com/euripedesdark/BRASIL-SAAS-ERP  
Issues: https://github.com/euripedesdark/BRASIL-SAAS-ERP/issues
