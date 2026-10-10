import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import ContaBancariaService from '../../services/ContaBancariaService';
import ExtratoService from '../../services/ExtratoService';
import './Extrato.css';
import { formatoData, localeAtivo } from '../shared/LocaleData.js';

const TIPO_OPTS = [
    { label: 'Entrada', value: 'ENTRADA' },
    { label: 'Saida', value: 'SAIDA' },
];

export const Extrato = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [contas, setContas] = useState([]);
    const [contaId, setContaId] = useState(null);
    const [extratos, setExtratos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const ofxRef = useRef(null);
    const importarOfx = async (ev) => {
        const arq = ev.target.files && ev.target.files[0];
        if (!arq || !contaId) return;
        const fd = new FormData();
        fd.append('arquivo', arq);
        try {
            const r = await apiFetch('/api/financeiro/extrato/importar-ofx?contaBancariaId=' + contaId, { method: 'POST', body: fd });
            const j = await r.json().catch(() => ({}));
            toast.current?.show({ severity: r.ok ? 'success' : 'error', summary: r.ok ? 'OFX importado' : 'Erro', detail: 'Novos: ' + (j.novos ?? 0) + ' - repetidos: ' + (j.repetidos ?? 0), life: 5000 });
            if (r.ok) carregarExtrato(contaId);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        if (ofxRef.current) ofxRef.current.value = '';
    };
    const [form, setForm] = useState({
        contaBancariaId: null,
        dataMovimento: new Date(),
        descricao: '',
        valor: 0,
        tipo: 'ENTRADA',
    });

    useEffect(() => {
        carregarContas();
    }, []);

    const carregarContas = async () => {
        try {
            const res = await ContaBancariaService.listar();
            const data = res?.data?.data ?? res?.data ?? [];
            const list = Array.isArray(data) ? data : [];
            setContas(list);
            if (list.length && !contaId) {
                setContaId(list[0].id);
                carregarExtrato(list[0].id);
            }
        } catch {
            setContas([]);
        }
    };

    const carregarExtrato = async (id) => {
        if (!id) return;
        setLoading(true);
        try {
            const res = await ExtratoService.porConta(id);
            const data = res?.data?.data ?? res?.data ?? [];
            setExtratos(Array.isArray(data) ? data : []);
        } catch {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Falha ao carregar extrato',
                life: 3000,
            });
            setExtratos([]);
        } finally {
            setLoading(false);
        }
    };

    const carregarPendentes = async () => {
        setLoading(true);
        setContaId(null);
        try {
            const res = await ExtratoService.pendentesConciliacao();
            const data = res?.data?.data ?? res?.data ?? [];
            setExtratos(Array.isArray(data) ? data : []);
        } catch {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Falha ao carregar pendentes',
                life: 3000,
            });
        } finally {
            setLoading(false);
        }
    };

    const salvar = async () => {
        setError('');
        if (!form.contaBancariaId || !form.descricao || !form.valor) {
            setError('Informe conta, descricao e valor');
            return;
        }
        setLoading(true);
        try {
            await ExtratoService.criar({
                contaBancariaId: form.contaBancariaId,
                dataMovimento: form.dataMovimento
                    ? new Date(form.dataMovimento).toISOString().slice(0, 10)
                    : null,
                descricao: form.descricao,
                valor: form.valor,
                tipo: form.tipo,
            });
            toast.current?.show({
                severity: 'success',
                summary: 'Lancamento criado',
                life: 2500,
            });
            setDialogVisible(false);
            if (form.contaBancariaId) {
                setContaId(form.contaBancariaId);
                carregarExtrato(form.contaBancariaId);
            }
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                    err?.response?.data?.error ||
                    'Erro ao criar lancamento'
            );
        } finally {
            setLoading(false);
        }
    };

    const moeda = (v) =>
        (v == null ? 0 : Number(v)).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });

    const dataBr = (d) => (d ? new Date(d).toLocaleDateString(localeAtivo()) : '');

    const contaOpts = contas.map((c) => ({
        label: `${c.banco} ${c.agencia}/${c.conta}${c.digito ? '-' + c.digito : ''}`,
        value: c.id,
    }));

    return (
        <div>
            <Toast ref={toast} />
            <Card title="Extrato Bancario">
                <p className="text-color-secondary mb-3">
                    Movimentos por conta. Use na conciliacao e ao baixar titulos com conta informada.
                </p>

                <div className="flex flex-wrap gap-2 align-items-end mb-3">
                    <div className="field m-0">
                        <label className="font-bold block mb-1">Conta</label>
                        <Dropdown
                            value={contaId}
                            options={contaOpts}
                            onChange={(e) => {
                                setContaId(e.value);
                                carregarExtrato(e.value);
                            }}
                            placeholder="Selecione"
                            style={{ minWidth: '220px' }}
                        />
                    </div>
                    <Button
                        label="Carregar"
                        icon="pi pi-refresh"
                        onClick={() => carregarExtrato(contaId)}
                        disabled={!contaId}
                    />
                    <Button
                        label="Pendentes conciliacao"
                        icon="pi pi-clock"
                        className="p-button-outlined"
                        onClick={carregarPendentes}
                    />
                    <Button
                        label="Novo lancamento"
                        icon="pi pi-plus"
                        onClick={() => {
                            setForm({
                                contaBancariaId: contaId,
                                dataMovimento: new Date(),
                                descricao: '',
                                valor: 0,
                                tipo: 'ENTRADA',
                            });
                            setError('');
                            setDialogVisible(true);
                        }}
                    />
                    <input ref={ofxRef} type='file' accept='.ofx,.ofc' style={{ display: 'none' }} onChange={importarOfx} />
                    <Button
                        label='Importar OFX'
                        icon='pi pi-upload'
                        outlined
                        disabled={!contaId}
                        onClick={() => ofxRef.current && ofxRef.current.click()}
                    />
                </div>

                <DataTable
                    value={extratos}
                    loading={loading}
                    paginator
                    rows={12}
                    emptyMessage="Nenhum lancamento"
                    size="small"
                >
                    <Column
                        field="dataMovimento"
                        header="Data"
                        body={(r) => dataBr(r.dataMovimento)}
                        style={{ width: '110px' }}
                    />
                    <Column field="contaBancariaId" header="Conta" style={{ width: '90px' }} />
                    <Column field="descricao" header="Descricao" />
                    <Column
                        field="tipo"
                        header="Tipo"
                        body={(r) => (
                            <Tag
                                value={r.tipo}
                                severity={r.tipo === 'ENTRADA' ? 'success' : 'danger'}
                            />
                        )}
                        style={{ width: '100px' }}
                    />
                    <Column
                        field="valor"
                        header="Valor"
                        body={(r) => moeda(r.valor)}
                        style={{ width: '120px' }}
                    />
                    <Column
                        field="saldoAtual"
                        header="Saldo"
                        body={(r) => moeda(r.saldoAtual)}
                        style={{ width: '120px' }}
                    />
                    <Column
                        field="conciliado"
                        header="Conciliado"
                        body={(r) => (
                            <Tag
                                value={r.conciliado ? 'Sim' : 'Nao'}
                                severity={r.conciliado ? 'success' : 'warning'}
                            />
                        )}
                        style={{ width: '110px' }}
                    />
                </DataTable>
            </Card>

            <Dialog
                header="Novo lancamento de extrato"
                visible={dialogVisible}
                style={{ width: '480px' }}
                onHide={() => setDialogVisible(false)}
                footer={
                    <div>
                        <Button
                            label="Cancelar"
                            className="p-button-text"
                            onClick={() => setDialogVisible(false)}
                        />
                        <Button label="Salvar" icon="pi pi-save" onClick={salvar} loading={loading} />
                    </div>
                }
            >
                {error && <Message severity="error" text={error} className="w-full mb-3" />}
                <div className="p-fluid grid">
                    <div className="col-12 field">
                        <label className="font-bold">Conta *</label>
                        <Dropdown
                            value={form.contaBancariaId}
                            options={contaOpts}
                            onChange={(e) => setForm({ ...form, contaBancariaId: e.value })}
                            placeholder="Selecione"
                        />
                    </div>
                    <div className="col-12 field">
                        <label className="font-bold">Data</label>
                        <Calendar
                            value={form.dataMovimento}
                            onChange={(e) => setForm({ ...form, dataMovimento: e.value })}
                            dateFormat={formatoData()}
                            showIcon
                        />
                    </div>
                    <div className="col-12 field">
                        <label className="font-bold">Descricao *</label>
                        <InputText
                            value={form.descricao}
                            onChange={(e) => setForm({ ...form, descricao: e.target.value })}
                        />
                    </div>
                    <div className="col-6 field">
                        <label className="font-bold">Valor *</label>
                        <InputNumber
                            value={form.valor}
                            onValueChange={(e) => setForm({ ...form, valor: e.value })}
                            mode="currency"
                            currency="BRL"
                            locale="pt-BR"
                        />
                    </div>
                    <div className="col-6 field">
                        <label className="font-bold">Tipo</label>
                        <Dropdown
                            value={form.tipo}
                            options={TIPO_OPTS}
                            onChange={(e) => setForm({ ...form, tipo: e.value })}
                            optionLabel="label"
                            optionValue="value"
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Extrato;
