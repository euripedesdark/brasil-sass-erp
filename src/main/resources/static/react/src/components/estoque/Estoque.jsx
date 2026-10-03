import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import EstoqueService from '../../services/EstoqueService';
import { useAuth } from '../../contexts/AuthContext';
import { useTranslation } from 'react-i18next';

export const Estoque = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const toast = useRef(null);
    const [saldos, setSaldos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [filtroProdutoId, setFiltroProdutoId] = useState(null);
    const [saldoConsulta, setSaldoConsulta] = useState(null);

    const empresaId = user?.empresaId;

    useEffect(() => {
        if (empresaId) carregarSaldos();
    }, [empresaId]);

    const carregarSaldos = async () => {
        setLoading(true);
        try {
            const res = await EstoqueService.listarSaldos(empresaId);
            const data = res?.data?.data ?? res?.data ?? [];
            setSaldos(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('inventory.loadError'),
                life: 3000,
            });
            setSaldos([]);
        } finally {
            setLoading(false);
        }
    };

    const consultarProduto = async () => {
        if (!filtroProdutoId || !empresaId) {
            toast.current?.show({
                severity: 'warn',
                summary: t('common.warning'),
                detail: t('inventory.productIdRequired'),
                life: 2500,
            });
            return;
        }
        try {
            const res = await EstoqueService.buscarSaldo(empresaId, filtroProdutoId);
            const data = res?.data?.data ?? res?.data;
            setSaldoConsulta(data);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('inventory.queryError'),
                life: 3000,
            });
        }
    };

    const qtdBody = (row) => {
        const q = Number(row.quantidade) || 0;
        const severity = q <= 0 ? 'danger' : q < 10 ? 'warning' : 'success';
        return <Tag value={q.toLocaleString('pt-BR')} severity={severity} />;
    };

    return (
        <div>
            <Toast ref={toast} />
            <Card title={t('inventory.title')}>
                <p className="text-color-secondary mb-3">
                    {t('inventory.description')}
                </p>

                <div className="flex flex-wrap gap-2 align-items-end mb-4">
                    <div className="field m-0">
                        <label className="font-bold block mb-1">{t('inventory.productId')}</label>
                        <InputNumber
                            value={filtroProdutoId}
                            onValueChange={(e) => setFiltroProdutoId(e.value)}
                            placeholder="ID"
                        />
                    </div>
                    <Button label={t('inventory.queryBalance')} icon="pi pi-search" onClick={consultarProduto} />
                    <Button
                        label={t('inventory.refreshList')}
                        icon="pi pi-refresh"
                        className="p-button-outlined"
                        onClick={carregarSaldos}
                    />
                </div>

                {saldoConsulta && (
                    <div className="mb-3 p-3 border-round surface-100">
                        <strong>{t('inventory.product', { id: saldoConsulta.produtoId })}</strong>
                        {` ${t('inventory.quantity')}: `}
                        <Tag
                            value={Number(saldoConsulta.quantidade || 0).toLocaleString('pt-BR')}
                            severity={
                                Number(saldoConsulta.quantidade) <= 0 ? 'danger' : 'success'
                            }
                        />
                    </div>
                )}

                <DataTable
                    value={saldos}
                    loading={loading}
                    paginator
                    rows={15}
                    rowsPerPageOptions={[15, 30, 50]}
                    emptyMessage={t('inventory.empty')}
                    size="small"
                >
                    <Column field="produtoId" header={t('inventory.productId')} style={{ width: '140px' }} />
                    <Column field="quantidade" header={t('inventory.quantity')} body={qtdBody} style={{ width: '140px' }} />
                    <Column field="empresaId" header={t('inventory.company')} style={{ width: '100px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Estoque;
