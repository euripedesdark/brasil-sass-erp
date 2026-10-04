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

Le système a **22 modules métier** sur un noyau commun
(`core`), tous avec controller, service, repository et écran.
Chiffres vérifiés le **03/10/2026** dans le code :

| | |
|---|---|
| Classes Java | **762** |
| Repositories Spring Data | **174** |
| Services | **147** |
| Endpoints REST | **451** |
| Tables PostgreSQL (schema `brasil_saas`) | **204** |
| Composants React | **123** |
| Migrations Flyway dans le repo | **15** (`V109`…`V126`) |

### 💰 Financeiro

> **Statut** : ✅ implémenté et répondant (`/api/financeiro/*` → 200)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Comptes à Payer/Recevoir** | Titres, échéances, flux d'approbation multiniveau, alertes d'échéance |
| ✅ **Baisses** | Avec remises, intérêts, pénalités et annulations |
| ✅ **Trésorerie** | Caisse, projection de flux de trésorerie, prêts et placements financiers |
| ✅ **Comptabilité Générale** | Écritures et comptes comptables, plan de comptes, centres de coûts |
| ✅ **Banques** | Comptes bancaires, relevé, rapprochement bancaire, remises et retours (CNAB/OFX) |
| ✅ **Budgets** | Budget vs réalisé |
| ✅ **Commissions** | Règles de commission sur ventes et OS |
| ✅ **Renégociation** | Renégociation de titres |
| ✅ **Provision PDD** | Provision pour créances douteuses |
| ✅ **Analyse de Rentabilité** | Par centre de coûts, plan de comptes et période |

**Endpoints** : `/api/financeiro/titulos`, `/lancamentos`, `/orcamentos`,
`/emprestimos`, `/planos-contas`, `/centros-custo`, `/contas-bancarias`,
`/caixas`, `/condicoes-pagamento`, `/tipos-pagamento`, `/conciliacao`,
`/extrato`, `/comissoes`, `/renegociacao`, `/remessas`, `/retornos` …
(55 endpoints dans le module)

---

### 🏙️ Cadastres

| Registre | Fonctionnalités |
|----------|-----------------|
| ✅ **Municipalités** | Table officielle IBGE, recherche par code ou nom, CEP |
| ✅ **Personnes** | Registre unique PF/PJ, contacts, adresses |
| ✅ **Clients / Fournisseurs** | Limite de crédit, logo, évaluation, historique |
| ✅ **Produits** | Variations, kits, NCM, images (MongoDB), e-commerce |
| ✅ **Services** | Pour NFS-e, intégration avec OS |
| ✅ **Catégories / Marques / Unités de Mesure / Transporteurs** | Support au registre de produits et fret |

**Endpoints** : `/api/cadastro/produtos`, `/clientes`, `/fournisseurs`,
`/pessoas`, `/servicos`, `/categorias`, `/marcas`, `/unidades-medida`,
`/transportadoras` + `/api/municipios` (31 endpoints dans le module)

---

### 📋 Services et Ventes

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Ordre de Service (OS)** | Émission, items, pointages, PDF, émission de NFS-e intégrée |
| ✅ **Commandes de Vente** | Intégration avec stock, finance et fiscal |
| ✅ **PDV** | Écran de point de vente |
| ✅ **Tables de Prix** | Items par produit |
| ✅ **Bonifications et Retours** | Avec items |
| ✅ **Contrats de Vente** | Avec items et règles de commission |

**Endpoints** : `/api/servicos/os`, `/api/vendas/pedidos`,
`/api/vendas/tabelas-preco` … (15 endpoints dans les deux modules)

---

### 🛒 Achats et Supply Chain

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Commandes d'Achat** | Avec items, intégration avec fournisseurs |
| ✅ **Réceptions** | Confirmation d'items |
| ✅ **Confirmation de Factures** | Validation de facture contre le reçu |
| ✅ **Supply Chain** | Demandes d'achat et cotations avec carte des fournisseurs |
| ✅ **Contrats d'Achat** | Avec items |

