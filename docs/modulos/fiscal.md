# Fiscal

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 7 |
| Controllers | 9 |
| Endpoints | 24 |
| Endpoints sem tela | 7 |

---

## 1. Menu

### Fiscal

| Submenu | Rota | Componente |
|---|---|---|
| NCM | `/fiscal/ncm` | `Ncm` |
| CFOP | `/fiscal/cfop` | `Cfop` |
| CEST | `/fiscal/cest` | `Cest` |
| ISSQN | `/fiscal/issqn` | `Issqn` |
| Entradas de NF | `/fiscal/entradas` | `EntradaNota` |
| Impostos | `/fiscal/impostos` | `Impostos` |
| Consulta SEFAZ | `/fiscal/sefaz` | `SefazConsulta` |

## 2. Função por função: tela e endpoint

### NCM

- **Rota:** `/fiscal/ncm`
- **Componente:** `Ncm`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/fiscal/ncm`
  - `/api/fiscal/ncm/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### CFOP

- **Rota:** `/fiscal/cfop`
- **Componente:** `Cfop`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/fiscal/cfop`
  - `/api/fiscal/cfop/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### CEST

- **Rota:** `/fiscal/cest`
- **Componente:** `Cest`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/fiscal/cest`
  - `/api/fiscal/cest/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### ISSQN

- **Rota:** `/fiscal/issqn`
- **Componente:** `Issqn`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/fiscal/issqn/municipio/*`
  - `/api/fiscal/issqn/uf/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Entradas de NF

- **Rota:** `/fiscal/entradas`
- **Componente:** `EntradaNota`
- **Endpoints usados:** 5
  - `/api`
  - `/api/auth/refresh`
  - `/api/fiscal/entradas`
  - `/api/fiscal/entradas/*/manifestacao`
  - `/api/fiscal/entradas/chave/*`

### Impostos

- **Rota:** `/fiscal/impostos`
- **Componente:** `Impostos`
- **Endpoints usados:** 9
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/fiscal/impostos*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Consulta SEFAZ

- **Rota:** `/fiscal/sefaz`
- **Componente:** `SefazConsulta`
- **Endpoints usados:** 11
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/fiscal/sefaz/consultar`
  - `/api/fiscal/sefaz/distribuicao`
  - `/api/fiscal/sefaz/status`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

## 3. Backend do módulo

### CertificadoDigitalController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/certificados` | sim |
| POST | `/api/fiscal/certificados/carregar` | sim |

### CestController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/cest` | sim |
| GET | `/api/fiscal/cest/por-ncm/{ncm}` | — |
| GET | `/api/fiscal/cest/{codigo}` | sim |

### CfopController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/cfop` | sim |
| GET | `/api/fiscal/cfop/{codigo}` | sim |

### EntradaNotaController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/entradas` | sim |
| GET | `/api/fiscal/entradas/chave/{chave}` | sim |
| POST | `/api/fiscal/entradas` | sim |
| POST | `/api/fiscal/entradas/{id}/manifestacao` | sim |

### ImpostoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/impostos` | — |
| GET | `/api/fiscal/impostos/{codigo}` | — |

### IssqnController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/issqn/municipio/{codigoIbge}` | sim |
| GET | `/api/fiscal/issqn/uf/{uf}` | sim |

### NcmController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/ncm` | sim |
| GET | `/api/fiscal/ncm/{codigo}` | sim |

### NfseController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/nfse/{id}/pdf` | — |
| GET | `/api/fiscal/nfse/{id}/xml` | — |
| POST | `/api/fiscal/nfse/emitir` | — |
| POST | `/api/fiscal/nfse/{id}/cancelar` | — |

### SefazConsultaController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/fiscal/sefaz/consultar` | sim |
| GET | `/api/fiscal/sefaz/distribuicao` | sim |
| GET | `/api/fiscal/sefaz/status` | sim |

