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
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';
import { desembrulharLista } from '../../services/ApiConfig';
import { useTranslation } from 'react-i18next';

export const Categoria = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const toast = useRef(null);
    const [categorias, setCategorias] = useState([]);
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

    const [novaCategoria, setNovaCategoria] = useState({
        id: null,
        descricao: '',
        ativo: true
    });

    const statusOptions = [
        { label: t('common.activeFeminine'), value: true },
        { label: t('common.inactiveFeminine'), value: false }
    ];

    useEffect(() => {
        fetchCategorias();
    }, [lazyParams]);

    const fetchCategorias = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/categorias?${params}`);
            if (response.ok) {
                const { lista, total } = desembrulharLista(await response.json());
                setCategorias(lista);
                setTotalRecords(total);
            } else {
                console.error('Erro ao carregar categorias');
            }
        } catch (err) {
            console.error('Erro ao carregar categorias', err);
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: t('catalog.loadCategories'),
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
                value={rowData.ativo ? t('common.activeFeminine') : t('common.inactiveFeminine')}
                severity={rowData.ativo ? 'success' : 'danger'} 
            />
        );
    };

    const salvarCategoria = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const categoriaParaSalvar = {
                ...novaCategoria,
                empresaId: user?.empresaId,
                usuarioId: user?.id
            };

            const method = categoriaParaSalvar.id ? 'PUT' : 'POST';
            const url = categoriaParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/cadastro/categorias/${categoriaParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/cadastro/categorias`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(categoriaParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: categoriaParaSalvar.id ? t('catalog.categoryUpdated') : t('catalog.categoryCreated'),
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchCategorias();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: t('errors.title'),
                    detail: err || t('catalog.saveCategoryError'),
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar categoria');
                        setError(t('catalog.saveCategoryError'));
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: t('errors.network'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirCategoria = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/categorias/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('catalog.categoryDeleted'),
                    life: 3000
                });
                fetchCategorias();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: t('errors.title'),
                    detail: err || t('catalog.deleteCategoryError'),
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: t('errors.network'),
                life: 3000
            });
        }
    };

    const abrirDialog = (categoria = null) => {
        if (categoria) {
            setNovaCategoria({
                id: categoria.id,
                descricao: categoria.descricao || '',
                ativo: categoria.ativo !== false
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovaCategoria({
            id: null,
            descricao: '',
            ativo: true
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="categoria-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip={t('common.edit')}
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirCategoria(rowData.id)}
                    tooltip={t('common.delete')}
                    disabled={!rowData.ativo}
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
                label={t('catalog.saveCategory')}
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarCategoria}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="categoria-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('catalog.categoriesTitle')} className="categoria-main-card">
                <div className="categoria-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="categoria-info">
                        <p className="text-muted m-0">{t('catalog.categoriesDescription')}</p>
                    </div>
                    <Button
                        label={t('catalog.newCategory')}
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={categorias}
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
                    currentPageReportTemplate={t('catalog.categoriesPageReport')}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={t('catalog.noCategories')}
                >
                    <Column field="descricao" header={t('common.description')} sortable style={{ width: '400px' }} />
                    <Column field="ativo" header={t('common.status')} body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novaCategoria.id ? `${t('common.edit')} ${t('catalog.category')}: ${novaCategoria.descricao}` : t('catalog.newCategory')}
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
                    {success && <Message severity="success" text={t('catalog.categorySaved')} className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{t('common.description')}</label>
                            <InputText
                                value={novaCategoria.descricao}
                                onChange={(e) => setNovaCategoria({...novaCategoria, descricao: e.target.value})}
                                placeholder={t('catalog.categoryPlaceholder')}
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{t('common.status')}</label>
                            <Dropdown
                                value={novaCategoria.ativo}
                                options={statusOptions}
                                onChange={(e) => setNovaCategoria({...novaCategoria, ativo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="categoria-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Descricao: </span>
                                                        <span className="font-bold">{t('common.description')}: </span>
                            <span>{novaCategoria.descricao}</span>
                        </div>
                        <div>
                            <span className="font-bold">Status: </span>
                            <Tag value={novaCategoria.ativo ? t('common.activeFeminine') : t('common.inactiveFeminine')} severity={novaCategoria.ativo ? 'success' : 'danger'} />
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Categoria;
