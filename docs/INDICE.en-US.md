# Documentation index — Brasil SaaS ERP

> Updated on 2026-10-04 — inventory checked against the code, database, and smoke test.

The repository root contains `README.md`, which links to the four language versions of the main README. The remaining documentation is organized here by topic.

## Main documentation (four languages)

| Language | File |
|---|---|
| 🇧🇷 Portuguese (Brazil) | [README.pt-BR.md](./i18n/README.pt-BR.md) |
| 🇺🇸 English | [README.en-US.md](./i18n/README.en-US.md) |
| 🇪🇸 Spanish | [README.es-ES.md](./i18n/README.es-ES.md) |
| 🇫🇷 French | [README.fr-FR.md](./i18n/README.fr-FR.md) |

## Living documentation (current system behavior)

| Document | Contents |
|---|---|
| [Modules](modulos/) | One file per module, covering endpoints, tables, and features |
| [Module index](modulos/README.md) | Module overview |
| [Navigation](navegacao.md) | Frontend routes and menu structure |
| [Architecture](arquitetura/) | System architecture |
| [Data model](dados/modelo-dados.md) | Data model (204 tables, as recorded in the source document) |
| [Authentication](dados/autenticacao.md) | Authentication and authorization |
| [Master data](dados/cadastro.md) | Master-data module |
| [Owner instructions](ia/INSTRUCOES-DO-DONO.md) | System-owner instructions |
| [Infrastructure](infra/) | Build, deployment, restore, and microservices |
| [Module infrastructure guide](infra/README_MODULES.md) | System modules |
| [Microservices guide](infra/README_MICROSSERVICOS.md) | Tax microservices |
| [Build guide](infra/BUILD.md) | How to build |
| [Migration guide](infra/MIGRATION.md) | Flyway migrations |
| [Restore guide](infra/RESTAURAR.md) | Database restoration |
| [Access profiles](perfis/) | Profile rules |
| [Changelog](guia/CHANGELOG.md) | Change history |

## Development working documents

> ⚠️ These files are development-session records produced with AI assistance. They are not official product documentation.

| Document | Contents |
|---|---|
| [AI working documents](ia/) | Development notes and investigations |

## Architecture and data references

| Area | Contents |
|---|---|
| [Architecture](arquitetura/) | System architecture |
| [Data](dados/) | Authentication, master data, and data model |

## Audits and reports (historical snapshots)

> ⚠️ These documents describe the system at the date in each filename. They may not reflect the current state. Use the README files and living documentation above for current information.

| Document | Contents |
|---|---|
| [Audits](auditorias/) | Audits and coverage maps |
| [Reports](relatorios/) | Test and diagnostic reports |
| [Incidents](incidentes/) | Incident records and coordination |
| [Authentication audit](ia/AUDITORIA_AUTENTICACAO_FRONTEND_BACKEND_24-09-2026.md) | Authentication audit dated 2026-09-24 |
| [Functional parity report](ia/RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md) | Functional parity report dated 2026-09-25 |

## Research references

| Document | Contents |
|---|---|
| [Research directory](pesquisa/) | Technical research and analysis |
| [Tax API manual](pesquisa/MEGA-MANUAL-API-FISCAL.md) | Tax API reference |
| [Transport, SPED, MDF-e, and CT-e APIs](pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md) | Transport and fiscal document APIs |
| [NF-e XML import](pesquisa/IMPORTACAO-NFE-POR-XML.md) | Importing NF-e from XML |
| [Municipal registration](pesquisa/REGISTRO-da-prefeitura-NFSE.md) | Municipal service-invoice registration |
| [Unified NFS-e contract](pesquisa/contrato-nfse-unico.md) | Unified response contract |

## Architecture decisions

| Document | Contents |
|---|---|
| [Identity architecture](ia/ARQUITETURA-IDENTIDADE.md) | Identity architecture |
| [Active Directory identity](ia/IDENTIDADE-AD.md) | Active Directory integration |
| [Tax microservices map](ia/MAPA-MICROSERVICOS-FISCAIS.md) | Tax microservices |
| [NFC-e map](ia/MAPA-NFCE.md) | Consumer electronic invoice |

## Pending reports

| Document | Contents |
|---|---|
| [Pending NCM report](ia/RELATORIO-NCM-PENDENTES.md) | Pending NCM classifications |
| [Pending ISSQN PDFs](ia/ISSQN-PDFS-PENDENTE.md) | Pending ISSQN PDF files |
| [Pending failover item](ia/FAILOVER-4567-PENDENTE.md) | Failover item 4567 |

## Documentation internationalization

[Markdown internationalization plan](i18n/MARKDOWN_I18N_COMPLETO_2026-10-04.md) — scope and rules for translating first-party Markdown documentation.
