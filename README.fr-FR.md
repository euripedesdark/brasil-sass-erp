# 🚀 Brasil SaaS ERP — Système de Gestion d’Entreprise

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](LICENSE.md)

**ERP Enterprise multi-entreprise avec IA assistante, couverture fiscale complète et architecture modulaire**

Créé par : **Euripedes Batista de Paiva Junior**

Criado por: **Euripedes Batista de Paiva Junior**

---

## 📖 À propos du Projet

**Brasil SaaS ERP** est un système de gestion d’entreprise complet, conçu pour desservir des entreprises de toute taille avec une solution robuste, sécurisée et évolutive. Migré de Delphi vers **Java Spring Boot 3.3.5** avec **Oracle JDK 21**, le système fournit des modules financiers, fiscaux, de stock, de services et de ventes avec de bonnes performances sur de gros volumes de données.

### ✨ Points forts Enterprise

| Fonctionnalité | Description |
|---------|-----------|
| 🏎️ **Haute Performance** | Migration de WebFlux vers un Spring MVC traditionnel, corrigeant la lenteur sur les tables de plus de 400 Mo |
| 📊 **Pagination Intelligente** | Chargement des enregistrements à la demande, idéal pour de très grandes bases |
| 🎨 **Frontend Moderne** | Interface réactive avec PrimeReact 10.8 et React 19 |
| 📝 **Rapports Professionnels** | Génération de PDF avec une mise en page moderne via OpenPDF 3.0.5 |
| 🌐 **API RESTful** | Backend robuste avec des endpoints paginés et des filtres avancés |
| 🔒 **Sécurité Multi-tenant** | Authentification JWT, autorisation par permission et isolation multi-entreprise |
| 📦 **Stockage Hybride** | PostgreSQL (données relationnelles) + MongoDB (binaires/images) + MinIO (gros fichiers) |
| 🤖 **IA Assistante** | Spring AI + OpenAI intégré pour une assistance intelligente |
| 📡 **Observabilité** | Actuator + Micrometer + Logback avec MDC pour le traçage de bout en bout |
| 🚛 **Documents de Transport** | NFS-e SP, MDF-e et CT-e reliés à SEFAZ ; génération de fichier SPED EFD ICMS/IPI |
| 🧾 **Contrat de réponse unique** | Un seul format de réponse que l’ERP lit, ajusté du côté de l’émetteur. La cause du refus est conservée et survit à la fermeture de l’écran |

---

## 🛠️ Stack Technologique Complet

### Backend (Núcleo)

