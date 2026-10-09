import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/agenda';
const TIPOS = ['REUNIAO', 'LIGACAO', 'VISITA', 'TAREFA', 'OUTRO'];

export const Agenda = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filtro, setFiltro] = useState('semana');
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState({ titulo: '', tipo: 'REUNIAO', inicio: new Date(), localEvento: '', responsavel: '', descricao: '' });

    const js = async (r) => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    };

    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const path = filtro === 'hoje' ? '/eventos/hoje' : filtro === 'semana' ? '/eventos/semana' : '/eventos';
            const list = await apiFetch(BASE + path).then(js);
            setRows(Array.isArray(list) ? list : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    }, [filtro]);

    useEffect(() => { carregar(); }, [carregar]);

    const salvar = async () => {
        if (!form.titulo?.trim()) return;
        const body = {
            ...form,
            inicio: form.inicio instanceof Date ? form.inicio.toISOString() : form.inicio,
        };
        await apiFetch(BASE + '/eventos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        }).then(js);
        setDlg(false);
        carregar();
    };

    const concluir = async (id) => {
        await apiFetch(BASE + '/eventos/' + id + '/concluir', { method: 'POST' }).then(js);
        carregar();
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Agenda</h2>
                    <span className="text-color-secondary">Reuniões, visitas e tarefas operacionais</span>
                </div>
                <div className="flex gap-2">
                    <Dropdown value={filtro} options={[
                        { label: 'Hoje', value: 'hoje' },
                        { label: 'Semana', value: 'semana' },
                        { label: 'Todos', value: 'todos' },
                    ]} onChange={(e) => setFiltro(e.value)} />
                    <Button label="Novo evento" icon="pi pi-plus" onClick={() => setDlg(true)} />
                </div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} dataKey="id" emptyMessage="Sem eventos">
                <Column field="titulo" header="Título" />
                <Column field="tipo" header="Tipo" />
                <Column field="inicio" header="Início" body={(r) => r.inicio ? new Date(r.inicio).toLocaleString('pt-BR') : '—'} />
                <Column field="localEvento" header="Local" />
                <Column field="responsavel" header="Responsável" />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} />} />
                <Column body={(r) => r.status === 'AGENDADO' ? (
                    <Button label="Concluir" size="small" text icon="pi pi-check" onClick={() => concluir(r.id)} />
                ) : null} />
            </DataTable>
            <Dialog header="Novo evento" visible={dlg} onHide={() => setDlg(false)} style={{ width: 'min(96vw, 520px)' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>Título *</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-6"><label>Tipo</label><Dropdown value={form.tipo} options={TIPOS.map((t) => ({ label: t, value: t }))} onChange={(e) => setForm({ ...form, tipo: e.value })} /></div>
                    <div className="col-6"><label>Início</label><Calendar value={form.inicio} onChange={(e) => setForm({ ...form, inicio: e.value })} showTime hourFormat="24" /></div>
                    <div className="col-6"><label>Local</label><InputText value={form.localEvento} onChange={(e) => setForm({ ...form, localEvento: e.target.value })} /></div>
                    <div className="col-6"><label>Responsável</label><InputText value={form.responsavel} onChange={(e) => setForm({ ...form, responsavel: e.target.value })} /></div>
                    <div className="col-12"><label>Descrição</label><InputTextarea rows={2} value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></div>
                </div>
                <Button label="Salvar" icon="pi pi-check" className="mt-3" onClick={salvar} />
            </Dialog>
        </div>
    );
};
export default Agenda;
