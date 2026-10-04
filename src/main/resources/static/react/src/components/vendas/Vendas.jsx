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
    // id da linha em operacao. Sem isso, dois cliques em "Faturar" chamam o
    // backend duas vezes: a segunda falha por falta de estoque e o usuario ve
    // dois toasts, sem entender o que aconteceu. A baixa de estoque, o titulo,
    // as parcelas e a comissao ja foram gravados na primeira.
    const [emAcao, setEmAcao] = useState(null);
    const [error, setError] = useState('');
    const [somenteLeitura, setSomenteLeitura] = useState(false);
    const [clientes, setClientes] = useState([]);
    const [clientesSugestoes, setClientesSugestoes] = useState([]);
    const [vendedores, setVendedores] = useState([]);
    const [condicoesPagamento, setCondicoesPagamento] = useState([]);
    const [produtosSugestoes, setProdutosSugestoes] = useState([]);
    const [produtoSelecionado, setProdutoSelecionado] = useState(null);

    const [form, setForm] = useState(emptyForm());
    const [itemAtual, setItemAtual] = useState(emptyItem());

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
            setVendedores((Array.isArray(vendedoresData) ? vendedoresData : [])
                .filter(v => v.tipoColaborador === 'VENDEDOR'));
            const condicoesData = condicoesRes?.data?.data ?? condicoesRes?.data ?? [];
            setCondicoesPagamento(Array.isArray(condicoesData) ? condicoesData : []);
        } catch (err) {
            console.error(t('legacyUi.vendasLegacy.loadAux'), err);
            toast.current?.show({
                severity: 'warn',
                summary: t('common.warning'),
                detail: getApiErrorMessage(err, t('legacyUi.vendasLegacy.loadAux')),
                life: 3500
            });
        }
    };

    const buscarClientes = (event) => {
        const query = (event.query || '').toLowerCase().trim();
        setClientesSugestoes(
            clientes.filter(c => {
                const p = c.pessoa || {};
                return !query ||
                    String(c.id).includes(query) ||
                    String(c.codigo || '').toLowerCase().includes(query) ||
                    String(p.nome || '').toLowerCase().includes(query) ||
                    String(p.documento || '').includes(query);
            }).slice(0, 20)
        );
    };

    const buscarProdutos = async (event) => {
        const query = (event.query || '').trim();
        if (!query) {
            setProdutosSugestoes([]);
            return;
        }
        try {
            const res = await ProdutoService.buscarPorNome(query, 0, 20);
            const data = res?.data?.data?.content ?? res?.data?.content ?? res?.data?.data ?? res?.data ?? [];
            setProdutosSugestoes(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
            setProdutosSugestoes([]);
        }
    };

    const clienteLabel = (c) => {
        const p = c?.pessoa || {};
        return c ? `${p.nome || 'Cliente'} · ${p.documento || c.codigo || c.id}` : '';
    };

    const produtoLabel = (p) => p ? `${p.nome || p.descricao || 'Produto'} · ${p.codigo || p.id}` : '';

    const fetchPedidos = async () => {
        setLoading(true);
        try {
            const res = await PedidoVendaService.listarPorEmpresa(empresaId);
            const data = res?.data?.data ?? res?.data ?? [];
            setPedidos(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
            toast.current?.show({
                severity: 'error', summary: 'Erro',
                detail: getApiErrorMessage(err, t('legacyUi.vendasLegacy.loadOrders')),
                life: 3000,
            });
            setPedidos([]);
        } finally {
            setLoading(false);
        }
    };

    const adicionarItem = () => {
        if (!itemAtual.quantidade || itemAtual.quantidade <= 0 || !itemAtual.valorUnitario) {
            setError(t('legacyUi.vendasLegacy.itemRequired'));
            return;
        }
        if (!itemAtual.produtoId && !itemAtual.servicoId) {
            setError(t('legacyUi.vendasLegacy.productRequired'));
            return;
        }
        setForm((prev) => ({
            ...prev, itens: [...prev.itens, { ...itemAtual, numeroItem: prev.itens.length + 1 }],
        }));
        setItemAtual(emptyItem());
        setError('');
    };

    const removerItem = (index) => {
        setForm((prev) => ({
            ...prev,
            itens: prev.itens.filter((_, i) => i !== index).map((it, i) => ({ ...it, numeroItem: i + 1 })),
        }));
    };

    const totalItens = () => form.itens.reduce((acc, it) => {
        const q = Number(it.quantidade) || 0;
        const vu = Number(it.valorUnitario) || 0;
        const d = Number(it.valorDesconto) || 0;
        return acc + (q * vu - d);
    }, 0);

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
        } finally {
            setLoading(false);
        }
    };

    // Faturar grava quatro coisas de uma vez (baixa de estoque, titulo,
    // parcelas e comissao) e nao tem como desfazer pela tela. Por isso
    // pergunta antes, e trava a linha enquanto a chamada acontece.
    const perguntarFaturar = (row) => {
        confirmDialog.require({
            header: t('common.confirm'),
            message: `Faturar o pedido ${row.numero || row.id}? Isso vai dar baixa no estoque, criar o titulo e as parcelas, e registrar a comissao do vendedor. Nao da para desfazer pela tela.`,
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('legacyUi.vendasLegacy.yesBill'),
            rejectLabel: 'Cancelar',
            accept: () => faturar(row.id),
        });
    };

    const faturar = async (id) => {
        setEmAcao(id);

        try {
            await PedidoVendaService.faturar(id);
            toast.current?.show({
                severity: 'success', summary: 'Faturado',
                detail: 'Estoque baixado, titulo e comissao gerados', life: 3500,
            });
            fetchPedidos();
        } catch (err) {
            const msg = getApiErrorMessage(err, t('legacyUi.vendasLegacy.billError'));
            setError(msg);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg, life: 4000 });
        } finally {
            setEmAcao(null);
        }
    };

    const confirmar = async (id) => {
        try {
            await PedidoVendaService.confirmar(id);
            toast.current?.show({
                severity: 'success', summary: t('legacyUi.vendasLegacy.quoteSuccess'),
                detail: 'Orçamento convertido em pedido. Agora pode ser faturado.', life: 3500,
            });
            fetchPedidos();
        } catch (err) {
            const msg = getApiErrorMessage(err, t('legacyUi.vendasLegacy.quoteError'));
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg, life: 4000 });
        }
    };

    const [dlgPos, setDlgPos] = useState(false);
    const [posId, setPosId] = useState(null);
    const [posMotivo, setPosMotivo] = useState('');
    const [posEquip, setPosEquip] = useState('');
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
            const msg = getApiErrorMessage(err, t('legacyUi.vendasLegacy.cancelError'));
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg, life: 4000 });
        }
    };

    const [dlgPos, setDlgPos] = useState(false);
    const [posId, setPosId] = useState(null);
    const [posMotivo, setPosMotivo] = useState('');
    const [posEquip, setPosEquip] = useState('');
    const abrirPosvenda = (row) => { setPosId(row.id); setPosMotivo(''); setPosEquip(''); setDlgPos(true); };
    const confirmarPosvenda = async () => {
        if (!posMotivo.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o motivo', life: 3000 }); return; }
        try {
            const r = await PedidoVendaService.posvenda(posId, { motivo: posMotivo, equipamento: posEquip || null });
            toast.current?.show({ severity: 'success', summary: 'OS aberta', detail: 'OS ' + ((r.data && (r.data.osNumero || r.data.osId)) || ''), life: 4000 });
            setDlgPos(false);
        } catch (err) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: getApiErrorMessage(err, 'Falha ao abrir OS'), life: 4000 }); }
    };
    const abrirNovo = () => {
        setForm(emptyForm()); setItemAtual(emptyItem()); setError('');
        setSomenteLeitura(false); setDialogVisible(true);
    };

    const abrirVer = (row) => {
        setForm({
            clienteId: row.clienteId, vendedorId: null, tipo: row.tipo || 'PEDIDO',
            dataEmissao: row.dataEmissao ? new Date(row.dataEmissao) : null,
            dataEntrega: row.dataEntrega ? new Date(row.dataEntrega) : null,
            condicaoPagamentoId: null, observacao: row.observacao || '',
            valorDesconto: 0, valorFrete: 0,
            itens: (row.itens || []).map((it) => ({
                numeroItem: it.numeroItem, produtoId: it.produtoId, servicoId: it.servicoId,
                descricao: it.descricao, quantidade: it.quantidade, unidade: it.unidade,
                valorUnitario: it.valorUnitario, valorDesconto: it.valorDesconto,
            })),
        });
        setSomenteLeitura(true); setError(''); setDialogVisible(true);
    };

    const statusBody = (row) => {
        const s = STATUS_LABEL[row.status] || { label: row.status, severity: 'secondary' };
        return <Tag value={s.label} severity={s.severity} />;
    };

    const moeda = (v) => (v == null ? 0 : Number(v)).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const dataBr = (d) => (d ? new Date(d).toLocaleDateString('pt-BR') : '');

    const acoes = (row) => (
        <div className="flex gap-1">
            <Button icon="pi pi-eye" className="p-button-text p-button-sm" tooltip="Ver" onClick={() => abrirVer(row)} />
            {row.status === 'ABERTO' && row.tipo === 'ORCAMENTO' && (
                <>
                    <Button icon="pi pi-check" className="p-button-text p-button-warning p-button-sm"
                        tooltip="Confirmar orçamento" disabled={emAcao === row.id} onClick={() => confirmar(row.id)} />
                    <Button icon="pi pi-times" className="p-button-text p-button-danger p-button-sm"
                        tooltip="Cancelar orçamento" disabled={emAcao === row.id} onClick={() => cancelar(row.id)} />
                </>
            )}
            {row.status === 'ABERTO' && row.tipo === 'PEDIDO' && (
                <>
                    <Button icon="pi pi-file-invoice" className="p-button-text p-button-success p-button-sm"
                        tooltip="Faturar (estoque + título)" loading={emAcao === row.id}
                        disabled={emAcao === row.id} onClick={() => perguntarFaturar(row)} />
                    <Button icon="pi pi-times" className="p-button-text p-button-danger p-button-sm"
                        tooltip="Cancelar pedido" onClick={() => cancelar(row.id)} />
                </>
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
                    <p className="m-0 text-color-secondary">
                        Fluxo: orçamento → confirmar → pedido → faturar (estoque + contas a receber) ou cancelar.
                    </p>
                    <Button label={t('legacyUi.vendasLegacy.new')} icon="pi pi-plus" onClick={abrirNovo} />
                </div>
                <DataTable value={pedidos} loading={loading} paginator rows={10} rowsPerPageOptions={[10, 25, 50]}
                    emptyMessage={t('legacyUi.vendasLegacy.empty')} size="small">
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
                footer={<div>
                    <Button label="Fechar" className="p-button-text" onClick={() => setDialogVisible(false)} />
                    {!somenteLeitura && <Button label="Salvar pedido" icon="pi pi-save" onClick={salvar} loading={loading} />}
                </div>}>
                {error && <Message severity="error" text={error} className="w-full mb-3" />}
                <div className="grid p-fluid">
                    <div className="col-12 md:col-3 field"><label className="font-bold">Operação *</label>
                        <Dropdown value={form.tipo}
                            options={[{ label: 'Pedido de venda', value: 'PEDIDO' }, { label: 'Orçamento', value: 'ORCAMENTO' }]}
                            onChange={(e) => setForm({ ...form, tipo: e.value })}
                            disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 md:col-5 field"><label className="font-bold">Cliente *</label>
                        <AutoComplete value={clientes.find(c => c.id === form.clienteId) || null}
                            suggestions={clientesSugestoes} completeMethod={buscarClientes}
                            itemTemplate={c => <div><strong>{c?.pessoa?.nome || 'Cliente'}</strong><small className="ml-2 text-color-secondary">{c?.pessoa?.documento || c?.codigo || c?.id}</small></div>}
                            selectedItemTemplate={clienteLabel}
                            onChange={(e) => setForm({ ...form, clienteId: e.value?.id || null })}
                            placeholder="Nome, CPF/CNPJ ou código" disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 md:col-4 field"><label className="font-bold">Vendedor</label>
                        <Dropdown value={form.vendedorId} options={vendedores}
                            optionLabel="id" optionValue="id"
                            itemTemplate={v => <div>Vendedor #{v.id} · matrícula {v.matricula || 'sem matrícula'}</div>}
                            valueTemplate={v => v ? `Vendedor #${v}` : 'Selecione'}
                            onChange={(e) => setForm({ ...form, vendedorId: e.value })}
                            placeholder="Selecione o vendedor" showClear disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 md:col-4 field"><label className="font-bold">Condição de pagamento</label>
                        <Dropdown value={form.condicaoPagamentoId} options={condicoesPagamento}
                            optionLabel="descricao" optionValue="id"
                            onChange={(e) => setForm({ ...form, condicaoPagamentoId: e.value })}
                            placeholder="Selecione a condição" showClear disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 md:col-4 field"><label className="font-bold">Data emissão</label>
                        <Calendar value={form.dataEmissao} onChange={(e) => setForm({ ...form, dataEmissao: e.value })} dateFormat="dd/mm/yy" showIcon disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 md:col-4 field"><label className="font-bold">Data entrega</label>
                        <Calendar value={form.dataEntrega} onChange={(e) => setForm({ ...form, dataEntrega: e.value })} dateFormat="dd/mm/yy" showIcon disabled={somenteLeitura} />
                    </div>
                    <div className="col-12 field"><label className="font-bold">Observação</label>
                        <InputText value={form.observacao} onChange={(e) => setForm({ ...form, observacao: e.target.value })} disabled={somenteLeitura} />
                    </div>
                </div>
                {!somenteLeitura && <>
                    <h4 className="mt-3">Itens</h4>
                    <div className="grid p-fluid">
                        <div className="col-6 md:col-2 field"><label>Produto *</label>
                            <AutoComplete
                                value={produtosSugestoes.find(p => p.id === itemAtual.produtoId) || null}
                                suggestions={produtosSugestoes}
                                completeMethod={buscarProdutos}
                                field="nome"
                                itemTemplate={p => <div><strong>{p.nome}</strong><small className="ml-2 text-color-secondary">{p.codigo || p.id}</small></div>}
                                selectedItemTemplate={produtoLabel}
                                onChange={e => {
                                    const p = e.value;
                                    setItemAtual({
                                        ...itemAtual,
                                        produtoId: p?.id || null,
                                        descricao: p?.nome || itemAtual.descricao,
                                        unidade: p?.unidadeSigla || itemAtual.unidade,
                                        valorUnitario: p?.precoVenda ?? itemAtual.valorUnitario
                                    });
                                }}
                                placeholder="Digite nome ou código"
                            /></div>
                        <div className="col-6 md:col-3 field"><label>Descrição</label>
                            <InputText value={itemAtual.descricao} onChange={(e) => setItemAtual({ ...itemAtual, descricao: e.target.value })} /></div>
                        <div className="col-4 md:col-2 field"><label>Qtd</label>
                            <InputNumber value={itemAtual.quantidade} onValueChange={(e) => setItemAtual({ ...itemAtual, quantidade: e.value })} min={0.001} minFractionDigits={0} maxFractionDigits={3} /></div>
                        <div className="col-4 md:col-2 field"><label>Vlr unit.</label>
                            <InputNumber value={itemAtual.valorUnitario} onValueChange={(e) => setItemAtual({ ...itemAtual, valorUnitario: e.value })} mode="currency" currency="BRL" locale="pt-BR" /></div>
                        <div className="col-4 md:col-2 field flex align-items-end"><Button label="Add" icon="pi pi-plus" onClick={adicionarItem} /></div>
                    </div>
                </>}
                <DataTable value={form.itens} emptyMessage="Sem itens" size="small" className="mt-2">
                    <Column field="numeroItem" header="#" style={{ width: '50px' }} />
                    <Column field="produtoId" header="Produto" /><Column field="descricao" header="Descricao" />
                    <Column field="quantidade" header="Qtd" />
                    <Column field="valorUnitario" header="Unit." body={(r) => moeda(r.valorUnitario)} />
                    {!somenteLeitura && <Column body={(_, { rowIndex }) => <Button icon="pi pi-trash" className="p-button-text p-button-danger p-button-sm" onClick={() => removerItem(rowIndex)} />} style={{ width: '60px' }} />}
                </DataTable>
                <div className="text-right mt-3 font-bold">Subtotal itens: {moeda(totalItens())}</div>
            </Dialog>
        </div>
    );
};

            <Dialog visible={dlgPos} onHide={() => setDlgPos(false)} header='Pós-venda: abrir OS' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Equipamento (opcional)</label><InputText value={posEquip} onChange={(e) => setPosEquip(e.target.value)} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Motivo *</label><InputTextarea rows={3} value={posMotivo} onChange={(e) => setPosMotivo(e.target.value)} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgPos(false)} /><Button label='Abrir OS' icon='pi pi-check' onClick={confirmarPosvenda} /></div>
            </Dialog>
export default Vendas;
