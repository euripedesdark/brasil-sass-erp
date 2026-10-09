> Updated on 2026-10-04 — inventory checked against code, database, and smoke test.

# Module map — menu, purpose, and coverage

This overview compares menu entries, backend controllers, endpoints, and available screens. The number of **endpoints without a screen** directly indicates the functionality gap.

| Module | Menu items | Controllers | Endpoints | No screen | Coverage |
|---|---:|---:|---:|---:|---:|
| [Master Data](./cadastro.md) | 10 | 11 | 54 | 0 | 100% |
| [Sales](./vendas.md) | 2 | 2 | 11 | 0 | 100% |
| [Purchasing](./compras.md) | 4 | 4 | 16 | 0 | 100% |
| [Inventory](./estoque.md) | 9 | 9 | 33 | 0 | 100% |
| [Finance](./financeiro.md) | 16 | 12 | 69 | 0 | 100% |
| [Tax](./fiscal.md) | 7 | 9 | 24 | 7 | 71% |
| [Production](./producao.md) | 4 | 4 | 16 | 0 | 100% |
| [Services](./servicos.md) | 3 | 1 | 10 | 0 | 100% |
| [Assets (FI-AA + PM)](./ativos.md) | 3 | 2 | 46 | 0 | 100% |
| [Human Resources](./rh.md) | 4 | 4 | 20 | 0 | 100% |
| [Business Intelligence](./bi.md) | 3 | 5 | 47 | 18 | 62% |
| [Reports](./relatorios.md) | 1 | 1 | 2 | 0 | 100% |
| [Artificial Intelligence](./ia.md) | 1 | 9 | 89 | 57 | 36% |
| [Core and Administration](./core.md) | 8 | 13 | 46 | 0 | 100% |
| **Total** | **72** | **84** | **437** | **82** | **81%** |

> `No screen` means a backend endpoint that is not called by any screen. The operator cannot access that functionality through the application.
