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
    const { t } = useTranslation();
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
                summary: 'Erro',
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
                const lista = Array.isArray(data) ? data : (data.content || []);
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

    const abrirNovaOS = () => {
        setOsSelecionada({
            dataMov: new Date(),
            baixaMov: 'N',
            itens: []
        });
        setEmitirNota(false);
        setDialogVisible(true);
    };

    const abrirEdicao = (os) => {
        setOsSelecionada({
            ...os,
            dataMov: os.dataMov ? new Date(os.dataMov) : new Date()
        });
        setDialogVisible(true);
    };

    const salvarOS = async () => {
        try {
            const isEdicao = !!(osSelecionada && osSelecionada.id);
            let payload;
            let url;
            let method;

            if (isEdicao) {
                // Adicionar itens via POST /{id}/itens
                url = `${ApiConfig.BASE_URL}/api/servicos/os/${osSelecionada.id}/itens`;
                method = 'POST';
                payload = null;
            } else {
                url = `${ApiConfig.BASE_URL}/api/servicos/os`;
                method = 'POST';
                payload = {
                    clienteId: osSelecionada.clienteId || null,
                    equipamento: osSelecionada.equipamento || null,
                    descricao: osSelecionada.descricao || null,
                    previsaoAt: osSelecionada.dataMov ? new Date(osSelecionada.dataMov).toISOString() : new Date().toISOString()
                };
            }

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: payload ? JSON.stringify(payload) : undefined
            });

            if (response.ok) {
                toast.current.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('legacyUi.os.saveSuccess')
                });

                if (emitirNota) {
                    // Redirecionar para tela de emissão de nota em nova aba
                    window.open(`/notas-fiscais?os=${osSelecionada.idMov || osSelecionada.id}`, '_blank');
                }

                setDialogVisible(false);
                carregarOS();
            } else {
                const err = await response.text();
                toast.current.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || t('legacyUi.os.saveError')
                });
            }
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: 'Erro',
                detail: t('legacyUi.os.saveError')
            });
        }
    };

    const adicionarItem = () => {
        const novoItem = {
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
        return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    const formatarData = (data) => {
        if (!data) return '';
        return new Date(data).toLocaleDateString('pt-BR');
    };

    const statusTemplate = (rowData) => {
        const statusClass = rowData.baixaMov === 'S' ? 'status-baixado' : 'status-aberto';
        const statusLabel = rowData.baixaMov === 'S' ? 'Baixado' : 'Aberto';
        return <span className={`status-badge ${statusClass}`}>{statusLabel}</span>;
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
                toast.current.show({ severity: 'error', summary: 'Erro', detail: err || t('legacyUi.os.closeError') });
            }
        } catch (error) {
            toast.current.show({ severity: 'error', summary: 'Erro', detail: t('legacyUi.os.closeError') });
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
            toast.current.show({ severity: 'error', summary: 'Impressao', detail: t('legacyUi.os.printError') });
        }
    };

    /** Registra horas trabalhadas na OS. */
    const apontarOS = async (os) => {
        const horas = Number(apontamento.horas);
        if (!(horas > 0)) {
            toast.current.show({ severity: 'warn', summary: 'Apontamento', detail: t('legacyUi.os.hoursRequired') });
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
            toast.current.show({ severity: 'success', summary: 'Apontamento', detail: `${horas}h registradas na OS` });
            setApontando(false);
            setApontamento({ horas: 1, descricao: '' });
            carregarOS();
        } catch (error) {
            toast.current.show({ severity: 'error', summary: 'Apontamento', detail: error.message || 'Falha ao apontar' });
        }
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="acoes-container">
                <Button 
                    icon="pi pi-eye" 
                    className="p-button-info p-button-sm mr-2" 
                    onClick={() => abrirEdicao(rowData)}
                    tooltip="Visualizar/Editar"
                />
                {rowData.baixaMov === 'N' && (
                    <>
                        <Button 
                            icon="pi pi-file-pdf" 
                            className="p-button-success p-button-sm mr-2" 
                            tooltip="Imprimir OS"
                            onClick={() => imprimirOS(rowData)}
                        />
                        <Button 
                            icon="pi pi-clock" 
                            className="p-button-info p-button-sm mr-2" 
                            tooltip="Apontar horas"
                            onClick={() => { setOsSelecionada(rowData); setApontando(true); }}
                        />
                        <Button 
                            icon="pi pi-receipt" 
                            className="p-button-warning p-button-sm" 
                            tooltip="Emitir Nota"
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
                        tooltip="Fechar OS"
                        onClick={() => fecharOS(rowData)}
                    />
                )}
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button label="Cancelar" icon="pi pi-times" className="p-button-text" onClick={() => setDialogVisible(false)} />
            <Button label="Salvar e Imprimir OS" icon="pi pi-check" className="p-button-success" onClick={salvarOS} />
            {osSelecionada?.baixaMov === 'N' && (
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
                            Emitir Nota Fiscal após salvar
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
                            <label htmlFor="cliente">Cliente</label>
                            <AutoComplete
                                id="cliente"
                                value={filtroCliente}
                                suggestions={clientesSugestoes}
                                completeMethod={buscarClientes}
                                field="nome"
                                onChange={(e) => setFiltroCliente(e.value)}
                                placeholder="Digite o nome do cliente"
                                dropdown
                                multiple
                            />
                        </div>
                        <div className="p-field p-col-12 p-md-6">
                            <label>&nbsp;</label>
                            <Button 
                                label="Pesquisar" 
                                icon="pi pi-search" 
                                onClick={carregarOS}
                                className="p-button-outlined"
                            />
                        </div>
                    </div>
                </div>
                
                <div className="toolbar-container">
                    <Button 
                        label="Nova Ordem de Serviço" 
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
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} OS"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    sortField={lazyParams.sortField}
                    sortOrder={lazyParams.sortOrder}
                    emptyMessage="Nenhuma ordem de serviço encontrada"
                >
                    <Column field="idMov" header="Número OS" sortable style={{ width: '100px' }} />
                    <Column field="cliente" header="Cliente" sortable filter filterPlaceholder="Buscar cliente" style={{ width: '250px' }} />
                    <Column field="dataMov" header="Data" body={(row) => formatarData(row.aberturaAt || row.dataMov)} sortable style={{ width: '140px' }} />
                    <Column field="valorTotal" header="Valor Total" body={(row) => formatarMoeda(row.valorTotal)} sortable style={{ width: '120px' }} />
                    <Column field="status" header="Status" body={(row) => statusTemplate({ ...row, status: row.status || (row.baixaMov === 'N' ? 'ABERTA' : 'FECHADA') })} sortable style={{ width: '120px' }} />
                    <Column body={acoesTemplate} style={{ width: '200px' }} />
                </DataTable>
            </div>
            
            <Dialog 
                visible={dialogVisible} 
                style={{ width: '900px' }} 
                header={osSelecionada?.idMov ? `Editar OS ${osSelecionada.idMov}` : 'Nova Ordem de Serviço'}
                footer={dialogFooter}
                onHide={() => setDialogVisible(false)}
                maximizable
            >
                <div className="p-fluid">
                    <div className="p-grid">
                        <div className="p-col-8">
                            <div className="p-field">
                                <label htmlFor="cliente">Cliente *</label>
                                <AutoComplete
                                    id="cliente"
                                    value={osSelecionada?.cliente}
                                    suggestions={clientesSugestoes}
                                    completeMethod={buscarClientes}
                                    field="nome"
                                    onChange={(e) => setOsSelecionada({...osSelecionada, cliente: e.value})}
                                    placeholder="Busque pelo cliente"
                                    dropdown
                                    required
                                />
                            </div>
                        </div>
                        <div className="p-col-4">
                            <div className="p-field">
                                <label htmlFor="dataMov">Data OS *</label>
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
                        <label htmlFor="observacao">Observações</label>
                        <InputTextarea 
                            id="observacao"
                            value={osSelecionada?.observacao || ''}
                            onChange={(e) => setOsSelecionada({...osSelecionada, observacao: e.target.value})}
                            rows={3}
                        />
                    </div>
                    
                    <div className="itens-os-section">
                        <div className="section-header">
                            <h3>Itens da Ordem de Serviço</h3>
                            <Button 
                                label="Adicionar Item" 
                                icon="pi pi-plus" 
                                className="p-button-sm p-button-outlined" 
                                onClick={adicionarItem}
                            />
                        </div>
                        
                        <DataTable 
                            value={osSelecionada?.itens || []}
                            editableRows
                            emptyMessage="Nenhum item adicionado"
                        >
                            <Column 
                                field="descricao" 
                                header="Descrição" 
                                editor={(options) => (
                                    <InputText 
                                        value={options.value} 
                                        onChange={(e) => options.editorCallback(e.target.value)}
                                        placeholder="Descrição do serviço/produto"
                                    />
                                )}
                                style={{ width: '40%' }}
                            />
                            <Column 
                                field="quantidade" 
                                header="Qtd" 
                                style={{ width: '80px' }}
                                editor={(options) => (
                                    <InputNumber
                                        value={options.value}
                                        onValueChange={(e) => options.editorCallback(e.value)}
                                        min={0}
                                    />
                                )}
                            />
                            <Column 
                                field="valorUnitario" 
                                header="Vl. Unitário" 
                                body={(row) => formatarMoeda(row.valorUnitario)}
                                style={{ width: '120px' }}
                                editor={(options) => (
                                    <InputNumber
                                        value={options.value}
                                        onValueChange={(e) => options.editorCallback(e.value)}
                                        mode="currency"
                                        currency="BRL"
                                        locale="pt-BR"
                                        min={0}
                                    />
                                )}
                            />
                            <Column 
                                field="totalItem" 
                                header="Total" 
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
                            <strong>Total da OS: {formatarMoeda(calcularTotalOS())}</strong>
                        </div>
                    </div>
                </div>
            </Dialog>

            <Dialog
                visible={apontando}
                onHide={() => setApontando(false)}
                header={`Apontar horas — OS ${osSelecionada?.numero || ''}`}
                modal
                style={{ width: 'min(96vw, 440px)' }}
                footer={
                    <div>
                        <Button label="Cancelar" icon="pi pi-times" className="p-button-text" onClick={() => setApontando(false)} />
                        <Button label="Registrar" icon="pi pi-check" className="p-button-success" onClick={() => apontarOS(osSelecionada)} />
                    </div>
                }
            >
                <div className="p-fluid grid">
                    <div className="field col-12">
                        <label htmlFor="apHoras">Horas trabalhadas</label>
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
                        <label htmlFor="apDesc">O que foi feito</label>
                        <InputTextarea
                            id="apDesc"
                            value={apontamento.descricao}
                            onChange={(e) => setApontamento({ ...apontamento, descricao: e.target.value })}
                            rows={4}
                            autoResize
                            placeholder="Troca do compressor, teste de pressão..."
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
}
