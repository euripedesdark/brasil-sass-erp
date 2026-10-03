# Core e Administração

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 8 |
| Controllers | 13 |
| Endpoints | 46 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Dashboard | `/dashboard` | `Dashboard` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Usuários e Permissões | `/admin/usuarios` | `Usuarios` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Imagens do Sistema | `/admin/configuracoes` | `Configuracoes` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Documentos | `/documentos` | `Documentos` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Meu Perfil | `/perfil` | `Perfil` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Auditoria de Paridade ERP | `/admin/paridade-erp` | `RelatorioParidadeERP` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Gerenciador SQL | `/admin/sql` | `SqlConsole` |

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Armazenamento de imagens | `/admin/armazenamento` | `ArmazenamentoImagens` |

## 2. Função por função: tela e endpoint

### Dashboard

- **Rota:** `/dashboard`
- **Componente:** `Dashboard`
- **Endpoints usados:** 11
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/bi/indicadores/atualizar/valores`
  - `/api/bi/indicadores/dashboard`
  - `/api/core/recent/updates`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Usuários e Permissões

- **Rota:** `/admin/usuarios`
- **Componente:** `Usuarios`
- **Endpoints usados:** 6
  - `/api`
  - `/api/auth/refresh`
  - `/api/core/perfis`
  - `/api/superadmin/usuarios`
  - `/api/superadmin/usuarios/*/modulos`
  - `/api/superadmin/usuarios/permissao`

### Imagens do Sistema

- **Rota:** `/admin/configuracoes`
- **Componente:** `Configuracoes`
- **Endpoints usados:** 5
  - `/api`
  - `/api/auth/refresh`
  - `/api/superadmin/assets/background`
  - `/api/superadmin/assets/login`
  - `/api/superadmin/assets/system/*`

### Documentos

- **Rota:** `/documentos`
- **Componente:** `Documentos`
- **Endpoints usados:** 4
  - `/api`
  - `/api/auth/refresh`
  - `/api/documentos`
  - `/api/documentos/*/conteudo`

### Meu Perfil

- **Rota:** `/perfil`
- **Componente:** `Perfil`
- **Endpoints usados:** 12
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/core/perfil`
  - `/api/core/perfil/senha`
  - `/api/core/usuarios/*/foto`
  - `/api/core/usuarios/foto/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Auditoria de Paridade ERP

- **Rota:** `/admin/paridade-erp`
- **Componente:** `RelatorioParidadeERP`
- **Endpoints usados:** nenhum identificado (tela pode estar mock ou só ler por outro caminho)

### Gerenciador SQL

- **Rota:** `/admin/sql`
- **Componente:** `SqlConsole`
- **Endpoints usados:** 14
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/superadmin/sql/campo`
  - `/api/superadmin/sql/catalogo`
  - `/api/superadmin/sql/executar`
  - `/api/superadmin/sql/script`
  - `/api/superadmin/sql/tabelas/*`
  - `/api/superadmin/sql/tabelas/*/dados`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Armazenamento de imagens

- **Rota:** `/admin/armazenamento`
- **Componente:** `ArmazenamentoImagens`
- **Endpoints usados:** 7
  - `/api`
  - `/api/auth/refresh`
  - `/api/superadmin/armazenamento`
  - `/api/superadmin/armazenamento/importar-imagens`
  - `/api/superadmin/armazenamento/purgar-imagens`
  - `/api/superadmin/armazenamento/recurso/*`
  - `/api/superadmin/armazenamento/staging-pendente`

## 3. Backend do módulo

### ArmazenamentoAdminController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/superadmin/armazenamento/staging-pendente` | sim |
| POST | `/api/superadmin/armazenamento/importar-imagens` | sim |
| POST | `/api/superadmin/armazenamento/purgar-imagens` | sim |
| POST | `/api/superadmin/armazenamento/recurso/{tipo}` | sim |

### AuthController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/auth/me` | sim |
| POST | `/api/auth/login` | sim |
| POST | `/api/auth/refresh` | sim |

### BancoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/core/bancos` | sim |
| GET | `/api/core/bancos/{compe}` | sim |

### DocumentoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/documentos` | sim |
| GET | `/api/documentos` | sim |
| GET | `/api/documentos/{id}/conteudo` | sim |
| POST | `/api/documentos` | sim |

### EmpresaLogoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/core/empresas/{id}/logo` | sim |
| GET | `/api/core/empresas/{id}/logo` | sim |
| POST | `/api/core/empresas/{id}/logo` | sim |

### GerenciadorSqlController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/superadmin/sql/catalogo` | sim |
| GET | `/api/superadmin/sql/tabelas/{tabela}` | sim |
| GET | `/api/superadmin/sql/tabelas/{tabela}/dados` | sim |
| POST | `/api/superadmin/sql/campo` | sim |
| POST | `/api/superadmin/sql/executar` | sim |
| POST | `/api/superadmin/sql/script` | sim |

### MinhaEmpresaController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/core/minha-empresa` | sim |
| POST | `/api/core/minha-empresa` | sim |
| PUT | `/api/core/minha-empresa` | sim |

### MunicipioController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/municipios/{id}` | sim |
| GET | `/api/municipios` | sim |
| GET | `/api/municipios/buscar` | sim |
| GET | `/api/municipios/codigo/{codigoIbge}` | sim |
| GET | `/api/municipios/{id}` | sim |
| POST | `/api/municipios` | sim |
| PUT | `/api/municipios/{id}` | sim |

### PerfilController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/core/perfil` | sim |
| PUT | `/api/core/perfil` | sim |
| PUT | `/api/core/perfil/senha` | sim |

### RecentController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/core/recent/updates` | sim |

### SuperAdminController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/superadmin/assets/system/{tipo}` | sim |
| POST | `/api/superadmin/assets/background` | sim |
| POST | `/api/superadmin/assets/login` | sim |

### UsuarioAdminController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/superadmin/usuarios` | sim |
| GET | `/api/superadmin/usuarios/{id}/modulos` | sim |
| POST | `/api/superadmin/usuarios/permissao` | sim |
| POST | `/api/superadmin/usuarios/{id}/modulos` | sim |

### UsuarioFotoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/core/usuarios/{id}/foto` | sim |
| GET | `/api/core/usuarios/{id}/foto` | sim |
| POST | `/api/core/usuarios/{id}/foto` | sim |

