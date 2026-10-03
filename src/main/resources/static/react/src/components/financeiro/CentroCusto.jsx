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

export const CentroCusto = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [centrosCusto, setCentrosCusto] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 10,
        sortField: 'codigo',
        sortOrder: 1
    });

    const [novoCentroCusto, setNovoCentroCusto] = useState({
        id: null,
        codigo: '',
        descricao: '',
        centroCustoPaiId: null,
        centroCustoPaiDescricao: '',
        ativo: true,
        nivel: 1
    });

    const statusOptions = [
        { label: 'Ativo', value: true },
        { label: 'Inativo', value: false }
    ];

    useEffect(() => {
        fetchCentrosCusto();
    }, [lazyParams]);

    const fetchCentrosCusto = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/centros-custo?${params}`);
            if (response.ok) {
                const data = await response.json();
                setCentrosCusto(data.content || data);
                setTotalRecords(data.totalElements || data.length || 0);
            } else {
                console.error('Erro ao carregar centros de custo');
            }
        } catch (err) {
            console.error('Erro ao carregar centros de custo', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar os centros de custo',
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
            sortField: event.sortField || 'codigo',
            sortOrder: event.sortOrder || 1
        });
    };

    const formatarMoeda = (valor) => {
        if (valor === null || valor === undefined) return 'R$ 0,00';
        return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    const statusTemplate = (rowData) => {
        return (
            <Tag 
                value={rowData.ativo ? 'Ativo' : 'Inativo'} 
                severity={rowData.ativo ? 'success' : 'danger'} 
            />
        );
    };

    const nivelTemplate = (rowData) => {
        const nivelClass = rowData.nivel === 1 ? 'primary' : 
                          rowData.nivel === 2 ? 'info' : 
                          rowData.nivel === 3 ? 'warning' : 'danger';
        return (
            <Tag 
                value={`Nivel ${rowData.nivel}`} 
                severity={nivelClass}
            />
        );
    };

    const salvarCentroCusto = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const centroParaSalvar = {
                ...novoCentroCusto,
                empresaId: user?.empresaId,
                usuarioId: user?.id
            };

            const method = centroParaSalvar.id ? 'PUT' : 'POST';
            const url = centroParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/financeiro/centros-custo/${centroParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/financeiro/centros-custo`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(centroParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Centro de custo ${centroParaSalvar.id ? 'atualizado' : 'criado'} com sucesso`,
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchCentrosCusto();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel salvar o centro de custo',
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar centro de custo');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao salvar centro de custo',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirCentroCusto = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/centros-custo/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Centro de custo excluido com sucesso',
                    life: 3000
                });
                fetchCentrosCusto();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel excluir o centro de custo',
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao excluir centro de custo',
                life: 3000
            });
        }
    };

    const abrirDialog = (centro = null) => {
        if (centro) {
            setNovoCentroCusto({
                id: centro.id,
                codigo: centro.codigo || '',
                descricao: centro.descricao || '',
                centroCustoPaiId: centro.centroCustoPai?.id,
                centroCustoPaiDescricao: centro.centroCustoPai?.descricao || '',
                ativo: centro.ativo !== false,
                nivel: centro.nivel || 1
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoCentroCusto({
            id: null,
            codigo: '',
            descricao: '',
            centroCustoPaiId: null,
            centroCustoPaiDescricao: '',
            ativo: true,
            nivel: 1
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="centro-custo-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirCentroCusto(rowData.id)}
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
                label="Salvar Centro de Custo"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarCentroCusto}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="centro-custo-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Centros de Custo" className="centro-custo-main-card">
                <div className="centro-custo-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="centro-custo-info">
                        <p className="text-muted m-0">Gestao hierarquica de centros de custo para rateio e analise financeira.</p>
                    </div>
                    <Button
                        label="Novo Centro de Custo"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={centrosCusto}
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
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} centros de custo"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum centro de custo encontrado"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '120px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '250px' }} />
                    <Column field="nivel" header="Nivel" body={nivelTemplate} sortable style={{ width: '100px' }} />
                    <Column field="centroCustoPai.descricao" header="Centro Pai" sortable style={{ width: '200px' }} />
                    <Column field="ativo" header="Status" body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoCentroCusto.id ? `Editar Centro: ${novoCentroCusto.descricao}` : 'Novo Centro de Custo'}
                visible={dialogVisible}
                style={{ width: '600px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Centro de custo salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Codigo</label>
                            <InputText
                                value={novoCentroCusto.codigo}
                                onChange={(e) => setNovoCentroCusto({...novoCentroCusto, codigo: e.target.value})}
                                placeholder="Codigo do centro de custo"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputText
                                value={novoCentroCusto.descricao}
                                onChange={(e) => setNovoCentroCusto({...novoCentroCusto, descricao: e.target.value})}
                                placeholder="Descricao do centro de custo"
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Centro Pai ID</label>
                            <InputText
                                value={novoCentroCusto.centroCustoPaiId}
                                onChange={(e) => setNovoCentroCusto({...novoCentroCusto, centroCustoPaiId: e.target.value})}
                                placeholder="ID do centro pai (opcional)"
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Nivel</label>
                            <InputText
                                value={novoCentroCusto.nivel}
                                onChange={(e) => setNovoCentroCusto({...novoCentroCusto, nivel: parseInt(e.target.value) || 1})}
                                placeholder="Nivel hierarquico"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <Dropdown
                                value={novoCentroCusto.ativo}
                                options={statusOptions}
                                onChange={(e) => setNovoCentroCusto({...novoCentroCusto, ativo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="centro-custo-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Codigo: </span>
                            <span>{novoCentroCusto.codigo}</span>
                        </div>
                        <div>
                            <span className="font-bold">Nivel: </span>
                            <span>{novoCentroCusto.nivel}</span>
                        </div>
                        <div>
                            <span className="font-bold">Status: </span>
                            <Tag value={novoCentroCusto.ativo ? 'Ativo' : 'Inativo'} severity={novoCentroCusto.ativo ? 'success' : 'danger'} />
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default CentroCusto;
