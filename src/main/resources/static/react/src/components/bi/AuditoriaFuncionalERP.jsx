import { useTranslation } from 'react-i18next';
import React, { useMemo, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Tag } from 'primereact/tag';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { ProgressBar } from 'primereact/progressbar';

const itens = [
 {modulo:'Arquitetura ERP',area:'Multiempresa e filiais',estado:'PARCIAL',faltam:'Hierarquia completa de empresas/filiais, consolidação, parametrização por estabelecimento, intercompany e rateios.'},
 {modulo:'Arquitetura ERP',area:'Workflow e aprovações',estado:'PARCIAL',faltam:'Motor genérico de workflow, alçadas, substitutos, SLA, aprovação multinível e trilha por processo.'},
 {modulo:'Arquitetura ERP',area:'Auditoria',estado:'PARCIAL',faltam:'Auditoria funcional completa por campo, antes/depois, justificativa, aprovação e histórico navegável por documento.'},
 {modulo:'Arquitetura ERP',area:'Integrações',estado:'PARCIAL',faltam:'Catálogo de integrações, filas/eventos efetivamente usados, retentativa, DLQ, monitoramento e reprocessamento operacional.'},
 {modulo:'Cadastro',area:'Cadastros mestres',estado:'PARCIAL',faltam:'Governança de dados mestres, duplicidade, versionamento, aprovação e manutenção em massa.'},
 {modulo:'Vendas',area:'Ciclo comercial',estado:'PARCIAL',faltam:'Orçamento → pedido → reserva → expedição → documento fiscal → entrega → financeiro em uma jornada única.'},
 {modulo:'Vendas',area:'Preços e descontos',estado:'PARCIAL',faltam:'Regras por cliente/grupo/região/canal, vigência, descontos por faixa, margem mínima e aprovação de exceções.'},
 {modulo:'Vendas',area:'Devoluções e pós-venda',estado:'PENDENTE',faltam:'Devolução, troca, bonificação, crédito, logística reversa e impacto integrado em estoque/fiscal/financeiro.'},
 {modulo:'Vendas',area:'CRM',estado:'PARCIAL',faltam:'Funil, oportunidades, atividades, tarefas, contatos, histórico comercial e conversão para proposta/pedido.'},
 {modulo:'Compras',area:'Procure-to-pay',estado:'PARCIAL',faltam:'Solicitação → cotação → aprovação → pedido → recebimento → conferência → documento fiscal → contas a pagar.'},
 {modulo:'Compras',area:'Fornecedores',estado:'PARCIAL',faltam:'Qualificação, homologação, score, documentos, validade, desempenho e bloqueios.'},
 {modulo:'Compras',area:'Contratos',estado:'PENDENTE',faltam:'Contratos de fornecimento, vigência, saldo, reajuste, consumo e renovação.'},
 {modulo:'Estoque/WMS',area:'Operação física',estado:'AVANÇADO',faltam:'Seriais, ondas de picking, regras de put-away, inventário cíclico avançado e produtividade operacional.'},
 {modulo:'Estoque/WMS',area:'Custos de estoque',estado:'PENDENTE',faltam:'Custo médio/PEPS/FIFO conforme parametrização, reavaliação, custo por lote e integração contábil.'},
 {modulo:'Estoque/WMS',area:'Planejamento',estado:'PARCIAL',faltam:'Ponto de pedido, estoque de segurança, lead time, lote econômico, cobertura e sugestão automática de compras.'},
 {modulo:'Financeiro',area:'Contas a receber/pagar',estado:'AVANÇADO',faltam:'Renegociação, antecipação, protesto, juros parametrizados, cobrança, régua e integração bancária completa.'},
 {modulo:'Financeiro',area:'Bancos',estado:'PARCIAL',faltam:'OFX/CNAB/PIX/boleto, retorno bancário, remessa, importação automática e conciliação assistida por regras.'},
 {modulo:'Financeiro',area:'Tesouraria',estado:'PARCIAL',faltam:'Fluxo de caixa projetado/realizado, aplicações, empréstimos, adiantamentos e posição financeira consolidada.'},
 {modulo:'Contábil',area:'Contabilidade',estado:'PARCIAL',faltam:'Partidas automáticas por evento, diário/razão, fechamento, períodos, centros de resultado, rateios e demonstrações.'},
 {modulo:'Contábil',area:'Ativo imobilizado',estado:'PENDENTE',faltam:'Aquisição, incorporação, depreciação, baixa, transferência, componentes e conciliação contábil.'},
 {modulo:'Fiscal',area:'Documentos fiscais',estado:'PARCIAL',faltam:'Emissão/entrada integrada, autorização, cancelamento, CC-e, inutilização, manifestação, contingência e retorno.'},
 {modulo:'Fiscal',area:'Obrigações',estado:'PENDENTE',faltam:'SPED e demais obrigações, validação, geração, transmissão, retorno, pendências e histórico.'},
 {modulo:'Fiscal',area:'Tributação',estado:'PARCIAL',faltam:'Motor tributário completo por operação/UF/regime/NCM/cliente, memória de cálculo e cenários de exceção.'},
 {modulo:'Fiscal',area:'Transição tributária',estado:'PENDENTE',faltam:'Parametrização e simulação para novos tributos, regras de vigência e impactos nos documentos.'},
 {modulo:'Produção/PCP',area:'BOM e produção',estado:'PARCIAL',faltam:'BOM versionada, roteiro, centros de trabalho, apontamento de tempo, perdas, coprodutos e subprodutos.'},
 {modulo:'Produção/PCP',area:'MRP',estado:'PENDENTE',faltam:'MRP completo com demanda, estoque, pedidos, lead time, lotes, capacidade e ordens sugeridas.'},
 {modulo:'Produção/PCP',area:'Custos industriais',estado:'PENDENTE',faltam:'Custo padrão/real, mão de obra, máquina, overhead, variações e custo por ordem.'},
 {modulo:'RH',area:'Pessoal',estado:'PARCIAL',faltam:'Admissão, férias, ponto, benefícios, afastamentos, rescisão, documentos e autosserviço.'},
 {modulo:'RH',area:'Folha',estado:'PARCIAL',faltam:'Cálculo abrangente, eventos, encargos, fechamento, integração bancária e obrigações trabalhistas.'},
 {modulo:'RH',area:'eSocial',estado:'PENDENTE',faltam:'Eventos, validação, transmissão, retorno, reprocessamento e monitoramento.'},
 {modulo:'Serviços',area:'Service desk/OS',estado:'PARCIAL',faltam:'SLA, contratos, agenda, técnicos, peças, custos, faturamento, portal e indicadores de atendimento.'},
 {modulo:'BI',area:'Analytics',estado:'PARCIAL',faltam:'Cobertura dos relatórios/agendamentos, filtros multidimensionais, drill-down, exportação e catálogo de métricas.'},
 {modulo:'BI',area:'Datalake',estado:'PARCIAL',faltam:'Pipelines operacionais, atualização incremental, qualidade, linhagem e monitoramento das cargas.'},
 {modulo:'IA',area:'Assistente',estado:'PARCIAL',faltam:'Configuração, prompts, templates, classificação, embeddings, previsões, análises e operações ainda não expostas no front.'},
 {modulo:'IA',area:'Previsões',estado:'PARCIAL',faltam:'Workspace para previsão de vendas/estoque/financeiro, histórico, confiança, recalculo e tratamento de pendências.'},
 {modulo:'Plataforma',area:'Segurança',estado:'PARCIAL',faltam:'Matriz de autorização mais granular, segregação de funções, políticas por operação e revisão de privilégios.'},
 {modulo:'Plataforma',area:'Relatórios',estado:'PARCIAL',faltam:'Catálogo único, filtros salvos, permissões, agendamento, distribuição, auditoria e parâmetros por empresa.'},
 {modulo:'Plataforma',area:'Operação em massa',estado:'PENDENTE',faltam:'Importação/exportação controlada, edição em massa, validação prévia, lote, rollback e relatório de erros.'},
 {modulo:'Plataforma',area:'Fechamentos',estado:'PENDENTE',faltam:'Fechamento mensal por módulo, bloqueio de períodos, reabertura autorizada e checklist de fechamento.'}
];

