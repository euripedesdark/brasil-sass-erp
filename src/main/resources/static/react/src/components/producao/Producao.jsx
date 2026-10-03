import React, { useState, useEffect } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { AutoComplete } from 'primereact/autocomplete';
import ProdutoService from '../../services/ProdutoService';
import EstruturaProdutoService from '../../services/EstruturaProdutoService';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';
import { RomaneioProducao } from './RomaneioProducao';

export const Producao = () => {
    const { user } = useAuth();
    const [ordens, setOrdens] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [produtosSugestoes, setProdutosSugestoes] = useState([]);
    const [estrutura, setEstrutura] = useState([]);

    const [novoPedido, setNovoPedido] = useState({
        numero: '',
        tipoProducao: 'INDUSTRIA',
        produtoFinalId: null,
        quantidadePlanejada: 0,
        unidadeMedida: 'KG',
        densidade: null,
        itens: []
    });

    const [itemAtual, setItemAtual] = useState({ produtoId: null, produto: null, quantidade: 0 });

    useEffect(() => {
        fetchOrdens();
    }, []);

    const fetchOrdens = async () => {
        setLoading(true);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/producao?empresaId=${user?.empresaId}`);
            const data = await response.json();
            setOrdens(data);
        } catch (err) {
            console.error('Erro ao carregar ordens de produção', err);
        } finally {
            setLoading(false);
        }
    };

    const buscarProdutos = async (event) => {
        const q = (event.query || '').trim();
        if (!q) return setProdutosSugestoes([]);
        try {
            const r = await ProdutoService.buscarPorNome(q, 0, 20);
            const d = r?.data?.data?.content ?? r?.data?.content ?? r?.data?.data ?? r?.data ?? [];
            setProdutosSugestoes(Array.isArray(d) ? d : []);
        } catch { setProdutosSugestoes([]); }
    };

    const carregarEstrutura = async (produtoId) => {
        setEstrutura([]);
        if (!produtoId) return;
        try {
            const r = await EstruturaProdutoService.listar(produtoId);
            setEstrutura(r.data?.data ?? r.data ?? []);
        } catch (e) {
            setError(e.response?.data?.message || 'Não foi possível carregar a BOM.');
        }
    };

    const adicionarItem = () => {
        if (itemAtual.produtoId && itemAtual.quantidade > 0) {
            setNovoPedido(prev => ({
                ...prev,
                itens: [...prev.itens, { ...itemAtual }]
            }));
            setItemAtual({ produtoId: null, produto: null, quantidade: 0 });
        }
    };

    const removerItem = (index) => {
        setNovoPedido(prev => ({
            ...prev,
            itens: prev.itens.filter((_, i) => i !== index)
        }));
    };

    const salvarOrdem = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/producao?empresaId=${user?.empresaId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(novoPedido)
            });

            if (response.ok) {
                setSuccess(true);
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchOrdens();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
            }
        } catch (err) {
            setError('Erro ao salvar ordem de produção');
        } finally {
            setLoading(false);
        }
    };

    const finalizarOrdem = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/producao/${id}/finalizar?empresaId=${user?.empresaId}`, {
                method: 'POST'
            });

            if (response.ok) {
                fetchOrdens();
            } else {
                alert('Erro ao finalizar produção: ' + await response.text());
            }
        } catch (err) {
            alert('Erro de conexão ao finalizar produção');
        }
    };

    const statusTemplate = (rowData) => {
        const severity = {
            'ABERTO': 'info',
            'EM_PROCESSO': 'warning',
            'FINALIZADO': 'success',
            'CANCELADO': 'danger'
        }[rowData.status] || 'info';

        return <Tag value={rowData.status} severity={severity} />;
    };

    const finalizarTemplate = (rowData) => {
        return (
            <Button
                icon="pi pi-check"
                className="p-button-success p-button-text"
                onClick={() => finalizarOrdem(rowData.id)}
                disabled={rowData.status === 'FINALIZADO' || rowData.status === 'CANCELADO'}
            />
        );
    };

    return (
        <div className="producao-enterprise-container">
            <Card title="Controle de Produção Industrial & Agrícola" className="prod-main-card">
                <div className="prod-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="prod-info">
                        <p className="text-muted m-0">Gestão de insumos, processamento e entrada de produtos acabados.</p>
                    </div>
                    <Button label="Nova Ordem de Produção" icon="pi pi-plus" onClick={() => setDialogVisible(true)} className="p-button-success" />
                </div>

                <DataTable
                    value={ordens}
                    loading={loading}
                    paginator
                    rows={10}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                >
                    <Column field="numero" header="Nº Ordem" sortable style={{ width: '15%' }}></Column>
                    <Column field="tipoProducao" header="Tipo" sortable style={{ width: '15%' }}></Column>
                    <Column field="produtoFinalId" header="Produto Final" sortable style={{ width: '20%' }}></Column>
                    <Column field="quantidadePlanejada" header="Qtd Planejada" sortable style={{ width: '15%' }}></Column>
                    <Column field="unidadeMedida" header="Unidade" sortable style={{ width: '10%' }}></Column>
                    <Column field="status" header="Status" body={statusTemplate} sortable style={{ width: '15%' }}></Column>
                    <Column header="Finalizar" body={finalizarTemplate} style={{ width: '10%' }}></Column>
                </DataTable>
            </Card>

            <RomaneioProducao empresaId={user?.empresaId} ordens={ordens} />

            <Dialog
                header="Lançamento de Ordem de Produção"
                visible={dialogVisible}
                style={{ width: '700px' }}
                onHide={() => setDialogVisible(false)}
            >
                <div className="p-fluid">
                    <div className="grid">
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Número da Ordem</label>
                            <InputText value={novoPedido.numero} onChange={(e) => setNovoPedido({...novoPedido, numero: e.target.value})} placeholder="OP-2026-001" />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Tipo de Produção</label>
                            <Dropdown
                                value={novoPedido.tipoProducao}
                                options={['INDUSTRIA', 'PECUARIA', 'AGRICULTURA']}
                                onChange={(e) => setNovoPedido({...novoPedido, tipoProducao: e.value})}
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Produto Final *</label>
                            <AutoComplete value={novoPedido.produtoFinal || null} suggestions={produtosSugestoes} completeMethod={buscarProdutos}
                                itemTemplate={p => <div><strong>{p.nome || p.descricao}</strong><small className="ml-2">{p.codigo || p.id}</small></div>}
                                selectedItemTemplate={p => p ? `${p.nome || p.descricao} · ${p.codigo || p.id}` : ''}
                                onChange={e => { setNovoPedido({...novoPedido, produtoFinal: e.value, produtoFinalId: e.value?.id || null}); carregarEstrutura(e.value?.id); }}
                                placeholder="Pesquisar produto" />
                        </div>
                        <div className="col-12 md:col-3 field">
                            <label className="font-bold mb-2 block">Quantidade</label>
                            <InputNumber value={novoPedido.quantidadePlanejada} onValueChange={(e) => setNovoPedido({...novoPedido, quantidadePlanejada: e.value})} />
                        </div>
                        <div className="col-12 md:col-3 field">
                            <label className="font-bold mb-2 block">Unidade</label>
                            <InputText value={novoPedido.unidadeMedida} onChange={(e) => setNovoPedido({...novoPedido, unidadeMedida: e.target.value})} placeholder="KG, L, UN" />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Densidade / Volume (Opcional)</label>
                            <InputNumber value={novoPedido.densidade} onValueChange={(e) => setNovoPedido({...novoPedido, densidade: e.value})} />
                        </div>
                    </div>

                    <Divider />
                    <h3 className="mb-3">Composição de Insumos (Matéria Prima)</h3>
                    {estrutura.length > 0 && <div className="mb-3">
                        <Message severity="info" text={`${estrutura.length} componente(s) encontrados na BOM.`} />
                        <Button label="Aplicar BOM" icon="pi pi-sitemap" className="p-button-outlined mt-2" onClick={() => setNovoPedido(prev => ({...prev, itens: estrutura.map(x => ({produtoId: x.produtoFilhoId, quantidade: Number(prev.quantidadePlanejada || 0) * Number(x.quantidade || 0) * (1 + Number(x.perdaPercentual || 0) / 100)}))}))} />
                    </div>}

                    <div className="grid align-items-end mb-4">
                        <div className="col-12 md:col-5 field">
                            <label className="font-bold mb-2 block">Insumo</label>
                            <AutoComplete value={itemAtual.produto || null} suggestions={produtosSugestoes} completeMethod={buscarProdutos}
                                itemTemplate={p => <div><strong>{p.nome || p.descricao}</strong><small className="ml-2">{p.codigo || p.id}</small></div>}
                                selectedItemTemplate={p => p ? `${p.nome || p.descricao} · ${p.codigo || p.id}` : ''}
                                onChange={e => setItemAtual({...itemAtual, produto: e.value, produtoId: e.value?.id || null})}
                                placeholder="Pesquisar insumo" />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Quantidade Necessária</label>
                            <InputNumber value={itemAtual.quantidade} onChange={(e) => setItemAtual({...itemAtual, quantidade: e.value})} />
                        </div>
                        <div className="col-12 md:col-3 field">
                            <Button label="Adicionar" icon="pi pi-plus" onClick={adicionarItem} className="p-button-outlined" />
                        </div>
                    </div>

                    <DataTable value={novoPedido.itens} className="p-datatable-sm mb-4" responsiveLayout="scroll">
                        <Column field="produtoId" header="ID Insumo"></Column>
                        <Column field="quantidade" header="Quantidade"></Column>
                        <Column body={(rowData, options) => (
                            <Button icon="pi pi-trash" className="p-button-danger p-button-text" onClick={() => removerItem(options.rowIndex)} />
                        )}></Column>
                    </DataTable>

                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Ordem de produção criada com sucesso!" className="w-full mb-3" />}

                    <div className="flex justify-content-end gap-2 mt-4">
                        <Button label="Cancelar" icon="pi pi-times" className="p-button-text" onClick={() => setDialogVisible(false)} />
                        <Button label="Salvar Ordem" icon="pi pi-save" onClick={salvarOrdem} loading={loading} />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Producao;
