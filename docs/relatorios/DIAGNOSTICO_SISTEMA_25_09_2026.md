> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../i18n/README.pt-BR.md) e
> [`docs/INDICE.md`](../INDICE.md).

# Diagnóstico — BRASIL-SAAS-ERP

**Data:** 25/09/2026
**Escopo:** por que o frontend "parece" menor que o backend, e quais erros precisam de trabalho.

---

## 0. Estado do build (o que NÃO está quebrado)

| Verificação | Resultado |
|---|---|
| `mvn -o compile` | **BUILD SUCCESS** — 557 arquivos Java |
| `npm run build` (Vite) | **OK** — 316 módulos, 7,5 s |
| `npm test` (smoke) | "OK" — **mas o teste é falso**, ver §6 |

Nenhum erro de compilação. Todos os erros abaixo são **de runtime**: o código compila,
mas não funciona no navegador.

> Observação: durante a checagem appeared um erro de compilação real em
> `core/controller/PerfilController.java` (importava `ApiResponse` do pacote
> `core.service.dto`, que não existe). O arquivo foi corrigido durante a análise e
> o build passou. Ele continua **fora do git** (`untracked`).

---

## 1. CAUSA RAIZ — 193 chamadas de API quebradas em 54 arquivos

Este é o motivo de a situação parecer "frontend menor que backend": **as telas existem,
mas não falam com o backend.** Duas falhas independentes, ambas verificadas.

### 1.1 — 68 chamadas com URL literalmente `"[object Object]"` (404)

19 services fazem:

```js
import ApiConfig from './ApiConfig';   // isso importa o OBJETO {BASE_URL, API_BASE_URL, getAuthHeader}
const BASE_URL = ApiConfig;            // BASE_URL passa a ser um objeto, não uma string
...
fetch(`${BASE_URL}/fiscal/ncm`)        // → "[object Object]/fiscal/ncm"
```

`ApiConfig` não é uma string. Interpolado num template literal vira `[object Object]`.

Arquivos afetados (todos): `NcmService`, `CfopService`, `CestService`, `IssqnService`,
`ImpostoService`, `SefazConsultaService`, `EntradaNotaService`, `MarcaService`,
`UnidadeMedidaService`, `TransportadoraService`, `ServicoCadastroService`,
`ClienteLogoService`, `FornecedorLogoService`, `FuncionarioFotoService`,
`FolhaPagamentoService`, `CargoService`, `EmpresaLogoService`, `LogosService`,
`SuperAdminService`.

**Módulos afetados na prática:** fiscal inteiro, cadastros, RH, superadmin, logos.
Essas telas nunca funcionaram.

### 1.2 — 123 chamadas `fetch()` sem header `Authorization` (401)

O backend é `SessionCreationPolicy.STATELESS` e o `JwtAuthenticationFilter` lê
**exclusivamente** o header `Authorization`:

```java
String header = request.getHeader("Authorization");
if (header != null && header.startsWith("Bearer ") ...) { ... }
// não há fallback para cookie
```

E `SecurityConfig` exige `authenticated()` em tudo que é `/api/**`.

Existem **3 clients HTTP diferentes** no frontend, e só um deles envia o token:

| Cliente | Token? | Consequência |
|---|---|---|
| `axios` global (via interceptor em `ApiConfig.js`) | ✅ sim | funciona |
| instância `api` (baseURL `/api`) | ✅ sim | funciona |
| `fetch()` nativo | ❌ **nenhum** | **401 em toda chamada** |

Só **6 arquivos** em todo o frontend leem o token:
`ApiConfig.js`, `AuthService.js`, `downloadService.js`, `ProducaoService.js`,
`RomaneioProducaoService.js`, `main.jsx`.

Ou seja: **todo o resto do frontend (≈50 arquivos) está chamando a API sem autenticação.**
Exemplo verificado — `financeiro/CentroCusto.jsx:61`:

```js
const response = await fetch(`${ApiConfig.BASE_URL}/api/financeiro/centros-custo?${params}`);
// sem segundo argumento: sem headers, sem token → 401
```

> O `withCredentials: true` espalhado por vários services não salva nada: não há
> sessão/cookie no backend para transmitir.

### 1.3 — Como corrigir (duas opções)

**Recomendado: um único client.** Matar o `fetch()` e padronizar no `api` do `ApiConfig.js`,
que já injeta o token:

```js
// ApiConfig.js — adicionar
export const apiGet = (url) => api.get(url);
```

Depois: trocar `fetch(url)` → `api.get(url)` e `fetch(url, {method:'POST', body})` →
`api.post(url, body)` nos ~50 arquivos. O `ApiConfig` já normaliza `/api` e o header.

**Mínimo (19 arquivos):** `const BASE_URL = ApiConfig.API_BASE_URL;` e adicionar o header.
Não resolve os outros ~35 arquivos com `fetch()`.

---

## 2. Cobertura real backend → frontend

| Métrica | Valor |
|---|---|
| Endpoints no backend | **404** |
| Com alguma tela/serviço consumindo | **156 (38 %)** |
| **Sem nenhum consumidor** | **248 (61 %)** |

