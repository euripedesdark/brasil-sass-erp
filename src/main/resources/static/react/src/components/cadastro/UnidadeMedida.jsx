import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
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
import { useAuth } from '../../contexts/AuthContext';
import { UnidadeMedidaService } from '../../services/UnidadeMedidaService';
import './UnidadeMedida.css';

export const UnidadeMedida = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [unidades, setUnidades] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const tipoOptions = [
        { label: 'Unidade', value: 'UNIDADE' },
        { label: 'Peso', value: 'PESO' },
        { label: 'Volume', value: 'VOLUME' },
        { label: 'Comprimento', value: 'COMPRIMENTO' }
    ];

    const [novaUnidade, setNovaUnidade] = useState({
        id: null,
        sigla: '',
        nome: '',
        tipo: 'UNIDADE'
    });

    useEffect(() => {
        fetchUnidades();
    }, []);

    const fetchUnidades = async () => {
        setLoading(true);
        try {
            const data = await UnidadeMedidaService.listar();
            setUnidades(data || []);
        } catch (err) {
            console.error('Erro ao carregar unidades de medida', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar as unidades de medida',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const salvarUnidade = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const unidadeParaSalvar = {
                ...novaUnidade
            };

            if (unidadeParaSalvar.id) {
                await UnidadeMedidaService.atualizar(unidadeParaSalvar.id, unidadeParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Unidade de medida atualizada com sucesso',
                    life: 3000
                });
            } else {
                await UnidadeMedidaService.criar(unidadeParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Unidade de medida criada com sucesso',
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchUnidades();
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || 'Erro ao salvar unidade de medida');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel salvar a unidade de medida',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirUnidade = async (id) => {
        try {
            await UnidadeMedidaService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: 'Sucesso',
                detail: 'Unidade de medida excluida com sucesso',
                life: 3000
            });
            fetchUnidades();
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel excluir a unidade de medida',
                life: 3000
            });
        }
    };

    const abrirDialog = (unidade = null) => {
        if (unidade) {
            setNovaUnidade({
                id: unidade.id,
                sigla: unidade.sigla || '',
                nome: unidade.nome || '',
                tipo: unidade.tipo || 'UNIDADE'
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovaUnidade({
            id: null,
            sigla: '',
            nome: '',
            tipo: 'UNIDADE'
        });
        setError('');
        setSuccess(false);
    };

    const tipoTemplate = (rowData) => {
        const tipo = tipoOptions.find(t => t.value === rowData.tipo);
        return tipo ? tipo.label : rowData.tipo;
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="unidade-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirUnidade(rowData.id)}
                    tooltip="Excluir"
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
                label="Salvar Unidade"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarUnidade}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="unidade-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Unidades de Medida" className="unidade-main-card">
                <div className="unidade-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="unidade-info">
                        <p className="text-muted m-0">Gestao de unidades de medida para produtos.</p>
                    </div>
                    <Button
                        label="Nova Unidade"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={unidades}
                    loading={loading}
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={unidades.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhuma unidade de medida encontrada"
                >
                    <Column field="sigla" header="Sigla" sortable style={{ width: '100px' }} />
                    <Column field="nome" header="Nome" sortable style={{ width: '250px' }} />
                    <Column field="tipo" header="Tipo" body={tipoTemplate} sortable style={{ width: '150px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novaUnidade.id ? `Editar Unidade: ${novaUnidade.sigla}` : 'Nova Unidade de Medida'}
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
                    {success && <Message severity="success" text="Unidade salva com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Sigla *</label>
                            <InputText
                                value={novaUnidade.sigla}
                                onChange={(e) => setNovaUnidade({...novaUnidade, sigla: e.target.value.toUpperCase()})}
                                placeholder="Sigla"
                                maxLength={10}
                                required
                            />
                        </div>
                        <div className="col-12 md:col-8 field">
                            <label className="font-bold mb-2 block">Nome *</label>
                            <InputText
                                value={novaUnidade.nome}
                                onChange={(e) => setNovaUnidade({...novaUnidade, nome: e.target.value})}
                                placeholder="Nome da unidade"
                                required
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Tipo</label>
                            <Dropdown
                                value={novaUnidade.tipo}
                                options={tipoOptions}
                                onChange={(e) => setNovaUnidade({...novaUnidade, tipo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="unidade-resumo flex flex-wrap justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Sigla: </span>
                            <span>{novaUnidade.sigla}</span>
                        </div>
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Nome: </span>
                            <span>{novaUnidade.nome}</span>
                        </div>
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Tipo: </span>
                            <span>{tipoOptions.find(t => t.value === novaUnidade.tipo)?.label || novaUnidade.tipo}</span>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default UnidadeMedida;
