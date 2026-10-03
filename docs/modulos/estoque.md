# Estoque

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 9 |
| Controllers | 9 |
| Endpoints | 33 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Estoque

| Submenu | Rota | Componente |
|---|---|---|
| Produtos e Saldos | `/estoque` | **sem rota** |
| Movimentações | `/estoque/movimentacoes` | `MovimentacoesEstoque` |
| Depósitos | `/estoque/depositos` | `Depositos` |
| Transferências | `/estoque/transferencias` | `TransferenciasEstoque` |
| Inventários | `/estoque/inventarios` | `InventariosEstoque` |
| Endereçamento WMS | `/estoque/enderecos` | `EnderecosEstoque` |
| Lotes e Validade | `/estoque/lotes` | `LotesEstoque` |
| Reservas | `/estoque/reservas` | `ReservasEstoque` |
| Separação e Expedição | `/estoque/expedicoes` | `ExpedicoesEstoque` |

## 2. Função por função: tela e endpoint

### Produtos e Saldos

- **Rota:** `/estoque`
- **Componente:** **sem rota registrada**

### Movimentações

- **Rota:** `/estoque/movimentacoes`
- **Componente:** `MovimentacoesEstoque`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/estoque/movimentacoes`
  - `/api/estoque/saldos`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Depósitos

- **Rota:** `/estoque/depositos`
- **Componente:** `Depositos`
- **Endpoints usados:** 2
  - `/api/estoque/depositos`
  - `/api/estoque/depositos/*`

### Transferências

- **Rota:** `/estoque/transferencias`
- **Componente:** `TransferenciasEstoque`
- **Endpoints usados:** 8
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/estoque/depositos`
  - `/api/estoque/enderecos`
  - `/api/estoque/lotes`
  - `/api/estoque/transferencias`
  - `/api/estoque/transferencias/interna`

### Inventários

- **Rota:** `/estoque/inventarios`
- **Componente:** `InventariosEstoque`
- **Endpoints usados:** 11
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/estoque/depositos`
  - `/api/estoque/enderecos`
  - `/api/estoque/inventarios`
  - `/api/estoque/inventarios/*`
  - `/api/estoque/inventarios/*/contagens`
  - `/api/estoque/inventarios/*/fechar`
  - `/api/estoque/inventarios/*/itens`
  - `/api/estoque/lotes`

### Endereçamento WMS

- **Rota:** `/estoque/enderecos`
- **Componente:** `EnderecosEstoque`
- **Endpoints usados:** 4
  - `/api/estoque/depositos`
  - `/api/estoque/enderecos`
  - `/api/estoque/enderecos/*`
  - `/api/estoque/enderecos/ocupacao`

### Lotes e Validade

- **Rota:** `/estoque/lotes`
- **Componente:** `LotesEstoque`
- **Endpoints usados:** 6
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/estoque/depositos`
  - `/api/estoque/lotes`
  - `/api/estoque/lotes/*`

### Reservas

- **Rota:** `/estoque/reservas`
- **Componente:** `ReservasEstoque`
- **Endpoints usados:** 12
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/estoque/depositos`
  - `/api/estoque/enderecos`
  - `/api/estoque/lotes`
  - `/api/estoque/reservas`
  - `/api/estoque/reservas/*`
  - `/api/estoque/reservas/*/*`
  - `/api/estoque/reservas/*/liberar`
  - `/api/estoque/reservas/*/separar`
  - `/api/estoque/reservas/picking-sugestoes`

### Separação e Expedição

- **Rota:** `/estoque/expedicoes`
- **Componente:** `ExpedicoesEstoque`
- **Endpoints usados:** 8
  - `/api/estoque/depositos`
  - `/api/estoque/expedicoes`
  - `/api/estoque/expedicoes/*`
  - `/api/estoque/expedicoes/*/*`
  - `/api/estoque/expedicoes/*/embalar`
  - `/api/estoque/expedicoes/*/expedir`
  - `/api/estoque/expedicoes/*/itens`
  - `/api/estoque/expedicoes/*/separar`

## 3. Backend do módulo

### DepositoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/estoque/depositos/{id}` | sim |
| GET | `/api/estoque/depositos` | sim |
| POST | `/api/estoque/depositos` | sim |
| PUT | `/api/estoque/depositos/{id}` | sim |

### EnderecoEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/estoque/enderecos/{id}` | sim |
| GET | `/api/estoque/enderecos` | sim |
| GET | `/api/estoque/enderecos/ocupacao` | sim |
| POST | `/api/estoque/enderecos` | sim |
| PUT | `/api/estoque/enderecos/{id}` | sim |

### ExpedicaoEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/estoque/expedicoes` | sim |
| POST | `/api/estoque/expedicoes` | sim |
| POST | `/api/estoque/expedicoes/{id}/embalar` | sim |
| POST | `/api/estoque/expedicoes/{id}/expedir` | sim |
| POST | `/api/estoque/expedicoes/{id}/separar` | sim |

### InventarioEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/estoque/inventarios` | sim |
| GET | `/api/estoque/inventarios/{id}/itens` | sim |
| POST | `/api/estoque/inventarios` | sim |
| POST | `/api/estoque/inventarios/{id}/contagens` | sim |
| POST | `/api/estoque/inventarios/{id}/fechar` | sim |

### LoteEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/estoque/lotes/{id}` | sim |
| GET | `/api/estoque/lotes` | sim |
| POST | `/api/estoque/lotes` | sim |
| PUT | `/api/estoque/lotes/{id}` | sim |

### MovimentacaoEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/estoque/movimentacoes` | sim |

### ReservaEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/estoque/reservas` | sim |
| GET | `/api/estoque/reservas/picking-sugestoes` | sim |
| POST | `/api/estoque/reservas` | sim |
| POST | `/api/estoque/reservas/{id}/liberar` | sim |
| POST | `/api/estoque/reservas/{id}/separar` | sim |

### SaldoEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/estoque/saldos` | sim |

### TransferenciaEstoqueController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/estoque/transferencias` | sim |
| POST | `/api/estoque/transferencias` | sim |
| POST | `/api/estoque/transferencias/interna` | sim |

