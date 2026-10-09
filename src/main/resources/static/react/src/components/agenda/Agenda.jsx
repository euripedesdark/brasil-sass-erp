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
import { useTranslation } from 'react-i18next';

const BASE = '/api/agenda';
const TIPOS = ['REUNIAO', 'LIGACAO', 'VISITA', 'TAREFA', 'OUTRO'];

export const Agenda = () => {
    const { t, i18n } = useTranslation();
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
            toast.current?.show({ severity: 'error', summary: t('common.error'), detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    }, [filtro, t]);

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
                    <h2 className="m-0">{t('legacyUi.agenda.title')}</h2>
                    <span className="text-color-secondary">{t('legacyUi.agenda.subtitle')}</span>
                </div>
                <div className="flex gap-2">
                    <Dropdown value={filtro} options={[
                        { label: t('legacyUi.agenda.today'), value: 'hoje' },
                        { label: t('legacyUi.agenda.week'), value: 'semana' },
                        { label: t('common.all'), value: 'todos' },
                    ]} onChange={(e) => setFiltro(e.value)} />
                    <Button label={t('legacyUi.agenda.newEvent')} icon="pi pi-plus" onClick={() => setDlg(true)} />
                </div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} dataKey="id" emptyMessage={t('legacyUi.agenda.noEvents')}>
                <Column field="titulo" header={t('common.title')} />
                <Column field="tipo" header={t('common.type')} />
                <Column field="inicio" header={t('legacyUi.agenda.start')} body={(r) => r.inicio ? new Date(r.inicio).toLocaleString(i18n.language) : '—'} />
                <Column field="localEvento" header={t('common.location')} />
                <Column field="responsavel" header={t('legacyUi.agenda.responsible')} />
                <Column field="status" header={t('common.status')} body={(r) => <Tag value={r.status} />} />
                <Column body={(r) => r.status === 'AGENDADO' ? (
                     <Button label={t('legacyUi.agenda.complete')} size="small" text icon="pi pi-check" onClick={() => concluir(r.id)} />
                ) : null} />
            </DataTable>
            <Dialog header={t('legacyUi.agenda.newEvent')} visible={dlg} onHide={() => setDlg(false)} style={{ width: 'min(96vw, 520px)' }}>
                <div className="grid p-fluid">
                    <div className="col-12"><label>{t('common.title')} *</label><InputText value={form.titulo} onChange={(e) => setForm({ ...form, titulo: e.target.value })} /></div>
                    <div className="col-6"><label>{t('common.type')}</label><Dropdown value={form.tipo} options={TIPOS.map((tipo) => ({ label: t(`legacyUi.agenda.types.${tipo}`), value: tipo }))} onChange={(e) => setForm({ ...form, tipo: e.value })} /></div>
                    <div className="col-6"><label>{t('legacyUi.agenda.start')}</label><Calendar value={form.inicio} onChange={(e) => setForm({ ...form, inicio: e.value })} showTime hourFormat="24" /></div>
                    <div className="col-6"><label>{t('common.location')}</label><InputText value={form.localEvento} onChange={(e) => setForm({ ...form, localEvento: e.target.value })} /></div>
                    <div className="col-6"><label>{t('legacyUi.agenda.responsible')}</label><InputText value={form.responsavel} onChange={(e) => setForm({ ...form, responsavel: e.target.value })} /></div>
                    <div className="col-12"><label>{t('common.description')}</label><InputTextarea rows={2} value={form.descricao} onChange={(e) => setForm({ ...form, descricao: e.target.value })} /></div>
                </div>
                 <Button label={t('common.save')} icon="pi pi-check" className="mt-3" onClick={salvar} />
            </Dialog>
        </div>
    );
};
export default Agenda;
