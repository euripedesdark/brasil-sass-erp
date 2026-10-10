import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import ContaBancariaService from '../../services/ContaBancariaService';
import { useTranslation } from 'react-i18next';
import './ContaBancaria.css';

const TIPO_OPTS = [
    { label: 'Corrente', value: 'CORRENTE' },
    { label: 'Poupanca', value: 'POUPANCA' },
    { label: 'Investimento', value: 'INVESTIMENTO' },
    { label: 'Caixa', value: 'CAIXA' },
];

export const ContaBancaria = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [contas, setContas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [form, setForm] = useState(emptyForm());

    function emptyForm() {
        return {
            banco: '',
            agencia: '',
            conta: '',
            digito: '',
            tipo: 'CORRENTE',
            saldoInicial: 0,
            ativa: true,
        };
    }

    useEffect(() => {
        carregar();
    }, []);

    const carregar = async () => {
        setLoading(true);
        try {
            const res = await ContaBancariaService.listar();
            const data = res?.data?.data ?? res?.data ?? [];
            setContas(Array.isArray(data) ? data : []);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('bankAccount.loadError'),
                life: 3000,
            });
            setContas([]);
        } finally {
            setLoading(false);
        }
    };

    const salvar = async () => {
        setError('');
        if (!form.banco || !form.agencia || !form.conta || !form.tipo) {
            setError(t('bankAccount.required'));
            return;
        }
        setLoading(true);
        try {
            await ContaBancariaService.criar({
                banco: form.banco,
                agencia: form.agencia,
                conta: form.conta,
                digito: form.digito || null,
                tipo: form.tipo,
                saldoInicial: form.saldoInicial || 0,
                ativa: true,
            });
            toast.current?.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('bankAccount.created'),
                life: 2500,
            });
            setDialogVisible(false);
            setForm(emptyForm());
            carregar();
        } catch (err) {
            const msg =
                err?.response?.data?.message ||
                err?.response?.data?.error ||
                'Erro ao salvar conta';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    const inativar = async (id) => {
        try {
            await ContaBancariaService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: t('bankAccount.deactivated'),
                life: 2500,
            });
            carregar();
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: err?.response?.data?.message || t('bankAccount.deactivateError'),
                life: 3000,
            });
        }
    };

    const moeda = (v) =>
        (v == null ? 0 : Number(v)).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

    return (
        <div>
            <Toast ref={toast} />
            <Card title={t('bankAccount.title')}>
                <div className="flex justify-content-between align-items-center mb-3">
                    <p className="m-0 text-color-secondary">
                        {t('bankAccount.description')}
                    </p>
                    <Button
                        label={t('bankAccount.new')}
                        icon="pi pi-plus"
                        onClick={() => {
                            setForm(emptyForm());
                            setError('');
                            setDialogVisible(true);
                        }}
                    />
                </div>

                <DataTable
                    value={contas}
                    loading={loading}
                    paginator
                    rows={10}
                    emptyMessage={t('bankAccount.empty')}
                    size="small"
                >
                    <Column field="id" header="ID" style={{ width: '70px' }} />
                    <Column field="banco" header={t('bankAccount.bank')} />
                    <Column field="agencia" header={t('bankAccount.branch')} style={{ width: '100px' }} />
                    <Column
                        header={t('bankAccount.account')}
                        body={(r) => `${r.conta}${r.digito ? '-' + r.digito : ''}`}
                        style={{ width: '120px' }}
                    />
                    <Column field="tipo" header={t('common.type')} style={{ width: '120px' }} />
                    <Column
                        field="saldoInicial"
                        header={t('bankAccount.initialBalance')}
                        body={(r) => moeda(r.saldoInicial)}
                        style={{ width: '130px' }}
                    />
                    <Column
                        field="ativa"
                        header={t('common.status')}
                        body={(r) => (
                            <Tag
                                value={r.ativa ? t('common.activeFeminine') : t('common.inactiveFeminine')}
                                severity={r.ativa ? 'success' : 'danger'}
                            />
                        )}
                        style={{ width: '100px' }}
                    />
                    <Column
                        body={(r) =>
                            r.ativa ? (
                                <Button
                                    icon="pi pi-ban"
                                    className="p-button-text p-button-danger p-button-sm"
                                    tooltip={t('bankAccount.deactivate')}
                                    onClick={() => inativar(r.id)}
                                />
                            ) : null
                        }
                        style={{ width: '70px' }}
                    />
                </DataTable>
            </Card>

            <Dialog
                header={t('bankAccount.newTitle')}
                visible={dialogVisible}
                style={{ width: '520px' }}
                onHide={() => setDialogVisible(false)}
                footer={
                    <div>
                        <Button
                            label={t('common.cancel')}
                            className="p-button-text"
                            onClick={() => setDialogVisible(false)}
                        />
                        <Button label={t('common.save')} icon="pi pi-save" onClick={salvar} loading={loading} />
                    </div>
                }
            >
                {error && <Message severity="error" text={error} className="w-full mb-3" />}
                <div className="p-fluid grid">
                    <div className="col-12 field">
                        <label className="font-bold">{t('bankAccount.bank')} *</label>
                        <InputText
                            value={form.banco}
                            onChange={(e) => setForm({ ...form, banco: e.target.value })}
                        />
                    </div>
                    <div className="col-4 field">
                        <label className="font-bold">{t('bankAccount.branch')} *</label>
                        <InputText
                            value={form.agencia}
                            onChange={(e) => setForm({ ...form, agencia: e.target.value })}
                        />
                    </div>
                    <div className="col-5 field">
                        <label className="font-bold">{t('bankAccount.account')} *</label>
                        <InputText
                            value={form.conta}
                            onChange={(e) => setForm({ ...form, conta: e.target.value })}
                        />
                    </div>
                    <div className="col-3 field">
                        <label>{t('bankAccount.digit')}</label>
                        <InputText
                            value={form.digito}
                            onChange={(e) => setForm({ ...form, digito: e.target.value })}
                        />
                    </div>
                    <div className="col-6 field">
                        <label className="font-bold">{t('common.type')} *</label>
                        <Dropdown
                            value={form.tipo}
                            options={TIPO_OPTS}
                            onChange={(e) => setForm({ ...form, tipo: e.value })}
                            optionLabel="label"
                            optionValue="value"
                        />
                    </div>
                    <div className="col-6 field">
                        <label>{t('bankAccount.initialBalance')}</label>
                        <InputNumber
                            value={form.saldoInicial}
                            onValueChange={(e) => setForm({ ...form, saldoInicial: e.value })}
                            mode="currency"
                            currency="BRL"
                            locale="pt-BR"
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default ContaBancaria;
