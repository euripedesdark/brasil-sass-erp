> Updated on 2026-10-04 — inventory checked against code, database, and smoke test.

# Module map — menu, purpose, and coverage

This overview compares menu entries, backend controllers, endpoints, and available screens. The number of **endpoints without a screen** directly indicates the functionality gap.

| Module | Menu items | Controllers | Endpoints | No screen | Coverage |
|---|---:|---:|---:|---:|---:|
| [Master Data](./cadastro.en-US.md) | 10 | 11 | 54 | 0 | 100% |
| [Sales](./vendas.en-US.md) | 2 | 2 | 11 | 0 | 100% |
| [Purchasing](./compras.en-US.md) | 4 | 4 | 16 | 0 | 100% |
| [Inventory](./estoque.en-US.md) | 9 | 9 | 33 | 0 | 100% |
| [Finance](./financeiro.en-US.md) | 16 | 12 | 69 | 0 | 100% |
| [Tax](./fiscal.en-US.md) | 7 | 9 | 24 | 7 | 71% |
| [Production](./producao.en-US.md) | 4 | 4 | 16 | 0 | 100% |
| [Services](./servicos.en-US.md) | 3 | 1 | 10 | 0 | 100% |
| [Assets (FI-AA + PM)](./ativos.en-US.md) | 3 | 2 | 46 | 0 | 100% |
| [Human Resources](./rh.en-US.md) | 4 | 4 | 20 | 0 | 100% |
| [Business Intelligence](./bi.en-US.md) | 3 | 5 | 47 | 18 | 62% |
| [Reports](./relatorios.en-US.md) | 1 | 1 | 2 | 0 | 100% |
| [Artificial Intelligence](./ia.en-US.md) | 1 | 9 | 89 | 57 | 36% |
| [Core and Administration](./core.en-US.md) | 8 | 13 | 46 | 0 | 100% |
| **Total** | **72** | **84** | **437** | **82** | **81%** |

> `No screen` means a backend endpoint that is not called by any screen. The operator cannot access that functionality through the application.
