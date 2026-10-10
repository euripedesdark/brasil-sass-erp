import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Checkbox } from 'primereact/checkbox';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { localeAtivo } from '../shared/LocaleData.js';

const BASE = '/api/financeiro/comissoes';

const erroDe = (e, padrao) => {
    const d = e?.payload;
    if (d?.errors?.[0]?.message) return d.errors[0].message;
    if (d?.message) return d.message;
    return padrao;
};

const vazio = () => ({
    nome: '', vendedorId: null, percentual: 0, faixaValorMin: 0, faixaValorMax: null,
    metaValor: null, baseCalculo: 'VALOR_LIQUIDO', ativo: true,
    vigenciaInicio: '', vigenciaFim: ''
});

/**
 * Regras de comissão.
 *
 * Sem nenhuma regra cadastrada, PedidoVendaServiceImpl cai no
 * orElse(BigDecimal.ZERO) e grava a comissão como 0,00% — sem erro e sem
 * aviso. Esta é a única tela onde a regra pode ser cadastrada, então é ela
 * que impede o problema: o backend já lê a regra, mas não há como criá-la
 * por outro caminho.
 *
 * A ordem importa na leitura: quando duas faixas se sobrepõem, vale a de
 * maior base (o repository ordena por faixa_valor_min). A tela mostra isso
 * na coluna de faixa, e a ordenação da tabela é a mesma do backend.
 */
