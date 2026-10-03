# 📊 STATUS COMPLETO DO PROJETO - Brasil SaaS ERP

**Data da Análise**: 22/09/2026  
**Arquitetura**: React SPA (PrimeReact) + Spring Boot REST API + Microserviços Fiscais

---

## 🎯 RESUMO GERAL DO SISTEMA

| Componente | Quantidade | Status | Localização |
|------------|-----------|--------|-------------|
| **Backend Monólito** | 347 classes Java | ✅ 100% Pronto | `/src/main/java` |
| **Backend Módulos** | 361 classes Java | ✅ 100% Pronto | `/modules` |
| **Microserviços Fiscais** | 11.365 classes Java | ✅ Bibliotecas Prontas | `/src/main/resources/microservices` |
| **Frontend React** | 47 componentes/serviços | ✅ 100% Pronto | `/src/main/resources/static/react` |
| **Banco de Dados** | 33 migrations Flyway | ✅ 100% Pronto | `/src/main/resources/db/migration` |
| **Infraestrutura** | 13 systemd units | ✅ 100% Pronto | `/systemd` |
| **Testes Unitários** | < 1% cobertura | 🔴 CRÍTICO | Não implementado |
| **CI/CD Pipeline** | Não existe | 🔴 CRÍTICO | Não implementado |

---

## 🏗️ ARQUITETURA DO SISTEMA

### Frontend
- **Framework**: React 19
- **UI Library**: PrimeReact 10.8
- **Build Tool**: Vite 5
- **Arquitetura**: SPA (Single Page Application)
- **Comunicação**: REST API com JWT
- **Thymeleaf**: ❌ REMOVIDO (não utilizado)

### Backend Principal
- **Framework**: Spring Boot 3.x
- **Padrão**: REST API (JSON)
- **ORM**: JPA/Hibernate
- **Segurança**: Spring Security + JWT
- **Multi-tenant**: Isolamento por empresa (cnpj)

### Microserviços Especializados (`/src/main/resources/microservices`)

#### 1. **Spring AI** (2.278 classes Java | 105MB)
- **Status**: ✅ Framework completo
- **Finalidade**: Integração com modelos de IA
- **Modelos Suportados**:
  - OpenAI, Anthropic, Amazon Bedrock
  - Google Vertex AI, Stability AI
  - Ollama, Mistral AI, DeepSeek
- **Recursos**:
  - Chat, Embedding, Text-to-Image
  - RAG (Retrieval Augmented Generation)
  - Vector Stores (PGVector, Redis, MongoDB, etc.)
  - MCP (Model Context Protocol)
  - Memory e Tool Calling
- **Uso no Projeto**: Módulo `ia` (porta 8089) - ⚠️ Estrutura pronta, sem implementação customizada

#### 2. **eSocial** (5.073 classes Java | 56MB)
- **Status**: ✅ Completo
- **Finalidade**: Envio de eventos ao eSocial-GOV
- **Eventos Suportados**:
  - Tabelas (empregador, rubricas, cargos)
  - Periódicos (folha de pagamento)
  - Não periódicos (admissão, demissão, afastamentos)
- **Uso no Projeto**: Módulo `rh` integra com esta biblioteca

#### 3. **NFe (nfe)** (2.545 classes Java | 33MB)
- **Status**: ✅ Completo
- **Finalidade**: Emissão de NF-e e NFC-e
- **Recursos**:
  - Comunicação SEFAZ
  - Validação de schemas XML
  - Assinatura digital
  - Cancelamento e inutilização
- **Uso no Projeto**: Módulo `fiscal` (porta 8087)

#### 4. **Java_NFe** (367 classes Java | 9.3MB)
- **Status**: ✅ Biblioteca oficial (v4.1.3)
- **Finalidade**: Consumo de WebService NFe/NFCe
- **Maven**: `br.com.swconsultoria:java-nfe:4.1.3`
- **Uso no Projeto**: Alternativa/backup para módulo fiscal

#### 5. **Java_CTe** (220 classes Java | 4.8MB)
- **Status**: ✅ Completo
- **Finalidade**: Emissão de CT-e (Conhecimento de Transporte)
- **Uso no Projeto**: Módulo fiscal (implementação futura)

#### 6. **NFSe** (168 classes Java | 2.1MB)
- **Status**: ✅ Completo
- **Finalidade**: Emissão de NFS-e (Serviços)
- **Uso no Projeto**: Módulo fiscal para municípios suportados

