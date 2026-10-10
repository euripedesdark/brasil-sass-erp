# Relatório fiel — o que falta para a paridade funcional (backend + frontend)

Data: 2026-10-10. Método: endpoints lidos no servidor hoje (`@Mapping` por
controller), corpo de services conferido por amostragem, 81 testes contados
por domínio, ~130 telas abertas no navegador com medição de contraste,
varredura de texto fixo (i18n) em todos os JSX. Dados de cópias locais
antigas foram descartados. Nomes genéricos; nada proprietário no frontend.

Legenda: OK = existe, lógica real e coberto por teste ou validado em tela.
PARCIAL = existe mas incompleto, sem teste ou sem validação. AUSENTE = sem
endpoint, stub ou só model. Entre colchetes o nível da evidência:
[server] lido hoje no servidor, [tela] validado no navegador, [suite]
coberto por teste, [estático] só inventário.

## Compras — quase completo
Backend [server]: requisição, cotação/mapa, pedido, aprovação/rejeição de
solicitação, recebimento total/parcial, devolução, conferência 3-way,
tolerância, bloqueio de baixa por divergência, cobertura total, rateio
multi-NF, contratos de fornecimento (CRUD + ativar/encerrar/liberar). Tudo
com lógica real. Falta: alçada por valor no pedido (só solicitação tem
aprovação) [server]; tolerância cadastrável (só por conferência) [server].
Testes: 9 arquivos [suite parcial].
Frontend [tela]: telas da conferência e do pedido 100% em 4 idiomas e sem
crash. Restam textos fixos em SupplyChainCompras (30), Contratos (21),
DevCompra (13) [estático].

## Estoque/WMS — completo no cadastro, parcial na prova
Backend [server]: saldos/ajustes, depósitos, endereços/ocupação,
lotes com quarentena/bloqueio/vencer-expirados, transferências (inclui
interna), inventário com contagens/fechamento, reservas com
liberar/separar/picking, ondas gerar/liberar/concluir, volumes/fechar,
putaway, expedição com conferência. Falta: FIFO/FEFO explícito [server];
prova de concorrência e expedição repetida (só 1 teste WMS, 5 estoque).
Frontend [tela]: sem crash, contraste ok. Restam textos fixos em WMS (36),
Transferências (21), Inventários (20), Lotes (14), Reservas (14),
Expedições (13), Endereços (9), Estoque (7) [estático].

## Vendas — parcial
Backend [server]: pedido, crédito, ATP por pedido, confirmar (reserva
multi-depósito), faturar (título + parcelas + baixa + comissão),
pós-venda, cancelar, previsão de tributação, tabelas de preço, devolução
com financeiro e contábil. Contratos e metas têm pacotes próprios.
Falta: backend dedicado de PDV (tela usa endpoints de pedido) [server];
backorder/promessa parcial [server]; estorno de comissão [server].
Testes: 7 arquivos [suite parcial].
Frontend [tela]: sem crash. Restam textos fixos em PDV (31), Devoluções
(20), Vendas (20), Tabelas (14), Metas (11), Contratos (vendas/contratos
não varrido p/ texto) [estático].

## Financeiro — amplo, com 3 faltas
Backend [server]: títulos com parcelas, baixa com juros/multa/desconto,
boletos com remessa/retorno CNAB, cobrança operacional
(carteira/ações/promessas), conciliação com automático, OFX, extrato,
fluxo de caixa, caixa, centros, contas, condições, tipos, comissões com
regras, empréstimos com quitar, orçamento com realizado, renegociação,
rentabilidade por cliente, Stripe checkout/invoice/webhook, crédito com
limite, aprovações de título. Falta: baixa automática por retorno CNAB
(só casa títulos) [server]; garantias [server]; aplicações só têm model
AplicacaoFinanceira sem controller [server]. Testes: 10 arquivos.
Frontend [tela]: sem crash. Restam textos fixos em Boletos (29),
Cobrança (28), Titulo (24), Conciliação (22), Extrato/Renegociação (17),
PlanoContas/CentroCusto (15/16), Condicao/Tipo (12/11), Comissões,
Crédito, Financeiro (7-9), Copa (12), Empréstimos (12), Orçamento (13)
[estático].

## Contabilidade — parcial (automático é a falta)
Backend [server]: lançamentos com partida dobrada/estorno, razão,
balancete, DRE, apuração do exercício, ECD gerar, motor de regras com
balancete/DRE/fechamento/rateio, gerar lançamento de título. Automático
só é chamado por devoluções, depreciação e ativos [server]; compra, venda,
baixa, estoque, produção e folha não invocam direto. Testes: 4 + 5.
Frontend [tela]: sem crash. Restam textos fixos em Contabilidade (47) e
Enterprise (16), LancamentoContabil (34) [estático].

## PCP/Produção — endpoints completos, profundidade a validar
Backend [server]: estrutura multinível, roteiros e centros, MPS
gerar/confirmar, MRP simular/gerar-sugestões, capacidade simular/carga/
agendar/calendário, ordens iniciar/finalizar, apontamentos com
estatísticas, OEE, custo por ordem, romaneios conferir/liberar. Falta:
MRP temporal (sem lead time, estoque de segurança ou lote mínimo no
código) [server]; terceirização/operação externa [server]; prova de
planejamento com múltiplos recursos. Testes: 6 arquivos.
Frontend [tela]: sem crash. Restam textos fixos em Producao (37),
Capacidade (36), Roteiros (36), Mrp (15), OEE (14), Romaneios (14),
Apontamentos (11), Estrutura (13), MPS (8) [estático].

## Qualidade — parcial, sem CAPA/instrumentos
Backend [server]: planos, inspeções com concluir, não-conformidades com
ações e encerrar. Falta: amostragem e instrumentos de medição, CAPA com
eficácia, bloqueio/liberação integrado a lote e ordem [server]. Teste: 1.
Frontend: Qualidade (36 fixos), tema escuro aplicado [tela/estático].

