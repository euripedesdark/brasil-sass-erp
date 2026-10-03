import React, { useState, useEffect } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Tag } from 'primereact/tag';
import { useConfirmation } from 'primereact/confirmdialog';
import ApiConfig, { apiFetch } from '../services/ApiConfig';
import { downloadAuthenticated } from '../services/downloadService';
import { useAuth } from '../contexts/AuthContext';
import { NotificationService } from '../services/NotificationService';
import { useTranslation } from 'react-i18next';

export const Fiscal = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const confirm = useConfirmation();
    const [notas, setNotas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [emitDialogVisible, setEmitDialogVisible] = useState(false);

    const [novaNota, setNovaNota] = useState({
        tipo: 'NFSe',
        pedidoId: '',
        ambiente: 'HOMOLOGACAO'
    });

    useEffect(() => {
        fetchNotas();
    }, []);

    const fetchNotas = async () => {
        setLoading(true);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/fiscal/nfse`);
            const data = await response.json();
            setNotas(data);
        } catch (err) {
            console.error('Erro ao carregar notas fiscais', err);
        } finally {
            setLoading(false);
        }
    };

    const emitirNota = async () => {
        setLoading(true);
        try {
            const endpoint = '/api/fiscal/nfse/emitir';
            const response = await apiFetch(`${ApiConfig.BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...novaNota, empresaId: user?.empresaId })
            });

            if (response.ok) {
                NotificationService.showSuccess(t('fiscal.invoiceIssued'));
                setEmitDialogVisible(false);
                fetchNotas();
            } else {
                const err = await response.text();
                throw new Error(err);
            }
        } catch (err) {
            NotificationService.showError(err.message || t('fiscal.emitError'));
        } finally {
            setLoading(false);
        }
    };

    const cancelarNota = (notaId) => {
        confirm({
            message: t('fiscal.cancelConfirm'),
            header: t('fiscal.cancelTitle'),
            icon: 'pi pi-exclamation-triangle',
            acceptClassName: 'p-button-danger',
            accept: async () => {
                try {
                    const response = await apiFetch(`${ApiConfig.BASE_URL}/api/fiscal/nfse/${notaId}/cancelar`, {
                        method: 'POST'
                    });
                    if (response.ok) {
                        NotificationService.showSuccess(t('fiscal.invoiceCanceled'));
                        fetchNotas();
                    } else {
                        throw new Error(await response.text());
                    }
                } catch (err) {
                    NotificationService.showError(err.message);
                }
            }
        });
    };

    const statusTemplate = (rowData) => {
        const severity = {
            'AUTORIZADA': 'success',
            'PENDENTE': 'warning',
            'REJEITADA': 'danger',
            'CANCELADA': 'info'
        }[rowData.status] || 'info';

        return <Tag value={rowData.status} severity={severity} />;
    };

    const baixarPdf = async (id) => {
        try {
            await downloadAuthenticated(
                `${ApiConfig.BASE_URL}/api/fiscal/nfse/${id}/pdf`,
                `nfse-${id}.pdf`
            );
        } catch (e) {
            toast.current?.show({
                severity: 'error',
                summary: t('errors.title'),
                detail: e.message || 'Não foi possível baixar o PDF da NFS-e.',
                life: 5000
            });
        }
    };

    const actionTemplate = (rowData) => {
        return (
            <div className="flex gap-2">
                <Button
                    icon="pi pi-file-pdf"
                    className="p-button-text"
                    onClick={() => baixarPdf(rowData.id)}
                />
                <Button
                    icon="pi pi-times"
                    className="p-button-text p-button-danger"
                    onClick={() => cancelarNota(rowData.id)}
                />
            </div>
        );
    };

    return (
        <div className="fiscal-container">
            <Card title={t('fiscal.screenTitle')} className="fiscal-main-card">
                <div className="flex justify-content-between align-items-center mb-4">
                    <div className="fiscal-info">
                        <p className="text-muted m-0">{t('fiscal.screenDescription')}</p>
                    </div>
                    <Button label={t('fiscal.emitNew')} icon="pi pi-plus" onClick={() => setEmitDialogVisible(true)} className="p-button-success" />
                </div>

                <DataTable
                    value={notas}
                    loading={loading}
                    paginator
                    rows={10}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                >
                    <Column field="numero" header={t('fiscal.number')} sortable></Column>
                    <Column field="tipo" header={t('fiscal.invoiceType')} sortable></Column>
                    <Column field="dataEmissao" header={t('fiscal.date')} sortable></Column>
                    <Column field="valorTotal" header={t('fiscal.totalValue')} sortable></Column>
                    <Column field="status" header={t('common.status')} body={statusTemplate} sortable></Column>
                    <Column header={t('fiscal.actions')} body={actionTemplate} style={{ width: '120px' }}></Column>
                </DataTable>
            </Card>

            <Dialog
                header={t('fiscal.documentTitle')}
                visible={emitDialogVisible}
                style={{ width: '500px' }}
                onHide={() => setEmitDialogVisible(false)}
            >
                <div className="p-fluid">
                    <div className="field mb-4">
                        <label className="font-bold mb-2 block">{t('fiscal.noteType')}</label>
                        <Dropdown
                            value={novaNota.tipo}
                            options={['NFSe']}
                            onChange={(e) => setNovaNota({...novaNota, tipo: e.value})}
                        />
                    </div>
                    <div className="field mb-4">
                        <label className="font-bold mb-2 block">{t('fiscal.orderServiceId')}</label>
                        <InputText
                            value={novaNota.pedidoId}
                            onChange={(e) => setNovaNota({...novaNota, pedidoId: e.target.value})}
                            placeholder={t('fiscal.example')}
                        />
                    </div>
                    <div className="field mb-4">
                        <label className="font-bold mb-2 block">{t('fiscal.environment')}</label>
                        <Dropdown
                            value={novaNota.ambiente}
                            options={['HOMOLOGACAO', 'PRODUCAO']}
                            onChange={(e) => setNovaNota({...novaNota, ambiente: e.value})}
                        />
                    </div>

                    <div className="flex justify-content-end gap-2 mt-4">
                        <Button label={t('common.cancel')} icon="pi pi-times" className="p-button-text" onClick={() => setEmitDialogVisible(false)} />
                        <Button label={t('fiscal.issue')} icon="pi pi-send" onClick={emitirNota} loading={loading} />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Fiscal;