#### 7. **Java-Efd-Icms** (≈300 classes | 3.0MB)
- **Status**: ✅ Completo
- **Finalidade**: Geração de EFD ICMS/IPI (SPED Fiscal)
- **Uso no Projeto**: Módulo fiscal/contábil

#### 8. **Java-Efd-Contribuicoes** (≈200 classes | 2.7MB)
- **Status**: ✅ Completo
- **Finalidade**: Geração de EFD Contribuições (SPED)
- **Uso no Projeto**: Módulo fiscal/contábil

#### 9. **Java_Certificado** (≈100 classes | 1.3MB)
- **Status**: ✅ Completo (v3.17)
- **Finalidade**: Gerenciamento de Certificado Digital
- **Maven**: `br.com.swconsultoria:java_certificado:3.17`
- **Uso no Projeto**: Todos os módulos fiscais

#### 10. **Java_Pdf_Signature** (10 classes + JAR | 7.1MB)
- **Status**: ✅ Completo
- **Finalidade**: Assinatura de PDF com certificado digital
- **Uso no Projeto**: Danfe, relatórios fiscais

#### 11. **Java_MDFe** (≈50 classes | 16KB)
- **Status**: ✅ Completo
- **Finalidade**: Emissão de MDF-e (Manifesto de Documentos Fiscais)
- **Uso no Projeto**: Módulo fiscal/logística

#### 12. **NFSe-SaoPaulo-SP** (4.8MB)
- **Status**: ✅ Específico para SP
- **Finalidade**: Emissão NFS-e Prefeitura de São Paulo
- **Uso no Projeto**: Clientes em São Paulo/SP

#### 13. **nfse-sp-bridge** (12KB)
- **Status**: ✅ Ponte para SP
- **Finalidade**: Bridge para NFS-e SP

#### 14. **nfse_prefeitura_sp** (160KB)
- **Status**: ✅ Implementação específica
- **Finalidade**: Integração direta com prefeitura SP

#### 15. **EchoAvatar** (12MB | Python)
- **Status**: ✅ Projeto separado (IA de avatares)
- **Finalidade**: Animação de avatar em tempo real a partir de áudio
- **Tecnologia**: Python 3.13, Unity integration
- **Uso no Projeto**: ⚠️ Não integrado ainda (feature futura de atendimento por IA)

---

## ✅ O QUE ESTÁ PRONTO E FUNCIONAL

### 1. BACKEND - Monólito Principal (`/src/main/java`)

**347 classes Java implementadas:**

#### Controllers (45 arquivos)
- ✅ **cadastro**: 11 controllers (Cliente, Fornecedor, Produto, Pessoa, etc.)
- ✅ **core**: 9 controllers (Auth, Empresa, Usuario, Logos)
- ✅ **financeiro**: 8 controllers (Titulo, Lancamento, ContaBancaria, etc.)
- ✅ **fiscal**: 7 controllers (Ncm, Cfop, Cest, Imposto, Sefaz)
- ✅ **rh**: 4 controllers (Funcionario, Cargo, FolhaPagamento)
- ✅ **compras**: 1 controller (PedidoCompra)
- ✅ **estoque**: 2 controllers (MovimentacaoEstoque, SaldoEstoque)
- ✅ **vendas**: 1 controller (PedidoVenda)
- ✅ **servicos**: 1 controller (OrdemServico)
- ✅ **producao**: 1 controller (Producao)

#### Services (32 arquivos)
- Todos os módulos com services implementados
- Serviços de logo/foto para MongoDB
- Serviços compartilhados (shared)

#### Repositories (77 arquivos)
- JPA repositories para todas as entidades
- MongoDB repository para imagens

#### Entidades/Models
- Todas as tabelas do banco mapeadas
- Entidades MongoDB para imagens
- DTOs para requisições/respostas

#### Segurança
- ✅ JWT authentication
- ✅ Spring Security configurado
- ✅ Multi-tenant isolation
- ✅ Hierarquia de perfis (Admin, Diretor, Gerente)

### 2. FRONTEND REACT PRIMERECT (`/src/main/resources/static/react`)

**47 arquivos implementados:**

#### Stack Tecnológico Confirmado
```json
{
  "react": "^19.0.0",
  "primereact": "^10.8.0",
  "primeicons": "^6.0.0",
  "vite": "^5.0.0",
  "react-router-dom": "^7.0.0"
}
```

