> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Vendas

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 2 |
| Controllers | 2 |
| Endpoints | 11 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Vendas

| Submenu | Rota | Componente |
|---|---|---|
| Pedidos de Venda | `/vendas` | **sem rota** |
| Tabelas de Preço | `/vendas/tabelas-preco` | `TabelasPreco` |

## 2. Função por função: tela e endpoint

### Pedidos de Venda

- **Rota:** `/vendas`
- **Componente:** **sem rota registrada**

### Tabelas de Preço

- **Rota:** `/vendas/tabelas-preco`
- **Componente:** `TabelasPreco`
- **Endpoints usados:** 5
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/vendas/tabelas-preco`
  - `/api/vendas/tabelas-preco/*`

## 3. Backend do módulo

### PedidoVendaController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/vendas/pedidos` | sim |
| GET | `/api/vendas/pedidos/{id}` | sim |
| POST | `/api/vendas/pedidos` | sim |
| POST | `/api/vendas/pedidos/{id}/cancelar` | sim |
| POST | `/api/vendas/pedidos/{id}/confirmar` | sim |
| POST | `/api/vendas/pedidos/{id}/faturar` | sim |

### TabelaPrecoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/vendas/tabelas-preco/{id}` | sim |
| GET | `/api/vendas/tabelas-preco` | sim |
| GET | `/api/vendas/tabelas-preco/{id}` | sim |
| POST | `/api/vendas/tabelas-preco` | sim |
| PUT | `/api/vendas/tabelas-preco/{id}` | sim |

