> Mis à jour le 04/10/2026 — inventaire vérifié dans le code, la base de données et le test smoke.

# Cartographie des modules — menu, fonction et couverture

Cette vue compare les entrées du menu, les contrôleurs du backend, les endpoints et les écrans disponibles. Le nombre d’**endpoints sans écran** indique directement les lacunes fonctionnelles.

| Module | Éléments de menu | Contrôleurs | Endpoints | Sans écran | Couverture |
|---|---:|---:|---:|---:|---:|
| [Référentiels](./cadastro.fr-FR.md) | 10 | 11 | 54 | 0 | 100% |
| [Ventes](./vendas.fr-FR.md) | 2 | 2 | 11 | 0 | 100% |
| [Achats](./compras.fr-FR.md) | 4 | 4 | 16 | 0 | 100% |
| [Stocks](./estoque.fr-FR.md) | 9 | 9 | 33 | 0 | 100% |
| [Finance](./financeiro.fr-FR.md) | 16 | 12 | 69 | 0 | 100% |
| [Fiscalité](./fiscal.fr-FR.md) | 7 | 9 | 24 | 7 | 71% |
| [Production](./producao.fr-FR.md) | 4 | 4 | 16 | 0 | 100% |
| [Services](./servicos.fr-FR.md) | 3 | 1 | 10 | 0 | 100% |
| [Immobilisations (FI-AA + PM)](./ativos.fr-FR.md) | 3 | 2 | 46 | 0 | 100% |
| [Ressources humaines](./rh.fr-FR.md) | 4 | 4 | 20 | 0 | 100% |
| [Business Intelligence](./bi.fr-FR.md) | 3 | 5 | 47 | 18 | 62% |
| [Rapports](./relatorios.fr-FR.md) | 1 | 1 | 2 | 0 | 100% |
| [Intelligence artificielle](./ia.fr-FR.md) | 1 | 9 | 89 | 57 | 36% |
| [Socle et administration](./core.fr-FR.md) | 8 | 13 | 46 | 0 | 100% |
| **Total** | **72** | **84** | **437** | **82** | **81%** |

> `Sans écran` désigne un endpoint présent dans le backend mais appelé par aucun écran. L’opérateur ne peut donc pas accéder à cette fonctionnalité depuis l’application.