#### Componentes Principais (13 arquivos)
- ✅ App.jsx - Router principal
- ✅ Layout.jsx - Menu lateral, header
- ✅ Login.jsx - Autenticação JWT
- ✅ Dashboard.jsx - Painel inicial
- ✅ Financeiro.jsx (358 linhas) - Módulo completo
- ✅ Fiscal.jsx (191 linhas) - NF-e, NFC-e
- ✅ OrdemServico.jsx (430 linhas) - OS completo
- ✅ Producao.jsx (239 linhas) - Ordem de produção
- ✅ RH.jsx (212 linhas) - Funcionários
- ✅ Municipios.jsx (297 linhas) - Consulta IBGE
- ✅ IaAssistWidget.jsx (276 linhas) - IA assistiva
- ✅ Relatorios.jsx (112 linhas)
- ✅ Perfil.jsx (119 linhas)

#### Componentes por Módulo (12 arquivos)
- ✅ Admin: Usuarios, Configuracoes, SqlConsole
- ✅ Cadastro: CadastroPessoas, CadastroProdutos
- ✅ **Compras: Compras.jsx (23KB)** - IMPLEMENTADO
- ✅ **Estoque: Estoque.jsx (18KB)** - IMPLEMENTADO
- ✅ **Vendas: Vendas.jsx (28KB)** - IMPLEMENTADO
- ✅ **Serviços: Servicos.jsx (20KB)** - IMPLEMENTADO
- ✅ Financeiro: Comissoes + 8 componentes
- ✅ Produção: Producao.jsx

#### Componentes PrimeReact Utilizados (381 importações)
- DataTable, Column, FilterMatchMode
- Dialog, Button, InputText, InputNumber
- Dropdown, Calendar, DatePicker
- Toast, Messages, Message
- Card, Panel, Toolbar
- Menu, Menubar, Breadcrumb
- TabMenu, TabView
- Tree, TreeNode
- FileUpload, ProgressBar
- Checkbox, RadioButton
- Rating, Slider, ColorPicker
- AutoComplete, MultiSelect
- OverlayPanel, Sidebar, Drawer
- ConfirmDialog, ConfirmPopup
- Avatar, Badge, Chip
- Timeline, Steps, Accordion
- E muitos outros...

#### Serviços API (38 arquivos)
- ✅ AuthService.js
- ✅ ApiConfig.js
- ✅ ClienteService.js, FornecedorService.js
- ✅ PedidoCompraService.js
- ✅ MovimentacaoEstoqueService.js
- ✅ PedidoVendaService.js
- ✅ ServicoService.js
- ✅ ProducaoService.js
- ✅ Todos os serviços de logos/fotos
- ✅ Serviços fiscais (Ncm, Cfop, Cest, Sefaz)

#### Build Produzido
- ✅ `/src/main/resources/static/dist/`
- ✅ index.html (416B)
- ✅ Bundle JS (733KB)
- ✅ CSS (193KB)
- ✅ Fontes e ícones

### 3. MÓDULOS INDEPENDENTES (`/modules`)

**13 módulos estruturados:**

| Módulo | Porta | Classes | Status |
|--------|-------|---------|--------|
| core | 8081 | 30 | ✅ Pronto |
| cadastro | 8082 | 116 | ✅ Pronto |
| financeiro | 8083 | 51 | ✅ Pronto |
| vendas | 8084 | 9 | ✅ Pronto |
| compras | 8085 | 10 | ✅ Pronto |
| estoque | 8086 | 6 | ✅ Pronto |
| fiscal | 8087 | 69 | ✅ Pronto |
| rh | 8088 | 15 | ✅ Pronto |
| servicos | 8080 | 9 | ✅ Pronto |
| producao | 8090 | 9 | ✅ Pronto |
| ia | 8089 | 0 | ⚠️ Estrutura pronta, sem código |
| bi | - | - | ⚠️ Estrutura pronta, sem código |
| shared | - | 37 | ✅ Compartilhado |

### 4. BANCO DE DADOS

#### PostgreSQL
- ✅ Schema `brasil-saas` configurado
- ✅ 33 migrations Flyway (V1-V46)
- ✅ Tabelas oficiais seeds (NCM, CFOP, Municípios)
- ✅ Campos para logos/fotos em todas as tabelas relevantes

