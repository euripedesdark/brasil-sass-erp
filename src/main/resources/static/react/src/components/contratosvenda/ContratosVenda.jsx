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

const BASE = '/api/vendas/contratos';
const money = (v) => Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const ContratosVenda = () => {
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
    useEffect(() => { carregar().catch((e) => toast.current?.show({ severity: 'error', detail: e.message })); }, []);
    const salvar = async () => {
        if (!form.titulo || !form.clienteId) {
            toast.current?.show({ severity: 'warn', detail: 'Título e clienteId obrigatórios', life: 3000 });
            return;
        }
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form) });
        setDlg(false); carregar();
    };
    const acao = async (id, path) => {
        await apiFetch(BASE + '/' + id + '/' + path, { method: 'POST' });
        carregar();
    };
    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between mb-3"><h2 className="m-0">Contratos comerciais (venda)</h2>
                <Button label="Novo" icon="pi pi-plus" onClick={() => setDlg(true)} /></div>
            <div className="grid mb-3">
                <div className="col-4"><Card><small>Rascunho</small><div className="text-xl font-bold">{resumo.rascunho ?? 0}</div></Card></div>
                <div className="col-4"><Card><small>Ativos</small><div className="text-xl font-bold">{resumo.ativos ?? 0}</div></Card></div>
                <div className="col-4"><Card><small>Encerrados</small><div className="text-xl font-bold">{resumo.encerrados ?? 0}</div></Card></div>
            </div>
            <DataTable value={rows} paginator rows={12} emptyMessage="Sem contratos">
                <Column field="numero" header="Nº" /><Column field="titulo" header="Título" />
                <Column field="clienteId" header="Cliente" /><Column field="valor" header="Valor" body={(r) => money(r.valor)} />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} />} />
                <Column body={(r) => (
                    <span className="flex gap-1">
                        {r.status === 'RASCUNHO' && <Button label="Ativar" size="small" text onClick={() => acao(r.id, 'ativar')} />}
                        {r.status === 'ATIVO' && <Button label="Encerrar" size="small" text onClick={() => acao(r.id, 'encerrar')} />}
                    </span>
                )} />
            </DataTable>
            <Dialog header="Contrato" visible={dlg} onHide={() => setDlg(false)} style={{ width: '420px' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>Título</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-12"><label>Cliente ID</label><InputNumber value={form.clienteId} onValueChange={(e) => setForm({ ...form, clienteId: e.value })} /></div>
                    <div className="col-12"><label>Valor</label><InputNumber value={form.valor} onValueChange={(e) => setForm({ ...form, valor: e.value })} mode="currency" currency="BRL" locale="pt-BR" /></div>
                </div>
                <Button label="Salvar" className="mt-3" onClick={salvar} />
            </Dialog>
        </div>
    );
};
export default ContratosVenda;
