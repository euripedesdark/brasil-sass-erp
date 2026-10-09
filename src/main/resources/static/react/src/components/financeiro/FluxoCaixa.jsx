import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { useTranslation } from 'react-i18next';

const fmt = (v, locale) => Number(v ?? 0).toLocaleString(locale, { style: 'currency', currency: 'BRL' });

export const FluxoCaixa = () => {
    const { t, i18n } = useTranslation();
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [dias, setDias] = useState(90);
    const [loading, setLoading] = useState(true);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? []); };
    const carregar = async (d) => {
        setLoading(true);
        try { setRows(await apiFetch('/api/financeiro/fluxo-caixa?dias=' + (d ?? dias)).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: t('common.error'), detail: t('cashFlow.loadError'), life: 4000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(90); }, []);
    const tot = (k) => rows.reduce((s, r) => s + Number(r[k] || 0), 0);
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>{t('cashFlow.title')}</h2><span className='bc-muted'>{t('cashFlow.subtitle')}</span></div>
                <div className='flex gap-2 align-items-end'><Dropdown value={dias} options={[30, 60, 90, 180].map((d) => ({ label: t('cashFlow.days', { count: d }), value: d }))} onChange={(e) => { setDias(e.value); carregar(e.value); }} /></div>
            </div>
            <div className='grid mb-3'>
                <div className='bc-form-col-4 col-12 md:col-4'><div className='p-3 border-round surface-card'><small>{t('cashFlow.inflows')}</small><div className='text-2xl font-bold' style={{ color: 'var(--green-500)' }}>{fmt(tot('entradas'), i18n.language)}</div></div></div>
                <div className='bc-form-col-4 col-12 md:col-4'><div className='p-3 border-round surface-card'><small>{t('cashFlow.outflows')}</small><div className='text-2xl font-bold' style={{ color: 'var(--red-500)' }}>{fmt(tot('saidas'), i18n.language)}</div></div></div>
                <div className='bc-form-col-4 col-12 md:col-4'><div className='p-3 border-round surface-card'><small>{t('cashFlow.net')}</small><div className='text-2xl font-bold'>{fmt(tot('entradas') - tot('saidas'), i18n.language)}</div></div></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={14} emptyMessage={t('cashFlow.empty')} responsiveLayout='scroll'>
                <Column field='semana' header={t('cashFlow.week')} style={{ width: '7rem' }} />
                <Column header={t('cashFlow.inflows')} body={(r) => fmt(r.entradas, i18n.language)} />
                <Column header={t('cashFlow.outflows')} body={(r) => fmt(r.saidas, i18n.language)} />
                <Column header={t('cashFlow.net')} body={(r) => fmt(r.liquido, i18n.language)} />
                <Column header={t('cashFlow.accumulated')} body={(r) => fmt(r.acumulado, i18n.language)} />
            </DataTable>
        </div>
    );
};
export default FluxoCaixa;