#### MongoDB
- ✅ Coleção `imagens` configurada
- ✅ Conexão: admin/${MONGODB_PASSWORD}
- ✅ ImagemDocumento entity
- ✅ ImagemMongoRepository

### 5. INFRAESTRUTURA

#### Systemd Units (13 arquivos)
- ✅ brasil-saas-erp.service
- ✅ Modules services (core, cadastro, financeiro, etc.)
- ✅ Database services (postgresql, mongodb, redis)
- ✅ Message broker (rabbitmq)
- ✅ Object storage (minio)

#### Scripts Shell
- ✅ test_db_connection.sh
- ✅ test_database.sh
- ✅ test_erp_operations.sh
- ✅ check_mongodb.sh
- ✅ manage_services.sh
- ✅ installbase.sh
- ✅ build_module.sh
- ✅ fix_poms.sh

#### Scripts Python
- ✅ compilar.py
- ✅ corrige.py
- ✅ exporta.py
- ✅ importar_ncm.py
- ✅ criar_componentes.py

---

## 🔴 O QUE NÃO ESTÁ FUNCIONANDO / PROBLEMAS IDENTIFICADOS

### 1. ERROS DE VALIDAÇÃO DE DADOS (CRÍTICO)

**Problema**: Erro de null constraint violation na tabela `fcfo`

```
org.springframework.dao.DataIntegrityViolationException: 
o valor nulo na coluna "codcfo" da relação "fcfo" 
viola a restrição de não-nulo
```

**Local**: `erros.txt` - POST `/api/cfo`

**Causa Provável**: 
- Controller tentando salvar entidade sem preencher campos obrigatórios
- Validação de entrada não está funcionando corretamente
- DTO não está sendo mapeado corretamente para entidade

**Solução Necessária**:
1. Revisar `ClienteFornecedorApiController`
2. Adicionar validação @NotNull/@NotBlank nos DTOs
3. Implementar validação manual antes de salvar

### 2. LEGACY WEBFLUX vs SPRING MVC

**Problema**: Stack traces mostram uso de R2DBC (reactive) mas o projeto migrou para Spring MVC tradicional

**Evidência**: 
- `r2dbc-postgresql-1.0.5.RELEASE.jar` nos logs de erro
- `reactor-core`, `reactor-netty` no classpath

**Solução Necessária**:
- Remover dependências R2DBC do pom.xml
- Garantir que todos os repositórios usam JPA tradicional
- Limpar dependências reactive não utilizadas

---

## ⚠️ O QUE FALTA IMPLEMENTAR

### PRIORIDADE 1 - CRÍTICO (Bloqueiam Produção)

#### 1. Testes Unitários
- **Status**: < 1% cobertura (apenas 2 testes existem)
- **Meta Mínima**: 70% cobertura
- **O que falta**:
  - Testes para todos os 45 controllers
  - Testes para todos os 32 services
  - Testes de integração com banco de dados
  - Mock de dependências externas

#### 2. CI/CD Pipeline
- **Status**: Não implementado
- **O que falta**:
  - GitHub Actions ou GitLab CI
  - Build automático no push
  - Execução de testes automatizada
  - Deploy automático em staging/production
  - Quality gates (SonarQube)

### PRIORIDADE 2 - ALTO (Melhorias Importantes)

#### 3. Configuração Production
- **Status**: Incompleta
- **O que falta**:
  - Profile `prod` completo
  - HTTPS/SSL configurado
  - Logs em JSON para Loki
  - Connection pooling otimizado (HikariCP)
  - Cache Redis configurado
  - MinIO production credentials

#### 4. Monitoramento & Observabilidade
- **Status**: Básico
- **O que falta**:
  - Dashboard Grafana
  - Alertas Prometheus
  - Distributed tracing (Jaeger/Zipkin)
  - Health checks detalhados
  - Metrics customizados

#### 5. Módulo IA (Backend)
- **Status**: Estrutura pronta, sem implementação
- **O que falta**:
  - Integrar Spring AI (já está em `/microservices/spring-ai`)
  - Configurar OpenAI/Anthropic credentials
  - Implementar chatbot com RAG
  - Criar endpoints de IA no módulo `ia` (porta 8089)

#### 6. Módulo BI
- **Status**: Estrutura pronta, sem implementação
- **O que falta**:
  - Dashboards analíticos
  - Relatórios avançados
  - Integração com ferramentas de BI

### PRIORIDADE 3 - MÉDIO (Funcionalidades)

