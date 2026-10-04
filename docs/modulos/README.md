> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Mapa de módulos — menu, função e cobertura

Uma visão por módulo do que existe no menu, o que está em código no backend e o
que tem tela. Serve para comparar a abrangência: o número de **endpoints sem tela**
é a medida direta do buraco.

| Módulo | Itens de menu | Controllers | Endpoints | Sem tela | Cobertura |
|---|---:|---:|---:|---:|---:|
| [Cadastros](./cadastro.md) | 10 | 11 | 54 | 0 | 100% |
| [Vendas](./vendas.md) | 2 | 2 | 11 | 0 | 100% |
| [Compras](./compras.md) | 4 | 4 | 16 | 0 | 100% |
| [Estoque](./estoque.md) | 9 | 9 | 33 | 0 | 100% |
| [Financeiro](./financeiro.md) | 16 | 12 | 69 | 0 | 100% |
| [Fiscal](./fiscal.md) | 7 | 9 | 24 | 7 | 71% |
| [Produção](./producao.md) | 4 | 4 | 16 | 0 | 100% |
| [Serviços](./servicos.md) | 3 | 1 | 10 | 0 | 100% |
| [Recursos Humanos](./rh.md) | 4 | 4 | 20 | 0 | 100% |
| [Business Intelligence](./bi.md) | 3 | 5 | 47 | 18 | 62% |
| [Relatórios](./relatorios.md) | 1 | 1 | 2 | 0 | 100% |
| [Inteligência Artificial](./ia.md) | 1 | 9 | 89 | 57 | 36% |
| [Core e Administração](./core.md) | 8 | 13 | 46 | 0 | 100% |
| **Total** | **72** | **84** | **437** | **82** | **81%** |

> `Sem tela` = endpoint que existe no backend e nenhuma tela chama. É a lista
> do que o operador não consegue fazer pelo sistema.

