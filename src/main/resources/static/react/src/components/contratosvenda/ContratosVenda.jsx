import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useTranslation } from 'react-i18next';

const BASE = '/api/vendas/contratos';
const money = (v, locale) => Number(v || 0).toLocaleString(locale, { style: 'currency', currency: 'BRL' });

export const ContratosVenda = () => {
    const { t, i18n } = useTranslation();
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [resumo, setResumo] = useState({});
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState({ titulo: '', clienteId: null, valor: 0 });
    const carregar = async () => {
        const [r, s] = await Promise.all([
            apiFetch(BASE).then((x) => x.json()),
            apiFetch(BASE + '/resumo').then((x) => x.json()),
        ]);
        setRows(Array.isArray(r) ? r : []);
        setResumo(s || {});
    };
    useEffect(() => { carregar().catch((e) => toast.current?.show({ severity: 'error', summary: t('common.error'), detail: t('salesContracts.loadError'), life: 4000 })); }, []);
    const salvar = async () => {
        if (!form.titulo || !form.clienteId) {
            toast.current?.show({ severity: 'warn', summary: t('common.error'), detail: t('salesContracts.required'), life: 3000 });
            return;
        }
        try {
            const response = await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form) });
            if (!response.ok) throw new Error(t('salesContracts.saveError'));
            setDlg(false);
            carregar().catch(() => toast.current?.show({ severity: 'error', summary: t('common.error'), detail: t('salesContracts.loadError'), life: 4000 }));
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: t('common.error'), detail: e.message || t('salesContracts.saveError'), life: 4000 });
        }
    };
    const acao = async (id, path) => {
        try {
            const response = await apiFetch(BASE + '/' + id + '/' + path, { method: 'POST' });
            if (!response.ok) throw new Error(t('salesContracts.actionError'));
            carregar().catch(() => toast.current?.show({ severity: 'error', summary: t('common.error'), detail: t('salesContracts.loadError'), life: 4000 }));
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: t('common.error'), detail: e.message || t('salesContracts.actionError'), life: 4000 });
        }
    };
    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between mb-3"><h2 className="m-0">{t('salesContracts.title')}</h2>
                <Button label={t('salesContracts.new')} icon="pi pi-plus" onClick={() => setDlg(true)} /></div>
            <div className="grid mb-3">
                <div className="col-4"><Card><small>{t('salesContracts.draft')}</small><div className="text-xl font-bold">{resumo.rascunho ?? 0}</div></Card></div>
                <div className="col-4"><Card><small>{t('salesContracts.active')}</small><div className="text-xl font-bold">{resumo.ativos ?? 0}</div></Card></div>
                <div className="col-4"><Card><small>{t('salesContracts.closed')}</small><div className="text-xl font-bold">{resumo.encerrados ?? 0}</div></Card></div>
            </div>
            <DataTable value={rows} paginator rows={12} emptyMessage={t('salesContracts.empty')}>
                <Column field="numero" header={t('salesContracts.number')} /><Column field="titulo" header={t('salesContracts.contractTitle')} />
                <Column field="clienteId" header={t('salesContracts.client')} /><Column field="valor" header={t('salesContracts.value')} body={(r) => money(r.valor, i18n.language)} />
                <Column field="status" header={t('salesContracts.status')} body={(r) => <Tag value={r.status === 'RASCUNHO' ? t('salesContracts.draft') : r.status === 'ATIVO' ? t('salesContracts.active') : r.status === 'ENCERRADO' ? t('salesContracts.closed') : r.status} />} />
                <Column header={t('salesContracts.actions')} body={(r) => (
                    <span className="flex gap-1">
                        {r.status === 'RASCUNHO' && <Button label={t('salesContracts.activate')} size="small" text onClick={() => acao(r.id, 'ativar')} />}
                        {r.status === 'ATIVO' && <Button label={t('salesContracts.close')} size="small" text onClick={() => acao(r.id, 'encerrar')} />}
                    </span>
                )} />
            </DataTable>
            <Dialog header={t('salesContracts.dialogTitle')} visible={dlg} onHide={() => setDlg(false)} style={{ width: '420px' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>{t('salesContracts.titleLabel')}</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-12"><label>{t('salesContracts.clientId')}</label><InputNumber value={form.clienteId} onValueChange={(e) => setForm({ ...form, clienteId: e.value })} /></div>
                    <div className="col-12"><label>{t('salesContracts.value')}</label><InputNumber value={form.valor} onValueChange={(e) => setForm({ ...form, valor: e.value })} mode="currency" currency="BRL" locale={i18n.language} /></div>
                </div>
                <Button label={t('salesContracts.save')} className="mt-3" onClick={salvar} />
            </Dialog>
        </div>
    );
};
export default ContratosVenda;