**Endpoints** : `/api/compras/pedidos`, `/recebimentos`,
`/supply-chain/solicitacoes`, `/supply-chain/cotacoes` … (12 endpoints)

---

### 📦 Stock

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Dépôts et Adresses** | Occupation par adresse |
| ✅ **Soldes, Lots et Séries** | Traçabilité |
| ✅ **Mouvements** | Entrées, sorties, ajustements |
| ✅ **Réserves et Transferts** | Entre dépôts |
| ✅ **Inventaires** | Avec items |
| ✅ **Expéditions** | Avec items |

**Endpoints** : `/api/estoque/depositos`, `/saldos`, `/movimentacoes`,
`/lotes`, `/reservas`, `/transferencias`, `/inventarios`, `/expedicoes` …
(17 endpoints)

---

### 🏭 Production Industrielle (PCP)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Ordres de Production** | Flux complet : intrants → processus → produit final |
| ✅ **Structure de Produit (BOM)** | Par produit parent |
| ✅ **Routages et Opérations** | Séquence d'opérations |
| ✅ **Centres de Travail et Capacité** | Planification par capacité |
| ✅ **Pointages** | Par production, employé, période et statut ; statistiques |
| ✅ **Romaneios** | Avec items |
| ✅ **MRP** | Planification des besoins |

**Endpoints** : `/api/producao/estruturas`, `/roteiros`, `/centros-trabalho`,
`/capacidade`, `/apontamentos`, `/romaneios`, `/mrp` … (30 endpoints)

---

### 📊 Comptabilité

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Écritures et Comptes** | Comptabilité en partie double |
| ✅ **Balance, Bilan, DRE, Grand Livre** | Rapports comptables |
| ✅ **Clôtures** | Clôture de période |

**Endpoints** : `/api/contabilidade/lancamentos`, `/balancete`, `/balanco`,
`/dre`, `/razao`, `/fechamentos` (15 endpoints)

---

### 🏢 Actifs (Gestion d'Actifs)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Actif Immobilisé** | Registre des biens |
| ✅ **Maintenances** | Historique de maintenance des actifs |

**Endpoints** : `/api/ativos/manutencoes` (4 endpoints)

---

### 📁 DMS (Gestion de Documents)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Documents** | Avec contenu, versions et approbations |
| ✅ **Rétention** | Politique de rétention |

**Endpoints** : `/api/dms/documentos`, `/retencao`, `/versoes/{id}/download` (9 endpoints)

---

### ✅ Qualité

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Plans d'Inspection** | Plans par produit/processus |
| ✅ **Inspections** | Enregistrement d'inspection |
| ✅ **Non-conformités** | Traitement des NC |

**Endpoints** : `/api/qualidade/planos`, `/inspecoes`, `/nao-conformidades` (7 endpoints)

---

### 📈 Projets

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Projets** | Résumé, étapes, mouvements |
| ✅ **Facturation** | Facturation par projet |
| ✅ **Risques et Changements** | Enregistrement des risques et changements |

**Endpoints** : `/api/projetos`, `/projetos/{id}/resumo`, `/etapas`,
`/movimentos`, `/faturamentos`, `/riscos`, `/mudancas` (14 endpoints)

---

### 🏗️ WMS (Warehouse)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Ondes de Picking** | Avec items |
| ✅ **Volumes** | Avec items |
| ✅ **Putaway** | Adressage de charge |

**Endpoints** : `/api/wms/ondas`, `/volumes`, `/putaway` (14 endpoints)

---

### 🔄 Workflow

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Définitions** | Avec étapes |
| ✅ **Instances et Tâches** | Tâches en attente par utilisateur |

**Endpoints** : `/api/workflow/definitions`, `/instances`, `/tasks/pendentes` (10 endpoints)

---

### 🌐 Portails

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Accès** | Contrôle d'accès aux portails |
| ✅ **Validation Publique** | Validation et « mon compte » sans login ERP |

**Endpoints** : `/api/portais/acessos`, `/publico/validar`, `/publico/minha-conta` (5 endpoints)

---

### 👥 RH

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Employés** | Lien avec personne, poste, photo |
| ✅ **Postes** | Structure des postes |
| ✅ **Paie** | Avec items |

