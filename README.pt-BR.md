# 🚀 Brasil SaaS  ERP - Sistema de Gestão Empresarial

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE.md)

**ERP Enterprise Multiempresa com IA Assistiva, Fiscal Completo e Arquitetura Modular**

Criado por: **Euripedes Batista de Paiva Junior**

---

## 📖 Sobre o Projeto

O **Brasil SaaS ERP** é um sistema de gestão empresarial completo, desenvolvido para atender empresas de todos os portes com uma solução robusta, segura e escalável. Migrado de Delphi para **Java Spring Boot 3.3.5** com **Oracle JDK 21**, o sistema oferece módulos financeiros, fiscais, de estoque, serviços e vendas com alta performance no tratamento de grandes volumes de dados.

### ✨ Destaques Enterprise

| Feature | Descrição |
|---------|-----------|
| 🏎️ **Alta Performance** | Migração de WebFlux para Spring MVC tradicional, resolvendo lentidão em tabelas com +400MB |
| 📊 **Paginação Inteligente** | Carregamento sob demanda de registros, ideal para bases de dados massivas |
| 🎨 **Frontend Moderno** | Interface reativa com PrimeReact 10.8 e React 19 |
| 📝 **Relatórios Profissionais** | Geração de PDFs com layout moderno usando OpenPDF 3.0.5 |
| 🌐 **API RESTful** | Backend robusto com endpoints paginados e filtros avançados |
| 🔒 **Segurança Multi-Tenant** | Autenticação JWT, autorização por permissão e isolamento multiempresa |
| 📦 **Armazenamento Híbrido** | PostgreSQL (dados relacionais) + MongoDB (binários/imagens) + MinIO (arquivos grandes) |
| 🤖 **IA Assistiva** | Spring AI + OpenAI integrado para assistência inteligente |
| 📡 **Observabilidade** | Actuator + Micrometer + Logback com MDC para rastreio fim-a-fim |
| 🚛 **Documentos de Transporte** | NFS-e SP, MDF-e e CT-e conectados à SEFAZ; SPED EFD ICMS/IPI gerando arquivo |
| 🧾 **Contrato único de resposta** | Um formato para o ERP ler, ajustado do lado do emissor. O motivo da recusa é persistido e sobrevive ao fechamento da tela |

---

## 🛠️ Stack Tecnológica Completa

### Backend (Núcleo)

