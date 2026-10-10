import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { useAuth } from '../../contexts/AuthContext';
import { MarcaService } from '../../services/MarcaService';
import { useTranslation } from 'react-i18next';
import './Marca.css';

export const Marca = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const toast = useRef(null);
    const [marcas, setMarcas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const [novaMarca, setNovaMarca] = useState({
        id: null,
        nome: '',
        descricao: ''
    });

    useEffect(() => {
        fetchMarcas();
    }, []);

    const fetchMarcas = async () => {
        setLoading(true);
        try {
            const data = await MarcaService.listar();
            setMarcas(data || []);
        } catch (err) {
            console.error('Erro ao carregar marcas', err);
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: t('catalog.loadBrands'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const salvarMarca = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const marcaParaSalvar = {
                ...novaMarca
            };

            if (marcaParaSalvar.id) {
                const response = await MarcaService.atualizar(marcaParaSalvar.id, marcaParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('catalog.brandUpdated'),
                    life: 3000
                });
            } else {
                const response = await MarcaService.criar(marcaParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('catalog.brandCreated'),
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchMarcas();
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || t('catalog.saveBrandError'));
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: err.message || t('catalog.saveBrandError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirMarca = async (id) => {
        try {
            await MarcaService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('catalog.brandDeleted'),
                life: 3000
            });
            fetchMarcas();
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: err.message || t('catalog.deleteBrandError'),
                life: 3000
            });
        }
    };

    const abrirDialog = (marca = null) => {
        if (marca) {
            setNovaMarca({
                id: marca.id,
                nome: marca.nome || '',
                descricao: marca.descricao || ''
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovaMarca({
            id: null,
            nome: '',
            descricao: ''
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="marca-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip={t('common.edit')}
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirMarca(rowData.id)}
                    tooltip={t('common.delete')}
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label={t('common.cancel')}
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
            />
            <Button
                label={t('catalog.saveBrand')}
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarMarca}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="marca-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('catalog.brands')} className="marca-main-card">
                <div className="marca-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="marca-info">
                        <p className="text-muted m-0">{t('catalog.brandsDescription')}</p>
                    </div>
                    <Button
                        label={t('catalog.newBrand')}
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={marcas}
                    loading={loading}
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={marcas.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={t('catalog.noBrands')}
                >
                    <Column field="nome" header={t('common.name')} sortable style={{ width: '250px' }} />
                    <Column field="descricao" header={t('common.description')} sortable style={{ width: '400px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novaMarca.id ? `${t('common.edit')} ${t('catalog.brand')}: ${novaMarca.nome}` : t('catalog.newBrand')}
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
                    {success && <Message severity="success" text={t('catalog.brandSaved')} className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{t('common.name')} *</label>
                            <InputText
                                value={novaMarca.nome}
                                onChange={(e) => setNovaMarca({...novaMarca, nome: e.target.value})}
                                placeholder={t('catalog.brandNamePlaceholder')}
                                required
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputText
                                value={novaMarca.descricao}
                                onChange={(e) => setNovaMarca({...novaMarca, descricao: e.target.value})}
                                placeholder="Descricao da marca"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="marca-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Nome: </span>
                            <span>{novaMarca.nome}</span>
                        </div>
                        <div>
                            <span className="font-bold">Descricao: </span>
                            <span>{novaMarca.descricao || 'N/A'}</span>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Marca;
