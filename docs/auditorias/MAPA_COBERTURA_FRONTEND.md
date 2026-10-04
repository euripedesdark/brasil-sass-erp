> ⏳ **Registro histórico** — este documento é um retrato pontual da data
> do arquivo e **não reflete o estado atual** do sistema. Para o estado
> atual, veja [`README.pt-BR.md`](../README.pt-BR.md) e
> [`docs/INDICE.md`](INDICE.md).

# Mapa de cobertura de frontend — levantamento histórico

> **STATUS HISTÓRICO — 30/09/2026:** este documento foi produzido antes das correções de cobertura de `ComissaoController`, `CaixaController` e da sincronização da tela fiscal com a API NFS-e. **Não use os números 212 nem as afirmações de que Comissão/Caixa não possuem backend como estado atual.** O levantamento vigente está em `docs/auditorias/MAPA_COBERTURA_FRONTEND_ATUAL_2026-09-30.md`.

**Data:** 26/09/2026
**Método:** extração dos 422 endpoints dos 81 controllers e confronto com os
chamados pelo frontend (146 caminhos distintos), resolvendo as constantes de base
de cada service.

> O número final carrega uma margem de erro conhecida — ver *Ressalva* no fim.
> As/controllers listados como "sem tela" foram conferidos um a um.

---

## Resumo

| | |
|---|---|
| Controllers | 81 |
| Endpoints | 422 |
| Endpoints sem nenhum acesso na tela | **212** |
| Controllers com pelo menos 1 endpoint inacessível | 47 |

---

## 1. Módulo de IA — 73 endpoints, nenhum na tela

O `IA.jsx` existe e conversa apenas com o chat. Todo o resto do backend de IA é
invisível: ninguém consegue classificar produto, gerar embedding, gerenciar
prompt ou ver análise preditiva.

| Controller | Sem tela | O que o backend faz |
|---|---|---|
| `AnalisePreditivaController` | 14/14 | previsão de venda, estoque e financeiro; análises pendentes e de alta confiança |
| `ClassificacaoController` | 14/14 | classificar texto/produto/lote, aprovar e rejeitar classificação |
| `EmbeddingController` | 12/12 | gerar, reindexar e buscar por similaridade |
| `PromptController` | 12/12 | biblioteca de prompts, favoritos, mais usados, públicos |
| `AIConfigController` | 5/5 | configuração da IA, teste de conexão, uso de tokens |

O `AuditoriaFuncionalERP.jsx` já registra isso: *"Configuração, prompts,
templates, classificação, embeddings, previsões… ainda não expostas no front"*.

## 2. Financeiro — 44 endpoints

| Controller | Sem tela | Observação |
|---|---|---|
| `BoletoController` | 10/10 | emitir boleto, remessa, retorno, serviço — **nenhuma tela** |
| `ReportController` | 9/9 | relatórios; `Relatorios.jsx` usa outro caminho |
| `ConciliacaoController` | 5/7 | conciliação bancária tem tela, mas parcial |
| `ComissaoController` | — | **não existe** (ver seção 4) |

## 3. Restante por área

| Área | Endpoints sem tela |
|---|---|
| `/api/bi` | 31 — indicadores, KPIs e relatórios agendados pela metade |
| `/api/fiscal` | 21 |
| `/api/estoque` | 15 |
| `/api/cadastro` | 14 |
| `/api/rh` | 12 |
| `/api/core` | 10 |
| `/api/vendas` | 9 |
| `/api/producao` | 8 |
| `/api/superadmin` | 6 |
| `/api/compras` | 6 |
| `/api/documentos` | 4 |

## 4. A lacuna mais grave: capacidade sem backend

O inverso também existe, e é pior — a tela está pronta e não conversa com nada:

| Tela | Chamada | Situação |
|---|---|---|
| **Comissões** | `/api/financeiro/comissoes` | **não existe controller**. A tela engole o 404 e fica branca |
| **Comissões** | regras de comissão | sem controller: nunca são cadastradas, então **toda comissão é gravada como 0,00%** |
| **Caixa** | `/api/caixa` | **não existe nenhum endpoint**. A tela do menu não tem como funcionar |
| **NF-e** | `/api/fiscal/nfe/emitir` | **não existe controller** (só NFS-e foi implementada) |

## 5. Backend pronto, tela usando só uma parte

Vale registrar: a tela de Títulos tem 11 endpoints no backend e a UI só lista —
os 10 de **fluxo de aprovação** (solicitar, aprovar, rejeitar) não têm tela
nenhuma. Mesmo formato em Conta Bancária e Extrato.

---

## Ressalva honesta sobre o método

O extrator casa literais de URL e resolve constantes de base. Validei contra
casos que sabiam existir e corrigi três padrões (`${ApiConfig.BASE_URL || ''}`,
`ApiConfig.API_BASE_URL`, templates em `apiFetch`). **Ainda restam falsos
positivos**: services que montam a URL com constante membro e chamam por
`apiFetch` — o extrator não captura. Cargo, Folha de Pagamento e parte de
Títulos aparecem como "sem tela" tendo tela; são ~20-30 endpoints de ruído.

Por isso: **212 é o teto, não o número exato.** Os controllers marcados como
inteiramente sem tela foram conferidos individualmente e são reais.

Para fechar a conta, o caminho é testar em runtime como fiz na auditoria de
quebras: chamar cada endpoint e ver o que responde, em vez de casar texto.

---

## Ordem sugerida

1. **Comissões e Caixa** — telas de menu mortas, mesmo padrão de trabalho
2. **Boletos** (10 endpoints, nenhum acessível) e **fluxo de aprovação de
   títulos** (10 endpoints, nenhum acessível) — capacidade pronta sem tela
3. **Módulo de IA** — 73 endpoints, é o maior bloco isolado
4. **NF-e** — maior trabalho; depende de definir o escopo