**Endpoints** : `/api/rh/funcionarios`, `/cargos`, `/folhas` (14 endpoints)

---

### 🤝 CRM

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Leads et Opportunités** | Pipeline de ventes |
| ✅ **Activités et Tâches** | Suivi |
| ✅ **Forecast** | Prévision de ventes |

**Endpoints** : `/api/crm/leads`, `/pipeline`, `/forecast`, `/atividades` (10 endpoints)

---

### 📊 BI (Intelligence d'Affaires)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Dashboards** | Publics, par type et par utilisateur, avec widgets |
| ✅ **KPIs et Indicateurs** | Calcul de KPI par type |
| ✅ **Rapports** | Par catégorie, exportation **PDF, Excel et CSV** |
| ✅ **Rapports Planifiés** | Par fréquence, file d'attente |
| ✅ **Reports avec Paramètres** | Rapports paramétrés |

**Endpoints** : `/api/bi/dashboards`, `/kpis`, `/indicadores`, `/relatorios`,
`/relatorios-agendados`, `/reports` … (46 endpoints — le plus grand module)

---

### 🤖 IA Assistée

| Ressource | Statut |
|---------|--------|
| ✅ **Chat avec IA** | Sessions et messages persistés |
| ✅ **Assistant ERP** | Avec audit d'utilisation |
| ✅ **Embeddings** | Recherche sémantique par entité |
| ✅ **Classifications et Analyses Prédictives** | Modèles entraînables |
| ✅ **Prompts et Templates** | Bibliothèque réutilisable |
| ⚠️ **Fournisseur** | Configurable (`app.ia.provider`, défaut `openai`) ; nécessite une clé API valide |

**Endpoints** : `/api/ia/config`, `/sessoes`, `/mensagens`, `/prompts`,
`/prompt-templates`, `/embeddings`, `/classificacoes`, `/analises` …
(74 endpoints)

---

### 🔐 Core, Auth et Superadmin

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Authentification** | JWT, login en base, login AD (SPNEGO), refresh token |
| ✅ **Utilisateurs, Profils et Permissions** | Autorisation par permission (`@PreAuthorize`) |
| ✅ **Multi-entreprise** | Utilisateur ↔ entreprise(s), isolation par entreprise |
| ✅ **Superadmin** | Catalogue SQL, modules par utilisateur, profils disponibles |
| ✅ **Audit** | Journal d'accès, notifications, sessions |

**Endpoints** : `/api/auth/login`, `/login/database`, `/login/ad`, `/refresh`,
`/me` + `/api/core/perfil`, `/core/minha-empresa` + `/api/superadmin/usuarios`,
`/superadmin/sql/catalogo` … (33 endpoints)

---

### 🖨️ Rapports

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Rapports par Type** | HTML et PDF (`/api/relatorios/{tipo}`, `/api/relatorios/pdf/{tipo}`) |

---

### 📄 Documents (shared)

| Fonctionnalité | Description |
|----------------|-----------|
| ✅ **Contenu de Document** | `/api/documentos/{id}/conteudo` — fichiers enregistrés dans MongoDB |

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

## 📊 Autres modules — structure et ce qui répond

### Pourquoi il n'y a pas de % unique ici

**Compter les fichiers ne mesure pas la prêts.** J'ai mesuré les modules par les mêmes
6 livrables du fiscal et presque tous donnent 100% structurel — parce que
tous ont model, repository, service, controller et écran. Cela ne
signifie pas que le registre de client s'ouvre, que le titre baisse ou que
la paie calcule. Cela signifie que les fichiers existent.

Ce qui mesure, c'est exécuter. Alors il y a deux colonnes :

