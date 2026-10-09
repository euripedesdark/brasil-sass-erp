import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Checkbox } from 'primereact/checkbox';

const BASE = '/api/fiscal/regras-tributarias';
const empty = () => ({
    nome: '', ncm: '', cfop: '', ufOrigem: '', ufDestino: '',
    cstIcms: '', aliquotaIcms: null, aliquotaIpi: null, aliquotaPis: null, aliquotaCofins: null,
    aliquotaSt: null, mva: null, aliquotaFcp: null, aliquotaInterna: null, reducaoBasePct: null,
    prioridade: 0, ativa: true,
});

export const RegrasTributarias = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [form, setForm] = useState(empty());
    const [busy, setBusy] = useState(false);

    const js = async (r) => {
        if (!r.ok) {
            const e = await r.json().catch(() => ({}));
            throw new Error(e.message || e.erro || ('HTTP ' + r.status));
        }
        return r.status === 204 ? null : r.json();
    };

    const carregar = async () => {
        setLoading(true);
        try {
            const j = await apiFetch(BASE).then(js);
            setRows(Array.isArray(j) ? j : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    const salvar = async () => {
        if (!form.nome) {
            toast.current?.show({ severity: 'warn', summary: 'Nome obrigatório', life: 2500 });
            return;
        }
        setBusy(true);
        try {
            const method = form.id ? 'PUT' : 'POST';
            const url = form.id ? BASE + '/' + form.id : BASE;
            await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form),
            }).then(js);
            setDlg(false);
            setForm(empty());
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const excluir = async (id) => {
        await apiFetch(BASE + '/' + id, { method: 'DELETE' });
        carregar();
    };

    const field = (label, key, num = false) => (
        <div className="col-6 md:col-4">
            <label className="block mb-1">{label}</label>
            {num ? (
                <InputNumber value={form[key]} onValueChange={(e) => setForm({ ...form, [key]: e.value })} className="w-full" />
            ) : (
                <InputText value={form[key] || ''} onChange={(e) => setForm({ ...form, [key]: e.target.value })} className="w-full" />
            )}
        </div>
    );

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 className="m-0">Regras tributárias</h2>
                    <span className="text-color-secondary">NCM/CFOP/UF → alíquotas, MVA, FCP, ST</span>
                </div>
                <Button label="Nova regra" icon="pi pi-plus" onClick={() => { setForm(empty()); setDlg(true); }} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} dataKey="id" size="small" emptyMessage="Cadastre regras por NCM/CFOP">
                <Column field="nome" header="Nome" />
                <Column field="ncm" header="NCM" />
                <Column field="cfop" header="CFOP" />
                <Column field="ufOrigem" header="UF ori" />
                <Column field="ufDestino" header="UF dest" />
                <Column field="aliquotaIcms" header="ICMS %" />
                <Column field="mva" header="MVA %" />
                <Column field="prioridade" header="Prio" />
                <Column field="ativa" header="Ativa" body={(r) => <Tag value={r.ativa ? 'Sim' : 'Não'} severity={r.ativa ? 'success' : 'danger'} />} />
                <Column body={(r) => (
                    <span className="flex gap-1">
                        <Button icon="pi pi-pencil" size="small" text onClick={() => { setForm({ ...empty(), ...r }); setDlg(true); }} />
                        <Button icon="pi pi-trash" size="small" text severity="danger" onClick={() => excluir(r.id)} />
                    </span>
                )} />
            </DataTable>
            <Dialog header={form.id ? 'Editar regra' : 'Nova regra'} visible={dlg} onHide={() => setDlg(false)} style={{ width: 'min(96vw, 720px)' }} maximizable>
                <div className="grid">
                    {field('Nome *', 'nome')}
                    {field('NCM', 'ncm')}
                    {field('CFOP', 'cfop')}
                    {field('UF origem', 'ufOrigem')}
                    {field('UF destino', 'ufDestino')}
                    {field('CST ICMS', 'cstIcms')}
                    {field('Alíq. ICMS %', 'aliquotaIcms', true)}
                    {field('Alíq. interna %', 'aliquotaInterna', true)}
                    {field('MVA %', 'mva', true)}
                    {field('Alíq. ST %', 'aliquotaSt', true)}
                    {field('FCP %', 'aliquotaFcp', true)}
                    {field('Red. base %', 'reducaoBasePct', true)}
                    {field('Alíq. IPI %', 'aliquotaIpi', true)}
                    {field('Alíq. PIS %', 'aliquotaPis', true)}
                    {field('Alíq. COFINS %', 'aliquotaCofins', true)}
                    {field('Prioridade', 'prioridade', true)}
                    <div className="col-6 md:col-4 flex align-items-center gap-2 mt-3">
                        <Checkbox inputId="ativa" checked={!!form.ativa} onChange={(e) => setForm({ ...form, ativa: e.checked })} />
                        <label htmlFor="ativa">Ativa</label>
                    </div>
                </div>
                <Button label="Salvar" icon="pi pi-check" className="mt-3" onClick={salvar} loading={busy} />
            </Dialog>
        </div>
    );
};
export default RegrasTributarias;
