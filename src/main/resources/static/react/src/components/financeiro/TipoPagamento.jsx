import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';

export const TipoPagamento = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [tipos, setTipos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 10,
        sortField: 'descricao',
        sortOrder: 1
    });

    const [novoTipo, setNovoTipo] = useState({
        id: null,
        descricao: '',
        codigo: '',
        ativo: true
    });

    const statusOptions = [
        { label: 'Ativo', value: true },
        { label: 'Inativo', value: false }
    ];

    useEffect(() => {
        fetchTipos();
    }, [lazyParams]);

    const fetchTipos = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/tipos-pagamento?${params}`);
            if (response.ok) {
                const data = await response.json();
                setTipos(data.content || data);
                setTotalRecords(data.totalElements || data.length || 0);
            } else {
                console.error('Erro ao carregar tipos de pagamento');
            }
        } catch (err) {
            console.error('Erro ao carregar tipos de pagamento', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar os tipos de pagamento',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const onLazyLoad = (event) => {
        setLazyParams({
            page: event.page,
            rows: event.rows,
            sortField: event.sortField || 'descricao',
            sortOrder: event.sortOrder || 1
        });
    };

    const statusTemplate = (rowData) => {
        return (
            <Tag 
                value={rowData.ativo ? 'Ativo' : 'Inativo'} 
                severity={rowData.ativo ? 'success' : 'danger'} 
            />
        );
    };

    const salvarTipo = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const tipoParaSalvar = {
                ...novoTipo,
                empresaId: user?.empresaId,
                usuarioId: user?.id
            };

            const method = tipoParaSalvar.id ? 'PUT' : 'POST';
            const url = tipoParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/financeiro/tipos-pagamento/${tipoParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/financeiro/tipos-pagamento`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(tipoParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Tipo de pagamento ${tipoParaSalvar.id ? 'atualizado' : 'criado'} com sucesso`,
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchTipos();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel salvar o tipo de pagamento',
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar tipo de pagamento');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao salvar tipo de pagamento',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirTipo = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/tipos-pagamento/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Tipo de pagamento excluido com sucesso',
                    life: 3000
                });
                fetchTipos();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel excluir o tipo de pagamento',
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao excluir tipo de pagamento',
                life: 3000
            });
        }
    };

    const abrirDialog = (tipo = null) => {
        if (tipo) {
            setNovoTipo({
                id: tipo.id,
                descricao: tipo.descricao || '',
                codigo: tipo.codigo || '',
                ativo: tipo.ativo !== false
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoTipo({
            id: null,
            descricao: '',
            codigo: '',
            ativo: true
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="tipo-pagamento-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirTipo(rowData.id)}
                    tooltip="Excluir"
                    disabled={!rowData.ativo}
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label="Cancelar"
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
            />
            <Button
                label="Salvar Tipo"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarTipo}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="tipo-pagamento-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Tipos de Pagamento" className="tipo-pagamento-main-card">
                <div className="tipo-pagamento-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="tipo-pagamento-info">
                        <p className="text-muted m-0">Meios de pagamento utilizados nas transacoes financeiras.</p>
                    </div>
                    <Button
                        label="Novo Tipo"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={tipos}
                    loading={loading}
                    paginator
                    lazy
                    first={lazyParams.page * lazyParams.rows}
                    rows={lazyParams.rows}
                    totalRecords={totalRecords}
                    onPage={onLazyLoad}
                    onSort={onLazyLoad}
                    sortField={lazyParams.sortField}
                    sortOrder={lazyParams.sortOrder}
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} tipos"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum tipo de pagamento encontrado"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '120px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '300px' }} />
                    <Column field="ativo" header="Status" body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoTipo.id ? `Editar Tipo: ${novoTipo.descricao}` : 'Novo Tipo de Pagamento'}
                visible={dialogVisible}
                style={{ width: '500px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Tipo de pagamento salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Codigo</label>
                            <InputText
                                value={novoTipo.codigo}
                                onChange={(e) => setNovoTipo({...novoTipo, codigo: e.target.value})}
                                placeholder="Codigo do tipo (ex: DIN, CRED, DEB)"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputText
                                value={novoTipo.descricao}
                                onChange={(e) => setNovoTipo({...novoTipo, descricao: e.target.value})}
                                placeholder="Descricao do tipo de pagamento"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <Dropdown
                                value={novoTipo.ativo}
                                options={statusOptions}
                                onChange={(e) => setNovoTipo({...novoTipo, ativo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="tipo-pagamento-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Codigo: </span>
                            <span>{novoTipo.codigo}</span>
                        </div>
                        <div>
                            <span className="font-bold">Descricao: </span>
                            <span>{novoTipo.descricao}</span>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default TipoPagamento;
