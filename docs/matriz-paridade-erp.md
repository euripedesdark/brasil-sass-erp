# Matriz de paridade funcional — Brasil SaaS ERP

Referência: `euripedesdark/brasil-sass-erp`, commit `3400d0f2efeb3a2be0a756d49520562001f4906d`, análise de código em 08/10/2026.
Escopo: processos empresariais dos dois relatórios fornecidos. Fiscal por último; IA fora da paridade. SAP é uma referência de abrangência, não uma especificação de produto/versão certificada.


## Correções locais posteriores à análise

- `src/main/resources/static/react/src/App.jsx`: corrigido JSX das rotas contabilidade e contabilidade/enterprise. Build Vite passou; smoke frontend passou 5 checks.
- Migration `V171__compras_3way_match_item.sql` renomeada para `V172__compras_3way_match_item.sql`, deixando a criação do cabeçalho e tabela detalhada em V171. V172 acrescenta explicitamente as cinco colunas da outra definição, pois CREATE TABLE IF NOT EXISTS não modifica tabela existente.
- Verificadas 59 versões únicas. SQL de V171 + V172 executado em PostgreSQL 18 temporário com tabelas de referência mínimas; repetição de V172 passou e as dez colunas representativas dos dois layouts foram confirmadas. Isso não valida o conjunto completo de migrations nem o boot do ERP.
- Maven test foi tentado novamente: resolução de artefatos Spring AI ainda retorna HTTP 403 de repo.spring.io; testes Java não executados.
- Antes de aplicar em banco existente, verificar flyway_schema_history: se a antiga V171 de 3-way já foi aplicada isoladamente, esta renumeração exige uma estratégia específica de compatibilidade. Não executar repair ou alterar checksums automaticamente.

### Bloqueio de baixa por conferência de compra

`TituloServiceImpl.baixar` consulta conferências vigentes do título, diretamente ou pelo pedido da mesma empresa. Bloqueia qualquer status diferente de APROVADA antes de persistir baixa ou mover saldo. A consulta considera a última avaliação de cada documento/recebimento; a aprovação de outro documento não substitui uma divergência. Títulos sem conferência mantêm o comportamento anterior: não foi imposta conferência obrigatória a todos os títulos nesta entrega.

Criado `TituloConferenciaCompraTest` com cinco cenários: três estados bloqueados, baixa aprovada e título sem conferência. Execução Maven tentada, mas interrompida na resolução Spring AI (HTTP 403), antes da compilação/testes. Consulta JPQL e testes permanecem sem validação em execução. Concorrência entre criação de conferência e pagamento, aprovação de exceção e obrigatoriedade de conferência ainda precisam de entrega específica; este bloqueio não conclui Procure-to-Pay.

### Conferência por item: linhas repetidas e compilação

Corrigido o agrupamento por produto e preço unitário: quantidades e valores de linhas equivalentes são somados em cópias, sem sobrescrever linhas nem reutilizar uma linha fiscal em dois grupos do pedido. Preços exatos são confrontados antes de preços divergentes; linhas fiscais não consumidas, inclusive sem produto, ficam divergentes. Grupos agregados não recebem ID de item original único. Esta entrega não trata rateio de recebimento parcial entre múltiplas NFs.

`ConferenciaFaturaCompraItem` foi alinhada com os campos já utilizados pelo serviço (número, descrição, totais, conformidade e tipo de divergência). A listagem passou a usar método existente declarado no repositório com filtro de exclusão lógica. O status da linha acompanha a conformidade.

Validação executada com fontes reais: 41 arquivos de dependências próprias compilados com javac/JDK 21 e Lombok; JUnit executou 15 testes, todos aprovados (10 de conferência, incluindo cinco regressões novas; 5 de bloqueio de baixa). Sem stubs de aplicação ou mudanças de assertivas para fazer passar. Isso valida as duas classes selecionadas e não o build completo, a consulta JPQL em banco, a concorrência ou o boot. O build Maven completo continua dependente da resolução do Spring AI. Log local: `/workspace/.tools/erp-focused-tests.log`.

### Vínculo financeiro e serialização da conferência

A conferência resolve automaticamente o título do pedido, rejeita troca por outro título, exige contas a pagar e chama `TituloRepository.findForUpdate`, a mesma trava pessimista usada na baixa. Recusa nova avaliação quando o título já está cancelado, baixado ou com saldo menor que o original por pagamento parcial. O título resolvido é persistido na conferência mesmo quando omitido no request. Isso ordena conferência e baixa nas transações que usam esses serviços; outros caminhos de pagamento ainda exigem auditoria.

Novos testes: `ConferenciaPagamentoCompraTest` (cinco cenários, incluindo fluxo aprovado e ordem da chamada de trava) e `ConferenciaVigenteQueryTest` (três cenários que executam a JPQL real com Hibernate/H2). A consulta foi validada para reavaliação de um documento sem liberar outro, exclusão lógica e restrição por empresa/título. A execução final compilou 43 arquivos reais e passou 23 testes, zero ignorados. Não foi realizado teste concorrente com duas transações PostgreSQL nem validação do ERP completo. Não há endpoint de aprovação excepcional de divergência nesta entrega.

A matriz abaixo registra o diagnóstico do commit de referência, anterior a essas correções locais.

## Integração com o main atualizado

As correções foram integradas ao commit remoto `9ee2e5e7da578bfe82c3d81dc2ccdb8001a415f6`, incluindo as entregas do Devin em ativos/manutenção e mapeamentos enterprise. A correção do controller, a entidade e V172 do main foram preservadas. A V172 local concorrente foi removida; V173 completa somente status e tolerância e normaliza status a partir de conforme, sem modificar migrations já publicadas.

A tela ConferenciaFaturasCompra usava estados não declarados (`detalhe`/`itens`); esses estados foram implementados, os erros de carregamento tratados e o envio bloqueado para formulário inválido/em processamento. A API valida IDs positivos, recebimento/NF obrigatórios, valor positivo e tolerância não negativa.