## PLM — completo e provado
Backend: ciclo mudança→aprovação→implementação com efeitos, revisões com
vigência exclusiva, permissões [suite: 8 testes + ciclo real]. Frontend:
tela 100% em 4 idiomas [tela].

## Ativos/Manutenção — completo e provado
Backend: aquisição, classes, depreciação mensal com contabilização,
baixa/adição/transferência/reavaliação/impairment, ordens com materiais e
horas, baixa de peças no estoque ao concluir com trava de saldo,
planos por tempo/contador, medições, MTBF/MTTR, preventivas [suite: 3
testes novos, 414 verdes]. Frontend: 3 telas 100% em 4 idiomas [tela].

## RH — amplo, faltam provisões e gestão de pessoas
Backend [server]: funcionários, cargos, ponto (bater/ajustar/falta),
folha com processar/importar-ponto/gerar-13º/cancelar, férias
programar/gozar/cancelar, rescisão calcular/efetivar, encargos, eSocial
com fila/transmitir (idempotente)/consultar, fotos. Folha processada gera
título a pagar. Falta: provisões de férias/13º [server]; benefícios,
recrutamento, treinamento, avaliação [server]. Testes: 3.
Frontend [tela]: sem crash. Restam textos fixos em Ponto (23), Esocial
(16), Ferias (16), RH (14), Rescisao (14), Folha (13), Encargos (5),
Cargo (4), FuncionarioFoto (8) [estático].

## CRM/Serviços/Projetos — parcial
Backend [server]: CRM com leads/etapas/gerar-pedido/pipeline/forecast/
atividades (sem campanhas); OS com itens/apontamentos/fechar/PDF (sem
recorrência/SLA contratual dedicado, há recurso genérico enterprise);
projetos com etapas/movimentos/riscos/mudanças/resumo (sem dependências
de cronograma). Testes: 0 nos três. Frontend: CRM (34), Servicos (19),
Projetos (44) fixos [estático].

## SupplyChain/Transportes — parcial
Backend [server]: demanda, ATP/calcular, planejamento, rotas, cargas com
tracking, fretes com conferência, planejamento de carga, GRC com
riscos/controles/vínculos/evidências/testes, TMS com ordens/fechamentos/
eventos/tracking/fretes. Falta: ATP com entradas futuras e por local
(só depósito-padrão, sem data) [server]; otimização de rotas e cubagem
[server]. Testes: 0 supplychain, 3 enterprise.
Frontend: ControlTower e Enterprise sem crash e com contraste ok [tela];
restam 23 fixos em cada [estático].

## DMS/Workflow/Portais/Integrações — parcial
Backend [server]: DMS com versões/download/aprovações/retenção (sem
assinatura); workflow com definições/etapas/instâncias/tarefas/decidir
(sem delegação/escalonamento); portais com acessos e minha-conta (sem
holerite/ponto/férias do funcionário); integrações com endpoints/
eventos/entregas/retry/webhook (consumidor e dead-letter sem prova).
Intercompany com reconciliar/eliminacoes/executar (cálculo a validar).
Testes: 0 dms/portais/integracoes, 1 workflow. Frontend: DMS (22),
Workflow (42), Portais (10+10), DocumentoFluxo (17+6) fixos [estático].

## BI/Cadastros/Segurança/Admin — base pronta
Backend [server]: BI com 6 controllers; cadastros (pessoas, clientes,
fornecedores, produtos, bancos, municípios) com CRUD; segurança com
@PreAuthorize por toda parte e tenant. Falta: drill-down/exportação com
prova, homologação e bloqueio propagado de fornecedor, duplicidade
[server]. Testes BI: 7. Frontend: BI (27), Relatorios (7+13+20+5),
Cadastros (2-13) fixos [estático].

## Fiscal — por último, como ordenado
Backend [server]: NCM/CEST/CFOP (consulta), entradas com importação e
análise de XML, NF-e emitir/cancelar/consultar (atrás de
sefaz.enabled, sem certificado de produção), NFS-e emitir/cancelar/
retornos, CT-e e MDF-e emitir/consultar/cancelar/encerrar, apuração com
calcular/encerrar/reabrir/transmitir, SPED gerar, Reinf gerar/fechar
(sem transmissão externa — documentado no código), obrigações com
agenda/entrega, regras tributárias CRUD, simulador, certificados,
SEFAZ status/consulta/distribuição, DIFAL e ICMS-ST calcular, painel e
prontidão. Faltas externas conhecidas e pendentes: NF automática da
devolução e transmissão externa do Reinf. Testes: 9.
Frontend [tela]: todas sem crash. Restam fixos em CteMdfe (30), NFe
(23), Nfse (22), Apuracoes (19), FiscalPainel (18), Obrigacoes (17),
EntradaNota/ImportarXml (10/16), e demais telas fiscais (3-11) [estático].

## Resumo executivo
- Backend tem endpoint real para ~90% das funções de referência; as
  faltas fiéis estão listadas acima por módulo (as maiores: MRP temporal,
  CAPA/instrumentos, provisões e gestão de pessoas no RH, FIFO, baixa
  CNAB automática, automático contábil amplo, PDV dedicado, campanhas,
  recorrência, assinatura, delegação, holerite no portal, ATP temporal,
  homologação, drill-down com prova; fiscal externo por último).
- Frontend: 0 crash em ~130 telas após 3 correções, tema escuro com
  contraste medido, datas por idioma, marca corrigida; i18n 100% em 6
  telas, restam 136 arquivos com 1882 ocorrências (lista por tela acima).
- Suite: 414 testes, zero falhas [suite].