| Module | Tables | Endpoints | Écran | Structurel | Répond (03/10/2026) |
|--------|:-------:|:---------:|:-----:|:----------:|:----------------------:|
| Cadastro | 21 | 31 | ✅ | 6/6 | ✅ |
| Financeiro | 32 | 55 | ✅ | 6/6 | ✅ |
| Estoque | 13 | 17 | ✅ | 5/6 | ✅ |
| Fiscal | 30 | 28 | ✅ | 6/6 | ⚠️ partiel (voir % fiscal) |
| Vendas | 11 | 7 | ✅ | 6/6 | ✅ |
| Compras | 11 | 12 | ✅ | 6/6 | ✅ |
| Produção | 10 | 30 | ✅ | 6/6 | ✅ |
| RH | 4 | 14 | ✅ | 6/6 | ✅ |
| Serviços | 3 | 8 | ✅ | 6/6 | ✅ |
| BI | 8 | 46 | ✅ | 6/6 | ✅ |
| Core/Auth | 18 | 33 | ✅ | 6/6 | ✅ |
| IA | 11 | 74 | ⚠️ 1 écran | 6/6 | ⚠️ partiel |
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

**Structurel** = les 6 livrables existent comme fichier.
**Répond** = l'API a répondu dans un smoke test avec token valide le
**03/10/2026**.

⚠️ **Aucun de ceux-ci n'a été validé fonctionnellement.** J'ai ouvert l'écran, non.
J'ai créé un enregistrement, non. Ce que le smoke test prouve, c'est que la **route
répond** ; cela ne prouve pas que la donnée retournée est correcte. Les deux
choses sont différentes.

### Ce que le smoke test a trouvé (03/10/2026)

67 routes appelées, une ou plus par module :

```
62  2xx   répondent
 5  400   paramètre obligatoire manquant (la route existe)
 0  500   aucune erreur
```

Les 400 sont le comportement attendu de validation
(`/api/bi/indicadores/dashboard`, `/api/ia/config`, `/api/ia/sessoes`,
`/api/producao/apontamentos/por-status`, `/api/producao/apontamentos/por-periodo`,
`/api/contabilidade/balancete`, `/api/fiscal/sefaz/status`,
`/api/wms/putaway`) — ils demandent un paramètre que le test n'a pas passé.

### Le bug de RH a été corrigé

Le smoke test de 26/09/2026 a trouvé un **500** réel :
`GET /api/rh/funcionarios` cassait avec
`LazyInitializationException` — `Funcionario` a
`@ManyToOne(fetch = LAZY)` pour `Cargo`, et Jackson sérialisait le
proxy après la fermeture de la session Hibernate.

**En 03/10/2026 c'est corrigé :** `/api/rh/funcionarios/pessoas`,
`/api/rh/cargos` et `/api/rh/folhas` répondent **200**. La correction
a été de chercher avec `join fetch` sur `Cargo` (ou DTO), ce qui évite que le
serializer touche le proxy hors de la session.

### Estoque n'a pas de couche de service

9 controllers, 12 repositories, **0 services**. La règle de métier
est dans le controller, parlant direct au repository. Ça fonctionne,
mais il n'y a pas où tester et ça n'isole pas la règle. Dans les autres modules la
séparation existe — c'est une incohérence à décider, pas un accident.

## ✅ Ce Qui Fonctionne

Vérifié le **03/10/2026** via `systemctl` et `ss`.

| Service (systemd) | Port | État |
|-------------------|-------|--------|
| `brasil-saas-erp.service` (Spring Boot, user `brasilsaas`) | 8080 | 🟢 running |
| `nfse-sp-api.service` (Java, primaire) | 4568 | 🟢 running |
| `nfse-sp-bridge.service` (Ruby, fallback) | 4569 | 🟢 running |
| `brasil_saas-watchdog.service` (vérification de contrat) | — | 🟢 running |
| `brasil_saas-minio.service` | 9000 / 9001 | 🟢 running |
| `auth-service.service` | 8081/8082 | 🟢 running |
| PostgreSQL (schema `brasil_saas`) | 5432 | 🟢 up |
| MongoDB | 27017 | 🟢 up (4 collections) |
| RabbitMQ | 5672 / 15672 | 🟢 up |
| Redis | 6379 | 🟢 up |
| nginx (proxy du domaine) | 80 / 443 | 🟢 up |