Validação nesta integração: 30 testes Java selecionados (conferência, baixa, vínculo financeiro, JPQL/H2 e HTTP MockMvc); build Vite; cinco checks smoke; regressão de renderização React; SQL V171/V172/V173 e repetição de V173 em PostgreSQL 18 com tabelas de referência mínimas. As contagens finais constam do log `/workspace/.tools/erp-focused-tests.log`. Os testes HTTP exercitam roteamento, validação e delegação com principal controlado; não cobrem toda a cadeia de autenticação/autorização. Build completo Maven e boot continuam sem validação devido à resolução do Spring AI.

## Entrega: conferência de recebimento parcial

Base: main `2510a647`, após incorporação do PR #40. O valor informado da fatura é confrontado com o total registrado na NF-e e com o recebimento escolhido; o pedido completo fica como referência e limite superior do recebido. Assim, uma entrega de R$ 40 de um pedido de R$ 150 pode ser aprovada, sem exigir faturar R$ 150.

Itens do pedido ainda não entregues e não faturados são excluídos desta conferência. Itens efetivamente recebidos sem fatura continuam divergentes; itens faturados sem recebimento e produtos recebidos sem correspondência no pedido também são divergentes. Conferência sem itens ou NF sem total não é aprovada. A tolerância monetária do total fiscal foi validada no limite e acima dele; regras de tolerância de preço por item continuam fora desta etapa.

Validação: 46 arquivos próprios compilados; 40 testes selecionados executados e aprovados, zero ignorados, incluindo dez novas regressões. Novo `RecebimentoParcialConferenciaTest`; testes anteriores de baixa/JPQL/HTTP permanecem aprovados. Não houve mudança de migration ou frontend nesta etapa. Não foi validado o build completo/boot. O confronto ainda exige que a NF represente o recebimento selecionado; rateio de um recebimento entre várias NFs, consumo acumulado e obrigatoriedade de conferência de todos os documentos antes de pagamento continuam pendentes.

## Como interpretar

- **Parcial**: há implementação concreta, mas integração/regra/teste do ciclo ainda não foi comprovado.
- **Lacuna confirmada no caminho**: o método inspecionado deixa de executar uma parte necessária descrita na linha; o escopo da conclusão é esse caminho.
- **Bloqueado**: defeito ou pré-requisito impede o check indicado.
- **Não verificável**: evidência disponível não permite decidir o ciclo. Não significa ausente.
- **Completo** exige processo operacional, persistência, regras, tela, permissões/tenant, integração, auditoria e teste ponta a ponta aprovado. Nenhuma linha recebeu esse status com a evidência atual.

Esta é uma matriz por famílias de processos, com requisitos de aceite na coluna de pendências; não uma contagem exaustiva de todos os requisitos SAP. Presença de arquivo/tela/teste não significa funcionamento. Testes citados como existentes não foram executados nesta análise. Ausência de teste significa ausência identificada em `src/test/java`, não em todo possível sistema externo de testes. Evidências estáticas e resultados do onboarding são separados.

## Matriz

