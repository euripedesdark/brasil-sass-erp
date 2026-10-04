import { useTranslation } from 'react-i18next';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { apiFetch } from '../../services/ApiConfig';

const BASE = '/api/producao/apontamentos';

const STATUS = [
    { label: 'Iniciado', value: 'INICIADO' },
    { label: 'Em andamento', value: 'EM_ANDAMENTO' },
    { label: 'Finalizado', value: 'FINALIZADO' },
    { label: 'Cancelado', value: 'CANCELADO' }
];

const TURNOS = [
    { label: 'Manhã', value: 'MANHA' },
    { label: 'Tarde', value: 'TARDE' },
    { label: 'Noite', value: 'NOITE' }
];

const SEVERIDADE = {
    INICIADO: 'info',
    EM_ANDAMENTO: 'warning',
    FINALIZADO: 'success',
    CANCELADO: 'danger'
};

const num = (v) => Number(v ?? 0);
const money = (v) => Number(v ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const dataHora = (v) => (v ? new Date(v).toLocaleString('pt-BR') : '—');

const erroDe = (e, padrao) => {
    const d = e?.payload;
    if (d?.errors?.[0]?.message) return d.errors[0].message;
    if (d?.message) return d.message;
    return padrao;
};

const listaDe = (r) => {
    const j = r?.data ?? r;
    return Array.isArray(j) ? j : (j?.content ?? []);
};

const vazio = () => ({
    producaoId: null, itemProducaoId: null, funcionarioId: null,
    horasTrabalhadas: 0, quantidadeProduzida: 0, quantidadeRefugo: 0,
    observacoes: '', maquinaEquipamentoId: null, turno: 'MANHA', operacaoRoteiroId: null
});

/**
 * Apontamentos de produção.
 *
 * A tela existia e só tinha "Consultar por ID da produção", digitado à mão. As
 * seis rotas de escrita e de filtro do ApontamentoProducaoController — criar,
 * finalizar, por funcionário, por período, por status e as estatísticas — não
 * tinham por onde ser acessadas.
 *
 * A empresa não vai na URL: vem do token. A tela antiga mandava
 * `?empresaId=${user?.empresaId}` e, quando o usuário não tinha o campo
 * preenchido, mandava `?empresaId=` vazio.
 */
export const ApontamentosProducao = () => {
    const { t } = useTranslation();
    const toast = React.useRef(null);

    const [producoes, setProducoes] = useState([]);
    const [operacoes, setOperacoes] = useState([]);
    const carregarOperacoes = async (producaoId) => {
        setOperacoes([]);
        if (!producaoId) return;
        try {
            const prod = producoes.find((x) => x.id === producaoId);
            if (!prod || !prod.produtoFinalId) return;
            const rr = await apiFetch('/api/producao/roteiros?produtoId=' + prod.produtoFinalId);
            const rots = await rr.json().catch(() => []);
            if (!Array.isArray(rots) || !rots.length) return;
            const ro = await apiFetch('/api/producao/roteiros/' + rots[0].id + '/operacoes');
            const ops = await ro.json().catch(() => []);
            setOperacoes(Array.isArray(ops) ? ops : []);
        } catch (e) { setOperacoes([]); }
    };
    const [funcionarios, setFuncionarios] = useState([]);
    const [apontamentos, setApontamentos] = useState([]);
    const [stats, setStats] = useState(null);
    const [carregando, setCarregando] = useState(false);
    const [erro, setErro] = useState('');

    const [filtro, setFiltro] = useState({ tipo: 'producao', producaoId: null, funcionarioId: null, status: null, de: null, ate: null });

    const [dialogo, setDialogo] = useState(false);
    const [salvando, setSalvando] = useState(false);
    const [form, setForm] = useState(vazio());

    // as ordens de produção vêm do próprio módulo: melhor que digitar o id
    useEffect(() => {
        Promise.all([
            apiFetch('/api/producao'),
            apiFetch('/api/rh/funcionarios')
        ]).then(async ([rp, rf]) => {
            const p = rp.ok ? await rp.json() : [];
            const f = rf.ok ? await rf.json() : [];
            setProducoes(Array.isArray(p) ? p : []);
            setFuncionarios(Array.isArray(f) ? f : []);
        }).catch(() => {
            setProducoes([]);
            setFuncionarios([]);
        });
    }, []);

    const mostrar = (severity, summary, detail) =>
        toast.current?.show({ severity, summary, detail, life: 4500 });

    const aplicar = useCallback(async (f) => {
        setCarregando(true);
        setErro('');
        try {
            if (f.tipo === 'funcionario' && !f.funcionarioId) throw new Error('Escolha o funcionário');
            if (f.tipo === 'status' && !f.status) throw new Error('Escolha o status');
            if (f.tipo === 'periodo' && (!f.de || !f.ate)) throw new Error('Informe o início e o fim do período');

            let url;
            if (f.tipo === 'producao') {
                if (!f.producaoId) throw new Error('Escolha a ordem de produção');
                url = `${BASE}/por-producao/${f.producaoId}`;
            } else if (f.tipo === 'funcionario') {
                url = `${BASE}/por-funcionario/${f.funcionarioId}`;
            } else if (f.tipo === 'status') {
                url = `${BASE}/por-status?status=${encodeURIComponent(f.status)}`;
            } else {
                // o backend exige ISO completo: 2026-09-01T00:00:00
                const de = f.de.toISOString().slice(0, 19);
                const ate = f.ate.toISOString().slice(0, 19);
                url = `${BASE}/por-periodo?dataInicio=${encodeURIComponent(de)}&dataFim=${encodeURIComponent(ate)}`;
            }

            const r = await apiFetch(url);
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || t('legacyUi.apontamento.loadError'));
            }
            setApontamentos(listaDe(await r.json()));

            // as estatísticas são por ordem; só faz sentido junto do filtro de
            // produção, nas demais ficariam vazias sem explicar por quê
            if (f.tipo === 'producao') {
                const s = await apiFetch(`${BASE}/estatisticas/${f.producaoId}`);
                if (s.ok) setStats((await s.json())?.data ?? null);
            } else {
                setStats(null);
            }
        } catch (e) {
            setApontamentos([]);
            setStats(null);
            setErro(e.message || t('legacyUi.apontamento.loadError'));
        } finally {
            setCarregando(false);
        }
    }, []);

    const salvar = async () => {
        if (!form.producaoId) { setErro('Escolha a ordem de produção'); return; }
        setSalvando(true);
        setErro('');
        try {
            const corpo = {
                ...form,
                horasTrabalhadas: num(form.horasTrabalhadas),
                quantidadeProduzida: num(form.quantidadeProduzida),
                quantidadeRefugo: num(form.quantidadeRefugo),
                itemProducaoId: form.itemProducaoId || null,
                funcionarioId: form.funcionarioId || null,
                maquinaEquipamentoId: form.maquinaEquipamentoId || null
            };
            const r = await apiFetch(BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpo)
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || t('legacyUi.apontamento.saveError'));
            }
            mostrar('success', t('legacyUi.apontamento.saved'), t('legacyUi.apontamento.savedDetail'));
            setDialogo(false);
            setForm(vazio());
            aplicar(filtro);
        } catch (e) {
            setErro(erroDe(e, t('legacyUi.apontamento.saveError')));
        } finally {
            setSalvando(false);
        }
    };

    const finalizar = (row) => {
        confirmDialog.require({
            header: t('legacyUi.apontamento.finalize'),
            message: `Finalizar o apontamento #${row.id}? Depois disso ele não volta para em andamento.`,
            icon: 'pi pi-check-circle',
            acceptLabel: t('legacyUi.apontamento.yesFinalize'),
            rejectLabel: t('legacyUi.apontamento.cancel'),
            accept: async () => {
                try {
                    const r = await apiFetch(`${BASE}/${row.id}/finalizar`, { method: 'PUT' });
                    if (!r.ok) {
                        const j = await r.json().catch(() => null);
                        throw new Error(j?.errors?.[0]?.message || t('legacyUi.apontamento.finalizeError'));
                    }
                    mostrar('success', t('legacyUi.apontamento.finalized'));
                    aplicar(filtro);
                } catch (e) {
                    mostrar('error', 'Falha ao finalizar', erroDe(e, t('legacyUi.apontamento.finalizeError')));
                }
            }
        });
    };

    const totais = useMemo(() => ({
        horas: apontamentos.reduce((s, a) => s + num(a.horasTrabalhadas), 0),
        produzido: apontamentos.reduce((s, a) => s + num(a.quantidadeProduzida), 0),
        refugo: apontamentos.reduce((s, a) => s + num(a.quantidadeRefugo), 0)
    }), [apontamentos]);

    const producoesOpts = producoes.map((p) => ({ label: `${p.numero} — ${p.tipoProducao || ''}`.trim(), value: p.id }));
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
                    Horas trabalhadas, produzido e refugo por ordem de produção.
                </span>
                <Button label={t('legacyUi.apontamento.new')} icon="pi pi-plus"
                        onClick={() => { setErro(''); setForm({ ...vazio(), producaoId: filtro.producaoId }); setDialogo(true); }} />
            </div>

            <Card>
                <div className="grid p-fluid">
                    {campo('ftipo', 'Consultar por',
                        <Dropdown id="ftipo" value={filtro.tipo} options={[
                            { label: 'Ordem de produção', value: 'producao' },
                            { label: 'Funcionário', value: 'funcionario' },
                            { label: 'Status', value: 'status' },
                            { label: 'Período', value: 'periodo' }
                        ]} onChange={(e) => setFiltro({ ...filtro, tipo: e.value })} />, 'col-12 md:col-3')}

                    {filtro.tipo === 'producao' && campo('fprod', 'Ordem de produção',
                        <Dropdown id="fprod" value={filtro.producaoId} options={producoesOpts}
                                  placeholder="Escolha a ordem" filter
                                  onChange={(e) => setFiltro({ ...filtro, producaoId: e.value })} />, 'col-12 md:col-4')}

                    {filtro.tipo === 'funcionario' && campo('ffunc', 'Funcionário',
                        <Dropdown id="ffunc" value={filtro.funcionarioId}
                                  options={funcionarios.map(f => ({
                                      label: `${f.matricula || f.id} — ${f.pessoaNome || f.nome || f.cargo || 'Colaborador'}`,
                                      value: f.id
                                  }))}
                                  optionLabel="label" optionValue="value" filter
                                  placeholder="Escolha o funcionário"
                                  onChange={(e) => setFiltro({ ...filtro, funcionarioId: e.value })} />, 'col-12 md:col-4')}

                    {filtro.tipo === 'status' && campo('fstatus', 'Status',
                        <Dropdown id="fstatus" value={filtro.status} options={STATUS}
                                  onChange={(e) => setFiltro({ ...filtro, status: e.value })} />, 'col-12 md:col-4')}

                    {filtro.tipo === 'periodo' && campo('fde', 'De',
                        <Calendar id="fde" value={filtro.de} showTime hourFormat="24"
                                  onChange={(e) => setFiltro({ ...filtro, de: e.value })} />, 'col-12 md:col-4')}

                    {filtro.tipo === 'periodo' && campo('fate', 'Até',
                        <Calendar id="fate" value={filtro.ate} showTime hourFormat="24"
                                  onChange={(e) => setFiltro({ ...filtro, ate: e.value })} />, 'col-12 md:col-4')}

                    <div className="col-12 md:col-3 flex align-items-end">
                        <Button label="Consultar" icon="pi pi-search" loading={carregando}
                                disabled={carregando} onClick={() => aplicar(filtro)} />
                    </div>
                </div>

                {erro && <Message severity="error" text={erro} className="mt-3" />}

                {stats && (
                    <div className="grid mt-4">
                        {[
                            ['Apontamentos', stats.totalApontamentos ?? 0],
                            ['Horas trabalhadas', money(stats.totalHorasTrabalhadas)],
                            ['Quantidade produzida', money(stats.totalQuantidadeProduzida)],
                            ['Refugo', money(stats.totalQuantidadeRefugo)],
                            ['% de refugo', `${money(stats.percentualRefugo)}%`]
                        ].map(([rotulo, valor]) => (
                            <div className="col-12 md:col" key={rotulo}>
                                <div className="bc-muted small">{rotulo}</div>
                                <div className="text-xl font-medium">{valor}</div>
                            </div>
                        ))}
                    </div>
                )}

                {apontamentos.length > 0 && !stats && (
                    <div className="bc-muted small mt-3">
                        Totais da consulta: {money(totais.horas)} h · produzido {money(totais.produzido)} · refugo {money(totais.refugo)}
                    </div>
                )}

                <DataTable
                    value={apontamentos}
                    loading={carregando}
                    dataKey="id"
                    paginator
                    rows={20}
                    rowsPerPageOptions={[20, 50, 100]}
                    responsiveLayout="scroll"
                    emptyMessage="Nenhum apontamento encontrado"
                    className="mt-3 p-datatable-sm"
                >
                    <Column field="id" header="#" sortable style={{ width: '80px' }} />
                    <Column field="producaoId" header={t('legacyUi.apontamento.order')} sortable style={{ width: '110px' }} />
                    <Column field="funcionarioId" header={t('legacyUi.apontamento.employee')} sortable style={{ width: '120px' }}
                        body={(r) => (r.funcionarioId ? `#${r.funcionarioId}` : '—')} />
                    <Column field="dataApontamento" header={t('common.date')} sortable body={(r) => dataHora(r.dataApontamento)} />
                    <Column field="horasTrabalhadas" header={t('common.description')} sortable body={(r) => money(r.horasTrabalhadas)} />
                    <Column field="quantidadeProduzida" header="Produzido" sortable body={(r) => money(r.quantidadeProduzida)} />
                    <Column field="quantidadeRefugo" header="Refugo" sortable body={(r) => money(r.quantidadeRefugo)} />
                    <Column field="turno" header="Turno" sortable style={{ width: '110px' }} />
                    <Column header={t('legacyUi.apontamento.status')} sortable sortField="status" style={{ width: '160px' }}
                        body={(r) => <Tag value={r.status || '—'} severity={SEVERIDADE[r.status] || 'secondary'} />} />
                    <Column field="observacoes" header={t('common.notes')} />
                    <Column header="" style={{ width: '7rem' }} body={(r) => (
                        <div className="flex gap-1">
                            <Button icon="pi pi-check-circle" rounded text severity="success"
                                    tooltip="Finalizar"
                                    disabled={r.status === 'FINALIZADO' || r.status === 'CANCELADO'}
                                    onClick={() => finalizar(r)} />
                        </div>
                    )} />
                </DataTable>
            </Card>

            <Dialog
                visible={dialogo}
                onHide={() => setDialogo(false)}
                header="Novo apontamento"
                modal
                style={{ width: 'min(96vw, 640px)' }}
                breakpoints={{ '960px': '95vw' }}
                footer={
                    <div className="flex justify-end gap-2">
                        <Button label="Cancelar" severity="secondary" text onClick={() => setDialogo(false)} />
                        <Button label={t('common.save')} icon="pi pi-check" loading={salvando} disabled={salvando} onClick={salvar} />
                    </div>
                }
            >
                <div className="grid p-fluid">
                    {campo('aprod', 'Ordem de produção *',
                        <Dropdown id="aprod" value={form.producaoId} options={producoesOpts}
                                  placeholder="Escolha a ordem" filter
                                  onChange={(e) => { setForm({ ...form, producaoId: e.value, operacaoRoteiroId: null }); carregarOperacoes(e.value); }} />, 'col-12')}

                    
                    {operacoes.length > 0 && campo('aop', 'Operacao do roteiro',
                        <Dropdown id="aop" value={form.operacaoRoteiroId} options={operacoes.map((o) => ({ label: (o.sequencia ?? "") + ' - ' + (o.descricao || o.nome || ('Op ' + o.id)), value: o.id }))}
                                  placeholder="Escolha a operacao" showClear
                                  onChange={(e) => setForm({ ...form, operacaoRoteiroId: e.value ?? null })} />)}

