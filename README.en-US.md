# 🚀 Brasil SaaS ERP — Business Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE.md)

**Multitenant Enterprise ERP with Assistive AI, Full Fiscal Coverage and Modular Architecture**

Created by: **Euripedes Batista de Paiva Junior**

Criado por: **Euripedes Batista de Paiva Junior**

---

## 📖 About the Project

**Brasil SaaS ERP** is a complete business management system, built to serve companies of every size with a robust, secure and scalable solution. Migrated from Delphi to **Java Spring Boot 3.3.5** on **Oracle JDK 21**, the system provides financial, fiscal, inventory, services and sales modules with high throughput on large data volumes.

### ✨ Enterprise Highlights

| Feature | Description |
|---------|-----------|
| 🏎️ **High Performance** | Migration from WebFlux to traditional Spring MVC, fixing slowness on tables over 400 MB |
| 📊 **Smart Pagination** | On-demand record loading, ideal for very large databases |
| 🎨 **Modern Frontend** | Reactive interface with PrimeReact 10.8 and React 19 |
| 📝 **Professional Reports** | PDF generation with modern layout using OpenPDF 3.0.5 |
| 🌐 **RESTful API** | Solid backend with paginated endpoints and advanced filters |
| 🔒 **Multi-Tenant Security** | JWT authentication, permission-based authorization and multi-company isolation |
| 📦 **Hybrid Storage** | PostgreSQL (relational data) + MongoDB (binaries/images) + MinIO (large files) |
| 🤖 **Assistive AI** | Spring AI + OpenAI integrated for intelligent assistance |
| 📡 **Observability** | Actuator + Micrometer + Logback with MDC for end-to-end tracing |
| 🚛 **Transport Documents** | NFS-e SP, MDF-e and CT-e wired to SEFAZ; SPED EFD ICMS/IPI file generation |
| 🧾 **Single response contract** | One response shape for the ERP to read, adjusted on the issuer side. The refusal reason is persisted and survives the screen closing |

---

## 🛠️ Complete Technology Stack

### Backend (Núcleo)

