# Documentação — Brasil SaaS ERP

> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

A raiz do repositório tem o `README.md` (ponte para os 4 idiomas) e os
READMEs em 4 idiomas. Tudo mais está aqui, organizado por assunto.

## Documentação principal (4 idiomas)

| Idioma | Arquivo |
|--------|---------|
| 🇧🇷 Português (Brasil) | [`./i18n/README.pt-BR.md`](./i18n/README.pt-BR.md) |
| 🇺🇸 English | [`./i18n/README.en-US.md`](./i18n/README.en-US.md) |
| 🇪🇸 Español | [`./i18n/README.es-ES.md`](./i18n/README.es-ES.md) |
| 🇫🇷 Français | [`./i18n/README.fr-FR.md`](./i18n/README.fr-FR.md) |

## Docs vivos (estado atual do sistema)

| Documento | Conteúdo |
|-----------|----------|
| [`modulos/`](modulos/) | Um arquivo por módulo — endpoints, tabelas, features |
| [`modulos/README.md`](modulos/README.md) | Índice dos módulos |
| [`navegacao.md`](navegacao.md) | Rotas do frontend e estrutura de menus |
| [`roadmap.md`](roadmap.md) | Fases concluídas e próximas |
| [`arquitetura/`](arquitetura/) | Arquitetura do sistema |
| [`dados/modelo-dados.md`](dados/modelo-dados.md) | Modelo de dados (204 tabelas) |
| [`dados/autenticacao.md`](dados/autenticacao.md) | Autenticação e autorização |
| [`dados/cadastro.md`](dados/cadastro.md) | Módulo de cadastros |
| [`INSTRUCOES-DO-DONO.md`](./ia/INSTRUCOES-DO-DONO.md) | Instruções do dono do sistema |
| [`infra/`](infra/) | Build, deploy, restore, microsserviços |
| [`infra/README_MODULES.md`](infra/README_MODULES.md) | Módulos do sistema |
| [`infra/README_MICROSSERVICOS.md`](infra/README_MICROSSERVICOS.md) | Microsserviços fiscais |
| [`infra/BUILD.md`](infra/BUILD.md) | Como buildar |
| [`infra/MIGRATION.md`](infra/MIGRATION.md) | Migrations Flyway |
| [`infra/RESTAURAR.md`](infra/RESTAURAR.md) | Como restaurar o banco |
| [`perfis/`](perfis/) | Regras de perfil |
| [`guia/CHANGELOG.md`](guia/CHANGELOG.md) | Changelog |


## Documentos de trabalho (gerados por IA)

> ⚠️ Os documentos abaixo são registros de sessão de desenvolvimento com
> assistente IA. Não são documentação oficial do sistema.

| Documento | Conteúdo |
|-----------|----------|
| [`ia/`](ia/) | 31 documentos de trabalho gerados por IA |

## Documentação de arquitetura

| Documento | Conteúdo |
|-----------|----------|
| [](arquitetura/) | Arquitetura do sistema |

## Documentação de dados

| Documento | Conteúdo |
|-----------|----------|
| [](dados/) | Autenticação, cadastros, modelo de dados |

## Auditorias e relatórios (registros históricos)

> ⚠️ Os documentos abaixo são **retratos pontuais** da data do arquivo.
> Não refletem o estado atual do sistema. Para o estado atual, veja os
> READMEs e os docs vivos acima.

| Documento | Data | Conteúdo |
|-----------|------|----------|
| [`auditorias/`](auditorias/) | 22–30/09/2026 | Auditorias e mapas de cobertura |
| [`relatorios/`](relatorios/) | 21–25/09/2026 | Relatórios de teste e diagnóstico |
| [`incidentes/`](incidentes/) | — | Incidentes e coordenação |
| [`./ia/AUDITORIA_AUTENTICACAO_FRONTEND_BACKEND_24-09-2026.md`](./ia/AUDITORIA_AUTENTICACAO_FRONTEND_BACKEND_24-09-2026.md) | 24/09/2026 | Auditoria de autenticação |
| [`./ia/RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md`](./ia/RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md) | 25/09/2026 | Paridade funcional |

## Pesquisa (referência)

| Documento | Conteúdo |
|-----------|----------|
| [`pesquisa/`](pesquisa/) | Pesquisas técnicas e análises |
| [`pesquisa/MEGA-MANUAL-API-FISCAL.md`](pesquisa/MEGA-MANUAL-API-FISCAL.md) | Manual da API fiscal |
| [`pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md`](pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md) | APIs de transporte |
| [`pesquisa/IMPORTACAO-NFE-POR-XML.md`](pesquisa/IMPORTACAO-NFE-POR-XML.md) | Importação de NF-e por XML |
| [`pesquisa/REGISTRO-da-prefeitura-NFSE.md`](pesquisa/REGISTRO-da-prefeitura-NFSE.md) | Registro na prefeitura |
| [`pesquisa/contrato-nfse-unico.md`](pesquisa/contrato-nfse-unico.md) | Contrato único de resposta |

## Decisões de arquitetura

| Documento | Conteúdo |
|-----------|----------|
| [`./ia/ARQUITETURA-IDENTIDADE.md`](./ia/ARQUITETURA-IDENTIDADE.md) | Arquitetura de identidade |
| [`./ia/IDENTIDADE-AD.md`](./ia/IDENTIDADE-AD.md) | Identidade com Active Directory |
| [`./ia/MAPA-MICROSERVICOS-FISCAIS.md`](./ia/MAPA-MICROSERVICOS-FISCAIS.md) | Mapa dos microsserviços fiscais |
| [`./ia/MAPA-NFCE.md`](./ia/MAPA-NFCE.md) | Mapa da NFC-e |

## Relatórios pendentes

| Documento | Conteúdo |
|-----------|----------|
| [`./ia/RELATORIO-NCM-PENDENTES.md`](./ia/RELATORIO-NCM-PENDENTES.md) | NCMs pendentes |
| [`./ia/ISSQN-PDFS-PENDENTE.md`](./ia/ISSQN-PDFS-PENDENTE.md) | PDFs de ISSQN pendentes |
| [`./ia/FAILOVER-4567-PENDENTE.md`](./ia/FAILOVER-4567-PENDENTE.md) | Failover 4567 pendente |


## Internacionalização da documentação

[`i18n/MARKDOWN_I18N_COMPLETO_2026-10-04.md`](i18n/MARKDOWN_I18N_COMPLETO_2026-10-04.md) — escopo e regras para a internacionalização dos arquivos Markdown próprios do produto.

