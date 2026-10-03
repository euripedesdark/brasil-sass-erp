# Serviços

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 3 |
| Controllers | 1 |
| Endpoints | 10 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Serviços

| Submenu | Rota | Componente |
|---|---|---|
| Catálogo de Serviços | `/servicos` | `Servicos` |
| Cadastro de Serviços | `/cadastro/servicos` | `ServicoCadastro` |
| Ordens de Serviço | `/ordens-servico` | `OrdemServico` |

## 2. Função por função: tela e endpoint

### Catálogo de Serviços

- **Rota:** `/servicos`
- **Componente:** `Servicos`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/servicos`
  - `/api/cadastro/servicos/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Cadastro de Serviços

- **Rota:** `/cadastro/servicos`
- **Componente:** `ServicoCadastro`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/servicos`
  - `/api/cadastro/servicos/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Ordens de Serviço

- **Rota:** `/ordens-servico`
- **Componente:** `OrdemServico`
- **Endpoints usados:** 8
  - `/api`
  - `/api/auth/refresh`
  - `/api/cadastro/clientes`
  - `/api/servicos/os`
  - `/api/servicos/os/*/apontamentos`
  - `/api/servicos/os/*/fechar`
  - `/api/servicos/os/*/itens`
  - `/api/servicos/os/*/pdf`

## 3. Backend do módulo

### OrdemServicoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/servicos/os/{id}` | sim |
| GET | `/api/servicos/os` | sim |
| GET | `/api/servicos/os/{id}` | sim |
| GET | `/api/servicos/os/{id}/itens` | sim |
| GET | `/api/servicos/os/{id}/pdf` | sim |
| POST | `/api/servicos/os` | sim |
| POST | `/api/servicos/os/{id}/apontamentos` | sim |
| POST | `/api/servicos/os/{id}/fechar` | sim |
| POST | `/api/servicos/os/{id}/itens` | sim |
| PUT | `/api/servicos/os/{id}` | sim |

