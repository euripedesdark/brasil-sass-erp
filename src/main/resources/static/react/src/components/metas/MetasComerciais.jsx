import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';

const BASE = '/api/vendas/metas';
const money = (v) => Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const MetasComerciais = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [resumo, setResumo] = useState({});
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState({ ano: new Date().getFullYear(), mes: new Date().getMonth() + 1, valorMeta: 0, canal: '' });
    const carregar = async () => {
        const [r, s] = await Promise.all([
            apiFetch(BASE).then((x) => x.json()),
            apiFetch(BASE + '/resumo').then((x) => x.json()),
        ]);
        setRows(Array.isArray(r) ? r : []);
        setResumo(s || {});
    };
    useEffect(() => { carregar().catch((e) => toast.current?.show({ severity: 'error', detail: e.message })); }, []);
    const salvar = async () => {
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form) });
        setDlg(false);
        carregar();
    };
    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between mb-3"><div><h2 className="m-0">Metas comerciais</h2></div>
                <Button label="Nova meta" icon="pi pi-plus" onClick={() => setDlg(true)} /></div>
            <div className="grid mb-3">
                <div className="col-4"><Card><small>Meta</small><div className="text-xl font-bold">{money(resumo.valorMeta)}</div></Card></div>
                <div className="col-4"><Card><small>Realizado</small><div className="text-xl font-bold">{money(resumo.valorRealizado)}</div></Card></div>
                <div className="col-4"><Card><small>Atingimento</small><div className="text-xl font-bold">{resumo.atingimentoPct ?? 0}%</div></Card></div>
            </div>
            <DataTable value={rows} paginator rows={12} emptyMessage="Sem metas">
                <Column field="ano" header="Ano" /><Column field="mes" header="Mês" /><Column field="canal" header="Canal" />
                <Column field="valorMeta" header="Meta" body={(r) => money(r.valorMeta)} />
                <Column field="valorRealizado" header="Realizado" body={(r) => money(r.valorRealizado)} />
            </DataTable>
            <Dialog header="Nova meta" visible={dlg} onHide={() => setDlg(false)} style={{ width: '400px' }}>
                <div className="grid p-fluid">
                    <div className="col-6"><label>Ano</label><InputNumber value={form.ano} onValueChange={(e) => setForm({ ...form, ano: e.value })} /></div>
                    <div className="col-6"><label>Mês</label><InputNumber value={form.mes} onValueChange={(e) => setForm({ ...form, mes: e.value })} /></div>
                    <div className="col-12"><label>Canal</label><InputText value={form.canal} onChange={(e) => setForm({ ...form, canal: e.target.value })} /></div>
                    <div className="col-12"><label>Valor meta</label><InputNumber value={form.valorMeta} onValueChange={(e) => setForm({ ...form, valorMeta: e.value })} mode="currency" currency="BRL" locale="pt-BR" /></div>
                </div>
                <Button label="Salvar" className="mt-3" onClick={salvar} />
            </Dialog>
        </div>
    );
};
export default MetasComerciais;
