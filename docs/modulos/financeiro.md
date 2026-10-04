> Atualizado em 04/10/2026 — inventário verificado no código, banco e smoke test.

# Financeiro

Gerado a partir do código: menu lateral, rotas do `App.jsx`, controllers e as
chamadas de API que cada tela realmente faz. Nomenclatura própria do sistema.

| | |
|---|---|
| Itens de menu | 16 |
| Controllers | 12 |
| Endpoints | 69 |
| Endpoints sem tela | 0 |

---

## 1. Menu

### Financeiro

| Submenu | Rota | Componente |
|---|---|---|
| Visão Geral | `/financeiro` | `FinanceiroHub` |
| Contas a Pagar e Receber | `/financeiro/lancamentos` | `Titulo` |
| Títulos | `/financeiro/titulos` | `Titulo` |
| Aprovações de Títulos | `/financeiro/aprovacoes-titulos` | `AprovacoesTitulos` |
| Comissões | `/financeiro/comissoes` | `Comissoes` |
| Caixa | `/financeiro/caixa` | `Caixa` |
| Boletos | `/financeiro/boletos` | `Boletos` |
| Extratos | `/financeiro/extrato` | `Extrato` |
| Conciliação Bancária | `/financeiro/conciliacao` | `ConciliacaoBancaria` |
| Auditoria Funcional ERP | `/bi/auditoria-funcional` | `AuditoriaFuncionalERP` |
| Contas Bancárias | `/financeiro/contas-bancarias` | `ContaBancaria` |
| Plano de Contas | `/financeiro/plano-contas` | `PlanoContas` |
| Lançamentos Contábeis | `/financeiro/contabil` | `LancamentoContabil` |
| Centro de Custos | `/financeiro/centro-custos` | `CentroCusto` |
| Condições de Pagamento | `/financeiro/condicoes-pagamento` | `CondicaoPagamento` |
| Tipos de Pagamento | `/financeiro/tipos-pagamento` | `TipoPagamento` |

## 2. Função por função: tela e endpoint

### Visão Geral

- **Rota:** `/financeiro`
- **Componente:** `FinanceiroHub`
- **Endpoints usados:** nenhum identificado (tela pode estar mock ou só ler por outro caminho)

### Contas a Pagar e Receber

