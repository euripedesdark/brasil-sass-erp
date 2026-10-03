import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { OverlayPanel } from 'primereact/overlaypanel';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Tag } from 'primereact/tag';
import { useAuth } from '../contexts/AuthContext';
import RecentService from '../services/RecentService';
import { useTranslation } from 'react-i18next';

const RecentUpdates = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const navigate = useNavigate();
    const op = useRef(null);
    const empresaId = user?.empresaId ?? null;
    const [updates, setUpdates] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchUpdates = useCallback(async () => {
        if (!empresaId) return;

        setLoading(true);
        try {
            const data = await RecentService.getUpdates(empresaId);
            setUpdates(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Erro ao buscar atualizações recentes:', error.response?.data || error);
            setUpdates([]);
        } finally {
            setLoading(false);
        }
    }, [empresaId]);

    useEffect(() => {
        if (empresaId) {
            fetchUpdates();
        }
    }, [empresaId, fetchUpdates]);

    const handleQuickEdit = useCallback((update) => {
        const paths = {
            Venda: '/vendas/editar/' + update.id,
            Financeiro: '/financeiro/editar/' + update.id,
            Estoque: '/estoque/editar/' + update.id
        };
        navigate(paths[update.modulo] || '/dashboard');
    }, [navigate]);

    const moduloTemplate = useCallback((rowData) => {
        const severity = {
            Venda: 'success',
            Financeiro: 'info',
            Estoque: 'warning'
        }[rowData.modulo] || 'info';

        return <Tag value={rowData.modulo} severity={severity} />;
    }, []);

    const actionTemplate = useCallback((rowData) => (
        <Button
            icon="pi pi-pencil"
            className="p-button-text p-button-sm"
            onClick={() => handleQuickEdit(rowData)}
        />
    ), [handleQuickEdit]);

    return (
        <div className="recent-updates-trigger">
            <Button
                label={t('recent.activities')}
                icon="pi pi-clock"
                className="p-button-rounded p-button-text"
                onClick={(e) => op.current?.toggle(e)}
            />

            <OverlayPanel ref={op}>
                {loading ? (
                    <div
                        className="loading-spinner p-4 text-center"
                        style={{ width: '450px' }}
                    >
                        <i className="pi pi-spin pi-spinner mr-2"></i>
                        {t('recent.loading')}
                    </div>
                ) : (
                    <div
                        className="recent-updates-container"
                        style={{ width: '450px' }}
                    >
                        <div className="recent-updates-header mb-3 flex justify-content-between align-items-center">
                            <span className="font-bold">{t('recent.title')}</span>
                            <Button
                                icon="pi pi-refresh"
                                className="p-button-text p-button-sm"
                                onClick={fetchUpdates}
                            />
                        </div>

                        <DataTable
                            value={updates}
                            responsiveLayout="scroll"
                            className="p-datatable-sm"
                            emptyMessage={t('recent.empty')}
                        >
                            <Column body={moduloTemplate} header={t('dashboard.module')} sortable />
                            <Column field="tipo" header={t('recent.type')} sortable />
                            <Column field="data" header={t('common.date')} sortable />
                            <Column header={t('recent.action')} body={actionTemplate} />
                        </DataTable>
                    </div>
                )}
            </OverlayPanel>
        </div>
    );
};

export default RecentUpdates;