{campo('afunc', 'Funcionário (id)',
                        <InputNumber id="afunc" value={form.funcionarioId} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, funcionarioId: e.value })} />)}

                    {campo('aturno', 'Turno',
                        <Dropdown id="aturno" value={form.turno} options={TURNOS}
                                  onChange={(e) => setForm({ ...form, turno: e.value })} />)}

                    {campo('ahoras', 'Horas trabalhadas *',
                        <InputNumber id="ahoras" value={form.horasTrabalhadas} minFractionDigits={2} maxFractionDigits={2}
                                     suffix=" h" onValueChange={(e) => setForm({ ...form, horasTrabalhadas: e.value ?? 0 })} />)}

                    {campo('aprodqtd', 'Quantidade produzida',
                        <InputNumber id="aprodqtd" value={form.quantidadeProduzida} minFractionDigits={0}
                                     onValueChange={(e) => setForm({ ...form, quantidadeProduzida: e.value ?? 0 })} />)}

                    {campo('arefugo', 'Quantidade de refugo',
                        <InputNumber id="arefugo" value={form.quantidadeRefugo} minFractionDigits={0}
                                     onValueChange={(e) => setForm({ ...form, quantidadeRefugo: e.value ?? 0 })} />)}

                    {campo('amaq', 'Máquina / equipamento (id)',
                        <InputNumber id="amaq" value={form.maquinaEquipamentoId} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, maquinaEquipamentoId: e.value })} />)}

                    {campo('aitem', 'Item da produção (id)',
                        <InputNumber id="aitem" value={form.itemProducaoId} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, itemProducaoId: e.value })} />, 'col-12')}

                    {campo('aobs', 'Observações',
                        <InputTextarea id="aobs" value={form.observacoes} rows={3} autoResize
                                       onChange={(e) => setForm({ ...form, observacoes: e.target.value })} />, 'col-12')}
                </div>
            </Dialog>
        </div>
    );
};

export default ApontamentosProducao;
