> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../README.pt-BR.md) e
> [`docs/INDICE.md`](INDICE.md).

# Auditoria e Correções — Cobertura de Frontend por Backend

**Data:** 25/09/2026
**Escopo:** garantir que todo backend que o usuário precisa operar tenha frontend com
acesso, visualizar, criar, modificar, imprimir e apagar.

---

## Fase 0 — Build quebrado (bloqueava tudo)

O build do frontend **não compilava**. Corrigido:

| Arquivo | Problema | Correção |
|---|---|---|
| `estoque/TransferenciasEstoque.jsx` | Objeto literal não fechado em `toast.show({... life:5000)` + bloco `if(modoInterno)` sem `}` — erro de sintaxe | Função `transfer` reescrita de forma legível, separada em `transferirInterno` / `transferirEntreDepositos` |
| `cadastro/Cliente.jsx` | Importava `{ ClienteService }`, mas o service só tinha `export default` | Export nomeado adicionado |
| `rh/FuncionarioFoto.jsx` | Importava `services/FuncionarioService` que **não existia** | Service criado + `fetchFuncionarios` deixou de retornar array vazio |

> Sem isso, `npm run build` falhava e nenhum deploy era possível.

---

## Fase 1 — Endpoints que o frontend chamava mas não existiam (16 → 404/405)

| Controller | Faltava | Situação |
|---|---|---|
| `rh/FuncionarioController` | `GET/PUT/DELETE /{id}` | lê, edita, desativa colaborador |
| `rh/CargoController` | `GET/PUT/DELETE /{id}` | lê, edita, desativa cargo |
| `rh/FolhaPagamentoController` | `PUT/DELETE /{id}` | edita folha aberta, exclui folha não paga |
| `financeiro/CentroCustoController` | `PUT/DELETE /{id}` | idem |
| `financeiro/PlanoContasController` | `PUT/DELETE /{id}` | idem |
| `financeiro/TipoPagamentoController` | `PUT/DELETE /{id}` | idem |
| `financeiro/CondicaoPagamentoController` | `PUT /{id}` | idem |
| `financeiro/ContaBancariaController` | `PUT /{id}` | idem |
| `financeiro/LancamentoContabilController` | `GET /{id}/partidas`, `PUT/DELETE /{id}` | lançamentos contábeis weren't editable at all |
| `servicos/OrdemServicoController` | `GET /{id}/itens`, `GET /{id}/pdf`, `PUT/DELETE /{id}` | impressão de OS |

### Correção de segurança aplicada

`core/RelatorioController` aceitava `empresaId` como **query param** e não tinha
autenticação — permitia ler o relatório de outra empresa. Agora o tenant vem
sempre do token (`@AuthenticationPrincipal`).

### Sobre permissões

Não existem permissões `bi:*` no banco (o módulo BI usa `hasRole('ADMIN')`).
Nenhum endpoint novo depende de `bi:*`, para não introduzir 403.

---

## Fase 2 — Impressão

- Criado `services/downloadService.js`:
  - `downloadAuthenticated()` — baixa PDF/Excel/CSV com o header `Authorization`.
    **Motivo:** `window.open()` não envia header e caía em 401. Era a causa do
    botão "Exportar PDF" quebrado.
  - `imprimirElemento()` — imprime só a tabela da tela via iframe isolado.
- Criado `components/shared/PrintButton.jsx` (reutilizável).
- `Relatorios.jsx` passa a usar download autenticado.

---

## Fase 3 — Telas que eram mock

| Tela | Antes | Depois |
|---|---|---|
| `cadastro/CadastroProdutos.jsx` | **STUB**: 4 produtos fictícios, nenhuma API, nenhum botão | CRUD completo (criar, editar, ativar/desativar, excluir), busca, filtros, cálculo de margem, impressão. O backend já tinha tudo isso + upload de imagens |
| `contexts/AuthContext.jsx` | `loadProfile()` **nunca era chamado**; `isAdmin`/`isDiretoria` sempre `undefined` | Carrega `/api/auth/me` e deriva `isAdmin`, `isDiretoria`, `perfis`, `permissoes`, `pode(recurso, acao)`. O menu **Administração inteiro** estava morto |

### Bugs de menu corrigidos

| Bug | Correção |
|---|---|
| "Console SQL" apontava para `/admin/paridade-sapadmin/sql` (rota inexistente → caía no 404 `/inicio`) | `/admin/sql` |
| `/estoque/inventarios` era rota sem item de menu | item adicionado |
| `/admin/paridade-erp` era rota sem item de menu | item adicionado (só admin) |

---

## Como validar

```bash
cd BRASIL-SAAS-ERP
mvn -o compile                  # backend
cd src/main/resources/static/react && npx vite build   # frontend
```
