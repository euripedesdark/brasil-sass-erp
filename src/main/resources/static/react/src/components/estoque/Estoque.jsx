import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputTextarea } from 'primereact/inputtextarea';
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
    const [ajusteVisible, setAjusteVisible] = useState(false);
    const [ajuste, setAjuste] = useState({ depositoId: '', produtoId: '', quantidadeDelta: '', motivo: '' });

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

    const realizarAjuste = async () => {
        const depositoId = Number(ajuste.depositoId);
        const produtoId = Number(ajuste.produtoId);
        const quantidadeDelta = Number(String(ajuste.quantidadeDelta).replace(',', '.'));
        if (!depositoId || !produtoId || !quantidadeDelta || !ajuste.motivo.trim()) {
            toast.current?.show({ severity: 'warn', summary: t('common.warning'), detail: 'Informe depósito, produto, quantidade e motivo.', life: 3000 });
            return;
        }
        try {
            await EstoqueService.ajustarSaldo(empresaId, { depositoId, produtoId, quantidadeDelta, motivo: ajuste.motivo.trim() });
            toast.current?.show({ severity: 'success', summary: t('common.success'), detail: 'Ajuste de inventário realizado.', life: 3000 });
            setAjusteVisible(false);
            setAjuste({ depositoId: '', produtoId: '', quantidadeDelta: '', motivo: '' });
            await carregarSaldos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: t('common.error'), detail: err?.response?.data?.message || 'Não foi possível realizar o ajuste.', life: 4000 });
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
            <Dialog header="Ajuste de inventário" visible={ajusteVisible} style={{ width: 'min(520px, 95vw)' }} onHide={() => setAjusteVisible(false)}
                footer={<div><Button label="Cancelar" text onClick={() => setAjusteVisible(false)} /><Button label="Aplicar ajuste" icon="pi pi-check" onClick={realizarAjuste} /></div>}>
                <div className="flex flex-column gap-3">
                    <div><label className="block mb-1">Depósito</label><InputNumber value={ajuste.depositoId} onValueChange={(e) => setAjuste(a => ({ ...a, depositoId: e.value ?? '' }))} useGrouping={false} className="w-full" /></div>
                    <div><label className="block mb-1">Produto</label><InputNumber value={ajuste.produtoId} onValueChange={(e) => setAjuste(a => ({ ...a, produtoId: e.value ?? '' }))} useGrouping={false} className="w-full" /></div>
                    <div><label className="block mb-1">Quantidade (+ entrada / − saída)</label><InputNumber value={ajuste.quantidadeDelta} onValueChange={(e) => setAjuste(a => ({ ...a, quantidadeDelta: e.value ?? '' }))} minFractionDigits={3} className="w-full" /></div>
                    <div><label className="block mb-1">Motivo</label><InputTextarea value={ajuste.motivo} onChange={(e) => setAjuste(a => ({ ...a, motivo: e.target.value }))} rows={3} autoResize className="w-full" /></div>
                </div>
            </Dialog>

            <Card title={t('inventory.title')}>
                <div className="flex justify-content-between align-items-center mb-3">
                    <p className="text-color-secondary m-0">{t('inventory.description')}</p>
                    <Button icon="pi pi-sliders-h" label="Ajustar inventário" onClick={() => setAjusteVisible(true)} />
                </div>
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
