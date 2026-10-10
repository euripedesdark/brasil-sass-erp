import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';
import './CondicaoPagamento.css';

export const CondicaoPagamento = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [condicoes, setCondicoes] = useState([]);
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

    const [novaCondicao, setNovaCondicao] = useState({
        id: null,
        descricao: '',
        dias: 30,
        ativo: true
    });

    const statusOptions = [
        { label: 'Ativa', value: true },
        { label: 'Inativa', value: false }
    ];

    useEffect(() => {
        fetchCondicoes();
    }, [lazyParams]);

    const fetchCondicoes = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/condicoes-pagamento?${params}`);
            if (response.ok) {
                const data = await response.json();
                setCondicoes(data.content || data);
                setTotalRecords(data.totalElements || data.length || 0);
            } else {
                console.error('Erro ao carregar condicoes de pagamento');
            }
        } catch (err) {
            console.error('Erro ao carregar condicoes de pagamento', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar as condicoes de pagamento',
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
                value={rowData.ativo ? 'Ativa' : 'Inativa'} 
                severity={rowData.ativo ? 'success' : 'danger'} 
            />
        );
    };

    const diasTemplate = (rowData) => {
        return <span>{rowData.dias} dias</span>;
    };

    const salvarCondicao = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const condicaoParaSalvar = {
                ...novaCondicao,
                empresaId: user?.empresaId,
                usuarioId: user?.id,
                dias: parseInt(novaCondicao.dias) || 0
            };

            const method = condicaoParaSalvar.id ? 'PUT' : 'POST';
            const url = condicaoParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/financeiro/condicoes-pagamento/${condicaoParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/financeiro/condicoes-pagamento`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(condicaoParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Condicao de pagamento ${condicaoParaSalvar.id ? 'atualizada' : 'criada'} com sucesso`,
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchCondicoes();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel salvar a condicao de pagamento',
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar condicao de pagamento');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao salvar condicao de pagamento',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirCondicao = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/condicoes-pagamento/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Condicao de pagamento excluida com sucesso',
                    life: 3000
                });
                fetchCondicoes();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel excluir a condicao de pagamento',
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao excluir condicao de pagamento',
                life: 3000
            });
        }
    };

    const abrirDialog = (condicao = null) => {
        if (condicao) {
            setNovaCondicao({
                id: condicao.id,
                descricao: condicao.descricao || '',
                dias: condicao.dias || 30,
                ativo: condicao.ativo !== false
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovaCondicao({
            id: null,
            descricao: '',
            dias: 30,
            ativo: true
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="condicao-pagamento-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirCondicao(rowData.id)}
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
                label="Salvar Condicao"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarCondicao}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="condicao-pagamento-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Condicoes de Pagamento" className="condicao-pagamento-main-card">
                <div className="condicao-pagamento-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="condicao-pagamento-info">
                        <p className="text-muted m-0">Configuracao de condicoes e prazos de pagamento para titulos.</p>
                    </div>
                    <Button
                        label="Nova Condicao"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={condicoes}
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
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} condicoes"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhuma condicao de pagamento encontrada"
                >
                    <Column field="descricao" header="Descricao" sortable style={{ width: '300px' }} />
                    <Column field="dias" header="Prazo" body={diasTemplate} sortable style={{ width: '120px' }} />
                    <Column field="ativo" header="Status" body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novaCondicao.id ? `Editar Condicao: ${novaCondicao.descricao}` : 'Nova Condicao de Pagamento'}
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
                    {success && <Message severity="success" text="Condicao de pagamento salva com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputText
                                value={novaCondicao.descricao}
                                onChange={(e) => setNovaCondicao({...novaCondicao, descricao: e.target.value})}
                                placeholder="Descricao da condicao (ex: A vista, 30 dias)"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Dias para Pagamento</label>
                            <InputNumber
                                value={novaCondicao.dias}
                                onValueChange={(e) => setNovaCondicao({...novaCondicao, dias: e.value})}
                                placeholder="Quantidade de dias"
                                min={0}
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <Dropdown
                                value={novaCondicao.ativo}
                                options={statusOptions}
                                onChange={(e) => setNovaCondicao({...novaCondicao, ativo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="condicao-pagamento-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Descricao: </span>
                            <span>{novaCondicao.descricao}</span>
                        </div>
                        <div>
                            <span className="font-bold">Prazo: </span>
                            <span>{novaCondicao.dias} dias</span>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default CondicaoPagamento;
