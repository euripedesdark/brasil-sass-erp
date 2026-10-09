import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/core/notificacoes';

export const Notificacoes = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [cnt, setCnt] = useState(0);
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState({ titulo: '', mensagem: '', tipo: 'INFO' });
    const carregar = async () => {
        const [r, c] = await Promise.all([
            apiFetch(BASE).then((x) => x.json()),
            apiFetch(BASE + '/contador').then((x) => x.json()),
        ]);
        setRows(Array.isArray(r) ? r : []);
        setCnt(c?.naoLidas ?? 0);
    };
    useEffect(() => { carregar().catch((e) => toast.current?.show({ severity: 'error', detail: e.message })); }, []);
    const ler = async (id) => { await apiFetch(BASE + '/' + id + '/lida', { method: 'POST' }); carregar(); };
    const lerTodas = async () => { await apiFetch(BASE + '/ler-todas', { method: 'POST' }); carregar(); };
    const criar = async () => {
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form) });
        setDlg(false); carregar();
    };
    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between mb-3">
                <div><h2 className="m-0">Notificações</h2><span className="text-color-secondary">{cnt} não lida(s)</span></div>
                <div className="flex gap-2">
                    <Button label="Marcar todas lidas" outlined onClick={lerTodas} />
                    <Button label="Nova" icon="pi pi-plus" onClick={() => setDlg(true)} />
                </div>
            </div>
            <DataTable value={rows} paginator rows={15} emptyMessage="Sem notificações">
                <Column field="titulo" header="Título" />
                <Column field="tipo" header="Tipo" />
                <Column field="lida" header="Lida" body={(r) => <Tag value={r.lida ? 'Sim' : 'Não'} severity={r.lida ? 'success' : 'warning'} />} />
                <Column body={(r) => !r.lida ? <Button icon="pi pi-check" text size="small" onClick={() => ler(r.id)} /> : null} />
            </DataTable>
            <Dialog header="Nova notificação" visible={dlg} onHide={() => setDlg(false)} style={{ width: '420px' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>Título</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-12"><label>Mensagem</label><InputTextarea rows={3} value={form.mensagem} onChange={(e) => setForm({ ...form, mensagem: e.target.value })} /></div>
                </div>
                <Button label="Enviar" className="mt-3" onClick={criar} />
            </Dialog>
        </div>
    );
};
export default Notificacoes;
