# Compras

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 4 |
| Controllers | 4 |
| Endpoints | 16 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Compras

| Submenu | Rota | Componente |
|---|---|---|
| Pedidos de Compra | `/compras` | **sem rota** |
| Suprimentos: Solicitações e Cotações | `/compras/supply-chain` | `SupplyChainCompras` |
| Recebimentos | `/compras/recebimentos` | `RecebimentosCompra` |
| Conferência 3-way | `/compras/conferencia-faturas` | `ConferenciaFaturasCompra` |

## 2. Função por função: tela e endpoint

### Pedidos de Compra

- **Rota:** `/compras`
- **Componente:** **sem rota registrada**

### Suprimentos: Solicitações e Cotações

- **Rota:** `/compras/supply-chain`
- **Componente:** `SupplyChainCompras`
- **Endpoints usados:** 14
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/compras/supply-chain`
  - `/api/compras/supply-chain/cotacoes`
  - `/api/compras/supply-chain/cotacoes/*/mapa`
  - `/api/compras/supply-chain/cotacoes/fornecedor/*/gerar-pedido`
  - `/api/compras/supply-chain/solicitacoes`
  - `/api/compras/supply-chain/solicitacoes/*/aprovar`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Recebimentos

- **Rota:** `/compras/recebimentos`
- **Componente:** `RecebimentosCompra`
- **Endpoints usados:** 4
  - `/api/compras`
  - `/api/compras/conferencia-faturas`
  - `/api/compras/recebimentos`
  - `/api/compras/recebimentos/*/itens`

### Conferência 3-way

- **Rota:** `/compras/conferencia-faturas`
- **Componente:** `ConferenciaFaturasCompra`
- **Endpoints usados:** 4
  - `/api/compras`
  - `/api/compras/conferencia-faturas`
  - `/api/compras/recebimentos`
  - `/api/compras/recebimentos/*/itens`

## 3. Backend do módulo

### ComprasSupplyChainController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/compras/supply-chain/cotacoes/{id}/mapa` | sim |
| GET | `/api/compras/supply-chain/solicitacoes` | sim |
| POST | `/api/compras/supply-chain/cotacoes` | sim |
| POST | `/api/compras/supply-chain/cotacoes/fornecedor/{cotacaoFornecedorId}/gerar-pedido` | sim |
| POST | `/api/compras/supply-chain/solicitacoes` | sim |
| POST | `/api/compras/supply-chain/solicitacoes/{id}/aprovar` | sim |

### ConferenciaFaturaCompraController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/compras/conferencia-faturas` | sim |
| POST | `/api/compras/conferencia-faturas` | sim |

### PedidoCompraController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/compras/pedidos` | sim |
| GET | `/api/compras/pedidos/{id}` | sim |
| POST | `/api/compras/pedidos` | sim |
| POST | `/api/compras/pedidos/{id}/cancelar` | sim |
| POST | `/api/compras/pedidos/{id}/receber` | sim |
| POST | `/api/compras/pedidos/{id}/receber-parcial` | sim |

### RecebimentoCompraController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/compras/recebimentos` | sim |
| GET | `/api/compras/recebimentos/{id}/itens` | sim |

