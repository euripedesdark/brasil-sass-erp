> Atualizado em 08/10/2026.

# Ativos — Imobilizado (FI-AA) e Manutenção (PM)

## Telas

| Menu | Rota | Componente |
|---|---|---|
| Ativos | `/ativos` | `Ativos` (abas: Ativos, Classes, Depreciação mensal, Relatórios) |
| Manutenção (ordens, notas, planos) | `/ativos/manutencao` | `ManutencaoAtivos` |
| Indicadores | `/ativos/indicadores` | `IndicadoresAtivos` |

## Imobilizado (FI-AA)

- **Classe de ativo** (`bc_ativo_classe`): método (`LINEAR`, `SOMA_DIGITOS`, `SALDO_DECRESCENTE`),
  vida útil, taxa anual e contas contábeis (ativo, depreciação acumulada, despesa, ganho/perda na
  baixa, reavaliação, impairment). O ativo herda os padrões da classe.
- **Movimentos** (`bc_ativo_movimento`): aquisição, adição ao custo, transferência (centro de custo,
  localização, responsável), reavaliação, impairment, depreciação, estorno e baixa total/parcial
  (por percentual, com valor de venda e ganho/perda apurados).
- **Depreciação mensal em lote** (`bc_ativo_depreciacao_execucao`): simular, executar (uma vez por
  período) e estornar (somente a última execução).
- **Relatórios**: posição por classe (custo, depreciação acumulada, impairment, valor contábil) e
  projeção de depreciação.
- **Contabilização**: gera lançamento no razão quando a classe tem as contas preenchidas; sem contas,
  o movimento fica só no histórico do ativo.

## Manutenção (PM)

- **Notas** (`bc_ativo_nota_manutencao`): avaria, solicitação; viram ordem corretiva.
- **Ordens** (`bc_ativo_manutencao`): tipos `CORRETIVA`, `PREVENTIVA`, `PREDITIVA`, `INSPECAO`, `MELHORIA`;
  ciclo `ABERTA → LIBERADA → EM_EXECUCAO → CONCLUIDA` (ou `CANCELADA`); causa, solução, checklist,
  horas de parada.
- **Materiais** e **apontamentos de horas**: compõem o custo (material + mão de obra + serviço).
- **Planos preventivos** (`bc_ativo_plano_manutencao`): ciclo por tempo (dias) ou por contador;
  geração automática das ordens vencidas.
- **Medições** (`bc_ativo_medicao`): atualizam o contador do ativo (não pode retroceder).
- **Histórico por ativo**: ordens, custos, horas trabalhadas e paradas.

## Endpoints

`/api/ativos/**` (`AtivosController`) e `/api/ativos/manutencoes|notas|planos|medicoes/**`
(`ManutencaoController`). Migration: `V173__ativos_fi_aa_pm.sql`.