| Prioridade | Domínio | Processo / requisito | Status | Código existente / evidência funcional | Falta implementar ou validar / aceite | Evidência | Tela existente | Testes / validação |
|---|---|---|---|---|---|---|---|---|
| P0 | Infraestrutura | Inicialização Flyway | Bloqueado | Duas migrations V171 no mesmo diretório. | Renumerar em tarefa de código; validar migração em banco novo e atualização em banco existente. | MIG | — | Nenhuma execução Flyway nesta análise |
| P0 | Frontend | Carregamento da aplicação | Bloqueado | Rota contabilidade com JSX malformado. | Corrigir fechamento das rotas e executar build e navegação autenticada. | APP | App.jsx | Build e HTTP /src/App.jsx falharam no onboarding |
| P0 | Backend | Compilação e testes | Não verificável | Java 21/Maven preparados; download Spring AI bloqueado por HTTP 403. | Aplicar rede para repo.spring.io e jitpack.io; executar Maven test. | — | — | Testes Java não executados |
| P1 | Compras | Solicitação → aprovação → cotação → pedido | Parcial | Serviço de aprovação, mapa comparativo e geração de pedido existe. | Validar alçadas, rejeição, fornecedor bloqueado, repetição da geração e fluxo na tela. | SC | compras/SupplyChainCompras.jsx | Sem teste dedicado de ciclo identificado |
| P1 | Compras | Recebimento parcial/múltiplo → estoque | Parcial | Acumula quantidade recebida; controla saldo e status PARCIAL/RECEBIDO; registra entrada. | Validar concorrência, múltiplos recebimentos, devolução e rastreabilidade por lote. | COMP | compras/Compras.jsx | Sem teste dedicado de recebimento identificado |
| P1 | Compras | Conferência 3-way por item | Parcial | Compara pedido, recebimento e NF; persiste divergências de preço/quantidade e item não pedido. | Mapas por produto sobrescrevem linhas repetidas; testar produto repetido, múltiplas NFs e recebimentos acumulados. | MATCH,TESTMATCH | compras/ConferenciaFaturasCompra.jsx | Teste específico existente, não executado |
| P1 | Compras | Tolerância da conferência | Parcial | Tolerância monetária aplicada aos totais; linhas com preço/quantidade diferente são divergentes. | Definir tolerância por item, unidade, percentual e regra fiscal; testar limites. | MATCH | compras/ConferenciaFaturasCompra.jsx | Sem comprovação de política por item |
| P1 | Compras | Divergência → bloqueio AP → aprovação → pagamento | Lacuna confirmada no caminho | Conferência grava status; receberParcial cria título ao completar recebimento. | Ligar resultado da conferência à liberação do título: conferir não atualiza título e baixa não consulta conferência. | MATCH,COMP,TIT | compras/ConferenciaFaturasCompra.jsx | Exigir teste de tentativa de pagamento com divergência |
| P1 | Vendas | Pedido → crédito → reserva | Parcial | Valida limite contra títulos abertos e reserva saldo com trava; cancela reservas. | Validar atraso, margem, desconto, backorder e promessa parcial; não reimplementar reserva existente. | VEN | vendas/Vendas.jsx | CalculoDescontoTest existe; ciclo não comprovado |
| P1 | Vendas | Faturamento → AR → comissão → devolução | Parcial | Serviço contém títulos, comissões por regra/faixa e baixa de estoque; DevolucaoController existe. | Testar faturamento repetido, parcelas, devolução parcial, crédito e estorno de comissão. | VEN | vendas/Vendas.jsx; vendas/Devolucoes.jsx | Sem teste dedicado de ciclo identificado |
| P1 | WMS | Reserva → onda → picking → packing → expedição | Parcial | Onda de reservas, separação, volumes, conferência e consumo de reserva implementados. | Testar quantidades, expedição repetida, concorrência e efeito real sobre saldo e venda. | WMS | wms/WMS.jsx | Sem teste dedicado WMS identificado |
| P1 | Estoque | Depósito/endereço/lote/inventário/transferência | Parcial | Controllers específicos e telas correspondentes existem. | Validar série, quarentena, avaria, capacidade, FIFO/FEFO e inventário com movimentos concorrentes; cadastro não prova algoritmo operacional. | WMS | estoque/InventariosEstoque.jsx; estoque/LotesEstoque.jsx | Sem teste dedicado de estoque identificado |
| P1 | ATP | Disponibilidade atual e promessa futura | Lacuna confirmada no cálculo | ATP subtrai reservas de estoque atual; grava CTP com mesmo valor. | Incluir entradas futuras de compra/produção e consumo por data/local: consulta atual não usa data nem local para calcular disponibilidade. | ATP | enterprise/SupplyChainControlTower.jsx | Exigir teste com compra futura e dois depósitos |
| P1 | Demanda | Forecast → consenso → planejamento | Parcial | API grava demanda, confiança, origem e parâmetros de planejamento. | Comprovar cálculo de previsão, sazonalidade, cenários e geração para MPS/MRP; gravar previsão manual não demonstra algoritmo. | ATP | enterprise/SupplyChainControlTower.jsx | Sem teste dedicado de previsão identificado |
| P1 | PCP/MRP | BOM multinível → necessidade líquida → compra/OP | Parcial | Explode BOM; desconta semiacabados; evita uso duplo do estoque; detecta ciclos; gera solicitações e OPs. | Cobrir lead time, reservas, recebimentos futuros, lotes mínimos/múltiplos e estoque de segurança em planejamento temporal. | MRP | producao/Producao.jsx | MrpServiceImplTest existente, não executado |
| P1 | PCP | MPS → capacidade → calendário → sequenciamento | Parcial | MpsService, calendário e agendamento de ordem/carga por centro existem. | Validar plano com múltiplos recursos, turnos, restrições e replanejamento; integração MPS/MRP precisa teste. | CAP | producao/Producao.jsx | CapacidadeServiceImplTest existente, não executado |
| P1 | Produção | Apontamento → consumo → qualidade → estoque | Parcial | Serviços de OP/apontamento e API OEE existem. | Demonstrar consumo real, refugo, retrabalho, subproduto, terceirização e bloqueio de qualidade no fechamento. | MRP,CUSTO | producao/ApontamentosProducao.jsx | Sem teste ponta a ponta identificado |
| P1 | Custos | Material + mão de obra → custo de OP | Parcial | Custo calcula materiais, horas por funcionário, produção/refugo e alertas de custo ausente. | Validar overhead, absorção, variação padrão/real, custo de subproduto e contabilização; cálculo atual não comprova esses processos. | CUSTO | producao/Producao.jsx | CustoProducaoServiceImplTest existente, não executado |
| P1 | Financeiro | Parcelas → baixa parcial → banco | Parcial | Baixa valida saldo, parcelas, juros/multa/desconto e conta da empresa; impede pendente de aprovação. | Validar fórmula financeira e efeitos sobre extrato/saldo com teste transacional; integrar divergência de compras. | TIT | financeiro (inventário abaixo) | Sem teste dedicado de baixa identificado |
| P1 | Tesouraria | OFX → conciliação → fluxo → empréstimo | Parcial | Controllers OFX, conciliação automática, fluxo projetado e empréstimos existem. | Demonstrar conciliação ambígua, pagamento em lote, garantias, aplicações e fechamento bancário. | TIT | financeiro (inventário abaixo) | Sem teste dedicado de tesouraria identificado |
| P1 | Contabilidade | Partidas → razão → balancete → DRE/balanço | Parcial | Valida débito/crédito; possui estorno, razão, balancete, DRE e balanço. | Reconciliar relatórios com partidas e plano de contas usando cenários reais; existem caminhos ctb e financeiro/enterprise distintos. | CTB,CTBE | contabil/Contabilidade.jsx; contabil/ContabilidadeEnterprise.jsx | Sem teste dedicado de contabilidade identificado |
| P1 | Contabilidade | Evento operacional → regra → lançamento automático | Lacuna confirmada no caminho | gerarDeTitulo é operação explícita do controller; regras enterprise são cadastradas via JDBC. | Integrar compra, venda, baixa, estoque e OP ao motor de regras. Busca por gerarDeTitulo/ContabilidadeService não encontrou invocação nos serviços desses módulos. | CTB,CTBE | contabil/ContabilidadeEnterprise.jsx | Exigir teste de evento → lançamento único |
| P1 | Contabilidade | Fechamento → bloqueio → reabertura | Parcial | exigirAberto protege operações ctb; fechar e reabrir existem; enterprise fecha outra estrutura. | Unificar efeito dos dois caminhos de fechamento; comprovar checklist, alçada, motivo e trilha de reabertura. | CTB,CTBE | contabil/Contabilidade.jsx | Sem teste de fechamento identificado |
| P1 | Controladoria | Orçamento → realizado → desvio → forecast | Parcial | OrcamentoService disponibiliza lançamento realizado e acompanhamento. | Comprovar integração automática por centro/produto/cliente, forecast e rentabilidade; evitar dupla entrada manual. | ORC | financeiro/Orcamento.jsx | Sem teste dedicado identificado |
| P1 | CRM | Lead → atividade → forecast → pedido | Parcial | Etapas, pipeline, forecast, atividades e gerarPedido implementados. | Validar conversão repetida, vínculo da oportunidade, contrato e pós-venda. | CRM | crm/CRM.jsx | Sem teste dedicado CRM identificado |
| P1 | PLM | Revisão → ECO → aprovação → efeitos | Parcial | Controller contém workflow, efeitos, aprovação e implementação em revisão/estrutura/roteiro/documento. | Verificar vigência de revisão consumida por PCP, histórico e idempotência; PLM vai além de CRUD nesta versão. | PLM | enterprise/SupplyChainEnterprise.jsx | Sem teste dedicado PLM identificado |
| P1 | Qualidade | Plano → inspeção → NC → CAPA → liberação | Parcial | Plano/inspeção/NC com validação de empresa existem. | Demonstrar amostragem, características/instrumentos, CAPA e bloqueio/liberação integrado ao lote e OP. | QUAL | qualidade/Qualidade.jsx | Sem teste dedicado de qualidade identificado |
| P1 | Ativos | Aquisição → depreciação → baixa | Parcial | AtivosController possui baixa e depreciar; indicadores de manutenção existem. | Testar capitalização, transferência, impairment, reavaliação e reflexo contábil. | ATIVO | ativos/Ativos.jsx | Sem teste dedicado de ativos identificado |
| P1 | Manutenção | Preventiva → OS → peça/hora → custo | Parcial | Manutenções e concluir existem; IndicadoresService gera preventivas. | Validar periodicidade, estoque de peças, compra, parada, MTBF/MTTR e título/lançamento. | ATIVO | ativos/IndicadoresAtivos.jsx | Sem teste dedicado de manutenção identificado |
| P1 | RH | Ponto → folha → férias/13º/rescisão → financeiro | Parcial | Processamento de folha e serviços de ponto, encargos, férias, 13º e rescisão existem. | Demonstrar fechamento por competência, provisões, benefícios e integração AP/contábil; transmissão eSocial fica no bloco Fiscal. | RH | rh/FolhaPagamento.jsx | Sem teste dedicado de folha identificado |
| P1 | BPM | Instância → tarefas → decisão → próxima etapa | Parcial | Tarefas com SLA; aprovação avança etapa e rejeição encerra instância. | Método decidir não verifica userId contra responsável; validar autorização efetiva, delegação, escalonamento e segregação no caminho completo. | WKF | workflow/Workflow.jsx | Sem teste dedicado workflow identificado |
| P1 | DMS | Documento → versão → aprovação → retenção | Parcial | Versões, conteúdo/download, decisão de aprovação e retenção existem. | Validar assinatura, documento obrigatório e vínculos com processos; controle de acesso por versão precisa teste. | DMS | dms/DMS.jsx | Sem teste dedicado DMS identificado |
| P1 | GRC/EHS | Risco → controle/evidência → avaliação → ação | Parcial | GRC tem testes/evidências; EHS tem ação, inspeção e permissão de trabalho com transições. | Comprovar eficácia do controle, segregação, vínculo operacional e auditoria de encerramento. | GRC,PLM | enterprise/RiskTransportEnterprise.jsx; enterprise/SupplyChainEnterprise.jsx | Sem teste dedicado GRC/EHS identificado |
| P1 | Portais | Acesso cliente/fornecedor/funcionário | Parcial | Token expira/revoga; minhaConta retorna títulos/pedidos filtrados por pessoa e tipo. | minhaConta não entrega holerite/ponto/férias para FUNCIONARIO; validar pagamento/documentos e acessos de vendedor/transportadora. | PORT | portais/PortalPublico.jsx | Sem teste dedicado de portal identificado |
| P1 | Integrações | Evento → outbox → entrega → retry | Lacuna confirmada no caminho | Publicar insere evento e entrega PENDENTE; retry altera status/data; webhook compara hash de segredo recebido em header. | Busca bc_int_delivery só encontrou controller; implementar/comprovar consumidor que envia, backoff, dead-letter e idempotência. Registro PENDENTE não prova entrega HTTP. | INT | enterprise/IntegrationEnterprise.jsx | Sem teste dedicado de entrega identificado |
| P1 | Estrutura empresarial | Empresa/filial/local/organização/hierarquia | Parcial | EnterpriseOperationsController mantém catálogo de recursos e períodos. | Validar consistência entre filial, centro, depósito e organização nas operações de compra/venda/produção. | ORG | enterprise/EnterpriseOperations.jsx | Sem teste dedicado de estrutura identificado |
| P1 | Cadastros mestres | Cadastro → aprovação → uso → histórico | Não verificável no ciclo | Controllers e serviços próprios de pessoa, cliente, fornecedor e produto existem. | Auditar duplicidade, homologação e bloqueio propagado; cadastro por si não comprova governança de dados. | ORG | cadastros existentes | Ciclo de aprovação não demonstrado nesta análise |
| P1 | Preços | Tabela → condição → prioridade → pedido | Parcial | TabelaPrecoController e tela TabelasPreco existem. | Comprovar combinação de desconto/acréscimo/frete/comissão, vigência, prioridade e aprovação de exceção no pedido. | PRECO,VEN | vendas/TabelasPreco.jsx | CalculoDescontoTest existe; motor de condições não comprovado |
| P1 | Transportes | Rota → carga → tracking → entrega → frete | Parcial | APIs de rota/carga/tracking/frete e ordens/paradas/entregas existem. | Validar otimização, cubagem, POD e vínculo do frete ao AP/AR e pedido; gravar rota não prova roteirização. | ATP,GRC | enterprise/RiskTransportEnterprise.jsx; enterprise/SupplyChainControlTower.jsx | Sem teste dedicado TMS identificado |
| P1 | Intercompany | Documento bilateral → reconciliação | Lacuna confirmada no caminho | CorporateController.reconciliar apenas marca registro reconciliado/status. | Gerar e conciliar contrapartida entre empresas, diferença cambial e auditoria; atual método não compara documentos. | CORP | enterprise/CorporateGovernance.jsx | Exigir teste com duas empresas e divergência |
| P1 | Consolidação | Saldos → conversão → eliminações → demonstrações | Lacuna confirmada no caminho | CorporateController.executar apenas altera status e campos de conclusão. | Implementar cálculo de saldos, moeda, eliminações e demonstração consolidada; não considerar atualização de status consolidação financeira. | CORP | enterprise/CorporateGovernance.jsx | Exigir teste de eliminação intercompany |
| P1 | Segurança | Tenant/RBAC → segregação → auditoria | Parcial | Resolver de tenant, filtro de módulo, autoridades e testes específicos existem. | Validar JDBC/raw SQL, portal público, referências cruzadas e segregação por aprovação. Preservar arquitetura de tenant. | TEN,PLM,WKF | transversal | Testes tenant/security/identity presentes, não executados |
| P2 | BI | Indicador → relatório → drill-down/exportação | Não verificável no ciclo | Há BI.jsx, RelatoriosBI.jsx e serviços próprios de report/relatório/dashboard/KPI. | Inventariar métodos/rotas reais e consumo das telas antes de repetir números de endpoints sem tela; validar filtros e exportação. | — | bi/BI.jsx; bi/RelatoriosBI.jsx | CoberturaFrontendBackendTest não equivale a validação de KPIs |
| P2 | Projetos | EAP → custo → progresso → faturamento | Parcial | ProjetoServiceImpl possui etapas, movimentos, riscos, mudanças e faturar/resumo. | Demonstrar dependências de cronograma, recursos/horas, compras, margem e contabilização. | PROJ | projetos/Projetos.jsx | Sem teste dedicado projetos identificado |
| P2 | Serviços | Contrato/SLA → OS → peças/horas → fechamento | Parcial | OS tem item, apontamento e fechamento; contrato é recurso enterprise. | Validar recorrência, SLA, garantia, técnico e integração contrato → OS → financeiro. | SERV,PLM | OrdemServico.jsx; enterprise/SupplyChainEnterprise.jsx | Sem teste dedicado serviço identificado |
| Último | Fiscal/compliance | NF/tributos/SPED/eSocial | Não verificável nesta etapa | Código fiscal existente foi preservado; não foram chamadas integrações fiscais. | Auditar por último o ciclo de documento, apuração, retenção e obrigações; não presumir defeito nem paridade. | — | fora da validação inicial | Nenhuma execução fiscal |

