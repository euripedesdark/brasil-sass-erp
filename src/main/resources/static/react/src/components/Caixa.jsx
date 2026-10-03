import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Toast } from 'primereact/toast';
import { CaixaService } from '../services/CaixaService';

export default function Caixa() {
    const { t } = useTranslation();
    const [caixas, setCaixas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({ first: 0, rows: 20, page: 0 });
    const [selectedCaixa, setSelectedCaixa] = useState(null);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const toast = useRef(null);

    useEffect(() => {
        loadCaixas();
    }, [lazyParams]);

    const loadCaixas = async () => {
        setLoading(true);
        try {
            const data = await CaixaService.search(searchTerm, lazyParams.page, lazyParams.rows);
            const lista = Array.isArray(data) ? data : (data?.content ?? []);
            setCaixas(lista);
            setTotalRecords(lista.length);
        } catch (error) {
            toast.current?.show({ severity: 'error', summary: t('legacyUi.caixa.attention'), detail: t('legacyUi.caixa.loadError') });
        } finally {
            setLoading(false);
        }
    };

    const onPageChange = (event) => {
        setLazyParams({
            first: event.first,
            rows: event.rows,
            page: event.page
        });
    };

    const openNew = () => {
        setSelectedCaixa({});
        setDialogVisible(true);
    };

    const hideDialog = () => {
        setDialogVisible(false);
        setSelectedCaixa(null);
    };

    const saveCaixa = async () => {
        try {
            const payload = {
                nome: (selectedCaixa.nome || '').trim(),
                saldo: selectedCaixa.saldo ?? 0,
                status: selectedCaixa.status || 'ATIVO'
            };
            if (!payload.nome) {
                toast.current?.show({ severity: 'warn', summary: t('legacyUi.caixa.attention'), detail: t('legacyUi.caixa.nameRequired') });
                return;
            }
            if (selectedCaixa.id) {
                await CaixaService.update(selectedCaixa.id, payload);
                toast.current?.show({ severity: 'success', summary: t('legacyUi.caixa.success'), detail: t('legacyUi.caixa.updated') });
            } else {
                await CaixaService.save(payload);
                toast.current?.show({ severity: 'success', summary: t('legacyUi.caixa.success'), detail: t('legacyUi.caixa.saved') });
            }
            loadCaixas();
            hideDialog();
        } catch (error) {
            toast.current?.show({ severity: 'error', summary: t('legacyUi.caixa.attention'), detail: t('legacyUi.caixa.saveError') });
        }
    };

    const deleteCaixa = async (caixa) => {
        try {
            await CaixaService.delete(caixa.id);
            toast.current?.show({ severity: 'success', summary: t('legacyUi.caixa.success'), detail: t('legacyUi.caixa.deactivated') });
            loadCaixas();
        } catch (error) {
            toast.current?.show({ severity: 'error', summary: t('legacyUi.caixa.attention'), detail: t('legacyUi.caixa.deleteError') });
        }
    };

    const actionBodyTemplate = (rowData) => {
        return (
            <div className="flex gap-2">
                <Button icon="pi pi-pencil" rounded outlined onClick={() => { setSelectedCaixa(rowData); setDialogVisible(true); }} />
                <Button icon="pi pi-trash" rounded outlined severity="danger" onClick={() => deleteCaixa(rowData)} />
            </div>
        );
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            
            <div className="card">
                <div className="flex justify-between mb-4">
                    <h2>Caixa</h2>
                    <div className="flex gap-2">
                        <InputText 
                            placeholder="Buscar caixa..." 
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && loadCaixas()}
                        />
                        <Button label={t('legacyUi.caixa.new')} icon="pi pi-plus" onClick={openNew} />
                    </div>
                </div>

                <DataTable 
                    value={caixas}
                    paginator
                    first={lazyParams.first}
                    rows={lazyParams.rows}
                    totalRecords={totalRecords}
                    lazy
                    loading={loading}
                    onPage={onPageChange}
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} caixas"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    emptyMessage={t('legacyUi.caixa.empty')}
                >
                    <Column field="id" header="ID" sortable style={{ width: '80px' }} />
                    <Column field="nome" header={t('legacyUi.caixa.name')} sortable />
                    <Column field="saldo" header={t('legacyUi.caixa.balance')} sortable body={(data) => `R$ ${Number(data.saldo ?? 0).toFixed(2)}`} />
                    <Column field="status" header={t('common.status')} sortable />
                    <Column body={actionBodyTemplate} style={{ width: '120px' }} />
                </DataTable>
            </div>

            <Dialog visible={dialogVisible} style={{ width: '500px' }} header={t('legacyUi.caixa.title')} modal onHide={hideDialog}>
                <div className="flex flex-column gap-3">
                    <div className="field">
                        <label htmlFor="nome">Nome</label>
                        <InputText id="nome" value={selectedCaixa?.nome || ''} onChange={(e) => setSelectedCaixa({...selectedCaixa, nome: e.target.value})} className="w-full" />
                    </div>
                    <div className="field">
                        <label htmlFor="saldo">Saldo Inicial</label>
                        <InputText id="saldo" type="number" value={selectedCaixa?.saldo || ''} onChange={(e) => setSelectedCaixa({...selectedCaixa, saldo: e.target.value === '' ? '' : Number(e.target.value)})} className="w-full" />
                    </div>
                </div>
                <div className="flex justify-end gap-2 mt-4">
                    <Button label={t('common.cancel')} icon="pi pi-times" onClick={hideDialog} severity="secondary" />
                    <Button label={t('common.save')} icon="pi pi-check" onClick={saveCaixa} />
                </div>
            </Dialog>
        </div>
    );
}
