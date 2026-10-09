import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Calendar } from 'primereact/calendar';

const BASE = '/api/compras/contratos';

const sev = (s) => {
    if (s === 'ATIVO') return 'success';
    if (s === 'ENCERRADO') return 'info';
    if (s === 'CANCELADO') return 'danger';
    return 'warning';
};

const money = (v) => {
    if (v == null) return '—';
    const n = Number(v);
    if (Number.isNaN(n)) return String(v);
    return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
};

export const ContratosFornecimento = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [dlg, setDlg] = useState(false);
    const [libDlg, setLibDlg] = useState(false);
    const [sel, setSel] = useState(null);
    const [libQtds, setLibQtds] = useState({});

    const [form, setForm] = useState({
        fornecedorId: null,
        numero: '',
        tipo: 'QUANTIDADE',
        vigenciaInicio: null,
        vigenciaFim: null,
        valorLimite: null,
        observacao: '',
        itens: [{ numeroItem: 1, produtoId: null, descricao: '', unidade: 'UN', quantidadeContratada: 1, valorUnitario: 0 }],
    });

    const js = async (r) => {
        if (!r.ok) {
            const err = await r.json().catch(() => ({}));
            throw new Error(err?.message || err?.erro || err?.errors?.[0]?.message || ('HTTP ' + r.status));
        }
        const j = await r.json().catch(() => null);
        return Array.isArray(j) ? j : (j?.data ?? j);
    };

    const carregar = async () => {
        setLoading(true);
        try {
            const a = await apiFetch(BASE).then(js);
            setRows(Array.isArray(a) ? a : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    const acao = async (id, op) => {
        setBusy(true);
        try {
            await apiFetch(BASE + '/' + id + '/' + op, { method: 'POST' }).then(js);
            toast.current?.show({ severity: 'success', summary: op, life: 2500 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const fmtDate = (d) => {
        if (!d) return null;
        const x = d instanceof Date ? d : new Date(d);
        const y = x.getFullYear();
        const m = String(x.getMonth() + 1).padStart(2, '0');
        const day = String(x.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    };

    const criar = async () => {
        if (!form.fornecedorId || !form.vigenciaInicio || !form.vigenciaFim || !form.itens?.length) {
            toast.current?.show({ severity: 'warn', summary: 'Preencha fornecedor, vigência e itens', life: 3000 });
            return;
        }
        setBusy(true);
        try {
            const body = {
                fornecedorId: form.fornecedorId,
                numero: form.numero || undefined,
                tipo: form.tipo || 'QUANTIDADE',
                vigenciaInicio: fmtDate(form.vigenciaInicio),
                vigenciaFim: fmtDate(form.vigenciaFim),
                valorLimite: form.valorLimite,
                observacao: form.observacao || undefined,
                itens: form.itens.map((it, i) => ({
                    numeroItem: it.numeroItem || i + 1,
                    produtoId: it.produtoId || undefined,
                    descricao: it.descricao || undefined,
                    unidade: it.unidade || 'UN',
                    quantidadeContratada: it.quantidadeContratada,
                    valorUnitario: it.valorUnitario,
                })),
            };
            await apiFetch(BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body),
            }).then(js);
            setDlg(false);
            toast.current?.show({ severity: 'success', summary: 'Contrato criado', life: 2500 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const abrirLiberar = (row) => {
        setSel(row);
        const q = {};
        (row.itens || []).forEach((it) => { q[it.id] = 0; });
        setLibQtds(q);
        setLibDlg(true);
    };

    const liberar = async () => {
        if (!sel) return;
        const quantidades = {};
        Object.entries(libQtds).forEach(([k, v]) => {
            if (v && Number(v) > 0) quantidades[k] = Number(v);
        });
        if (!Object.keys(quantidades).length) {
            toast.current?.show({ severity: 'warn', summary: 'Informe quantidades', life: 3000 });
            return;
        }
        setBusy(true);
        try {
            await apiFetch(BASE + '/' + sel.id + '/liberar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quantidades }),
            }).then(js);
            setLibDlg(false);
            toast.current?.show({ severity: 'success', summary: 'Pedido liberado', life: 3000 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    const botoes = (r) => (
        <span className="flex gap-1 flex-wrap">
            {r.status === 'RASCUNHO' && (
                <Button icon="pi pi-check" size="small" tooltip="Ativar" onClick={() => acao(r.id, 'ativar')} disabled={busy} />
            )}
            {r.status === 'ATIVO' && (
                <>
                    <Button icon="pi pi-shopping-cart" size="small" tooltip="Liberar pedido" onClick={() => abrirLiberar(r)} disabled={busy} />
                    <Button icon="pi pi-lock" size="small" severity="secondary" tooltip="Encerrar" onClick={() => acao(r.id, 'encerrar')} disabled={busy} />
                </>
            )}
            {(r.status === 'RASCUNHO' || r.status === 'ATIVO') && (
                <Button icon="pi pi-times" size="small" severity="danger" tooltip="Cancelar" onClick={() => acao(r.id, 'cancelar')} disabled={busy} />
            )}
        </span>
    );

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Contratos de fornecimento</h2>
                    <span className="text-color-secondary">Acordo QUANTIDADE/VALOR → liberação gera pedido de compra</span>
                </div>
                <Button label="Novo contrato" icon="pi pi-plus" onClick={() => setDlg(true)} />
            </div>

            <DataTable value={rows} loading={loading} paginator rows={12}
                emptyMessage="Nenhum contrato. Crie um e ative para liberar pedidos."
                responsiveLayout="scroll" dataKey="id">
                <Column field="numero" header="Número" style={{ width: '8rem' }} />
                <Column field="tipo" header="Tipo" style={{ width: '7rem' }} />
                <Column field="fornecedorId" header="Forn. ID" style={{ width: '6rem' }} />
                <Column header="Vigência" body={(r) => `${r.vigenciaInicio || '—'} → ${r.vigenciaFim || '—'}`} />
                <Column header="Limite" body={(r) => money(r.valorLimite)} />
                <Column header="Liberado" body={(r) => money(r.valorLiberado)} />
                <Column header="Saldo" body={(r) => money(r.saldoValor)} />
                <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column header="Ações" body={botoes} style={{ width: '10rem' }} />
            </DataTable>

            <Dialog header="Novo contrato" visible={dlg} onHide={() => setDlg(false)} style={{ width: 'min(96vw, 560px)' }} maximizable>
                <div className="flex flex-column gap-3">
                    <div className="grid">
                        <div className="col-6">
                            <label className="block mb-1">Fornecedor ID *</label>
                            <InputNumber value={form.fornecedorId} onValueChange={(e) => setForm({ ...form, fornecedorId: e.value })} className="w-full" useGrouping={false} />
                        </div>
                        <div className="col-6">
                            <label className="block mb-1">Número</label>
                            <InputText value={form.numero} onChange={(e) => setForm({ ...form, numero: e.target.value })} className="w-full" placeholder="auto se vazio" />
                        </div>
                        <div className="col-6">
                            <label className="block mb-1">Tipo</label>
                            <Dropdown value={form.tipo} options={[{ label: 'QUANTIDADE', value: 'QUANTIDADE' }, { label: 'VALOR', value: 'VALOR' }]}
                                onChange={(e) => setForm({ ...form, tipo: e.value })} className="w-full" />
                        </div>
                        <div className="col-6">
                            <label className="block mb-1">Valor limite</label>
                            <InputNumber value={form.valorLimite} onValueChange={(e) => setForm({ ...form, valorLimite: e.value })} mode="currency" currency="BRL" locale="pt-BR" className="w-full" />
                        </div>
                        <div className="col-6">
                            <label className="block mb-1">Início *</label>
                            <Calendar value={form.vigenciaInicio} onChange={(e) => setForm({ ...form, vigenciaInicio: e.value })} dateFormat="dd/mm/yy" className="w-full" />
                        </div>
                        <div className="col-6">
                            <label className="block mb-1">Fim *</label>
                            <Calendar value={form.vigenciaFim} onChange={(e) => setForm({ ...form, vigenciaFim: e.value })} dateFormat="dd/mm/yy" className="w-full" />
                        </div>
                    </div>
                    <label className="block">Itens</label>
                    {(form.itens || []).map((it, idx) => (
                        <div key={idx} className="grid align-items-end">
                            <div className="col-4">
                                <InputText placeholder="Descrição" value={it.descricao || ''} className="w-full"
                                    onChange={(e) => {
                                        const itens = [...form.itens];
                                        itens[idx] = { ...itens[idx], descricao: e.target.value };
                                        setForm({ ...form, itens });
                                    }} />
                            </div>
                            <div className="col-3">
                                <InputNumber placeholder="Qtd" value={it.quantidadeContratada} className="w-full"
                                    onValueChange={(e) => {
                                        const itens = [...form.itens];
                                        itens[idx] = { ...itens[idx], quantidadeContratada: e.value };
                                        setForm({ ...form, itens });
                                    }} />
                            </div>
                            <div className="col-3">
                                <InputNumber placeholder="Vlr unit" value={it.valorUnitario} mode="currency" currency="BRL" locale="pt-BR" className="w-full"
                                    onValueChange={(e) => {
                                        const itens = [...form.itens];
                                        itens[idx] = { ...itens[idx], valorUnitario: e.value };
                                        setForm({ ...form, itens });
                                    }} />
                            </div>
                            <div className="col-2">
                                <InputText placeholder="UN" value={it.unidade || 'UN'} className="w-full"
                                    onChange={(e) => {
                                        const itens = [...form.itens];
                                        itens[idx] = { ...itens[idx], unidade: e.target.value };
                                        setForm({ ...form, itens });
                                    }} />
                            </div>
                        </div>
                    ))}
                    <Button label="Add item" icon="pi pi-plus" text size="small"
                        onClick={() => setForm({
                            ...form,
                            itens: [...form.itens, { numeroItem: form.itens.length + 1, descricao: '', unidade: 'UN', quantidadeContratada: 1, valorUnitario: 0 }],
                        })} />
                    <Button label="Salvar" icon="pi pi-check" onClick={criar} loading={busy} />
                </div>
            </Dialog>

            <Dialog header={`Liberar pedido — ${sel?.numero || ''}`} visible={libDlg} onHide={() => setLibDlg(false)} style={{ width: 'min(96vw, 480px)' }}>
                <div className="flex flex-column gap-3">
                    {(sel?.itens || []).map((it) => (
                        <div key={it.id} className="flex justify-content-between align-items-center gap-2">
                            <span className="flex-1">{it.descricao || ('Item ' + it.numeroItem)} — saldo {it.saldo ?? (it.quantidadeContratada - (it.quantidadeLiberada || 0))}</span>
                            <InputNumber value={libQtds[it.id] || 0} onValueChange={(e) => setLibQtds({ ...libQtds, [it.id]: e.value })}
                                min={0} className="w-8rem" />
                        </div>
                    ))}
                    <Button label="Gerar pedido de compra" icon="pi pi-shopping-cart" onClick={liberar} loading={busy} />
                </div>
            </Dialog>
        </div>
    );
};

export default ContratosFornecimento;
