import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Toast } from 'primereact/toast';

const BASE = '/api/conhecimento/artigos';

export const Conhecimento = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [q, setQ] = useState('');
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState({ titulo: '', categoria: '', conteudo: '', tags: '' });
    const carregar = async () => {
        const url = q ? BASE + '?q=' + encodeURIComponent(q) : BASE;
        const r = await apiFetch(url).then((x) => x.json());
        setRows(Array.isArray(r) ? r : []);
    };
    useEffect(() => { carregar().catch((e) => toast.current?.show({ severity: 'error', detail: e.message })); }, []);
    const salvar = async () => {
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form) });
        setDlg(false); carregar();
    };
    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between mb-3 flex-wrap gap-2">
                <h2 className="m-0">Base de conhecimento</h2>
                <div className="flex gap-2">
                    <InputText value={q} onChange={(e) => setQ(e.target.value)} placeholder="Buscar..." />
                    <Button icon="pi pi-search" onClick={carregar} />
                    <Button label="Novo artigo" icon="pi pi-plus" onClick={() => setDlg(true)} />
                </div>
            </div>
            <DataTable value={rows} paginator rows={12} emptyMessage="Sem artigos">
                <Column field="titulo" header="Título" /><Column field="categoria" header="Categoria" /><Column field="tags" header="Tags" />
            </DataTable>
            <Dialog header="Artigo" visible={dlg} onHide={() => setDlg(false)} style={{ width: 'min(96vw, 560px)' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>Título</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-6"><label>Categoria</label><InputText value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })} /></div>
                    <div className="col-6"><label>Tags</label><InputText value={form.tags} onChange={(e) => setForm({ ...form, tags: e.target.value })} /></div>
                    <div className="col-12"><label>Conteúdo</label><InputTextarea rows={6} value={form.conteudo} onChange={(e) => setForm({ ...form, conteudo: e.target.value })} /></div>
                </div>
                <Button label="Salvar" className="mt-3" onClick={salvar} />
            </Dialog>
        </div>
    );
};
export default Conhecimento;
