import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/rh/esocial-fila';

export const Esocial = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [funcs, setFuncs] = useState([]);
    const [tiposMap, setTiposMap] = useState({});
    const [f, setF] = useState({ tipo: 'S-2200', payload: '{}' });
    const [busy, setBusy] = useState(false);

    const js = async (r) => {
        if (!r.ok) {
            const err = await r.json().catch(() => ({}));
            throw new Error(err?.message || err?.erro || ('HTTP ' + r.status));
        }
        const j = await r.json().catch(() => null);
        return Array.isArray(j) ? j : (j?.data ?? j);
    };

    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            setRows(await apiFetch(BASE).then(js));
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        carregar();
        apiFetch(BASE + '/tipos').then(js).then((m) => {
            if (m && typeof m === 'object') setTiposMap(m);
        }).catch(() => {});
        apiFetch('/api/rh/funcionarios').then(js).then((l) => setFuncs(Array.isArray(l) ? l : [])).catch(() => {});
    }, [carregar]);

    const tiposOpts = Object.keys(tiposMap).length
        ? Object.entries(tiposMap).map(([k, v]) => ({ label: k + ' — ' + v, value: k }))
        : ['S-2200', 'S-1200', 'S-1210', 'S-2299', 'S-3000', 'S-1000', 'S-1299'].map((t) => ({ label: t, value: t }));

    const salvar = async () => {
        setBusy(true);
        try {
            await apiFetch(BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    tipo: f.tipo,
                    funcionarioId: f.funcionarioId ? Number(f.funcionarioId) : null,
                    payload: f.payload || '{}',
                }),
            }).then(js);
            setDlg(false);
            setF({ tipo: 'S-2200', payload: '{}' });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const transmitir = async (id) => {
        setBusy(true);
        try {
            const j = await apiFetch(BASE + '/' + id + '/transmitir', { method: 'POST' }).then(js);
            toast.current?.show({
                severity: j.status === 'ENVIADO' ? 'success' : 'warn',
                summary: j.status || 'resposta',
                detail: (j.erro || j.protocolo || j.recibo || '').slice(0, 200),
                life: 6000,
            });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const consultar = async (id) => {
        setBusy(true);
        try {
            const j = await apiFetch(BASE + '/' + id + '/consultar', { method: 'POST' }).then(js);
            toast.current?.show({
                severity: j.status === 'PROCESSADO' ? 'success' : j.status === 'RECUSADO' ? 'danger' : 'info',
                summary: j.status,
                detail: (j.recibo || j.erro || j.protocolo || '').slice(0, 200),
                life: 6000,
            });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const excluir = async (id) => {
        await apiFetch(BASE + '/' + id, { method: 'DELETE' });
        carregar();
    };

    const sev = (s) => {
        if (s === 'ENVIADO' || s === 'PROCESSADO') return 'success';
        if (s === 'FALHA' || s === 'RECUSADO') return 'danger';
        return 'warning';
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Fila eSocial</h2>
                    <span className="text-color-secondary">
                        Enfileira no esocial-jt (microservices/esocial). ENVIADO = aceito pelo JT; use Consultar para status do governo.
                    </span>
                </div>
                <Button label="Novo evento" icon="pi pi-plus" onClick={() => setDlg(true)} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage="Nenhum evento" dataKey="id" responsiveLayout="scroll">
                <Column field="id" header="ID" style={{ width: '4rem' }} />
                <Column field="tipo" header="Tipo" style={{ width: '7rem' }} />
                <Column field="funcionarioId" header="Func." style={{ width: '5rem' }} />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column field="protocolo" header="Protocolo" />
                <Column field="recibo" header="Recibo/estado" />
                <Column header="" body={(r) => (
                    <div className="flex gap-1 flex-wrap">
                        {(r.status === 'PENDENTE' || r.status === 'FALHA') && (
                            <Button label="Transmitir" size="small" icon="pi pi-send" onClick={() => transmitir(r.id)} disabled={busy} />
                        )}
                        {(r.status === 'ENVIADO' || r.status === 'PROCESSADO' || r.status === 'RECUSADO') && (
                            <Button label="Consultar" size="small" icon="pi pi-refresh" severity="help" onClick={() => consultar(r.id)} disabled={busy} />
                        )}
                        {r.status !== 'ENVIADO' && r.status !== 'PROCESSADO' && (
                            <Button icon="pi pi-trash" rounded text severity="danger" onClick={() => excluir(r.id)} />
                        )}
                    </div>
                )} style={{ width: '14rem' }} />
            </DataTable>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header="Novo evento" modal style={{ width: 'min(96vw, 520px)' }}>
                <div className="grid p-fluid">
                    <div className="col-12">
                        <label className="bc-label">Evento</label>
                        <Dropdown value={f.tipo} options={tiposOpts} onChange={(e) => setF({ ...f, tipo: e.value })} filter className="w-full" />
                    </div>
                    <div className="col-12">
                        <label className="bc-label">Funcionário</label>
                        <Dropdown value={f.funcionarioId} options={funcs.map((x) => ({ label: x.nome || String(x.id), value: x.id }))}
                            onChange={(e) => setF({ ...f, funcionarioId: e.value })} filter placeholder="Opcional" showClear className="w-full" />
                    </div>
                    <div className="col-12">
                        <label className="bc-label">Payload JSON (dadosOcorrencia do JT)</label>
                        <InputTextarea value={f.payload || ''} onChange={(e) => setF({ ...f, payload: e.target.value })} rows={5} className="w-full font-mono text-sm" />
                    </div>
                </div>
                <div className="flex justify-content-end gap-2 mt-3">
                    <Button label="Cancelar" text severity="secondary" onClick={() => setDlg(false)} />
                    <Button label="Registrar" icon="pi pi-check" onClick={salvar} loading={busy} />
                </div>
            </Dialog>
        </div>
    );
};
export default Esocial;
