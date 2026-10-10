import React, { useEffect, useMemo, useRef, useState } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { AutoComplete } from 'primereact/autocomplete';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import axios from 'axios';
import ProdutoService from '../../services/ProdutoService';
import { formatoData } from '../shared/LocaleData.js';

const emptyForm = () => ({ nome: '', codigo: '', descricao: '', moeda: 'BRL', vigenciaInicio: null, vigenciaFim: null, percentualDescontoMaximo: 0, itens: [] });
const errorMessage = (err) => err?.response?.data?.errors?.[0]?.message || err?.response?.data?.message || err?.response?.data?.error || err?.message || 'Operação não realizada';

export const TabelasPreco = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [tabelas, setTabelas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [visible, setVisible] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [form, setForm] = useState(emptyForm());
    const [produtoBusca, setProdutoBusca] = useState(null);
    const [produtoSugestoes, setProdutoSugestoes] = useState([]);
    const [item, setItem] = useState({ produtoId: null, preco: 0, precoMinimo: null, percentualDescontoMaximo: 0 });
    const moeda = useMemo(() => (v) => Number(v || 0).toLocaleString(localeAtivo(), { style: 'currency', currency: form.moeda || 'BRL' }), [form.moeda]);

    const carregar = async () => {
        setLoading(true);
        try { const res = await axios.get('/api/vendas/tabelas-preco'); const data = res?.data?.data ?? res?.data ?? []; setTabelas(Array.isArray(data) ? data : []); }
        catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: errorMessage(err), life: 4000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, []);

    const buscarProdutos = async (event) => {
        const q = (event.query || '').trim();
        if (!q) return setProdutoSugestoes([]);
        try { const res = await ProdutoService.buscarPorNome(q, 0, 20); const data = res?.data?.data?.content ?? res?.data?.content ?? res?.data?.data ?? res?.data ?? []; setProdutoSugestoes(Array.isArray(data) ? data : []); }
        catch { setProdutoSugestoes([]); }
    };
    const abrirNovo = () => { setEditingId(null); setForm(emptyForm()); setProdutoBusca(null); setItem({ produtoId: null, preco: 0, precoMinimo: null, percentualDescontoMaximo: 0 }); setVisible(true); };
    const editar = async (row) => {
        try {
            const res = await axios.get('/api/vendas/tabelas-preco/' + row.id); const data = res.data?.data ?? res.data; const t = data.tabela ?? row;
            setEditingId(row.id);
            setForm({ nome: t.nome || '', codigo: t.codigo || '', descricao: t.descricao || '', moeda: t.moeda || 'BRL', vigenciaInicio: t.vigenciaInicio ? new Date(t.vigenciaInicio) : null, vigenciaFim: t.vigenciaFim ? new Date(t.vigenciaFim) : null, percentualDescontoMaximo: Number(t.percentualDescontoMaximo || 0), itens: data.itens || [] });
            setVisible(true);
        } catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: errorMessage(err), life: 4000 }); }
    };
    const adicionarItem = () => {
        if (!item.produtoId) return toast.current?.show({ severity: 'warn', summary: 'Produto', detail: t('legacyUi.vendasLegacy.productRequired'), life: 3000 });
        if (Number(item.preco) <= 0) return toast.current?.show({ severity: 'warn', summary: 'Preço', detail: t('legacyUi.vendasLegacy.itemRequired'), life: 3000 });
        setForm(prev => ({ ...prev, itens: [...prev.itens.filter(x => x.produtoId !== item.produtoId), { ...item }] }));
        setProdutoBusca(null); setItem({ produtoId: null, preco: 0, precoMinimo: null, percentualDescontoMaximo: 0 });
    };
    const salvar = async () => {
        if (!form.nome.trim()) return toast.current?.show({ severity: 'warn', summary: 'Validação', detail: t('legacyUi.vendasLegacy.new'), life: 3000 });
        try {
            const payload = { ...form, vigenciaInicio: form.vigenciaInicio ? form.vigenciaInicio.toISOString().slice(0, 10) : null, vigenciaFim: form.vigenciaFim ? form.vigenciaFim.toISOString().slice(0, 10) : null, itens: form.itens.map(x => ({ produtoId: x.produtoId, preco: x.preco, precoMinimo: x.precoMinimo, percentualDescontoMaximo: x.percentualDescontoMaximo || 0, vigenciaInicio: x.vigenciaInicio || null, vigenciaFim: x.vigenciaFim || null })) };
            if (editingId) await axios.put('/api/vendas/tabelas-preco/' + editingId, payload); else await axios.post('/api/vendas/tabelas-preco', payload);
            toast.current?.show({ severity: 'success', summary: 'Salvo', detail: 'Tabela de preço salva', life: 3000 }); setVisible(false); carregar();
        } catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: errorMessage(err), life: 4500 }); }
    };
    const excluir = async (row) => { if (!window.confirm(t('common.confirm'))) return; try { await axios.delete('/api/vendas/tabelas-preco/' + row.id); carregar(); } catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: errorMessage(err), life: 4000 }); } };
    const produtoNome = (id) => { const p = produtoSugestoes.find(x => x.id === id); return p ? (p.nome || p.descricao || p.codigo) : '#' + id; };
    const acoes = row => <div className="flex gap-1"><Button icon="pi pi-pencil" className="p-button-text p-button-sm" tooltip="Editar" onClick={() => editar(row)} /><Button icon="pi pi-trash" className="p-button-text p-button-danger p-button-sm" tooltip="Desativar" onClick={() => excluir(row)} /></div>;
    return <div>
        <Toast ref={toast} />
        <Card title={t('menu.priceTables')}>
            <div className="flex justify-content-between align-items-center mb-3"><span className="text-color-secondary">Política comercial por tabela, vigência e produto.</span><Button label={t('common.newFemale')} icon="pi pi-plus" onClick={abrirNovo} /></div>
            <DataTable value={tabelas} loading={loading} paginator rows={10} size="small" emptyMessage={t('common.noResults')}>
                <Column field="codigo" header="Código" style={{ width: '120px' }} /><Column field="nome" header="Nome" /><Column field="moeda" header="Moeda" style={{ width: '100px' }} />
                <Column field="percentualDescontoMaximo" header="Desconto máx." body={r => Number(r.percentualDescontoMaximo || 0).toFixed(2) + '%'} /><Column field="ativo" header="Status" body={() => <Tag value="ATIVA" severity="success" />} /><Column body={acoes} style={{ width: '110px' }} />
            </DataTable>
        </Card>
        <Dialog header={editingId ? t('common.edit') : `${t('common.newFemale')} de preço`} visible={visible} style={{ width: '1000px' }} maximizable onHide={() => setVisible(false)}
            footer={<div><Button label={t('common.cancel')} className="p-button-text" onClick={() => setVisible(false)} /><Button label={t('common.save')} icon="pi pi-save" onClick={salvar} /></div>}>
            <div className="grid p-fluid">
                <div className="col-12 md:col-4 field"><label>Nome *</label><InputText value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} /></div>
                <div className="col-12 md:col-2 field"><label>Código</label><InputText value={form.codigo} onChange={e => setForm({ ...form, codigo: e.target.value })} /></div>
                <div className="col-12 md:col-2 field"><label>Moeda</label><InputText maxLength={3} value={form.moeda} onChange={e => setForm({ ...form, moeda: e.target.value.toUpperCase() })} /></div>
                <div className="col-12 md:col-2 field"><label>Desconto máx.</label><InputNumber value={form.percentualDescontoMaximo} suffix=" %" min={0} max={100} onValueChange={e => setForm({ ...form, percentualDescontoMaximo: e.value || 0 })} /></div>
                <div className="col-12 md:col-2 field"><label>Início</label><Calendar value={form.vigenciaInicio} onChange={e => setForm({ ...form, vigenciaInicio: e.value })} dateFormat={formatoData()} showIcon /></div>
                <div className="col-12 field"><label>Descrição</label><InputText value={form.descricao} onChange={e => setForm({ ...form, descricao: e.target.value })} /></div>
            </div>
            <h4>Itens da tabela</h4>
            <div className="grid p-fluid align-items-end">
                <div className="col-12 md:col-4 field"><label>Produto</label><AutoComplete value={produtoBusca} suggestions={produtoSugestoes} completeMethod={buscarProdutos} field="nome" itemTemplate={p => <div><strong>{p.nome}</strong><small className="ml-2 text-color-secondary">{p.codigo || p.id}</small></div>} onChange={e => { setProdutoBusca(e.value); setItem({ ...item, produtoId: e.value?.id || null }); }} placeholder="Nome ou código" /></div>
                <div className="col-12 md:col-2 field"><label>Preço</label><InputNumber value={item.preco} mode="currency" currency={form.moeda || 'BRL'} locale={localeAtivo()} onValueChange={e => setItem({ ...item, preco: e.value || 0 })} /></div>
                <div className="col-12 md:col-2 field"><label>Preço mínimo</label><InputNumber value={item.precoMinimo} mode="currency" currency={form.moeda || 'BRL'} locale={localeAtivo()} onValueChange={e => setItem({ ...item, precoMinimo: e.value })} /></div>
                <div className="col-12 md:col-2 field"><label>Desc. máx.</label><InputNumber value={item.percentualDescontoMaximo} suffix=" %" min={0} max={100} onValueChange={e => setItem({ ...item, percentualDescontoMaximo: e.value || 0 })} /></div>
                <div className="col-12 md:col-2 field"><Button label={t('legacyUi.vendasLegacy.add')} icon="pi pi-plus" onClick={adicionarItem} /></div>
            </div>
            <DataTable value={form.itens} size="small" emptyMessage={t('common.noResults')}>
                <Column field="produtoId" header={t('legacyUi.vendasLegacy.product')} body={r => produtoNome(r.produtoId)} /><Column field="preco" header={t('legacyUi.vendasLegacy.price')} body={r => moeda(r.preco)} /><Column field="precoMinimo" header={t('legacyUi.vendasLegacy.price')} body={r => r.precoMinimo == null ? '-' : moeda(r.precoMinimo)} /><Column field="percentualDescontoMaximo" header="Desc. máx." body={r => Number(r.percentualDescontoMaximo || 0).toFixed(2) + '%'} />
                <Column body={(_, meta) => <Button icon="pi pi-trash" className="p-button-text p-button-danger p-button-sm" onClick={() => setForm(prev => ({ ...prev, itens: prev.itens.filter((__, i) => i !== meta.rowIndex) }))} />} style={{ width: '60px' }} />
            </DataTable>
        </Dialog>
    </div>;
};
export default TabelasPreco;