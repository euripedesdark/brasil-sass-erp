> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Recursos Humanos

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 4 |
| Controllers | 4 |
| Endpoints | 20 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Recursos Humanos

| Submenu | Rota | Componente |
|---|---|---|
| Colaboradores | `/rh` | `RH` |
| Cargos | `/rh/cargos` | `Cargo` |
| Folha de Pagamento | `/rh/folha` | `FolhaPagamento` |
| Fotos dos Colaboradores | `/rh/fotos` | `FuncionarioFoto` |

## 2. Função por função: tela e endpoint

### Colaboradores

- **Rota:** `/rh`
- **Componente:** `RH`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/rh/funcionarios`
  - `/api/rh/funcionarios/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Cargos

- **Rota:** `/rh/cargos`
- **Componente:** `Cargo`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/rh/cargos`
  - `/api/rh/cargos/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Folha de Pagamento

- **Rota:** `/rh/folha`
- **Componente:** `FolhaPagamento`
- **Endpoints usados:** 12
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/rh/folhas`
  - `/api/rh/folhas/*`
  - `/api/rh/folhas/*/cancelar`
  - `/api/rh/folhas/*/processar`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Fotos dos Colaboradores

- **Rota:** `/rh/fotos`
- **Componente:** `FuncionarioFoto`
- **Endpoints usados:** 5
  - `/api`
  - `/api/auth/refresh`
  - `/api/rh/funcionarios`
  - `/api/rh/funcionarios/*`
  - `/api/rh/funcionarios/*/foto`

## 3. Backend do módulo

### CargoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/rh/cargos/{id}` | sim |
| GET | `/api/rh/cargos` | sim |
| GET | `/api/rh/cargos/{id}` | sim |
| POST | `/api/rh/cargos` | sim |
| PUT | `/api/rh/cargos/{id}` | sim |

### FolhaPagamentoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/rh/folhas/{id}` | sim |
| GET | `/api/rh/folhas` | sim |
| GET | `/api/rh/folhas/{id}` | sim |
| POST | `/api/rh/folhas` | sim |
| POST | `/api/rh/folhas/{id}/cancelar` | sim |
| POST | `/api/rh/folhas/{id}/processar` | sim |
| PUT | `/api/rh/folhas/{id}` | sim |

### FuncionarioController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/rh/funcionarios/{id}` | sim |
| GET | `/api/rh/funcionarios` | sim |
| GET | `/api/rh/funcionarios/{id}` | sim |
| POST | `/api/rh/funcionarios` | sim |
| PUT | `/api/rh/funcionarios/{id}` | sim |

### FuncionarioFotoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/rh/funcionarios/{id}/foto` | sim |
| GET | `/api/rh/funcionarios/{id}/foto` | sim |
| POST | `/api/rh/funcionarios/{id}/foto` | sim |