L'ERP tourne du jar
`/opt/brasil-saas-erp/brasil-saas-erp-1.0.0-SNAPSHOT.jar`
(unit `brasil-saas-erp.service`). Les microservices de NF-e tournent des
jars dans `src/main/resources/microservices/` — **attention** : sur
le serveur, les jars actifs sont dans
`/home/euripedes/BrasilCloudERP/src/main/resources/microservices/`
(autre checkout), pas dans ce repo.

### ⚠️ Le proxy failover de NFS-e (4567) est tombé

L'ERP appelle l'émission de NFS-e sur
`brasil-saas.fiscal.nfse.url`, qui par défaut est
`http://127.0.0.1:4567/api/nfse-sp` — le **proxy failover**. En
03/10/2026 **rien n'écoute sur 4567** (connexion refusée), bien que les
deux implémentations (4568 Java, 4569 Ruby) et le watchdog soient debout.

Conséquence : à moins que `/etc/brasil-saas/erp.env` (fichier de
l'utilisateur `brasilsaas`, illisible sans sudo) ne surcharge l'URL vers la
4568, **l'émission de NFS-e est cassée maintenant**. Preuve : la dernière
NFS-e en base est de **26/09/2026** (nota 33), avec 4 enregistrements
`FALHA_EMISSAO` ce jour-là et aucune tentative depuis.

Action : lever le proxy de la 4567 (ou pointer
`brasil-saas.fiscal.nfse.url` directement vers
`http://127.0.0.1:4568/api/nfse-sp`), et enregistrer dans `erp.env` quelle
URL est en usage.

### Ce que l'ERP valide seul

| Vérification | Résultat (03/10/2026) |
|-------------|------------------------|
| `GET /api/fiscal/mdfe/status` → SVRS | **200** (`cStat 107`) |
| `GET /api/fiscal/cte/status` → SEFAZ SP | **200** (`cStat 107`) |
| `GET /api/fiscal/sped/efd/exemplo` | **200** (fichier valide) |
| `GET /api/fiscal/nfse/retornos/recusas` et `/para-conferir` | **200** |

Le MDF-e est servi par la **SVRS** (`ufAtendente: RS`) et le CT-e par le
**portail de SP** (`ufAtendente: SP`), avec le **même A1**. Ce sont
des autoriseurs différents, pas des certificats différents.

### Ce qui n'est pas encore prêt

| Item | Situation |
|------|----------|
| **Proxy failover 4567** | Tombé ; URL par défaut de l'ERP. Voir ci-dessus. |
| **NF-e / NFC-e** | `NFeServiceImpl` a **3 TODOs**. L'interface promet `emitirNFe`, `cancelarNFe` et `consultarSituacao` ; aucune des trois n'est implémentée. |
| **Émission de MDF-e** | La connexion avec la SVRS est prête (`cStat 107`) et le certificat signe. **L'assemblage du document n'existe pas** : `MdfeEmissaoService` n'a que `statusServico` et `consultarRecibo`. |
| **Émission de CT-e** | Idem. `CteEmissaoService` n'a que `statusServico`. |
| **SPED EFD Contribuições** | La bibliothèque résout dans le `pom.xml`, mais **pas d'endpoint** — seul l'ICMS/IPI en a. |
| **IA (Spring AI)** | Module présent (74 endpoints), avec embeddings, chat et classifications. N'a pas été validé fonctionnellement ; nécessite une clé API valide. |
| **Écran de l'IA** | 1 écran seul (`IaAssistWidget`) ; les 74 endpoints n'ont pas d'écran équivalent. |
| **Validation du CIOT** | `cStat 684` entre en production à la SEFAZ le **23/11/2026**. Le `infCIOT` est déjà dans le XSD ; il manque la validation dans l'émetteur. |

**Pourquoi le MDF-e et le CT-e ne sont pas dans la liste des « prêts » malgré
leur réponse `cStat 107` :** le `cStat 107` est le *status du service*, et
prouve que la chaîne TLS, le certificat du client et l'envelope SOAP
sont corrects. Cela **ne prouve pas que le XML du document soit là**, car
aucun document n'a été assemblé ni envoyé. Ce sont deux choses différentes, et
le README les sépare pour ça.

---

## 📡 Endpoints de l'API

Le backend expose **451 endpoints REST** (`/api/**`). La liste
complète est dans [`docs/INDICE.md`](docs/INDICE.md) et dans
le Swagger UI : <http://localhost:8080/swagger-ui.html>.

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
GET    /api/indicadores                     /indicadores/categoria/{categoria}
GET    /api/relatorios/{id}                 /relatorios/{id}/pdf  /excel  /csv
GET    /api/relatorios-agendados            /relatorios-agendados/pendentes
GET    /api/reports/{id}                    /reports/category/{category}
```

### Fiscal — NFS-e São Paulo

L'émission est faite par une API séparée. L'ERP appelle
`brasil-saas.fiscal.nfse.url` (par défaut
`http://127.0.0.1:4567/api/nfse-sp`, le proxy failover —
voir l'avis dans « Ce Qui Fonctionne »). Les implémentations
actives sont la **Java sur la 4568** (primaire) et la **Ruby sur la 4569**
(fallback), surveillées par le `brasil_saas-watchdog`, qui vérifie le
**contrat** de réponse, pas seulement le port.

```
POST   /api/fiscal/nfse/emitir
POST   /api/fiscal/nfse/{id}/cancelar
GET    /api/fiscal/nfse
GET    /api/fiscal/nfse/{id}/xml
GET    /api/fiscal/nfse/{id}/pdf
GET    /api/fiscal/nfse/{id}/retornos
GET    /api/fiscal/nfse/{id}/retornos/{retornoId}/bruto
GET    /api/fiscal/nfse/retornos/recusas           seulement les refusées
GET    /api/fiscal/nfse/retornos/para-conferir     celles qu'on n'a pas pu savoir
```

Le motif du refus de la mairie est enregistré **avant** que l'exception
remonte, avec le `cStat` et le corps brut dans Mongo. L'écran
d'erreur est un mur ; l'enregistrement non.

### Fiscal — MDF-e et CT-e

```
GET    /api/fiscal/mdfe/status                     cStat 107 si la SVRS est debout
GET    /api/fiscal/mdfe/recibo?numero={recibo}     2ème appel : du reçu vers la clé
GET    /api/fiscal/cte/status                      cStat 107 si la SEFAZ est debout
```

Les deux utilisent le même contrat de réponse, avec les **trois
états** de `sucesso` : `true` (autorisé), `false`
(refusé, avec `cStat` et motif) et `null` (**on n'a pas pu
savoir**). Le troisième état existe pour ne pas dupliquer le
document : la SVRS rejette la clé naturelle répétée, et réémettre
à l'aveugle est exactement ce qui crée la duplication.

Le MDF-e rend **recibo** au premier appel et
**protocolo** au second. Lire la clé dans la réponse de
l'envoi donne NPE — la méthode n'existe pas.

### Fiscal — SPED EFD ICMS/IPI

```
POST   /api/fiscal/sped/efd/gerar                  génère le fichier EFD
GET    /api/fiscal/sped/efd/exemplo                génère celui d'exemple, pour vérifier le format
```

**EFD n'est envoyé à personne.** Le fichier est généré,
signé et gardé ; qui cherche après c'est la SEFAZ ou la
Receita. Pas de web service, pas de protocole, pas de file.

### Fiscal — consultation et distribution

```
GET    /api/fiscal/sefaz/status
GET    /api/fiscal/sefaz/consultar
GET    /api/fiscal/sefaz/distribuicao
```

### Fiscal — tables et registres

```
GET    /api/fiscal/cest?busca={termo}              recherche par code, description ou NCM
GET    /api/fiscal/cfop?tipoOperacao=ENTRADA|SAIDA
GET    /api/fiscal/ncm
GET    /api/fiscal/ncm/{codigo}
GET    /api/fiscal/issqn
GET    /api/fiscal/impostos
GET    /api/fiscal/certificados
```

`GET /api/cadastro/servicos?nome={termo}` alimente l'autocomplete
du nom du service, qui suggère un service déjà enregistré
pendant que la personne tape. Deux registres avec un nom
similaire et un code municipal différent est la cause la plus
courante de refus à la mairie.

### Demais módulos (racines)

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
