# Business Intelligence

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 3 |
| Controllers | 5 |
| Endpoints | 47 |
| Endpoints sem tela | 18 |

---

## 1. Menu

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Business Intelligence | `/bi` | `BI` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| KPIs | `/bi/kpis` | `Kpis` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Relatórios Agendados | `/bi/relatorios-agendados` | `RelatoriosAgendados` |

## 2. Função por função: tela e endpoint

### Business Intelligence

- **Rota:** `/bi`
- **Componente:** `BI`
- **Endpoints usados:** 11
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/bi/dashboards`
  - `/api/bi/dashboards/*`
  - `/api/bi/indicadores`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### KPIs

- **Rota:** `/bi/kpis`
- **Componente:** `Kpis`
- **Endpoints usados:** 13
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/bi/kpis`
  - `/api/bi/kpis/*`
  - `/api/bi/kpis/*/calculate`
  - `/api/bi/kpis/refresh`
  - `/api/bi/kpis/type/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Relatórios Agendados

- **Rota:** `/bi/relatorios-agendados`
- **Componente:** `RelatoriosAgendados`
- **Endpoints usados:** 15
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/bi/relatorios`
  - `/api/bi/relatorios-agendados`
  - `/api/bi/relatorios-agendados/*`
  - `/api/bi/relatorios-agendados/*/agendar`
  - `/api/bi/relatorios-agendados/*/executar`
  - `/api/bi/relatorios-agendados/executar-todos`
  - `/api/bi/relatorios-agendados/pendentes`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

## 3. Backend do módulo

### DashboardController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/bi/dashboards/{id}` | sim |
| GET | `/api/bi/dashboards` | sim |
| GET | `/api/bi/dashboards/publicos` | — |
| GET | `/api/bi/dashboards/tipo/{tipo}` | — |
| GET | `/api/bi/dashboards/usuario/{usuarioId}` | — |
| GET | `/api/bi/dashboards/{id}` | sim |
| POST | `/api/bi/dashboards` | sim |
| POST | `/api/bi/dashboards/{id}/duplicar` | — |
| PUT | `/api/bi/dashboards/{id}` | sim |

### IndicadorController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/bi/indicadores/{id}` | sim |
| GET | `/api/bi/indicadores` | sim |
| GET | `/api/bi/indicadores/categoria/{categoria}` | — |
| GET | `/api/bi/indicadores/dashboard` | sim |
| GET | `/api/bi/indicadores/{id}` | sim |
| POST | `/api/bi/indicadores` | sim |
| POST | `/api/bi/indicadores/atualizar/valores` | sim |
| POST | `/api/bi/indicadores/calcular/categoria/{categoria}` | — |
| POST | `/api/bi/indicadores/calcular/todos` | — |
| POST | `/api/bi/indicadores/{id}/atualizar` | — |
| PUT | `/api/bi/indicadores/{id}` | sim |

### KpiController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/bi/kpis/{id}` | sim |
| GET | `/api/bi/kpis` | sim |
| GET | `/api/bi/kpis/type/{kpiType}` | sim |
| GET | `/api/bi/kpis/{id}` | sim |
| GET | `/api/bi/kpis/{id}/calculate` | sim |
| POST | `/api/bi/kpis` | sim |
| POST | `/api/bi/kpis/refresh` | sim |
| PUT | `/api/bi/kpis/{id}` | sim |

### RelatorioAgendadoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/bi/relatorios-agendados/{id}` | sim |
| GET | `/api/bi/relatorios-agendados` | sim |
| GET | `/api/bi/relatorios-agendados/frequencia/{frequencia}` | — |
| GET | `/api/bi/relatorios-agendados/pendentes` | sim |
| GET | `/api/bi/relatorios-agendados/{id}` | sim |
| POST | `/api/bi/relatorios-agendados` | sim |
| POST | `/api/bi/relatorios-agendados/executar-todos` | sim |
| POST | `/api/bi/relatorios-agendados/{id}/agendar` | sim |
| POST | `/api/bi/relatorios-agendados/{id}/executar` | sim |
| PUT | `/api/bi/relatorios-agendados/{id}` | sim |

### ReportController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/bi/reports/{id}` | — |
| GET | `/api/bi/reports` | — |
| GET | `/api/bi/reports/category/{category}` | — |
| GET | `/api/bi/reports/{id}` | — |
| POST | `/api/bi/reports` | — |
| POST | `/api/bi/reports/{id}/generate` | — |
| POST | `/api/bi/reports/{id}/schedule` | — |
| POST | `/api/bi/reports/{id}/unschedule` | — |
| PUT | `/api/bi/reports/{id}` | — |

