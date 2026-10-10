import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { formatoData, localeAtivo } from '../shared/LocaleData.js';

const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const OP = '/api/financeiro/cobranca-op';
const TIPOS_ACAO = ['LEMBRETE', 'AVISO', 'LIGACAO', 'EMAIL', 'WHATSAPP', 'NEGATIVACAO', 'OUTRO'];

export const Cobranca = () => {
    const toast = useRef(null);
    const navigate = useNavigate();
    const [carteira, setCarteira] = useState([]);
    const [promessas, setPromessas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dlgAcao, setDlgAcao] = useState(false);
    const [dlgProm, setDlgProm] = useState(false);
    const [selTitulo, setSelTitulo] = useState(null);
    const [fAcao, setFAcao] = useState({ tipo: 'LEMBRETE', nivel: 1, observacao: '' });
    const [fProm, setFProm] = useState({ valor: null, data: null, observacao: '' });
    const [histAcoes, setHistAcoes] = useState([]);
    const [dlgHist, setDlgHist] = useState(false);

    const js = async (r) => {
        const j = await r.json().catch(() => null);
        return Array.isArray(j) ? j : (j?.data ?? j?.content ?? j ?? []);
    };

    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [c, p] = await Promise.all([
                apiFetch(OP + '/carteira').then(js),
                apiFetch(OP + '/promessas').then(js),
            ]);
            setCarteira(Array.isArray(c) ? c : []);
            setPromessas(Array.isArray(p) ? p : []);
        } catch {
            toast.current?.show({ severity: 'error', summary: 'Erro ao carregar cobrança', life: 4000 });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    const abrirAcao = (row) => {
        setSelTitulo(row);
        setFAcao({ tipo: 'LEMBRETE', nivel: Math.max(1, row.nivelSugerido || 1), observacao: '' });
        setDlgAcao(true);
    };

    const salvarAcao = async () => {
        try {
            const r = await apiFetch(OP + '/acoes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    tituloId: selTitulo.tituloId,
                    tipo: fAcao.tipo,
                    nivel: fAcao.nivel,
                    observacao: fAcao.observacao,
                }),
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.message || 'Falha ao registrar ação');
            }
            toast.current?.show({ severity: 'success', summary: 'Ação registrada', life: 3000 });
            setDlgAcao(false);
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        }
    };

    const abrirPromessa = (row) => {
        setSelTitulo(row);
        setFProm({ valor: Number(row.valorSaldo || 0), data: null, observacao: '' });
        setDlgProm(true);
    };

    const salvarPromessa = async () => {
        if (!fProm.valor || !fProm.data) {
            toast.current?.show({ severity: 'warn', summary: 'Informe valor e data', life: 3000 });
            return;
        }
        try {
            const iso = fProm.data.toISOString().slice(0, 10);
            const r = await apiFetch(OP + '/promessas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    tituloId: selTitulo.tituloId,
                    valor: fProm.valor,
                    dataPrometida: iso,
                    observacao: fProm.observacao,
                }),
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.message || 'Falha ao gravar promessa');
            }
            toast.current?.show({ severity: 'success', summary: 'Promessa registrada', life: 3000 });
            setDlgProm(false);
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        }
    };

    const statusPromessa = async (id, status) => {
        try {
            const r = await apiFetch(OP + '/promessas/' + id + '/status', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status }),
            });
            if (!r.ok) throw new Error('Falha');
            carregar();
        } catch {
            toast.current?.show({ severity: 'error', summary: 'Erro ao atualizar promessa', life: 3500 });
        }
    };

    const processarVencidas = async () => {
        const r = await apiFetch(OP + '/promessas/processar-vencidas', { method: 'POST' });
        const j = await r.json().catch(() => ({}));
        toast.current?.show({ severity: 'info', summary: `${j.quebradas || 0} promessa(s) marcada(s) como QUEBRADA`, life: 3500 });
        carregar();
    };

    const verHist = async (row) => {
        setSelTitulo(row);
        const lista = await apiFetch(OP + '/acoes?tituloId=' + row.tituloId).then(js);
        setHistAcoes(Array.isArray(lista) ? lista : []);
        setDlgHist(true);
    };

    const sevAtraso = (d) => (d <= 0 ? 'success' : d <= 15 ? 'info' : d <= 30 ? 'warning' : 'danger');

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Cobrança operacional</h2>
                    <span className="bc-muted">Carteira, níveis de cobrança e promessas (FSCM-lite)</span>
                </div>
                <div className="flex gap-2">
                    <Button label="Processar promessas vencidas" icon="pi pi-sync" outlined onClick={processarVencidas} />
                    <Button label="Renegociação" icon="pi pi-replay" text onClick={() => navigate('/financeiro/renegociacao')} />
                    <Button icon="pi pi-refresh" rounded text onClick={carregar} />
                </div>
            </div>

            <TabView>
                <TabPanel header={`Carteira (${carteira.length})`}>
                    <DataTable value={carteira} loading={loading} paginator rows={15} dataKey="tituloId" emptyMessage="Sem títulos em aberto a receber">
                        <Column field="tituloId" header="#" style={{ width: '5rem' }} />
                        <Column field="descricao" header="Descrição" />
                        <Column field="dataVencimento" header="Vencimento" style={{ width: '8rem' }} />
                        <Column header="Saldo" body={(r) => fmt(r.valorSaldo)} style={{ width: '9rem' }} />
                        <Column header="Atraso" body={(r) => <Tag value={`${r.diasAtraso}d`} severity={sevAtraso(r.diasAtraso)} />} style={{ width: '6rem' }} />
                        <Column header="Nível sug." body={(r) => r.nivelSugerido} style={{ width: '6rem' }} />
                        <Column header="Último" body={(r) => r.ultimoNivel} style={{ width: '5rem' }} />
                        <Column header="" style={{ width: '16rem' }} body={(r) => (
                            <div className="flex gap-1 flex-wrap">
                                <Button label="Ação" size="small" onClick={() => abrirAcao(r)} />
                                <Button label="Promessa" size="small" outlined onClick={() => abrirPromessa(r)} />
                                <Button icon="pi pi-history" rounded text tooltip="Histórico" onClick={() => verHist(r)} />
                            </div>
                        )} />
                    </DataTable>
                </TabPanel>
                <TabPanel header={`Promessas (${promessas.length})`}>
                    <DataTable value={promessas} loading={loading} paginator rows={10} dataKey="id" emptyMessage="Nenhuma promessa">
                        <Column field="id" header="#" style={{ width: '5rem' }} />
                        <Column field="tituloId" header="Título" />
                        <Column header="Valor" body={(r) => fmt(r.valorPrometido)} />
                        <Column field="dataPrometida" header="Data" />
                        <Column field="status" header="Status" body={(r) => (
                            <Tag value={r.status} severity={r.status === 'ABERTA' ? 'info' : r.status === 'CUMPRIDA' ? 'success' : r.status === 'QUEBRADA' ? 'danger' : 'secondary'} />
                        )} />
                        <Column field="observacao" header="Obs." />
                        <Column header="" style={{ width: '12rem' }} body={(r) => r.status === 'ABERTA' ? (
                            <div className="flex gap-1">
                                <Button label="Cumprida" size="small" severity="success" onClick={() => statusPromessa(r.id, 'CUMPRIDA')} />
                                <Button label="Quebrada" size="small" severity="danger" outlined onClick={() => statusPromessa(r.id, 'QUEBRADA')} />
                            </div>
                        ) : null} />
                    </DataTable>
                </TabPanel>
            </TabView>

            <Dialog visible={dlgAcao} onHide={() => setDlgAcao(false)} header={`Ação de cobrança — título #${selTitulo?.tituloId || ''}`} modal style={{ width: 'min(480px, 96vw)' }}>
                <div className="grid p-fluid">
                    <div className="col-12 md:col-6">
                        <label>Tipo</label>
                        <Dropdown value={fAcao.tipo} options={TIPOS_ACAO.map((t) => ({ label: t, value: t }))} onChange={(e) => setFAcao({ ...fAcao, tipo: e.value })} />
                    </div>
                    <div className="col-12 md:col-6">
                        <label>Nível (1–5)</label>
                        <InputNumber value={fAcao.nivel} onValueChange={(e) => setFAcao({ ...fAcao, nivel: e.value })} min={1} max={5} showButtons />
                    </div>
                    <div className="col-12">
                        <label>Observação</label>
                        <InputTextarea rows={3} value={fAcao.observacao} onChange={(e) => setFAcao({ ...fAcao, observacao: e.target.value })} />
                    </div>
                </div>
                <div className="flex justify-end gap-2 mt-3">
                    <Button label="Cancelar" text severity="secondary" onClick={() => setDlgAcao(false)} />
                    <Button label="Registrar" icon="pi pi-check" onClick={salvarAcao} />
                </div>
            </Dialog>

            <Dialog visible={dlgProm} onHide={() => setDlgProm(false)} header={`Promessa — título #${selTitulo?.tituloId || ''}`} modal style={{ width: 'min(420px, 96vw)' }}>
                <div className="grid p-fluid">
                    <div className="col-12">
                        <label>Valor prometido *</label>
                        <InputNumber value={fProm.valor} onValueChange={(e) => setFProm({ ...fProm, valor: e.value })} mode="currency" currency="BRL" locale="pt-BR" min={0.01} />
                    </div>
                    <div className="col-12">
                        <label>Data prometida *</label>
                        <Calendar value={fProm.data} onChange={(e) => setFProm({ ...fProm, data: e.value })} dateFormat={formatoData()} showIcon minDate={new Date()} />
                    </div>
                    <div className="col-12">
                        <label>Observação</label>
                        <InputTextarea rows={2} value={fProm.observacao} onChange={(e) => setFProm({ ...fProm, observacao: e.target.value })} />
                    </div>
                    <div className="col-12"><small className="bc-muted">Saldo do título: {fmt(selTitulo?.valorSaldo)}</small></div>
                </div>
                <div className="flex justify-end gap-2 mt-3">
                    <Button label="Cancelar" text severity="secondary" onClick={() => setDlgProm(false)} />
                    <Button label="Salvar promessa" icon="pi pi-check" severity="success" onClick={salvarPromessa} />
                </div>
            </Dialog>

            <Dialog visible={dlgHist} onHide={() => setDlgHist(false)} header={`Histórico de ações — #${selTitulo?.tituloId || ''}`} modal style={{ width: 'min(640px, 96vw)' }}>
                <DataTable value={histAcoes} emptyMessage="Sem ações" size="small">
                    <Column field="createdAt" header="Data" body={(r) => r.createdAt ? new Date(r.createdAt).toLocaleString(localeAtivo()) : '—'} />
                    <Column field="tipo" header="Tipo" />
                    <Column field="nivel" header="Nível" />
                    <Column field="observacao" header="Obs." />
                </DataTable>
            </Dialog>
        </div>
    );
};

export default Cobranca;