- **Rota:** `/financeiro/lancamentos`
- **Componente:** `Titulo`
- **Endpoints usados:** 22
  - `/api`
  - `/api/api/financeiro/contas-bancarias`
  - `/api/api/financeiro/tipos-pagamento`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/contas-bancarias`
  - `/api/financeiro/tipos-pagamento`
  - `/api/financeiro/titulos`
  - `/api/financeiro/titulos/*`
  - `/api/financeiro/titulos/*/aprovacao/solicitar`
  - `/api/financeiro/titulos/*/aprovacoes`
  - `/api/financeiro/titulos/*/baixar`
  - `/api/financeiro/titulos/*/parcelas`
  - `/api/financeiro/titulos/aprovacoes/*/aprovar`
  - `/api/financeiro/titulos/aprovacoes/*/rejeitar`
  - `/api/financeiro/titulos/aprovacoes/pendentes`
  - `/api/financeiro/titulos/vencimentos`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Títulos

- **Rota:** `/financeiro/titulos`
- **Componente:** `Titulo`
- **Endpoints usados:** 22
  - `/api`
  - `/api/api/financeiro/contas-bancarias`
  - `/api/api/financeiro/tipos-pagamento`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/contas-bancarias`
  - `/api/financeiro/tipos-pagamento`
  - `/api/financeiro/titulos`
  - `/api/financeiro/titulos/*`
  - `/api/financeiro/titulos/*/aprovacao/solicitar`
  - `/api/financeiro/titulos/*/aprovacoes`
  - `/api/financeiro/titulos/*/baixar`
  - `/api/financeiro/titulos/*/parcelas`
  - `/api/financeiro/titulos/aprovacoes/*/aprovar`
  - `/api/financeiro/titulos/aprovacoes/*/rejeitar`
  - `/api/financeiro/titulos/aprovacoes/pendentes`
  - `/api/financeiro/titulos/vencimentos`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Aprovações de Títulos

- **Rota:** `/financeiro/aprovacoes-titulos`
- **Componente:** `AprovacoesTitulos`
- **Endpoints usados:** 12
  - `/api`
  - `/api/auth/refresh`
  - `/api/financeiro/titulos`
  - `/api/financeiro/titulos/*`
  - `/api/financeiro/titulos/*/aprovacao/solicitar`
  - `/api/financeiro/titulos/*/aprovacoes`
  - `/api/financeiro/titulos/*/baixar`
  - `/api/financeiro/titulos/*/parcelas`
  - `/api/financeiro/titulos/aprovacoes/*/aprovar`
  - `/api/financeiro/titulos/aprovacoes/*/rejeitar`
  - `/api/financeiro/titulos/aprovacoes/pendentes`
  - `/api/financeiro/titulos/vencimentos`

### Comissões

- **Rota:** `/financeiro/comissoes`
- **Componente:** `Comissoes`
- **Endpoints usados:** 14
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/comissoes`
  - `/api/financeiro/comissoes/*/pagar`
  - `/api/financeiro/comissoes/regras`
  - `/api/financeiro/comissoes/regras/*`
  - `/api/financeiro/comissoes/resumo`
  - `/api/rh/funcionarios`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Caixa

- **Rota:** `/financeiro/caixa`
- **Componente:** `Caixa`
- **Endpoints usados:** 5
  - `/api`
  - `/api/auth/refresh`
  - `/api/caixa`
  - `/api/financeiro/caixas`
  - `/api/financeiro/caixas/*`

### Boletos

- **Rota:** `/financeiro/boletos`
- **Componente:** `Boletos`
- **Endpoints usados:** 12
  - `/api`
  - `/api/auth/refresh`
  - `/api/core/bancos`
  - `/api/core/bancos/*`
  - `/api/financeiro/boletos`
  - `/api/financeiro/boletos/*/arquivo`
  - `/api/financeiro/boletos/emitir`
  - `/api/financeiro/boletos/nosso-numero`
  - `/api/financeiro/boletos/remessas`
  - `/api/financeiro/boletos/retornos`
  - `/api/financeiro/boletos/servico`
  - `/api/financeiro/boletos/validar`

### Extratos

- **Rota:** `/financeiro/extrato`
- **Componente:** `Extrato`
- **Endpoints usados:** 7
  - `/api`
  - `/api/auth/refresh`
  - `/api/financeiro/contas-bancarias`
  - `/api/financeiro/contas-bancarias/*`
  - `/api/financeiro/extratos`
  - `/api/financeiro/extratos/conta/*`
  - `/api/financeiro/extratos/pendentes-conciliacao`

### Conciliação Bancária

- **Rota:** `/financeiro/conciliacao`
- **Componente:** `ConciliacaoBancaria`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/refresh`
  - `/api/financeiro/conciliacoes`
  - `/api/financeiro/conciliacoes/*/baixas`
  - `/api/financeiro/conciliacoes/*/fechar`
  - `/api/financeiro/conciliacoes/*/itens`
  - `/api/financeiro/conciliacoes/*/pendentes`
  - `/api/financeiro/conciliacoes/*/vincular`
  - `/api/financeiro/contas-bancarias`
  - `/api/financeiro/contas-bancarias/*`

### Auditoria Funcional ERP

- **Rota:** `/bi/auditoria-funcional`
- **Componente:** `AuditoriaFuncionalERP`
- **Endpoints usados:** nenhum identificado (tela pode estar mock ou só ler por outro caminho)

### Contas Bancárias

- **Rota:** `/financeiro/contas-bancarias`
- **Componente:** `ContaBancaria`
- **Endpoints usados:** 4
  - `/api`
  - `/api/auth/refresh`
  - `/api/financeiro/contas-bancarias`
  - `/api/financeiro/contas-bancarias/*`

### Plano de Contas

- **Rota:** `/financeiro/plano-contas`
- **Componente:** `PlanoContas`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/plano-contas`
  - `/api/financeiro/plano-contas/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Lançamentos Contábeis

- **Rota:** `/financeiro/contabil`
- **Componente:** `LancamentoContabil`
- **Endpoints usados:** 11
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/lancamentos`
  - `/api/financeiro/lancamentos/*`
  - `/api/financeiro/lancamentos/*/partidas`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Centro de Custos

- **Rota:** `/financeiro/centro-custos`
- **Componente:** `CentroCusto`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/centros-custo`
  - `/api/financeiro/centros-custo/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Condições de Pagamento

- **Rota:** `/financeiro/condicoes-pagamento`
- **Componente:** `CondicaoPagamento`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/condicoes-pagamento`
  - `/api/financeiro/condicoes-pagamento/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

### Tipos de Pagamento

- **Rota:** `/financeiro/tipos-pagamento`
- **Componente:** `TipoPagamento`
- **Endpoints usados:** 10
  - `/api`
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/api/auth/me`
  - `/api/auth/refresh`
  - `/api/financeiro/tipos-pagamento`
  - `/api/financeiro/tipos-pagamento/*`
  - `/auth/login`
  - `/auth/logout`
  - `/auth/me`

## 3. Backend do módulo

### BoletoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/financeiro/boletos` | sim |
| GET | `/api/financeiro/boletos/remessas` | sim |
| GET | `/api/financeiro/boletos/retornos` | sim |
| GET | `/api/financeiro/boletos/servico` | sim |
| GET | `/api/financeiro/boletos/{id}/arquivo` | sim |
| POST | `/api/financeiro/boletos/emitir` | sim |
| POST | `/api/financeiro/boletos/nosso-numero` | sim |
| POST | `/api/financeiro/boletos/remessas` | sim |
| POST | `/api/financeiro/boletos/retornos` | sim |
| POST | `/api/financeiro/boletos/validar` | sim |

### CaixaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/caixas/{id}` | sim |
| GET | `/api/financeiro/caixas` | sim |
| GET | `/api/financeiro/caixas/{id}` | sim |
| POST | `/api/financeiro/caixas` | sim |
| PUT | `/api/financeiro/caixas/{id}` | sim |

### CentroCustoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/centros-custo/{id}` | sim |
| GET | `/api/financeiro/centros-custo` | sim |
| POST | `/api/financeiro/centros-custo` | sim |
| PUT | `/api/financeiro/centros-custo/{id}` | sim |

### ComissaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/comissoes/regras/{id}` | sim |
| GET | `/api/financeiro/comissoes` | sim |
| GET | `/api/financeiro/comissoes/regras` | sim |
| GET | `/api/financeiro/comissoes/resumo` | sim |
| POST | `/api/financeiro/comissoes/regras` | sim |
| POST | `/api/financeiro/comissoes/{id}/pagar` | sim |
| PUT | `/api/financeiro/comissoes/regras/{id}` | sim |

### ConciliacaoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/financeiro/conciliacoes` | sim |
| GET | `/api/financeiro/conciliacoes/{id}/baixas` | sim |
| GET | `/api/financeiro/conciliacoes/{id}/itens` | sim |
| GET | `/api/financeiro/conciliacoes/{id}/pendentes` | sim |
| POST | `/api/financeiro/conciliacoes` | sim |
| POST | `/api/financeiro/conciliacoes/{id}/fechar` | sim |
| POST | `/api/financeiro/conciliacoes/{id}/vincular` | sim |

### CondicaoPagamentoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/condicoes-pagamento/{id}` | sim |
| GET | `/api/financeiro/condicoes-pagamento` | sim |
| POST | `/api/financeiro/condicoes-pagamento` | sim |
| PUT | `/api/financeiro/condicoes-pagamento/{id}` | sim |

### ContaBancariaController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/contas-bancarias/{id}` | sim |
| GET | `/api/financeiro/contas-bancarias` | sim |
| POST | `/api/financeiro/contas-bancarias` | sim |
| PUT | `/api/financeiro/contas-bancarias/{id}` | sim |

### ExtratoController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/financeiro/extratos/conta/{contaId}` | sim |
| GET | `/api/financeiro/extratos/pendentes-conciliacao` | sim |
| POST | `/api/financeiro/extratos` | sim |

### LancamentoContabilController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/lancamentos/{id}` | sim |
| GET | `/api/financeiro/lancamentos` | sim |
| GET | `/api/financeiro/lancamentos/{id}` | sim |
| GET | `/api/financeiro/lancamentos/{id}/partidas` | sim |
| POST | `/api/financeiro/lancamentos` | sim |
| PUT | `/api/financeiro/lancamentos/{id}` | sim |

### PlanoContasController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/plano-contas/{id}` | sim |
| GET | `/api/financeiro/plano-contas` | sim |
| POST | `/api/financeiro/plano-contas` | sim |
| PUT | `/api/financeiro/plano-contas/{id}` | sim |

### TipoPagamentoController

| | Endpoint | Acesso por tela |
|---|---|---|
| DELETE | `/api/financeiro/tipos-pagamento/{id}` | sim |
| GET | `/api/financeiro/tipos-pagamento` | sim |
| POST | `/api/financeiro/tipos-pagamento` | sim |
| PUT | `/api/financeiro/tipos-pagamento/{id}` | sim |

### TituloController

| | Endpoint | Acesso por tela |
|---|---|---|
| GET | `/api/financeiro/titulos` | sim |
| GET | `/api/financeiro/titulos/aprovacoes/pendentes` | sim |
| GET | `/api/financeiro/titulos/vencimentos` | sim |
| GET | `/api/financeiro/titulos/{id}` | sim |
| GET | `/api/financeiro/titulos/{id}/aprovacoes` | sim |
| GET | `/api/financeiro/titulos/{id}/parcelas` | sim |
| POST | `/api/financeiro/titulos/aprovacoes/{aprovacaoId}/aprovar` | sim |
| POST | `/api/financeiro/titulos/aprovacoes/{aprovacaoId}/rejeitar` | sim |
| POST | `/api/financeiro/titulos/{id}/aprovacao/solicitar` | sim |
| POST | `/api/financeiro/titulos/{id}/baixar` | sim |
| POST | `/api/financeiro/titulos/{id}/parcelas` | sim |

