import { useTranslation } from 'react-i18next';
import React, { useEffect, useState, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'primereact/dropdown';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import RomaneioProducaoService from '../../services/RomaneioProducaoService';

const novoItem = () => ({
    produtoId: null,
    descricao: '',
    quantidade: 0,
    unidadeMedida: 'UN',
    lote: '',
    observacoes: ''
});

export const RomaneioProducao = ({ empresaId, ordens = [] }) => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [romaneios, setRomaneios] = useState([]);
    const [loading, setLoading] = useState(false);
    const [visible, setVisible] = useState(false);
    const [error, setError] = useState('');
    const [form, setForm] = useState({
        numero: '',
        producaoId: null,
        dataRomaneio: new Date(),
        destino: '',
        responsavelId: null,
        veiculoId: null,
        status: 'ABERTO',
        observacoes: '',
        itens: [novoItem()]
    });

    useEffect(() => {
        if (empresaId) carregar();
    }, [empresaId]);

    const carregar = async () => {
        setLoading(true);
        try {
            setRomaneios(await RomaneioProducaoService.listar(empresaId));
        } catch (e) {
            setError(e.message);
        } finally {
            setLoading(false);
        }
    };

    const abrirNovo = () => {
        setError('');
        setForm({
            numero: '',
            producaoId: null,
            dataRomaneio: new Date(),
            destino: '',
            responsavelId: null,
            veiculoId: null,
            status: 'ABERTO',
            observacoes: '',
            itens: [novoItem()]
        });
        setVisible(true);
    };

    const atualizarItem = (index, campo, valor) => {
        setForm(prev => ({
            ...prev,
            itens: prev.itens.map((item, i) => i === index ? { ...item, [campo]: valor } : item)
        }));
    };

    const adicionarItem = () => setForm(prev => ({ ...prev, itens: [...prev.itens, novoItem()] }));

    const removerItem = (index) => {
        setForm(prev => ({
            ...prev,
            itens: prev.itens.length === 1 ? prev.itens : prev.itens.filter((_, i) => i !== index)
        }));
    };

    const salvar = async () => {
        setLoading(true);
        setError('');
        try {
            const payload = {
                ...form,
                dataRomaneio: form.dataRomaneio
                    ? form.dataRomaneio.toISOString().slice(0, 10)
                    : null
            };
            await RomaneioProducaoService.criar(empresaId, payload);
            toast.current?.show({ severity: 'success', summary: 'Romaneio criado', life: 2500 });
            setVisible(false);
            await carregar();
        } catch (e) {
            setError(e.message);
        } finally {
            setLoading(false);
        }
    };

    const acao = async (id, path, okMsg) => {
        try {
            await RomaneioProducaoService.acao(id, path);
            toast.current?.show({ severity: 'success', summary: okMsg, life: 2500 });
            carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message || 'Operação não realizada', life: 4500 });
        }
    };
    const conferirRomaneio = (id) => acao(id, 'conferir', 'Romaneio conferido');
    const liberarRomaneio = (id) => acao(id, 'liberar', 'Romaneio liberado');
    const cancelarRomaneio = (id) => acao(id, 'cancelar', 'Romaneio cancelado');

    const statusTemplate = (row) => (
        <Tag value={row.status} severity={
            row.status === 'CANCELADO' ? 'danger' :
            row.status === 'LIBERADO' ? 'success' :
            row.status === 'CONFERIDO' ? 'warning' : 'info'
        } />
    );

    return (
        <div>
            <Toast ref={toast} />
        <Card title="Romaneios de Produção" className="prod-romaneio-card">
            <div className="flex justify-content-between align-items-center mb-3">
                <span className="text-muted">Ciclo: ABERTO → conferir → CONFERIDO → liberar → LIBERADO</span>
                <Button label={t('legacyUi.romaneio.new')} icon="pi pi-file-plus" onClick={abrirNovo} />
            </div>

            {error && !visible && <Message severity="error" text={error} className="w-full mb-3" />}

            <DataTable value={romaneios} loading={loading} paginator rows={8} responsiveLayout="scroll" emptyMessage={t('legacyUi.romaneio.empty')}>
                <Column field="numero" header={t('legacyUi.romaneio.number')} sortable />
                <Column field="producao.numero" header={t('legacyUi.romaneio.order')} sortable />
                <Column field="dataRomaneio" header={t('legacyUi.romaneio.date')} sortable />
                <Column field="destino" header={t('legacyUi.romaneio.destination')} />
                <Column field="status" header={t('legacyUi.romaneio.status')} body={statusTemplate} />
                <Column field="itens.length" header={t('legacyUi.romaneio.items')} />
                <Column header="Ações" body={(row) => (
                    <div className="flex gap-1">
                        <Button icon="pi pi-eye" className="p-button-rounded p-button-text p-button-info" tooltip="Conferir"
                            disabled={row.status !== 'ABERTO'} onClick={() => conferirRomaneio(row.id)} />
                        <Button icon="pi pi-check" className="p-button-rounded p-button-text p-button-success" tooltip="Liberar"
                            disabled={row.status !== 'CONFERIDO'} onClick={() => liberarRomaneio(row.id)} />
                        <Button icon="pi pi-times" className="p-button-rounded p-button-text p-button-danger" tooltip="Cancelar"
                            disabled={row.status !== 'ABERTO'} onClick={() => cancelarRomaneio(row.id)} />
                    </div>
                )} />
            </DataTable>

            <Dialog
                header="Novo Romaneio de Produção"
                visible={visible}
                style={{ width: '900px' }}
                modal
                onHide={() => setVisible(false)}
            >
                <div className="p-fluid">
                    <div className="grid">
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold">Número</label>
                            <InputText value={form.numero} onChange={e => setForm({ ...form, numero: e.target.value })} placeholder="ROM-2026-0001" />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold">Ordem de Produção</label>
                            <Dropdown
                                value={form.producaoId}
                                options={ordens}
                                optionLabel="numero"
                                optionValue="id"
                                placeholder="Selecione a OP"
                                onChange={e => setForm({ ...form, producaoId: e.value })}
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold">Data</label>
                            <Calendar value={form.dataRomaneio} onChange={e => setForm({ ...form, dataRomaneio: e.value })} dateFormat="dd/mm/yy" showIcon />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold">Destino</label>
                            <InputText value={form.destino} onChange={e => setForm({ ...form, destino: e.target.value })} placeholder={t('legacyUi.romaneio.destination')} />
                        </div>
                        <div className="col-12 md:col-3 field">
                            <label className="font-bold">Responsável (ID)</label>
                            <InputNumber value={form.responsavelId} onValueChange={e => setForm({ ...form, responsavelId: e.value })} />
                        </div>
                        <div className="col-12 md:col-3 field">
                            <label className="font-bold">Veículo (ID)</label>
                            <InputNumber value={form.veiculoId} onValueChange={e => setForm({ ...form, veiculoId: e.value })} />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold">Observações</label>
                            <InputText value={form.observacoes} onChange={e => setForm({ ...form, observacoes: e.target.value })} />
                        </div>
                    </div>

                    <h4>{t('legacyUi.romaneio.items')}</h4>
                    {form.itens.map((item, index) => (
                        <div className="grid align-items-end mb-2" key={index}>
                            <div className="col-12 md:col-2 field">
                                <label>Produto (ID)</label>
                                <InputNumber value={item.produtoId} onValueChange={e => atualizarItem(index, 'produtoId', e.value)} />
                            </div>
                            <div className="col-12 md:col-3 field">
                                <label>Descrição</label>
                                <InputText value={item.descricao} onChange={e => atualizarItem(index, 'descricao', e.target.value)} />
                            </div>
                            <div className="col-12 md:col-2 field">
                                <label>Quantidade</label>
                                <InputNumber value={item.quantidade} onValueChange={e => atualizarItem(index, 'quantidade', e.value)} min={0} />
                            </div>
                            <div className="col-12 md:col-2 field">
                                <label>Unidade</label>
                                <InputText value={item.unidadeMedida} onChange={e => atualizarItem(index, 'unidadeMedida', e.target.value)} />
                            </div>
                            <div className="col-12 md:col-2 field">
                                <label>Lote</label>
                                <InputText value={item.lote} onChange={e => atualizarItem(index, 'lote', e.target.value)} />
                            </div>
                            <div className="col-12 md:col-1 field">
                                <Button icon="pi pi-trash" severity="danger" text onClick={() => removerItem(index)} />
                            </div>
                        </div>
                    ))}

                    <div className="flex justify-content-between mt-3">
                        <Button label={t('legacyUi.romaneio.add')} icon="pi pi-plus" outlined onClick={adicionarItem} />
                        <div className="flex gap-2">
                            <Button label={t('legacyUi.romaneio.cancel')} text onClick={() => setVisible(false)} />
                            <Button label={t('legacyUi.romaneio.save')} icon="pi pi-save" loading={loading} onClick={salvar} />
                        </div>
                    </div>

                    {error && <Message severity="error" text={error} className="w-full mt-3" />}
                </div>
            </Dialog>
        </Card>
        </div>
    );
};

export default RomaneioProducao;
