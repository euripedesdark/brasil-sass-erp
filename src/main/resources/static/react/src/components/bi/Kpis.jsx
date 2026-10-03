import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import KpiService from '../../services/KpiService';
import { useAuth } from '../../contexts/AuthContext';

export const Kpis = () => {
  const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [kpis, setKpis] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [kpiSelecionado, setKpiSelecionado] = useState(null);

    const vazio = {
        id: null,
        name: '',
        description: '',
        kpiType: 'FINANCEIRO',
        queryFormula: '',
        targetValue: null,
        unit: '',
        format: 'NUMERO',
        thresholdGood: null,
        thresholdWarning: null,
        colorGood: '#10b981',
        colorWarning: '#f59e0b',
        colorBad: '#ef4444',
        isActive: true
    };

    const [form, setForm] = useState(vazio);

    const tipoOptions = [
        { label: 'Financeiro', value: 'FINANCEIRO' },
        { label: 'Vendas', value: 'VENDAS' },
        { label: 'Estoque', value: 'ESTOQUE' },
        { label: 'Produção', value: 'PRODUCAO' },
        { label: 'RH', value: 'RH' },
        { label: 'Fiscal', value: 'FISCAL' },
        { label: 'Genérico', value: 'GENERICO' }
    ];

    const fetchKpis = async () => {
        setLoading(true);
        try {
            const data = await KpiService.listar(user?.empresaId || '');
            setKpis(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(t('legacyUi.kpi.loadError'), err);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('legacyUi.kpi.loadError'), life: 3000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchKpis();
    }, []);

    const abrirNovo = () => {
        setKpiSelecionado(null);
        setForm(vazio);
        setDialogVisible(true);
    };

    const abrirEdicao = (kpi) => {
        setKpiSelecionado(kpi);
        setForm({
            id: kpi.id,
            name: kpi.name || '',
            description: kpi.description || '',
            kpiType: kpi.kpiType || 'FINANCEIRO',
            queryFormula: kpi.queryFormula || '',
            targetValue: kpi.targetValue,
            unit: kpi.unit || '',
            format: kpi.format || 'NUMERO',
            thresholdGood: kpi.thresholdGood,
            thresholdWarning: kpi.thresholdWarning,
            colorGood: kpi.colorGood || '#10b981',
            colorWarning: kpi.colorWarning || '#f59e0b',
            colorBad: kpi.colorBad || '#ef4444',
            isActive: kpi.isActive !== false
        });
        setDialogVisible(true);
    };

    const salvar = async () => {
        if (!form.name?.trim()) {
            toast.current?.show({ severity: 'warn', summary: t('common.warning'), detail: t('legacyUi.kpi.nameRequired'), life: 3000 });
            return;
        }
        try {
            if (form.id) {
                await KpiService.atualizar(user?.empresaId, form.id, form);
            } else {
                await KpiService.criar(user?.empresaId, form);
            }
            toast.current?.show({ severity: 'success', summary: t('messages.success'), detail: t('legacyUi.kpi.saved'), life: 3000 });
            setDialogVisible(false);
            fetchKpis();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const excluir = (kpi) => {
        confirmDialog({
            message: `Deseja realmente excluir o KPI "${kpi.name}"?`,
            header: 'Confirmar exclusão',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('legacyUi.kpi.delete'),
            rejectLabel: t('legacyUi.kpi.cancel'),
            acceptClassName: 'p-button-danger',
            accept: async () => {
                try {
                    await KpiService.excluir(user?.empresaId, kpi.id);
                    toast.current?.show({ severity: 'success', summary: t('messages.success'), detail: t('legacyUi.kpi.deleted'), life: 3000 });
                    fetchKpis();
                } catch (err) {
                    toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
                }
            }
        });
    };

    const calcular = async (kpi) => {
        try {
            await KpiService.calcular(user?.empresaId, kpi.id);
            toast.current?.show({ severity: 'success', summary: 'Calculado', detail: `Valor do KPI "${kpi.name}" atualizado`, life: 3000 });
            fetchKpis();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const refreshTodos = async () => {
        try {
            await KpiService.refresh(user?.empresaId);
            toast.current?.show({ severity: 'success', summary: t('messages.success'), detail: t('legacyUi.kpi.refreshed'), life: 3000 });
            fetchKpis();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const statusTemplate = (rowData) => (
        <Tag severity={rowData.isActive !== false ? 'success' : 'danger'}
             value={rowData.isActive !== false ? t('legacyUi.kpi.active') : t('legacyUi.kpi.inactive')} />
    );

    const acoesTemplate = (rowData) => (
        <div className="flex gap-2">
            <Button icon="pi pi-calculator" size="small" text rounded severity="info"
                    tooltip={t('legacyUi.kpi.calculate')} onClick={() => calcular(rowData)} />
            <Button icon="pi pi-pencil" size="small" text rounded onClick={() => abrirEdicao(rowData)} />
            <Button icon="pi pi-trash" size="small" text rounded severity="danger"
                    onClick={() => excluir(rowData)} />
        </div>
    );

    const header = (
        <div className="flex justify-content-between align-items-center flex-wrap gap-2">
            <span className="font-bold text-lg">Indicadores de Desempenho (KPIs)</span>
            <div className="flex gap-2">
                <Button label={t('legacyUi.kpi.refresh')} icon="pi pi-refresh" severity="secondary" outlined
                        loading={loading} onClick={refreshTodos} />
                <Button label={t('legacyUi.kpi.new')} icon="pi pi-plus" onClick={abrirNovo} />
            </div>
        </div>
    );

    const dialogFooter = (
        <div className="flex justify-content-end gap-2">
            <Button label="Cancelar" icon="pi pi-times" text onClick={() => setDialogVisible(false)} />
            <Button label="Salvar" icon="pi pi-check" onClick={salvar} autoFocus />
        </div>
    );

    return (
        <div className="kpi-container">
            <Toast ref={toast} />
            <ConfirmDialog />
            <Card header={header}>
                <DataTable
                    value={kpis}
                    loading={loading}
                    paginator
                    rows={10}
                    dataKey="id"
                    emptyMessage={t('legacyUi.kpi.empty')}
                    size="small"
                    stripedRows
                >
                    <Column field="name" header="Nome" sortable />
                    <Column field="kpiType" header="Tipo" body={(r) => <Tag value={r.kpiType} severity="info" />} sortable />
                    <Column field="unit" header="Unidade" />
                    <Column field="targetValue" header="Meta" body={(r) => (r.targetValue != null ? `${r.targetValue}${r.unit ? ' ' + r.unit : ''}` : '-')} />
                    <Column field="currentValue" header="Atual" body={(r) => (r.currentValue != null ? r.currentValue : '-')} />
                    <Column field="isActive" header="Status" body={statusTemplate} />
                    <Column body={acoesTemplate} header="Ações" style={{ width: '140px' }} />
                </DataTable>
            </Card>

            <Dialog visible={dialogVisible} style={{ width: '560px' }}
                    header={form.id ? 'Editar KPI' : t('legacyUi.kpi.new')}
                    modal dismissableMask onHide={() => setDialogVisible(false)} footer={dialogFooter}>
                <div className="grid p-fluid">
                    <div className="col-12 field">
                        <label htmlFor="kpi-name">Nome *</label>
                        <InputText id="kpi-name" value={form.name}
                                   onChange={(e) => setForm({ ...form, name: e.target.value })} />
                    </div>
                    <div className="col-12 field">
                        <label htmlFor="kpi-desc">Descrição</label>
                        <InputTextarea id="kpi-desc" rows={2} value={form.description}
                                       onChange={(e) => setForm({ ...form, description: e.target.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="kpi-tipo">Tipo</label>
                        <Dropdown id="kpi-tipo" value={form.kpiType} options={tipoOptions}
                                  onChange={(e) => setForm({ ...form, kpiType: e.value })} />
                    </div>
                    <div className="col-3 field">
                        <label htmlFor="kpi-unit">Unidade</label>
                        <InputText id="kpi-unit" value={form.unit}
                                   onChange={(e) => setForm({ ...form, unit: e.target.value })} />
                    </div>
                    <div className="col-3 field">
                        <label htmlFor="kpi-target">Meta</label>
                        <InputNumber id="kpi-target" value={form.targetValue} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, targetValue: e.value })} />
                    </div>
                    <div className="col-12 field">
                        <label htmlFor="kpi-query">Fórmula / Consulta</label>
                        <InputTextarea id="kpi-query" rows={3} value={form.queryFormula}
                                       placeholder="Expressão ou consulta usada para calcular o KPI"
                                       onChange={(e) => setForm({ ...form, queryFormula: e.target.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="kpi-thgood">Limiar bom</label>
                        <InputNumber id="kpi-thgood" value={form.thresholdGood} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, thresholdGood: e.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="kpi-thwarn">Limiar de alerta</label>
                        <InputNumber id="kpi-thwarn" value={form.thresholdWarning} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, thresholdWarning: e.value })} />
                    </div>
                    <div className="col-12 field flex align-items-center gap-3">
                        <label htmlFor="kpi-active">Ativo</label>
                        <input id="kpi-active" type="checkbox" checked={form.isActive}
                               onChange={(e) => setForm({ ...form, isActive: e.target.checked })} />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Kpis;
