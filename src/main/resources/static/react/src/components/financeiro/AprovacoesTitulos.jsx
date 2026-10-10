import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import { useTranslation } from 'react-i18next';
import TituloService from '../../services/TituloService';
import { localeAtivo } from '../shared/LocaleData.js';

/**
 * Fila de aprovações de títulos financeiros.
 * Item 1 da Fase 1 — Relatório de Paridade Funcional ERP (25/09/2026).
 * Consome GET /api/financeiro/titulos/aprovacoes/pendentes (V86/V87).
 */
export const AprovacoesTitulos = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [aprovacoes, setAprovacoes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [observacoes, setObservacoes] = useState({});

    useEffect(() => {
        fetchPendentes();
    }, []);

    const fetchPendentes = async () => {
        setLoading(true);
        try {
            const res = await TituloService.aprovacoesPendentes();
            const data = res?.data?.data ?? res?.data ?? [];
            setAprovacoes(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
            toast.current?.show({
                severity: 'error',
                summary: t('finance.approvals.error'),
                detail: t('finance.approvals.loadError'),
                life: 3000,
            });
            setAprovacoes([]);
        } finally {
            setLoading(false);
        }
    };

    const decidir = async (row, acao) => {
        const observacao = observacoes[row.id] || '';
        if (acao === 'REJEITAR' && !observacao.trim()) {
            toast.current?.show({
                severity: 'warn',
                summary: t('finance.approvals.observationRequired'),
                detail: t('finance.approvals.rejectionReason'),
                life: 3000,
            });
            return;
        }
        setLoading(true);
        try {
            if (acao === 'APROVAR') {
                await TituloService.aprovar(row.id, observacao || null);
            } else {
                await TituloService.rejeitar(row.id, observacao);
            }
            toast.current?.show({
                severity: 'success',
                summary: acao === 'APROVAR' ? t('finance.approvals.approved') : t('finance.approvals.rejected'),
                detail: t('finance.approvals.decisionSaved', { level: row.nivel }),
                life: 3000,
            });
            setObservacoes((prev) => ({ ...prev, [row.id]: '' }));
            await fetchPendentes();
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.response?.data?.error ||
                t('finance.approvals.decisionError');
            toast.current?.show({ severity: 'error', summary: t('finance.approvals.error'), detail: msg, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    const moeda = (v) =>
        (v == null ? 0 : Number(v)).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });

    const dataBr = (d) => (d ? new Date(d).toLocaleDateString(localeAtivo()) : '');

    const tipoBody = (row) => {
        if (row.tipoTitulo === 'R') return <Tag value={t('finance.approvals.receivable')} severity="success" />;
        if (row.tipoTitulo === 'P') return <Tag value={t('finance.approvals.payable')} severity="warning" />;
        return <Tag value={row.tipoTitulo || '—'} />;
    };

    const nivelBody = (row) => <Tag value={t('finance.approvals.level', { level: row.nivel })} severity="info" />;

    const solicitanteBody = (row) => row.solicitanteNome || row.usuarioSolicitanteId || '—';

    const obsBody = (row) => (
        <InputText
            placeholder={t('finance.approvals.observationPlaceholder')}
            value={observacoes[row.id] || ''}
            onChange={(e) => setObservacoes((prev) => ({ ...prev, [row.id]: e.target.value }))}
            style={{ width: '100%' }}
        />
    );

    const acoes = (row) => (
        <div className="flex gap-1">
            <Button
                label={t('finance.approvals.approve')}
                icon="pi pi-check"
                className="p-button-sm p-button-success"
                loading={loading}
                onClick={() => decidir(row, 'APROVAR')}
            />
            <Button
                label={t('finance.approvals.reject')}
                icon="pi pi-times"
                className="p-button-sm p-button-danger"
                loading={loading}
                onClick={() => decidir(row, 'REJEITAR')}
            />
        </div>
    );

    return (
        <div>
            <Toast ref={toast} />
            <Card title={t('finance.approvals.title')}>
                <p className="text-color-secondary mb-3">
                    {t('finance.approvals.description')}
                </p>
                <div className="flex gap-2 align-items-end mb-3">
                    <Button label={t('common.refresh')} icon="pi pi-refresh" onClick={fetchPendentes} loading={loading} />
                </div>
                <DataTable
                    value={aprovacoes}
                    loading={loading}
                    paginator
                    rows={12}
                    rowsPerPageOptions={[12, 25, 50]}
                    emptyMessage={t('finance.approvals.empty')}
                    size="small"
                >
                    <Column field="numeroDocumento" header={t('finance.approvals.document')} style={{ width: '130px' }} />
                    <Column field="tipoTitulo" header={t('finance.approvals.type')} body={tipoBody} style={{ width: '100px' }} />
                    <Column field="valorSaldo" header={t('finance.approvals.balance')} body={(r) => moeda(r.valorSaldo)} style={{ width: '130px' }} />
                    <Column field="dataVencimento" header={t('finance.approvals.dueDate')} body={(r) => dataBr(r.dataVencimento)} style={{ width: '110px' }} />
                    <Column field="nivel" header={t('finance.approvals.levelLabel')} body={nivelBody} style={{ width: '90px' }} />
                    <Column field="solicitanteNome" header={t('finance.approvals.requester')} body={solicitanteBody} style={{ width: '150px' }} />
                    <Column field="dataSolicitacao" header={t('finance.approvals.requestedAt')} body={(r) => (r.dataSolicitacao ? new Date(r.dataSolicitacao).toLocaleString(localeAtivo()) : '—')} style={{ width: '150px' }} />
                    <Column header={t('finance.approvals.observation')} body={obsBody} style={{ minWidth: '200px' }} />
                    <Column header={t('finance.approvals.decision')} body={acoes} style={{ width: '210px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default AprovacoesTitulos;
