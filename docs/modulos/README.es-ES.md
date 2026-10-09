> Actualizado el 04/10/2026 — inventario verificado en el código, la base de datos y la prueba smoke.

# Mapa de módulos — menú, función y cobertura

Esta vista compara las opciones del menú, los controladores del backend, los endpoints y las pantallas disponibles. El número de **endpoints sin pantalla** indica directamente la brecha funcional.

| Módulo | Elementos de menú | Controladores | Endpoints | Sin pantalla | Cobertura |
|---|---:|---:|---:|---:|---:|
| [Registros](./cadastro.md) | 10 | 11 | 54 | 0 | 100% |
| [Ventas](./vendas.md) | 2 | 2 | 11 | 0 | 100% |
| [Compras](./compras.md) | 4 | 4 | 16 | 0 | 100% |
| [Inventario](./estoque.md) | 9 | 9 | 33 | 0 | 100% |
| [Finanzas](./financeiro.md) | 16 | 12 | 69 | 0 | 100% |
| [Fiscal](./fiscal.md) | 7 | 9 | 24 | 7 | 71% |
| [Producción](./producao.md) | 4 | 4 | 16 | 0 | 100% |
| [Servicios](./servicos.md) | 3 | 1 | 10 | 0 | 100% |
| [Activos (FI-AA + PM)](./ativos.md) | 3 | 2 | 46 | 0 | 100% |
| [Recursos humanos](./rh.md) | 4 | 4 | 20 | 0 | 100% |
| [Inteligencia empresarial](./bi.md) | 3 | 5 | 47 | 18 | 62% |
| [Informes](./relatorios.md) | 1 | 1 | 2 | 0 | 100% |
| [Inteligencia artificial](./ia.md) | 1 | 9 | 89 | 57 | 36% |
| [Núcleo y administración](./core.md) | 8 | 13 | 46 | 0 | 100% |
| **Total** | **72** | **84** | **437** | **82** | **81%** |

> `Sin pantalla` significa que existe un endpoint en el backend, pero ninguna pantalla lo utiliza. El operador no puede acceder a esa función desde la aplicación.
