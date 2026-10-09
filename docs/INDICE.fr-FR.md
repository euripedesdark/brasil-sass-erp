# Index de la documentation — Brasil SaaS ERP

> Mis à jour le 04/10/2026 — inventaire vérifié à partir du code, de la base de données et du test smoke.

La racine du dépôt contient `README.md`, qui renvoie vers les quatre versions linguistiques du README principal. Le reste de la documentation est organisé par sujet ci-dessous.

## Documentation principale (quatre langues)

| Langue | Fichier |
|---|---|
| 🇧🇷 Portugais (Brésil) | [README.pt-BR.md](./i18n/README.pt-BR.md) |
| 🇺🇸 Anglais | [README.en-US.md](./i18n/README.en-US.md) |
| 🇪🇸 Espagnol | [README.es-ES.md](./i18n/README.es-ES.md) |
| 🇫🇷 Français | [README.fr-FR.md](./i18n/README.fr-FR.md) |

## Documentation active (état actuel du système)

| Document | Contenu |
|---|---|
| [Modules](modulos/) | Un fichier par module, avec les endpoints, tables et fonctionnalités |
| [Index des modules](modulos/README.md) | Vue d’ensemble des modules |
| [Navigation](navegacao.md) | Routes du frontend et structure des menus |
| [Architecture](arquitetura/) | Architecture du système |
| [Modèle de données](dados/modelo-dados.md) | Modèle de données (204 tables, selon le document source) |
| [Authentification](dados/autenticacao.md) | Authentification et autorisation |
| [Données de référence](dados/cadastro.md) | Module de données de référence |
| [Instructions du propriétaire](ia/INSTRUCOES-DO-DONO.md) | Instructions du propriétaire du système |
| [Infrastructure](infra/) | Build, déploiement, restauration et microservices |
| [Guide des modules](infra/README_MODULES.md) | Modules du système |
| [Guide des microservices](infra/README_MICROSSERVICOS.md) | Microservices fiscaux |
| [Guide de build](infra/BUILD.md) | Procédure de build |
| [Guide des migrations](infra/MIGRATION.md) | Migrations Flyway |
| [Guide de restauration](infra/RESTAURAR.md) | Restauration de la base de données |
| [Profils d’accès](perfis/) | Règles des profils |
| [Journal des modifications](guia/CHANGELOG.md) | Historique des modifications |

## Documents de travail du développement

> ⚠️ Ces fichiers sont des comptes rendus de sessions de développement produits avec l’aide d’une IA. Ils ne constituent pas la documentation officielle du produit.

| Document | Contenu |
|---|---|
| [Documents de travail IA](ia/) | Notes et recherches de développement |

## Références d’architecture et de données

| Domaine | Contenu |
|---|---|
| [Architecture](arquitetura/) | Architecture du système |
| [Données](dados/) | Authentification, données de référence et modèle de données |

## Audits et rapports (instantanés historiques)

> ⚠️ Ces documents décrivent le système à la date indiquée dans leur nom et peuvent ne plus refléter son état actuel. Pour l’état actuel, consultez les README et la documentation active ci-dessus.

| Document | Contenu |
|---|---|
| [Audits](auditorias/) | Audits et cartes de couverture |
| [Rapports](relatorios/) | Rapports de tests et diagnostics |
| [Incidents](incidentes/) | Registres d’incidents et coordination |
| [Audit de l’authentification](ia/AUDITORIA_AUTENTICACAO_FRONTEND_BACKEND_24-09-2026.md) | Audit du 24/09/2026 |
| [Rapport de parité fonctionnelle](ia/RELATORIO_PARIDADE_FUNCIONAL_ERP_25-09-2026.md) | Parité fonctionnelle du 25/09/2026 |

## Références de recherche

| Document | Contenu |
|---|---|
| [Répertoire de recherche](pesquisa/) | Recherche technique et analyses |
| [Manuel de l’API fiscale](pesquisa/MEGA-MANUAL-API-FISCAL.md) | Référence de l’API fiscale |
| [APIs transport, SPED, MDF-e et CT-e](pesquisa/APIS-DE-TRANSPORTE-SPED-MDFE-CTE.md) | APIs de transport et documents fiscaux |
| [Importation de NF-e par XML](pesquisa/IMPORTACAO-NFE-POR-XML.md) | Importer une NF-e depuis XML |
| [Enregistrement municipal](pesquisa/REGISTRO-da-prefeitura-NFSE.md) | Enregistrement municipal des factures de services |
| [Contrat unifié NFS-e](pesquisa/contrato-nfse-unico.md) | Contrat de réponse unifié |

## Décisions d’architecture

| Document | Contenu |
|---|---|
| [Architecture d’identité](ia/ARQUITETURA-IDENTIDADE.md) | Architecture d’identité |
| [Identité Active Directory](ia/IDENTIDADE-AD.md) | Intégration Active Directory |
| [Carte des microservices fiscaux](ia/MAPA-MICROSERVICOS-FISCAIS.md) | Microservices fiscaux |
| [Carte NFC-e](ia/MAPA-NFCE.md) | Facture électronique destinée aux consommateurs |

## Rapports en attente

| Document | Contenu |
|---|---|
| [Rapport NCM en attente](ia/RELATORIO-NCM-PENDENTES.md) | Classifications NCM en attente |
| [PDF ISSQN en attente](ia/ISSQN-PDFS-PENDENTE.md) | Fichiers PDF ISSQN en attente |
| [Élément de basculement en attente](ia/FAILOVER-4567-PENDENTE.md) | Élément de basculement 4567 |

## Internationalisation de la documentation

[Plan d’internationalisation Markdown](i18n/MARKDOWN_I18N_COMPLETO_2026-10-04.md) — périmètre et règles de traduction de la documentation Markdown propre au produit.
