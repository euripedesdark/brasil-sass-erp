import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
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
import { useAuth } from '../../contexts/AuthContext';
import { CargoService } from '../../services/CargoService';

export const Cargo = () => {
  const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [cargos, setCargos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const [novoCargo, setNovoCargo] = useState({
        id: null,
        nome: '',
        salarioBase: 0,
        ativo: true
    });

    const statusOptions = [
        { label: 'Ativo', value: true },
        { label: 'Inativo', value: false }
    ];

    useEffect(() => {
        fetchCargos();
    }, []);

    const fetchCargos = async () => {
        setLoading(true);
        try {
            const data = await CargoService.listar();
            setCargos(data || []);
        } catch (err) {
            console.error('Erro ao carregar cargos', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: t('legacyUi.cargo.loadError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const salvarCargo = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const cargoParaSalvar = {
                ...novoCargo
            };

            if (cargoParaSalvar.id) {
                await CargoService.atualizar(cargoParaSalvar.id, cargoParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: t('legacyUi.cargo.saved'),
                    life: 3000
                });
            } else {
                await CargoService.criar(cargoParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: t('legacyUi.cargo.saved'),
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchCargos();
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || t('legacyUi.cargo.saveError'));
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || t('legacyUi.cargo.saveError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirCargo = async (id) => {
        try {
            await CargoService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: 'Sucesso',
                detail: t('legacyUi.cargo.saved'),
                life: 3000
            });
            fetchCargos();
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || t('legacyUi.cargo.deleteError'),
                life: 3000
            });
        }
    };

    const statusTemplate = (rowData) => {
        return (
            <Tag 
                value={rowData.ativo ? 'Ativo' : 'Inativo'} 
                severity={rowData.ativo ? 'success' : 'danger'} 
            />
        );
    };

    const salarioTemplate = (rowData) => {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(rowData.salarioBase || 0);
    };

    const abrirDialog = (cargo = null) => {
        if (cargo) {
            setNovoCargo({
                id: cargo.id,
                nome: cargo.nome || '',
                salarioBase: cargo.salarioBase || 0,
                ativo: cargo.ativo !== false
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoCargo({
            id: null,
            nome: '',
            salarioBase: 0,
            ativo: true
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="cargo-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip={t('legacyUi.cargo.edit')}
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirCargo(rowData.id)}
                    tooltip={t('legacyUi.cargo.delete')}
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label={t('legacyUi.cargo.cancel')}
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
            />
            <Button
                label={t('legacyUi.cargo.save')}
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarCargo}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="cargo-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('legacyUi.cargo.title')} className="cargo-main-card">
                <div className="cargo-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="cargo-info">
                        <p className="text-muted m-0">Gestao de cargos e funcoes.</p>
                    </div>
                    <Button
                        label={t('legacyUi.cargo.new')}
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={cargos}
                    loading={loading}
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={cargos.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={t('legacyUi.cargo.empty')}
                >
                    <Column field="nome" header={t('legacyUi.cargo.name')} sortable style={{ width: '300px' }} />
                    <Column field="salarioBase" header={t('legacyUi.cargo.salary')} body={salarioTemplate} sortable style={{ width: '150px' }} />
                    <Column field="ativo" header={t('legacyUi.cargo.status')} body={statusTemplate} sortable style={{ width: '120px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoCargo.id ? `${t('legacyUi.cargo.edit')} Cargo: ${novoCargo.nome}` : t('legacyUi.cargo.new')}
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
                    {success && <Message severity="success" text="Cargo salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Nome *</label>
                            <InputText
                                value={novoCargo.nome}
                                onChange={(e) => setNovoCargo({...novoCargo, nome: e.target.value})}
                                placeholder="Nome do cargo"
                                required
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Salario Base *</label>
                            <InputNumber
                                value={novoCargo.salarioBase}
                                onChange={(e) => setNovoCargo({...novoCargo, salarioBase: e.value})}
                                mode="currency"
                                currency="BRL"
                                locale="pt-BR"
                                placeholder="Salario base"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <Dropdown
                                value={novoCargo.ativo}
                                options={statusOptions}
                                onChange={(e) => setNovoCargo({...novoCargo, ativo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="cargo-resumo flex flex-wrap justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Nome: </span>
                            <span>{novoCargo.nome}</span>
                        </div>
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Salario: </span>
                            <span>{new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(novoCargo.salarioBase || 0)}</span>
                        </div>
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Status: </span>
                            <Tag value={novoCargo.ativo ? 'Ativo' : 'Inativo'} severity={novoCargo.ativo ? 'success' : 'danger'} />
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Cargo;