const sev = {AVANÇADO:'success', PARCIAL:'warning', PENDENTE:'danger'};
export default function AuditoriaFuncionalERP(){
    const { t } = useTranslation();
 const [modulo,setModulo]=useState(null);
 const [estado,setEstado]=useState(null);
 const [busca,setBusca]=useState('');
 const modulos=[...new Set(itens.map(x=>x.modulo))].map(x=>({label:x,value:x}));
 const estados=[...new Set(itens.map(x=>x.estado))].map(x=>({label:x,value:x}));
 const filtrados=useMemo(()=>itens.filter(x=>
   (!modulo||x.modulo===modulo)&&(!estado||x.estado===estado)&&
   (!busca||Object.values(x).join(' ').toLowerCase().includes(busca.toLowerCase()))
 ),[modulo,estado,busca]);
 const total=itens.length, advanced=itens.filter(x=>x.estado==='AVANÇADO').length, partial=itens.filter(x=>x.estado==='PARCIAL').length, pending=itens.filter(x=>x.estado==='PENDENTE').length;
 const cobertura=Math.round(((advanced*1+partial*.5)/(total))*100);
 return <div className="p-3">
  <Card title={t('legacyUi.audit.title')}>
   <p className="text-600">Mapa do que já existe no backend/frontend e do que ainda precisa ser implementado para alcançar cobertura funcional de um ERP empresarial completo. Não é uma certificação nem uma equivalência de produto.</p>
   <div className="grid">
    <div className="col-12 md:col-3"><Card title={t('legacyUi.audit.audited')}><strong className="text-3xl">{total}</strong></Card></div>
    <div className="col-12 md:col-3"><Card title={t('legacyUi.audit.advanced')}><strong className="text-3xl">{advanced}</strong></Card></div>
    <div className="col-12 md:col-3"><Card title={t('legacyUi.audit.partial')}><strong className="text-3xl">{partial}</strong></Card></div>
    <div className="col-12 md:col-3"><Card title={t('legacyUi.audit.pending')}><strong className="text-3xl">{pending}</strong></Card></div>
   </div>
   <div className="mb-4"><div className="flex justify-content-between"><span>Cobertura estimada da matriz</span><strong>{cobertura}%</strong></div><ProgressBar value={cobertura}/></div>
   <div className="flex flex-wrap gap-2 mb-3">
    <Dropdown value={modulo} options={modulos} onChange={e=>setModulo(e.value)} placeholder={t('legacyUi.audit.modules')} showClear filter/>
    <Dropdown value={estado} options={estados} onChange={e=>setEstado(e.value)} placeholder={t('legacyUi.audit.states')} showClear/>
    <span className="p-input-icon-left"><i className="pi pi-search"/><InputText value={busca} onChange={e=>setBusca(e.target.value)} placeholder={t('legacyUi.audit.search')}/></span>
   </div>
   <DataTable value={filtrados} paginator rows={15} stripedRows responsiveLayout="scroll" emptyMessage="Nenhum item encontrado">
    <Column field="modulo" header="Módulo" sortable/>
    <Column field="area" header="Área" sortable/>
    <Column field="estado" header={t('legacyUi.audit.status')} sortable body={r=><Tag value={r.estado} severity={sev[r.estado]}/>} />
    <Column field="faltam" header="O que falta" />
   </DataTable>
  </Card>
 </div>;
}
