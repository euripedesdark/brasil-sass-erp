# Cadastros

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 10 |
| Controllers | 11 |
| Endpoints | 54 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Cadastros

| Submenu | Rota | Componente |
|---|---|---|
| Pessoas | `/cadastro/pessoas` | `CadastroPessoas` |
| Clientes | `/cadastro/clientes` | `Cliente` |
| Fornecedores | `/cadastro/fornecedores` | `Fornecedor` |
| Produtos | `/cadastro/produtos` | `CadastroProdutos` |
| Categorias | `/cadastro/categorias` | `Categoria` |
| Marcas | `/cadastro/marcas` | `Marca` |
| Serviços | `/cadastro/servicos` | `ServicoCadastro` |
| Transportadoras | `/cadastro/transportadoras` | `Transportadora` |
| Unidades de Medida | `/cadastro/unidades-medida` | `UnidadeMedida` |
| Municípios IBGE | `/cadastro/municipios` | `Municipios` |

## 2. Função por função: tela e endpoint

### Pessoas

- **Rota:** `/cadastro/pessoas`
- **Componente:** `CadastroPessoas`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/pessoas`
  - `/api/cadastro/pessoas/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Clientes

- **Rota:** `/cadastro/clientes`
- **Componente:** `Cliente`
- **Endpoints usados:** 13
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/clientes`
  - `/api/cadastro/clientes/*`
  - `/api/cadastro/clientes/*/limite-credito`
  - `/api/cadastro/clientes/logo`
  - `/api/cadastro/clientes/logo/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Fornecedores

- **Rota:** `/cadastro/fornecedores`
- **Componente:** `Fornecedor`
- **Endpoints usados:** 12
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/fornecedores`
  - `/api/cadastro/fornecedores/*`
  - `/api/cadastro/fornecedores/logo`
  - `/api/cadastro/fornecedores/logo/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Produtos

- **Rota:** `/cadastro/produtos`
- **Componente:** `CadastroProdutos`
- **Endpoints usados:** 15
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/categorias`
  - `/api/cadastro/marcas`
  - `/api/cadastro/produtos`
  - `/api/cadastro/produtos/*`
  - `/api/cadastro/produtos/*/imagens`
  - `/api/cadastro/produtos/*/imagens/*`
  - `/api/cadastro/unidades-medida`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Categorias

- **Rota:** `/cadastro/categorias`
- **Componente:** `Categoria`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/categorias`
  - `/api/cadastro/categorias/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Marcas

- **Rota:** `/cadastro/marcas`
- **Componente:** `Marca`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/marcas`
  - `/api/cadastro/marcas/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Serviços

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

### Transportadoras

- **Rota:** `/cadastro/transportadoras`
- **Componente:** `Transportadora`
- **Endpoints usados:** 11
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/pessoas`
  - `/api/cadastro/transportadoras`
  - `/api/cadastro/transportadoras/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Unidades de Medida

- **Rota:** `/cadastro/unidades-medida`
- **Componente:** `UnidadeMedida`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/cadastro/unidades-medida`
  - `/api/cadastro/unidades-medida/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Municípios IBGE

- **Rota:** `/cadastro/municipios`
- **Componente:** `Municipios`
- **Endpoints usados:** 4
  - `/api`
  - `/api/municipios`
  - `/api/municipios/*`
  - `/api/municipios/buscar`

## 3. Backend do módulo

### CategoriaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/categorias/{id}` | sim |
| GET | `/api/cadastro/categorias` | sim |
| GET | `/api/cadastro/categorias/{id}` | sim |
| POST | `/api/cadastro/categorias` | sim |
| PUT | `/api/cadastro/categorias/{id}` | sim |

### ClienteController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/clientes/{id}` | sim |
| GET | `/api/cadastro/clientes` | sim |
| GET | `/api/cadastro/clientes/{id}` | sim |
| POST | `/api/cadastro/clientes` | sim |
| PUT | `/api/cadastro/clientes/{id}` | sim |

### ClienteLogoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/clientes/logo` | sim |
| GET | `/api/cadastro/clientes/logo/{id}` | sim |
| POST | `/api/cadastro/clientes/logo` | sim |

### FornecedorController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/fornecedores/{id}` | sim |
| GET | `/api/cadastro/fornecedores` | sim |
| GET | `/api/cadastro/fornecedores/{id}` | sim |
| POST | `/api/cadastro/fornecedores` | sim |
| PUT | `/api/cadastro/fornecedores/{id}` | sim |

### FornecedorLogoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/fornecedores/logo` | sim |
| GET | `/api/cadastro/fornecedores/logo/{id}` | sim |
| POST | `/api/cadastro/fornecedores/logo` | sim |

### MarcaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/marcas/{id}` | sim |
| GET | `/api/cadastro/marcas` | sim |
| GET | `/api/cadastro/marcas/{id}` | sim |
| POST | `/api/cadastro/marcas` | sim |
| PUT | `/api/cadastro/marcas/{id}` | sim |

### PessoaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/pessoas/{id}` | sim |
| GET | `/api/cadastro/pessoas` | sim |
| GET | `/api/cadastro/pessoas/{id}` | sim |
| POST | `/api/cadastro/pessoas` | sim |
| PUT | `/api/cadastro/pessoas/{id}` | sim |

### ProdutoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/produtos/{id}` | sim |
| GET | `/api/cadastro/produtos` | sim |
| GET | `/api/cadastro/produtos/{id}` | sim |
| GET | `/api/cadastro/produtos/{id}/imagens` | sim |
| GET | `/api/cadastro/produtos/{id}/imagens/{imagemId}` | sim |
| POST | `/api/cadastro/produtos` | sim |
| POST | `/api/cadastro/produtos/{id}/imagens` | sim |
| PUT | `/api/cadastro/produtos/{id}` | sim |

### ServicoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/servicos/{id}` | sim |
| GET | `/api/cadastro/servicos` | sim |
| GET | `/api/cadastro/servicos/{id}` | sim |
| POST | `/api/cadastro/servicos` | sim |
| PUT | `/api/cadastro/servicos/{id}` | sim |

### TransportadoraController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/transportadoras/{id}` | sim |
| GET | `/api/cadastro/transportadoras` | sim |
| GET | `/api/cadastro/transportadoras/{id}` | sim |
| POST | `/api/cadastro/transportadoras` | sim |
| PUT | `/api/cadastro/transportadoras/{id}` | sim |

### UnidadeMedidaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/cadastro/unidades-medida/{id}` | sim |
| GET | `/api/cadastro/unidades-medida` | sim |
| GET | `/api/cadastro/unidades-medida/{id}` | sim |
| POST | `/api/cadastro/unidades-medida` | sim |
| PUT | `/api/cadastro/unidades-medida/{id}` | sim |

