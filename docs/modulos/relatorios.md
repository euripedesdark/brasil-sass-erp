> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Relatórios

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 1 |
| Controllers | 1 |
| Endpoints | 2 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Relatórios | `/relatorios` | `Relatorios` |

## 2. Função por função: tela e endpoint

### Relatórios

- **Rota:** `/relatorios`
- **Componente:** `Relatorios`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/relatorios/*`
  - `/api/relatorios/pdf/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

## 3. Backend do módulo

### RelatorioController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/relatorios/pdf/{tipo}` | sim |
| GET | `/api/relatorios/{tipo}` | sim |