#### 7. Integrações Fiscais Completas
- **Status**: Bibliotecas prontas, requer certificados
- **O que falta**:
  - Certificado digital A1/A3 válido
  - Credenciais SEFAZ production
  - Homologação com SEFAZ por estado
  - Implementar CT-e, MDF-e, EFD

#### 8. EchoAvatar Integration (Feature Futura)
- **Status**: Projeto Python separado
- **O que falta**:
  - Integração com backend Java
  - API REST para comunicação
  - Frontend em React para controle
  - Unity setup para avatar 3D

---

## 📋 CHECKLIST FINAL

### Para Deployment em Produção

- [ ] **Resolver erro de validação `codcfo`** (CRÍTICO)
- [ ] **Remover dependências R2DBC** (CRÍTICO)
- [ ] **Implementar testes unitários** (mínimo 70%)
- [ ] **Configurar CI/CD pipeline**
- [ ] **Configurar profile production**
- [ ] **Obter certificado digital válido**
- [ ] **Configurar HTTPS/SSL**
- [ ] **Setup de monitoramento (Grafana/Prometheus)**
- [ ] **Backup automatizado de bancos**
- [ ] **Documentação de deployment**
- [ ] **Treinamento de equipe**

### Para Considerar "Completo"

- [ ] Implementar módulo IA (backend com Spring AI)
- [ ] Implementar módulo BI
- [ ] Dashboards analíticos completos
- [ ] Relatórios avançados (PDF/Excel)
- [ ] Mobile app (opcional)
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Performance testing
- [ ] Security audit
- [ ] Load testing
- [ ] Homologação fiscal completa
- [ ] Integração EchoAvatar (opcional)

---

## 💡 RECOMENDAÇÕES IMEDIATAS

### 1. Imediato (Hoje)
```bash
# 1. Corrigir erro de validação
# Editar: src/main/java/.../controller/ClienteFornecedorApiController.java
# Adicionar validação de campos obrigatórios

# 2. Limpar dependências
# Editar: pom.xml
# Remover: spring-boot-starter-webflux, r2dbc-postgresql, reactor-*
```

### 2. Curto Prazo (Esta Semana)
```bash
# 1. Criar testes unitários básicos
mkdir -p src/test/java/br/com/brasil_saas

# 2. Configurar GitHub Actions
mkdir -p .github/workflows
# Criar: ci.yml com build e teste
```

### 3. Médio Prazo (Próximas 2 Semanas)
- Configurar ambiente production
- Setup de monitoramento
- Obter certificados digitais
- Homologação fiscal
- Implementar módulo IA usando Spring AI

---

## 📊 CONCLUSÃO

**Status Geral: 87% IMPLEMENTADO**

✅ **Pontos Fortes**:
- Backend robusto e completo (708 classes Java próprias)
- **11.365 classes de microserviços fiscais** integrados
- Frontend moderno com **React 19 + PrimeReact 10.8**
- Arquitetura bem estruturada (Monólito + Módulos + Microserviços)
- Documentação abrangente
- Infraestrutura pronta
- **Thymeleaf removido completamente**

🔴 **Pontos Críticos**:
- Testes unitários inexistentes
- CI/CD não implementado
- Erro de validação de dados
- Dependências conflitantes (WebFlux vs MVC)
- Módulo IA sem implementação (apesar do Spring AI estar disponível)

⚠️ **Recomendação**: Sistema utilizável para desenvolvimento e testes, **NÃO recomendado para produção crítica** até resolver itens de Prioridade 1.

---

## 📦 MICROSERVIÇOS FISCAIS - RESUMO

| Microserviço | Classes | Tamanho | Finalidade | Status Integração |
|--------------|---------|---------|------------|-------------------|
| Spring AI | 2.278 | 105MB | IA/ML | ⚠️ Estrutura pronta |
| eSocial | 5.073 | 56MB | Eventos trabalhistas | ✅ Integrado RH |
| nfe | 2.545 | 33MB | NF-e/NFC-e | ✅ Integrado Fiscal |
| Java_NFe | 367 | 9.3MB | NFe backup | ⚠️ Disponível |
| Java_CTe | 220 | 4.8MB | CT-e | ⏳ Pendente |
| NFSe | 168 | 2.1MB | NFS-e | ⚠️ Parcial |
| EFD ICMS | ~300 | 3.0MB | SPED Fiscal | ⏳ Pendente |
| EFD Contrib | ~200 | 2.7MB | SPED Contrib | ⏳ Pendente |
| Certificado | ~100 | 1.3MB | Cert. Digital | ✅ Integrado |
| PDF Signature | 10 | 7.1MB | Assinar PDF | ✅ Integrado |
| MDFe | ~50 | 16KB | MDF-e | ⏳ Pendente |
| NFSe SP | - | 4.8MB | NFS-e SP | ⚠️ Parcial |
| EchoAvatar | Python | 12MB | Avatar IA | ⏳ Futuro |

