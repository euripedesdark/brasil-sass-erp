import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Toast } from 'primereact/toast';
import { AtpConfirmDialog } from './AtpConfirmDialog';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import { AutoComplete } from 'primereact/autocomplete';
import { Dropdown } from 'primereact/dropdown';
import { InputTextarea } from 'primereact/inputtextarea';
import PedidoVendaService from '../../services/PedidoVendaService';
import ClienteService from '../../services/ClienteService';
import ProdutoService from '../../services/ProdutoService';
import axios from 'axios';
import { useAuth } from '../../contexts/AuthContext';

/** Status reais do backend: ABERTO | FATURADO | CANCELADO */
const STATUS_LABEL = {
    ABERTO: { label: 'Aberto', severity: 'info' },
    FATURADO: { label: 'Faturado', severity: 'success' },
    CANCELADO: { label: 'Cancelado', severity: 'danger' },
};

const getApiErrorMessage = (err, fallback) =>
    err?.response?.data?.errors?.[0]?.message ||
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    fallback;

export const Vendas = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [pedidos, setPedidos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [emAcao, setEmAcao] = useState(null);
    const [error, setError] = useState('');
    const [somenteLeitura, setSomenteLeitura] = useState(false);
    const [ufDestinoTax, setUfDestinoTax] = useState('');
    const [taxPrev, setTaxPrev] = useState(null);
    const [taxDlg, setTaxDlg] = useState(false);
    const [clientes, setClientes] = useState([]);
    const [creditoCli, setCreditoCli] = useState(null);
    const [dlgAtp, setDlgAtp] = useState(false);
    const [atpPedidoId, setAtpPedidoId] = useState(null);
    const buscarCredito = async (clienteId) => { setCreditoCli(null); if (!clienteId) return; try { const r = await PedidoVendaService.credito(clienteId); setCreditoCli(r?.data ?? null); } catch (e) { setCreditoCli(null); } };
    const [clientesSugestoes, setClientesSugestoes] = useState([]);
    const [vendedores, setVendedores] = useState([]);
    const [condicoesPagamento, setCondicoesPagamento] = useState([]);
    const [produtosSugestoes, setProdutosSugestoes] = useState([]);
    const [produtoSelecionado, setProdutoSelecionado] = useState(null);
    const [form, setForm] = useState(emptyForm());
    const [itemAtual, setItemAtual] = useState(emptyItem());
    const [dlgPos, setDlgPos] = useState(false);
    const [posId, setPosId] = useState(null);
    const [posMotivo, setPosMotivo] = useState('');
    const [posEquip, setPosEquip] = useState('');

    function emptyForm() {
        return {
            clienteId: null, vendedorId: null, tipo: 'PEDIDO',
            dataEmissao: new Date(), dataEntrega: null,
            condicaoPagamentoId: null, observacao: '', valorDesconto: 0,
            valorFrete: 0, itens: [],
        };
    }

    function emptyItem() {
        return {
            produtoId: null, servicoId: null, descricao: '', quantidade: 1,
            unidade: 'UN', valorUnitario: 0, valorDesconto: 0,
        };
    }

    const empresaId = user?.empresaId;

    useEffect(() => {
        if (!empresaId) return;
        fetchPedidos();
        carregarCadastros();
    }, [empresaId]);

    const carregarCadastros = async () => {
        try {
            const [clientesRes, vendedoresRes, condicoesRes] = await Promise.all([
                ClienteService.listar(0, 100),
                axios.get('/api/rh/funcionarios', { params: { empresaId } }),
                axios.get('/api/financeiro/condicoes-pagamento')
            ]);
            const clientesData = clientesRes?.data?.content ?? clientesRes?.data?.data?.content ?? [];
            setClientes(Array.isArray(clientesData) ? clientesData : []);
            const vendedoresData = vendedoresRes?.data?.data ?? vendedoresRes?.data ?? [];
            setVendedores((Array.isArray(vendedoresData) ? vendedoresData : []).filter(v => v.tipoColaborador === 'VENDEDOR'));
            const condicoesData = condicoesRes?.data?.data ?? condicoesRes?.data ?? [];
            setCondicoesPagamento(Array.isArray(condicoesData) ? condicoesData : []);
        } catch (err) {
            toast.current?.show({ severity: 'warn', summary: t('common.warning'), detail: getApiErrorMessage(err, t('legacyUi.vendasLegacy.loadAux')), life: 3500 });
        }
    };

    const buscarClientes = (event) => {
        const query = (event.query || '').toLowerCase().trim();
        setClientesSugestoes(clientes.filter(c => {
            const p = c.pessoa || {};
            return !query || String(c.id).includes(query) || String(c.codigo || '').toLowerCase().includes(query) || String(p.nome || '').toLowerCase().includes(query) || String(p.documento || '').includes(query);
        }).slice(0, 20));
    };

    const buscarProdutos = async (event) => {
        const query = (event.query || '').trim();
        if (!query) { setProdutosSugestoes([]); return; }
        try {
            const res = await ProdutoService.buscarPorNome(query, 0, 20);
            const data = res?.data?.data?.content ?? res?.data?.content ?? res?.data?.data ?? res?.data ?? [];
            setProdutosSugestoes(Array.isArray(data) ? data : []);
        } catch (err) { setProdutosSugestoes([]); }
    };

    const clienteLabel = (c) => { const p = c?.pessoa || {}; return c ? `${p.nome || 'Cliente'} · ${p.documento || c.codigo || c.id}` : ''; };
    const produtoLabel = (p) => p ? `${p.nome || p.descricao || 'Produto'} · ${p.codigo || p.id}` : '';

    const fetchPedidos = async () => {
        setLoading(true);
        try {
            const res = await PedidoVendaService.listarPorEmpresa(empresaId);
            const data = res?.data?.data ?? res?.data ?? [];
            setPedidos(Array.isArray(data) ? data : []);
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.vendasLegacy.loadOrders')), life: 3000 });
            setPedidos([]);
        } finally { setLoading(false); }
    };

    const adicionarItem = () => {
        if (!itemAtual.quantidade || itemAtual.quantidade <= 0 || !itemAtual.valorUnitario) { setError(t('legacyUi.vendasLegacy.itemRequired')); return; }
        if (!itemAtual.produtoId && !itemAtual.servicoId) { setError(t('legacyUi.vendasLegacy.productRequired')); return; }
        setForm((prev) => ({ ...prev, itens: [...prev.itens, { ...itemAtual, numeroItem: prev.itens.length + 1 }] }));
        setItemAtual(emptyItem());
        setError('');
    };

    const removerItem = (index) => {
        setForm((prev) => ({ ...prev, itens: prev.itens.filter((_, i) => i !== index).map((it, i) => ({ ...it, numeroItem: i + 1 })) }));
    };

    const totalItens = () => form.itens.reduce((acc, it) => {
        const q = Number(it.quantidade) || 0;
        const vu = Number(it.valorUnitario) || 0;
        const d = Number(it.valorDesconto) || 0;
        return acc + (q * vu - d);
    }, 0);

    
    const previaTributaria = async () => {
        if (!form.itens?.length) {
            toast.current?.show({ severity: 'warn', summary: 'Itens', detail: 'Inclua itens antes da prévia', life: 3000 });
            return;
        }
        setLoading(true);
        try {
            const body = {
                empresaId, clienteId: form.clienteId || 0,
                itens: form.itens.map((it, i) => ({
                    numeroItem: it.numeroItem || i + 1,
                    produtoId: it.produtoId || null,
                    quantidade: it.quantidade,
                    valorUnitario: it.valorUnitario,
                    valorDesconto: it.valorDesconto || 0,
                })),
            };
            const res = await PedidoVendaService.preverTributacao(body, {
                ufDestino: ufDestinoTax || undefined,
                consumidorFinal: true,
                contribuinte: false,
            });
            setTaxPrev(res?.data?.data ?? res?.data ?? res);
            setTaxDlg(true);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Prévia tributária', detail: getApiErrorMessage(e, 'Falha'), life: 5000 });
        } finally {
            setLoading(false);
        }
    };

    const salvar = async () => {
        setError('');
        if (!empresaId) return setError(t('legacyUi.vendasLegacy.companyMissing'));
        if (!form.clienteId) return setError(t('legacyUi.vendasLegacy.customerRequired'));
        if (!form.itens.length) return setError(t('legacyUi.vendasLegacy.itemsRequired'));
        setLoading(true);
        try {
            const payload = {
                empresaId, clienteId: form.clienteId, vendedorId: form.vendedorId || null,
                tipo: form.tipo || 'PEDIDO', status: 'ABERTO',
                dataEmissao: form.dataEmissao ? new Date(form.dataEmissao).toISOString().slice(0, 10) : null,
                dataEntrega: form.dataEntrega ? new Date(form.dataEntrega).toISOString().slice(0, 10) : null,
                condicaoPagamentoId: form.condicaoPagamentoId || null,
                observacao: form.observacao || null,
                valorDesconto: form.valorDesconto || 0, valorFrete: form.valorFrete || 0,
                itens: form.itens.map((it, i) => ({
                    numeroItem: it.numeroItem || i + 1, produtoId: it.produtoId || null,
                    servicoId: it.servicoId || null, descricao: it.descricao || null,
                    quantidade: it.quantidade, unidade: it.unidade || 'UN',
                    valorUnitario: it.valorUnitario, valorDesconto: it.valorDesconto || 0,
                })),
            };
            await PedidoVendaService.criar(payload);
            toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: t('legacyUi.vendasLegacy.created'), life: 3000 });
            setDialogVisible(false); setForm(emptyForm()); fetchPedidos();
        } catch (err) {
            const msg = getApiErrorMessage(err, t('legacyUi.vendasLegacy.saveError'));
            setError(msg);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg, life: 4000 });
        } finally { setLoading(false); }
    };

    const perguntarFaturar = (row) => {
        confirmDialog.require({
            header: t('common.confirm'),
            message: `Faturar o pedido ${row.numero || row.id}? Isso vai dar baixa no estoque, criar o titulo e as parcelas, e registrar a comissao do vendedor.`,
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('legacyUi.vendasLegacy.yesBill'),
            rejectLabel: 'Cancelar',
            accept: () => faturar(row.id),
        });
    };

    const faturar = async (id, forcar) => {
        setEmAcao(id);
        try {
            await PedidoVendaService.faturar(id, forcar);
            toast.current?.show({ severity: 'success', summary: 'Faturado', detail: 'Estoque baixado, titulo e comissao gerados', life: 3500 });
            fetchPedidos();
        } catch (err) {
            const msg = getApiErrorMessage(err, t('legacyUi.vendasLegacy.billError'));
            setError(msg);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg, life: 4000 });
            if (!forcar && /credito|limite/i.test(msg || '')) { confirmDialog.require({ header: 'Limite estourado', accept: () => faturar(id, true) }); return; }
        } finally { setEmAcao(null); }
    };

    const confirmar = async (id) => {
        try {
            await PedidoVendaService.confirmar(id);
            toast.current?.show({ severity: 'success', summary: t('legacyUi.vendasLegacy.quoteSuccess'), detail: 'Orçamento convertido em pedido. Agora pode ser faturado.', life: 3500 });
            setDlgAtp(false);
            fetchPedidos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.vendasLegacy.quoteError')), life: 4000 });
        }
    };

    const abrirPosvenda = (row) => { setPosId(row.id); setPosMotivo(''); setPosEquip(''); setDlgPos(true); };
    const confirmarPosvenda = async () => {
        if (!posMotivo.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o motivo', life: 3000 }); return; }
        try {
            const r = await PedidoVendaService.posvenda(posId, { motivo: posMotivo, equipamento: posEquip || null });
            toast.current?.show({ severity: 'success', summary: 'OS aberta', detail: 'OS ' + (r.data?.osNumero ?? r.data?.osId ?? ''), life: 4000 });
            setDlgPos(false);
        } catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, 'Falha ao abrir OS'), life: 4000 }); }
    };

    const cancelar = async (id) => {
        try {
            await PedidoVendaService.cancelar(id);
            toast.current?.show({ severity: 'success', summary: 'Cancelado', detail: t('legacyUi.vendasLegacy.cancelSuccess'), life: 3000 });
            fetchPedidos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, t('legacyUi.vendasLegacy.cancelError')), life: 4000 });
        }
    };

    const abrirNovo = () => { setForm(emptyForm()); setItemAtual(emptyItem()); setError(''); setSomenteLeitura(false); setDialogVisible(true); };
    const abrirVer = (row) => {
        setForm({
            clienteId: row.clienteId, vendedorId: null, tipo: row.tipo || 'PEDIDO',
            dataEmissao: row.dataEmissao ? new Date(row.dataEmissao) : null,
            dataEntrega: row.dataEntrega ? new Date(row.dataEntrega) : null,
            condicaoPagamentoId: null, observacao: row.observacao || '', valorDesconto: 0, valorFrete: 0,
            itens: (row.itens || []).map((it) => ({ numeroItem: it.numeroItem, produtoId: it.produtoId, servicoId: it.servicoId, descricao: it.descricao, quantidade: it.quantidade, unidade: it.unidade, valorUnitario: it.valorUnitario, valorDesconto: it.valorDesconto })),
        });
        setSomenteLeitura(true); setError(''); setDialogVisible(true);
    };

    const statusBody = (row) => { const s = STATUS_LABEL[row.status] || { label: row.status, severity: 'secondary' }; return <Tag value={s.label} severity={s.severity} />; };
    const moeda = (v) => (v == null ? 0 : Number(v)).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const dataBr = (d) => (d ? new Date(d).toLocaleDateString('pt-BR') : '');

    const acoes = (row) => (
        <div className="flex gap-1">
            <Button icon="pi pi-eye" className="p-button-text p-button-sm" tooltip="Ver" onClick={() => abrirVer(row)} />
            {row.status === 'ABERTO' && row.tipo === 'ORCAMENTO' && (
                <>
                    <Button icon="pi pi-check" className="p-button-text p-button-warning p-button-sm" tooltip="Ver ATP e confirmar" disabled={emAcao === row.id} onClick={() => { setAtpPedidoId(row.id); setDlgAtp(true); }} />
                    <Button icon="pi pi-times" className="p-button-text p-button-danger p-button-sm" tooltip="Cancelar orçamento" disabled={emAcao === row.id} onClick={() => cancelar(row.id)} />
                </>
            )}
            {row.status === 'ABERTO' && row.tipo === 'PEDIDO' && (
                <>
                    <Button icon="pi pi-file-invoice" className="p-button-text p-button-success p-button-sm" tooltip="Faturar (estoque + título)" loading={emAcao === row.id} disabled={emAcao === row.id} onClick={() => perguntarFaturar(row)} />
                    <Button icon="pi pi-times" className="p-button-text p-button-danger p-button-sm" tooltip="Cancelar pedido" onClick={() => cancelar(row.id)} />
                </>
            )}
            {row.status === 'FATURADO' && (
                <Button icon='pi pi-wrench' className='p-button-text p-button-help p-button-sm' tooltip='Pós-venda (abrir OS)' onClick={() => abrirPosvenda(row)} />
            )}
        </div>
    );

    return (
        <div>
            <Toast ref={toast} />
            <Card title={t('legacyUi.vendasLegacy.title')}>
                <div className="flex justify-content-between align-items-center mb-3">
                    <p className="m-0 text-color-secondary">Fluxo: orçamento → ATP → confirmar → pedido → faturar ou cancelar.</p>
                    <Button label={t('legacyUi.vendasLegacy.new')} icon="pi pi-plus" onClick={abrirNovo} />
                </div>
                <DataTable value={pedidos} loading={loading} paginator rows={10} rowsPerPageOptions={[10, 25, 50]} emptyMessage={t('legacyUi.vendasLegacy.empty')} size="small">
                    <Column field="numero" header={t('legacyUi.vendasLegacy.number')} style={{ width: '140px' }} />
                    <Column field="tipo" header={t('legacyUi.vendasLegacy.type')} style={{ width: '120px' }} />
                    <Column field="clienteId" header={t('legacyUi.vendasLegacy.customer')} body={r => clientes.find(c => c.id === r.clienteId)?.pessoa?.nome || r.clienteId} style={{ minWidth: '220px' }} />
                    <Column field="dataEmissao" header={t('legacyUi.vendasLegacy.issueDate')} body={(r) => dataBr(r.dataEmissao)} style={{ width: '110px' }} />
                    <Column field="valorTotal" header={t('legacyUi.vendasLegacy.total')} body={(r) => moeda(r.valorTotal)} style={{ width: '120px' }} />
                    <Column field="status" header={t('legacyUi.vendasLegacy.status')} body={statusBody} style={{ width: '120px' }} />
                    <Column field="tituloId" header="Titulo" style={{ width: '90px' }} />
                    <Column body={acoes} style={{ width: '140px' }} />
                </DataTable>
            </Card>

            <Dialog header={somenteLeitura ? 'Pedido (visualizacao)' : `${t('legacyUi.vendasLegacy.new')} de venda`} visible={dialogVisible}
                style={{ width: '860px' }} onHide={() => setDialogVisible(false)} maximizable
                footer={<div className="flex gap-2 flex-wrap justify-content-end">
                    <Button label="Prévia tributária" icon="pi pi-percentage" outlined onClick={previaTributaria} loading={loading} />
                    <Button label="Fechar" className="p-button-text" onClick={() => setDialogVisible(false)} />
                    {!somenteLeitura && <Button label="Salvar pedido" icon="pi pi-save" onClick={salvar} loading={loading} />}
                </div>}>
                {error && <Message severity="error" text={error} className="mb-3 w-full" />}
                <div className="grid p-fluid">
                    <div className="col-12 md:col-6">
                        <label>Cliente *</label>
                        <AutoComplete value={clientes.find(c => c.id === form.clienteId) || null} suggestions={clientesSugestoes} completeMethod={buscarClientes}
                            field="id" itemTemplate={(c) => clienteLabel(c)} selectedItemTemplate={(c) => clienteLabel(c)}
                            onChange={(e) => { const c = e.value; setForm({ ...form, clienteId: c?.id || null }); if (c?.id) buscarCredito(c.id); }}
                            disabled={somenteLeitura} dropdown forceSelection placeholder="Buscar cliente" />
                        {creditoCli && <small className="text-color-secondary">Limite {Number(creditoCli.limite ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} · disponivel {Number(creditoCli.disponivel ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</small>}
                    </div>
                    <div className="col-12 md:col-3">
                        <label>Tipo</label>
                        <Dropdown value={form.tipo} options={[{ label: 'Pedido', value: 'PEDIDO' }, { label: 'Orçamento', value: 'ORCAMENTO' }]} onChange={(e) => setForm({ ...form, tipo: e.value })} disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 md:col-3">
                        <label>Emissão</label>
                        <Calendar value={form.dataEmissao} onChange={(e) => setForm({ ...form, dataEmissao: e.value })} dateFormat="dd/mm/yy" showIcon disabled={somenteLeitura} />
                    </div>
                </div>
                {!somenteLeitura && (
                    <div className="grid p-fluid mt-3 align-items-end">
                        <div className="col-12 md:col-5">
                            <label>Produto</label>
                            <AutoComplete value={produtoSelecionado} suggestions={produtosSugestoes} completeMethod={buscarProdutos}
                                field="nome" itemTemplate={(p) => produtoLabel(p)}
                                onChange={(e) => {
                                    const p = e.value;
                                    setProdutoSelecionado(p);
                                    if (p && p.id) setItemAtual({ ...itemAtual, produtoId: p.id, descricao: p.nome || p.descricao, valorUnitario: p.precoVenda || p.valorVenda || 0 });
                                }} dropdown forceSelection placeholder="Buscar produto" />
                        </div>
                        <div className="col-4 md:col-2"><label>Qtd</label><InputNumber value={itemAtual.quantidade} onValueChange={(e) => setItemAtual({ ...itemAtual, quantidade: e.value })} min={0.001} /></div>
                        <div className="col-4 md:col-2"><label>Unit.</label><InputNumber value={itemAtual.valorUnitario} onValueChange={(e) => setItemAtual({ ...itemAtual, valorUnitario: e.value })} mode="currency" currency="BRL" locale="pt-BR" /></div>
                        <div className="col-4 md:col-2"><Button label="Add" icon="pi pi-plus" onClick={adicionarItem} /></div>
                    </div>
                )}
                <DataTable value={form.itens} className="mt-3" size="small" emptyMessage="Sem itens">
                    <Column field="produtoId" header="Produto" /><Column field="descricao" header="Descricao" />
                    <Column field="quantidade" header="Qtd" />
                    <Column field="valorUnitario" header="Unit." body={(r) => moeda(r.valorUnitario)} />
                    {!somenteLeitura && <Column body={(_, { rowIndex }) => <Button icon="pi pi-trash" className="p-button-text p-button-danger p-button-sm" onClick={() => removerItem(rowIndex)} />} style={{ width: '60px' }} />}
                </DataTable>
                <div className="flex justify-content-between align-items-center mt-3 flex-wrap gap-2">
                    <div className="flex align-items-center gap-2">
                        <label>UF destino (prévia)</label>
                        <InputText value={ufDestinoTax} onChange={(e) => setUfDestinoTax(e.target.value.toUpperCase())} maxLength={2} style={{ width: '4rem' }} placeholder="RJ" />
                    </div>
                    <div className="font-bold">Subtotal itens: {moeda(totalItens())}</div>
                </div>
            </Dialog>

            <AtpConfirmDialog
                visible={dlgAtp}
                pedidoId={atpPedidoId}
                onHide={() => setDlgAtp(false)}
                onConfirm={async (id) => { await confirmar(id); setDlgAtp(false); }}
                getError={(err) => toast.current?.show({ severity: 'error', summary: 'ATP', detail: getApiErrorMessage(err, 'Falha ATP'), life: 4000 })}
            />

            <Dialog visible={dlgPos} onHide={() => setDlgPos(false)} header='Pós-venda: abrir OS' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Equipamento (opcional)</label><InputText value={posEquip} onChange={(e) => setPosEquip(e.target.value)} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Motivo *</label><InputTextarea rows={3} value={posMotivo} onChange={(e) => setPosMotivo(e.target.value)} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgPos(false)} /><Button label='Abrir OS' icon='pi pi-check' onClick={confirmarPosvenda} /></div>
            </Dialog>
            <Dialog header="Prévia de impostos" visible={taxDlg} onHide={() => setTaxDlg(false)} style={{ width: 'min(96vw, 560px)' }}>
                {taxPrev && (
                    <div className="grid">
                        <div className="col-6"><b>UF</b> {taxPrev.ufOrigem || '—'} → {taxPrev.ufDestino || '—'}</div>
                        <div className="col-6"><b>Total impostos</b> {moeda(taxPrev.totalImpostos)}</div>
                        <div className="col-4"><b>ICMS</b> {moeda(taxPrev.totalIcms)}</div>
                        <div className="col-4"><b>PIS</b> {moeda(taxPrev.totalPis)}</div>
                        <div className="col-4"><b>COFINS</b> {moeda(taxPrev.totalCofins)}</div>
                        <div className="col-4"><b>DIFAL</b> {moeda(taxPrev.totalDifal)}</div>
                        <div className="col-4"><b>ICMS-ST</b> {moeda(taxPrev.totalIcmsSt)}</div>
                        <div className="col-12 text-color-secondary text-sm">Cadastre regras em Fiscal → Regras tributárias (NCM/CFOP). Não emite NFe.</div>
                    </div>
                )}
            </Dialog>
        </div>
    );
};

export default Vendas;

