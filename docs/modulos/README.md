> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Mapa de módulos — menu, função e cobertura

Uma visão por módulo do que existe no menu, o que está em código no backend e o
que tem tela. Serve para comparar a abrangência: o número de **endpoints sem tela**
é a medida direta do buraco.

| Módulo | Itens de menu | Controllers | Endpoints | Sem tela | Cobertura |
|---|---:|---:|---:|---:|---:|
| [Cadastros](docs/modulos/cadastro.md) | 10 | 11 | 54 | 0 | 100% |
| [Vendas](docs/modulos/vendas.md) | 2 | 2 | 11 | 0 | 100% |
| [Compras](docs/modulos/compras.md) | 4 | 4 | 16 | 0 | 100% |
| [Estoque](docs/modulos/estoque.md) | 9 | 9 | 33 | 0 | 100% |
| [Financeiro](docs/modulos/financeiro.md) | 16 | 12 | 69 | 0 | 100% |
| [Fiscal](docs/modulos/fiscal.md) | 7 | 9 | 24 | 7 | 71% |
| [Produção](docs/modulos/producao.md) | 4 | 4 | 16 | 0 | 100% |
| [Serviços](docs/modulos/servicos.md) | 3 | 1 | 10 | 0 | 100% |
| [Recursos Humanos](docs/modulos/rh.md) | 4 | 4 | 20 | 0 | 100% |
| [Business Intelligence](docs/modulos/bi.md) | 3 | 5 | 47 | 18 | 62% |
| [Relatórios](docs/modulos/relatorios.md) | 1 | 1 | 2 | 0 | 100% |
| [Inteligência Artificial](docs/modulos/ia.md) | 1 | 9 | 89 | 57 | 36% |
| [Core e Administração](docs/modulos/core.md) | 8 | 13 | 46 | 0 | 100% |
| **Total** | **72** | **84** | **437** | **82** | **81%** |

> `Sem tela` = endpoint que existe no backend e nenhuma tela chama. É a lista
> do que o operador não consegue fazer pelo sistema.

