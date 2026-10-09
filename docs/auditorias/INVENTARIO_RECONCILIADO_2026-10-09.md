# Inventário reconciliado: preservar o que já foi entregue

Conferência de 09/10/2026. Base main: `c898a5dfa65561dfe5f1d2c2c3353137abc23983`, incluindo PR #122. Auditoria estática do código atual, rotas, chamadas e histórico incorporado; não altera aplicação, telas, migrations ou dados.

## Correção da classificação anterior

A lista anterior misturava funcionalidades entregues, automações ainda não conectadas e requisitos externos. Isso não permite declarar módulos inteiros incompletos. A ausência de um método em um serviço também não permite ignorar caminhos alternativos.

As PRs #12, #13, #14, #15, #16, #38, #44, #46, #48, #75, #94, #113, #114, #117, #118 e #121 foram conferidas como ancestrais da main atual. Foram lidos o escopo das PRs relevantes e os caminhos existentes. Documentos antigos de roadmap não são fonte de backlog independente.

## Recursos entregues: não recriar

| Área | Histórico incorporado | Evidência na main |
|---|---|---|
| Compras e conferência 3-way | #12, #33, #40–#42 | Pedidos, recebimentos, supply-chain e conferência de faturas em /api/compras |
| Vendas e devoluções | #13, #44, #48, #101, #113, #118, #121 | DevolucaoService; tela Devolucoes.jsx; rota vendas/devolucoes; saldo devolvível e recebimento |
| Devolução de compra | #118 | DevCompraService; DevCompra.jsx; rota compras/devolucoes; saída física e proteção de reservas |
| Estoque e WMS | #13, #43, #47, #52, #113 | /api/wms; reservas, picking, packing e expedição; não criar outra tela de expedição |
| Produção, PCP e planejamento | #1–#3, #5, #14, #15 | /api/producao/mrp, capacidade, ordens e apontamentos |
| Qualidade | #13, #15, #54 | /api/qualidade; inspeções e não conformidades |
| Financeiro e crédito | #14, #29, #30, #45, #60, #114, #118 | TituloServiceImpl.baixar/gerarParcelas; CreditoService; StripeFinanceServiceImpl; telas atuais |
| Contabilidade | #9, #14, #46, #114 | Gerar por título, lançar, estornar, razão, balancete e fechamento/reabertura; Contabilidade.jsx |
| Ativos e manutenção | #38, #39 | AtivoContabilService e /api/ativos; contabilização e estorno de depreciação existentes |
| RH, projetos, BI e workflow | #6, #7, #14, #15, #63, #71 | Controllers e rotas próprios; não voltar ao roadmap que os trata como módulos ausentes |
| Fiscal NF-e/NFS-e/CT-e/MDF-e | #8, #11, #16 | Implementações e telas de emissão/consulta/eventos existentes |
| Reinf local | #75, #117 | ReinfService, ReinfController, Reinf.jsx e rota fiscal/reinf: geração, detalhe e fechamento locais |
| Contratos e demais expansões | #72, #78, #93, #94, #122 | Contratos, agenda, helpdesk, metas, notificações e conhecimento entregues |
| IAM, Samba/CA, login e Stripe local | Confirmação do proprietário e PRs de integração | Resolvidos no ambiente informado; fora das pendências desta etapa |

A tabela confirma entrega e existência do recurso. Não afirma certificação funcional de todos os cenários de cada módulo nem homologação externa integral.

## Duas ausências delimitadas nos caminhos examinados

### Integração automática da devolução com financeiro, contabilidade e documento fiscal

Solicitar/aprovar/receber em DevolucaoService e solicitar/devolver/cancelar em DevCompraService realizam o fluxo físico. Não chamam ajuste de título, estorno contábil ou emissão fiscal automática. A tela de vendas chama esses mesmos endpoints; a de compras informa explicitamente que devolver baixa estoque.

Os recursos financeiros e contábeis existem separadamente. Estornar e gerarDeTitulo são operações reais de ContabilidadeServiceImpl, expostas pela tela atual. Foram também conferidos ContabilidadeEnterpriseController (outro contrato, via JdbcTemplate), TituloServiceImpl, StripeFinanceServiceImpl e referências aos serviços de devolução. Não foi encontrado um caminho alternativo que conecte automaticamente esses atos ao recebimento da devolução.

O NFeXmlBuilder usado pela aplicação define finNFe=1 no caminho de emissão por pedido. A biblioteca fiscal vendorizada contém finalidade de devolução e modelos de imposto devolvido; isso comprova capacidade da biblioteca, não ligação automática ao processo comercial do ERP.

A PR #44 já assinalava estorno de título/comissão como pendente; #48 entregava a tela reaproveitada e não alterava fiscal. Portanto não falta tela de devolução nem módulo de contabilidade: a ausência delimitada é o encadeamento automático. Não cancelar uma venda inteira para atender uma devolução parcial nem inventar regra de restituição.

### Transmissão externa Reinf

A PR #75 declara expressamente EFD-Reinf local, sem transmissão RFB. A implementação atual, o controller e a própria tela mantêm esse contrato. ReinfController possui listar/detalhe/gerar/fechar, não transmissão. O payload informa fechamento local. Não foi encontrada implementação Reinf alternativa no código próprio ou microserviços examinados.

Geração e fechamento não são pendências: já foram entregues. Transmissão é extensão ao contrato local da #75, não uma razão para recriar tela ou classificar todo o Fiscal como incompleto.

## Critério de preservação

1. Não criar telas substitutas, apagar versões pelo nome ou unificar contratos distintos sem demonstrar necessidade.
2. Não repetir blocos já incorporados por PRs antigas.
3. Qualquer alteração funcional futura precisa de um caso concreto, rastreio completo dos caminhos existentes e teste de regressão do contrato preservado.
4. Não alterar migrations históricas, certificados, autenticação ou dados para fazer a auditoria.
5. Restrições ambientais informadas como resolvidas pelo proprietário não entram no backlog.
6. Sem uma nova falha reproduzida ou evolução deliberadamente especificada, o resultado desta conferência não autoriza uma nova rodada de construção de módulos.

Nenhum teste de execução foi repetido nesta auditoria, pois não houve alteração funcional. As validações anteriores de 355 casos, PostgreSQL e Docker pertencem às entregas #118/#121; não são usadas como prova universal de equivalência ao SAP.