## Bloqueios e divergências em relação aos relatórios recebidos

1. No commit analisado, a duplicidade encontrada é **V171**, não V155–V158. Os arquivos são `V171__compras_conferencia_fatura_item.sql` e `V171__compras_3way_match_item.sql`. Não foi verificado o merge do PR #31 pela API GitHub; a conclusão vem dos arquivos locais.
2. Existe conferência de compra por item e um teste específico. Não há evidência para dizer que a comparação por item inteira está ausente. O bloqueio de pagamento por divergência não está ligado nesse caminho.
3. Reserva na venda, recebimento parcial, MRP multinível, capacidade e custo de OP já têm implementações. Fechamento/reabertura contábil também existem. Devem ser integrados e testados, não recriados sem análise.
4. PLM/EHS possuem transições e efeitos além de cadastros. A profundidade e o consumo operacional desses efeitos precisam ser comprovados.
5. Consolidar e reconciliar no controller corporativo apenas atualizam status. Não produzem, nesses métodos, o cálculo financeiro que os nomes sugerem.
6. Os números antigos de 82 endpoints sem tela, 18 de BI, seis classes de teste e percentuais de código de terceiros não foram reproduzidos. Não devem orientar entrega como fatos confirmados. A lista atual de testes está abaixo.

## Validação em execução

