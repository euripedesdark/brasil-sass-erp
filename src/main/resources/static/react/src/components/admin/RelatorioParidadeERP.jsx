import { useTranslation } from 'react-i18next';
import React, { useMemo, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Tag } from 'primereact/tag';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Accordion, AccordionTab } from 'primereact/accordion';
import { ProgressBar } from 'primereact/progressbar';
import { Divider } from 'primereact/divider';

const areas = [
 {area:'Cadastros e dados mestres',status:'PARCIAL',coverage:78,priority:'ALTA',done:'Pessoas, clientes, fornecedores, produtos, categorias, marcas, serviços, unidades, transportadoras, municípios e imagens.',missing:'Governança de dados mestres, deduplicação, versionamento, múltiplos endereços/funções e aprovação em massa.'},
 {area:'Vendas / Comercial',status:'PARCIAL',coverage:78,priority:'CRÍTICA',done:'Pedidos, orçamento, preços, comissões, PDV, devoluções e integração com estoque/financeiro.',missing:'Contratos comerciais avançados, política de desconto/margem, metas por território/canal, recorrência e fulfillment ponta a ponta.'},
 {area:'Compras / Sourcing',status:'PARCIAL',coverage:86,priority:'ALTA',done:'Solicitação/cotação/pedido/recebimento, conferência, supply chain e devolução.',missing:'Sourcing estratégico e homologação aprofundada. Contratos de fornecimento e liberação de pedido já entregues.'},
 {area:'Estoque / WMS',status:'AVANÇADO',coverage:88,priority:'ALTA',done:'Depósitos, endereços, lotes, reservas, inventários, picking/expedição, transferências e saldos.',missing:'Serialização, ondas, put-away automático, FEFO/FIFO configurável e control tower logística.'},
 {area:'Produção / PCP',status:'AVANÇADO',coverage:82,priority:'CRÍTICA',done:'OP, BOM/estrutura, roteiros, capacidade, MRP, MPS, OEE, apontamentos e custos.',missing:'APS avançado, sequenciamento com restrições, engenharia de mudanças integrada e custeio industrial completo.'},
 {area:'Financeiro / Tesouraria',status:'AVANÇADO',coverage:84,priority:'CRÍTICA',done:'Títulos, baixa, bancos, OFX, caixa, conciliação, crédito, cobrança, orçamento e tesouraria projetada.',missing:'CNAB/bancos em escala, aplicações, risco financeiro, cash pooling e fechamento operacional mais profundo.'},
 {area:'Contabilidade / Close',status:'AVANÇADO',coverage:82,priority:'CRÍTICA',done:'Plano de contas, partidas dobradas, centros de custo, períodos, regras de lançamento, validação, rateios, balancete, DRE e fechamento.',missing:'Automação contábil por todos os documentos, consolidação legal completa e integrações contábeis externas.'},
 {area:'Fiscal Brasil',status:'AVANÇADO',coverage:75,priority:'CRÍTICA',done:'NF-e real condicionada à SEFAZ/certificado, NFS-e, NCM/CFOP/CEST/ISSQN, entrada, SPED, CT-e/MDF-e e certificado.',missing:'Homologação operacional por UF/município, NFC-e completa, eventos/contingência e transmissão efetiva de todos os documentos.'},
 {area:'RH / Folha / eSocial',status:'PARCIAL',coverage:58,priority:'ALTA',done:'Funcionários, cargos, folha, ponto, férias, rescisão, encargos e fila eSocial.',missing:'Motor CLT completo, benefícios, afastamentos, fechamento eSocial ponta a ponta e autosserviço.'},
 {area:'Ativos / Manutenção',status:'AVANÇADO',coverage:78,priority:'MÉDIA',done:'Ativos, depreciação/indicadores e manutenção.',missing:'Planejamento preventivo avançado, ordens, peças, calibração e ciclo completo de manutenção.'},
 {area:'Qualidade',status:'AVANÇADO',coverage:72,priority:'MÉDIA',done:'Planos, inspeções e não conformidades.',missing:'Amostragem estatística, instrumentos, CAPA/8D, auditorias e integração completa com fornecedores/produção.'},
 {area:'CRM / Customer Experience',status:'PARCIAL',coverage:65,priority:'ALTA',done:'Leads, funil e geração de pedido.',missing:'Campanhas, atividades completas, segmentação, forecast, contratos comerciais e customer success.'},
 {area:'Serviços',status:'PARCIAL',coverage:65,priority:'ALTA',done:'Catálogo, ordens de serviço e contratos de serviço adicionados.',missing:'SLA/agenda/técnicos/peças/faturamento/portal e service parts completos.'},
 {area:'Projetos / Professional Services',status:'AVANÇADO',coverage:78,priority:'MÉDIA',done:'Projetos, etapas, custos/receitas, riscos, mudanças e faturamento por marco.',missing:'Portfólio, capacidade, planejamento de recursos e integração financeira mais profunda.'},
 {area:'Supply Chain / Transporte',status:'AVANÇADO',coverage:78,priority:'ALTA',done:'Reposição, políticas, ordens de transporte, demanda, planejamento, ATP/CTP, rotas, cargas, tracking e conferência de frete.',missing:'APS avançado com restrições, otimização de rotas e settlement/EDI de transportadoras em escala.'},
 {area:'Engenharia / PLM',status:'PARCIAL',coverage:52,priority:'ALTA',done:'Revisões de produto e mudanças de engenharia adicionadas; BOM/roteiros existentes na produção.',missing:'ECM completo, documentos, aprovação, efeito de mudança e lifecycle integrado.'},
 {area:'EHS',status:'BÁSICO',coverage:35,priority:'MÉDIA',done:'Registro de ocorrências, severidade, causa, ação corretiva e encerramento modelados.',missing:'Riscos, inspeções, permissões, ambiente, incidentes avançados e indicadores regulatórios.'},
 {area:'DMS / Workflow / Portais',status:'AVANÇADO',coverage:78,priority:'ALTA',done:'DMS com versões/aprovação, workflow, portais e auditoria.',missing:'Assinatura/retensão avançada, BPM mais rico e governança transversal de documentos.'},
 {area:'BI / Analytics',status:'PARCIAL',coverage:48,priority:'CRÍTICA',done:'BI, KPIs, relatórios, agendamento e auditoria funcional.',missing:'DW/DataLake dimensional, ETL incremental, catálogo de métricas, cubos, drill-down e control tower.'},
 {area:'IA aplicada ao ERP',status:'PARCIAL',coverage:55,priority:'MÉDIA',done:'Chat, sessões, prompts, classificação, embeddings e análises preditivas.',missing:'Governança de agentes, automações transacionais, avaliação de modelos e integração de recomendações nos processos.'},
 {area:'Plataforma / Integrações',status:'PARCIAL',coverage:68,priority:'ALTA',done:'JWT/AD, multiempresa, APIs REST, Redis/RabbitMQ, Stripe por empresa, observabilidade e OpenAPI.',missing:'Catálogo de integrações, eventos de domínio, DLQ/reprocessamento, gateway, conectores bancários e monitoramento operacional unificado.'}
];

