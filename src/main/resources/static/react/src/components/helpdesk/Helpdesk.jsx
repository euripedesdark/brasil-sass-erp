import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';

const BASE = '/api/helpdesk';
const PRIORIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'];
const STATUS = ['ABERTO', 'EM_ANDAMENTO', 'AGUARDANDO', 'RESOLVIDO', 'FECHADO', 'CANCELADO'];

export const Helpdesk = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [resumo, setResumo] = useState({});
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState({ titulo: '', descricao: '', prioridade: 'MEDIA', categoria: '', solicitante: '' });
    const [comentDlg, setComentDlg] = useState(false);
    const [comentarios, setComentarios] = useState([]);
    const [chamadoId, setChamadoId] = useState(null);
    const [texto, setTexto] = useState('');

    const js = async (r) => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.status === 204 ? null : r.json();
    };

    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [c, s] = await Promise.all([
                apiFetch(BASE + '/chamados').then(js),
                apiFetch(BASE + '/resumo').then(js),
            ]);
            setRows(Array.isArray(c) ? c : []);
            setResumo(s || {});
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    const salvar = async () => {
        if (!form.titulo?.trim()) {
            toast.current?.show({ severity: 'warn', summary: 'Título obrigatório', life: 2500 });
            return;
        }
        await apiFetch(BASE + '/chamados', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(form),
        }).then(js);
        setDlg(false);
        setForm({ titulo: '', descricao: '', prioridade: 'MEDIA', categoria: '', solicitante: '' });
        carregar();
    };

    const status = async (id, st) => {
        await apiFetch(BASE + '/chamados/' + id + '/status?status=' + st, { method: 'POST' }).then(js);
        carregar();
    };

    const abrirComents = async (id) => {
        setChamadoId(id);
        const list = await apiFetch(BASE + '/chamados/' + id + '/comentarios').then(js);
        setComentarios(Array.isArray(list) ? list : []);
        setComentDlg(true);
    };

    const enviarComent = async () => {
        await apiFetch(BASE + '/chamados/' + chamadoId + '/comentarios', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ texto }),
        }).then(js);
        setTexto('');
        abrirComents(chamadoId);
    };

    const sev = (s) => {
        if (s === 'ABERTO') return 'danger';
        if (s === 'EM_ANDAMENTO') return 'warning';
        if (s === 'RESOLVIDO' || s === 'FECHADO') return 'success';
        return 'info';
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 className="m-0">Helpdesk</h2>
                    <span className="text-color-secondary">Chamados de suporte interno / cliente</span>
                </div>
                <Button label="Novo chamado" icon="pi pi-plus" onClick={() => setDlg(true)} />
            </div>
            <div className="grid mb-3">
                {['abertos', 'emAndamento', 'resolvidos', 'fechados'].map((k) => (
                    <div className="col-6 md:col-3" key={k}>
                        <Card><small>{k}</small><div className="text-2xl font-bold">{resumo[k] ?? 0}</div></Card>
                    </div>
                ))}
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} dataKey="id" emptyMessage="Sem chamados">
                <Column field="numero" header="Nº" />
                <Column field="titulo" header="Título" />
                <Column field="prioridade" header="Prioridade" />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column field="solicitante" header="Solicitante" />
                <Column body={(r) => (
                    <span className="flex gap-1 flex-wrap">
                        <Button icon="pi pi-comments" size="small" text onClick={() => abrirComents(r.id)} />
                        <Dropdown
                            options={STATUS.map((s) => ({ label: s, value: s }))}
                            value={r.status}
                            onChange={(e) => status(r.id, e.value)}
                            className="w-10rem"
                        />
                    </span>
                )} />
            </DataTable>
            <Dialog header="Novo chamado" visible={dlg} onHide={() => setDlg(false)} style={{ width: 'min(96vw, 520px)' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>Título *</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-12"><label>Descrição</label><InputTextarea rows={3} value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></div>
                    <div className="col-6"><label>Prioridade</label><Dropdown value={form.prioridade} options={PRIORIDADES.map((p) => ({ label: p, value: p }))} onChange={(e) => setForm({ ...form, prioridade: e.value })} /></div>
                    <div className="col-6"><label>Categoria</label><InputText value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })} /></div>
                    <div className="col-12"><label>Solicitante</label><InputText value={form.solicitante} onChange={(e) => setForm({ ...form, solicitante: e.target.value })} /></div>
                </div>
                <Button label="Abrir chamado" icon="pi pi-check" className="mt-3" onClick={salvar} />
            </Dialog>
            <Dialog header="Comentários" visible={comentDlg} onHide={() => setComentDlg(false)} style={{ width: 'min(96vw, 480px)' }}>
                <DataTable value={comentarios} size="small" emptyMessage="Sem comentários">
                    <Column field="autor" header="Autor" />
                    <Column field="texto" header="Texto" />
                </DataTable>
                <InputTextarea className="w-full mt-3" rows={2} value={texto} onChange={(e) => setTexto(e.target.value)} placeholder="Novo comentário" />
                <Button label="Enviar" icon="pi pi-send" className="mt-2" onClick={enviarComent} disabled={!texto.trim()} />
            </Dialog>
        </div>
    );
};
export default Helpdesk;
