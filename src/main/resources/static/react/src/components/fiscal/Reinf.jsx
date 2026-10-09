import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';

const BASE = '/api/fiscal/reinf';

const competenciaAtual = () => {
    const d = new Date();
    return String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear();
};

export const Reinf = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [comp, setComp] = useState(competenciaAtual);
    const [busy, setBusy] = useState(false);
    const [payloadDlg, setPayloadDlg] = useState(false);
    const [payloadTxt, setPayloadTxt] = useState('');

    const js = async (r) => {
        if (!r.ok) {
            const err = await r.json().catch(() => ({}));
            throw new Error(err?.message || err?.erro || ('HTTP ' + r.status));
        }
        const j = await r.json().catch(() => null);
        return Array.isArray(j) ? j : (j?.data ?? j);
    };

    const carregar = async () => {
        setLoading(true);
        try {
            const q = comp ? ('?competencia=' + encodeURIComponent(comp)) : '';
            const a = await apiFetch(BASE + q).then(js);
            setRows(Array.isArray(a) ? a : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, [comp]);

    const money = (v) => {
        if (v == null) return '—';
        const n = Number(v);
        if (Number.isNaN(n)) return String(v);
        return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    const gerar = async () => {
        if (!comp) return;
        setBusy(true);
        try {
            await apiFetch(BASE + '/gerar?competencia=' + encodeURIComponent(comp), { method: 'POST' }).then(js);
            toast.current?.show({ severity: 'success', summary: 'R-2010 e R-2020 gerados', life: 3000 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const fechar = async () => {
        if (!comp) return;
        setBusy(true);
        try {
            await apiFetch(BASE + '/fechar?competencia=' + encodeURIComponent(comp), { method: 'POST' }).then(js);
            toast.current?.show({ severity: 'success', summary: 'R-2099 fechado', life: 3000 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const verPayload = async (id) => {
        try {
            const d = await apiFetch(BASE + '/' + id).then(js);
            let txt = d.payload || '';
            try { txt = JSON.stringify(JSON.parse(txt), null, 2); } catch { /* keep raw */ }
            setPayloadTxt(txt);
            setPayloadDlg(true);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        }
    };

    const sev = (s) => {
        if (s === 'TRANSMITIDO') return 'success';
        if (s === 'FECHADO') return 'info';
        if (s === 'GERADO') return 'warning';
        return 'secondary';
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">EFD-Reinf</h2>
                    <span className="bc-muted">
                        Gera R-2020 (serviços prestados) e R-2010 (tomados) a partir das NFS-e. Fechamento R-2099 local — sem transmissão à RFB.
                    </span>
                </div>
                <div className="flex gap-2 flex-wrap align-items-center">
                    <InputText value={comp} onChange={(e) => setComp(e.target.value)} placeholder="MM/AAAA" style={{ width: '7.5rem' }} />
                    <Button label="Gerar período" icon="pi pi-file" loading={busy} onClick={gerar} />
                    <Button label="Fechar (R-2099)" icon="pi pi-lock" severity="help" loading={busy} onClick={fechar} />
                </div>
            </div>

            <Card className="mb-3 shadow-1">
                <div className="text-sm text-color-secondary">
                    Ambiente de produção da RFB <strong>não</strong> é chamado. O protocolo gravado é local
                    (prefixo LOCAL-). Use o payload para validar volumes antes de integrar a transmissão.
                </div>
            </Card>

            <DataTable value={rows} loading={loading} paginator rows={12}
                emptyMessage="Nenhum evento. Informe a competência e clique em Gerar período."
                responsiveLayout="scroll">
                <Column field="competencia" header="Competência" style={{ width: '8rem' }} />
                <Column field="evento" header="Evento" style={{ width: '7rem' }} />
                <Column field="totalDocs" header="Docs" style={{ width: '5rem' }} />
                <Column field="valorTotal" header="Valor" body={(r) => money(r.valorTotal)} />
                <Column field="protocolo" header="Protocolo" />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column header="" body={(r) => (
                    <Button icon="pi pi-eye" size="small" text tooltip="Ver payload" onClick={() => verPayload(r.id)} />
                )} style={{ width: '4rem' }} />
            </DataTable>

            <Dialog header="Payload do evento" visible={payloadDlg} onHide={() => setPayloadDlg(false)}
                style={{ width: 'min(96vw, 640px)' }} maximizable>
                <pre className="m-0 p-3 surface-ground border-round overflow-auto" style={{ maxHeight: '60vh', fontSize: '0.8rem' }}>
                    {payloadTxt}
                </pre>
            </Dialog>
        </div>
    );
};

export default Reinf;