const statusSeverity = {AUSENTE:'danger',BÁSICO:'warning',PARCIAL:'warning',ATIVO:'success'};
const prioritySeverity = {CRÍTICA:'danger',ALTA:'warning',MÉDIA:'info'};

export default function RelatorioParidadeERP() {
    const { t } = useTranslation();
 const [filtro,setFiltro]=useState('TODOS');
 const [busca,setBusca]=useState('');
 const [expanded,setExpanded]=useState(false);
 const filtered=useMemo(()=>areas.filter(a=>
   (filtro==='TODOS'||a.status===filtro||a.priority===filtro) &&
   (!busca||[a.area,a.done,a.missing].join(' ').toLowerCase().includes(busca.toLowerCase()))
 ),[filtro,busca]);
 const total=Math.round(areas.reduce((s,a)=>s+a.coverage,0)/areas.length);
 const critical=areas.filter(a=>a.priority==='CRÍTICA'&&a.coverage<70);
 const missing=areas.filter(a=>a.status==='AUSENTE');
 const summary=[
  ['Cobertura funcional estimada',total+'%','Média das áreas; não é certificação de equivalência com um produto específico.'],
  ['Gaps críticos',critical.length,'Áreas que ainda impedem equivalência operacional ampla.'],
  ['Módulos com cobertura ausente',missing.length,'Necessitam ciclo funcional próprio.'],
  ['Fluxos ponta a ponta incompletos',7,'Comercial, fiscal, financeiro, produção, suprimentos, RH e analytics ainda têm quebras.']
 ];
 return <div className="p-3">
  <Card title={t('legacyUi.parity.title')}>
   <p className="mt-0">
    Auditoria funcional baseada no código atual do repositório: controllers, serviços, entidades, migrations, rotas React,
    componentes, serviços API e documentação de roadmap. O baseline usado é o próprio objetivo documentado no projeto:
    referências de processos encontradas no próprio projeto e em padrões usuais de ERP empresarial. A cobertura abaixo é uma estimativa de gap para planejamento, não é uma certificação de equivalência com um produto específico.
   </p>
   <div className="grid">
    {summary.map(([label,value,help])=><div className="col-12 md:col-3" key={label}>
      <Card className="h-full"><small>{label}</small><div className="text-3xl font-bold mt-2">{value}</div><small>{help}</small></Card>
    </div>)}
   </div>
   <Divider />
   <div className="flex flex-wrap gap-2 align-items-end mb-3">
    <span className="p-input-icon-left"><i className="pi pi-search"/><InputText value={busca} onChange={e=>setBusca(e.target.value)} placeholder={t('legacyUi.parity.search')} /></span>
    <Dropdown value={filtro} options={['TODOS','CRÍTICA','ALTA','MÉDIA','PARCIAL','AUSENTE','BÁSICO'].map(x=>({label:x,value:x}))} onChange={e=>setFiltro(e.value)} />
    <Tag value={t('legacyUi.parity.noChange')} severity="info" />
   </div>
   <DataTable value={filtered} paginator rows={10} stripedRows responsiveLayout="scroll" dataKey="area">
    <Column field="area" header="Área" sortable />
    <Column field="status" header={t('legacyUi.parity.status')} body={r=><Tag value={r.status} severity={statusSeverity[r.status]||'info'} />} sortable />
    <Column field="coverage" header="Cobertura estimada" body={r=><div style={{minWidth:130}}><ProgressBar value={r.coverage} showValue /></div>} sortable />
    <Column field="priority" header={t('legacyUi.parity.priority')} body={r=><Tag value={r.priority} severity={prioritySeverity[r.priority]||'info'} />} sortable />
    <Column header="O que falta" body={r=><span>{r.missing}</span>} />
   </DataTable>
  </Card>
  <Card title={t('legacyUi.parity.map')} className="mt-3">
   <Accordion multiple activeIndex={expanded?areas.map((_,i)=>i):[]}>
    {areas.map(a=><AccordionTab key={a.area} header={<span><strong>{a.area}</strong> — {a.coverage}% <Tag value={a.priority} severity={prioritySeverity[a.priority]||'info'} className="ml-2"/></span>}>
      <div className="grid">
       <div className="col-12 md:col-6"><h4>{t('legacyUi.parity.exists')}</h4><p>{a.done}</p></div>
       <div className="col-12 md:col-6"><h4>{t('legacyUi.parity.gap')}</h4><p>{a.missing}</p></div>
      </div>
    </AccordionTab>)}
   </Accordion>
  </Card>
  <Card title={t('legacyUi.parity.order')} className="mt-3">
   <ol>
    <li><strong>Fiscal transacional real:</strong> emissão, autorização, rejeição, cancelamento, CC-e, contingência e escrituração.</li>
    <li><strong>Contábil + financeiro:</strong> integração automática documento → lançamento → centro de custo → DRE/balanço → fechamento.</li>
    <li><strong>Comercial:</strong> entrega/devolução/bonificação/CRM/contratos/crédito e aprovações.</li>
    <li><strong>Produção:</strong> BOM multinível → roteiro → MRP → chão de fábrica → consumo → custo real.</li>
    <li><strong>Suprimentos/WMS:</strong> planejamento, FEFO/FIFO, serial, devoluções, inventário cíclico e abastecimento.</li>
    <li><strong>RH/Ativos/Qualidade:</strong> transformar cadastros isolados em ciclos operacionais completos.</li>
    <li><strong>BI:</strong> ETL, histórico, dimensões/fatos, cubos e dashboards executivos.</li>
    <li><strong>Workflow e DMS:</strong> aprovações, auditoria, documentos e integrações transversais.</li>
   </ol>
  </Card>
  <Card title={t('legacyUi.parity.closing')} className="mt-3">
   <p>Um módulo só deve ser considerado equivalente quando possuir, para cada processo relevante, <strong>entrada, consulta, tratamento, aprovação/administração, análise, auditoria e integração com o próximo processo</strong>, com permissões e isolamento por empresa.</p>
  </Card>
 </div>;
}