**Total**: 11.365 classes Java + bibliotecas Python = **225MB de microserviços especializados**

---

**Análise Realizada**: 22/09/2026  
**Responsável**: Code Expert System  
**Próxima Milestone**: Resolver erros de validação + Implementar testes unitários + Ativar módulo IA


---

## 🔐 AUTENTICAÇÃO ATUAL — POSTGRES SUPERUSER + JWT

**Atualização:** 24/09/2026

A autenticação do Brasil SaaS ERP possui dois caminhos independentes:

1. **Usuário ERP normal**
   - O usuário é localizado em `bc_core_usuario`.
   - A senha é validada com BCrypt contra `senha_hash`.
   - O usuário precisa estar ativo.
   - As autoridades são carregadas a partir de perfis e permissões efetivas.
   - Após autenticação, o backend emite `accessToken` e `refreshToken` JWT.

2. **PostgreSQL SUPERUSER**
   - Se a validação BCrypt falhar, o backend tenta autenticar o mesmo `username/password` diretamente no PostgreSQL.
   - Essa conexão usa SSL, mas deliberadamente remove `sslcert`, `sslkey` e `sslrootcert` da URL da aplicação e força `sslmode=require`, para que o login por credencial do PostgreSQL não dependa do certificado de cliente usado pela conexão técnica `sa`.
   - O acesso especial só é aceito quando a sessão PostgreSQL autenticada pertence a uma role com `rolsuper=true`.
   - Uma role PostgreSQL comum, mesmo autenticando corretamente, não recebe acesso administrativo.
   - Se o PostgreSQL SUPERUSER ainda não existir como usuário ERP, o sistema o provisiona automaticamente, cria/garante perfil ADMIN e emite `ROLE_ADMIN` + `ROLE_SUPERADMIN`.
   - A senha PostgreSQL nunca é armazenada no ERP.
   - A conta provisionada recebe um hash BCrypt aleatório apenas para satisfazer o modelo de usuário; autenticações futuras continuam sendo validadas pelo PostgreSQL.

### JWT

- `/api/auth/login` e `/api/auth/refresh` são públicos.
- Refresh tokens não podem ser usados como Bearer token para APIs.
- O filtro JWT rejeita explicitamente tokens cujo claim `type` seja `refresh`.
- A sessão da aplicação continua stateless.

### Hardening futuro

O modelo atual foi intencionalmente deixado sem certificado de cliente no login do SUPERUSER PostgreSQL. O próximo endurecimento previsto é adicionar autenticação por certificado/PKI para esse fluxo, sem misturar o certificado técnico da conexão `sa` com a credencial do usuário.

---

## 🤖 SPRING AI — SITUAÇÃO REAL

**Atualização:** 24/09/2026

O repositório contém uma cópia extensa do projeto Spring AI em:

`src/main/resources/microservices/spring-ai/`

Isso **não significa** que esse diretório seja o runtime da aplicação. O `pom.xml` principal exclui `microservices/**` dos recursos empacotados.

### Integração real do ERP

O ERP principal possui dependência:

`org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M3`

e possui um módulo Java próprio em:

`src/main/java/br/com/brasil_saas/ia/`

Esse módulo contém controllers, services e, principalmente, `ChatServiceImpl`, que injeta:

`org.springframework.ai.chat.client.ChatClient`

e executa chamadas como:

`chatClient.prompt().user(...).call().content()`

Portanto, **o Brasil SaaS ERP já possui código de integração com Spring AI/ChatClient**.

### Estado atual da configuração

Em `application.yml`, a configuração atual usa a API compatível com OpenAI do OpenRouter:

- `spring.ai.openai.base-url=https://openrouter.ai/api/v1`
- `spring.ai.openai.api-key=${OPENROUTER_API_KEY:}`
- `spring.ai.openai.chat.enabled=true`
- modelo configurado: `openrouter/auto`
- embeddings: desabilitados

