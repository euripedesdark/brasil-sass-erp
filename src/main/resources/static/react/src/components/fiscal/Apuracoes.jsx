import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';

const BASE = '/api/fiscal/apuracoes';

const competenciaAtual = () => {
    const d = new Date();
    return String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
};

export const Apuracoes = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [impostos, setImpostos] = useState([]);
    const [dlg, setDlg] = useState(false);
    const [impId, setImpId] = useState(null);
    const [comp, setComp] = useState(competenciaAtual);
    const [filtroComp, setFiltroComp] = useState(competenciaAtual);
    const [resumo, setResumo] = useState(null);
    const [busy, setBusy] = useState(false);

    const js = async (r) => {
        if (!r.ok) {
            const err = await r.json().catch(() => ({}));
            const e = new Error(err?.message || err?.erro || ('HTTP ' + r.status));
            e.status = r.status;
            e.body = err;
            throw e;
        }
        const j = await r.json().catch(() => null);
        return Array.isArray(j) ? j : (j?.data ?? j);
    };

    const carregar = async () => {
        setLoading(true);
        try {
            const a = await apiFetch(BASE).then(js);
            setRows(Array.isArray(a) ? a : []);
            const im = await apiFetch('/api/fiscal/impostos').then(js);
            setImpostos(Array.isArray(im) ? im : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro ao carregar', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    const carregarResumo = async (competencia) => {
        if (!competencia) return;
        try {
            const r = await apiFetch(BASE + '/resumo?competencia=' + encodeURIComponent(competencia)).then(js);
            setResumo(r);
        } catch {
            setResumo(null);
        }
    };

    useEffect(() => { carregar(); }, []);
    useEffect(() => { carregarResumo(filtroComp); }, [filtroComp, rows]);

    const nomeImp = (id) => (impostos.find((i) => i.id === id)?.sigla || ('#' + id));

    const money = (v) => {
        if (v == null) return '—';
        const n = Number(v);
        if (Number.isNaN(n)) return String(v);
        return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    const acao = async (id, op) => {
        try {
            await apiFetch(BASE + '/' + id + '/' + op, { method: 'POST' }).then(js);
            toast.current?.show({ severity: 'success', summary: op, life: 2500 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message || 'Falhou', life: 4000 });
        }
    };

    const calcular = async () => {
        if (!impId || !comp) return;
        setBusy(true);
        try {
            await apiFetch(
                BASE + '/calcular?impostoId=' + impId + '&competencia=' + encodeURIComponent(comp),
                { method: 'POST' }
            ).then(js);
            setDlg(false);
            toast.current?.show({ severity: 'success', summary: 'Apurado', life: 2500 });
            setFiltroComp(comp);
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setBusy(false);
        }
    };

    const calcularTodas = async () => {
        if (!filtroComp) return;
        setBusy(true);
        try {
            await apiFetch(
                BASE + '/calcular-todas?competencia=' + encodeURIComponent(filtroComp),
                { method: 'POST' }
            ).then(js);
            toast.current?.show({ severity: 'success', summary: 'Competência apurada', detail: filtroComp, life: 3000 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setBusy(false);
        }
    };

    const sev = (s) => (s === 'TRANSMITIDA' ? 'success' : s === 'ENCERRADA' ? 'info' : 'warning');

    const rowsFiltradas = filtroComp
        ? rows.filter((r) => r.competencia === filtroComp)
        : rows;

    const botoes = (r) => (
        <span className="flex gap-1">
            {r.status === 'ABERTA' && (
                <Button icon="pi pi-lock" tooltip="Encerrar" size="small" onClick={() => acao(r.id, 'encerrar')} />
            )}
            {r.status === 'ENCERRADA' && (
                <>
                    <Button icon="pi pi-undo" tooltip="Reabrir" size="small" severity="secondary" onClick={() => acao(r.id, 'reabrir')} />
                    <Button icon="pi pi-send" tooltip="Transmitir" size="small" onClick={() => acao(r.id, 'transmitir')} />
                </>
            )}
        </span>
    );

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Apuração de impostos</h2>
                    <span className="bc-muted">
                        ICMS/IPI e PIS/COFINS com crédito das entradas; ISS de NFS-e; demais pela alíquota padrão
                    </span>
                </div>
                <div className="flex gap-2 flex-wrap">
                    <span className="p-input-icon-left">
                        <i className="pi pi-calendar" />
                        <InputText
                            value={filtroComp}
                            onChange={(e) => setFiltroComp(e.target.value)}
                            placeholder="MM/AAAA"
                            style={{ width: '7.5rem' }}
                        />
                    </span>
                    <Button
                        label="Apurar todos"
                        icon="pi pi-calculator"
                        severity="help"
                        loading={busy}
                        onClick={calcularTodas}
                        disabled={!filtroComp}
                        tooltip="Calcula todos os impostos ativos da competência"
                    />
                    <Button label="Apurar um" icon="pi pi-plus" onClick={() => { setComp(filtroComp); setDlg(true); }} />
                </div>
            </div>

            {resumo && (
                <div className="grid mb-3">
                    <div className="col-12 md:col-3">
                        <Card className="shadow-1">
                            <div className="text-500 text-sm">Competência</div>
                            <div className="text-xl font-bold">{resumo.competencia}</div>
                        </Card>
                    </div>
                    <div className="col-12 md:col-3">
                        <Card className="shadow-1">
                            <div className="text-500 text-sm">Total devido</div>
                            <div className="text-xl font-bold">{money(resumo.totalDevido)}</div>
                        </Card>
                    </div>
                    <div className="col-12 md:col-3">
                        <Card className="shadow-1">
                            <div className="text-500 text-sm">Total crédito</div>
                            <div className="text-xl font-bold text-green-600">{money(resumo.totalCredito)}</div>
                        </Card>
                    </div>
                    <div className="col-12 md:col-3">
                        <Card className="shadow-1">
                            <div className="text-500 text-sm">Total a pagar</div>
                            <div className="text-xl font-bold text-orange-600">{money(resumo.totalPagar)}</div>
                        </Card>
                    </div>
                </div>
            )}

            <DataTable
                value={rowsFiltradas}
                loading={loading}
                paginator
                rows={15}
                emptyMessage="Nenhuma apuração nesta competência. Use Apurar todos ou Apurar um."
                responsiveLayout="scroll"
            >
                <Column field="competencia" header="Competência" style={{ width: '8rem' }} />
                <Column header="Imposto" body={(r) => nomeImp(r.impostoId)} />
                <Column field="baseCalculo" header="Base" body={(r) => money(r.baseCalculo)} />
                <Column field="valorDevido" header="Devido" body={(r) => money(r.valorDevido)} />
                <Column field="valorCredito" header="Crédito" body={(r) => money(r.valorCredito)} />
                <Column field="valorPagar" header="A pagar" body={(r) => money(r.valorPagar)} />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column header="Ações" body={botoes} style={{ width: '8rem' }} />
            </DataTable>

            <Dialog header="Apurar um imposto" visible={dlg} onHide={() => setDlg(false)} style={{ width: '26rem' }}>
                <div className="flex flex-column gap-3">
                    <span>
                        <label className="bc-label">Imposto</label>
                        <Dropdown
                            value={impId}
                            options={impostos.map((i) => ({ label: i.sigla + ' — ' + (i.nome || ''), value: i.id }))}
                            onChange={(e) => setImpId(e.value)}
                            placeholder="Escolha"
                            filter
                            className="w-full"
                        />
                    </span>
                    <span>
                        <label className="bc-label">Competência (MM/AAAA)</label>
                        <InputText value={comp} onChange={(e) => setComp(e.target.value)} placeholder="MM/AAAA" className="w-full" />
                    </span>
                    <Button label="Calcular" icon="pi pi-check" onClick={calcular} disabled={!impId || !comp || busy} loading={busy} />
                </div>
            </Dialog>
        </div>
    );
};

export default Apuracoes;
