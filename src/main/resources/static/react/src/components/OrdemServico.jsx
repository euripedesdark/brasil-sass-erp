import { useTranslation } from 'react-i18next';
import React, { useState, useEffect } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Toast } from 'primereact/toast';
import { Calendar } from 'primereact/calendar';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { AutoComplete } from 'primereact/autocomplete';
import ApiConfig, { apiFetch } from '../services/ApiConfig';
export default function OrdemServico() {
    const { t, i18n } = useTranslation();
    const [osList, setOsList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 20,
        sortField: 'dataMov',
        sortOrder: 1
    });
    
    const [filtroCliente, setFiltroCliente] = useState('');
    const [clientesSugestoes, setClientesSugestoes] = useState([]);
    const [produtosSugestoes, setProdutosSugestoes] = useState([]);
    const [servicosSugestoes, setServicosSugestoes] = useState([]);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [osSelecionada, setOsSelecionada] = useState(null);
    const [emitirNota, setEmitirNota] = useState(false);
    const toast = React.useRef(null);
    // apontamento de horas: o tecnico registra quanto trabalhou na OS
    const [apontando, setApontando] = useState(false);
    const [apontamento, setApontamento] = useState({ horas: 1, descricao: '' });

    useEffect(() => {
        carregarOS();
    }, [lazyParams]);

    const carregarOS = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows
            });
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/servicos/os?${params}`);
            if (response.ok) {
                const data = await response.json();
                // Backend retorna PageResponse { content, totalElements }
                const lista = Array.isArray(data) ? data : (data.content || []);
                setOsList(lista);
                setTotalRecords(Array.isArray(data) ? data.length : (data.totalElements ?? lista.length));
            } else {
                setOsList([]);
                setTotalRecords(0);
                toast.current?.show({
                    severity: 'warn',
                    summary: t('common.warning'),
                    detail: t('legacyUi.os.loadError')
                });
            }
        } catch (error) {
            setOsList([]);
            setTotalRecords(0);
            toast.current.show({
                severity: 'error',
                summary: t('legacyUi.os.error'),
                detail: t('legacyUi.os.loadError')
            });
        } finally {
            setLoading(false);
        }
    };

    const onLazyLoad = (event) => {
        setLazyParams({
            page: event.first / event.rows,
            rows: event.rows,
            sortField: event.sortField,
            sortOrder: event.sortOrder
        });
    };

    const buscarClientes = async (event) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/clientes?size=20`);
            if (response.ok) {
                const data = await response.json();
                const page = data?.data ?? data;
                const lista = Array.isArray(page) ? page : (page?.content || []);
                setClientesSugestoes(lista.map(c => ({
                    nome: c.nome || c.razaoSocial || `Cliente ${c.id}`,
                    id: c.id
                })).filter(c =>
                    c.nome.toLowerCase().includes((event.query || '').toLowerCase())
                ));
            } else {
                setClientesSugestoes([]);
            }
        } catch (error) {
            console.error('Erro ao buscar clientes', error);
            setClientesSugestoes([]);
        }
    };

    const buscarProdutos = async (event) => {
        try {
            const termo = event.query || '';
            const response = await apiFetch(
                `${ApiConfig.BASE_URL}/api/cadastro/produtos?nome=${encodeURIComponent(termo)}&ativo=true&size=20`
            );
            if (!response.ok) return setProdutosSugestoes([]);
            const data = await response.json();
            const page = data?.data ?? data;
            setProdutosSugestoes(Array.isArray(page) ? page : (page?.content || []));
        } catch {
            setProdutosSugestoes([]);
        }
    };

    const buscarServicos = async (event) => {
        try {
            const termo = event.query || '';
            const response = await apiFetch(
                `${ApiConfig.BASE_URL}/api/cadastro/servicos?nome=${encodeURIComponent(termo)}&ativo=true&size=20`
            );
            if (!response.ok) return setServicosSugestoes([]);
            const data = await response.json();
            const page = data?.data ?? data;
            setServicosSugestoes(Array.isArray(page) ? page : (page?.content || []));
        } catch {
            setServicosSugestoes([]);
        }
    };

    const abrirNovaOS = () => {
        setOsSelecionada({
            dataMov: new Date(),
            status: 'ABERTA',
            clienteId: null,
            cliente: null,
            equipamento: '',
            descricao: '',
            itens: []
        });
        setEmitirNota(false);
        setDialogVisible(true);
    };

    const abrirEdicao = async (os) => {
        try {
            const [itensResponse, clienteResponse] = await Promise.all([
                apiFetch(`${ApiConfig.BASE_URL}/api/servicos/os/${os.id}/itens`),
                os.clienteId ? apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/clientes/${os.clienteId}`) : Promise.resolve(null)
            ]);
            const itens = itensResponse?.ok ? await itensResponse.json() : [];
            let cliente = null;
            if (clienteResponse?.ok) {
                const body = await clienteResponse.json();
                const data = body?.data ?? body;
                cliente = {
                    id: data?.id,
                    nome: data?.nome || data?.razaoSocial || ('Cliente ' + os.clienteId)
                };
            }
            setOsSelecionada({
                ...os,
                cliente,
                dataMov: os.dataMov ? new Date(os.dataMov) : (os.aberturaAt ? new Date(os.aberturaAt) : new Date()),
                itens: Array.isArray(itens) ? itens.map(item => ({
                    ...item,
                    totalItem: item.valorTotal ?? ((item.quantidade || 0) * (item.valorUnitario || 0))
                })) : []
            });
        } catch {
            setOsSelecionada({ ...os, itens: [] });
        }
        setDialogVisible(true);
    };

    const salvarOS = async () => {
        try {
            if (!osSelecionada?.clienteId) {
                toast.current?.show({ severity: 'warn', summary: t('legacyUi.os.customer'), detail: t('legacyUi.os.selectCustomer'), life: 4000 });
                return;
            }

            const isEdicao = !!osSelecionada.id;
            const payload = {
                clienteId: osSelecionada.clienteId,
                equipamento: osSelecionada.equipamento || null,
                descricao: osSelecionada.descricao || null,
                previsaoAt: osSelecionada.dataMov ? new Date(osSelecionada.dataMov).toISOString() : new Date().toISOString()
            };
            const url = isEdicao
                ? `${ApiConfig.BASE_URL}/api/servicos/os/${osSelecionada.id}`
                : `${ApiConfig.BASE_URL}/api/servicos/os`;

            const response = await apiFetch(url, {
                method: isEdicao ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const body = await response.json().catch(() => null);
                const criada = body?.data ?? body;
                const osId = criada?.id ?? osSelecionada.id;
                const novosItens = (osSelecionada.itens || []).filter(item => !item.id);

                for (const item of novosItens) {
                    if (!item.produtoId && !item.servicoId) {
                        throw new Error('Cada item precisa apontar para um produto ou serviço do cadastro.');
                    }
                    const itemResponse = await apiFetch(`${ApiConfig.BASE_URL}/api/servicos/os/${osId}/itens`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            produtoId: item.produtoId || null,
                            servicoId: item.servicoId || null,
                            quantidade: item.quantidade || 1,
                            valorUnitario: item.valorUnitario || 0
                        })
                    });
                    if (!itemResponse.ok) {
                        const errorBody = await itemResponse.text();
                        throw new Error(errorBody || 'Não foi possível gravar um item da OS.');
                    }
                }
                toast.current.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('legacyUi.os.saveSuccess')
                });

                if (emitirNota) {
                    toast.current?.show({
                        severity: 'info',
                        summary: t('legacyUi.os.fiscalTitle'),
                        detail: t('legacyUi.os.fiscalHelp'),
                        life: 6000
                    });
                }

                setDialogVisible(false);
                carregarOS();
            } else {
                const err = await response.text();
                toast.current.show({
                    severity: 'error',
                    summary: t('legacyUi.os.error'),
                    detail: err || t('legacyUi.os.saveError')
                });
            }
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('legacyUi.os.error'),
                detail: t('legacyUi.os.saveError')
            });
        }
    };

    const adicionarItem = () => {
        const novoItem = {
            produtoId: null,
            servicoId: null,
            produto: null,
            servico: null,
            descricao: '',
            quantidade: 1,
            valorUnitario: 0,
            totalItem: 0
        };
        setOsSelecionada({
            ...osSelecionada,
            itens: [...(osSelecionada.itens || []), novoItem]
        });
    };

    const removerItem = (index) => {
        const itensAtualizados = osSelecionada.itens.filter((_, i) => i !== index);
        setOsSelecionada({...osSelecionada, itens: itensAtualizados});
    };

    const atualizarItem = (index, campo, valor) => {
        const itensAtualizados = [...osSelecionada.itens];
        itensAtualizados[index][campo] = valor;
        
        // Recalcular total do item
        if (campo === 'quantidade' || campo === 'valorUnitario') {
            const qtd = campo === 'quantidade' ? valor : itensAtualizados[index].quantidade;
            const vlr = campo === 'valorUnitario' ? valor : itensAtualizados[index].valorUnitario;
            itensAtualizados[index].totalItem = qtd * vlr;
        }
        
        setOsSelecionada({...osSelecionada, itens: itensAtualizados});
    };

    const calcularTotalOS = () => {
        if (!osSelecionada?.itens) return 0;
        return osSelecionada.itens.reduce((acc, item) => acc + (item.totalItem || 0), 0);
    };

    const formatarMoeda = (valor) => {
        return valor.toLocaleString(i18n.language, { style: 'currency', currency: 'BRL' });
    };

    const formatarData = (data) => {
        if (!data) return '';
        return new Date(data).toLocaleDateString(i18n.language);
    };

    const statusTemplate = (rowData) => {
        const status = rowData.status || 'ABERTA';
        const fechado = status === 'FECHADA' || status === 'CANCELADA';
        const statusLabel = status === 'FECHADA' ? t('legacyUi.os.statusClosed') : status === 'CANCELADA' ? t('legacyUi.os.statusCancelled') : t('legacyUi.os.statusOpen');
        return <span className={`status-badge ${fechado ? 'status-baixado' : 'status-aberto'}`}>{statusLabel}</span>;
    };

    const fecharOS = async (os) => {
        try {
            const laudo = window.prompt(t('legacyUi.os.technicalReport')) ?? '';
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/servicos/os/${os.id}/fechar?laudo=${encodeURIComponent(laudo)}`, {
                method: 'POST'
            });
            if (response.ok) {
                toast.current.show({ severity: 'success', summary: t('messages.success'), detail: t('legacyUi.os.closed') });
                carregarOS();
            } else {
                const err = await response.text();
                toast.current.show({ severity: 'error', summary: t('legacyUi.os.error'), detail: err || t('legacyUi.os.closeError') });
            }
        } catch (error) {
            toast.current.show({ severity: 'error', summary: t('legacyUi.os.error'), detail: t('legacyUi.os.closeError') });
        }
    };

    /**
     * Impressao da OS em PDF.
     *
     * O botao "Imprimir OS" estava na tela desde o inicio sem onClick: aparecia,
     * nao fazia nada e nao dava nenhuma pista do motivo. O endpoint
     * /api/servicos/os/{id}/pdf ja existia e continua sem uso.
     *
     * O download e por fetch e nao por window.open porque a rota exige o token:
     * abrir a URL direto devolveria 401 e o navegador mostraria o JSON de erro
     * no lugar do PDF.
     */
    const imprimirOS = async (os) => {
        try {
            const resposta = await apiFetch(`${ApiConfig.BASE_URL}/api/servicos/os/${os.id}/pdf`);
            if (!resposta.ok) {
                throw new Error(t('legacyUi.os.pdfError'));
            }
            const blob = await resposta.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `os-${os.numero || os.id}.pdf`;
            a.click();
            URL.revokeObjectURL(url);
        } catch (error) {
            toast.current.show({ severity: 'error', summary: t('legacyUi.os.print'), detail: t('legacyUi.os.printError') });
        }
    };

    /** Registra horas trabalhadas na OS. */
    const apontarOS = async (os) => {
        const horas = Number(apontamento.horas);
        if (!(horas > 0)) {
            toast.current.show({ severity: 'warn', summary: t('legacyUi.os.timeEntry'), detail: t('legacyUi.os.hoursRequired') });
            return;
        }
        try {
            const resposta = await apiFetch(`${ApiConfig.BASE_URL}/api/servicos/os/${os.id}/apontamentos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ horas, descricao: apontamento.descricao || '' })
            });
            if (!resposta.ok) {
                const j = await resposta.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || t('legacyUi.os.pointingError'));
            }
            toast.current.show({ severity: 'success', summary: t('legacyUi.os.timeEntry'), detail: `${horas}h ${t('legacyUi.os.hoursRecorded')}` });
            setApontando(false);
            setApontamento({ horas: 1, descricao: '' });
            carregarOS();
        } catch (error) {
            toast.current.show({ severity: 'error', summary: t('legacyUi.os.timeEntry'), detail: error.message || t('legacyUi.os.pointingFailed') });
        }
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="acoes-container">
                <Button 
                    icon="pi pi-eye" 
                    className="p-button-info p-button-sm mr-2" 
                    onClick={() => abrirEdicao(rowData)}
                    tooltip={t('legacyUi.os.view')}
                />
                {rowData.status !== 'FECHADA' && rowData.status !== 'CANCELADA' && (
                    <>
                        <Button 
                            icon="pi pi-file-pdf" 
                            className="p-button-success p-button-sm mr-2" 
                            tooltip={t('legacyUi.os.print')}
                            onClick={() => imprimirOS(rowData)}
                        />
                        <Button 
                            icon="pi pi-clock" 
                            className="p-button-info p-button-sm mr-2" 
                            tooltip={t('legacyUi.os.pointHours')}
                            onClick={() => { setOsSelecionada(rowData); setApontando(true); }}
                        />
                        <Button 
                            icon="pi pi-receipt" 
                            className="p-button-warning p-button-sm" 
                            tooltip={t('legacyUi.os.issueInvoice')}
                            onClick={() => {
                                setOsSelecionada(rowData);
                                setEmitirNota(true);
                                setDialogVisible(true);
                            }}
                        />
                    </>
                )}
                {(rowData.status ? (rowData.status !== 'FECHADA' && rowData.status !== 'CANCELADA') : rowData.baixaMov === 'N') && rowData.id && (
                    <Button
                        icon="pi pi-check-circle"
                        className="p-button-secondary p-button-sm"
                        tooltip={t('legacyUi.os.close')}
                        onClick={() => fecharOS(rowData)}
                    />
                )}
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button label={t('legacyUi.os.cancel')} icon="pi pi-times" className="p-button-text" onClick={() => setDialogVisible(false)} />
            <Button label={t('legacyUi.os.savePrint')} icon="pi pi-check" className="p-button-success" onClick={salvarOS} />
            {osSelecionada?.status !== 'FECHADA' && osSelecionada?.status !== 'CANCELADA' && (
                <div className="mt-3">
                    <div className="p-checkbox">
                        <input 
                            type="checkbox" 
                            id="emitirNota" 
                            checked={emitirNota}
                            onChange={(e) => setEmitirNota(e.target.checked)}
                            className="p-checkbox-input"
                        />
                        <label htmlFor="emitirNota" className="p-checkbox-label ml-2">
                            {t('legacyUi.os.issueInvoiceAfterSave')}
                        </label>
                    </div>
                </div>
            )}
        </div>
    );

    return (
        <div className="ordem-servico-container">
            <Toast ref={toast} />
            
            <div className="card">
                <h2>{t('legacyUi.os.title')}</h2>
                
                <div className="filtros-container">
                    <div className="p-fluid p-formgrid p-grid">
                        <div className="p-field p-col-12 p-md-6">
                            <label htmlFor="cliente">{t('legacyUi.os.customer')}</label>
                            <AutoComplete
                                id="cliente"
                                value={filtroCliente}
                                suggestions={clientesSugestoes}
                                completeMethod={buscarClientes}
                                field="nome"
                                onChange={(e) => setFiltroCliente(e.value)}
                                placeholder={t('legacyUi.os.customerNamePlaceholder')}
                                dropdown
                                multiple
                            />
                        </div>
                        <div className="p-field p-col-12 p-md-6">
                            <label>&nbsp;</label>
                            <Button 
                                label={t('legacyUi.os.search')} 
                                icon="pi pi-search" 
                                onClick={carregarOS}
                                className="p-button-outlined"
                            />
                        </div>
                    </div>
                </div>
                
                <div className="toolbar-container">
                    <Button 
                        label={t('legacyUi.os.newOrder')} 
                        icon="pi pi-plus" 
                        className="p-button-success" 
                        onClick={abrirNovaOS}
                    />
                </div>
                
                <DataTable 
                    value={osList}
                    paginator
                    first={lazyParams.page * lazyParams.rows}
                    rows={lazyParams.rows}
                    totalRecords={totalRecords}
                    lazy
                    onPage={onLazyLoad}
                    onSort={onLazyLoad}
                    loading={loading}
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    currentPageReportTemplate={t('legacyUi.os.pageReport')}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    sortField={lazyParams.sortField}
                    sortOrder={lazyParams.sortOrder}
                    emptyMessage={t('legacyUi.os.emptyOrders')}
                >
                    <Column field="idMov" header={t('legacyUi.os.number')} sortable style={{ width: '100px' }} />
                    <Column field="cliente" header={t('legacyUi.os.customer')} sortable filter filterPlaceholder={t('legacyUi.os.filterCustomer')} style={{ width: '250px' }} />
                    <Column field="dataMov" header={t('legacyUi.os.date')} body={(row) => formatarData(row.aberturaAt || row.dataMov)} sortable style={{ width: '140px' }} />
                    <Column field="valorTotal" header={t('legacyUi.os.totalValue')} body={(row) => formatarMoeda(row.valorTotal)} sortable style={{ width: '120px' }} />
                    <Column field="status" header="Status" body={(row) => statusTemplate({ ...row, status: row.status || (row.baixaMov === 'N' ? 'ABERTA' : 'FECHADA') })} sortable style={{ width: '120px' }} />
                    <Column body={acoesTemplate} style={{ width: '200px' }} />
                </DataTable>
            </div>
            
            <Dialog 
                visible={dialogVisible} 
                style={{ width: '900px' }} 
                header={osSelecionada?.idMov ? t('legacyUi.os.editOrder', { id: osSelecionada.idMov }) : t('legacyUi.os.newOrder')}
                footer={dialogFooter}
                onHide={() => setDialogVisible(false)}
                maximizable
            >
                <div className="p-fluid">
                    <div className="p-grid">
                        <div className="p-col-8">
                            <div className="p-field">
                                <label htmlFor="cliente">{t('legacyUi.os.customerRequired')}</label>
                                <AutoComplete
                                    id="cliente"
                                    value={osSelecionada?.cliente}
                                    suggestions={clientesSugestoes}
                                    completeMethod={buscarClientes}
                                    field="nome"
                                    onChange={(e) => {
                                        const cliente = e.value;
                                        setOsSelecionada({
                                            ...osSelecionada,
                                            cliente,
                                            clienteId: cliente?.id ?? null
                                        });
                                    }}
                                    placeholder={t('legacyUi.os.searchCustomerPlaceholder')}
                                    dropdown
                                    required
                                />
                            </div>
                        </div>
                        <div className="p-col-4">
                            <div className="p-field">
                                <label htmlFor="dataMov">{t('legacyUi.os.orderDateRequired')}</label>
                                <Calendar 
                                    id="dataMov"
                                    value={osSelecionada?.dataMov}
                                    onChange={(e) => setOsSelecionada({...osSelecionada, dataMov: e.value})}
                                    dateFormat="dd/mm/yy"
                                    showIcon
                                    required
                                />
                            </div>
                        </div>
                    </div>
                    
                    <div className="p-field">
                        <label htmlFor="equipamento">{t('legacyUi.os.equipment')}</label>
                        <InputText
                            id="equipamento"
                            value={osSelecionada?.equipamento || ''}
                            onChange={(e) => setOsSelecionada({...osSelecionada, equipamento: e.target.value})}
                            placeholder={t('legacyUi.os.equipmentPlaceholder')}
                        />
                    </div>
                    <div className="p-field">
                        <label htmlFor="observacao">{t('legacyUi.os.description')}</label>
                        <InputTextarea 
                            id="observacao"
                            value={osSelecionada?.descricao || ''}
                            onChange={(e) => setOsSelecionada({...osSelecionada, descricao: e.target.value})}
                            rows={3}
                        />
                    </div>
                    
                    <div className="itens-os-section">
                        <div className="section-header">
                            <h3>{t('legacyUi.os.itemsTitle')}</h3>
                            <Button 
                                label={t('legacyUi.os.addItem')} 
                                icon="pi pi-plus" 
                                className="p-button-sm p-button-outlined" 
                                onClick={adicionarItem}
                            />
                        </div>
                        
                        <DataTable 
                            value={osSelecionada?.itens || []}
                            editableRows
                            emptyMessage={t('legacyUi.os.emptyItems')}
                        >
                            <Column
                                header={t('legacyUi.os.product')}
                                body={(row, options) => (
                                    <AutoComplete
                                        value={row.produto || null}
                                        suggestions={produtosSugestoes}
                                        completeMethod={buscarProdutos}
                                        field="nome"
                                        dropdown
                                        onChange={(e) => {
                                            const p = e.value;
                                            const itens = [...osSelecionada.itens];
                                            itens[options.rowIndex] = {
                                                ...itens[options.rowIndex],
                                                produto: p,
                                                produtoId: p?.id ?? null,
                                                servico: null,
                                                servicoId: null,
                                                descricao: p?.nome || itens[options.rowIndex].descricao || '',
                                                valorUnitario: p?.precoVenda ?? itens[options.rowIndex].valorUnitario ?? 0
                                            };
                                            setOsSelecionada({...osSelecionada, itens});
                                        }}
                                        placeholder={t('legacyUi.os.product')}
                                        className="w-full"
                                    />
                                )}
                                style={{ width: '28%' }}
                            />
                            <Column
                                header={t('legacyUi.os.service')}
                                body={(row, options) => (
                                    <AutoComplete
                                        value={row.servico || null}
                                        suggestions={servicosSugestoes}
                                        completeMethod={buscarServicos}
                                        field="nome"
                                        dropdown
                                        onChange={(e) => {
                                            const servico = e.value;
                                            const itens = [...osSelecionada.itens];
                                            itens[options.rowIndex] = {
                                                ...itens[options.rowIndex],
                                                servico,
                                                servicoId: servico?.id ?? null,
                                                produto: null,
                                                produtoId: null,
                                                descricao: servico?.nome || itens[options.rowIndex].descricao || '',
                                                valorUnitario: servico?.preco ?? servico?.valor ?? itens[options.rowIndex].valorUnitario ?? 0
                                            };
                                            setOsSelecionada({...osSelecionada, itens});
                                        }}
                                        placeholder={t('legacyUi.os.service')}
                                        className="w-full"
                                    />
                                )}
                                style={{ width: '28%' }}
                            />
                            <Column
                                field="descricao"
                                header={t('legacyUi.os.description')}
                                body={(row) => row.descricao || '—'} 
                                editor={(options) => (
                                    <InputText 
                                        value={options.value} 
                                        onChange={(e) => options.editorCallback(e.target.value)}
                                        placeholder={t('legacyUi.os.itemDescriptionPlaceholder')}
                                    />
                                )}
                                style={{ width: '40%' }}
                            />
                            <Column
                                field="quantidade"
                                header={t('legacyUi.os.quantity')}
                                body={(row, options) => (
                                    <InputNumber
                                        value={row.quantidade ?? 1}
                                        onValueChange={(e) => atualizarItem(options.rowIndex, 'quantidade', e.value ?? 0)}
                                        min={0}
                                        minFractionDigits={0}
                                        maxFractionDigits={4}
                                    />
                                )}
                                style={{ width: '100px' }}
                            />
                            <Column
                                field="valorUnitario"
                                header={t('legacyUi.os.unitValue')}
                                body={(row, options) => (
                                    <InputNumber
                                        value={row.valorUnitario ?? 0}
                                        onValueChange={(e) => atualizarItem(options.rowIndex, 'valorUnitario', e.value ?? 0)}
                                        mode="currency"
                                        currency="BRL"
                                        locale={i18n.language}
                                        min={0}
                                    />
                                )}
                                style={{ width: '140px' }}
                            />
                            <Column 
                                field="totalItem" 
                                header={t('legacyUi.os.total')} 
                                body={(row) => formatarMoeda(row.totalItem)}
                                style={{ width: '120px' }}
                            />
                            <Column 
                                body={(rowData, options) => (
                                    <Button 
                                        icon="pi pi-trash" 
                                        className="p-button-danger p-button-sm" 
                                        onClick={() => removerItem(options.rowIndex)}
                                    />
                                )}
                                style={{ width: '60px' }}
                            />
                        </DataTable>
                        
                        <div className="total-os">
                            <strong>{t('legacyUi.os.orderTotal')}: {formatarMoeda(calcularTotalOS())}</strong>
                        </div>
                    </div>
                </div>
            </Dialog>

            <Dialog
                visible={apontando}
                onHide={() => setApontando(false)}
                header={t('legacyUi.os.pointHoursHeader', { id: osSelecionada?.numero || '' })}
                modal
                style={{ width: 'min(96vw, 440px)' }}
                footer={
                    <div>
                        <Button label={t('legacyUi.os.cancel')} icon="pi pi-times" className="p-button-text" onClick={() => setApontando(false)} />
                        <Button label={t('legacyUi.os.register')} icon="pi pi-check" className="p-button-success" onClick={() => apontarOS(osSelecionada)} />
                    </div>
                }
            >
                <div className="p-fluid grid">
                    <div className="field col-12">
                        <label htmlFor="apHoras">{t('legacyUi.os.hoursWorked')}</label>
                        <InputNumber
                            inputId="apHoras"
                            value={apontamento.horas}
                            onValueChange={(e) => setApontamento({ ...apontamento, horas: e.value ?? 0 })}
                            minFractionDigits={2}
                            maxFractionDigits={2}
                            suffix=" h"
                        />
                    </div>
                    <div className="field col-12">
                        <label htmlFor="apDesc">{t('legacyUi.os.workDone')}</label>
                        <InputTextarea
                            id="apDesc"
                            value={apontamento.descricao}
                            onChange={(e) => setApontamento({ ...apontamento, descricao: e.target.value })}
                            rows={4}
                            autoResize
                            placeholder={t('legacyUi.os.workPlaceholder')}
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
}