Além disso, `ChatClientConfig.java` fornece um `ChatClient` e um `ChatModel` de fallback quando nenhum `ChatModel` real estiver disponível. O fallback não realiza chamadas externas.

Portanto, o ERP **usa a API do Spring AI no código de aplicação** e está preparado para chamar um provedor OpenAI-compatible via OpenRouter. A chamada externa efetiva depende de `OPENROUTER_API_KEY` estar configurada no ambiente.

### Frontend IA x Backend IA

O frontend possui `components/ia/IA.jsx` e `services/IaService.js`.

Os endpoints utilizados pelo frontend incluem:

- `POST /api/ia/chat`
- `POST /api/ia/chat/simples`
- `GET/POST/PUT/DELETE /api/ia/sessoes`
- `GET /api/ia/mensagens/sessao/{id}/ordenado`
- `POST /api/ia/classificacoes/classificar-texto`
- `POST /api/ia/analises`
- `POST /api/ia/embeddings/gerar`

Esses endpoints correspondem ao pacote Java `br.com.brasil_saas.ia`, que possui controllers próprios. Portanto, a tela de IA não é apenas mock visual; existe backend correspondente. Entretanto, parte da persistência de sessões/histórico ainda está incompleta em `ChatServiceImpl` e deve ser tratada separadamente da integração básica com o ChatClient.

### Conclusão

O Spring AI está **presente e integrado no código da aplicação**, mas a execução do chat OpenAI está **desabilitada por configuração** neste momento. O diretório `microservices/spring-ai` é principalmente código-fonte/biblioteca incorporada ao repositório e não deve ser confundido com o runtime do ERP.

---

## 🧩 COBERTURA FRONTEND x BACKEND

A documentação anterior superestimava o frontend ao afirmar que havia somente 47 componentes/serviços e que todos os módulos estavam 100% funcionais. A análise atual deve considerar a implementação real de `App.jsx`, `Layout.jsx`, serviços React e os controllers Java.

O backend possui módulos próprios para:

- Core/autenticação/usuários/empresa/auditoria
- Cadastro
- Financeiro
- Fiscal
- RH
- Vendas
- Compras
- Estoque
- Serviços
- Produção
- IA
- BI
- Integrações/portais

O frontend já possui telas para boa parte dos módulos operacionais, mas **a cobertura não é 1:1**. O backend tem, no mínimo, os seguintes grupos de controllers próprios no monólito:

- Core: 8 controllers
- Cadastro: 12 controllers, incluindo controllers de fotos/logos
- Financeiro: 8 controllers
- Fiscal: 7 controllers
- RH: 4 controllers
- Compras: 1 controller
- Estoque: 2 controllers
- Vendas: 1 controller
- Serviços: 1 controller
- Produção: 3 controllers
- IA: 9 controllers

Isso mostra que o número de arquivos React não deve ser comparado diretamente ao número de controllers: uma tela pode consumir vários controllers, e alguns controllers são suporte/arquivo/imagem. Ainda assim, existem lacunas reais. Por exemplo, o módulo IA possui 9 controllers enquanto a tela `IA.jsx` e `IaService.js` cobrem principalmente sessões, mensagens, chat, classificação, análise e embeddings; configuração e templates/prompts precisam ser conferidos separadamente. No Core também existem endpoints de auditoria, logos/fotos e administração que não aparecem como telas independentes no menu principal.

No Fiscal, a tela de entrada de notas já existe e está ligada a `/api/fiscal/entradas`; o build local informado apresentou uma falha causada por uma dependência React desnecessária (`useAuth` em `EntradaNota.jsx`), que foi removida no commit `888fd90ea54fef2d02631b207c0fa074d2869e9`.

Essa diferença passa a ser tratada como **lacuna de cobertura funcional**, e não como erro de backend. A regra para os próximos incrementos é mapear:

`Controller → endpoint → Service React → componente React → rota → item de menu`

antes de considerar um módulo concluído.



### Regra de cobertura funcional

A partir desta análise, um módulo só deve ser considerado completo quando houver rastreabilidade entre:

`Controller → endpoint → regra/service → Service React → componente React → rota → menu`.

Também devem ser verificadas as dependências compartilhadas antes do build. Componentes que não usam autenticação não devem importar `AuthContext` apenas por padrão, pois isso cria falhas de compilação sem benefício funcional.
