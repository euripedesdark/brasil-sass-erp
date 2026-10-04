> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Inteligência Artificial

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 1 |
| Controllers | 9 |
| Endpoints | 89 |
| Endpoints sem tela | 57 |

---

## 1. Menu

### Geral

| Submenu | Rota | Componente |
|---|---|---|
| Inteligência Artificial | `/ia` | `IA` |

## 2. Função por função: tela e endpoint

### Inteligência Artificial

- **Rota:** `/ia`
- **Componente:** `IA`
- **Endpoints usados:** 22
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/ia/analises`
  - `/api/ia/analises/prever-estoque`
  - `/api/ia/analises/prever-financeiro`
  - `/api/ia/analises/prever-vendas`
  - `/api/ia/chat`
  - `/api/ia/chat/simples`
  - `/api/ia/classificacoes/classificar-texto`
  - `/api/ia/config`
  - `/api/ia/config/remaining-tokens`
  - `/api/ia/config/test-connection`
  - `/api/ia/embeddings/gerar`
  - `/api/ia/mensagens/sessao/*/ordenado`
  - `/api/ia/sessoes`
  - `/api/ia/sessoes/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

## 3. Backend do módulo

### AIConfigController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/ia/config` | sim |
| GET | `/api/ia/config/remaining-tokens` | sim |
| POST | `/api/ia/config` | sim |
| POST | `/api/ia/config/reset-usage` | — |
| POST | `/api/ia/config/test-connection` | sim |

### AnalisePreditivaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/analises/{id}` | sim |
| GET | `/api/ia/analises` | sim |
| GET | `/api/ia/analises/entidade/{entidade}/{entidadeId}` | — |
| GET | `/api/ia/analises/high-confidence` | — |
| GET | `/api/ia/analises/pendentes` | — |
| GET | `/api/ia/analises/prever-estoque` | sim |
| GET | `/api/ia/analises/prever-financeiro` | sim |
| GET | `/api/ia/analises/prever-vendas` | sim |
| GET | `/api/ia/analises/tipo/{tipo}` | — |
| GET | `/api/ia/analises/{id}` | sim |
| POST | `/api/ia/analises` | sim |
| POST | `/api/ia/analises/executar-pendentes` | — |
| POST | `/api/ia/analises/{id}/recalcular` | — |
| PUT | `/api/ia/analises/{id}` | sim |

### ChatController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/ia/chat/gerar-resposta` | — |
| POST | `/api/ia/chat` | sim |
| POST | `/api/ia/chat/contexto` | — |
| POST | `/api/ia/chat/corrigir` | — |
| POST | `/api/ia/chat/extrair-informacoes` | — |
| POST | `/api/ia/chat/gerar-resposta-historico` | — |
| POST | `/api/ia/chat/resumir` | — |
| POST | `/api/ia/chat/simples` | sim |
| POST | `/api/ia/chat/traduzir` | — |

### ChatMensagemController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/mensagens/sessao/{sessaoId}` | sim |
| DELETE | `/api/ia/mensagens/{id}` | sim |
| GET | `/api/ia/mensagens/sessao/{sessaoId}` | sim |
| GET | `/api/ia/mensagens/sessao/{sessaoId}/ordenado` | sim |
| GET | `/api/ia/mensagens/{id}` | sim |
| POST | `/api/ia/mensagens` | — |
| POST | `/api/ia/mensagens/{id}/classificar` | — |

### ChatSessaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/sessoes/{id}` | sim |
| GET | `/api/ia/sessoes` | sim |
| GET | `/api/ia/sessoes/favoritos/{usuarioId}` | — |
| GET | `/api/ia/sessoes/recentes/{usuarioId}` | — |
| GET | `/api/ia/sessoes/usuario/{usuarioId}` | — |
| GET | `/api/ia/sessoes/{id}` | sim |
| POST | `/api/ia/sessoes` | sim |
| POST | `/api/ia/sessoes/{id}/favorito` | — |
| POST | `/api/ia/sessoes/{id}/limpar` | — |
| PUT | `/api/ia/sessoes/{id}` | sim |

### ClassificacaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/classificacoes/{id}` | sim |
| GET | `/api/ia/classificacoes` | — |
| GET | `/api/ia/classificacoes/entidade/{entidadeId}` | — |
| GET | `/api/ia/classificacoes/high-confidence/{tipo}` | — |
| GET | `/api/ia/classificacoes/pendentes/{tipo}` | — |
| GET | `/api/ia/classificacoes/tipo/{tipo}` | — |
| GET | `/api/ia/classificacoes/{id}` | sim |
| POST | `/api/ia/classificacoes` | — |
| POST | `/api/ia/classificacoes/classificar-lote` | — |
| POST | `/api/ia/classificacoes/classificar-produto` | — |
| POST | `/api/ia/classificacoes/classificar-texto` | sim |
| POST | `/api/ia/classificacoes/{id}/aprovar` | — |
| POST | `/api/ia/classificacoes/{id}/rejeitar` | — |
| PUT | `/api/ia/classificacoes/{id}` | sim |

### EmbeddingController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/embeddings/entidade/{entidadeTipo}/{entidadeId}` | — |
| DELETE | `/api/ia/embeddings/{id}` | sim |
| GET | `/api/ia/embeddings` | — |
| GET | `/api/ia/embeddings/entidade-tipo/{entidadeTipo}` | — |
| GET | `/api/ia/embeddings/entidade/{entidadeTipo}/{entidadeId}` | — |
| GET | `/api/ia/embeddings/mais-similar` | — |
| GET | `/api/ia/embeddings/similares` | — |
| GET | `/api/ia/embeddings/{id}` | sim |
| POST | `/api/ia/embeddings` | — |
| POST | `/api/ia/embeddings/gerar` | sim |
| POST | `/api/ia/embeddings/reindexar` | — |
| PUT | `/api/ia/embeddings/{id}` | sim |

### PromptController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/prompts/{id}` | — |
| GET | `/api/ia/prompts` | — |
| GET | `/api/ia/prompts/buscar` | — |
| GET | `/api/ia/prompts/categoria/{categoria}` | — |
| GET | `/api/ia/prompts/favoritos` | — |
| GET | `/api/ia/prompts/mais-usados` | — |
| GET | `/api/ia/prompts/publicos` | — |
| GET | `/api/ia/prompts/{id}` | — |
| POST | `/api/ia/prompts` | — |
| POST | `/api/ia/prompts/{id}/favorito` | — |
| POST | `/api/ia/prompts/{id}/incrementar-uso` | — |
| PUT | `/api/ia/prompts/{id}` | — |

### PromptTemplateController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/ia/prompt-templates/{id}` | — |
| GET | `/api/ia/prompt-templates` | — |
| GET | `/api/ia/prompt-templates/category/{category}` | — |
| GET | `/api/ia/prompt-templates/paginated` | — |
| GET | `/api/ia/prompt-templates/{id}` | — |
| POST | `/api/ia/prompt-templates` | — |