export const RegrasComissao = () => {
    const { t } = useTranslation();
    const BASES = [
        { label: t('commissionRules.netValue'), value: 'VALOR_LIQUIDO' },
        { label: t('commissionRules.grossValue'), value: 'VALOR_BRUTO' }
    ];
    const toast = useRef(null);

    const [regras, setRegras] = useState([]);
    const [vendedores, setVendedores] = useState([]);
    const [loading, setLoading] = useState(true);
    const [salvando, setSalvando] = useState(false);
    const [dialog, setDialog] = useState(false);
    const [editando, setEditando] = useState(null);
    const [form, setForm] = useState(vazio());
    const [erro, setErro] = useState('');

    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [r, v] = await Promise.all([
                apiFetch(`${BASE}/regras`),
                apiFetch('/api/rh/funcionarios')
            ]);
            setRegras(r.ok ? ((await r.json())?.data ?? []) : []);
            if (v.ok) {
                const lista = (await v.json())?.data ?? [];
                setVendedores((Array.isArray(lista) ? lista : []).map((f) => ({
                    label: f.nome || f.nomePessoa || `#${f.id}`,
                    value: f.id
                })));
            }
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: t('common.error'),
                detail: erroDe(e, t('commissionRules.loadError')), life: 5000
            });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    const abrir = (regra) => {
        setErro('');
        if (regra) {
            setEditando(regra);
            setForm({
                ...vazio(), ...regra,
                vigenciaInicio: regra.vigenciaInicio ? String(regra.vigenciaInicio).slice(0, 10) : '',
                vigenciaFim: regra.vigenciaFim ? String(regra.vigenciaFim).slice(0, 10) : ''
            });
        } else {
            setEditando(null);
            setForm(vazio());
        }
        setDialog(true);
    };

    const salvar = async () => {
        if (!form.nome?.trim()) { setErro(t('commissionRules.nameRequired')); return; }
        if (!(Number(form.percentual) > 0)) { setErro(t('commissionRules.percentageRequired')); return; }
        setErro('');
        setSalvando(true);
        try {
            const corpo = {
                ...form,
                // data vazia viraria "Invalid Date" no backend
                vigenciaInicio: form.vigenciaInicio || null,
                vigenciaFim: form.vigenciaFim || null
            };
            const url = editando ? `${BASE}/regras/${editando.id}` : `${BASE}/regras`;
            const r = await apiFetch(url, {
                method: editando ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpo)
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || t('commissionRules.saveError'));
            }
            toast.current?.show({
                severity: 'success', summary: editando ? t('commissionRules.updated') : t('commissionRules.created'),
                detail: t('commissionRules.nextOrders'), life: 4000
            });
            setDialog(false);
            carregar();
        } catch (e) {
            setErro(e.message || t('commissionRules.saveError'));
        } finally {
            setSalvando(false);
        }
    };

    const excluir = (regra) => {
        confirmDialog.require({
            header: t('commissionRules.deleteTitle'),
            message: `Excluir a regra "${regra.nome}"? Os pedidos já faturados mantêm a comissão que gravaram; a regra passa a valer só para os próximos.`,
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('commissionRules.confirmDelete'),
            rejectLabel: t('common.cancel'),
            accept: async () => {
                try {
                    const r = await apiFetch(`${BASE}/regras/${regra.id}`, { method: 'DELETE' });
                    if (!r.ok && r.status !== 404) throw new Error('Falhou');
                    toast.current?.show({
                        severity: 'success', summary: t('commissionRules.deleted'), life: 3000
                    });
                    carregar();
                } catch {
                    toast.current?.show({
                        severity: 'error', summary: t('common.error'),
                        detail: t('commissionRules.deleteError'), life: 5000
                    });
                }
            }
        });
    };

    const money = (v) => `R$ ${Number(v ?? 0).toLocaleString(localeAtivo(), { minimumFractionDigits: 2 })}`;
    const campo = (id, rotulo, children, w) => (
        <div className={w || 'col-12 md:col-6'}>
            <label className="bc-label" htmlFor={id}>{rotulo}</label>
            {children}
        </div>
    );

    return (
        <div>
            <Toast ref={toast} />
            <ConfirmDialog />

            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <span className="bc-muted">
                    {t('commissionRules.description')}
                </span>
                <Button label={t('commissionRules.new')} icon="pi pi-plus" onClick={() => abrir(null)} />
            </div>

            <DataTable
                value={regras}
                loading={loading}
                dataKey="id"
                paginator rows={10}
                emptyMessage={t('commissionRules.empty')}
                responsiveLayout="scroll"
            >
                <Column field="nome" header={t('commissionRules.rule')} sortable />
                <Column header={t('commissionRules.salesperson')} body={(r) =>
                    vendedores.find((v) => v.value === r.vendedorId)?.label || t('commissionRules.general')} />
                <Column header={t('commissionRules.salesRange')} body={(r) => (
                    <span>
                        {money(r.faixaValorMin)}
                        {r.faixaValorMax != null ? ` a ${money(r.faixaValorMax)}` : ' em diante'}
                    </span>
                )} />
                <Column header={t('commissionRules.target')} body={(r) => (r.metaValor != null ? money(r.metaValor) : '—')} />
                <Column header={t('commissionRules.percentage')} body={(r) => (
                    <Tag value={`${Number(r.percentual ?? 0).toFixed(2)}%`} severity="info" />
                )} />
                <Column header={t('commissionRules.validity')} body={(r) => {
                    const ini = r.vigenciaInicio ? String(r.vigenciaInicio).slice(0, 10) : null;
                    const fim = r.vigenciaFim ? String(r.vigenciaFim).slice(0, 10) : null;
                    return <span>{ini || t('commissionRules.noStart')} → {fim || t('commissionRules.noEnd')}</span>;
                }} />
                <Column header={t('commissionRules.status')} body={(r) => (
                    <Tag value={r.ativo ? t('commissionRules.active') : t('commissionRules.inactive')} severity={r.ativo ? 'success' : 'secondary'} />
                )} style={{ width: '9rem' }} />
                <Column header="" body={(r) => (
                    <div className="flex gap-1">
                        <Button icon="pi pi-pencil" rounded text tooltip={t('commissionRules.edit')} onClick={() => abrir(r)} />
                        <Button icon="pi pi-trash" rounded text severity="danger" tooltip={t('commissionRules.delete')}
                                onClick={() => excluir(r)} />
                    </div>
                )} style={{ width: '6rem' }} />
            </DataTable>

            <Dialog
                visible={dialog}
                onHide={() => setDialog(false)}
                header={editando ? `${t('commissionRules.edit')}: ${editando.nome}` : t('commissionRules.newTitle')}
                modal
                style={{ width: 'min(96vw, 640px)' }}
                breakpoints={{ '960px': '95vw' }}
                footer={
                    <div className="flex justify-end gap-2">
                        <Button label={t('commissionRules.cancel')} severity="secondary" text onClick={() => setDialog(false)} />
                        <Button label={t('commissionRules.save')} icon="pi pi-check" loading={salvando}
                                disabled={salvando} onClick={salvar} />
                    </div>
                }
            >
                <div className="grid p-fluid">
                    {erro && <div className="col-12"><Message severity="error" text={erro} /></div>}

                    {campo('nome', t('commissionRules.name'),
                        <InputText id="nome" value={form.nome}
                                   onChange={(e) => setForm({ ...form, nome: e.target.value })} />, 'col-12')}

                    {campo('vend', 'Vendedor',
                        <Dropdown id="vend" value={form.vendedorId} options={vendedores}
                                  placeholder={t('commissionRules.salespersonPlaceholder')}
                                  showClear
                                  onChange={(e) => setForm({ ...form, vendedorId: e.value ?? null })} />)}

                    {campo('perc', t('commissionRules.percentage'),
                        <InputNumber id="perc" value={form.percentual} mode="decimal"
                                     suffix=" %" minFractionDigits={2} maxFractionDigits={2}
                                     onValueChange={(e) => setForm({ ...form, percentual: e.value ?? 0 })} />)}

                    {campo('base', t('commissionRules.calculationBase'),
                        <Dropdown id="base" value={form.baseCalculo} options={BASES}
                                  onChange={(e) => setForm({ ...form, baseCalculo: e.value })} />)}

                    {campo('min', t('commissionRules.minSales'),
                        <InputNumber id="min" value={form.faixaValorMin} mode="currency" currency="BRL" locale="pt-BR"
                                     onValueChange={(e) => setForm({ ...form, faixaValorMin: e.value ?? 0 })} />)}

                    {campo('max', t('commissionRules.maxSales'),
                        <InputNumber id="max" value={form.faixaValorMax} mode="currency" currency="BRL" locale="pt-BR"
                                     placeholder={t('commissionRules.noCeiling')}
                                     onValueChange={(e) => setForm({ ...form, faixaValorMax: e.value ?? null })} />)}

                    {campo('meta', t('commissionRules.accumulatedTarget'),
                        <InputNumber id="meta" value={form.metaValor} mode="currency" currency="BRL" locale="pt-BR"
                                     placeholder={t('commissionRules.optional')}
                                     onValueChange={(e) => setForm({ ...form, metaValor: e.value ?? null })} />)}

                    {campo('vinicio', t('commissionRules.validityStart'),
                        <InputText id="vinicio" type="date" value={form.vigenciaInicio}
                                   onChange={(e) => setForm({ ...form, vigenciaInicio: e.target.value })} />)}

                    {campo('vfim', t('commissionRules.validityEnd'),
                        <InputText id="vfim" type="date" value={form.vigenciaFim}
                                   onChange={(e) => setForm({ ...form, vigenciaFim: e.target.value })} />)}

                    <div className="col-12">
                        <div className="flex align-items-center gap-2">
                            <Checkbox inputId="ativo" checked={form.ativo}
                                      onChange={(e) => setForm({ ...form, ativo: !!e.checked })} />
                            <label htmlFor="ativo" className="bc-label m-0">{t('commissionRules.activeRule')}</label>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default RegrasComissao;
