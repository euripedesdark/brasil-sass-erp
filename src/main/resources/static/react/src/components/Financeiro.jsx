import { useTranslation } from 'react-i18next';
import React, { useState, useEffect } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Toast } from 'primereact/toast';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { LancamentoService } from '../services/LancamentoService';
import { formatoData, localeAtivo } from './shared/LocaleData.js';

export default function Financeiro() {
    const { t } = useTranslation();
    const [lancamentos, setLancamentos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 20,
        sortField: 'datavencimento',
        sortOrder: 1
    });
    
    const [filtroDocumento, setFiltroDocumento] = useState('');
    const [filtroTipo, setFiltroTipo] = useState(null);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [lancamentoSelecionado, setLancamentoSelecionado] = useState(null);
    const toast = React.useRef(null);

    const tiposLancamento = [
        { label: 'Contas a Pagar', value: 1 },
        { label: 'Contas a Receber', value: 2 }
    ];

    useEffect(() => {
        carregarLancamentos();
    }, [lazyParams]);

    const carregarLancamentos = async () => {
        setLoading(true);
        try {
            const filters = {};
            if (filtroDocumento) filters.documento = filtroDocumento;
            if (filtroTipo) filters.tipo = filtroTipo.value;
            
            const response = await LancamentoService.listarPaginado(
                lazyParams.page,
                lazyParams.rows,
                {
                    ...filters,
                    sort: lazyParams.sortField || 'datavencimento',
                    direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
                }
            );
            
            setLancamentos(response.content);
            setTotalRecords(response.totalElements);
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.financeiroLegacy.loadError')
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

    const abrirNovo = () => {
        setLancamentoSelecionado({
            dataLan: new Date(),
            dataVencimento: new Date(),
            baixado: 'N'
        });
        setDialogVisible(true);
    };

    const abrirEdicao = (lancamento) => {
        setLancamentoSelecionado({
            ...lancamento,
            dataLan: lancamento.dataLan ? new Date(lancamento.dataLan) : new Date(),
            dataVencimento: lancamento.dataVencimento ? new Date(lancamento.dataVencimento) : new Date()
        });
        setDialogVisible(true);
    };

    const salvarLancamento = async () => {
        try {
            const dados = {
                ...lancamentoSelecionado,
                dataLan: lancamentoSelecionado.dataLan?.toISOString().split('T')[0],
                dataVencimento: lancamentoSelecionado.dataVencimento?.toISOString().split('T')[0]
            };
            
            await LancamentoService.salvar(dados);
            
            toast.current.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('legacyUi.financeiroLegacy.saved')
            });
            
            setDialogVisible(false);
            carregarLancamentos();
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.financeiroLegacy.saveError')
            });
        }
    };

    const baixarLancamento = async (lancamento) => {
        try {
            await LancamentoService.baixar(lancamento.idLan);
            
            toast.current.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('legacyUi.financeiroLegacy.settled')
            });
            
            carregarLancamentos();
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.financeiroLegacy.settleError')
            });
        }
    };

    const excluirLancamento = async (lancamento) => {
        try {
            await LancamentoService.excluir(lancamento.idLan);
            
            toast.current.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('legacyUi.financeiroLegacy.deleted')
            });
            
            carregarLancamentos();
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.financeiroLegacy.deleteError')
            });
        }
    };

    const formatarMoeda = (valor) => {
        return valor.toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });
    };

    const formatarData = (data) => {
        if (!data) return '';
        return new Date(data).toLocaleDateString(localeAtivo());
    };

    const statusTemplate = (rowData) => {
        const statusClass = rowData.baixado === 'S' ? 'status-baixado' : 'status-pendente';
        const statusLabel = rowData.baixado === 'S' ? 'Baixado' : 'Pendente';
        return <span className={`status-badge ${statusClass}`}>{statusLabel}</span>;
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="acoes-container">
                {rowData.baixado === 'N' && (
                    <Button 
                        icon="pi pi-check" 
                        className="p-button-success p-button-sm mr-2" 
                        onClick={() => baixarLancamento(rowData)}
                        tooltip="Baixar"
                    />
                )}
                <Button 
                    icon="pi pi-pencil" 
                    className="p-button-warning p-button-sm mr-2" 
                    onClick={() => abrirEdicao(rowData)}
                    tooltip="Editar"
                />
                {rowData.baixado === 'N' && (
                    <Button 
                        icon="pi pi-trash" 
                        className="p-button-danger p-button-sm" 
                        onClick={() => excluirLancamento(rowData)}
                        tooltip="Excluir"
                    />
                )}
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button label={t('common.cancel')} icon="pi pi-times" className="p-button-text" onClick={() => setDialogVisible(false)} />
            <Button label={t('common.save')} icon="pi pi-check" className="p-button-text" onClick={salvarLancamento} />
        </div>
    );

    return (
        <div className="financeiro-container">
            <Toast ref={toast} />
            
            <div className="card">
                <h2>Gestão Financeira</h2>
                
                <div className="filtros-container">
                    <div className="p-fluid p-formgrid p-grid">
                        <div className="p-field p-col-12 p-md-4">
                            <label htmlFor="documento">Documento</label>
                            <InputText 
                                id="documento" 
                                value={filtroDocumento}
                                onChange={(e) => setFiltroDocumento(e.target.value)}
                                placeholder={t('legacyUi.financeiroLegacy.searchDoc')}
                            />
                        </div>
                        <div className="p-field p-col-12 p-md-4">
                            <label htmlFor="tipo">Tipo</label>
                            <Dropdown 
                                id="tipo"
                                value={filtroTipo}
                                options={tiposLancamento}
                                onChange={(e) => setFiltroTipo(e.value)}
                                placeholder={t('legacyUi.financeiroLegacy.typePlaceholder')}
                                showClear
                            />
                        </div>
                        <div className="p-field p-col-12 p-md-4">
                            <label>&nbsp;</label>
                            <Button 
                                label={t('legacyUi.financeiroLegacy.apply')} 
                                icon="pi pi-filter" 
                                onClick={carregarLancamentos}
                                className="p-button-outlined"
                            />
                        </div>
                    </div>
                </div>
                
                <div className="toolbar-container">
                    <Button 
                        label={t('legacyUi.financeiroLegacy.new')} 
                        icon="pi pi-plus" 
                        className="p-button-success" 
                        onClick={abrirNovo}
                    />
                </div>
                
                <DataTable 
                    value={lancamentos}
                    paginator
                    first={lazyParams.page * lazyParams.rows}
                    rows={lazyParams.rows}
                    totalRecords={totalRecords}
                    lazy
                    onPage={onLazyLoad}
                    onSort={onLazyLoad}
                    loading={loading}
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} lançamentos"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    sortField={lazyParams.sortField}
                    sortOrder={lazyParams.sortOrder}
                    emptyMessage={t('legacyUi.financeiroLegacy.empty')}
                >
                    <Column field="idLan" header={t('legacyUi.financeiroLegacy.code')} sortable style={{ width: '80px' }} />
                    <Column field="documento" header={t('legacyUi.financeiroLegacy.document')} sortable filter filterPlaceholder="Buscar documento" style={{ width: '120px' }} />
                    <Column field="dataLan" header={t('legacyUi.financeiroLegacy.launchDate')} body={(row) => formatarData(row.dataLan)} sortable style={{ width: '120px' }} />
                    <Column field="dataVencimento" header={t('legacyUi.financeiroLegacy.dueDate')} body={(row) => formatarData(row.dataVencimento)} sortable style={{ width: '120px' }} />
                    <Column field="valorLancamento" header={t('legacyUi.financeiroLegacy.value')} body={(row) => formatarMoeda(row.valorLancamento)} sortable style={{ width: '120px' }} />
                    <Column field="obsLan" header={t('legacyUi.financeiroLegacy.observations')} style={{ width: '200px' }} />
                    <Column field="baixado" header={t('legacyUi.financeiroLegacy.status')} body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '150px' }} />
                </DataTable>
            </div>
            
            <Dialog 
                visible={dialogVisible} 
                style={{ width: '600px' }} 
                header={lancamentoSelecionado?.idLan ? t('legacyUi.financeiroLegacy.editTitle') : t('legacyUi.financeiroLegacy.new')}
                footer={dialogFooter}
                onHide={() => setDialogVisible(false)}
            >
                <div className="p-fluid">
                    <div className="p-field">
                        <label htmlFor="documento">Documento</label>
                        <InputText 
                            id="documento" 
                            value={lancamentoSelecionado?.documento || ''}
                            onChange={(e) => setLancamentoSelecionado({...lancamentoSelecionado, documento: e.target.value})}
                        />
                    </div>
                    <div className="p-grid">
                        <div className="p-col-6">
                            <div className="p-field">
                                <label htmlFor="dataLan">Data Lançamento</label>
                                <Calendar 
                                    id="dataLan"
                                    value={lancamentoSelecionado?.dataLan}
                                    onChange={(e) => setLancamentoSelecionado({...lancamentoSelecionado, dataLan: e.value})}
                                    dateFormat={formatoData()}
                                    showIcon
                                />
                            </div>
                        </div>
                        <div className="p-col-6">
                            <div className="p-field">
                                <label htmlFor="dataVencimento">Data Vencimento</label>
                                <Calendar 
                                    id="dataVencimento"
                                    value={lancamentoSelecionado?.dataVencimento}
                                    onChange={(e) => setLancamentoSelecionado({...lancamentoSelecionado, dataVencimento: e.value})}
                                    dateFormat={formatoData()}
                                    showIcon
                                />
                            </div>
                        </div>
                    </div>
                    <div className="p-field">
                        <label htmlFor="valor">Valor</label>
                        <InputNumber 
                            id="valor"
                            value={lancamentoSelecionado?.valorLancamento}
                            onChange={(e) => setLancamentoSelecionado({...lancamentoSelecionado, valorLancamento: e.value})}
                            mode="currency"
                            currency="BRL"
                            locale={localeAtivo()}
                        />
                    </div>
                    <div className="p-field">
                        <label htmlFor="obs">Observações</label>
                        <InputText 
                            id="obs" 
                            value={lancamentoSelecionado?.obsLan || ''}
                            onChange={(e) => setLancamentoSelecionado({...lancamentoSelecionado, obsLan: e.target.value})}
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
}