| Módulo | Endpoints | Cobertos | Sem front |
|---|---|---|---|
| ia | 89 | 24 | **65** |
| bi | 58 | 24 | 34 |
| financeiro | 47 | 14 | 33 |
| cadastro | 61 | 32 | 29 |
| core | 25 | 11 | 14 |
| estoque | 33 | 18 | 15 |
| fiscal | 18 | 2 | **16** |
| rh | 20 | 6 | 14 |
| producao | 16 | 6 | 10 |
| compras | 16 | 10 | 6 |
| vendas | 11 | 2 | 9 |
| servicos | 10 | 7 | 3 |

Detalhe por módulo em `/tmp/opencode/missing_final.json` (gerado pelo script de análise).

### Observações

- **fiscal 2/18** — as 7 telas existem (`Ncm`, `Cfop`, `Cest`, `Issqn`, `Impostos`,
  `EntradaNota`, `SefazConsulta`), mas todas as services estão com o bug do
  `[object Object]`. Não é falta de tela, é tela morta.
- **ia 65/89 sem front** — o módulo de IA é o maior do backend e o menos exposto.
- **vendas 2/11** — só `PedidoVenda` e `TabelaPreco` têm chamada; as demais rotas não.
- **bi 34/58** — existem 3 controllers sobrepostos (`RelatorioController`,
  `ReportController`, `RelatorioAgendadoController`) com sobreposição de função.
  Sinal de refactor pendente.

---

## 3. Flyway vai quebrar em produção

`application-prod.yml:23` tem `validate-on-migrate: true`. Duas mudanças não commitadas
violam isso:

| Migration | Mudança | Efeito em prod |
|---|---|---|
| `V78__estoque_rastreabilidade_lote_reserva.sql` | **editada** (`DO $` → `DO $$`) | *checksum mismatch* → **startup falha** |
| `V75__compras_requisicao_cotacao_workflow.sql` | **renomeada** para `V88__...` | V75 some do histórico, V88 tenta rodar de novo |

`validate-on-migrate: false` só no perfil `dev` — por isso o problema não aparece localmente.

**Correção:** nunca editar/renomear migration já aplicada. Reverter V78 e criar uma
`V89__` nova com a alteração; manter V75 no lugar.

---

## 4. Três `ApiResponse` diferentes no mesmo projeto

| Classe | Envelope JSON | Usada por |
|---|---|---|
| `shared.dto.ApiResponse` | `{success, message, data, errors[], timestamp}` | 5 arquivos |
| `shared.model.ApiResponse` | `{data, errors[{field,message,code}], meta{timestamp,path,statusCode}}` | 2 arquivos |
| `shared.web.ApiResponse` | `{success, code, data}` | 11 arquivos |

18 controllers devolvem três formatos diferentes. O frontend precisa desembrulhar
três envelopes distintos — e `IA.jsx`, `Fiscal.jsx` etc. já fazem isso
(`data?.data?.accessToken` vs `response.data.data`).

**Correção:** ficar com `shared.web.ApiResponse` (a mais usada e mais simples) e migrar
os outros 7 arquivos. Quebra a API, então fazer junto com o item 1.3.

---

## 5. Componentes mortos (nunca montados)

7 arquivos `.jsx` não são importados por ninguém:

| Arquivo | Observação |
|---|---|
| `components/IaAssistWidget.jsx` | consome `/api/ia/recomendacoes/pendentes` — **assistente de IA nunca aparece** |
| `components/RecentUpdates.jsx` | |
| `components/cadastro/ClienteLogo.jsx` | |
| `components/cadastro/FornecedorLogo.jsx` | |
| `components/core/EmpresaLogo.jsx` | |
| `components/core/Logos.jsx` | |
| `components/rh/FuncionarioFoto.jsx` | |

`PrintButton.jsx` está OK (usado em `CadastroProdutos.jsx` e `Relatorios.jsx`).

O `IaAssistWidget` é o mais relevante: parece functionality pronta e nunca foi ligado
ao `Layout`.

---

## 6. O smoke test não testa nada

`frontend.smoke.test.js` verifica apenas que **7 arquivos existem**. Não faz
nenhuma verificação de API, rota, import ou sintaxe de tela. Foi por isso que os
193 bugs de runtime passaram: o build compila, o smoke passa, e a aplicação continua
quebrada em produção.

---

## 7. Ordem de trabalho sugerida

1. **Unificar o client HTTP** (§1.3) — mata 191 das 193 chamadas quebradas de uma vez.
   É o item de maior retorno e destrava tudo que vem depois.
2. **Corrigir o Flyway** (§3) — só quebra em prod, mas quando quebra o serviço não sobe.
3. **Montar `IaAssistWidget` e `RecentUpdates`** (§5) — 2 telas prontas, só falta ligar.
4. **Unificar `ApiResponse`** (§4) — junto com o passo 1.
5. **Cobrir os 248 endpoints sem front** (§2) — por módulo, começando por
   `financeiro` (33) e `bi` (34), que são os que o usuário mais opera.
6. **Substituir o smoke test** (§6) por testes que de fato importem as telas e
   verifiquem as rotas — foi a lacuna que deixou os bugs passarem.

---

## Apêndice — como reproduzir a análise

Scripts em `/tmp/opencode/`:
- `gap5.py` → tabela de cobertura (§2), grava `missing_final.json`
- `fetch_audit.py` → diagnóstico das 193 chamadas (§1), grava `fetch_audit.json`

Ambos varrem `src/main/java` (rotas) e `src/main/resources/static/react/src`
(resolvendo constantes e template literals, incluindo `axios`, `api` e `fetch`).