Resultados observados durante o onboarding deste mesmo checkout: `npm ci` na raiz e frontend passou; `npm test` frontend passou cinco checks estáticos. `npm run build -- --outDir /workspace/.cache/brasil-sass-erp/frontend-dist` falhou no JSX de App.jsx; `/src/App.jsx` respondeu HTTP 500 no Vite. Maven falhou na resolução do Spring AI por HTTP 403 antes de compilar. Isso não valida nenhum processo empresarial Java. Não foram executadas migrações nem testes com banco nesta análise.

## Ordem de execução das entregas

P0 (JSX, unicidade Flyway, rede/dependências) → Compras → WMS → Vendas → Financeiro/Tesouraria → Contabilidade → PCP/MRP/Custos → Qualidade → PLM → Ativos/Manutenção → RH → CRM → Controladoria → GRC/EHS → DMS/BPM → Portais → Integrações → Intercompany/Governança → BI → Projetos/Serviços → Fiscal.
Estrutura, cadastros, preços, ATP, demanda, transportes e segurança entram como requisitos das entregas que os consomem. Cada entrega deve incluir cenário feliz, rejeição/divergência, acesso de outra empresa, repetição/idempotência e efeitos persistidos. Nenhuma mudança de aplicação foi feita para este documento.

## Evidências de código

Links relativos ao repositório; `#L` informa o ponto inicial no GitHub. O identificador na matriz aponta para estas referências.

