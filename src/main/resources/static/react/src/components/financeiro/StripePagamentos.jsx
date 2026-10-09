import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';
import { useNavigate } from 'react-router-dom';

const BASE = '/api/financeiro/stripe';

export const StripePagamentos = () => {
    const toast = useRef(null);
    const navigate = useNavigate();
    const [rows, setRows] = useState([]);
    const [status, setStatus] = useState(null);
    const [loading, setLoading] = useState(true);

    const carregar = async () => {
        setLoading(true);
        try {
            const [p, s] = await Promise.all([
                apiFetch(BASE + '/pagamentos').then(async (r) => {
                    if (!r.ok) throw new Error('HTTP ' + r.status);
                    return r.json();
                }),
                apiFetch(BASE + '/status').then(async (r) => (r.ok ? r.json() : null)).catch(() => null),
            ]);
            setRows(Array.isArray(p) ? p : []);
            setStatus(s);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    const money = (cents, cur) => {
        if (cents == null) return '—';
        return (Number(cents) / 100).toLocaleString('pt-BR', { style: 'currency', currency: (cur || 'brl').toUpperCase() });
    };

    const sev = (s) => {
        if (!s) return 'info';
        const u = s.toUpperCase();
        if (u.includes('COMPLETE') || u.includes('PAID') || u.includes('SUCCEEDED')) return 'success';
        if (u.includes('OPEN') || u.includes('PENDING')) return 'warning';
        if (u.includes('FAIL') || u.includes('CANCEL') || u.includes('EXPIRE')) return 'danger';
        return 'info';
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Stripe — pagamentos</h2>
                    <span className="text-color-secondary">Checkout e invoices ligados a títulos a receber</span>
                </div>
                <div className="flex gap-2">
                    <Button label="Configurar empresa" icon="pi pi-cog" outlined onClick={() => navigate('/configurar-empresa')} />
                    <Button label="Atualizar" icon="pi pi-refresh" onClick={carregar} />
                </div>
            </div>
            {status && (
                <Card className="mb-3">
                    <div className="flex flex-wrap gap-4 align-items-center">
                        <Tag value={status.habilitado ? 'Stripe habilitado' : 'Stripe desabilitado'} severity={status.habilitado ? 'success' : 'danger'} />
                        {status.chaveMascarada && <span>Chave: {status.chaveMascarada}</span>}
                        <span>Registros: {status.pagamentos ?? rows.length}</span>
                        {status.motivo && <span className="text-color-secondary">{status.motivo}</span>}
                    </div>
                    {status.webhookPath && (
                        <div className="mt-3 text-sm">
                            <b>Webhook (Dashboard Stripe):</b>
                            <div><code>{typeof window !== 'undefined' ? window.location.origin : ''}{status.webhookPath}</code></div>
                            <div className="text-color-secondary">{status.webhookHint}</div>
                        </div>
                    )}
                </Card>
            )}
            <DataTable value={rows} loading={loading} paginator rows={15} dataKey="id" emptyMessage="Nenhum pagamento Stripe. Use o botão de cartão nos títulos a receber.">
                <Column field="id" header="ID" style={{ width: '4rem' }} />
                <Column field="tituloId" header="Título" />
                <Column header="Valor" body={(r) => money(r.amount, r.currency)} />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column field="checkoutSessionId" header="Session" style={{ maxWidth: '10rem' }} />
                <Column field="invoiceId" header="Invoice" />
                <Column header="URL" body={(r) => r.checkoutUrl ? <a href={r.checkoutUrl} target="_blank" rel="noreferrer">Abrir</a> : '—'} />
            </DataTable>
        </div>
    );
};
export default StripePagamentos;
