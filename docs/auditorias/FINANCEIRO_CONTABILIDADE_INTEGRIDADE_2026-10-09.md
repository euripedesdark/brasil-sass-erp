# Financeiro e Contabilidade: integridade dos fluxos existentes

Base: main `03a0548412790f6ae8bbd1b5dbff22f928280a18`, depois da PR 113.
As funcionalidades existentes foram preservadas e corrigidas nos pontos abaixo.

## Evidencias e correcoes

| Cenario | Comportamento anterior | Comportamento corrigido |
| --- | --- | --- |
| Titulo emitido em 01/01, vencendo em 31/01, sem condicao de pagamento | Parcela vencia em 01/01; falha reproduzida por teste | Parcela conserva 31/01 |
| Dois centavos em quatro parcelas | Arredondamento HALF_UP podia deixar a ultima parcela negativa | Centavos distribuidos sem parcela negativa e sem alterar a soma |
| Partida com debito 10 e credito -1 | Gravacao aceita; falha reproduzida por teste | Valores negativos recusados na inclusao e na contabilizacao |
| Original estornado mais contrapartida no mesmo periodo | Relatorio excluia o original e apresentava saldo incorreto; falha reproduzida por teste | Original e contrapartida permanecem no razao, balancete, balanco e DRE |
| Edicao de partida simultanea a contabilizacao | Consulta do rascunho sem trava | Inclusao, remocao e contabilizacao bloqueiam o mesmo lancamento |
| Duas solicitacoes de contabilizacao do titulo | Verificacao de duplicidade sem trava do titulo | Titulo bloqueado antes de conferir/gerar o lancamento |

Prazos explicitamente configurados continuam sendo utilizados. Na ausencia de
prazo configurado, a data original do titulo prevalece; se ela tambem estiver
ausente, permanece o comportamento de vencimento na emissao. Valores historicos
nao sao reescritos. O parcelamento recusa saldo nulo ou negativo.

O estorno mantem o lancamento original como ESTORNADO e grava a contrapartida
como LANCADO. Ambos sao movimentos contabilizados: excluir o original de
relatorios alterava retroativamente o saldo. As restricoes de periodos fechados
permanecem aplicadas.

## Verificacao

Os testes de regressao usam os servicos existentes, em vez de copiar suas regras
em funcoes artificiais. A suite foi reconstruida com `clean test` sobre a main
que inclui as correcoes de Vendas/Estoque. O resultado final fica na PR.

O cenario PostgreSQL descartavel foi ampliado para conferir, com repositories
reais: vencimento da parcela, contabilizacao do titulo, recusa de duplicidade,
estorno, saldo zero por conta no balancete e no razao, fechamento, bloqueio de
estorno em periodo fechado e reabertura. Todos os dados sinteticos ficam na
transacao de teste com rollback. Nenhuma migration historica foi alterada.

Esses cenarios possuem criterios objetivos de encerramento. Eles nao substituem
a validacao das integracoes externas Reinf, certificados ou a conciliacao
financeira/fiscal de devolucoes registrada no inventario. Nenhum desses fluxos
externos e declarado concluido por contagem de classes ou por testes com mocks.
