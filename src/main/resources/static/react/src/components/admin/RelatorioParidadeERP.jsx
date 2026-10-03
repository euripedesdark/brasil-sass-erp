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
 {area:'Cadastros e dados mestres',status:'PARCIAL',coverage:70,priority:'ALTA',done:'Pessoas, clientes, fornecedores, produtos, categorias, marcas, serviços, unidades, transportadoras, municípios.',missing:'Hierarquias comerciais completas, múltiplos endereços/funções, parceiros de negócio avançados, condições por parceiro, crédito/limites, anexos/documentos, versionamento e governança de dados mestres.'},
 {area:'Vendas / Comercial',status:'PARCIAL',coverage:65,priority:'CRÍTICA',done:'Orçamento → confirmação → pedido → reserva → faturamento; tabela de preços e comissão.',missing:'Devolução, troca, bonificação, contratos, recorrência, CRM completo, metas, política comercial, crédito, aprovação de descontos, entrega, frete, documentos fiscais de saída e pós-venda.'},
 {area:'Compras / Suprimentos',status:'PARCIAL',coverage:70,priority:'ALTA',done:'Solicitação → cotação → comparação → pedido → recebimento parcial/total → estoque → conferência financeira.',missing:'Homologação de fornecedores, RFQ completo, mapas de cotação robustos, contratos, programação de compras, recebimento cego, devolução a fornecedor, avaliação de fornecedor e planejamento de abastecimento.'},
 {area:'Estoque / WMS',status:'PARCIAL',coverage:80,priority:'ALTA',done:'Depósitos, saldos, movimentos, lotes, endereços, reservas, picking, packing, expedição, transferências internas, inventário.',missing:'FEFO/FIFO configurável, regras automáticas de endereçamento, mapa de armazém, serialização, rastreabilidade ponta a ponta, ondas de picking, cross-docking, devoluções e inventário cíclico avançado.'},
 {area:'Produção / PCP',status:'PARCIAL',coverage:55,priority:'CRÍTICA',done:'OP, BOM/estrutura, componentes, apontamentos, romaneio e finalização.',missing:'BOM multinível/versionada completa, roteiros, centros de trabalho, capacidade, MRP, APS/planejamento, chão de fábrica, refugo, co-produtos/subprodutos, backflush robusto, custo padrão/real e análise de variância.'},
 {area:'Financeiro',status:'PARCIAL',coverage:70,priority:'CRÍTICA',done:'Títulos, parcelas, baixa, contas bancárias, extrato, caixa, plano de contas, lançamentos, centros de custo, conciliação.',missing:'Fluxo de caixa projetado, orçamento, cobrança, renegociação, adiantamentos, CNAB/OFX, PIX/boleto, protesto, juros/multa parametrizados, rateios, fechamento financeiro e integração contábil completa.'},
 {area:'Contabilidade',status:'PARCIAL',coverage:45,priority:'CRÍTICA',done:'Plano de contas e lançamentos contábeis.',missing:'Partidas automáticas por documento, períodos fiscais, fechamento, balancete, razão, diário, DRE, balanço, apuração, rateios, múltiplos livros e trilha contábil completa.'},
 {area:'Fiscal Brasil',status:'PARCIAL',coverage:45,priority:'CRÍTICA',done:'NCM, CFOP, CEST, ISSQN, impostos, entrada de nota e consulta SEFAZ; bibliotecas fiscais presentes.',missing:'Emissão real NF-e/NFC-e/NFS-e, autorização/cancelamento/inutilização/CC-e, manifestação, contingência, certificados, DANFE, CT-e/MDF-e, escrituração, SPED/EFD/ECD/ECF e tratamento real de rejeições SEFAZ.'},
 {area:'RH / Folha',status:'PARCIAL',coverage:35,priority:'ALTA',done:'Cadastro de cargos, funcionários e estrutura inicial de folha.',missing:'Folha CLT completa, férias, rescisão, 13º, encargos, pró-labore, ponto, banco de horas, benefícios, medicina/segurança, eventos e fechamento eSocial.'},
 {area:'Ativo imobilizado / Manutenção',status:'AUSENTE',coverage:5,priority:'MÉDIA',done:'Não há ciclo operacional equivalente fechado.',missing:'Cadastro de bens, aquisição, capitalização, localização, responsáveis, depreciação contábil/fiscal, baixas, transferências, reavaliação, manutenção preventiva/corretiva e custo do ativo.'},
 {area:'Qualidade',status:'AUSENTE',coverage:5,priority:'MÉDIA',done:'Não há módulo operacional completo.',missing:'Planos de inspeção, amostragem, recebimento/processo/expedição, instrumentos, resultados, bloqueio/liberação, não conformidade, causa raiz, CAPA/8D e indicadores.'},
 {area:'BI / Analytics',status:'PARCIAL',coverage:35,priority:'CRÍTICA',done:'BI, KPIs, relatórios e relatórios agendados existentes.',missing:'Data warehouse/data lake operacional, ETL incremental, histórico dimensional, cubos/pivôs, dashboards executivos completos, margem, DRE analítica, ABC, drill-down, relatórios ad-hoc e alertas.'},
 {area:'IA aplicada ao ERP',status:'PARCIAL',coverage:40,priority:'MÉDIA',done:'Chat, sessões, classificações, embeddings e previsões no backend.',missing:'Workspace completo para configuração, prompts/templates, classificação operacional, análises pendentes, previsão de vendas/estoque/financeiro, governança, histórico, aprovação e integração das recomendações aos processos.'},
 {area:'Documentos / DMS',status:'BÁSICO',coverage:20,priority:'MÉDIA',done:'Armazenamento de imagens e infraestrutura Mongo existente.',missing:'DMS genérico, anexos por entidade, versionamento, metadados, busca, permissões, retenção, assinatura, auditoria, PDF/XML e documentos fiscais vinculados.'},
 {area:'Workflow / Aprovações / Auditoria',status:'PARCIAL',coverage:35,priority:'ALTA',done:'RBAC, permissões e alguns fluxos de aprovação.',missing:'Motor genérico de workflow, alçadas por valor, aprovação multinível, segregação de funções, SLA, notificações, auditoria de negócio completa, histórico de alterações e reversão controlada.'},
 {area:'Plataforma / Integrações',status:'PARCIAL',coverage:45,priority:'ALTA',done:'JWT, APIs REST, microserviços, Redis/RabbitMQ previstos/configurados.',missing:'API gateway consolidado, webhooks, SDK/API externa, idempotência, filas/eventos efetivamente usados no domínio, importação/exportação, conectores bancários/fiscais e monitoramento operacional.'}
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