| Camada | Tecnologia | Versão | Finalidade |
|--------|------------|--------|------------|
| **Linguagem** | Oracle JDK | 21 (LTS) | Base de longo suporte com records, pattern matching |
| **Framework** | Spring Boot | 3.3.5 | Autoconfiguração, Tomcat embutido, Actuator |
| **Web** | Spring MVC + Jackson | - | Camada REST (/api/**), JSON consistente |
| **Segurança** | Spring Security 6 + JWT | - | Autenticação stateless, @PreAuthorize |
| **ORM** | Spring Data JPA + Hibernate | 6.5+ | Persistência com ddl-auto: validate |
| **Migrations** | Flyway | Latest | Migrations versionadas (V109…V126) |
| **Boilerplate** | Lombok + MapStruct | - | Menos código, mapeamento DTO↔entidade |
| **Validação** | Bean Validation (Jakarta) | - | Validação de entrada na borda da API |
| **Documentação** | springdoc-openapi | Latest | Swagger UI em /swagger-ui.html |

### Dados & Infraestrutura

| Componente | Tecnologia | Versão | Finalidade |
|------------|------------|--------|------------|
| **Banco Relacional** | PostgreSQL | 18 | Núcleo ACID com schema `brasil_saas` |
| **Encryption** | mTLS (PKI) | - | ca.crt/sa.crt/sa.pk8 for a secure connection |
| **Documentos/Imagens** | MongoDB | Latest | Coleção `imagens` para binários/logos/fotos |
| **Cache** | Redis | Latest | Cache/sessões para tabelas quentes (NCM, municípios) |
| **Mensageria** | RabbitMQ | Latest | Eventos assíncronos (pedido → estoque/financeiro) |
| **Object Storage** | MinIO | Latest | Storage de objetos/arquivos grandes (hom/prod) |

### Fiscal (Diferencial Competitivo)

| Module | Tecnologia | Status |
|--------|------------|--------|
| **NFS-e São Paulo** | proxy com failover + API Java + bridge Ruby | ✅ Emitindo em produção, com troca automática de implementação |
| **SPED EFD ICMS/IPI** | java-efd-icms 3.21.1 | ✅ Gera arquivo válido, contadores conferidos |
| **MDF-e** | fincatto documentofiscal 5.1.2 | ⚠️ Conecta e assina (`cStat 107`); emissão não implementada |
| **CT-e** | fincatto documentofiscal 5.1.2 | ⚠️ Conecta e assina (`cStat 107`); emissão não implementada |
| **NF-e/NFC-e** | java-nfe (swconsultoria) | ❌ `NFeServiceImpl` tem 89 linhas e 3 TODOs — não emite |
| **Consulta/DistDFe** | wmixvideo nfe | ✅ Consulta, manifestação do destinatário |
| **SPED EFD Contribuições** | java-efd-contribuicoes 1.32.1 | ⚠️ Biblioteca no `pom.xml`, sem endpoint |
| **Validação XML** | JAXB/XSD | ✅ Schemas SEFAZ validados |
| **Certificado Digital** | A1 ICP-Brasil | ✅ O e-CNPJ A1 do ERP assina NFS-e, MDF-e e CT-e |

**Sobre o MDF-e e o CT-e:** ambos consultam a SEFAZ em homologação e recebem
`cStat 107` ("Serviço em Operação"). This proves the TLS chain, the certificate of the
cliente e o envelope SOAP. **Não prova o XML do documento** — nenhum MDF-e nem
CT-e foi emitido ainda, e a montagem do MDF-e não está implementada.

Detalhe em
[`docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`](docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md).

**A deadline that cannot be ignored:** NT 2026.001 makes the `infCIOT`
group required on road MDF-e as of **23/11/2026** (rejection `cStat 684`).
`infCIOT` is already in the XSD; the validation in the issuer is missing.

#### NFS-e São Paulo: proxy with failover between two implementations

The São Paulo NFS-e is not served by a single implementation. The ERP always talks to
**4567**, and a proxy decides who answers:

```
ERP  ──►  4567  nfse-failover.rb  (proxy, periodic health check)
              ├──►  4568  nfse-sp-api      Java   PRIMARY
              └──►  4569  nfse-sp-bridge   Ruby   FALLBACK
                        (São Paulo city hall, via A1)
```

| Piece | Role |
|---|---|
| **4567 — `nfse-failover.rb`** | Proxy. Tries the Java side, falls back to the Ruby one, health check every 10s |
| **4568 — `nfse-sp-api`** | **Primary** implementation. Validates the XML against the schema **before** sending |
| **4569 — `nfse-sp-bridge`** | **Fallback** implementation, in Sinatra. Takes over when the Java one fails |

The ERP finds out who answered from the `X-Backend` header the proxy returns.

**Why two implementations and not one:** the Ruby side was written first and is the one
that validated the rules against the São Paulo city hall for real — it issued and
cancelled an NFS-e in production, with the city hall's literal messages recorded in
[`src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md`](src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md).
The Java side reimplemented the same contract with the same scope, and comparing the XMLs
between the two is what caught the signature and namespace bugs on the Java side. The Ruby
side stays up **on purpose**: it is the oracle the Java side is checked against.

The order is not decorative. The primary is the one that validates against the schema; if
it were inverted, a broken implementation would issue against the wrong contract and the
ERP would only find out after the city hall rejected it.

**What the proxy switches, and what it does not.** The proxy switches upstream when the
connection fails. It does **not** switch when the implementation answers `200` with the
wrong contract — and that is the case that matters: on 26/09/2026, with the Java API down,
the Ruby side issued note 29 with `success=true`, the city hall accepted it, and the ERP
answered with an error because it read `"sucesso"` where the Ruby side wrote `success`.
That is why the watchdog exists: it watches the response, not just the port.

A 4567 é o default do código, não um número escolhido:

```java
// NfseEmissaoService.java
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}")
```

The A1 certificate is the company's e-CNPJ, and it is **stored in MongoDB** — the collection
`documentos` holds a `tipoEntidade: "certificado_digital"` document with the `.pfx`
íntegro. É por isso que o A1 chega junto na restauração do dump. O `.pfx` foi
emitido com **RC2-40-CBC**, que o OpenSSL 3 tirou do provider padrão: o Java
aceita, e o Ruby precisa do provider legacy (`OPENSSL_MODULES`). A reemissão
com AES resolveria de vez.

### Observability & Operação

| Ferramenta | Finalidade |
|------------|------------|
| **Actuator + Micrometer** | /actuator/health\|metrics para monitoramento real |
| **Logback + MDC** | Logs com traceId, empresaId, usuarioId; prod em JSON p/ Loki |
| **Scripts Shell** | test_db_connection.sh, test_database.sh, test_erp_operations.sh |

### Frontend React ✅ COMPLETO

**LOCALIZAÇÃO**: `src/main/resources/static/react/`

| Componente | Quantidade | Status | Description |
|------------|------------|--------|-----------|
| **Componentes React** | 25 arquivos JSX | ✅ Completo | Componentes por módulo |
| **Serviços API** | 11 arquivos JS | ✅ Completo | Integração REST |
| **Contextos** | 1 (Auth) | ✅ Completo | Gerenciamento de sessão |
| **Build Produzido** | ~2.2MB | ✅ Otimizado | Em `static/dist/` |

**Tecnologias**: React 19, PrimeReact 10.8, React Router 7, Axios, Vite 5

**Funcionalidades**:
- ✅ Autenticação JWT com refresh automático
- ✅ 132 components implemented (41,338 lines)
- ✅ Módulo Produção completo (ordens de produção)
- ✅ Design responsivo com PrimeReact
- ✅ Integração total com backend Spring Boot
- ⚠️ 4 placeholder components (Purchases, Inventory, Sales, Services)

📄 **Ver análise completa**: [docs/frontend/FRONTEND_ANALISE_COMPLETA.md](docs/frontend/FRONTEND_ANALISE_COMPLETA.md)

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| **React** | 19 | SPA administrativa |
| **Vite** | 5.4+ | Build system rápido |
| **PrimeReact** | 10.8 | Componentes UI profissionais |
| **Axios** | Latest | Cliente HTTP para API |

### Build & VCS

| Ferramenta | Finalidade |
|------------|------------|
| **Maven** | Build e gerenciamento de dependências |
| **Git** | Versionamento de código |

---

## 📦 System Modules

The system has **22 business modules** on top of a common core
(`core`), all with controller, service, repository and screen.
Numbers verified on **03/10/2026** in the code:

| | |
|---|---|
| Java classes | **762** |
| Spring Data repositories | **174** |
| Services | **147** |
| REST endpoints | **451** |
| Tables in PostgreSQL (schema `brasil_saas`) | **204** |
| React components | **123** |
| Flyway migrations in the repo | **15** (`V109`…`V126`) |

### 💰 Financeiro

> **Status**: ✅ implemented and answering (`/api/financeiro/*` → 200)

| Feature | Description |
|----------------|-----------|
| ✅ **Payable/Receivable** | Bills, installments, multi-level approval flow, due-date alerts |
| ✅ **Settlements** | With discounts, interest, fines and reversals |
| ✅ **Treasury** | Cash, cash-flow projection, loans and financial investments |
| ✅ **General Accounting** | Journal entries and postings, chart of accounts, cost centers |
| ✅ **Banks** | Bank accounts, statement, bank reconciliation, remittances and returns (CNAB/OFX) |
| ✅ **Budgets** | Budgeted vs actual |
| ✅ **Commissions** | Commission rules on sales and service orders |
| ✅ **Renegotiation** | Bill renegotiation |
| ✅ **PDD Provision** | Provision for doubtful debtors |
| ✅ **Profitability Analysis** | By cost center, chart of accounts and period |

**Endpoints**: `/api/financeiro/titulos`, `/lancamentos`, `/orcamentos`,
`/emprestimos`, `/planos-contas`, `/centros-custo`, `/contas-bancarias`,
`/caixas`, `/condicoes-pagamento`, `/tipos-pagamento`, `/conciliacao`,
`/extrato`, `/comissoes`, `/renegociacao`, `/remessas`, `/retornos` …
(55 endpoints in the module)

---

### 🏙️ Cadastros

| Registry | Features |
|----------|-----------------|
| ✅ **Municipalities** | Official IBGE table, search by code or name, ZIP code |
| ✅ **People** | Single PF/PJ registry, contacts, addresses |
| ✅ **Customers / Suppliers** | Credit limit, logo, rating, history |
| ✅ **Products** | Variations, kits, NCM, images (MongoDB), e-commerce |
| ✅ **Services** | For NFS-e, integration with service orders |
| ✅ **Categories / Brands / Units of Measure / Carriers** | Support for the product registry and freight |

**Endpoints**: `/api/cadastro/produtos`, `/clientes`, `/fornecedores`,
`/pessoas`, `/servicos`, `/categorias`, `/marcas`, `/unidades-medida`,
`/transportadoras` + `/api/municipios` (31 endpoints in the module)

---

### 📋 Services and Sales

| Feature | Description |
|----------------|-----------|
| ✅ **Service Order** | Issuance, items, time tracking, PDF, integrated NFS-e issuance |
| ✅ **Sales Orders** | Integration with inventory, finance and fiscal |
| ✅ **POS** | Point-of-sale screen |
| ✅ **Price Tables** | Items per product |
| ✅ **Bonuses and Returns** | With items |
| ✅ **Sales Contracts** | With items and commission rules |

**Endpoints**: `/api/servicos/os`, `/api/vendas/pedidos`,
`/api/vendas/tabelas-preco` … (15 endpoints across the two modules)

---

### 🛒 Purchases and Supply Chain

| Feature | Description |
|----------------|-----------|
| ✅ **Purchase Orders** | With items, supplier integration |
| ✅ **Receipts** | Item inspection |
| ✅ **Invoice Matching** | Invoice validation against what was received |
| ✅ **Supply Chain** | Purchase requests and quotations with a supplier map |
| ✅ **Purchase Contracts** | With items |

**Endpoints**: `/api/compras/pedidos`, `/recebimentos`,
`/supply-chain/solicitacoes`, `/supply-chain/cotacoes` … (12 endpoints)

---

### 📦 Inventory

| Feature | Description |
|----------------|-----------|
| ✅ **Warehouses and Locations** | Occupancy by location |
| ✅ **Balances, Batches and Serials** | Traceability |
| ✅ **Movements** | Entries, exits, adjustments |
| ✅ **Reservations and Transfers** | Between warehouses |
| ✅ **Inventories** | With items |
| ✅ **Shipments** | With items |

**Endpoints**: `/api/estoque/depositos`, `/saldos`, `/movimentacoes`,
`/lotes`, `/reservas`, `/transferencias`, `/inventarios`, `/expedicoes` …
(17 endpoints)

---

### 🏭 Industrial Production (PCP)

| Feature | Description |
|----------------|-----------|
| ✅ **Production Orders** | Full flow: inputs → process → finished product |
| ✅ **Bill of Materials (BOM)** | Per parent product |
| ✅ **Routings and Operations** | Sequence of operations |
| ✅ **Work Centers and Capacity** | Scheduling by capacity |
| ✅ **Time Tracking** | By production, employee, period and status; statistics |
| ✅ **Manifests** | With items |
| ✅ **MRP** | Requirements planning |

**Endpoints**: `/api/producao/estruturas`, `/roteiros`, `/centros-trabalho`,
`/capacidade`, `/apontamentos`, `/romaneios`, `/mrp` … (30 endpoints)

---

### 📊 Accounting

| Feature | Description |
|----------------|-----------|
| ✅ **Journal Entries and Postings** | Double-entry bookkeeping |
| ✅ **Trial Balance, Balance Sheet, P&L, Ledger** | Accounting reports |
| ✅ **Closings** | Period closing |

**Endpoints**: `/api/contabilidade/lancamentos`, `/balancete`, `/balanco`,
`/dre`, `/razao`, `/fechamentos` (15 endpoints)

---

### 🏢 Assets (Asset Management)

| Feature | Description |
|----------------|-----------|
| ✅ **Fixed Assets** | Asset registry |
| ✅ **Maintenance** | Asset maintenance history |

**Endpoints**: `/api/ativos/manutencoes` (4 endpoints)

---

### 📁 DMS (Document Management)

| Feature | Description |
|----------------|-----------|
| ✅ **Documents** | With content, versions and approvals |
| ✅ **Retention** | Retention policy |

**Endpoints**: `/api/dms/documentos`, `/retencao`, `/versoes/{id}/download` (9 endpoints)

---

### ✅ Quality

| Feature | Description |
|----------------|-----------|
| ✅ **Inspection Plans** | Plans per product/process |
| ✅ **Inspections** | Inspection record |
| ✅ **Non-conformities** | NC handling |

**Endpoints**: `/api/qualidade/planos`, `/inspecoes`, `/nao-conformidades` (7 endpoints)

---

### 📈 Projects

| Feature | Description |
|----------------|-----------|
| ✅ **Projects** | Summary, stages, movements |
| ✅ **Billing** | Billing per project |
| ✅ **Risks and Changes** | Risk and change record |

**Endpoints**: `/api/projetos`, `/projetos/{id}/resumo`, `/etapas`,
`/movimentos`, `/faturamentos`, `/riscos`, `/mudancas` (14 endpoints)

---

### 🏗️ WMS (Warehouse)

| Feature | Description |
|----------------|-----------|
| ✅ **Picking Waves** | With items |
| ✅ **Volumes** | With items |
| ✅ **Putaway** | Load placement |

**Endpoints**: `/api/wms/ondas`, `/volumes`, `/putaway` (14 endpoints)

---

### 🔄 Workflow

| Feature | Description |
|----------------|-----------|
| ✅ **Definitions** | With stages |
| ✅ **Instances and Tasks** | Pending tasks per user |

**Endpoints**: `/api/workflow/definitions`, `/instances`, `/tasks/pendentes` (10 endpoints)

---

### 🌐 Portals

| Feature | Description |
|----------------|-----------|
| ✅ **Accesses** | Portal access control |
| ✅ **Public Validation** | Validation and "my account" without ERP login |

**Endpoints**: `/api/portais/acessos`, `/publico/validar`, `/publico/minha-conta` (5 endpoints)

---

### 👥 HR

| Feature | Description |
|----------------|-----------|
| ✅ **Employees** | Link to person, position, photo |
| ✅ **Positions** | Position structure |
| ✅ **Payroll** | With items |

**Endpoints**: `/api/rh/funcionarios`, `/cargos`, `/folhas` (14 endpoints)

---

### 🤝 CRM

| Feature | Description |
|----------------|-----------|
| ✅ **Leads and Opportunities** | Sales pipeline |
| ✅ **Activities and Tasks** | Follow-up |
| ✅ **Forecast** | Sales forecast |

**Endpoints**: `/api/crm/leads`, `/pipeline`, `/forecast`, `/atividades` (10 endpoints)

---

### 📊 BI (Business Intelligence)

| Feature | Description |
|----------------|-----------|
| ✅ **Dashboards** | Public, by type and by user, with widgets |
| ✅ **KPIs and Indicators** | KPI calculation per type |
| ✅ **Reports** | By category, export **PDF, Excel and CSV** |
| ✅ **Scheduled Reports** | By frequency, pending queue |
| ✅ **Parameterized Reports** | Parameterized reports |

**Endpoints**: `/api/bi/dashboards`, `/kpis`, `/indicadores`, `/relatorios`,
`/relatorios-agendados`, `/reports` … (46 endpoints — the largest module)

---

### 🤖 Assistive AI

| Resource | Status |
|---------|--------|
| ✅ **AI Chat** | Persisted sessions and messages |
| ✅ **ERP Assistant** | With usage audit |
| ✅ **Embeddings** | Semantic search per entity |
| ✅ **Classifications and Predictive Analytics** | Trainable models |
| ✅ **Prompts and Templates** | Reusable library |
| ⚠️ **Provider** | Configurable (`app.ia.provider`, default `openai`); needs a valid key |

**Endpoints**: `/api/ia/config`, `/sessoes`, `/mensagens`, `/prompts`,
`/prompt-templates`, `/embeddings`, `/classificacoes`, `/analises` …
(74 endpoints)

---

### 🔐 Core, Auth and Superadmin

| Feature | Description |
|----------------|-----------|
| ✅ **Authentication** | JWT, database login, AD login (SPNEGO), refresh token |
| ✅ **Users, Profiles and Permissions** | Authorization by permission (`@PreAuthorize`) |
| ✅ **Multi-company** | User ↔ company(ies), isolation per company |
| ✅ **Superadmin** | SQL catalog, modules per user, available profiles |
| ✅ **Audit** | Access log, notifications, sessions |

**Endpoints**: `/api/auth/login`, `/login/database`, `/login/ad`, `/refresh`,
`/me` + `/api/core/perfil`, `/core/minha-empresa` + `/api/superadmin/usuarios`,
`/superadmin/sql/catalogo` … (33 endpoints)

---

### 🖨️ Reports

| Feature | Description |
|----------------|-----------|
| ✅ **Reports by Type** | HTML and PDF (`/api/relatorios/{tipo}`, `/api/relatorios/pdf/{tipo}`) |

---

### 📄 Documents (shared)

| Feature | Description |
|----------------|-----------|
| ✅ **Document Content** | `/api/documentos/{id}/conteudo` — files stored in MongoDB |

---

## 🗂️ Project Structure

```
BRASIL-SAAS-ERP/
├── src/main/java/br/com/brasil_saas/     # Monólito principal (762 classes Java)
│   ├── core/         # Autenticação, usuários, empresas, perfis, permissões
│   ├── cadastro/     # Pessoas, produtos, clientes, fornecedores, marcas, categorias
│   ├── financeiro/   # Títulos, lançamentos, conciliação, plano de contas
│   ├── vendas/       # Pedidos de venda, faturamento
│   ├── compras/      # Pedidos de compra, itens
│   ├── estoque/      # Saldos, movimentações, controle de estoque
│   ├── fiscal/       # NF-e, NFC-e, NCM, CFOP, CEST, impostos, entrada de notas
│   ├── rh/           # Funcionários, cargos, folha de pagamento
│   ├── producao/     # Ordem de produção, itens de produção, apontamento
│   ├── bi/           # Business Intelligence (em desenvolvimento)
│   ├── ia/           # IA assistiva com Spring AI (em desenvolvimento)
│   ├── servicos/     # Ordens de serviço, emissão de NFS-e
│   ├── portais/      # Integrações com portais externos (estrutura)
│   └── integracoes/  # Serviços de integração (estrutura)
│
├── modules/          # Módulos independentes (279 classes Java em 12 módulos)
│   ├── core/         # Módulo core (30 classes) - Porta 8081
│   ├── shared/       # Biblioteca compartilhada (22 classes)
│   ├── cadastro/     # Cadastros (116 classes) - Porta 8082
│   ├── financeiro/   # Financeiro (51 classes) - Porta 8083
│   ├── vendas/       # Vendas (9 classes) - Porta 8084
│   ├── compras/      # Compras (10 classes) - Porta 8085
│   ├── estoque/      # Estoque (6 classes) - Porta 8086
│   ├── fiscal/       # Fiscal (89 classes) - Porta 8087
│   │   ├── mdfe/      # MDF-e: config, emissao, contrato, controller
│   │   ├── cte/       # CT-e: emissao e controller
│   │   ├── sped/      # SPED EFD ICMS/IPI: gerador e controller
│   │   └── nfse/      # NFS-e: emissao, retorno, controller
│   ├── rh/           # Recursos Humanos (15 classes) - Porta 8088
│   ├── servicos/     # Serviços (9 classes) - Porta 8080
│   ├── producao/     # Produção Industrial (9 classes) - Porta 8090 ✅ NOVO
│   └── ia/           # Assistive AI (0 classes) - Porta 8089 ⚠️ Estrutura vazia
│
├── src/main/resources/microservices/  # Material de referencia (554 MB, 23 pastas)
│   │
│   │   Origem de cada pasta (URL do repositorio) e o que serve dela em
│   │   docs/pesquisa/MICROSERVICES-O-QUE-TEM.md
│   │   ⚠️ EXCLUIDO do empacotamento pelo pom.xml. Nao entra no JAR e nao e
│   │   copiado para target/classes. E consulta, nao dependencia.
│   │
│   ├── nfse-sp-api/         # A API de NFS-e que roda na 4567 (codigo nosso)
│   ├── nfse-sp-bridge/      # Emissor Ruby, desligado (codigo nosso)
│   ├── nfse-watchdog/       # Watchdog, desligado de proposito (codigo nosso)
│   ├── nfse-failover/       # Teste do failover (codigo nosso)
│   │   │
│   │   O manifesto de hashes (MANIFESTO-MICROSERVICES.json) fica aqui quando
│   │   gerado, mas e gitignored: 2,6 MB de SHA-256 e artefato de build, nao
│   │   documentacao. Regenera com scripts/gerar_manifesto.py
│   ├── nfe/                 # fincatto documentofiscal 5.1.2 <- a lib do MDF-e
│   ├── PL_MDFe_300b_NT012025_1.05/  # 41 XSD + 6 PDF oficiais da SEFAZ
│   ├── NFSe-SaoPaulo-SP/    # XSD e manual da prefeitura de SP
│   ├── sped-mdfe/           # PHP nfeephp. So XSD e exemplos servem
│   ├── Java_MDFe/           # VAZIA: so um README de Discord, zero codigo
│   ├── Java_Certificado/    # Docs de A1 e A3
│   ├── l10n-brazil/         # OCA: as tabelas fiscais em CSV (cfop, cest, ncm)
│   ├── esocial/  Java_NFe/  Java_CTe/  Java_Efd-Icms/   # Referencia
│   └── ... 23 no total
│
├── src/main/resources/db/migration/   # Migrations Flyway (V1-V101)
│   ├── V1__init_schema.sql             # Schema inicial
│   ├── V2__core.sql                    # Core: usuários, empresas, perfis
│   ├── V3__cadastro.sql                # Cadastros básicos
│   ├── V4__fiscal.sql                  # Tabelas fiscais
│   ├── V5__financeiro.sql              # Financeiro completo (439 linhas)
│   ├── V15__seed_perfis_permissoes.sql # Seed de perfis e permissões
│   ├── V16__seed_tabelas_oficiais.sql  # Tabelas oficiais (municípios, etc)
│   ├── V17__migracao_sysfluxo.sql      # Migração do legado SysFluxo
│   ├── V18__seed_permissoes_cadastro.sql
│   ├── V19__seed_permissoes_fiscais.sql
│   ├── V20__vendas.sql                 # Módulo de vendas
│   ├── V21__compras.sql                # Módulo de compras
│   ├── V22__estoque.sql                # Módulo de estoque
│   ├── V23__rh.sql                     # Módulo de RH
│   ├── V24__alinhar_entidades_operacionais.sql
│   ├── V25__permissoes_adicionais.sql
│   ├── V26__adicionar_soft_delete_saldo_estoque.sql
│   ├── V27__produto_link_e_imagem_mongodb.sql
│   ├── V28__permissoes_financeiro.sql
│   ├── V29__permissoes_rh_cargo_func.sql
│   ├── V30__seed_fornecedor_ref.sql
│   ├── V31__fiscal_entrada_tipo_operacao.sql
│   ├── V32__permissoes_fase_final.sql
│   ├── V33__seed_fornecedor_cliente_produto_ref.sql
│   ├── V35__servicos_ordem_servico.sql # Ordem de serviço
│   ├── V39__logo_foto_colunas.sql      # Colunas de logo/foto
│   ├── V40__seed_referencias_minimas.sql
│   ├── V41__servicos_ordem_servico_segundo.sql
│   ├── V42__fiscal_entrada_tipo_operacao_segundo.sql
│   ├── V43__permissoes_servicos_e_entrada.sql
│   ├── V44__add_logo_fields_to_tables.sql
│   ├── V45__add_hierarquia_nivel_to_perfil.sql
│   ├── V46__seed_diretoria_gerente_hierarquia.sql
│   │   ... V47 a V97: modulos seguintes e reconciliacao de permissoes
│   ├── V92__servico_codigo_tributacao_municipal.sql  # Codigo municipal do servico
│   ├── V93__nfse_arquivos_mongo_e_chave_nacional.sql  # XML no Mongo, chave nacional
│   ├── V94__caixa.sql
│   ├── V95__empresa_cnpj_e_usuario_sem_empresa.sql
│   ├── V96__catalogo_bancos.sql
│   ├── V97__reconcilia_permissoes_por_perfil.sql
│   ├── V98__nfse_retorno_prefeitura.sql    # bc_fis_nfse_retorno
│   ├── V99__nfse_retorno_colunas_base.sql  # Colunas que a entidade exigia
│   ├── V100__cfop_e_cest_do_oca.sql        # 619 CFOP + 1.043 CEST
│   └── V101__servicos_teste_com_codigos_municipais_validos.sql
│
│   ⚠️ Migration aplicada nao se reescreve. O Flyway grava o checksum no
│   historico, e uma migration alterada derruba a aplicacao na subida.
│   Correcao sempre para frente, com uma V nova.
│
├── systemd_units/    # Serviços Linux (systemd)
│   ├── brasilsaas-erp-core.service
│   ├── brasilsaas-erp-cadastro.service
│   └── ...
│
├── scripts/          # Scripts de automação
│   ├── manage_services.sh    # Gerenciamento mestre
│   ├── build_module.sh        # Build individual
│   ├── installbase.sh         # Instalação Linux (também está na raiz)
│   ├── install-sysfluxo.ps1   # Instalação Windows
│   ├── test_db_connection.sh  # Smoke test DB
│   ├── test_erp_operations.sh # Teste operações
│   ├── check_mongodb.sh       # Validação MongoDB
│   │
│   │   Fiscal e material de referencia:
│   ├── gerar_manifesto.py       # SHA-256 dos 16.299 arquivos do material
│   ├── restaurar_microservices.sh  # Baixa, apaga o .git e VERIFICA o hash
│   ├── salvar_docs_mongo.py    # Documentacao no Mongo; recusa credencial
│   └── montar_cadeia_sefaz.py  # Monta o truststore JKS com as CAs do sistema
│
├── src/main/resources/static/react/  # Frontend React
│   ├── src/
│   │   ├── components/     # Componentes PrimeReact
│   │   ├── contexts/       # Contextos React
│   │   └── services/       # Serviços API
│   ├── package.json        # React 19, PrimeReact 10.8, Vite 5.4
│   └── vite.config.js
│
├── frontend/         # Frontend alternativo (em desenvolvimento)
│   ├── public/
│   └── src/
│
├── docs/             # Documentação técnica
│   ├── pesquisa/     # Investigações e achados de campo
│   │   ├── docs/pesquisa/MEGA-MANUAL-API-FISCAL.md        # Como montar a API, passo a passo
│   │   ├── docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md  # As 3 APIs, com o testado
│   │   ├── docs/pesquisa/MICROSERVICES-O-QUE-TEM.md        # As 23 pastas e a origem de cada
│   │   ├── docs/pesquisa/TELAS-VAZIAS-DO-FISCAL.md         # CFOP e CEST que mostravam vazio
│   │   ├── docs/pesquisa/contrato-nfse-unico.md            # O contrato de resposta
│   │   ├── docs/pesquisa/o-que-sobrevive-a-tela-de-erro.md  # A tabela de retornos
│   │   ├── docs/pesquisa/REGISTRO-da-prefeitura-NFSE.md     # O que a prefeitura respondeu
│   │   ├── docs/pesquisa/pesquisa-failover-chunked.md       # Por que o proxy perdia o corpo
│   │   ├── docs/pesquisa/proxy-nginx-e-bug-do-fallback.md   # O bug que gerou notas duplicadas
│   │   ├── docs/pesquisa/CAUSA-do-crash-loop-NFSE.md        # A causa do crash de 8,6 h
│   │   └── docs/pesquisa/diagrama-fallback-nfse.md          # O desenho do fallback
│   │
│   ├── modulos/      # Documentação por módulo
│   ├── api/          # Contratos de API
│   ├── docs/./arquitetura.md
│   ├── docs/./modelo-dados.md
│   ├── docs/./autenticacao.md
│   ├── docs/./roadmap.md
│   └── docs/./RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md
│
├── pom.xml           # Maven principal (Spring Boot 3.3.5)
├── pom-parent.xml    # Parent POM modular
├── pom-multimodule.xml # Build multi-módulo
└── docs/modulos/README.md         # Este arquivo
```

---

## 🚀 How to Run the Application

### Prerequisites

| Dependency | Minimum Version | Required |
|-------------|---------------|-------------|
| ☕ Oracle JDK | 21 (LTS) | ✅ Yes |
| 📦 Apache Maven | 3.9+ | ✅ Yes |
| 🟢 Node.js | 22+ LTS | ✅ (frontend) |
| 🐘 PostgreSQL | 18+ | ✅ Yes |
| 🍃 MongoDB | Latest | ✅ (images) |
| 🔧 Git | Latest | ✅ Yes |

### 🐧 Installation on Linux (Ubuntu/Debian/CentOS/Fedora)

```bash
# Tornar script executável
chmod +x installbase.sh

# Executar instalação automatizada
sudo ./installbase.sh
```

**The script will:**
1. Detect the Linux distribution
2. Install JDK 21 via SDKMAN
3. Install Maven and Node.js
4. Configure PostgreSQL and MongoDB
5. Generate the Easy-RSA PKI and set up PostgreSQL mTLS
6. **Install the ICP-Brasil CAs and assemble the JKS truststore** (MDF-e e CT-e)
7. Clone Maven dependencies
8. Automatic React frontend build

Step 6 is not optional. SVRS presents a certificate from ICP-Brasil
(`AC SERPRO SSLv1`, vindo da `Raiz Brasileira v10`), e num Debian limpo nenhuma
dessas duas está no bundle de CAs: sem elas o handshake morre com
`PKIX path building failed`. The script leaves:

```
/etc/brasil-saas/certs/truststore-sefaz.jks   644   public CA only
/etc/brasil-saas/sefaz.env                   644   path and state, no secret
/etc/brasil-saas/cert.env                    600   the A1 and the passwords
```

The truststore is separate from the system truststore **on purpose**: the ERP is Java
and the fiscal library reads its own JKS. Without assembling it, the same error comes
back and it looks like the CAs never went in.

`cert.env` must be filled in with the issuer's A1:

```bash
BRASIL_SAAS_MDFE_CERTIFICADO=/caminho/do/a1.pfx
BRASIL_SAAS_MDFE_CERT_PASS=...
```

**The same A1 works for NFS-e, MDF-e and CT-e**, desde que seja e-CNPJ A1 da
ICP-Brasil. These are not different certificates: they are different uses of the same one.

### Operation scripts

```bash
# Sobe o ambiente de desenvolvimento (Postgres, Mongo, backend 8080, Vite 5173)
./subir-dev.sh --reiniciar
./subir-dev.sh --parar

# Gera o manifesto de hashes das 23 pastas de microservices
python3 scripts/gerar_manifesto.py

# Baixa os repositórios, apaga o .git e VERIFICA o hash de cada arquivo
./scripts/restaurar_microservices.sh --verificar

# Grava a documentação no Mongo e recusa documento com credencial
python3 scripts/salvar_docs_mongo.py
```

O `restaurar_microservices.sh` importa SHA-256 dos 16.299 arquivos das pastas
de referência. **Só 3 das 23 mantêm o `.git` com remote** — das outras 20 o
history is lost, and a `clone` brings today's HEAD, which may be a different version
do material com que o ERP foi testado. O hash é o que diz se o que baixou é o
mesmo, e o script **diz que não bate** em vez de deixar material errado entrar
em silêncio.

### 🪟 Instalação no Windows

```powershell
# Executar PowerShell como Administrador
.\install-sysfluxo.ps1
```

**The script will:**
1. Verificar permissões de administrador
2. Instalar JDK 21 Oracle via winget/choco
3. Instalar Node.js, Maven e PostgreSQL
4. Configurar firewall nas portas necessárias
5. Build automático do frontend

### 📝 Configuração Manual (Passo a Passo)

#### 1️⃣ Clonar o Repositório

```bash
git clone https://github.com/euripedesdark/BRASIL-SAAS-ERP.git
cd BRASIL-SAAS-ERP
```

#### 2️⃣ Configurar Banco de Dados

```sql
-- Criar banco no PostgreSQL
CREATE DATABASE "brasil-saas" WITH OWNER = postgres ENCODING = 'UTF8';

-- Criar schema
CREATE SCHEMA brasil_saas;
```

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brasil-saas?currentSchema=brasil_saas
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

# MongoDB
spring.data.mongodb.uri=mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil_saas?authSource=admin
```

#### 3️⃣ Build do Frontend

```bash
cd src/main/resources/static/react
npm install
npm run build
cd ../../../../
```

#### 4️⃣ Build do Backend

```bash
mvn clean package -DskipTests
```

#### 5️⃣ Executar a Aplicação

```bash
# Modo desenvolvimento
mvn spring-boot:run

# Ou executar JAR
java -jar target/BRASIL-SAAS-ERP-web-1.0.0.jar

# Modo produção
java -jar target/BRASIL-SAAS-ERP-web-1.0.0.jar --spring.profiles.active=prod
```

---

## 🌐 Accessing the System

| URL | Description |
|-----|-----------|
| http://localhost:8080 | URL Principal |
| http://localhost:8080/api/* | API REST |
| http://localhost:8080/swagger-ui.html | Documentação Swagger |
| http://localhost:8080/actuator/health | Health Check |
| http://localhost:8080/actuator/metrics | Métricas |

### 🔑 Credenciais Padrão

| Usuário | Senha | Perfil |
|---------|-------|--------|
| sysdba | masterkey | SuperAdmin |

> ⚠️ **Importante**: Alterar após o primeiro acesso!

---

## 📊 Fiscal completion percentage

**Critério usado**, para o número não ser opinião. Cada documento fiscal precisa
de 6 entregáveis:

| # | Entregável |
|---|-----------|
| 1 | **Connection to SEFAZ/city hall** answering |
| 2 | **Montagem do documento** (o XML: emitente, veiculo, motorista, LAC) |
| 3 | **Assinatura** com A1 ICP-Brasil |
| 4 | **Envio e protocolo** |
| 5 | **Consulta / cancelamento** |
| 6 | **Screen** showing what the city hall returned |

| Module | 1 | 2 | 3 | 4 | 5 | 6 | % |
|--------|---|---|---|---|---|---|---|
| **NFS-e São Paulo** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **100%** |
| **SPED EFD ICMS/IPI** | — | ✅ | ⚪ | — | — | ⚪ | **40%** |
| **MDF-e** | ✅ | ❌ | ⚪ | ❌ | ⚪ | ❌ | **25%** |
| **CT-e** | ✅ | ❌ | ⚪ | ❌ | ❌ | ❌ | **17%** |
| **NF-e / NFC-e** | ❌ | ❌ | ❌ | ❌ | ❌ | ⚪ | **0%** |
| **SPED EFD Contribuições** | — | ❌ | ⚪ | — | — | ⚪ | **20%** |
| **Consulta/DistDFe** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | **100%** |

`✅` pronto e verificado · `⚪` existe mas não verificado · `❌` não existe
`—` não se aplica (EFD não se envia a ninguém: é arquivo, não web service)

**Lendo os números:**

- **MDF-e and CT-e are at 25% and 17%, not at 100%.** The connection works and the
  SEFAZ responde `cStat 107` com o seu A1 — isso é o entregável 1, o mais
  difícil, porque eram 4 camadas entre o ERP e a resposta (A1, CAs da
  ICP-Brasil, truststore JKS, mutual TLS). **what is missing is deliverable 2**, the
  montagem do documento, e ele é o trabalho maior. `cStat 107` é o *status do
  service*: it says nothing about the XML, because no XML was ever assembled.
- **SPED EFD ICMS/IPI em 40%** e não em 100% porque gera arquivo válido, com
  os contadores conferidos, mas lê o cabeçalho do corpo da requisição em vez
  of the bank, and there is no screen for it. Deliverables 2 and 5 are missing.
- **NF-e em 0%.** Não é exagero: os 3 métodos da interface `NFeService` estão
  como `TODO` em `NFeServiceImpl`. A interface existe, a implementação não.
- **Consulta/DistDFe em 100%** de 6 em 6.

The percentage is about the **fiscal scope**, not the whole system. The other modules are measured on their own,
abaixo.

## 📊 Other modules — structure and what answers

### Why there is no single % here

**Counting files does not measure readiness.** I measured the modules by the same
6 deliverables of the fiscal one and almost all give 100% structural — because
all have model, repository, service, controller and screen. That does not
mean the customer registry opens, that the bill settles or that the payroll computes. It means the files exist.

What measures is to run. So there are two columns:

| Module | Tables | Endpoints | Screen | Structural | Responds (03/10/2026) |
|--------|:-------:|:---------:|:----:|:----------:|:----------------------:|
| Cadastro | 21 | 31 | ✅ | 6/6 | ✅ |
| Financeiro | 32 | 55 | ✅ | 6/6 | ✅ |
| Estoque | 13 | 17 | ✅ | 5/6 | ✅ |
| Fiscal | 30 | 28 | ✅ | 6/6 | ⚠️ parcial (see fiscal %) |
| Vendas | 11 | 7 | ✅ | 6/6 | ✅ |
| Compras | 11 | 12 | ✅ | 6/6 | ✅ |
| Produção | 10 | 30 | ✅ | 6/6 | ✅ |
| RH | 4 | 14 | ✅ | 6/6 | ✅ |
| Serviços | 3 | 8 | ✅ | 6/6 | ✅ |
| BI | 8 | 46 | ✅ | 6/6 | ✅ |
| Core/Auth | 18 | 33 | ✅ | 6/6 | ✅ |
| IA | 11 | 74 | ⚠️ 1 screen | 6/6 | ⚠️ parcial |
| Contabilidade | 3 | 15 | ✅ | 6/6 | ✅ |
| CRM | 4 | 10 | ✅ | 6/6 | ✅ |
| Ativos | 2 | 4 | ✅ | 6/6 | ✅ |
| DMS | 3 | 9 | ✅ | 6/6 | ✅ |
| Qualidade | 3 | 7 | ✅ | 6/6 | ✅ |
| Projetos | 6 | 14 | ✅ | 6/6 | ✅ |
| WMS | 4 | 14 | ✅ | 6/6 | ✅ |
| Workflow | 4 | 10 | ✅ | 6/6 | ✅ |
| Portais | 1 | 5 | ✅ | 6/6 | ✅ |
| Relatórios | — | 2 | ✅ | 6/6 | ✅ |

**Structural** = the 6 deliverables exist as files.
**Responds** = the API answered in a smoke test with a valid token on
**03/10/2026**.

⚠️ **None of these was functionally validated.** I opened the screen, no.
I created a record, no. What the smoke test proves is that the **route
responds**; it does not prove the returned data is right. The two
things are different.

### What the smoke test found (03/10/2026)

67 routes called, one or more per module:

```
62  2xx   respondem
 5  400   parâmetro obrigatório faltando (a rota existe)
 0  500   nenhum erro
```

The 400s are expected validation behaviour
(`/api/bi/indicadores/dashboard`, `/api/ia/config`, `/api/ia/sessoes`,
`/api/producao/apontamentos/por-status`, `/api/producao/apontamentos/por-periodo`,
`/api/contabilidade/balancete`, `/api/fiscal/sefaz/status`,
`/api/wms/putaway`) — they ask for a parameter the test did not pass.

### The RH bug was fixed

The 26/09/2026 smoke test found a real **500**:
`GET /api/rh/funcionarios` broke with
`LazyInitializationException` — `Funcionario` has
`@ManyToOne(fetch = LAZY)` to `Cargo`, and Jackson serialized the
proxy after the Hibernate session closed.

**On 03/10/2026 it is fixed:** `/api/rh/funcionarios/pessoas`,
`/api/rh/cargos` and `/api/rh/folhas` answer **200**. The fix
was to fetch with `join fetch` on `Cargo` (or a DTO), which keeps the
serializer from touching the proxy outside the session.

### Inventory has no service layer

9 controllers, 12 repositories, **0 services**. The business rule sits
inside the controller, talking straight to the repository. It works,
but there is nowhere to test and it does not isolate the rule. In the other modules the
separation exists — it is an inconsistency to decide, not an accident.

## ✅ What Is Running

Verified on **03/10/2026** via `systemctl` and `ss`.

| Service (systemd) | Port | State |
|-------------------|-------|--------|
| `brasil-saas-erp.service` (Spring Boot, user `brasilsaas`) | 8080 | 🟢 running |
| `nfse-sp-api.service` (Java, primary) | 4568 | 🟢 running |
| `nfse-sp-bridge.service` (Ruby, fallback) | 4569 | 🟢 running |
| `brasil_saas-watchdog.service` (contract check) | — | 🟢 running |
| `brasil_saas-minio.service` | 9000 / 9001 | 🟢 running |
| `auth-service.service` | 8081/8082 | 🟢 running |
| PostgreSQL (schema `brasil_saas`) | 5432 | 🟢 up |
| MongoDB | 27017 | 🟢 up (4 collections) |
| RabbitMQ | 5672 / 15672 | 🟢 up |
| Redis | 6379 | 🟢 up |
| nginx (domain proxy) | 80 / 443 | 🟢 up |

The ERP runs from the jar
`/opt/brasil-saas-erp/brasil-saas-erp-1.0.0-SNAPSHOT.jar`
(unit `brasil-saas-erp.service`). The NF-e microservices run from the
jars inside `src/main/resources/microservices/` — **attention**: on
the server, the active jars are in
`/home/euripedes/BrasilCloudERP/src/main/resources/microservices/`
(another checkout), not in this repo.

### ⚠️ The NFS-e failover proxy (4567) is down

The ERP calls NFS-e issuance at
`brasil-saas.fiscal.nfse.url`, whose default is
`http://127.0.0.1:4567/api/nfse-sp` — the **failover proxy**. On
03/10/2026 **nothing listens on 4567** (connection refused), although the
two implementations (4568 Java, 4569 Ruby) and the watchdog are
up.

Consequence: unless `/etc/brasil-saas/erp.env` (file of user
`brasilsaas`, unreadable without sudo) overrides the URL to
4568, **NFS-e issuance is broken right now**. Evidence: the last
NFS-e in the database is from **26/09/2026** (note 33), with 4
`FALHA_EMISSAO` records on that day and no attempt since.

Action: bring the 4567 proxy up (or point `brasil-saas.fiscal.nfse.url`
straight at `http://127.0.0.1:4568/api/nfse-sp`), and record in
`erp.env` which URL is in use.

### What the ERP validates on its own

| Check | Result (03/10/2026) |
|-------------|------------------------|
| `GET /api/fiscal/mdfe/status` → SVRS | **200** (`cStat 107`) |
| `GET /api/fiscal/cte/status` → SEFAZ SP | **200** (`cStat 107`) |
| `GET /api/fiscal/sped/efd/exemplo` | **200** (valid file) |
| `GET /api/fiscal/nfse/retornos/recusas` and `/para-conferir` | **200** |

MDF-e is served by **SVRS** (`ufAtendente: RS`) and CT-e by the
**SP portal** (`ufAtendente: SP`), with the **same A1**. They are
different authorizers, not different certificates.

### What is not ready yet

| Item | Situation |
|------|----------|
| **Failover proxy 4567** | Down; the ERP URL default. See above. |
| **NF-e / NFC-e** | `NFeServiceImpl` has **3 TODOs**. The interface promises `emitirNFe`, `cancelarNFe` and `consultarSituacao`; none of the three is implemented. |
| **MDF-e issuance** | The connection to SVRS is ready (`cStat 107`) and the certificate signs. **The document assembly does not exist**: `MdfeEmissaoService` has only `statusServico` and `consultarRecibo`. |
| **CT-e issuance** | Ditto. `CteEmissaoService` has only `statusServico`. |
| **SPED EFD Contributions** | The library resolves in `pom.xml`, but there is **no endpoint** — only ICMS/IPI has one. |
| **IA (Spring AI)** | Module present (74 endpoints), with embeddings, chat and classifications. Not functionally validated; needs a valid API key. |
| **IA screen** | 1 screen only (`IaAssistWidget`); the 74 endpoints have no equivalent screen. |
| **CIOT validation** | `cStat 684` goes live on SEFAZ on **23/11/2026**. `infCIOT` is already in the XSD; the validation in the issuer is missing. |

**Why MDF-e and CT-e are not on the "ready" list despite
answering `cStat 107`:** the `cStat 107` is the *service status*, and
it proves that the TLS chain, the client certificate and the SOAP envelope
are correct. It does **not prove the document's XML is there**, because
no document was assembled or sent. They are two different things,
and the README separates them for that reason.

---

## 📡 API Endpoints

The backend exposes **451 REST endpoints** (`/api/**`). The full
list is in [`docs/INDICE.md`](docs/INDICE.md) and in the
Swagger UI: <http://localhost:8080/swagger-ui.html>.

### Municípios

```
GET    /api/municipios/paginado?page=0&size=20&sort=nome,asc
GET    /api/municipios/buscar?termo={codigo_ou_nome}
GET    /api/municipios/codigo/{codigoIbge}
GET    /api/municipios/{id}
POST   /api/municipios
PUT    /api/municipios/{id}
DELETE /api/municipios/{id}
```

### Financeiro

```
GET    /api/financeiro/titulos/paginado?page=0&size=20
GET    /api/financeiro/lancamentos/paginado?page=0&size=20
GET    /api/financeiro/resumo?inicio=YYYY-MM-DD&fim=YYYY-MM-DD
POST   /api/financeiro/titulos
POST   /api/financeiro/lancamentos
PUT    /api/financeiro/titulos/{id}/baixar
DELETE /api/financeiro/titulos/{id}/estornar
GET    /api/financeiro/extrato/{idConta}
GET    /api/financeiro/conciliacao
GET    /api/financeiro/fluxo-caixa
GET    /api/financeiro/orcamentos
GET    /api/financeiro/emprestimos
GET    /api/financeiro/planos-contas
GET    /api/financeiro/centros-custo
GET    /api/financeiro/contas-bancarias
GET    /api/financeiro/caixas
GET    /api/financeiro/condicoes-pagamento
GET    /api/financeiro/tipos-pagamento
GET    /api/financeiro/remessas
GET    /api/financeiro/retornos
```

### Ordem de Serviço

```
GET    /api/servicos/os/{id}
GET    /api/servicos/os/{id}/itens
GET    /api/servicos/os/{id}/pdf
POST   /api/servicos/os
PUT    /api/servicos/os/{id}
POST   /api/servicos/os/{id}/emitir-nota
POST   /api/servicos/os/{id}/fechar
```

### Relatórios

```
GET    /api/relatorios/{tipo}
GET    /api/relatorios/pdf/{tipo}
GET    /api/relatorios/financeiro?inicio=YYYY-MM-DD&fim=YYYY-MM-DD
GET    /api/relatorios/os-pdf/{id}
GET    /api/relatorios/dre?competencia=YYYY-MM
```

### BI

```
GET    /api/bi/dashboards                      /dashboards/publicos   /dashboards/tipo/{tipo}
GET    /api/bi/kpis/{id}/calculate             /bi/kpis/type/{kpiType}
GET    /api/bi/indicadores                     /indicadores/categoria/{categoria}
GET    /api/bi/relatorios/{id}                 /relatorios/{id}/pdf  /excel  /csv
GET    /api/bi/relatorios-agendados            /relatorios-agendados/pendentes
GET    /api/bi/reports/{id}                    /reports/category/{category}
```

### Fiscal — NFS-e São Paulo

Issuance is done by a separate API. The ERP calls
`brasil-saas.fiscal.nfse.url` (default
`http://127.0.0.1:4567/api/nfse-sp`, the failover proxy —
see the warning in "What Is Running"). The active implementations
are the **Java one on 4568** (primary) and the **Ruby one on 4569**
(fallback), watched by `brasil_saas-watchdog`, which checks the
response **contract**, not just the port.

```
POST   /api/fiscal/nfse/emitir
POST   /api/fiscal/nfse/{id}/cancelar
GET    /api/fiscal/nfse
GET    /api/fiscal/nfse/{id}/xml
GET    /api/fiscal/nfse/{id}/pdf
GET    /api/fiscal/nfse/{id}/retornos
GET    /api/fiscal/nfse/{id}/retornos/{retornoId}/bruto
GET    /api/fiscal/nfse/retornos/recusas           só as recusadas
GET    /api/fiscal/nfse/retornos/para-conferir     as que não deu para saber
```

The reason for the city hall's rejection is recorded **before** the
exception goes up, with the `cStat` and the raw body in Mongo. The
error screen is a wall; the record is not.

### Fiscal — MDF-e and CT-e

```
GET    /api/fiscal/mdfe/status                     cStat 107 se a SVRS estiver de pé
GET    /api/fiscal/mdfe/recibo?numero={recibo}     2ª chamada: do recibo para a chave
GET    /api/fiscal/cte/status                      cStat 107 se a SEFAZ estiver de pé
```

Both use the same response contract, with the **three states** of
`sucesso`: `true` (authorized), `false` (rejected, with `cStat`
and reason) and `null` (**could not tell**). The third state exists so
as not to duplicate a document: SVRS rejects a repeated natural key, and
reissuing in the dark is exactly what creates the duplication.

MDF-e returns a **receipt** on the first call and a **protocol** on the
second. Reading the key in the submission response gives an NPE — the
method does not exist.

### Fiscal — SPED EFD ICMS/IPI

```
POST   /api/fiscal/sped/efd/gerar                  gera o arquivo EFD
GET    /api/fiscal/sped/efd/exemplo                gera o de exemplo, para conferir o formato
```

**EFD is not sent to anyone.** The file is generated, signed and stored; whoever
looks it up afterwards is SEFAZ or the Receita. No web service, no protocol, no queue.

### Fiscal — query and distribution

```
GET    /api/fiscal/sefaz/status
GET    /api/fiscal/sefaz/consultar
GET    /api/fiscal/sefaz/distribuicao
```

### Fiscal — tables and registries

```
GET    /api/fiscal/cest?busca={termo}              busca por código, descrição ou NCM
GET    /api/fiscal/cfop?tipoOperacao=ENTRADA|SAIDA
GET    /api/fiscal/ncm
GET    /api/fiscal/ncm/{codigo}
GET    /api/fiscal/issqn
GET    /api/fiscal/impostos
GET    /api/fiscal/certificados
```

`GET /api/cadastro/servicos?nome={termo}` feeds the
autocomplete of the service name, which suggests an already registered
service while the person types. Two registrations with a similar name and a
different municipal code is the most common cause of rejection at the
city hall.

### Other modules (roots)

```
GET    /api/cadastro/produtos      /clientes   /fornecedores   /pessoas   /servicos
GET    /api/estoque/depositos      /saldos     /movimentacoes  /lotes     /reservas
       /estoque/transferencias     /inventarios /expedicoes
GET    /api/vendas/pedidos         /vendas/tabelas-preco
GET    /api/compras/pedidos        /compras/recebimentos
       /compras/supply-chain/solicitacoes
GET    /api/producao/estruturas    /producao/roteiros  /producao/centros-trabalho
       /producao/apontamentos      /producao/romaneios
GET    /api/contabilidade/lancamentos /contabilidade/balancete /contabilidade/dre
GET    /api/rh/funcionarios/pessoas /rh/cargos /rh/folhas
GET    /api/crm/leads              /crm/pipeline   /crm/forecast  /crm/atividades
GET    /api/ativos/manutencoes
GET    /api/dms/documentos         /dms/retencao
GET    /api/qualidade/planos       /qualidade/inspecoes /qualidade/nao-conformidades
GET    /api/projetos               /projetos/{id}/resumo /projetos/{id}/etapas
GET    /api/wms/ondas              /wms/putaway
GET    /api/workflow/definitions   /workflow/instances /workflow/tasks/pendentes
GET    /api/portais/acessos        /portais/publico/validar
GET    /api/ia/config              /ia/sessoes   /ia/prompts   /ia/embeddings
GET    /api/core/perfil            /core/minha-empresa  /core/recent/updates
GET    /api/superadmin/usuarios    /superadmin/sql/catalogo
```

---

## 🎯 Special Features

### 🔍 Busca Dinâmica de Municípios
Digite o código IBGE ou nome e o sistema localiza instantaneamente.

### 📄 Smart Pagination
Todas as telas exibem máx. 20 linhas, com navegação entre páginas.

### 🌳 Lazy Loading
Dados carregados sob demanda, reduzindo consumo de memória.

### 📊 Dashboard Financeiro
Visualize totais por status (Aberto, Pago, Atrasado) em tempo real.

### 🖨️ Professional Reports
PDFs com layout moderno, cores diferenciadas e formatação empresarial.

### 🔐 Hierarquia de Permissões

| Perfil | Acesso |
|--------|--------|
| **SuperAdmin** | Completo, incluindo gerenciamento de administradores |
| **Diretor** | Todas funcionalidades exceto gerenciar administradores |
| **Gerente** | Todas funcionalidades operacionais |
| **Usuário** | Limitado às permissões específicas |

---

## ⚙️ Advanced Configuration

### Alterar Porta do Servidor

```properties
# application.properties
server.port=8080  # Altere para a porta desejada
```

### Modo Produção

```bash
mvn clean package
java -jar target/BRASIL-SAAS-ERP-web-1.0.0.jar --spring.profiles.active=prod
```

### Logs Detalhados

```properties
logging.level.br.com.brasil_saas=DEBUG
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Variáveis de Ambiente

```bash
# PostgreSQL
export DATABASE_URL="jdbc:postgresql://localhost:5432/brasil-saas?currentSchema=brasil_saas"
export DATABASE_USERNAME="sa"
export DATABASE_PASSWORD="<sua-senha>"

# MongoDB
export MONGODB_URI="mongodb://admin:${MONGODB_PASSWORD}@localhost:27017/brasil_saas?authSource=admin"

# JWT
export JWT_SECRET="ALTERE_ME_32_BYTES_OU_MAIS"

# Imagens
export IMAGENS_ENTRADA_DIR="/path/para/imagens"
```

---

## 🧪 Test and Validation Scripts

| Script | Finalidade |
|--------|------------|
| `test_db_connection.sh` | Tests the PostgreSQL connection |
| `test_database.sh` | Valida schema e migrations |
| `test_erp_operations.sh` | Smoke test de operações ERP |
| `check_mongodb.sh` | Checks the connection and the MongoDB collection |

---

## 🔒 Security

| Recurso | Description |
|---------|-----------|
| 🔐 **Senhas Criptografadas** | BCrypt no banco de dados |
| 🛡️ **Filtro de Autenticação** | Em todas as rotas API e web |
| 🔑 **Tokens JWT** | Com expiração configurável |
| 🚫 **SQL Injection** | Protegido via JPA/Hibernate |
| 🌐 **CORS Configurado** | Para APIs externas |
| 📁 **Upload Seguro** | Validação de tipo e tamanho (10MB) |
| 🔐 **mTLS** | Conexão PostgreSQL criptografada com PKI |

---

## 🐛 Troubleshooting

### Erro: "Port 8080 already in use"

```bash
# Linux
sudo lsof -i :8080
sudo kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Erro: "Database not found"

Verifique se o PostgreSQL está rodando:

```bash
sudo systemctl status postgresql
psql -h localhost -p 5432 -U sa -d brasil-saas
```

### Erro: "npm not found"

Instale Node.js 22+ LTS:

```bash
# Linux
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs

# Windows (PowerShell)
winget install OpenJS.NodeJS.LTS
```

### Frontend não carrega ou aparece em branco

**Verifique se o build foi executado:**

```bash
cd src/main/resources/static/react
npm install && npm run build
```

**Status do Build:**
- ✅ Build has already run (2.2 MB in `static/dist/`)
- ✅ 25 React components compiled
- ✅ Assets otimizados (JS, CSS, fonts, icons)

**Se precisar rebuild:**
```bash
# Limpar e rebuild
rm -rf ../dist
npm run build

# Verificar output
ls -lh ../dist/assets/
```

**Problemas comuns:**
1. **Cache do navegador** - Ctrl+F5 para hard refresh
2. **CORS errors** - Verificar backend rodando na porta 8080
3. **Token expirado** - Fazer login novamente
4. **Erro 404 em assets** - Confirmar path correto no index.html

### Erro: "No plugin found for prefix 'lint'"

Este erro ocorre ao executar `mvn lint`. O projeto não usa o plugin `maven-lint-plugin`. Use:

```bash
# Para compilar
mvn clean compile

# Para verificar código
mvn spotbugs:check
# ou
mvn pmd:check
```

---

## 📂 Unreferenced Files / Backup

Os seguintes arquivos e diretórios são backups ou não estão em uso ativo:

| Type | Arquivo/Diretório | Description |
|------|-------------------|-----------|
| 📁 Backup | `old-controller-backup/` | Controllers antigos (backup) |
| 📁 Backup | `old-service-backup/` | Services antigos (backup) |
| 📁 Backup | `old-java-backup/` | Classes Java antigas (backup) |
| 📁 Backup | `bkp/` | Diretório de backups gerais |
| 📁 Backup | `sysfluxo - bkp/` | Backup do sistema legado SysFluxo |
| 📄 Backup | `pom.xml2` | Versão alternativa do POM |
| 🐍 Script | `compilar.py` | Script Python de compilação (legado) |
| 🐍 Script | `importa_ncm.py` | Importação NCM (uso pontual) |
| 🐍 Script | `exporta.py` | Exportação de dados |
| 🐍 Script | `exporta2.py` | Exportação de dados (v2) |
| 🐍 Script | `corrige.py` | Script de correções |

---

---

## 📄 License

This project is under the **GNU Affero General Public License v3.0 (AGPLv3)**.

The full text, unchanged, is in [`LICENSE.md`](LICENSE.md).

AGPLv3 requires that the source code be offered to anyone who uses the program,
including when the use is **over the network** — that is what the "Affero" refers to.
For an ERP reached through a browser, that clause is the one that matters: whoever
points a browser at this system is entitled to the source.

The third-party components shipped in `src/main/resources/microservices/` keep their
own licenses (MIT, Apache-2.0 and BSD), all compatible with AGPLv3. Each component's
original `LICENSE` file stays where it is and is not replaced by this one.

© 2026 Brasil SaaS ERP

---

---

---

## ❤️ Support the Project

Brasil SaaS ERP is an open source ERP maintained by a single developer.

If this project helped you, your company or your team, please consider supporting its development.

**PIX:**

```
24adc62c-b073-4587-974d-03fe35f6733f
```

A donation of any amount pays for the server, the digital certificate and the city hall
issuance fees. It does not buy a delivery date on any issue.

### 💳 International transfer

The PIX key does not work outside Brazil. To donate from abroad, use a bank transfer.

**If you are sending from a bank in the United States**, you can use these details for a
domestic transfer. **If you are sending from anywhere else**, make a Swift international
transfer.

| | |
|---|---|
| **Name** | Euripedes Batista de Paiva Junior |
| **Account type** | Checking |
| **Routing number** (for wire and ACH) | `101019628` |
| **Account number** | `215822927677` |
| **Bank name and address** | Wise US Inc, 108 W 13th St, Wilmington, DE, 19801, United States |
| **SWIFT/BIC** | `TRWIUS35XXX` |

The routing number is only used when the money leaves the United States. Anywhere else,
the field to use is `SWIFT/BIC`.

---

## 📬 Suporte

For questions, suggestions or bug reports:

- 📧 **Email**: euripedesdark@gmail.com
- 🔗 **GitHub Issues**: https://github.com/euripedesdark/BrasilCloudERP/issues
- 📖 **Documentação**: `/docs/`

---

## 🤝 Como Contribuir

The project is under AGPLv3 and accepts contributions.

1. **Abra uma issue** antes de escrever código, descrevendo o problema ou a melhoria.
   Sem issue beforehand, a mudança pode ir para um caminho que ninguém usa.
2. **Crie uma branch** com nome descritivo: `minha-melhoria`.
3. **Commit by topic.** One commit does one thing; a commit with six things cannot
   ser revertido sozinho.
4. **Rode os testes** antes de abrir o PR: `mvn test`.
5. **Abra o Pull Request** contra a `main` e descreva o que muda e por quê.

```bash
git clone https://github.com/euripedesdark/BrasilCloudERP.git
cd BrasilCloudERP
mvn test
```

### O que não mexer sem issue

| Área | Why |
|---|---|
| Database | A wrong `ALTER` in production has no rollback |
| Active Directory | Grupo errado dá acesso a quem não deveria |
| `SQL_AUTHORITIES` | Define a autorização; mudar é decisão de negócio |
| `pg_hba.conf`, `pg_ident.conf` | Controlam quem entra no Postgres |

---

## ❓ Perguntas Frequentes

**O ERP emite nota fiscal hoje?**
Emite NFS-e de São Paulo pela API única na porta 4567. NF-e e NFC-e **não**: o
`NFeServiceImpl` tem 89 linhas e 3 `TODO`s, e nenhuma das três promised operations
está implementada.

**MDF-e e CT-e estão prontos?**
No, despite answering `cStat 107`. That code is the *service status*: it proves that
the TLS, the client certificate and the SOAP envelope are correct. It does not prove that the XML
do documento existe, porque nenhum documento foi montado.

**Qual a diferença entre estar em produção e responder?**
Services like SVRS and SEFAZ answer a status call. Answering is not issuing.
O README separa as duas coisas por isso.

**Do I need an A1 certificate to run this?**
Para a integração com SEFAZ, sim, um A1 e-CNPJ da ICP-Brasil. O mesmo A1 vale para
NFS-e, MDF-e and CT-e: these are different uses of the same certificate, not different certificates
diferentes.

**Como a autenticação funciona?**
O ERP autentica contra o Active Directory pelo Auth Service, e a autorização vem dos
directory groups. There is no local password in the ERP: who gets in is whoever the AD recognizes.

**Qual banco de dados usa?**
PostgreSQL 18 para dados relacionais, MongoDB para binários e imagens, MinIO para
arquivos grandes.

**Posso mudar a licença?**
A AGPLv3 é a licença atual. Alterar isso é decisão do titular do projeto, e precisa
passar por todos que já receberam o código sob AGPLv3.

**Por que a tela de cadastro de pessoas aparece vazia?**
Isso é frontend, não permissão. A tela abre o formulário e os campos não são
preenchidos; nenhuma configuração de grupo AD corrige.

## 🎉 Contributors

| Name | Função |
|------|--------|
| **Eurípedes Batista de Paiva Junior** | Desenvolvedor Principal |

---

## 🏆 Comparison with Major ERPs

| Feature | SAP | Sankya | **Brasil SaaS ERP** |
|----------------|-----|--------|---------------------|
| Multiempresa Nativo | ✅ | ✅ | ✅ |
| Fiscal Brasileiro | ⚠️ Complexo | ✅ | ✅ **Mais ágil** |
| IA Integrada | ❌ | ❌ | ✅ **Spring AI** |
| Código Aberto | ❌ | ❌ | ⚠️ Privado |
| Customização | ⚠️ Cara | ⚠️ Limitada | ✅ **Total** |
| Preço | $$$$ | $$$ | **$$** |
| Suporte Local | ⚠️ Terceiros | ✅ | ✅ **Direto** |
| Cloud-Native | ⚠️ Adaptação | ⚠️ Adaptação | ✅ **Nativo** |
| Observability | ✅ | ⚠️ | ✅ **Completa** |

---

<div align="center">

**Feito com ❤️ usando Spring Boot + React + PrimeReact**

🚀 **Enterprise Ready - Fiscalmente Conforme - Observável**

</div>


---

## 🔐 Authentication and Authorization

O Brasil SaaS ERP possui dois mecanismos de autenticação:

- **ERP users**: username + password validated with BCrypt.
- **PostgreSQL SUPERUSER**: username + password authenticated directly in PostgreSQL. The role must have rolsuper=true.

A regular PostgreSQL role does not replace the ERP user’s BCrypt password.

### PostgreSQL SUPERUSER

Any PostgreSQL SUPERUSER can authenticate into the ERP using its own credential. If there is not yet a userrio ERP correspondente, o sistema o provisiona automaticamente, garante o perfil ADMIN e emite as authorities:

- ROLE_ADMIN
- ROLE_SUPERADMIN

The PostgreSQL password is never stored in the ERP.

The application’s normal connection uses the technical identity configured in the datasource. A SUPERUSER’s authentication uses a temporary PostgreSQL connection with the identity the user supplied and SSL, without reusing the role’s client certificate técnica sa.

### Usuários com perfil GERENTE/DIRETORIA/USUARIO

The profile defines **authorization**, not authentication. These users keep depending on the BCrypt password registereda no ERP. Criar uma role PostgreSQL comum com o mesmo username não concede automaticamente acesso.

### Fluxo resumido

    POST /api/auth/login
            |
            +--> ERP user + BCrypt ----------------> JWT
            |
            +--> PostgreSQL SUPERUSER ----------------> ADMIN + SUPERADMIN + JWT

Documentação completa: docs/autenticacao.md

Arquitetura: docs/arquitetura.md

### Ecosystem Dependencies

## Corporate Authentication

Brasil SaaS ERP uses the BrasilCloud Auth Service for centralized authentication.

Auth Service:

https://github.com/euripedesdark/auth-service

License:

AGPLv3

---

## Auth Service Requirements

The Auth Service is designed to operate with:

- LDAP
- LDAPS
- Active Directory

Recommended environments:

### Linux

Samba Active Directory

Full guide:

https://medium.com/meetcyber/full-guide-deploying-samba-active-directory-on-fedora-and-rocky-with-hybrid-dns-and-gpo-support-30f47fab1e90

### Windows

Windows Server Active Directory with LDAPS enabled.

---

## Note

BrasilCloudERP can be evaluated and run independently of the full corporate environment deployment.

For production, using the Auth Service integrated with an LDAP/LDAPS directory is recommended.
