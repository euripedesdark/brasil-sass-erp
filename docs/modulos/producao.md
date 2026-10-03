# Produção

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

### Produção

| Submenu | Rota | Componente |
|---|---|---|
| Ordens de Produção | `/producao` | `Producao` |
| Romaneios de Produção | `/producao/romaneios` | `RomaneioProducao` |
| Estrutura / BOM | `/producao/estrutura` | `EstruturaProduto` |
| Apontamentos de Produção | `/producao/apontamentos` | `ApontamentosProducao` |

## 2. Função por função: tela e endpoint

### Ordens de Produção

- **Rota:** `/producao`
- **Componente:** `Producao`
- **Endpoints usados:** 16
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/producao`
  - `/api/producao/*/finalizar`
  - `/api/producao/estruturas`
  - `/api/producao/estruturas/*`
  - `/api/producao/romaneios`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Romaneios de Produção

- **Rota:** `/producao/romaneios`
- **Componente:** `RomaneioProducao`
- **Endpoints usados:** 3
  - `/api`
  - `/api/auth/refresh`
  - `/api/producao/romaneios`

### Estrutura / BOM

- **Rota:** `/producao/estrutura`
- **Componente:** `EstruturaProduto`
- **Endpoints usados:** 13
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/estoque-baixo`
  - `/api/producao/estruturas`
  - `/api/producao/estruturas/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Apontamentos de Produção

- **Rota:** `/producao/apontamentos`
- **Componente:** `ApontamentosProducao`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/refresh`
  - `/api/producao`
  - `/api/producao/apontamentos`
  - `/api/producao/apontamentos/*/finalizar`
  - `/api/producao/apontamentos/estatisticas/*`
  - `/api/producao/apontamentos/por-funcionario/*`
  - `/api/producao/apontamentos/por-periodo`
  - `/api/producao/apontamentos/por-producao/*`
  - `/api/producao/apontamentos/por-status`

## 3. Backend do módulo

### ApontamentoProducaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/producao/apontamentos/{id}` | sim |
| GET | `/api/producao/apontamentos/estatisticas/{producaoId}` | sim |
| GET | `/api/producao/apontamentos/por-funcionario/{funcionarioId}` | sim |
| GET | `/api/producao/apontamentos/por-periodo` | sim |
| GET | `/api/producao/apontamentos/por-producao/{producaoId}` | sim |
| GET | `/api/producao/apontamentos/por-status` | sim |
| POST | `/api/producao/apontamentos` | sim |
| PUT | `/api/producao/apontamentos/{id}/finalizar` | sim |

### EstruturaProdutoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/producao/estruturas/{id}` | sim |
| GET | `/api/producao/estruturas/{produtoPaiId}` | sim |
| POST | `/api/producao/estruturas` | sim |

### ProducaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/producao` | sim |
| POST | `/api/producao` | sim |
| POST | `/api/producao/{id}/finalizar` | sim |

### RomaneioProducaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/producao/romaneios` | sim |
| POST | `/api/producao/romaneios` | sim |