| Camada | Tecnologia | Versão | Finalidade |
|--------|------------|--------|------------|
| **Linguagem** | Oracle JDK | 21 (LTS) | Base de longo suporte com records, pattern matching |
| **Framework** | Spring Boot | 3.3.5 | Autoconfiguração, Tomcat embutido, Actuator |
| **Web** | Spring MVC + Jackson | - | Camada REST (/api/**), JSON consistente |
| **Segurança** | Spring Security 6 + JWT | - | Autenticação stateless, @PreAuthorize |
| **ORM** | Spring Data JPA + Hibernate | 6.5+ | Persistência com ddl-auto: validate |
| **Migrations** | Flyway | Latest | Migrations versionadas (`V109`…`V126` no repo) |
| **Boilerplate** | Lombok + MapStruct | - | Menos código, mapeamento DTO↔entidade |
| **Validação** | Bean Validation (Jakarta) | - | Validação de entrada na borda da API |
| **Documentação** | springdoc-openapi | Latest | Swagger UI em /swagger-ui.html |

### Dados & Infraestrutura

| Componente | Tecnologia | Versão | Finalidade |
|------------|------------|--------|------------|
| **Banco Relacional** | PostgreSQL | 18 | Núcleo ACID com schema `brasil_saas` |
| **Criptografia** | mTLS (PKI) | - | ca.crt/sa.crt/sa.pk8 para conexão segura |
| **Documentos/Imagens** | MongoDB | Latest | Coleção `imagens` para binários/logos/fotos |
| **Cache** | Redis | Latest | Cache/sessões para tabelas quentes (NCM, municípios) |
| **Mensageria** | RabbitMQ | Latest | Eventos assíncronos (pedido → estoque/financeiro) |
| **Object Storage** | MinIO | Latest | Storage de objetos/arquivos grandes (hom/prod) |

### Fiscal (Diferencial Competitivo)

| Módulo | Tecnologia | Status |
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
`cStat 107` ("Serviço em Operação"). Isso prova a cadeia TLS, o certificado do
cliente e o envelope SOAP. **Não prova o XML do documento** — nenhum MDF-e nem
CT-e foi emitido ainda, e a montagem do MDF-e não está implementada.

Detalhe em
[`docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`](docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md).

**Prazo que não pode ser ignorado:** a NT 2026.001 torna o grupo `infCIOT`
obrigatório no MDF-e rodoviário a partir de **23/11/2026** (rejeição
`cStat 684`). O `infCIOT` já existe no XSD; falta a validação no emissor.

#### NFS-e São Paulo: proxy com failover entre duas implementações

A NFS-e de São Paulo não é atendida por uma única implementação. O ERP fala
sempre com a **4567**, e um proxy decide quem atende:

```
ERP  ──►  4567  nfse-failover.rb  (proxy, health check periódico)
              ├──►  4568  nfse-sp-api      Java   PRIMÁRIA
              └──►  4569  nfse-sp-bridge   Ruby   FALLBACK
                        (prefeitura de São Paulo, via A1)
```

| Peça | Papel |
|---|---|
| **4567 — `nfse-failover.rb`** | Proxy. Tenta a Java, cai para a Ruby, health check a cada 10s |
| **4568 — `nfse-sp-api`** | Implementação **primária**. Valida o XML contra o schema **antes** de mandar |
| **4569 — `nfse-sp-bridge`** | Implementação **fallback**, em Sinatra. Assume quando a Java falha |

O ERP descobre quem atendeu pelo header `X-Backend` que o proxy devolve.

**Por que duas implementações, e não uma:** a Ruby foi escrita primeiro e é a
que validou as regras contra a prefeitura de São Paulo de verdade — emitiu e
cancelou NFS-e em produção, com as mensagens literais da prefeitura
registradas em
[`src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md`](src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md).
A Java reimplementou o mesmo contrato com o mesmo escopo, e a comparação dos
XMLs entre as duas foi o que pegou os bugs de assinatura e de namespace do lado
Java. A Ruby continua de pé **de propósito**: é o oráculo contra o qual a Java é
conferida.

A ordem não é decorativa. A primária é a que valida contra o schema; se fosse
invertida, uma implementação quebrada emitiria com um contrato errado e o ERP
só descobriria depois que a prefeitura recusasse.

**O que o proxy troca, e o que ele não troca.** O nginx troca de upstream quando
a conexão falha. Ele **não** troca quando a implementação responde `200` com o
contrato errado — e é esse o caso que importa: em 26/09/2026, com a API Java
fora, a Ruby emitiu a nota 29 com `success=true`, a prefeitura aceitou, e o ERP
respondeu erro porque lia `"sucesso"` onde a Ruby escrevia `success`. Por isso
existe o watchdog, que observa a resposta e não só a porta.

A 4567 é o default do código, não um número escolhido:

```java
// NfseEmissaoService.java
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}")
```

O certificado A1 é o e-CNPJ da empresa, e é **guardado no MongoDB** — a coleção
`documentos` tem um documento `tipoEntidade: "certificado_digital"` com o `.pfx`
íntegro. É por isso que o A1 chega junto na restauração do dump. O `.pfx` foi
emitido com **RC2-40-CBC**, que o OpenSSL 3 tirou do provider padrão: o Java
aceita, e o Ruby precisa do provider legacy (`OPENSSL_MODULES`). A reemissão
com AES resolveria de vez.

### Observabilidade & Operação

| Ferramenta | Finalidade |
|------------|------------|
| **Actuator + Micrometer** | /actuator/health\|metrics para monitoramento real |
| **Logback + MDC** | Logs com traceId, empresaId, usuarioId; prod em JSON p/ Loki |
| **Scripts Shell** | test_db_connection.sh, test_database.sh, test_erp_operations.sh |

### Frontend React ✅ COMPLETO

**LOCALIZAÇÃO**: `src/main/resources/static/react/`

| Componente | Quantidade | Status | Descrição |
|------------|------------|--------|-----------|
| **Componentes React** | 25 arquivos JSX | ✅ Completo | Componentes por módulo |
| **Serviços API** | 11 arquivos JS | ✅ Completo | Integração REST |
| **Contextos** | 1 (Auth) | ✅ Completo | Gerenciamento de sessão |
| **Build Produzido** | ~2.2MB | ✅ Otimizado | Em `static/dist/` |

**Tecnologias**: React 19, PrimeReact 10.8, React Router 7, Axios, Vite 5

**Funcionalidades**:
- ✅ Autenticação JWT com refresh automático
- ✅ 25 componentes implementados (3,762 linhas)
- ✅ Módulo Produção completo (ordens de produção)
- ✅ Design responsivo com PrimeReact
- ✅ Integração total com backend Spring Boot
- ⚠️ 4 componentes placeholder (Compras, Estoque, Vendas, Serviços)

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

## 📦 Módulos do Sistema

O sistema tem **22 módulos de negócio** sobre um núcleo comum
(`core`), todos com controller, service, repository e tela.
Números verificados em **03/10/2026** no código:

| | |
|---|---|
| Classes Java | **762** |
| Repositórios Spring Data | **174** |
| Services | **147** |
| Endpoints REST | **451** |
| Tabelas no PostgreSQL (schema `brasil_saas`) | **204** |
| Componentes React | **123** |
| Migrations Flyway no repo | **15** (`V109`…`V126`) |

### 💰 Financeiro

> **Status**: ✅ implementado e respondendo (`/api/financeiro/*` → 200)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Contas a Pagar/Receber** | Títulos, parcelas, fluxo de aprovação multinível, alertas de vencimento |
| ✅ **Baixas** | Com descontos, juros, multas e estornos |
| ✅ **Tesouraria** | Caixa, projeção de fluxo de caixa, empréstimos e aplicações financeiras |
| ✅ **Contabilidade Geral** | Lançamentos e partidas contábeis, plano de contas, centros de custo |
| ✅ **Bancos** | Contas bancárias, extrato, conciliação bancária, remessas e retornos (CNAB/OFX) |
| ✅ **Orçamentos** | Orçado vs realizado |
| ✅ **Comissões** | Regras de comissão sobre vendas e OS |
| ✅ **Renegociação** | Renegociação de títulos |
| ✅ **Provisão PDD** | Provisão para devedores duvidosos |
| ✅ **Análise de Rentabilidade** | Por centro de custo, plano de contas e período |

**Endpoints**: `/api/financeiro/titulos`, `/lancamentos`, `/orcamentos`,
`/emprestimos`, `/planos-contas`, `/centros-custo`, `/contas-bancarias`,
`/caixas`, `/condicoes-pagamento`, `/tipos-pagamento`, `/conciliacao`,
`/extrato`, `/comissoes`, `/renegociacao`, `/remessas`, `/retornos` …
(55 endpoints no módulo)

---

### 🏙️ Cadastros

| Cadastro | Funcionalidades |
|----------|-----------------|
| ✅ **Municípios** | Tabela oficial IBGE, busca por código ou nome, CEP |
| ✅ **Pessoas** | Cadastro único PF/PJ, contatos, endereços |
| ✅ **Clientes / Fornecedores** | Limite de crédito, logo, avaliação, histórico |
| ✅ **Produtos** | Variações, kits, NCM, imagens (MongoDB), e-commerce |
| ✅ **Serviços** | Para NFS-e, integração com OS |
| ✅ **Categorias / Marcas / Unidades de Medida / Transportadoras** | Apoio ao cadastro de produtos e fretes |

**Endpoints**: `/api/cadastro/produtos`, `/clientes`, `/fornecedores`,
`/pessoas`, `/servicos`, `/categorias`, `/marcas`, `/unidades-medida`,
`/transportadoras` + `/api/municipios` (31 endpoints no módulo)

---

### 📋 Serviços e Vendas

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Ordem de Serviço** | Emissão, itens, apontamentos, PDF, emissão de NFS-e integrada |
| ✅ **Pedidos de Venda** | Integração com estoque, financeiro e fiscal |
| ✅ **PDV** | Tela de ponto de venda |
| ✅ **Tabelas de Preço** | Itens por produto |
| ✅ **Bonificações e Devoluções** | Com itens |
| ✅ **Contratos de Venda** | Com itens e regras de comissão |

**Endpoints**: `/api/servicos/os`, `/api/vendas/pedidos`,
`/api/vendas/tabelas-preco` … (15 endpoints nos dois módulos)

---

### 🛒 Compras e Supply Chain

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Pedidos de Compra** | Com itens, integração com fornecedores |
| ✅ **Recebimentos** | Conferência de itens |
| ✅ **Conferência de Faturas** | Validação de fatura contra o recebido |
| ✅ **Supply Chain** | Solicitações de compra e cotações com mapa de fornecedores |
| ✅ **Contratos de Compra** | Com itens |

**Endpoints**: `/api/compras/pedidos`, `/recebimentos`,
`/supply-chain/solicitacoes`, `/supply-chain/cotacoes` … (12 endpoints)

---

### 📦 Estoque

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Depósitos e Endereços** | Ocupação por endereço |
| ✅ **Saldos, Lotes e Séries** | Rastreabilidade |
| ✅ **Movimentações** | Entradas, saídas, ajustes |
| ✅ **Reservas e Transferências** | Entre depósitos |
| ✅ **Inventários** | Com itens |
| ✅ **Expedições** | Com itens |

**Endpoints**: `/api/estoque/depositos`, `/saldos`, `/movimentacoes`,
`/lotes`, `/reservas`, `/transferencias`, `/inventarios`, `/expedicoes` …
(17 endpoints)

---

### 🏭 Produção Industrial (PCP)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Ordens de Produção** | Fluxo completo: insumos → processo → produto final |
| ✅ **Estrutura de Produto (BOM)** | Por produto pai |
| ✅ **Roteiros e Operações** | Sequência de operações |
| ✅ **Centros de Trabalho e Capacidade** | Agendamento por capacidade |
| ✅ **Apontamentos** | Por produção, funcionário, período e status; estatísticas |
| ✅ **Romaneios** | Com itens |
| ✅ **MRP** | Planejamento de necessidades |

**Endpoints**: `/api/producao/estruturas`, `/roteiros`, `/centros-trabalho`,
`/capacidade`, `/apontamentos`, `/romaneios`, `/mrp` … (30 endpoints)

---

### 📊 Contabilidade

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Lançamentos e Partidas** | Contabilidade em partidas dobradas |
| ✅ **Balancete, Balanço, DRE, Razão** | Relatórios contábeis |
| ✅ **Fechamentos** | Fechamento de período |

**Endpoints**: `/api/contabilidade/lancamentos`, `/balancete`, `/balanco`,
`/dre`, `/razao`, `/fechamentos` (15 endpoints)

---

### 🏢 Ativos (Gestão de Ativos)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Ativo Imobilizado** | Cadastro de bens |
| ✅ **Manutenções** | Histórico de manutenção dos ativos |

**Endpoints**: `/api/ativos/manutencoes` (4 endpoints)

---

### 📁 DMS (Gestão de Documentos)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Documentos** | Com conteúdo, versões e aprovações |
| ✅ **Retenção** | Política de retenção |

**Endpoints**: `/api/dms/documentos`, `/retencao`, `/versoes/{id}/download` (9 endpoints)

---

### ✅ Qualidade

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Planos de Inspeção** | Planos por produto/processo |
| ✅ **Inspeções** | Registro de inspeção |
| ✅ **Não-conformidades** | Tratamento de NC |

**Endpoints**: `/api/qualidade/planos`, `/inspecoes`, `/nao-conformidades` (7 endpoints)

---

### 📈 Projetos

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Projetos** | Resumo, etapas, movimentos |
| ✅ **Faturamento** | Faturamento por projeto |
| ✅ **Riscos e Mudanças** | Registro de riscos e mudanças |

**Endpoints**: `/api/projetos`, `/projetos/{id}/resumo`, `/etapas`,
`/movimentos`, `/faturamentos`, `/riscos`, `/mudancas` (14 endpoints)

---

### 🏗️ WMS (Warehouse)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Ondas de Picking** | Com itens |
| ✅ **Volumes** | Com itens |
| ✅ **Putaway** | Endereçamento de carga |

**Endpoints**: `/api/wms/ondas`, `/volumes`, `/putaway` (14 endpoints)

---

### 🔄 Workflow

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Definições** | Com estágios |
| ✅ **Instâncias e Tarefas** | Tarefas pendentes por usuário |

**Endpoints**: `/api/workflow/definitions`, `/instances`, `/tasks/pendentes` (10 endpoints)

---

### 🌐 Portais

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Acessos** | Controle de acesso a portais |
| ✅ **Validação Pública** | Validação e "minha conta" sem login do ERP |

**Endpoints**: `/api/portais/acessos`, `/publico/validar`, `/publico/minha-conta` (5 endpoints)

---

### 👥 RH

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Funcionários** | Vínculo com pessoa, cargo, foto |
| ✅ **Cargos** | Estrutura de cargos |
| ✅ **Folha de Pagamento** | Com itens |

**Endpoints**: `/api/rh/funcionarios`, `/cargos`, `/folhas` (14 endpoints)

---

### 🤝 CRM

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Leads e Oportunidades** | Pipeline de vendas |
| ✅ **Atividades e Tarefas** | Acompanhamento |
| ✅ **Forecast** | Previsão de vendas |

**Endpoints**: `/api/crm/leads`, `/pipeline`, `/forecast`, `/atividades` (10 endpoints)

---

### 📊 BI (Inteligência de Negócios)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Dashboards** | Públicos, por tipo e por usuário, com widgets |
| ✅ **KPIs e Indicadores** | Cálculo de KPI por tipo |
| ✅ **Relatórios** | Por categoria, exportação **PDF, Excel e CSV** |
| ✅ **Relatórios Agendados** | Por frequência, fila de pendentes |
| ✅ **Reports com Parâmetros** | Relatórios parametrizados |

**Endpoints**: `/api/bi/dashboards`, `/kpis`, `/indicadores`, `/relatorios`,
`/relatorios-agendados`, `/reports` … (46 endpoints — o maior módulo)

---

### 🤖 IA Assistiva

| Recurso | Status |
|---------|--------|
| ✅ **Chat com IA** | Sessões e mensagens persistidas |
| ✅ **Assistente ERP** | Com auditoria de uso |
| ✅ **Embeddings** | Busca semântica por entidade |
| ✅ **Classificações e Análises Preditivas** | Modelos treináveis |
| ✅ **Prompts e Templates** | Biblioteca reutilizável |
| ⚠️ **Provedor** | Configurável (`app.ia.provider`, padrão `openai`); precisa de chave válida |

**Endpoints**: `/api/ia/config`, `/sessoes`, `/mensagens`, `/prompts`,
`/prompt-templates`, `/embeddings`, `/classificacoes`, `/analises` …
(74 endpoints)

---

### 🔐 Core, Auth e Superadmin

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Autenticação** | JWT, login em banco, login AD (SPNEGO), refresh token |
| ✅ **Usuários, Perfis e Permissões** | Autorização por permissão (`@PreAuthorize`) |
| ✅ **Multiempresa** | Usuário ↔ empresa(s), isolamento por empresa |
| ✅ **Superadmin** | Catálogo SQL, módulos por usuário, perfis disponíveis |
| ✅ **Auditoria** | Log de acesso, notificações, sessões |

**Endpoints**: `/api/auth/login`, `/login/database`, `/login/ad`, `/refresh`,
`/me` + `/api/core/perfil`, `/core/minha-empresa` + `/api/superadmin/usuarios`,
`/superadmin/sql/catalogo` … (33 endpoints)

---

### 🖨️ Relatórios

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Relatórios por Tipo** | HTML e PDF (`/api/relatorios/{tipo}`, `/api/relatorios/pdf/{tipo}`) |

---

### 📄 Documentos (shared)

| Funcionalidade | Descrição |
|----------------|-----------|
| ✅ **Conteúdo de Documento** | `/api/documentos/{id}/conteudo` — arquivos gravados no MongoDB |

---

## 🗂️ Estrutura do Projeto

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
│   └── ia/           # IA Assistiva (0 classes) - Porta 8089 ⚠️ Estrutura vazia
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

## 🚀 Como Subir a Aplicação

### Pré-requisitos

| Dependência | Versão Mínima | Obrigatório |
|-------------|---------------|-------------|
| ☕ Oracle JDK | 21 (LTS) | ✅ Sim |
| 📦 Apache Maven | 3.9+ | ✅ Sim |
| 🟢 Node.js | 22+ LTS | ✅ (frontend) |
| 🐘 PostgreSQL | 18+ | ✅ Sim |
| 🍃 MongoDB | Latest | ✅ (imagens) |
| 🔧 Git | Latest | ✅ Sim |

### 🐧 Instalação no Linux (Ubuntu/Debian/CentOS/Fedora)

```bash
# Tornar script executável
chmod +x installbase.sh

# Executar instalação automatizada
sudo ./installbase.sh
```

**O script irá:**
1. Detectar distribuição Linux
2. Instalar JDK 21 via SDKMAN
3. Instalar Maven e Node.js
4. Configurar PostgreSQL e MongoDB
5. Gerar a PKI Easy-RSA e configurar o mTLS do PostgreSQL
6. **Instalar as CAs da ICP-Brasil e montar o truststore JKS** (MDF-e e CT-e)
7. Clonar dependências Maven
8. Build automático do frontend React

O passo 6 não é opcional. A SVRS apresenta certificado da ICP-Brasil
(`AC SERPRO SSLv1`, vindo da `Raiz Brasileira v10`), e num Debian limpo nenhuma
dessas duas está no bundle de CAs: sem elas o handshake morre com
`PKIX path building failed`. O script deixa:

```
/etc/brasil-saas/certs/truststore-sefaz.jks   644   só CA pública
/etc/brasil-saas/sefaz.env                   644   caminho e UF, sem segredo
/etc/brasil-saas/cert.env                    600   o A1 e as senhas
```

O truststore é separado do truststore do sistema **de propósito**: o ERP é Java
e a biblioteca fiscal lê um JKS próprio. Sem montá-lo, o erro volta idêntico e
parece que as CAs não entraram.

O `cert.env` precisa ser preenchido com o A1 do emitente:

```bash
BRASIL_SAAS_MDFE_CERTIFICADO=/caminho/do/a1.pfx
BRASIL_SAAS_MDFE_CERT_PASS=...
```

**O mesmo A1 serve para NFS-e, MDF-e e CT-e**, desde que seja e-CNPJ A1 da
ICP-Brasil. Não são certificados diferentes: são usos diferentes do mesmo.

### Scripts de operação

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
histórico se perdeu, e um `clone` traz o HEAD de hoje, que pode ser outra versão
do material com que o ERP foi testado. O hash é o que diz se o que baixou é o
mesmo, e o script **diz que não bate** em vez de deixar material errado entrar
em silêncio.

### 🪟 Instalação no Windows

```powershell
# Executar PowerShell como Administrador
.\install-sysfluxo.ps1
```

**O script irá:**
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

## 🌐 Acessando o Sistema

| URL | Descrição |
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

## 📊 Percentual de conclusão do fiscal

**Critério usado**, para o número não ser opinião. Cada documento fiscal precisa
de 6 entregáveis:

| # | Entregável |
|---|-----------|
| 1 | **Conexão com a SEFAZ/prefeitura** respondendo |
| 2 | **Montagem do documento** (o XML: emitente, veiculo, motorista, LAC) |
| 3 | **Assinatura** com A1 ICP-Brasil |
| 4 | **Envio e protocolo** |
| 5 | **Consulta / cancelamento** |
| 6 | **Tela** com o retorno da prefeitura |

| Módulo | 1 | 2 | 3 | 4 | 5 | 6 | % |
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

- **MDF-e e CT-e estão em 25% e 17%, não em 100%.** A conexão funciona e a
  SEFAZ responde `cStat 107` com o seu A1 — isso é o entregável 1, o mais
  difícil, porque eram 4 camadas entre o ERP e a resposta (A1, CAs da
  ICP-Brasil, truststore JKS, mutual TLS). **O que falta é o entregável 2**, a
  montagem do documento, e ele é o trabalho maior. `cStat 107` é o *status do
  serviço*: não diz nada sobre o XML, porque nenhum XML foi montado.
- **SPED EFD ICMS/IPI em 40%** e não em 100% porque gera arquivo válido, com
  os contadores conferidos, mas lê o cabeçalho do corpo da requisição em vez
  do banco, e não tem tela. Faltam os entregáveis 2 e 5.
- **NF-e em 0%.** Não é exagero: os 3 métodos da interface `NFeService` estão
  como `TODO` em `NFeServiceImpl`. A interface existe, a implementação não.
- **Consulta/DistDFe em 100%** de 6 em 6.

O % é do **escopo fiscal**, não do sistema. Os demais módulos têm medição própria,
abaixo.

## 📊 Demais módulos — estrutura e o que responde

### Por que não há um % único aqui

**Contar arquivo não mede prontidão.** Medi os módulos pelos mesmos
6 entregáveis do fiscal e quase todos dão 100% estrutural — porque
todos têm model, repository, service, controller e tela. Isso não
significa que o cadastro de cliente abre, que o título baixa ou que
a folha calcula. Significa que os arquivos existem.

O que mede é executar. Então há duas colunas:

| Módulo | Tabelas | Endpoints | Tela | Estrutural | Responde (03/10/2026) |
|--------|:-------:|:---------:|:----:|:----------:|:----------------------:|
| Cadastro | 21 | 31 | ✅ | 6/6 | ✅ |
| Financeiro | 32 | 55 | ✅ | 6/6 | ✅ |
| Estoque | 13 | 17 | ✅ | 5/6 | ✅ |
| Fiscal | 30 | 28 | ✅ | 6/6 | ⚠️ parcial (ver % fiscal) |
| Vendas | 11 | 7 | ✅ | 6/6 | ✅ |
| Compras | 11 | 12 | ✅ | 6/6 | ✅ |
| Produção | 10 | 30 | ✅ | 6/6 | ✅ |
| RH | 4 | 14 | ✅ | 6/6 | ✅ |
| Serviços | 3 | 8 | ✅ | 6/6 | ✅ |
| BI | 8 | 46 | ✅ | 6/6 | ✅ |
| Core/Auth | 18 | 33 | ✅ | 6/6 | ✅ |
| IA | 11 | 74 | ⚠️ 1 tela | 6/6 | ⚠️ parcial |
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

**Estrutural** = os 6 entregáveis existem como arquivo.
**Responde** = a API respondeu num smoke test com token válido em
**03/10/2026**.

⚠️ **Nenhum destes foi validado funcionalmente.** Abri a tela, não.
Criei registro, não. O que o smoke test prova é que a **rota
responde**; não prova que o dado devolvido esteja certo. As duas
coisas são diferentes.

### O que o smoke test achou (03/10/2026)

67 rotas chamadas, uma ou mais por módulo:

```
62  2xx   respondem
 5  400   parâmetro obrigatório faltando (a rota existe)
 0  500   nenhum erro
```

Os 400 são comportamento esperado de validação
(`/api/bi/indicadores/dashboard`, `/api/ia/config`, `/api/ia/sessoes`,
`/api/producao/apontamentos/por-status`, `/api/producao/apontamentos/por-periodo`,
`/api/contabilidade/balancete`, `/api/fiscal/sefaz/status`,
`/api/wms/putaway`) — pedem um parâmetro que o teste não passou.

### O bug do RH foi corrigido

O smoke test de 26/09/2026 achou um **500** real:
`GET /api/rh/funcionarios` quebrava com
`LazyInitializationException` — `Funcionario` tem
`@ManyToOne(fetch = LAZY)` para `Cargo`, e o Jackson serializava o
proxy depois de a sessão do Hibernate fechar.

**Em 03/10/2026 está corrigido:** `/api/rh/funcionarios/pessoas`,
`/api/rh/cargos` e `/api/rh/folhas` respondem **200**. A correção
foi buscar com `join fetch` no `Cargo` (ou DTO), o que evita o
serializer tocar no proxy fora da sessão.

### Estoque não tem camada de service

9 controllers, 12 repositories, **0 services**. A regra de negócio
está dentro do controller, falando direto no repository. Funciona,
mas não tem onde testar e não isola a regra. Nos outros módulos a
separação existe — é uma inconsistência a decidir, não um acidente.

## ✅ O que está rodando

Verificado em **03/10/2026** via `systemctl` e `ss`.

| Serviço (systemd) | Porta | Estado |
|-------------------|-------|--------|
| `brasil-saas-erp.service` (Spring Boot, user `brasilsaas`) | 8080 | 🟢 running |
| `nfse-sp-api.service` (Java, primária) | 4568 | 🟢 running |
| `nfse-sp-bridge.service` (Ruby, fallback) | 4569 | 🟢 running |
| `brasil_saas-watchdog.service` (checagem de contrato) | — | 🟢 running |
| `brasil_saas-minio.service` | 9000 / 9001 | 🟢 running |
| `auth-service.service` | 8081/8082 | 🟢 running |
| PostgreSQL (schema `brasil_saas`) | 5432 | 🟢 up |
| MongoDB | 27017 | 🟢 up (4 coleções) |
| RabbitMQ | 5672 / 15672 | 🟢 up |
| Redis | 6379 | 🟢 up |
| nginx (proxy do domínio) | 80 / 443 | 🟢 up |

O ERP roda do jar
`/opt/brasil-saas-erp/brasil-saas-erp-1.0.0-SNAPSHOT.jar`
(unit `brasil-saas-erp.service`). As microserviços de NF-e rodam dos
jars dentro de `src/main/resources/microservices/` — **atenção**: no
servidor, os jars ativos estão em
`/home/euripedes/BrasilCloudERP/src/main/resources/microservices/`
(outro checkout), não neste repo.

### ⚠️ O proxy failover da NFS-e (4567) está fora do ar

O ERP chama a emissão de NFS-e em
`brasil-saas.fiscal.nfse.url`, que por padrão é
`http://127.0.0.1:4567/api/nfse-sp` — o **proxy failover**. Em
03/10/2026 **nada escuta na 4567** (conexão recusada), embora as
duas implementações (4568 Java, 4569 Ruby) e o watchdog estejam de
pé.

Consequência: a menos que `/etc/brasil-saas/erp.env` (arquivo do
usuário `brasilsaas`, ilegível sem sudo) sobrescreva a URL para a
4568, **a emissão de NFS-e está quebrada agora**. Evidência: a última
NFS-e no banco é de **26/09/2026** (nota 33), com 4 registros
`FALHA_EMISSAO` naquele dia e nenhuma tentativa desde então.

Ação: subir o proxy da 4567 (ou apontar `brasil-saas.fiscal.nfse.url`
diretamente para `http://127.0.0.1:4568/api/nfse-sp`), e registrar
no `erp.env` qual URL está em uso.

### O que o ERP valida sozinho

| Verificação | Resultado (03/10/2026) |
|-------------|------------------------|
| `GET /api/fiscal/mdfe/status` → SVRS | **200** (`cStat 107`) |
| `GET /api/fiscal/cte/status` → SEFAZ SP | **200** (`cStat 107`) |
| `GET /api/fiscal/sped/efd/exemplo` | **200** (arquivo válido) |
| `GET /api/fiscal/nfse/retornos/recusas` e `/para-conferir` | **200** |

O MDF-e é atendido pela **SVRS** (`ufAtendente: RS`) e o CT-e pelo
**portal de SP** (`ufAtendente: SP`), com o **mesmo A1**. São
autorizadores diferentes, não certificados diferentes.

### O que ainda não está pronto

| Item | Situação |
|------|----------|
| **Proxy failover 4567** | Fora do ar; padrão da URL do ERP. Ver acima. |
| **NF-e / NFC-e** | `NFeServiceImpl` tem **3 TODOs**. A interface promete `emitirNFe`, `cancelarNFe` e `consultarSituacao`; nenhuma das três é implementada. |
| **Emissão de MDF-e** | A conexão com a SVRS está pronta (`cStat 107`) e o certificado assina. **A montagem do documento não existe**: `MdfeEmissaoService` tem só `statusServico` e `consultarRecibo`. |
| **Emissão de CT-e** | Idem. `CteEmissaoService` tem só `statusServico`. |
| **SPED EFD Contribuições** | A biblioteca resolve no `pom.xml`, mas **não há endpoint** — só o ICMS/IPI tem. |
| **IA (Spring AI)** | Módulo presente (74 endpoints), com embeddings, chat e classifications. Não foi validado funcionalmente; precisa de chave de API válida. |
| **Tela da IA** | 1 tela só (`IaAssistWidget`); os 74 endpoints não têm tela equivalente. |
| **Validação do CIOT** | `cStat 684` entra em produção na SEFAZ em **23/11/2026**. O `infCIOT` já está no XSD; falta a validação no emissor. |

**Por que o MDF-e e o CT-e não estão na lista de "prontos" apesar de
responderem `cStat 107`:** o `cStat 107` é o *status do serviço*, e
prova que a cadeia TLS, o certificado do cliente e o envelope SOAP
estão certos. Ele **não prova que o XML do documento esteja**, porque
nenhum documento foi montado nem enviado. São duas coisas diferentes,
e o README separa por isso.

---

## 📡 Endpoints da API

O backend expõe **451 endpoints REST** (`/api/**`). A lista
completa está em [`docs/INDICE.md`](docs/INDICE.md) e no
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

A emissão é feita por uma API separada. O ERP chama
`brasil-saas.fiscal.nfse.url` (padrão
`http://127.0.0.1:4567/api/nfse-sp`, o proxy failover —
veja o aviso em "O que está rodando"). As implementações
ativas são a **Java na 4568** (primária) e a **Ruby na 4569**
(fallback), vigiadas pelo `brasil_saas-watchdog`, que checa o
**contrato** de resposta, não só a porta.

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

O motivo da recusa da prefeitura é gravado **antes** de a
exceção subir, com o `cStat` e o corpo bruto no Mongo. A
tela de erro é uma parede; o registro não.

### Fiscal — MDF-e e CT-e

```
GET    /api/fiscal/mdfe/status                     cStat 107 se a SVRS estiver de pé
GET    /api/fiscal/mdfe/recibo?numero={recibo}     2ª chamada: do recibo para a chave
GET    /api/fiscal/cte/status                      cStat 107 se a SEFAZ estiver de pé
```

Os dois usam o mesmo contrato de resposta, com os **três
estados** de `sucesso`: `true` (autorizado), `false`
(recusado, com `cStat` e motivo) e `null` (**não deu para
saber**). O terceiro estado existe para não duplicar
documento: a SVRS rejeita chave natural repetida, e reemitir
no escuro é exatamente o que cria a duplicidade.

O MDF-e devolve **recibo** na primeira chamada e
**protocolo** na segunda. Ler a chave na resposta do envio
dá NPE — o método não existe.

### Fiscal — SPED EFD ICMS/IPI

```
POST   /api/fiscal/sped/efd/gerar                  gera o arquivo EFD
GET    /api/fiscal/sped/efd/exemplo                gera o de exemplo, para conferir o formato
```

**EFD não se envia para ninguém.** O arquivo é gerado,
assinado e guardado; quem busca depois é a SEFAZ ou a
Receita. Sem web service, sem protocolo, sem fila.

### Fiscal — consulta e distribuição

```
GET    /api/fiscal/sefaz/status
GET    /api/fiscal/sefaz/consultar
GET    /api/fiscal/sefaz/distribuicao
```

### Fiscal — tabelas e cadastros

```
GET    /api/fiscal/cest?busca={termo}              busca por código, descrição ou NCM
GET    /api/fiscal/cfop?tipoOperacao=ENTRADA|SAIDA
GET    /api/fiscal/ncm
GET    /api/fiscal/ncm/{codigo}
GET    /api/fiscal/issqn
GET    /api/fiscal/impostos
GET    /api/fiscal/certificados
```

`GET /api/cadastro/servicos?nome={termo}` alimenta o
autocomplete do nome do serviço, que sugere serviço já
cadastrado enquanto a pessoa digita. Dois cadastros com nome
parecido e código municipal diferente é a causa mais comum de
recusa na prefeitura.

### Demais módulos (raízes)

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

## 🎯 Funcionalidades Especiais

### 🔍 Busca Dinâmica de Municípios
Digite o código IBGE ou nome e o sistema localiza instantaneamente.

### 📄 Paginação Inteligente
Todas as telas exibem máx. 20 linhas, com navegação entre páginas.

### 🌳 Lazy Loading
Dados carregados sob demanda, reduzindo consumo de memória.

### 📊 Dashboard Financeiro
Visualize totais por status (Aberto, Pago, Atrasado) em tempo real.

### 🖨️ Relatórios Profissionais
PDFs com layout moderno, cores diferenciadas e formatação empresarial.

### 🔐 Hierarquia de Permissões

| Perfil | Acesso |
|--------|--------|
| **SuperAdmin** | Completo, incluindo gerenciamento de administradores |
| **Diretor** | Todas funcionalidades exceto gerenciar administradores |
| **Gerente** | Todas funcionalidades operacionais |
| **Usuário** | Limitado às permissões específicas |

---

## ⚙️ Configurações Avançadas

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

## 🧪 Scripts de Teste e Validação

| Script | Finalidade |
|--------|------------|
| `test_db_connection.sh` | Testa conexão PostgreSQL |
| `test_database.sh` | Valida schema e migrations |
| `test_erp_operations.sh` | Smoke test de operações ERP |
| `check_mongodb.sh` | Verifica conexão e coleção MongoDB |

---

## 🔒 Segurança

| Recurso | Descrição |
|---------|-----------|
| 🔐 **Senhas Criptografadas** | BCrypt no banco de dados |
| 🛡️ **Filtro de Autenticação** | Em todas as rotas API e web |
| 🔑 **Tokens JWT** | Com expiração configurável |
| 🚫 **SQL Injection** | Protegido via JPA/Hibernate |
| 🌐 **CORS Configurado** | Para APIs externas |
| 📁 **Upload Seguro** | Validação de tipo e tamanho (10MB) |
| 🔐 **mTLS** | Conexão PostgreSQL criptografada com PKI |

---

## 🐛 Solução de Problemas

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
- ✅ Build já foi executado (2.2MB em `static/dist/`)
- ✅ 25 componentes React compilados
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

## 📂 Arquivos Não Referenciados / Backup

Os seguintes arquivos e diretórios são backups ou não estão em uso ativo:

| Tipo | Arquivo/Diretório | Descrição |
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

## 📄 Licença

Este projeto está sob a **GNU Affero General Public License v3.0 (AGPLv3)**.

O texto completo, inalterado, está em [`LICENSE.md`](LICENSE.md).

A AGPLv3 exige que o código-fonte seja oferecido a quem usa o programa, inclusive
quando o uso é **pela rede** — por isso o "Affero". Para um ERP acessado por
navegador, essa cláusula é a que importa: quem aponta o navegador para este
sistema tem direito ao código.

Os componentes de terceiros distribuídos em `src/main/resources/microservices/`
conservam suas próprias licenças (MIT, Apache-2.0 e BSD), todas compatíveis com a
AGPLv3. Os arquivos `LICENSE` originais de cada componente permanecem no lugar e
não são substituídos por este.

© 2026 Brasil SaaS ERP

---

---

---

## ❤️ Apoie o Projeto

Brasil SaaS ERP é um ERP Open Source mantido por um único desenvolvedor.

Se este projeto ajudou você, sua empresa ou sua equipe, considere apoiar o desenvolvimento.

**PIX:**

```
24adc62c-b073-4587-974d-03fe35f6733f
```

Uma doação de qualquer valor paga servidor, certificado digital e as taxas de emissão
da prefeitura. Ela não compra garantia de prazo em nenhuma issue.

### 💳 Transferência internacional

A chave PIX não funciona fora do Brasil. Para doar de fora, use transferência bancária.

**Se você envia de um banco dos Estados Unidos**, pode usar estes dados para uma
transferência doméstica. **Se envia de qualquer outro lugar**, faça uma transferência
internacional Swift.

| | |
|---|---|
| **Nome** | Euripedes Batista de Paiva Junior |
| **Tipo de conta** | Checking |
| **Routing number** (para wire e ACH) | `101019628` |
| **Número da conta** | `215822927677` |
| **Nome e endereço do banco** | Wise US Inc, 108 W 13th St, Wilmington, DE, 19801, United States |
| **SWIFT/BIC** | `TRWIUS35XXX` |

O `Routing number` só é usado quando o dinheiro sai dos Estados Unidos. Fora de lá,
o campo é o `SWIFT/BIC`.

---

## 📬 Suporte

Para dúvidas, sugestões ou reporte de bugs:

- 📧 **Email**: euripedesdark@gmail.com
- 🔗 **GitHub Issues**: https://github.com/euripedesdark/BrasilCloudERP/issues
- 📖 **Documentação**: `/docs/`

---

## 🤝 Como Contribuir

O projeto está sob AGPLv3 e aceita contribuições.

1. **Abra uma issue** antes de escrever código, descrevendo o problema ou a melhoria.
   Sem issue beforehand, a mudança pode ir para um caminho que ninguém usa.
2. **Crie uma branch** com nome descritivo: `minha-melhoria`.
3. **Commite por tema.** Um commit faz uma coisa; um commit com seis coisas não pode
   ser revertido sozinho.
4. **Rode os testes** antes de abrir o PR: `mvn test`.
5. **Abra o Pull Request** contra a `main` e descreva o que muda e por quê.

```bash
git clone https://github.com/euripedesdark/BrasilCloudERP.git
cd BrasilCloudERP
mvn test
```

### O que não mexer sem issue

| Área | Por quê |
|---|---|
| Banco de dados | Um `ALTER` errado em produção não tem rollback |
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
Não, apesar de responderem `cStat 107`. Esse código é o *status do serviço*: prova que
o TLS, o certificado do cliente e o envelope SOAP estão corretos. Não prova que o XML
do documento existe, porque nenhum documento foi montado.

**Qual a diferença entre estar em produção e responder?**
Serviços como SVRS e SEFAZ respondem a uma chamada de status. Responder não é emitir.
O README separa as duas coisas por isso.

**Preciso de certificado A1 para rodar?**
Para a integração com SEFAZ, sim, um A1 e-CNPJ da ICP-Brasil. O mesmo A1 vale para
NFS-e, MDF-e e CT-e: são usos diferentes do mesmo certificado, não certificados
diferentes.

**Como a autenticação funciona?**
O ERP autentica contra o Active Directory pelo Auth Service, e a autorização vem dos
grupos do diretório. Não há senha local no ERP: quem entra é quem o AD reconhece.

**Qual banco de dados usa?**
PostgreSQL 18 para dados relacionais, MongoDB para binários e imagens, MinIO para
arquivos grandes.

**Posso mudar a licença?**
A AGPLv3 é a licença atual. Alterar isso é decisão do titular do projeto, e precisa
passar por todos que já receberam o código sob AGPLv3.

**Por que a tela de cadastro de pessoas aparece vazia?**
Isso é frontend, não permissão. A tela abre o formulário e os campos não são
preenchidos; nenhuma configuração de grupo AD corrige.

## 🎉 Contribuidores

| Nome | Função |
|------|--------|
| **Eurípedes Batista de Paiva Junior** | Desenvolvedor Principal |

---

## 🏆 Comparativo com Grandes ERPs

| Funcionalidade | SAP | Sankya | **Brasil SaaS ERP** |
|----------------|-----|--------|---------------------|
| Multiempresa Nativo | ✅ | ✅ | ✅ |
| Fiscal Brasileiro | ⚠️ Complexo | ✅ | ✅ **Mais ágil** |
| IA Integrada | ❌ | ❌ | ✅ **Spring AI** |
| Código Aberto | ❌ | ❌ | ⚠️ Privado |
| Customização | ⚠️ Cara | ⚠️ Limitada | ✅ **Total** |
| Preço | $$$$ | $$$ | **$$** |
| Suporte Local | ⚠️ Terceiros | ✅ | ✅ **Direto** |
| Cloud-Native | ⚠️ Adaptação | ⚠️ Adaptação | ✅ **Nativo** |
| Observabilidade | ✅ | ⚠️ | ✅ **Completa** |

---

<div align="center">

**Feito com ❤️ usando Spring Boot + React + PrimeReact**

🚀 **Enterprise Ready - Fiscalmente Conforme - Observável**

</div>


---

## 🔐 Autenticação e Autorização

O Brasil SaaS ERP possui dois mecanismos de autenticação:

- **Usuários ERP**: username + senha validada com BCrypt.
- **PostgreSQL SUPERUSER**: username + senha autenticados diretamente no PostgreSQL. A role precisa ter rolsuper=true.

Uma role PostgreSQL comum não substitui a senha BCrypt do usuário ERP.

### PostgreSQL SUPERUSER

Qualquer PostgreSQL SUPERUSER pode autenticar no ERP usando sua própria credencial. Se ainda não existir um usuário ERP correspondente, o sistema o provisiona automaticamente, garante o perfil ADMIN e emite as authorities:

- ROLE_ADMIN
- ROLE_SUPERADMIN

A senha PostgreSQL nunca é armazenada no ERP.

A conexão normal da aplicação usa a identidade técnica configurada no datasource. A autenticação de um SUPERUSER usa uma conexão PostgreSQL temporária com a identidade informada pelo usuário e SSL, sem reutilizar o certificado de cliente da role técnica sa.

### Usuários com perfil GERENTE/DIRETORIA/USUARIO

O perfil define **autorização**, não autenticação. Esses usuários continuam dependendo da senha BCrypt cadastrada no ERP. Criar uma role PostgreSQL comum com o mesmo username não concede automaticamente acesso.

### Fluxo resumido

    POST /api/auth/login
            |
            +--> usuário ERP + BCrypt ----------------> JWT
            |
            +--> PostgreSQL SUPERUSER ----------------> ADMIN + SUPERADMIN + JWT

Documentação completa: docs/autenticacao.md

Arquitetura: docs/arquitetura.md

### Dependências do Ecossistema

## Autenticação Corporativa

O Brasil SaaS ERP utiliza o BrasilCloud Auth Service para autenticação centralizada.

Auth Service:

https://github.com/euripedesdark/auth-service

Licença:

AGPLv3

---

## Requisitos do Auth Service

O Auth Service foi projetado para operar com:

- LDAP
- LDAPS
- Active Directory

Ambientes recomendados:

### Linux

Samba Active Directory

Guia completo:

https://medium.com/meetcyber/full-guide-deploying-samba-active-directory-on-fedora-and-rocky-with-hybrid-dns-and-gpo-support-30f47fab1e90

### Windows

Windows Server Active Directory com LDAPS habilitado.

---

## Observação

O BrasilCloudERP pode ser avaliado e executado independentemente da implantação completa do ambiente corporativo.

Para produção, recomenda-se a utilização do Auth Service integrado a um diretório LDAP/LDAPS.
