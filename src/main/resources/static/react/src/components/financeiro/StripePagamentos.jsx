import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const BASE = '/api/financeiro/stripe';

export const StripePagamentos = () => {
    const { t, i18n } = useTranslation();
    const toast = useRef(null);
    const navigate = useNavigate();
    const [rows, setRows] = useState([]);
    const [status, setStatus] = useState(null);
    const [hooks, setHooks] = useState([]);
    const [loading, setLoading] = useState(true);

    const carregar = async () => {
        setLoading(true);
        try {
            const [p, s, h] = await Promise.all([
                apiFetch(BASE + '/pagamentos').then(async (r) => {
                    if (!r.ok) throw new Error('HTTP ' + r.status);
                    return r.json();
                }),
                apiFetch(BASE + '/status').then(async (r) => (r.ok ? r.json() : null)).catch(() => null),
                apiFetch(BASE + '/webhooks').then(async (r) => (r.ok ? r.json() : [])).catch(() => []),
            ]);
            setRows(Array.isArray(p) ? p : []);
            setStatus(s);
            setHooks(Array.isArray(h) ? h : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: t('legacyUi.stripePayments.error'), detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    const money = (cents, cur) => {
        if (cents == null) return '—';
        return (Number(cents) / 100).toLocaleString(i18n.language, { style: 'currency', currency: (cur || 'brl').toUpperCase() });
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
                    <h2 className="m-0">{t('legacyUi.stripePayments.title')}</h2>
                    <span className="text-color-secondary">{t('legacyUi.stripePayments.subtitle')}</span>
                </div>
                <div className="flex gap-2">
                    <Button label={t('legacyUi.stripePayments.credentials')} icon="pi pi-cog" outlined onClick={() => navigate('/configurar-empresa')} />
                    <Button label={t('legacyUi.stripePayments.refresh')} icon="pi pi-refresh" onClick={carregar} />
                </div>
            </div>
            {status && (
                <Card className="mb-3">
                    <div className="flex flex-wrap gap-4 align-items-center">
                        <Tag value={status.habilitado ? t('legacyUi.stripePayments.enabled') : t('legacyUi.stripePayments.disabled')} severity={status.habilitado ? 'success' : 'danger'} />
                        {status.chaveMascarada && <span>{t('legacyUi.stripePayments.key', { key: status.chaveMascarada })}</span>}
                        <span>{t('legacyUi.stripePayments.records', { count: status.pagamentos ?? rows.length })}</span>
                        {status.motivo && <span className="text-color-secondary">{status.motivo}</span>}
                    </div>
                    {status.webhookPath && (
                        <div className="mt-3 text-sm">
                            <b>{t('legacyUi.stripePayments.webhook')}</b>
                            <div><code>{typeof window !== 'undefined' ? window.location.origin : ''}{status.webhookPath}</code></div>
                            <div className="text-color-secondary">{status.webhookHint}</div>
                        </div>
                    )}
                </Card>
            )}
            <DataTable value={rows} loading={loading} paginator rows={15} dataKey="id" emptyMessage={t('legacyUi.stripePayments.emptyPayments')}>
                <Column field="id" header="ID" style={{ width: '4rem' }} />
                <Column field="tituloId" header={t('legacyUi.stripePayments.titleId')} />
                <Column header={t('legacyUi.stripePayments.amount')} body={(r) => money(r.amount, r.currency)} />
                <Column field="status" header={t('legacyUi.stripePayments.status')} body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column field="checkoutSessionId" header={t('legacyUi.stripePayments.session')} style={{ maxWidth: '10rem' }} />
                <Column field="invoiceId" header={t('legacyUi.stripePayments.invoice')} />
                <Column header={t('legacyUi.stripePayments.url')} body={(r) => r.checkoutUrl ? <a href={r.checkoutUrl} target="_blank" rel="noreferrer">{t('legacyUi.stripePayments.open')}</a> : '—'} />
            </DataTable>
            <h3 className="mt-4">{t('legacyUi.stripePayments.webhookEvents')}</h3>
            <DataTable value={hooks} emptyMessage={t('legacyUi.stripePayments.noEvents')} size="small" paginator rows={10}>
                <Column field="id" header="ID" style={{ width: '4rem' }} />
                <Column field="stripeEventId" header={t('legacyUi.stripePayments.eventId')} />
                <Column field="eventType" header={t('legacyUi.stripePayments.type')} />
            </DataTable>
        </div>
    );
};
export default StripePagamentos;
