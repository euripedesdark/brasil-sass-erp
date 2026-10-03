import { useTranslation } from 'react-i18next';

import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import { AutoComplete } from 'primereact/autocomplete';
import { Dropdown } from 'primereact/dropdown';
import PedidoCompraService from '../../services/PedidoCompraService';
import FornecedorService from '../../services/FornecedorService';
import ProdutoService from '../../services/ProdutoService';
import axios from 'axios';
import { useAuth } from '../../contexts/AuthContext';

const STATUS_LABEL = {
    ABERTO: { label: 'Aberto', severity: 'info' },
    PARCIAL: { label: 'Parcial', severity: 'warning' },
    RECEBIDO: { label: 'Recebido', severity: 'success' },
    CANCELADO: { label: 'Cancelado', severity: 'danger' },
};

export const Compras = () => {
    const { t } = useTranslation();
    
    const { user } = useAuth();
    const toast = useRef(null);
    const [pedidos, setPedidos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [recebimentoVisible, setRecebimentoVisible] = useState(false);
    const [historicoVisible, setHistoricoVisible] = useState(false);
    const [error, setError] = useState('');
    const [somenteLeitura, setSomenteLeitura] = useState(false);
    const [fornecedores, setFornecedores] = useState([]);
    const [fornecedoresSugestoes, setFornecedoresSugestoes] = useState([]);
    const [condicoesPagamento, setCondicoesPagamento] = useState([]);
    const [produtosSugestoes, setProdutosSugestoes] = useState([]);
    const [recebimentoPedido, setRecebimentoPedido] = useState(null);
    const [recebimentoItens, setRecebimentoItens] = useState({});
    const [recebimentos, setRecebimentos] = useState([]);
    const [historicoPedido, setHistoricoPedido] = useState(null);

    const [form, setForm] = useState(emptyForm());
    const [itemAtual, setItemAtual] = useState(emptyItem());

    function emptyForm() {
        return { fornecedorId: null, dataEmissao: new Date(), dataPrevisaoEntrega: null, condicaoPagamentoId: null, observacao: '', valorDesconto: 0, valorFrete: 0, itens: [] };
    }
    function emptyItem() {
        return { produtoId: null, descricao: '', quantidade: 1, unidade: 'UN', valorUnitario: 0, valorDesconto: 0 };
    }

    const empresaId = user?.empresaId;

    useEffect(() => {
        if (!empresaId) return;
        fetchPedidos();
        carregarCadastros();
    }, [empresaId]);

    const getApiErrorMessage = (err, fallback) =>
        err?.response?.data?.errors?.[0]?.message || err?.response?.data?.message ||
        err?.response?.data?.error || err?.message || fallback;

    const carregarCadastros = async () => {
        try {
            const [fornecedoresRes, condicoesRes] = await Promise.all([
                FornecedorService.listar(0, 100),
                axios.get('/api/financeiro/condicoes-pagamento')
            ]);
            const fornecedoresData = fornecedoresRes?.data?.content ?? fornecedoresRes?.data?.data?.content ?? [];
            setFornecedores(Array.isArray(fornecedoresData) ? fornecedoresData : []);
            const condicoesData = condicoesRes?.data?.data ?? condicoesRes?.data ?? [];
            setCondicoesPagamento(Array.isArray(condicoesData) ? condicoesData : []);
        } catch (err) {
            toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: getApiErrorMessage(err, t('legacyUi.comprasLegacy.loadAux')), life: 3500 });
        }
    };

    const buscarFornecedores = (event) => {
        const query = (event.query || '').toLowerCase().trim();
        setFornecedoresSugestoes(fornecedores.filter(f => {
            const p = f.pessoa || {};
            return !query || String(f.id).includes(query) || String(f.codigo || '').toLowerCase().includes(query) ||
                String(p.nome || '').toLowerCase().includes(query) || String(p.documento || '').includes(query);
        }).slice(0, 20));
    };

    const buscarProdutos = async (event) => {
        const query = (event.query || '').trim();
        if (!query) return setProdutosSugestoes([]);
        try {
            const res = await ProdutoService.buscarPorNome(query, 0, 20);
            const data = res?.data?.data?.content ?? res?.data?.content ?? res?.data?.data ?? res?.data ?? [];
            setProdutosSugestoes(Array.isArray(data) ? data : []);
        } catch { setProdutosSugestoes([]); }
    };

    const fetchPedidos = async () => {
        setLoading(true);
        try {
            const res = await PedidoCompraService.listarPorEmpresa(empresaId);
            const data = res?.data?.data ?? res?.data ?? [];
            setPedidos(Array.isArray(data) ? data : []);
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.comprasLegacy.loadOrders')), life: 3000 });
            setPedidos([]);
        } finally { setLoading(false); }
    };

    const adicionarItem = () => {
        if (!itemAtual.quantidade || itemAtual.quantidade <= 0 || !itemAtual.valorUnitario) return setError(t('legacyUi.comprasLegacy.itemRequired'));
        if (!itemAtual.produtoId && !itemAtual.descricao) return setError(t('legacyUi.comprasLegacy.productRequired'));
        setForm(prev => ({ ...prev, itens: [...prev.itens, { ...itemAtual, numeroItem: prev.itens.length + 1 }] }));
        setItemAtual(emptyItem()); setError('');
    };

    const removerItem = index => setForm(prev => ({ ...prev, itens: prev.itens.filter((_, i) => i !== index).map((it, i) => ({ ...it, numeroItem: i + 1 })) }));

    const totalItens = () => form.itens.reduce((acc, it) => acc + ((Number(it.quantidade) || 0) * (Number(it.valorUnitario) || 0) - (Number(it.valorDesconto) || 0)), 0);
    const moeda = v => (v == null ? 0 : Number(v)).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const dataBr = d => d ? new Date(d).toLocaleDateString('pt-BR') : '';

    const salvar = async () => {
        setError('');
        if (!empresaId) return setError(t('legacyUi.comprasLegacy.companyMissing'));
        if (!form.fornecedorId) return setError(t('legacyUi.comprasLegacy.supplierRequired'));
        if (!form.itens.length) return setError(t('legacyUi.comprasLegacy.itemsRequired'));
        setLoading(true);
        try {
            await PedidoCompraService.criar({
                empresaId, fornecedorId: form.fornecedorId, status: 'ABERTO',
                dataEmissao: form.dataEmissao ? new Date(form.dataEmissao).toISOString().slice(0, 10) : null,
                dataPrevisaoEntrega: form.dataPrevisaoEntrega ? new Date(form.dataPrevisaoEntrega).toISOString().slice(0, 10) : null,
                condicaoPagamentoId: form.condicaoPagamentoId || null, observacao: form.observacao || null,
                valorDesconto: form.valorDesconto || 0, valorFrete: form.valorFrete || 0,
                itens: form.itens.map((it, i) => ({ numeroItem: it.numeroItem || i + 1, produtoId: it.produtoId || null, descricao: it.descricao || null, quantidade: it.quantidade, unidade: it.unidade || 'UN', valorUnitario: it.valorUnitario, valorDesconto: it.valorDesconto || 0 }))
            });
            toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: t('legacyUi.comprasLegacy.created'), life: 3000 });
            setDialogVisible(false); setForm(emptyForm()); fetchPedidos();
        } catch (err) {
            const msg = getApiErrorMessage(err, t('legacyUi.comprasLegacy.saveError'));
            setError(msg); toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg, life: 4000 });
        } finally { setLoading(false); }
    };

    const abrirRecebimento = async row => {
        try {
            const res = await PedidoCompraService.buscarPorId(row.id);
            const pedido = res?.data?.data ?? res?.data ?? row;
            const itens = (pedido.itens || []).map(it => ({
                ...it,
                restante: Math.max(0, Number(it.quantidade || 0) - Number(it.quantidadeRecebida || 0)),
                receber: Math.max(0, Number(it.quantidade || 0) - Number(it.quantidadeRecebida || 0))
            }));
            setRecebimentoPedido({ ...pedido, itens });
            setRecebimentoItens(Object.fromEntries(itens.map(it => [it.id, it.receber])));
            setRecebimentoVisible(true);
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.comprasLegacy.openReceiptError')), life: 4000 });
        }
    };

    const confirmarRecebimento = async () => {
        if (!recebimentoPedido) return;
        const quantidades = Object.fromEntries(Object.entries(recebimentoItens).filter(([, q]) => Number(q) > 0).map(([id, q]) => [id, Number(q)]));
        if (!Object.keys(quantidades).length) return toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: t('legacyUi.comprasLegacy.receiveQty'), life: 3000 });
        try {
            await PedidoCompraService.receberParcial(recebimentoPedido.id, quantidades);
            toast.current?.show({ severity: 'success', summary: t('legacyUi.comprasLegacy.received'), detail: 'Estoque atualizado e recebimento formal criado', life: 3500 });
            setRecebimentoVisible(false); setRecebimentoPedido(null); fetchPedidos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.comprasLegacy.receiveError')), life: 4500 });
        }
    };

    const abrirHistorico = async row => {
        try {
            const res = await PedidoCompraService.listarRecebimentos(row.id);
            const data = res?.data?.data ?? res?.data ?? [];
            setRecebimentos(Array.isArray(data) ? data : []);
            setHistoricoPedido(row);
            setHistoricoVisible(true);
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.comprasLegacy.historyError')), life: 4000 });
        }
    };

    const cancelar = async id => {
        try { await PedidoCompraService.cancelar(id); toast.current?.show({ severity: 'success', summary: 'Cancelado', detail: t('legacyUi.comprasLegacy.cancelled'), life: 3000 }); fetchPedidos(); }
        catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.comprasLegacy.cancelError')), life: 4000 }); }
    };

    const abrirNovo = () => { setForm(emptyForm()); setItemAtual(emptyItem()); setError(''); setSomenteLeitura(false); setDialogVisible(true); };
    const abrirVer = row => {
        setForm({ fornecedorId: row.fornecedorId, dataEmissao: row.dataEmissao ? new Date(row.dataEmissao) : null, dataPrevisaoEntrega: row.dataPrevisaoEntrega ? new Date(row.dataPrevisaoEntrega) : null, condicaoPagamentoId: null, observacao: row.observacao || '', valorDesconto: 0, valorFrete: 0, itens: row.itens || [] });
        setSomenteLeitura(true); setError(''); setDialogVisible(true);
    };

    const statusBody = row => { const s = STATUS_LABEL[row.status] || { label: row.status, severity: 'secondary' }; return <Tag value={s.label} severity={s.severity} />; };

    const acoes = row => (
        <div className="flex gap-1">
            <Button icon="pi pi-eye" className="p-button-text p-button-sm" tooltip="Ver" onClick={() => abrirVer(row)} />
            {(row.status === 'ABERTO' || row.status === 'PARCIAL') && <Button icon="pi pi-box" className="p-button-text p-button-success p-button-sm" tooltip="Receber" onClick={() => abrirRecebimento(row)} />}
            {row.status === 'ABERTO' && <Button icon="pi pi-times" className="p-button-text p-button-danger p-button-sm" tooltip="Cancelar" onClick={() => cancelar(row.id)} />}
            <Button icon="pi pi-history" className="p-button-text p-button-sm" tooltip="Histórico de recebimentos" onClick={() => abrirHistorico(row)} />
        </div>
    );

    return <div>
        <Toast ref={toast} />
        <Card title={t('legacyUi.comprasLegacy.title')}>
            <div className="flex justify-content-between align-items-center mb-3">
                <p className="m-0 text-color-secondary">Fluxo: pedido → recebimento parcial/integral → estoque → contas a pagar → conferência 3-way.</p>
                <Button label={t('legacyUi.comprasLegacy.new')} icon="pi pi-plus" onClick={abrirNovo} />
            </div>
            <DataTable value={pedidos} loading={loading} paginator rows={10} rowsPerPageOptions={[10, 25, 50]} emptyMessage={t('legacyUi.comprasLegacy.empty')} size="small">
                <Column field="numero" header="Número" style={{ width: '130px' }} />
                <Column field="fornecedorId" header="Fornecedor" body={r => fornecedores.find(f => f.id === r.fornecedorId)?.pessoa?.nome || r.fornecedorId} />
                <Column field="dataEmissao" header="Emissão" body={r => dataBr(r.dataEmissao)} style={{ width: '105px' }} />
                <Column field="valorTotal" header="Total" body={r => moeda(r.valorTotal)} style={{ width: '120px' }} />
                <Column field="status" header="Status" body={statusBody} style={{ width: '115px' }} />
                <Column field="tituloId" header="Título" style={{ width: '85px' }} />
                <Column body={acoes} style={{ width: '190px' }} />
            </DataTable>
        </Card>

        <Dialog header={somenteLeitura ? t('legacyUi.comprasLegacy.preview') : `${t('legacyUi.comprasLegacy.new')} de compra`} visible={dialogVisible} style={{ width: '860px' }} onHide={() => setDialogVisible(false)} maximizable
            footer={<div><Button label={t('legacyUi.comprasLegacy.close')} className="p-button-text" onClick={() => setDialogVisible(false)} />{!somenteLeitura && <Button label={t('legacyUi.comprasLegacy.save')} icon="pi pi-save" onClick={salvar} loading={loading} />}</div>}>
            {error && <Message severity="error" text={error} className="w-full mb-3" />}
            <div className="grid p-fluid">
                <div className="col-12 md:col-4 field"><label className="font-bold">Fornecedor *</label>
                    <AutoComplete value={fornecedores.find(f => f.id === form.fornecedorId) || null} suggestions={fornecedoresSugestoes} completeMethod={buscarFornecedores} field="id"
                        itemTemplate={f => <div><strong>{f?.pessoa?.nome || 'Fornecedor'}</strong><small className="ml-2 text-color-secondary">{f?.pessoa?.documento || f?.codigo || f?.id}</small></div>}
                        selectedItemTemplate={f => f ? `${f?.pessoa?.nome || 'Fornecedor'} · ${f?.pessoa?.documento || f?.codigo || f?.id}` : ''} onChange={e => setForm({ ...form, fornecedorId: e.value?.id || null })} placeholder="Digite nome, CPF/CNPJ ou código" disabled={somenteLeitura} />
                </div>
                <div className="col-12 md:col-4 field"><label className="font-bold">Condição de pagamento</label>
                    <Dropdown value={form.condicaoPagamentoId} options={condicoesPagamento} optionLabel="descricao" optionValue="id" onChange={e => setForm({ ...form, condicaoPagamentoId: e.value })} placeholder="Selecione a condição" showClear disabled={somenteLeitura} />
                </div>
                <div className="col-12 md:col-4 field"><label className="font-bold">Data emissão</label>
                    <Calendar value={form.dataEmissao} onChange={e => setForm({ ...form, dataEmissao: e.value })} dateFormat="dd/mm/yy" showIcon disabled={somenteLeitura} />
                </div>
                <div className="col-12 md:col-4 field"><label className="font-bold">Previsão entrega</label>
                    <Calendar value={form.dataPrevisaoEntrega} onChange={e => setForm({ ...form, dataPrevisaoEntrega: e.value })} dateFormat="dd/mm/yy" showIcon disabled={somenteLeitura} />
                </div>
                <div className="col-12 field"><label className="font-bold">Observação</label><InputText value={form.observacao} onChange={e => setForm({ ...form, observacao: e.target.value })} disabled={somenteLeitura} /></div>
            </div>
            {!somenteLeitura && <><h4 className="mt-3">Itens</h4><div className="grid p-fluid">
                <div className="col-6 md:col-2 field"><label>Produto *</label><AutoComplete value={produtosSugestoes.find(p => p.id === itemAtual.produtoId) || null} suggestions={produtosSugestoes} completeMethod={buscarProdutos} field="nome"
                    itemTemplate={p => <div><strong>{p.nome}</strong><small className="ml-2 text-color-secondary">{p.codigo || p.id}</small></div>}
                    selectedItemTemplate={p => p ? `${p.nome || p.descricao} · ${p.codigo || p.id}` : ''} onChange={e => { const p = e.value; setItemAtual({ ...itemAtual, produtoId: p?.id || null, descricao: p?.nome || itemAtual.descricao, unidade: p?.unidadeSigla || itemAtual.unidade, valorUnitario: p?.precoCusto ?? itemAtual.valorUnitario }); }} placeholder="Digite nome ou código" /></div>
                <div className="col-6 md:col-3 field"><label>Descrição</label><InputText value={itemAtual.descricao} onChange={e => setItemAtual({ ...itemAtual, descricao: e.target.value })} /></div>
                <div className="col-4 md:col-2 field"><label>Qtd</label><InputNumber value={itemAtual.quantidade} onValueChange={e => setItemAtual({ ...itemAtual, quantidade: e.value })} min={0.001} maxFractionDigits={3} /></div>
                <div className="col-4 md:col-2 field"><label>Vlr unit.</label><InputNumber value={itemAtual.valorUnitario} onValueChange={e => setItemAtual({ ...itemAtual, valorUnitario: e.value })} mode="currency" currency="BRL" locale="pt-BR" /></div>
                <div className="col-4 md:col-2 field flex align-items-end"><Button label="Add" icon="pi pi-plus" onClick={adicionarItem} /></div>
            </div></>}
            <DataTable value={form.itens} emptyMessage="Sem itens" size="small" className="mt-2">
                <Column field="numeroItem" header="#" style={{ width: '50px' }} /><Column field="produtoId" header="Produto" /><Column field="descricao" header="Descrição" /><Column field="quantidade" header="Qtd" /><Column field="quantidadeRecebida" header="Recebida" /><Column field="valorUnitario" header="Unit." body={r => moeda(r.valorUnitario)} />
                {!somenteLeitura && <Column body={(_, { rowIndex }) => <Button icon="pi pi-trash" className="p-button-text p-button-danger p-button-sm" onClick={() => removerItem(rowIndex)} />} style={{ width: '60px' }} />}
            </DataTable>
            <div className="text-right mt-3 font-bold">Subtotal itens: {moeda(totalItens())}</div>
        </Dialog>

        <Dialog header={recebimentoPedido ? `Receber pedido ${recebimentoPedido.numero || recebimentoPedido.id}` : 'Receber'} visible={recebimentoVisible} style={{ width: '760px' }} onHide={() => setRecebimentoVisible(false)}
            footer={<div><Button label="Cancelar" className="p-button-text" onClick={() => setRecebimentoVisible(false)} /><Button label="Confirmar recebimento" icon="pi pi-check" onClick={confirmarRecebimento} /></div>}>
            <Message severity="info" text="Informe somente a quantidade que chegou nesta entrega. O sistema acumula recebimentos e movimenta o estoque apenas pelo novo recebido." className="w-full mb-3" />
            <DataTable value={recebimentoPedido?.itens || []} size="small">
                <Column field="numeroItem" header="#" />
                <Column field="descricao" header="Produto" />
                <Column field="quantidade" header="Pedida" />
                <Column field="quantidadeRecebida" header="Já recebida" />
                <Column field="restante" header="Restante" />
                <Column header="Receber" body={it => <InputNumber value={recebimentoItens[it.id] ?? 0} onValueChange={e => setRecebimentoItens(prev => ({ ...prev, [it.id]: e.value || 0 }))} min={0} max={it.restante} maxFractionDigits={3} />} />
            </DataTable>
        </Dialog>

        <Dialog header={historicoPedido ? `Histórico de recebimentos — ${historicoPedido.numero || historicoPedido.id}` : 'Histórico'} visible={historicoVisible} style={{ width: '760px' }} onHide={() => setHistoricoVisible(false)}>
            <DataTable value={recebimentos} size="small" emptyMessage="Nenhum recebimento registrado">
                <Column field="numero" header="Documento" />
                <Column field="dataRecebimento" header="Data" body={r => dataBr(r.dataRecebimento)} />
                <Column field="status" header="Status" />
                <Column field="valorTotal" header="Valor" body={r => moeda(r.valorTotal)} />
                <Column field="documentoFornecedor" header="Documento fornecedor" />
            </DataTable>
        </Dialog>
    </div>;
};

export default Compras;