- **COMP**: [PedidoCompraServiceImpl.java](../src/main/java/br/com/brasil_saas/compras/service/impl/PedidoCompraServiceImpl.java#L142) — `receberParcial`.
- **MATCH**: [ConferenciaFaturaCompraServiceImpl.java](../src/main/java/br/com/brasil_saas/compras/service/impl/ConferenciaFaturaCompraServiceImpl.java#L131) — `boolean totaisOk`.
- **SC**: [ComprasSupplyChainServiceImpl.java](../src/main/java/br/com/brasil_saas/compras/service/impl/ComprasSupplyChainServiceImpl.java#L27) — `aprovarSolicitacao`.
- **VEN**: [PedidoVendaServiceImpl.java](../src/main/java/br/com/brasil_saas/vendas/service/impl/PedidoVendaServiceImpl.java#L185) — `validarCredito`.
- **WMS**: [WmsServiceImpl.java](../src/main/java/br/com/brasil_saas/wms/service/impl/WmsServiceImpl.java#L208) — `finalizarExpedicao`.
- **ATP**: [SupplyChainEnterpriseController.java](../src/main/java/br/com/brasil_saas/supplychain/controller/SupplyChainEnterpriseController.java#L19) — `double atp=`.
- **MRP**: [MrpServiceImpl.java](../src/main/java/br/com/brasil_saas/producao/service/impl/MrpServiceImpl.java#L74) — `gerarSugestoes`.
- **CAP**: [CapacidadeServiceImpl.java](../src/main/java/br/com/brasil_saas/producao/service/impl/CapacidadeServiceImpl.java#L137) — `agendarOrdem`.
- **CUSTO**: [CustoProducaoServiceImpl.java](../src/main/java/br/com/brasil_saas/producao/service/impl/CustoProducaoServiceImpl.java#L37) — `Resultado calcular`.
- **TIT**: [TituloServiceImpl.java](../src/main/java/br/com/brasil_saas/financeiro/service/impl/TituloServiceImpl.java#L114) — `PENDENTE_APROVACAO`.
- **CTB**: [ContabilidadeServiceImpl.java](../src/main/java/br/com/brasil_saas/contabilidade/service/impl/ContabilidadeServiceImpl.java#L178) — `gerarDeTitulo`.
- **CTBE**: [ContabilidadeEnterpriseController.java](../src/main/java/br/com/brasil_saas/contabil/controller/ContabilidadeEnterpriseController.java#L13) — `/regras`.
- **CORP**: [CorporateController.java](../src/main/java/br/com/brasil_saas/enterprise/controller/CorporateController.java#L31) — `executar(`.
- **PLM**: [SupplyChainEnterpriseController.java](../src/main/java/br/com/brasil_saas/enterprise/controller/SupplyChainEnterpriseController.java#L123) — `implementarMudanca`.
- **QUAL**: [QualidadeController.java](../src/main/java/br/com/brasil_saas/qualidade/controller/QualidadeController.java#L45) — `criarInspecao`.
- **ATIVO**: [AtivosController.java](../src/main/java/br/com/brasil_saas/ativos/controller/AtivosController.java#L73) — `depreciar(`.
- **RH**: [FolhaPagamentoServiceImpl.java](../src/main/java/br/com/brasil_saas/rh/service/impl/FolhaPagamentoServiceImpl.java#L160) — `processar(`.
- **CRM**: [CrmServiceImpl.java](../src/main/java/br/com/brasil_saas/crm/service/impl/CrmServiceImpl.java#L99) — `gerarPedido`.
- **DMS**: [DmsServiceImpl.java](../src/main/java/br/com/brasil_saas/dms/service/impl/DmsServiceImpl.java#L46) — `novaVersao`.
- **WKF**: [WorkflowServiceImpl.java](../src/main/java/br/com/brasil_saas/workflow/service/impl/WorkflowServiceImpl.java#L90) — `decidir(`.
- **PORT**: [PortalServiceImpl.java](../src/main/java/br/com/brasil_saas/portais/service/impl/PortalServiceImpl.java#L74) — `minhaConta`.
- **INT**: [IntegrationEnterpriseController.java](../src/main/java/br/com/brasil_saas/enterprise/controller/IntegrationEnterpriseController.java#L37) — `publicar(`.
- **GRC**: [RiskTransportEnterpriseController.java](../src/main/java/br/com/brasil_saas/enterprise/controller/RiskTransportEnterpriseController.java#L108) — `testes(`.
- **ORG**: [EnterpriseOperationsController.java](../src/main/java/br/com/brasil_saas/enterprise/controller/EnterpriseOperationsController.java#L20) — `TABLES`.
- **ORC**: [OrcamentoService.java](../src/main/java/br/com/brasil_saas/financeiro/service/OrcamentoService.java#L47) — `acompanhamento`.
- **PROJ**: [ProjetoServiceImpl.java](../src/main/java/br/com/brasil_saas/projetos/service/impl/ProjetoServiceImpl.java#L173) — `faturar(`.
- **SERV**: [OrdemServicoServiceImpl.java](../src/main/java/br/com/brasil_saas/servicos/service/impl/OrdemServicoServiceImpl.java#L58) — `fechar(`.
- **PRECO**: [TabelaPrecoController.java](../src/main/java/br/com/brasil_saas/vendas/controller/TabelaPrecoController.java#L27) — `class TabelaPrecoController`.
- **TEN**: [EmpresaTenantIdentifierResolver.java](../src/main/java/br/com/brasil_saas/shared/tenant/EmpresaTenantIdentifierResolver.java#L94) — `resolveCurrentTenantIdentifier`.
- **APP**: [App.jsx](../src/main/resources/static/react/src/App.jsx#L182) — `path="contabilidade"`.
- **TESTMATCH**: [ConferenciaFaturaCompraServiceImplTest.java](../src/test/java/br/com/brasil_saas/compras/service/impl/ConferenciaFaturaCompraServiceImplTest.java#L20) — `class Conferencia`.
- **MIG**: [migration de conferência](../src/main/resources/db/migration/V171__compras_conferencia_fatura_item.sql) e [migration de 3-way](../src/main/resources/db/migration/V172__compras_3way_match_complemento.sql).

## Inventário de testes Java próprios

- [CoberturaFrontendBackendTest.java](../src/test/java/br/com/brasil_saas/CoberturaFrontendBackendTest.java).
- [TesteGeralSistema.java](../src/test/java/br/com/brasil_saas/TesteGeralSistema.java).
- [TesteModelosSimples.java](../src/test/java/br/com/brasil_saas/TesteModelosSimples.java).
- [ConferenciaFaturaCompraServiceImplTest.java](../src/test/java/br/com/brasil_saas/compras/service/impl/ConferenciaFaturaCompraServiceImplTest.java).
- [CapacidadeServiceImplTest.java](../src/test/java/br/com/brasil_saas/producao/service/CapacidadeServiceImplTest.java).
- [CustoProducaoServiceImplTest.java](../src/test/java/br/com/brasil_saas/producao/service/CustoProducaoServiceImplTest.java).
- [MrpServiceImplTest.java](../src/test/java/br/com/brasil_saas/producao/service/MrpServiceImplTest.java).
- [IdentityServiceTest.java](../src/test/java/br/com/brasil_saas/shared/identity/IdentityServiceTest.java).
- [IdentityServiceTratamentoDeErroTest.java](../src/test/java/br/com/brasil_saas/shared/identity/IdentityServiceTratamentoDeErroTest.java).
- [CustomUserDetailsServiceTest.java](../src/test/java/br/com/brasil_saas/shared/security/CustomUserDetailsServiceTest.java).
- [ModuloAcessoFilterTest.java](../src/test/java/br/com/brasil_saas/shared/security/ModuloAcessoFilterTest.java).
- [ConfiguracaoIsolamentoPorEmpresaTest.java](../src/test/java/br/com/brasil_saas/shared/tenant/ConfiguracaoIsolamentoPorEmpresaTest.java).
- [EmpresaTenantIdentifierResolverTest.java](../src/test/java/br/com/brasil_saas/shared/tenant/EmpresaTenantIdentifierResolverTest.java).
- [CalculoDescontoTest.java](../src/test/java/br/com/brasil_saas/vendas/service/CalculoDescontoTest.java).

## Inventário complementar de telas financeiras

- [AprovacoesTitulos.jsx](../src/main/resources/static/react/src/components/financeiro/AprovacoesTitulos.jsx).
- [Boletos.jsx](../src/main/resources/static/react/src/components/financeiro/Boletos.jsx).
- [CentroCusto.jsx](../src/main/resources/static/react/src/components/financeiro/CentroCusto.jsx).
- [Cobranca.jsx](../src/main/resources/static/react/src/components/financeiro/Cobranca.jsx).
- [Comissoes.jsx](../src/main/resources/static/react/src/components/financeiro/Comissoes.jsx).
- [ConciliacaoBancaria.jsx](../src/main/resources/static/react/src/components/financeiro/ConciliacaoBancaria.jsx).
- [CondicaoPagamento.jsx](../src/main/resources/static/react/src/components/financeiro/CondicaoPagamento.jsx).
- [ContaBancaria.jsx](../src/main/resources/static/react/src/components/financeiro/ContaBancaria.jsx).
- [Credito.jsx](../src/main/resources/static/react/src/components/financeiro/Credito.jsx).
- [Emprestimos.jsx](../src/main/resources/static/react/src/components/financeiro/Emprestimos.jsx).
- [Extrato.jsx](../src/main/resources/static/react/src/components/financeiro/Extrato.jsx).
- [Financeiro.jsx](../src/main/resources/static/react/src/components/financeiro/Financeiro.jsx).
- [FinanceiroHub.jsx](../src/main/resources/static/react/src/components/financeiro/FinanceiroHub.jsx).
- [FluxoCaixa.jsx](../src/main/resources/static/react/src/components/financeiro/FluxoCaixa.jsx).
- [LancamentoContabil.jsx](../src/main/resources/static/react/src/components/financeiro/LancamentoContabil.jsx).
- [Orcamento.jsx](../src/main/resources/static/react/src/components/financeiro/Orcamento.jsx).
- [PlanoContas.jsx](../src/main/resources/static/react/src/components/financeiro/PlanoContas.jsx).
- [RegrasComissao.jsx](../src/main/resources/static/react/src/components/financeiro/RegrasComissao.jsx).
- [Renegociacao.jsx](../src/main/resources/static/react/src/components/financeiro/Renegociacao.jsx).
- [TipoPagamento.jsx](../src/main/resources/static/react/src/components/financeiro/TipoPagamento.jsx).
- [Titulo.jsx](../src/main/resources/static/react/src/components/financeiro/Titulo.jsx).


## Entrega — encadeamento automatico devolucao para financeiro (venda e compra)

Branch codex/devolucao-integracao-financeira. Sem migration, sem tela nova, sem alteracao de contrato existente.

- DevolucaoService.receber (venda): apos o recebimento fisico, calcula o
  valor devolvido proporcional aos itens do pedido (valorTotal por produto,
  rateando desconto da linha, HALF_UP em 2 casas) e, com a mesma trava
  pessimista do titulo via TituloService.baixar:
  - titulo ABERTO/PARCIAL com saldo: cria Baixa de ajuste sem conta
    bancaria (sem movimentacao de caixa), rateando parcelas;
  - titulo BAIXADO/saldo zerado: cria titulo P a restituir ao cliente;
  - sem titulo ou titulo CANCELADO: so registra o fluxo, sem financeiro.
- DevCompraService.devolver (compra): simetrico. Titulo P ABERTO/PARCIAL
  tem a Baixa de ajuste (bloqueio por conferencia pendente preservado, via
  baixar); titulo BAIXADO gera titulo R de credito junto ao fornecedor.
- Idempotencia: confere Baixa/titulo de restituicao existentes pelo marcador
  (DEVOLUCAO_VENDA id, DEVOLUCAO_COMPRA id) antes de criar; o recebimento
  continua bloqueando reexecucao por status com trava pessimista.
- Trilha documental: liga DEVOLUCAO_VENDA e DEVOLUCAO_COMPRA a
  TITULO e PEDIDO em documento_fluxo em todos os caminhos.
- Nao emite documento fiscal nem contabiliza automaticamente: o titulo/Baixa
  gerados seguem o caminho existente (gerarDeTitulo) e a NF de devolucao
  permanece pendencia explicita (Fiscal por ultimo, conforme objetivo).

Arquivos da entrega:

    src/main/java/br/com/brasil_saas/vendas/devolucao/DevolucaoService.java
    src/main/java/br/com/brasil_saas/compras/service/DevCompraService.java
    src/main/java/br/com/brasil_saas/financeiro/repository/TituloRepository.java
    src/test/java/br/com/brasil_saas/vendas/devolucao/DevolucaoServiceTest.java
    src/test/java/br/com/brasil_saas/compras/service/DevCompraServiceTest.java
    src/test/java/br/com/brasil_saas/database/DevolucoesCreditoPostgresScenario.java
    docs/matriz-paridade-erp.md

Validacao: 28 casos nos dois servicos de devolucao (12 compra + 16 venda,
incluindo 7 novos de integracao) e 62 no bloco vizinho
(conferencia, baixa, fluxo, pedido, desconto), zero falhas, JDK 21, fora do
build Maven completo. Pendente: ajuste contabil automatico (requer plano de
contas por empresa), emissao fiscal automatica da devolucao e transmissao
externa do Reinf.

## Entrega — rateio de recebimento entre varias NFs com consumo acumulado

Branch codex/devolucao-integracao-financeira (mesmo bloco). Sem migration, sem tela nova.

- Antes, cada NF e cada recebimento so podiam ser consumidos por um par
  aprovado: a segunda NF do mesmo recebimento caia em DIVERGENTE.
- Agora o recebimento pode ser faturado por varias NFs. O consumo acumulado
  e a soma do valor e das quantidades faturadas das conferencias APROVADAS
  anteriores do mesmo recebimento (outras NFs); a reavaliacao do mesmo par
  substitui a anterior e nao entra na soma.
- Regras novas em conferir: a NF precisa caber na parte ainda nao consumida
  do recebimento (valor); por produto, o faturado nao pode passar do
  recebido menos o ja faturado (tipo novo CONSUMO_ACUMULADO_EXCEDIDO).
- A mesma NF com outro recebimento continua bloqueada: uma NF pertence a
  um unico recebimento.
- Em linha: faturado menor que recebido passa a ser parcial valida; o teto
  e dado pelo consumo acumulado, nao pela igualdade. Faturado acima do
  recebido continua divergente, assim como preco divergente e item nao pedido.
- A cobertura total antes do pagamento segue pendente (proximo passo
  natural deste encadeamento).

Arquivos da entrega:

    src/main/java/br/com/brasil_saas/compras/service/impl/ConferenciaFaturaCompraServiceImpl.java
    src/main/java/br/com/brasil_saas/compras/repository/ConferenciaFaturaCompraRepository.java
    src/test/java/br/com/brasil_saas/compras/service/impl/RecebimentoRateioMultiNfTest.java
    src/test/java/br/com/brasil_saas/compras/service/impl/RecebimentoParcialConferenciaTest.java
    docs/matriz-paridade-erp.md

Validacao: 5 casos novos de rateio (primeira NF aprova, segunda aprova
dentro do acumulado, terceira acima diverge, mesma NF em outro recebimento
bloqueia, reavaliacao do mesmo par nao soma em duplicata) e 1 caso antigo
atualizado para a nova regra (consumo total bloqueia, NF reutilizada
bloqueia). Bloco compras: 33 casos sem falha. Suite completa: 368 testes,
zero falhas, zero erros, JDK 21, incluindo contexto Spring real.

## Entrega — cobertura total antes do pagamento (compra)

Branch codex/devolucao-integracao-financeira (mesmo bloco). Sem migration, sem tela nova.

- Antes, a baixa bloqueava titulo com conferencia vigente pendente ou
  divergente, mas um titulo com recebimento ou NF-e nunca conferidos
  passava livre.
- Agora, havendo contexto de compra (pedidos vinculados ao titulo ou
  conferencias vigentes), o pagamento exige: todo recebimento coberto por
  conferencia APROVADA vigente, na quantidade por produto; toda NF-e
  autorizada do pedido coberta por conferencia APROVADA vigente.
- Titulos sem nenhum documento de compra seguem liberados, como antes;
  pedido sem recebimento, NF-e e conferencia nao tem o que exigir
  (adiantamento e servico preservados).
- Mensagens dizem o documento faltante (recebimento, produto com recebido
  e conferido, NF-e), sem alterar o bloqueio anterior.

Arquivos da entrega:

    src/main/java/br/com/brasil_saas/financeiro/service/impl/TituloServiceImpl.java
    src/main/java/br/com/brasil_saas/compras/repository/PedidoCompraRepository.java
    src/main/java/br/com/brasil_saas/fiscal/repository/NfeRepository.java
    src/test/java/br/com/brasil_saas/financeiro/service/impl/TituloCoberturaTotalCompraTest.java
    src/test/java/br/com/brasil_saas/financeiro/service/impl/TituloConferenciaCompraTest.java
    src/test/java/br/com/brasil_saas/financeiro/service/impl/TituloBaixaRateioParcelasTest.java
    src/test/java/br/com/brasil_saas/database/DevolucoesCreditoPostgresScenario.java
    src/test/java/br/com/brasil_saas/database/VendasReservasPostgresScenario.java
    docs/matriz-paridade-erp.md

Validacao: 5 casos novos de cobertura (recebimento sem conferencia,
parcialmente conferido, NF autorizada sem conferencia, cobertura total
libera, sem contexto libera) e 11 preservados do bloco financeiro.
Suite completa: 373 testes, zero falhas, zero erros, JDK 21, incluindo
contexto Spring real; package valido.

## Entrega — tolerancia de preco por item

Branch codex/devolucao-integracao-financeira (mesmo bloco). Sem migration, sem tela nova.

- O campo de tolerancia por item (V173) existia na entidade mas nunca era
  preenchido nem usado: toda diferenca de preco caia em PRECO_DIVERGENTE.
- Agora cada linha recebe a tolerancia informada e a divergencia de preco
  dentro do limite e absolvida (quantidade continua exata: so preco usa
  tolerancia). Com tolerancia zero, o comportamento e identico ao anterior.
- Sem efeito sobre rateio, consumo acumulado, pagamento ou reavaliacao.

Arquivos da entrega:

    src/main/java/br/com/brasil_saas/compras/service/impl/ConferenciaFaturaCompraServiceImpl.java
    src/test/java/br/com/brasil_saas/compras/service/impl/RecebimentoParcialConferenciaTest.java
    docs/matriz-paridade-erp.md

Validacao: 2 casos novos (preco dentro da tolerancia aprova e grava o
campo na linha; preco acima diverge com o tipo preservado). Suite
completa: 375 testes, zero falhas, zero erros, JDK 21.

## Entrega — apuracao do resultado do exercicio

Branch codex/financeiro-ciclo-titulos. Sem migration, sem tela nova: painel na aba Fechamentos.

- Antes, nao existia encerramento: receitas e despesas nunca eram zeradas
  contra lucros acumulados.
- Agora apurarResultado zera as contas de resultado (codigo 3, mesmo
  criterio da DRE) contra a conta informada, em 31/12, com origem
  ENCERRAMENTO. Exige jan-nov fechados e dezembro aberto (recebe o
  lancamento), recusa exercicio futuro, conta de resultado como destino,
  rascunho no ano, ausencia de movimento e reaplicacao.
- Tela: exercicio, conta de lucros e botao Apurar com confirmacao; erros
  voltam em toast sem gravar nada.

Arquivos da entrega:

    src/main/java/br/com/brasil_saas/contabilidade/service/ContabilidadeService.java
    src/main/java/br/com/brasil_saas/contabilidade/service/impl/ContabilidadeServiceImpl.java
    src/main/java/br/com/brasil_saas/contabilidade/controller/ContabilidadeController.java
    src/test/java/br/com/brasil_saas/contabilidade/service/impl/ContabilidadeApuracaoTest.java
    src/main/resources/static/react/src/components/contabil/Contabilidade.jsx
    docs/matriz-paridade-erp.md

Validacao: 7 casos novos (lucro, prejuizo, sem jan-nov, dezembro fechado,
ja apurado, futuro e conta invalida, sem movimento). Suite: 382 testes.
