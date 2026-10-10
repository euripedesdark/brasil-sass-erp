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
import './PlanoContas.css';

export const PlanoContas = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [contas, setContas] = useState([]);
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

    const [novaConta, setNovaConta] = useState({
        id: null,
        codigo: '',
        descricao: '',
        tipo: 'ANALITICA',
        natureza: 'DEVEDORA',
        contaPaiId: null,
        contaPaiDescricao: '',
        nivel: 1,
        ativa: true
    });

    const tipoContaOptions = [
        { label: 'Analítica', value: 'ANALITICA' },
        { label: 'Sintética', value: 'SINTETICA' }
    ];

    const naturezaOptions = [
        { label: 'Devedora', value: 'DEVEDORA' },
        { label: 'Credora', value: 'CREDORA' },
        { label: 'Mista', value: 'MISTA' }
    ];

    const statusOptions = [
        { label: 'Ativa', value: true },
        { label: 'Inativa', value: false }
    ];

    useEffect(() => {
        fetchContas();
    }, [lazyParams]);

    const fetchContas = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/plano-contas?${params}`);
            if (response.ok) {
                const data = await response.json();
                setContas(data.content || data);
                setTotalRecords(data.totalElements || data.length || 0);
            } else {
                console.error('Erro ao carregar plano de contas');
            }
        } catch (err) {
            console.error('Erro ao carregar plano de contas', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar o plano de contas',
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
                value={rowData.ativa ? 'Ativa' : 'Inativa'} 
                severity={rowData.ativa ? 'success' : 'danger'} 
            />
        );
    };

    const tipoTemplate = (rowData) => {
        const tipoMap = {
            'ANALITICA': 'Analítica',
            'SINTETICA': 'Sintética'
        };
        return (
            <Tag 
                value={tipoMap[rowData.tipo] || rowData.tipo} 
                severity="info"
            />
        );
    };

    const naturezaTemplate = (rowData) => {
        const naturezaMap = {
            'DEVEDORA': 'Devedora',
            'CREDORA': 'Credora',
            'MISTA': 'Mista'
        };
        const severityMap = {
            'DEVEDORA': 'danger',
            'CREDORA': 'success',
            'MISTA': 'warning'
        };
        return (
            <Tag 
                value={naturezaMap[rowData.natureza] || rowData.natureza} 
                severity={severityMap[rowData.natureza] || 'info'}
            />
        );
    };

    const nivelTemplate = (rowData) => {
        const nivelClass = rowData.nivel === 1 ? 'primary' : 
                          rowData.nivel === 2 ? 'info' : 
                          rowData.nivel === 3 ? 'warning' : 'danger';
        return (
            <Tag 
                value={`N${rowData.nivel}`} 
                severity={nivelClass}
            />
        );
    };

    const salvarConta = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const contaParaSalvar = {
                ...novaConta,
                empresaId: user?.empresaId,
                usuarioId: user?.id,
                nivel: parseInt(novaConta.nivel) || 1
            };

            const method = contaParaSalvar.id ? 'PUT' : 'POST';
            const url = contaParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/financeiro/plano-contas/${contaParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/financeiro/plano-contas`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(contaParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Conta ${contaParaSalvar.id ? 'atualizada' : 'criada'} com sucesso`,
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchContas();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel salvar a conta do plano de contas',
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar conta do plano de contas');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao salvar conta do plano de contas',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirConta = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/plano-contas/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Conta do plano de contas excluida com sucesso',
                    life: 3000
                });
                fetchContas();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel excluir a conta do plano de contas',
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao excluir conta do plano de contas',
                life: 3000
            });
        }
    };

    const abrirDialog = (conta = null) => {
        if (conta) {
            setNovaConta({
                id: conta.id,
                codigo: conta.codigo || '',
                descricao: conta.descricao || '',
                tipo: conta.tipo || 'ANALITICA',
                natureza: conta.natureza || 'DEVEDORA',
                contaPaiId: conta.contaPaiId,
                contaPaiDescricao: conta.contaPai?.descricao || '',
                nivel: conta.nivel || 1,
                ativa: conta.ativa !== false
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovaConta({
            id: null,
            codigo: '',
            descricao: '',
            tipo: 'ANALITICA',
            natureza: 'DEVEDORA',
            contaPaiId: null,
            contaPaiDescricao: '',
            nivel: 1,
            ativa: true
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="plano-contas-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirConta(rowData.id)}
                    tooltip="Excluir"
                    disabled={!rowData.ativa}
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
                label="Salvar Conta"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarConta}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="plano-contas-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Plano de Contas" className="plano-contas-main-card">
                <div className="plano-contas-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="plano-contas-info">
                        <p className="text-muted m-0">Estrutura hierarquica do plano de contas contabil para do sistema.</p>
                    </div>
                    <Button
                        label="Nova Conta"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={contas}
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
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} contas"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhuma conta encontrada"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '120px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '300px' }} />
                    <Column field="nivel" header="Nivel" body={nivelTemplate} sortable style={{ width: '80px' }} />
                    <Column field="tipo" header="Tipo" body={tipoTemplate} sortable style={{ width: '120px' }} />
                    <Column field="natureza" header="Natureza" body={naturezaTemplate} sortable style={{ width: '120px' }} />
                    <Column field="contaPai.codigo" header="Conta Pai" sortable style={{ width: '120px' }} />
                    <Column field="ativa" header="Status" body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novaConta.id ? `Editar Conta: ${novaConta.codigo} - ${novaConta.descricao}` : 'Nova Conta do Plano de Contas'}
                visible={dialogVisible}
                style={{ width: '700px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Conta do plano de contas salva com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Codigo</label>
                            <InputText
                                value={novaConta.codigo}
                                onChange={(e) => setNovaConta({...novaConta, codigo: e.target.value})}
                                placeholder="Codigo da conta (ex: 1.1.1)"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputText
                                value={novaConta.descricao}
                                onChange={(e) => setNovaConta({...novaConta, descricao: e.target.value})}
                                placeholder="Descricao da conta"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Nivel</label>
                            <InputNumber
                                value={novaConta.nivel}
                                onValueChange={(e) => setNovaConta({...novaConta, nivel: e.value})}
                                placeholder="Nivel"
                                min={1}
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Tipo</label>
                            <Dropdown
                                value={novaConta.tipo}
                                options={tipoContaOptions}
                                onChange={(e) => setNovaConta({...novaConta, tipo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Natureza</label>
                            <Dropdown
                                value={novaConta.natureza}
                                options={naturezaOptions}
                                onChange={(e) => setNovaConta({...novaConta, natureza: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Conta Pai ID</label>
                            <InputNumber
                                value={novaConta.contaPaiId}
                                onValueChange={(e) => setNovaConta({...novaConta, contaPaiId: e.value})}
                                placeholder="ID da conta pai (opcional para contas filhas)"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <Dropdown
                                value={novaConta.ativa}
                                options={statusOptions}
                                onChange={(e) => setNovaConta({...novaConta, ativa: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="plano-contas-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Codigo: </span>
                            <span>{novaConta.codigo}</span>
                        </div>
                        <div>
                            <span className="font-bold">Tipo: </span>
                            <Tag value={novaConta.tipo === 'ANALITICA' ? 'Analítica' : 'Sintética'} severity="info" />
                        </div>
                        <div>
                            <span className="font-bold">Natureza: </span>
                            <Tag 
                                value={novaConta.natureza === 'DEVEDORA' ? 'Devedora' : novaConta.natureza === 'CREDORA' ? 'Credora' : 'Mista'}
                                severity={novaConta.natureza === 'DEVEDORA' ? 'danger' : novaConta.natureza === 'CREDORA' ? 'success' : 'warning'}
                            />
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default PlanoContas;
