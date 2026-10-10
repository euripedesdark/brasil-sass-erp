import React, { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { Card } from 'primereact/card';
import './Qualidade.css';
import { TabView, TabPanel } from 'primereact/tabview';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Message } from 'primereact/message';

const msg = (e) =>
  e?.response?.data?.errors?.[0]?.message ||
  e?.response?.data?.message ||
  e?.response?.data?.error ||
  e?.message ||
  'Operação não realizada';

const sevInsp = (s) => {
  switch (s) {
    case 'ABERTA': return 'info';
    case 'CONCLUIDA': return 'success';
    default: return 'secondary';
  }
};
const sevNc = (s) => {
  switch (s) {
    case 'ABERTA': return 'warning';
    case 'EM_ACAO': return 'info';
    case 'ENCERRADA': return 'success';
    default: return 'secondary';
  }
};
const sevResult = (r) => (r === 'APROVADO' ? 'success' : r === 'REPROVADO' ? 'danger' : 'secondary');

export default function Qualidade() {
  const toast = useRef(null);
  const [tab, setTab] = useState(0);
  const [planos, setPlanos] = useState([]);
  const [inspecoes, setInspecoes] = useState([]);
  const [ncs, setNcs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dialog, setDialog] = useState(null);
  const [form, setForm] = useState({});
  const [processando, setProcessando] = useState(false);
  const [capaNc, setCapaNc] = useState(null);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [p, i, n] = await Promise.all([
        axios.get('/api/qualidade/planos'),
        axios.get('/api/qualidade/inspecoes'),
        axios.get('/api/qualidade/nao-conformidades'),
      ]);
      setPlanos(p.data?.data ?? p.data ?? []);
      setInspecoes(i.data?.data ?? i.data ?? []);
      setNcs(n.data?.data ?? n.data ?? []);
    } catch (e) {
      setError(msg(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const save = async () => {
    setProcessando(true);
    try {
      if (dialog === 'plano') {
        await axios.post('/api/qualidade/planos', form);
        toast.current?.show({ severity: 'success', summary: 'Plano salvo', life: 2500 });
      } else if (dialog === 'inspecao') {
        await axios.post('/api/qualidade/inspecoes', form);
        toast.current?.show({ severity: 'success', summary: 'Inspeção aberta', life: 2500 });
      } else if (dialog === 'nc') {
        await axios.post('/api/qualidade/nao-conformidades', form);
        toast.current?.show({ severity: 'success', summary: 'NC criada', life: 2500 });
      }
      setDialog(null);
      setForm({});
      await load();
    } catch (e) {
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const concluir = async (row, resultado) => {
    setProcessando(true);
    try {
      await axios.post(`/api/qualidade/inspecoes/${row.id}/concluir`, {
        resultado,
        gerarNc: resultado === 'REPROVADO',
        descricaoNc: resultado === 'REPROVADO' ? `Reprovado na inspeção ${row.numero}` : undefined,
      });
      toast.current?.show({
        severity: resultado === 'APROVADO' ? 'success' : 'warn',
        summary: resultado === 'APROVADO' ? 'Inspeção aprovada' : 'Inspeção reprovada — NC gerada',
        life: 3500,
      });
      await load();
    } catch (e) {
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const salvarCapa = async () => {
    if (!capaNc?.id) return;
    setProcessando(true);
    try {
      await axios.post(`/api/qualidade/nao-conformidades/${capaNc.id}/acoes`, {
        causaRaiz: capaNc.causaRaiz,
        acaoCorretiva: capaNc.acaoCorretiva,
        acaoPreventiva: capaNc.acaoPreventiva,
        prazo: capaNc.prazo || null,
      });
      toast.current?.show({ severity: 'success', summary: 'CAPA registrada', life: 2500 });
      setCapaNc(null);
      await load();
    } catch (e) {
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const encerrarNc = async (row) => {
    setProcessando(true);
    try {
      await axios.post(`/api/qualidade/nao-conformidades/${row.id}/encerrar`);
      toast.current?.show({ severity: 'success', summary: 'NC encerrada', life: 2500 });
      await load();
    } catch (e) {
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button label="Cancelar" text onClick={() => { setDialog(null); setForm({}); }} />
      <Button label="Salvar" icon="pi pi-save" loading={processando} onClick={save} />
    </div>
  );

  return (
    <div className="p-3 qualidade-enterprise-container">
      <Toast ref={toast} />
      <Card title="Gestão da Qualidade" subTitle="Planos → Inspeção → NC / CAPA">
        {error && <Message severity="error" text={error} className="mb-2" />}
        <TabView activeIndex={tab} onTabChange={(e) => setTab(e.index)}>
          <TabPanel header="Planos de inspeção">
            <div className="flex justify-content-end mb-3">
              <Button label="Novo plano" icon="pi pi-plus" onClick={() => { setForm({ tipo: 'RECEBIMENTO', ativo: true }); setDialog('plano'); }} />
            </div>
            <DataTable value={planos} loading={loading} paginator rows={10} emptyMessage="Sem planos">
              <Column field="codigo" header="Código" sortable />
              <Column field="descricao" header="Descrição" sortable />
              <Column field="tipo" header="Tipo" />
              <Column field="ativo" header="Ativo" body={(r) => (r.ativo ? 'Sim' : 'Não')} />
            </DataTable>
          </TabPanel>

          <TabPanel header="Inspeções">
            <div className="flex justify-content-end mb-3">
              <Button
                label="Nova inspeção"
                icon="pi pi-plus"
                onClick={() => {
                  setForm({
                    dataInspecao: new Date().toISOString().slice(0, 10),
                    status: 'ABERTA',
                    referenciaTipo: 'RECEBIMENTO',
                    planoId: planos[0]?.id || null,
                  });
                  setDialog('inspecao');
                }}
              />
            </div>
            <DataTable value={inspecoes} loading={loading} paginator rows={10} emptyMessage="Sem inspeções">
              <Column field="numero" header="Número" />
              <Column field="planoId" header="Plano" />
              <Column field="referenciaTipo" header="Origem" />
              <Column field="dataInspecao" header="Data" />
              <Column header="Status" body={(r) => <Tag value={r.status || '—'} severity={sevInsp(r.status)} />} />
              <Column header="Resultado" body={(r) => (r.resultado ? <Tag value={r.resultado} severity={sevResult(r.resultado)} /> : '—')} />
              <Column
                header="Ações"
                body={(r) => (
                  <div className="flex gap-1">
                    <Button icon="pi pi-check" text rounded severity="success" tooltip="Aprovar" disabled={processando || r.status !== 'ABERTA'} onClick={() => concluir(r, 'APROVADO')} />
                    <Button icon="pi pi-times" text rounded severity="danger" tooltip="Reprovar (gera NC)" disabled={processando || r.status !== 'ABERTA'} onClick={() => concluir(r, 'REPROVADO')} />
                  </div>
                )}
              />
            </DataTable>
          </TabPanel>

          <TabPanel header="Não conformidades">
            <div className="flex justify-content-end mb-3">
              <Button label="Nova NC" icon="pi pi-plus" onClick={() => { setForm({ severidade: 'MEDIA', status: 'ABERTA' }); setDialog('nc'); }} />
            </div>
            <DataTable value={ncs} loading={loading} paginator rows={10} emptyMessage="Sem NCs">
              <Column field="numero" header="Número" />
              <Column field="inspecaoId" header="Inspeção" />
              <Column field="severidade" header="Severidade" />
              <Column header="Status" body={(r) => <Tag value={r.status || '—'} severity={sevNc(r.status)} />} />
              <Column field="descricao" header="Descrição" />
              <Column field="prazo" header="Prazo" />
              <Column
                header="Ações"
                body={(r) => (
                  <div className="flex gap-1">
                    <Button icon="pi pi-pencil" text rounded tooltip="CAPA" disabled={r.status === 'ENCERRADA'} onClick={() => setCapaNc({ ...r })} />
                    <Button icon="pi pi-check" text rounded severity="success" tooltip="Encerrar" disabled={processando || r.status === 'ENCERRADA'} onClick={() => encerrarNc(r)} />
                  </div>
                )}
              />
            </DataTable>
          </TabPanel>
        </TabView>

        <Dialog
          header={dialog === 'plano' ? 'Plano de inspeção' : dialog === 'inspecao' ? 'Inspeção' : 'Não conformidade'}
          visible={!!dialog}
          onHide={() => { setDialog(null); setForm({}); }}
          footer={footer}
          modal
          style={{ width: 'min(680px,92vw)' }}
        >
          {dialog === 'plano' && (
            <div className="grid p-fluid">
              <div className="col-12 md:col-4"><label>Código</label><InputText value={form.codigo || ''} onChange={(e) => setForm({ ...form, codigo: e.target.value })} /></div>
              <div className="col-12 md:col-8"><label>Descrição</label><InputText value={form.descricao || ''} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></div>
              <div className="col-12"><label>Tipo</label><Dropdown value={form.tipo} options={['RECEBIMENTO', 'PROCESSO', 'FINAL']} onChange={(e) => setForm({ ...form, tipo: e.value })} /></div>
            </div>
          )}
          {dialog === 'inspecao' && (
            <div className="grid p-fluid">
              <div className="col-12 md:col-6">
                <label>Plano *</label>
                <Dropdown value={form.planoId} options={planos.map((p) => ({ label: `${p.codigo} — ${p.descricao}`, value: p.id }))} onChange={(e) => setForm({ ...form, planoId: e.value })} placeholder="Selecione o plano" />
              </div>
              <div className="col-12 md:col-6">
                <label>Tipo de referência</label>
                <Dropdown value={form.referenciaTipo} options={['RECEBIMENTO', 'PRODUCAO', 'EXPEDICAO', 'OUTRO']} onChange={(e) => setForm({ ...form, referenciaTipo: e.value })} />
              </div>
              <div className="col-12 md:col-6">
                <label>Número (opcional)</label>
                <InputText value={form.numero || ''} onChange={(e) => setForm({ ...form, numero: e.target.value })} placeholder="Gerado automaticamente" />
              </div>
              <div className="col-12"><label>Observação</label><InputTextarea rows={3} value={form.observacao || ''} onChange={(e) => setForm({ ...form, observacao: e.target.value })} /></div>
            </div>
          )}
          {dialog === 'nc' && (
            <div className="grid p-fluid">
              <div className="col-12 md:col-6"><label>Número (opcional)</label><InputText value={form.numero || ''} onChange={(e) => setForm({ ...form, numero: e.target.value })} /></div>
              <div className="col-12 md:col-6"><label>Severidade</label><Dropdown value={form.severidade} options={['BAIXA', 'MEDIA', 'ALTA', 'CRITICA']} onChange={(e) => setForm({ ...form, severidade: e.value })} /></div>
              <div className="col-12"><label>Descrição *</label><InputTextarea rows={4} value={form.descricao || ''} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></div>
            </div>
          )}
        </Dialog>

        <Dialog
          header={`CAPA — ${capaNc?.numero || ''}`}
          visible={!!capaNc}
          onHide={() => setCapaNc(null)}
          modal
          style={{ width: 'min(640px,92vw)' }}
          footer={
            <div className="flex justify-content-end gap-2">
              <Button label="Fechar" text onClick={() => setCapaNc(null)} />
              <Button label="Salvar CAPA" icon="pi pi-save" loading={processando} onClick={salvarCapa} />
            </div>
          }
        >
          {capaNc && (
            <div className="grid p-fluid">
              <div className="col-12"><label>Causa raiz</label><InputTextarea rows={3} value={capaNc.causaRaiz || ''} onChange={(e) => setCapaNc({ ...capaNc, causaRaiz: e.target.value })} /></div>
              <div className="col-12"><label>Ação corretiva *</label><InputTextarea rows={3} value={capaNc.acaoCorretiva || ''} onChange={(e) => setCapaNc({ ...capaNc, acaoCorretiva: e.target.value })} /></div>
              <div className="col-12"><label>Ação preventiva</label><InputTextarea rows={3} value={capaNc.acaoPreventiva || ''} onChange={(e) => setCapaNc({ ...capaNc, acaoPreventiva: e.target.value })} /></div>
              <div className="col-12 md:col-6"><label>Prazo</label><InputText type="date" value={capaNc.prazo || ''} onChange={(e) => setCapaNc({ ...capaNc, prazo: e.target.value })} /></div>
              <div className="col-12"><small className="text-color-secondary">Para encerrar a NC é obrigatório ter ação corretiva registrada.</small></div>
            </div>
          )}
        </Dialog>
      </Card>
    </div>
  );
}
