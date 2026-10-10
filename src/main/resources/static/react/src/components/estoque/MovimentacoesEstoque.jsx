import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import EstoqueService from '../../services/EstoqueService';
import { useAuth } from '../../contexts/AuthContext';
import { localeAtivo } from '../shared/LocaleData.js';

export const MovimentacoesEstoque = () => {
        const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [movs, setMovs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [produtoId, setProdutoId] = useState(null);

    const empresaId = user?.empresaId;

    useEffect(() => {
        if (empresaId) carregar();
    }, [empresaId]);

    const carregar = async (filtroProduto) => {
        setLoading(true);
        try {
            const res = await EstoqueService.listarMovimentacoes(
                empresaId,
                filtroProduto != null ? filtroProduto : undefined
            );
            const data = res?.data?.data ?? res?.data ?? [];
            setMovs(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('estoque.movements.loadError'),
                life: 3000,
            });
            setMovs([]);
        } finally {
            setLoading(false);
        }
    };

    const tipoBody = (row) => {
        const tipo = (row.tipo || '').toUpperCase();
        const severity = tipo === 'ENTRADA' ? 'success' : tipo === 'SAIDA' ? 'danger' : 'info';
        return <Tag value={row.tipo} severity={severity} />;
    };

    const dataBody = (row) =>
        row.dataMovimento ? new Date(row.dataMovimento).toLocaleString(localeAtivo()) : '';

    const qtdBody = (row) => Number(row.quantidade || 0).toLocaleString(localeAtivo());

    return (
        <div>
            <Toast ref={toast} />
            <Card title={t('estoque.movements.title')}>
                <p className="text-color-secondary mb-3">
                    {t('estoque.movements.description')}
                </p>

                <div className="flex flex-wrap gap-2 align-items-end mb-3">
                    <div className="field m-0">
                        <label className="font-bold block mb-1">{t('estoque.movements.productFilter')}</label>
                        <InputNumber value={produtoId} onValueChange={(e) => setProdutoId(e.value)} />
                    </div>
                    <Button label={t('common.filter')} icon="pi pi-filter" onClick={() => carregar(produtoId)} />
                    <Button
                        label={t('estoque.movements.allCompany')}
                        icon="pi pi-list"
                        className="p-button-outlined"
                        onClick={() => {
                            setProdutoId(null);
                            carregar(null);
                        }}
                    />
                </div>

                <DataTable
                    value={movs}
                    loading={loading}
                    paginator
                    rows={15}
                    rowsPerPageOptions={[15, 30, 50]}
                    emptyMessage={t('estoque.movements.empty')}
                    size="small"
                >
                    <Column field="dataMovimento" header={t('common.date')} body={dataBody} style={{ width: '160px' }} />
                    <Column field="produtoId" header={t('estoque.common.product')} style={{ width: '100px' }} />
                    <Column field="loteId" header={t('estoque.common.lot')} style={{ width: '90px' }} />
                    <Column field="enderecoId" header={t('estoque.common.address')} style={{ width: '100px' }} />
                    <Column field="tipo" header={t('common.type')} body={tipoBody} style={{ width: '110px' }} />
                    <Column field="origem" header={t('estoque.common.origin')} style={{ width: '100px' }} />
                    <Column field="origemId" header={t('estoque.common.originId')} style={{ width: '100px' }} />
                    <Column field="quantidade" header={t('estoque.common.quantityShort')} body={qtdBody} style={{ width: '100px' }} />
                    <Column
                        field="saldoApos"
                        header={t('estoque.movements.balanceAfter')}
                        body={(r) => Number(r.saldoApos || 0).toLocaleString(localeAtivo())}
                        style={{ width: '110px' }}
                    />
                    <Column field="observacao" header={t('common.note')} />
                </DataTable>
            </Card>
        </div>
    );
};

export default MovimentacoesEstoque;