| Camada | Tecnologia | Versão | Finalidade |
|--------|------------|--------|------------|
| **Linguagem** | Oracle JDK | 21 (LTS) | Base de longo suporte com records, pattern matching |
| **Framework** | Spring Boot | 3.3.5 | Autoconfiguração, Tomcat embutido, Actuator |
| **Web** | Spring MVC + Jackson | - | Camada REST (/api/**), JSON consistente |
| **Segurança** | Spring Security 6 + JWT | - | Autenticação stateless, @PreAuthorize |
| **ORM** | Spring Data JPA + Hibernate | 6.5+ | Persistência com ddl-auto: validate |
| **Migrations** | Flyway | Latest | Migrations versionadas (V1…V101) |
| **Boilerplate** | Lombok + MapStruct | - | Menos código, mapeamento DTO↔entidade |
| **Validação** | Bean Validation (Jakarta) | - | Validação de entrada na borda da API |
| **Documentação** | springdoc-openapi | Latest | Swagger UI em /swagger-ui.html |

### Dados & Infraestrutura

| Componente | Tecnologia | Versão | Finalidade |
|------------|------------|--------|------------|
| **Banco Relacional** | PostgreSQL | 18 | Núcleo ACID com schema `brasil_saas` |
| **Chiffrement** | mTLS (PKI) | - | ca.crt/sa.crt/sa.pk8 pour une connexion sécurisée |
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
`cStat 107` ("Serviço em Operação"). Cela prouve la chaîne TLS, le certificat du
cliente e o envelope SOAP. **Não prova o XML do documento** — nenhum MDF-e nem
CT-e foi emitido ainda, e a montagem do MDF-e não está implementada.

Detalhe em
[`docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`](docs/pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md).

**L’échéance qu’on ne peut pas ignorer :** la NT 2026.001 rend le groupe `infCIOT`
obligatoire sur le MDF-e routier à compter du **23/11/2026** (rejet `cStat 684`).
`infCIOT` est déjà dans le XSD ; la validation dans l’émetteur manque.

#### NFS-e São Paulo : proxy avec bascule entre deux implémentations

La NFS-e de São Paulo n’est pas servie par une seule implémentation. L’ERP parle toujours
à **4567**, et un proxy décide qui répond :

```
ERP  ──►  4567  nfse-failover.rb  (proxy, health check périodique)
              ├──►  4568  nfse-sp-api      Java   PRIMAIRE
              └──►  4569  nfse-sp-bridge   Ruby   REPLI
                        (mairie de São Paulo, via A1)
```

| Pièce | Rôle |
|---|---|
| **4567 — `nfse-failover.rb`** | Proxy. Essaie le Java, bascule sur le Ruby, health check toutes les 10 s |
| **4568 — `nfse-sp-api`** | Implémentation **primaire**. Valide le XML contre le schéma **avant** d’envoyer |
| **4569 — `nfse-sp-bridge`** | Implémentation **de repli**, en Sinatra. Prend le relais quand le Java échoue |

L’ERP sait qui a répondu grâce à l’en-tête `X-Backend` renvoyé par le proxy.

**Pourquoi deux implémentations et pas une :** le Ruby a été écrit en premier et c’est lui
qui a validé les règles auprès de la mairie de São Paulo pour de vrai — il a émis et
annulé une NFS-e en production, avec les messages littéraux de la mairie consignés dans
[`src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md`](src/main/resources/microservices/nfse-sp-bridge/BRASIL-SAAS.md).
Le Java a réimplémenté le même contrat avec le même périmètre, et c’est la comparaison des
XML entre les deux qui a révélé les bugs de signature et de namespace côté Java. Le Ruby
reste debout **exprès** : c’est l’oracle contre lequel le Java est vérifié.

L’ordre n’est pas décoratif. La primaire est celle qui valide contre le schéma ; si c’était
inversé, une implémentation cassée émettrait avec un mauvais contrat et l’ERP ne le
découvrirait qu’après le rejet de la mairie.

**Ce que le proxy change, et ce qu’il ne change pas.** Le proxy change d’upstream quand la
connexion échoue. Il ne change **pas** quand l’implémentation répond `200` avec le mauvais
contrat — et c’est le cas qui compte : le 26/09/2026, l’API Java étant tombée, le Ruby a
émis la note 29 avec `success=true`, la mairie l’a acceptée, et l’ERP a répondu en erreur
parce qu’il lisait `"sucesso"` là où le Ruby écrivait `success`. C’est pourquoi le
watchdog existe : il surveille la réponse, pas seulement le port.

A 4567 é o default do código, não um número escolhido:

```java
// NfseEmissaoService.java
@Value("${brasil-saas.fiscal.nfse.url:http://127.0.0.1:4567/api/nfse-sp}")
```

Le certificat A1 est l’e-CNPJ de l’entreprise, et il est **conservé dans MongoDB** — la collection
`documentos` contient un document `tipoEntidade: "certificado_digital"` avec le `.pfx`
íntegro. É por isso que o A1 chega junto na restauração do dump. O `.pfx` foi
emitido com **RC2-40-CBC**, que o OpenSSL 3 tirou do provider padrão: o Java
aceita, e o Ruby precisa do provider legacy (`OPENSSL_MODULES`). A reemissão
com AES resolveria de vez.

### Observabilité & Operação

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
- ✅ 25 composants implémentés (3 762 lignes)
- ✅ Módulo Produção completo (ordens de produção)
- ✅ Design responsivo com PrimeReact
- ✅ Integração total com backend Spring Boot
- ⚠️ 4 composants de remplacement (Achats, Stock, Ventes, Services)

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

## 📦 Modules du Système

### 💰 Financeiro Completo (ERP Moderno)

> **Status**: ✅ **IMPLEMENTADO E VALIDADO** - Migration V5 + V28 com 439 linhas de SQL

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Contas a Pagar** | Fluxo de aprovação multinível, integração bancária (CNAB), alertas de vencimento |
| ✅ **Contas a Receber** | Emissão de faturas/boletos, cobrança automática, conciliação bancária |
| ✅ **Tesouraria** | Projeção de fluxo de caixa, controle de empréstimos e aplicações financeiras |
| ✅ **Contabilidade Geral** | Lançamentos automatizados, conformidade fiscal, partidas dobradas |
| ✅ **Contabilidade de Custos** | Centros de custo hierárquicos, análise de rentabilidade (orçado vs realizado) |
| ✅ **Relatórios Executivos** | DRE, balanço patrimonial, dashboards, indicadores de liquidez |
| ✅ **Lançamentos Contábeis** | Paginação (máx. 20 linhas/página), histórico auditável |
| ✅ **Baixas Financeiras** | Com descontos, juros, multas e estornos |
| ✅ **Extrato Bancário** | Integrado com conciliação automática |
| ✅ **Resumo Financeiro** | Por período, centro de custo, plano de contas |
| ✅ **Comissões** | Cálculo automático sobre vendas e OS |
| ✅ **Orçamento vs Realizado** | Comparativo gerencial |
| ✅ **Provisão PDD** | Provisão para devedores duvidosos |
| ✅ **Integração Bancária** | CNAB, OFX, importação de extrato |

#### Estrutura do Módulo Financeiro (`modules/financeiro`)

- **52 classes Java** implémentant toutes les entités et services
- **Entidades**: Titulo, TituloParcela, Baixa, LancamentoContabil, LancamentoPartida, PlanoContas, CentroCusto, ContaBancaria, TipoPagamento, CondicaoPagamento, Extrato, ConciliacaoBancaria, Renegociacao, FluxoAprovacao, Aprovacao, ProjecaoFluxoCaixa, AnaliseRentabilidade, ProvisaoPdd, IntegracaoBancaria, AplicacaoFinanceira, Emprestimo, Orcamento, OrcamentoRealizado, Comissao
- **Endpoints REST**: `/api/financeiro/titulos`, `/api/financeiro/lancamentos`, `/api/financeiro/baixas`, `/api/financeiro/extrato`, `/api/financeiro/conciliacao`, `/api/financeiro/fluxo-caixa`

---

### 🏙️ Cadastros

| Cadastro | Funcionalidades | Status |
|----------|-----------------|--------|
| ✅ **Municípios** | Busca dinâmica por código IBGE ou nome (tabela oficial) | ✅ Implementado |
| ✅ **Pessoas** | Cadastro único para PF e PJ com dados completos | ✅ Implementado |
| ✅ **Clientes** | Gestão com limite de crédito, logo, histórico | ✅ Implementado |
| ✅ **Fornecedores** | Avaliação, classificação, logo | ✅ Implementado |
| ✅ **Produtos** | Variações, kits, NCM, imagens no MongoDB | ✅ Implementado |
| ✅ **Serviços** | Para NFS-e, integração com OS | ✅ Implementado |
| ✅ **Funcionários** | Vínculo com pessoa, foto, cargo | ✅ Implementado |
| ✅ **Empresas** | Multi-tenant, logo, configurações | ✅ Implementado |
| ✅ **Condições de Pagamento** | Prazos, parcelas | ✅ Financeiro |
| ✅ **Tipos de Documentos** | Classificação fiscal | ✅ Fiscal |
| ✅ **Catégories** | Catégorisation des produits/services | ✅ Implémenté |
| ✅ **Marcas** | Gestão de marcas de produtos | ✅ Implementado |
| ✅ **Unidades de Medida** | Unidades para produtos | ✅ Implementado |
| ✅ **Transportadoras** | Transportadoras para frete | ✅ Implementado |

---

### 📋 Serviços e Vendas

| Module | Funcionalidades | Status |
|--------|-----------------|--------|
| ✅ **Ordem de Serviço (OS)** | Emissão de comprovante, cálculo de comissão técnica | ✅ Implementado (V35, V41) |
| ✅ **Vente de Produits** | Note fiscale en option, intégration du stock | ✅ Implémenté (V20) |
| ✅ **Faturamento** | Em aba separada, geração de títulos automáticos | ✅ Implementado |
| ✅ **Cálculo ICMS** | Automático para vendas interestaduais | ✅ Implementado |
| ✅ **Emissão NF-e/NFS-e** | Integrada com módulo fiscal | ✅ Implementado |
| ✅ **Pedidos de Compra** | Integração com fornecedores | ✅ Implementado (V21) |
| ✅ **Controle de Estoque** | Saldos, movimentações, ajustes | ✅ Implementado (V22) |
| ✅ **Entrada de Notas** | Tipo de operação fiscal | ✅ Implementado (V31, V42) |

---

### 📄 Rapports Professionnels

| Relatório | Formato | Recursos |
|-----------|---------|----------|
| ✅ **Financeiro Analítico** | PDF | Layout moderno, cores, formatação empresarial |
| ✅ **Ordem de Serviço** | PDF | Comprovante formatado com logo |
| ✅ **Extrato de Lançamentos** | PDF | Filtros por período, conta, centro de custo |
| ✅ **DRE Gerencial** | PDF | Demonstrativo de resultados |
| ✅ **Balanço Patrimonial** | PDF | Ativo, passivo, patrimônio líquido |
| ✅ **Curva ABC** | PDF | Classificação de produtos/clientes |

---

### 🏭 Produção Industrial

> **Status**: ✅ **IMPLEMENTADO** - Controller, Service, Repository e Models

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Ordem de Produção** | Fluxo completo: Insumos → Processo → Produto Final |
| ✅ **Pesos e Medidas** | Suporte a densidades, unidades especiais |
| ✅ **Rastreabilidade** | Lote, série, validade |
| ✅ **Pointage** | Heures travaillées, consommation d’intrants |
| ✅ **Itens de Produção** | Controle de itens produzidos |

---

### 🤖 IA Assistante

| Recurso | Status |
|---------|--------|
| ✅ **Chat com IA** | Spring AI + OpenAI integrado |
| ✅ **Análise de Dados** | Recomendações baseadas em histórico |
| ✅ **Alertas Inteligentes** | Notificações proativas |
| ⚠️ **Chave API** | Módulo pronto, necessita chave OpenAI válida |

---

## 🗂️ Structure du Projet

```
BRASIL-SAAS-ERP/
├── src/main/java/br/com/brasil_saas/     # Monólito principal (347 classes Java)
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
│   └── ia/           # IA Assistante (0 classes) - Porta 8089 ⚠️ Estrutura vazia
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

## 🚀 Comment Lancer l’Application

### Prérequis

| Dépendance | Version Minimale | Obligatoire |
|-------------|---------------|-------------|
| ☕ Oracle JDK | 21 (LTS) | ✅ Oui |
| 📦 Apache Maven | 3.9+ | ✅ Oui |
| 🟢 Node.js | 22+ LTS | ✅ (frontend) |
| 🐘 PostgreSQL | 18+ | ✅ Oui |
| 🍃 MongoDB | Latest | ✅ (images) |
| 🔧 Git | Latest | ✅ Oui |

### 🐧 Installation sur Linux (Ubuntu/Debian/CentOS/Fedora)

```bash
# Tornar script executável
chmod +x installbase.sh

# Executar instalação automatizada
sudo ./installbase.sh
```

**Le script va :**
1. Détecter la distribution Linux
2. Installer le JDK 21 via SDKMAN
3. Installer Maven et Node.js
4. Configurer PostgreSQL et MongoDB
5. Générer la PKI Easy-RSA et configurer le mTLS de PostgreSQL
6. **Installer les AC d’ICP-Brasil et monter le truststore JKS** (MDF-e e CT-e)
7. Cloner les dépendances Maven
8. Compilation automatique du frontend React

L’étape 6 n’est pas optionnelle. La SVRS présente un certificat d’ICP-Brasil
(`AC SERPRO SSLv1`, vindo da `Raiz Brasileira v10`), e num Debian limpo nenhuma
dessas duas está no bundle de CAs: sem elas o handshake morre com
`PKIX path building failed`. Le script laisse :

```
/etc/brasil-saas/certs/truststore-sefaz.jks   644   uniquement l’AC publique
/etc/brasil-saas/sefaz.env                   644   chemin et UF, sans secret
/etc/brasil-saas/cert.env                    600   l’A1 et les mots de passe
```

Le truststore est séparé de celui du système **exprès** : l’ERP est en Java et la
bibliothèque fiscale lit son propre JKS. Sans le constituer, la même erreur revient et
laisse croire que les AC ne sont jamais entrées.

`cert.env` doit être renseigné avec l’A1 de l’émetteur :

```bash
BRASIL_SAAS_MDFE_CERTIFICADO=/caminho/do/a1.pfx
BRASIL_SAAS_MDFE_CERT_PASS=...
```

**Le même A1 sert à la NFS-e, au MDF-e et au CT-e**, desde que seja e-CNPJ A1 da
ICP-Brasil. Ce ne sont pas des certificats différents : ce sont des usages différents du même.

### Scripts d’exploitation

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
l’historique est perdu, et un `clone` amène le HEAD d’aujourd’hui, qui peut être une autre version
do material com que o ERP foi testado. O hash é o que diz se o que baixou é o
mesmo, e o script **diz que não bate** em vez de deixar material errado entrar
em silêncio.

### 🪟 Instalação no Windows

```powershell
# Executar PowerShell como Administrador
.\install-sysfluxo.ps1
```

**Le script va :**
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

## 🌐 Accéder au Système

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

## 📊 Pourcentage d’avancement du fiscal

**Critério usado**, para o número não ser opinião. Cada documento fiscal precisa
de 6 entregáveis:

| # | Entregável |
|---|-----------|
| 1 | **Connexion au SEFAZ/à la mairie** qui répond |
| 2 | **Montagem do documento** (o XML: emitente, veiculo, motorista, LAC) |
| 3 | **Assinatura** com A1 ICP-Brasil |
| 4 | **Envio e protocolo** |
| 5 | **Consulta / cancelamento** |
| 6 | **Écran** affichant le retour de la mairie |

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

- **Le MDF-e et le CT-e sont à 25 % et 17 %, pas à 100 %.** La connexion fonctionne et la
  SEFAZ responde `cStat 107` com o seu A1 — isso é o entregável 1, o mais
  difícil, porque eram 4 camadas entre o ERP e a resposta (A1, CAs da
  ICP-Brasil, truststore JKS, mutual TLS). **ce qui manque, c’est le livrable 2**, le
  montagem do documento, e ele é o trabalho maior. `cStat 107` é o *status do
  service* : cela ne dit rien du XML, car aucun XML n’a jamais été assemblé.
- **SPED EFD ICMS/IPI em 40%** e não em 100% porque gera arquivo válido, com
  os contadores conferidos, mas lê o cabeçalho do corpo da requisição em vez
  de la banque, et il n'y a pas d'écran. Les livrables 2 et 5 manquent.
- **NF-e em 0%.** Não é exagero: os 3 métodos da interface `NFeService` estão
  como `TODO` em `NFeServiceImpl`. A interface existe, a implementação não.
- **Consulta/DistDFe em 100%** de 6 em 6.

Le pourcentage porte sur le **périmètre fiscal**, pas sur le système. Les autres modules ont leur propre mesure,
abaixo.

## 📊 Autres modules — structure et ce qui répond

### Por que não há um % único aqui

**Compter des fichiers ne mesure pas l’avancement.** J’ai mesuré les 12 modules avec les mêmes 6
entregáveis do fiscal e **onze deram 100%** — porque todos têm model, repository,
service, controller e tela. Isso não significa que o cadastro de cliente abre, que
o título baixa ou que a folha calcula. Significa que os arquivos existem.

O que mede é executar. Então há duas colunas, e elas discordam:

| Module | Tabela | Entidade | Repo | Service | Controller | Tela | Estrutural | Responde |
|--------|:------:|:--------:|:----:|:-------:|:----------:|:----:|:----------:|:--------:|
| Cadastro | 36 | 21 | 21 | 28 | 12 | 11 | 6/6 | ✅ |
| Financeiro | 32 | 32 | 22 | 12 | 12 | 15 | 6/6 | ✅ |
| Estoque | 13 | 12 | 12 | **0** | 9 | 9 | 5/6 | ✅ |
| Fiscal | 29 | 29 | 26 | 6 | 12 | 10 | 6/6 | ⚠️ parcial |
| Vendas | 11 | 5 | 5 | 2 | 2 | 2 | 6/6 | ✅ |
| Compras | 11 | 10 | 8 | 4 | 4 | 4 | 6/6 | ✅ |
| Produção | 6 | 6 | 5 | 11 | 4 | 4 | 6/6 | ✅ |
| RH | 4 | 4 | 3 | 2 | 4 | 4 | 6/6 | ❌ |
| Serviços | 3 | 3 | 3 | 2 | 1 | 1 | 6/6 | ✅ |
| BI | 8 | 17 | 9 | 14 | 6 | 4 | 6/6 | ✅ |
| Core | 15 | 9 | 7 | 14 | 10 | 6 | 6/6 | ✅ |
| IA | 10 | 10 | 10 | 18 | 9 | **1** | 6/6 | ⚠️ parcial |

**Estrutural** = os 6 entregáveis existem como arquivo.
**Responde** = a API respondeu num smoke test com token válido.

⚠️ **Nenhum destes foi validado funcionalmente.** Abri a tela, não. Criei
registro, não. O que o smoke test prova é que a **rota responde**; não prova
que o dado devolvido esteja certo. As duas coisas são diferentes, e confundir
elas é como se descobre que "o módulo está pronto" e a tela mostra valor errado.

### O que o smoke test achou

28 rotas chamada, uma por módulo:

```
16  2xx   respondem
 1  500   QUEBROU   <- o que interessa
 4  404   rota não existe no caminho testado
 6  400   parâmetro obrigatório faltando
```

**O 500 é um bug real e reproduzível:**

```
GET /api/rh/funcionarios?page=0&size=1   ->  500

HttpMessageNotWritableException: Could not write JSON:
  could not initialize proxy [br.com.brasil_saas.rh.model.Cargo#1]
Caused by: LazyInitializationException: no Session
```

`Funcionario` tem `@ManyToOne(fetch = FetchType.LAZY)` para `Cargo`. A sessão
do Hibernate fecha quando a transação do controller termina, e o Jackson tenta
serializar o proxy **depois** — sem sessão. O `GET /api/rh/cargos` funciona
(200); o de funcionários não.

Correção: buscar o funcionário com `join fetch` no `Cargo`, ou marcar o
relacionamento como `EAGER`, ou devolver um DTO em vez da entidade. A
recomendada é o `join fetch`, porque `EAGER` em `ManyToOne` resolve o problema
trocando por N+1 em toda listagem.

### Os 404 e 400 são meus, não do sistema

Chamei caminhos que não existem. `rh/funcionarios` só tem `GET /{id}`, sem
listagem — o `500` acima veio de `GET /{id}` com página. `core/empresas` e
`core/usuarios` não têm rota de listagem nesse caminho. Não são bugs; são URLs
que eu chutei.

**A lição:** as porcentagens estruturais daqui são confiáveis — os arquivos
existem. A coluna "Responde" é de uma amostra de **uma rota por módulo**, o que
ne couvre pas le module. Le mesurer pour de vrai demande une suite de tests, et l'ERP n'en a **aucune
teste automatizado**.

### Estoque não tem camada de service

9 controllers, 12 repositories, **0 services**. La règle métier se trouve à l'intérieur
du controller, en parlant directement au repository. Ça marche, mais il n'y a nulle part
tester et n’isole pas la règle. Dans les autres modules, la séparation existe — c’est une
inconsistência a decidir, não um acidente.

## ✅ Ce Qui Tourne

Vérifié le **26/09/2026**. Les huit services sont démarrés et répondent.

| Service | Port | État | Comment cela a été vérifié |
|---------|-------|--------|---------------------|
| **PostgreSQL** | 5432 | 🟢 `active/enabled` | `select 'ok'` retorna `ok` |
| **MongoDB** | 27017 | 🟢 `active/enabled` | se connecte ; 4 collections |
| **RabbitMQ** | 5672 / 15672 | 🟢 `active/enabled` | le panneau répond |
| **MinIO** | 9000 / 9001 | 🟢 `active/enabled` | `/minio/health/live` → 200 |
| **Redis** | 6379 | 🟢 `active/enabled` | `PING` → `PONG` |
| **ERP (Spring Boot)** | 8080 | 🟢 de pé | répond ; un 500 avec un faux jeton est le comportement normal |
| **Vite (frontend)** | 5173 | 🟢 `HTTP 200` | page servie |
| **API NFS-e** | 4567 | 🟢 `active/enabled` | `sucesso=true`, certificat et mot de passe chargés |

### Éteints volontairement

| Service | Pourquoi |
|---------|---------|
| `nfse-sp-bridge` (Ruby) | **Une seule API, sans bascule automatique.** Un repli qui change d’implémentation quand l’appel expire émet une note en double — la réponse est perdue, l’ERP réémet, et la mairie se retrouve avec deux. Le bridge reste sur le disque ; le contrat `contrato_nfse` est identique des deux côtés. |
| `nfse-watchdog` | Il utilisait `consulta-cnpj` comme health check, qui va à la mairie et prend 1,27 s. Il a fait tomber l’API Java **3 fois** pendant que l’ERP émettait par elle. Ne pas le rallumer tel quel. |
| `nginx` | Remplacé par l’API unique. Le proxy lisait le corps HTTP depuis la socket et ne savait le lire qu’avec `Content-Length` ; l’ERP envoie `Transfer-Encoding: chunked`, donc le corps était jeté. |

### Ce que l’ERP valide tout seul

| Vérification | Résultat |
|-------------|-----------|
| `GET /api/fiscal/nfse/status` (via 4567) | `sucesso=true` |
| `POST /api/fiscal/mdfe/status` → SVRS | **`cStat 107`** "Service en Opération" *(littéral renvoyé par la SEFAZ)* |
| `GET /api/fiscal/cte/status` → SEFAZ de SP | **`cStat 107`** "Service en Opération." *(littéral renvoyé par la SEFAZ)* |
| `GET /api/fiscal/sped/efd/exemplo` | fichier valide, 19 lignes, compteurs vérifiés |

O MDF-e é atendido pela **SVRS** (`ufAtendente: RS`) e o CT-e pelo **portal de
SP** (`ufAtendente: SP`), com o **mesmo A1**. São autorizadores diferentes, não
certificats différents.

### Ce qui n’est pas encore prêt

| Élément | État |
|------|----------|
| **NF-e / NFC-e** | `NFeServiceImpl` tem 89 linhas e **3 TODOs**. A interface promete `emitirNFe`, `cancelarNFe` e `consultarSituacao`; nenhuma das três é implementada. |
| **IA (Spring AI)** | Módulo presente e conectado, com embeddings, chat e classifications. Não foi validado nesta rodada. |
| **Émission de MDF-e** | La connexion à la SVRS est prête et testée (`cStat 107`), et le certificat signe. **A montagem do documento não existe ainda**: `MdfeEmissaoService` tem só `statusServico` e `consultarRecibo`. Falta construir o XML com emitente, veículo, motorista, LAC, municípios e CIOT. |
| **Emissão de CT-e** | Idem. Só `statusServico`. |
| **SPED EFD Contributions** | La bibliothèque se résout dans `pom.xml`, mais **il n’y a pas d’endpoint** — seul ICMS/IPI en a un. |
| **Production (SEFAZ)** | Jamais testée. La bibliothèque renvoie un `HOMOLOGACAO` fixe ; la configuration l'écrase, et l’écraser n’est pas la même chose que cela fonctionne. |
| **Onglet de retour à l’écran** | Les données sont dans Mongo et l’endpoint existe ; l’onglet manque dans `Nfse.jsx`. |
| **Validation du CIOT** | `cStat 684` entre en production à la SEFAZ le **23/11/2026**. `infCIOT` est déjà dans le XSD ; la validation dans l’émetteur manque. |

**Por que o MDF-e e o CT-e não estão na lista de "prontos" apesar de
répondent `cStat 107` :** le `cStat 107` est le *statut du service*, et il prouve que la
chaîne TLS, le certificat client et l’enveloppe SOAP sont corrects. Il **ne
prova que o XML do documento esteja**, porque nenhum documento foi montado nem
enviado. São duas coisas diferentes, e o README separa por isso.

---

## 📡 Endpoints de l’API

### Municípios

```
GET    /api/municipios/paginado?page=0&size=20&sort=nome,asc
GET    /api/municipios/buscar?termo={codigo_ou_nome}
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
```

### Ordem de Serviço

```
GET    /api/servicos/ordens/paginado
POST   /api/servicos/ordens
PUT    /api/servicos/ordens/{id}
POST   /api/servicos/ordens/{id}/emitir-nota
POST   /api/servicos/ordens/{id}/fechar
GET    /api/servicos/ordens/{id}/pdf
```

### Relatórios

```
GET    /api/relatorios/financeiro-pdf?inicio=YYYY-MM-DD&fim=YYYY-MM-DD
GET    /api/relatorios/os-pdf/{id}
GET    /api/relatorios/dre?competencia=YYYY-MM
```

### Fiscal — NFS-e São Paulo

L’API d’émission est un service séparé, sur le port **4567**. L’ERP lui parle via
`brasil-saas.fiscal.nfse.url`. É **uma API só, sem fallback automático** — o
bridge Ruby e o nginx estão desligados de propósito, porque um fallback que
troca de implementação em produção emite nota duplicada quando a resposta se
perde.

```
POST   /api/fiscal/nfse/emitir
POST   /api/fiscal/nfse/{id}/cancelar
GET    /api/fiscal/nfse/{id}/xml
GET    /api/fiscal/nfse/{id}/pdf
GET    /api/fiscal/nfse/retornos                   conversas com a prefeitura
GET    /api/fiscal/nfse/retornos/recusas           só as recusadas
GET    /api/fiscal/nfse/retornos/para-conferir     as que não deu para saber
```

Le motif du rejet de la mairie est enregistré **avant** que l’exception ne remonte, avec le
`cStat` e o corpo bruto no Mongo. A tela de erro é uma parede; o registro não.

### Fiscal — MDF-e e CT-e

```
GET    /api/fiscal/mdfe/status                     cStat 107 se a SVRS estiver de pé
GET    /api/fiscal/mdfe/recibo?numero={recibo}     2ª chamada: do recibo para a chave
GET    /api/fiscal/cte/status                      cStat 107 se a SEFAZ estiver de pé
```

Os dois usam o mesmo contrato de resposta, com os **três estados** de
`sucesso`: `true` (autorizado), `false` (recusado, com `cStat` e motivo) e
`null` (**não deu para saber**). O terceiro estado existe para não duplicar
documento: a SVRS rejeita chave natural repetida, e reemitir no escuro é
exatamente o que cria a duplicidade.

O MDF-e devolve **recibo** na primeira chamada e **protocolo** na segunda.
Ler a chave na resposta do envio dá NPE — o método não existe.

### Fiscal — SPED EFD ICMS/IPI

```
POST   /api/fiscal/sped/efd/gerar                  gera o arquivo EFD
GET    /api/fiscal/sped/efd/exemplo                gera o de exemplo, para conferir o formato
```

**EFD não se envia para ninguém.** O arquivo é gerado, assinado e guardado; quem
busca depois é a SEFAZ ou a Receita. Sem web service, sem protocolo, sem fila.

### Fiscal — tabelas e cadastros

```
GET    /api/fiscal/cest?busca={termo}              busca por código, descrição ou NCM
GET    /api/fiscal/cfop?tipoOperacao=ENTRADA|SAIDA
GET    /api/fiscal/ncm
GET    /api/fiscal/issqn
GET    /api/fiscal/impostos
GET    /api/fiscal/certificados
```

`GET /api/cadastro/servicos?nome={termo}` alimenta o autocomplete do nome do
service, ce qui suggère un service déjà enregistré pendant que la personne saisit. Deux
cadastros com nome parecido e código municipal diferente é a causa mais comum
de rejet à la mairie.

---

## 🎯 Fonctionnalités Spéciales

### 🔍 Busca Dinâmica de Municípios
Digite o código IBGE ou nome e o sistema localiza instantaneamente.

### 📄 Pagination Intelligente
Todas as telas exibem máx. 20 linhas, com navegação entre páginas.

### 🌳 Lazy Loading
Dados carregados sob demanda, reduzindo consumo de memória.

### 📊 Dashboard Financeiro
Visualize totais por status (Aberto, Pago, Atrasado) em tempo real.

### 🖨️ Rapports Professionnels
PDFs com layout moderno, cores diferenciadas e formatação empresarial.

### 🔐 Hierarquia de Permissões

| Perfil | Acesso |
|--------|--------|
| **SuperAdmin** | Completo, incluindo gerenciamento de administradores |
| **Diretor** | Todas funcionalidades exceto gerenciar administradores |
| **Gerente** | Todas funcionalidades operacionais |
| **Usuário** | Limitado às permissões específicas |

---

## ⚙️ Configuration Avancée

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

## 🧪 Scripts de Test et de Validation

| Script | Finalidade |
|--------|------------|
| `test_db_connection.sh` | Teste la connexion PostgreSQL |
| `test_database.sh` | Valida schema e migrations |
| `test_erp_operations.sh` | Smoke test de operações ERP |
| `check_mongodb.sh` | Vérifie la connexion et la collection MongoDB |

---

## 🔒 Sécurité

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

## 🐛 Résolution des Problèmes

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
- ✅ La compilation a déjà été exécutée (2,2 Mo dans `static/dist/`)
- ✅ 25 composants React compilés
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

## 📂 Fichiers Non Référencés / Sauvegarde

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

## 📄 Licence

Ce projet est sous **GNU Affero General Public License v3.0 (AGPLv3)**.

Le texte complet, inchangé, se trouve dans [`LICENSE.md`](LICENSE.md).

L’AGPLv3 exige que le code source soit proposé à quiconque utilise le programme, y
compris lorsque l’usage se fait **par le réseau** — d’où le "Affero". Pour un ERP
accessible par navigateur, c’est cette clause qui compte : quiconque pointe son
navigateur vers ce système a droit au code.

Les composants tiers livrés dans `src/main/resources/microservices/` conservent
leurs propres licences (MIT, Apache-2.0 et BSD), toutes compatibles avec l’AGPLv3. Les
fichiers `LICENSE` d’origine de chaque composant restent en place et ne sont pas
remplacés par celui-ci.

© 2026 Brasil SaaS ERP

---

---

---

## ❤️ Soutenir le Projet

Brasil SaaS ERP est un ERP open source maintenu par un seul développeur.

Si ce projet vous a aidé, vous, votre entreprise ou votre équipe, envisagez de soutenir son développement.

**PIX:**

```
24adc62c-b073-4587-974d-03fe35f6733f
```

Un don, quel qu’en soit le montant, paie le serveur, le certificat numérique et les
frais d’émission auprès de la mairie. Il n’achète aucune date de livraison sur une
issue.

### 💳 Virement international

La clé PIX ne fonctionne pas hors du Brésil. Pour faire un don depuis l’étranger, utilisez un virement bancaire.

**Si vous envoyez depuis une banque aux États-Unis**, vous pouvez utiliser ces
coordonnées pour un virement domestique. **Si vous envoyez depuis un autre endroit**,
faites un virement international Swift.

| | |
|---|---|
| **Nom** | Euripedes Batista de Paiva Junior |
| **Type de compte** | Checking |
| **Routing number** (pour wire et ACH) | `101019628` |
| **Numéro de compte** | `215822927677` |
| **Nom et adresse de la banque** | Wise US Inc, 108 W 13th St, Wilmington, DE, 19801, United States |
| **SWIFT/BIC** | `TRWIUS35XXX` |

Le routing number ne sert que lorsque l’argent sort des États-Unis. Ailleurs, le champ à
utiliser est `SWIFT/BIC`.

---

## 📬 Suporte

Pour toute question, suggestion ou signalement de bug :

- 📧 **Email**: euripedesdark@gmail.com
- 🔗 **GitHub Issues**: https://github.com/euripedesdark/BrasilCloudERP/issues
- 📖 **Documentação**: `/docs/`

---

## 🤝 Como Contribuir

Le projet est sous AGPLv3 et accepte les contributions.

1. **Abra uma issue** antes de escrever código, descrevendo o problema ou a melhoria.
   Sem issue beforehand, a mudança pode ir para um caminho que ninguém usa.
2. **Crie uma branch** com nome descritivo: `minha-melhoria`.
3. **Commitez par sujet.** Un commit fait une chose ; un commit qui en fait six ne peut
   ser revertido sozinho.
4. **Rode os testes** antes de abrir o PR: `mvn test`.
5. **Abra o Pull Request** contra a `main` e descreva o que muda e por quê.

```bash
git clone https://github.com/euripedesdark/BrasilCloudERP.git
cd BrasilCloudERP
mvn test
```

### O que não mexer sem issue

| Área | Pourquoi |
|---|---|
| Base de données | Un `ALTER` raté en production n'a pas de retour arrière |
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
Non, malgré leur réponse `cStat 107`. Ce code est le *statut du service* : il prouve que
le TLS, le certificat client et l’enveloppe SOAP sont corrects. Il ne prouve pas que le XML
do documento existe, porque nenhum documento foi montado.

**Qual a diferença entre estar em produção e responder?**
Des services comme SVRS et SEFAZ répondent à une valeur de statut. Répondre n'est pas émettre.
O README separa as duas coisas por isso.

**Ai-je besoin d’un certificat A1 pour faire tourner ça ?**
Para a integração com SEFAZ, sim, um A1 e-CNPJ da ICP-Brasil. O mesmo A1 vale para
NFS-e, MDF-e et CT-e : ce sont des usages différents du même certificat, pas des certificats différents
diferentes.

**Como a autenticação funciona?**
O ERP autentica contra o Active Directory pelo Auth Service, e a autorização vem dos
groupes du répertoire. Il n’y a pas de mot de passe local dans l’ERP : entre qui l’AD reconnaît.

**Qual banco de dados usa?**
PostgreSQL 18 para dados relacionais, MongoDB para binários e imagens, MinIO para
arquivos grandes.

**Posso mudar a licença?**
A AGPLv3 é a licença atual. Alterar isso é decisão do titular do projeto, e precisa
passar por todos que já receberam o código sob AGPLv3.

**Por que a tela de cadastro de pessoas aparece vazia?**
Isso é frontend, não permissão. A tela abre o formulário e os campos não são
preenchidos; nenhuma configuração de grupo AD corrige.

## 🎉 Contributeurs

| Nom | Função |
|------|--------|
| **Eurípedes Batista de Paiva Junior** | Desenvolvedor Principal |

---

## 🏆 Comparatif avec les Grands ERP

| Fonctionnalité | SAP | Sankya | **Brasil SaaS ERP** |
|----------------|-----|--------|---------------------|
| Multiempresa Nativo | ✅ | ✅ | ✅ |
| Fiscal Brasileiro | ⚠️ Complexo | ✅ | ✅ **Mais ágil** |
| IA Integrada | ❌ | ❌ | ✅ **Spring AI** |
| Código Aberto | ❌ | ❌ | ⚠️ Privado |
| Customização | ⚠️ Cara | ⚠️ Limitada | ✅ **Total** |
| Preço | $$$$ | $$$ | **$$** |
| Suporte Local | ⚠️ Terceiros | ✅ | ✅ **Direto** |
| Cloud-Native | ⚠️ Adaptação | ⚠️ Adaptação | ✅ **Nativo** |
| Observabilité | ✅ | ⚠️ | ✅ **Completa** |

---

<div align="center">

**Feito com ❤️ usando Spring Boot + React + PrimeReact**

🚀 **Enterprise Ready - Fiscalmente Conforme - Observável**

</div>


---

## 🔐 Authentification et Autorisation

O Brasil SaaS ERP possui dois mecanismos de autenticação:

- **Utilisateurs ERP** : nom d’utilisateur + mot de passe validé par BCrypt.
- **PostgreSQL SUPERUSER**: nom d’utilisateur + mot de passe authentifiés directement dans PostgreSQL. Le rôle doit avoir rolsuper=true.

Un rôle PostgreSQL ordinaire ne remplace pas le mot de passe BCrypt de l’utilisateur ERP.

### PostgreSQL SUPERUSER

Tout SUPERUSER PostgreSQL peut s’authentifier dans l’ERP avec ses propres identifiants. S’il n’existe pas encore d’utilisateurrio ERP correspondente, o sistema o provisiona automaticamente, garante o perfil ADMIN e emite as authorities:

- ROLE_ADMIN
- ROLE_SUPERADMIN

Le mot de passe PostgreSQL n’est jamais conservé dans l’ERP.

La connexion normale de l’application utilise l’identité technique configurée dans la datasource. L’authentification d’un SUPERUSER usa uma conexão PostgreSQL temporária com a identidade informada pelo usuário e SSL, sem reutilizar o certificado de cliente da role técnica sa.

### Usuários com perfil GERENTE/DIRETORIA/USUARIO

Le profil définit l’**autorisation**, pas l’authentification. Ces utilisateurs dépendent toujours du mot de passe BCrypt enregistréa no ERP. Criar uma role PostgreSQL comum com o mesmo username não concede automaticamente acesso.

### Fluxo resumido

    POST /api/auth/login
            |
            +--> utilisateur ERP + BCrypt ----------------> JWT
            |
            +--> PostgreSQL SUPERUSER ----------------> ADMIN + SUPERADMIN + JWT

Documentação completa: docs/autenticacao.md

Arquitetura: docs/arquitetura.md
### Dépendances de l’Écosystème

## Authentification d’Entreprise

Brasil SaaS ERP utilise BrasilCloud Auth Service pour l’authentification centralisée.

Auth Service :

https://github.com/euripedesdark/auth-service

Licence :

AGPLv3

---

## Prérequis de l’Auth Service

L’Auth Service est conçu pour fonctionner avec :

- LDAP
- LDAPS
- Active Directory

Environnements recommandés :

### Linux

Samba Active Directory

Guide complet :

https://medium.com/meetcyber/full-guide-deploying-samba-active-directory-on-fedora-and-rocky-with-hybrid-dns-and-gpo-support-30f47fab1e90

### Windows

Windows Server Active Directory avec LDAPS activé.

---

## Remarque

BrasilCloudERP peut être évalué et exécuté indépendamment du déploiement complet de l’environnement d’entreprise.

Pour la production, il est recommandé d’utiliser l’Auth Service intégré à un annuaire LDAP/LDAPS.
