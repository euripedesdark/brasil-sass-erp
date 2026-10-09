# Evidencias dos modulos na main — 9 de outubro de 2026

Base inspecionada: `694fdb3da551cd9e8f0089d4f50986af07d0d39b`.

Contagem de arquivos Java proprios por pacote; testes fora do pacote nao entram nesta coluna. Interfaces e implementacoes sao arquivos distintos. Referencia SQL nao prova que uma migration pode ser aplicada nem que uma integracao externa funciona. Nenhum numero representa percentual de conclusao.

| Modulo | Java | Entidades | Controllers | Services anotados | Arquivos de teste no pacote | Migrations que referenciam tabelas |
|---|---:|---:|---:|---:|---:|---:|
| Fiscal | 127 | 32 | 24 | 29 | 8 | 8 |
| Financeiro | 120 | 38 | 21 | 15 | 8 | 6 |
| Cadastro | 119 | 21 | 12 | 12 | 0 | 2 |
| Producao | 52 | 11 | 10 | 8 | 6 | 7 |
| Compras | 48 | 15 | 6 | 5 | 6 | 7 |
| Estoque | 34 | 12 | 9 | 0 | 4 | 1 |
| Vendas | 27 | 7 | 3 | 4 | 5 | 4 |
| Contabilidade | 11 | 3 | 2 | 2 | 1 | 3 |
| WMS | 11 | 4 | 1 | 1 | 1 | 2 |

Os testes globais, contratos e pacotes transversais, como enterprise, podem validar estes modulos. Ausencia de teste dentro de um pacote nao comprova ausencia de testes ou funcionalidade.

## Fiscal

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/fiscal/controller/IcmsStControllerTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/ApuracaoServiceTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/DifalServiceTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/FiscalProntidaoServiceTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/IcmsStServiceTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/ReinfServiceTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/TributacaoSimuladorServiceTest.java`
- `src/test/java/br/com/brasil_saas/fiscal/service/TributacaoSimularItensTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V109__busca_inteligente_fiscal.sql`
- `src/main/resources/db/migration/V147__fiscal_obrigacoes.sql`
- `src/main/resources/db/migration/V149__obrigacao_competencia_varchar.sql`
- `src/main/resources/db/migration/V156__nfse_identificadores_nacionais.sql`
- `src/main/resources/db/migration/V172__compras_3way_match_complemento.sql`
- `src/main/resources/db/migration/V178__reinf_payload_e_protocolo.sql`
- `src/main/resources/db/migration/V180__fiscal_regra_mva_fcp_interna.sql`

## Financeiro

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/financeiro/CaixaMovimentoRulesTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/CobrancaOperacionalRulesTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/CopaRulesTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/CreditoLimiteRulesTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/service/CmvCalculadoraTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/service/StripeFinanceServiceListTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/service/impl/TituloBaixaRateioParcelasTest.java`
- `src/test/java/br/com/brasil_saas/financeiro/service/impl/TituloConferenciaCompraTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V116__contabilidade.sql`
- `src/main/resources/db/migration/V157__stripe_financeiro.sql`
- `src/main/resources/db/migration/V174__caixa_movimento.sql`
- `src/main/resources/db/migration/V176__cobranca_acoes_promessas.sql`
- `src/main/resources/db/migration/V185__contratos_auditoria_colunas_legadas.sql`

## Cadastro

Testes encontrados no pacote:

Sem arquivo de teste neste pacote; conferir testes globais e transversais.

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V155__nfse_codigo_tributacao_nacional.sql`

## Producao

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/producao/ApontamentoIniciaOpRulesTest.java`
- `src/test/java/br/com/brasil_saas/producao/ProducaoStatusRulesTest.java`
- `src/test/java/br/com/brasil_saas/producao/RomaneioStatusRulesTest.java`
- `src/test/java/br/com/brasil_saas/producao/service/CapacidadeServiceImplTest.java`
- `src/test/java/br/com/brasil_saas/producao/service/CustoProducaoServiceImplTest.java`
- `src/test/java/br/com/brasil_saas/producao/service/MrpServiceImplTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V130__pcp_roteiros_centros_trabalho.sql`
- `src/main/resources/db/migration/V131__pcp_alocacao_capacidade.sql`
- `src/main/resources/db/migration/V132__pcp_audit_cols.sql`
- `src/main/resources/db/migration/V135__pcp_mps.sql`
- `src/main/resources/db/migration/V137__mps_periodo_varchar.sql`
- `src/main/resources/db/migration/V151__apontamento_operacao.sql`

## Compras

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/compras/controller/ConferenciaFaturaCompraHttpTest.java`
- `src/test/java/br/com/brasil_saas/compras/repository/ConferenciaVigenteQueryTest.java`
- `src/test/java/br/com/brasil_saas/compras/service/ContratoFornecimentoServiceTest.java`
- `src/test/java/br/com/brasil_saas/compras/service/impl/ConferenciaFaturaCompraServiceImplTest.java`
- `src/test/java/br/com/brasil_saas/compras/service/impl/ConferenciaPagamentoCompraTest.java`
- `src/test/java/br/com/brasil_saas/compras/service/impl/RecebimentoParcialConferenciaTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V154__compra_devolucao.sql`
- `src/main/resources/db/migration/V171__compras_conferencia_fatura_item.sql`
- `src/main/resources/db/migration/V172__compras_3way_match_complemento.sql`
- `src/main/resources/db/migration/V173__compras_status_tolerancia_conferencia.sql`
- `src/main/resources/db/migration/V179__compras_contrato_fornecimento.sql`
- `src/main/resources/db/migration/V185__contratos_auditoria_colunas_legadas.sql`

## Estoque

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/estoque/TransferenciaItensRulesTest.java`
- `src/test/java/br/com/brasil_saas/estoque/controller/InventarioCancelarRulesTest.java`
- `src/test/java/br/com/brasil_saas/estoque/controller/LoteStatusRulesTest.java`
- `src/test/java/br/com/brasil_saas/estoque/controller/ReservaCancelarRulesTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`

## Vendas

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/vendas/AtpRulesTest.java`
- `src/test/java/br/com/brasil_saas/vendas/devolucao/DevolucaoServiceTest.java`
- `src/test/java/br/com/brasil_saas/vendas/service/CalculoDescontoTest.java`
- `src/test/java/br/com/brasil_saas/vendas/service/PedidoTributacaoServiceTest.java`
- `src/test/java/br/com/brasil_saas/vendas/service/impl/PedidoVendaServiceImplTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V139__vendas_devolucao.sql`
- `src/main/resources/db/migration/V141__vendas_devolucao_audit.sql`
- `src/main/resources/db/migration/V142__vendas_devolucao_cols.sql`

## Contabilidade

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/contabilidade/service/impl/ContabilidadeServiceImplTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V116__contabilidade.sql`
- `src/main/resources/db/migration/V118__contabilidade_periodo_varchar.sql`

## WMS

Testes encontrados no pacote:

- `src/test/java/br/com/brasil_saas/wms/service/impl/WmsServiceImplTest.java`

Migrations com referencia a tabelas do modulo:

- `src/main/resources/db/migration/B184__bootstrap_erp_novo.sql`
- `src/main/resources/db/migration/V121__wms.sql`
