import React, { useState, useEffect, useRef, useCallback } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { formatoData } from '../shared/LocaleData.js';

const fmt = (v) => Number(v ?? 0).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });
const BASE = '/api/rh/rescisao';

const MOTIVOS = [
    { label: 'Sem justa causa', value: 'SEM_JUSTA_CAUSA' },
    { label: 'Com justa causa', value: 'COM_JUSTA_CAUSA' },
    { label: 'Pedido de demissão', value: 'PEDIDO_DEMISSAO' },
    { label: 'Término de contrato', value: 'TERMINO_CONTRATO' },
];

export const Rescisao = () => {
    const toast = useRef(null);
    const [funcs, setFuncs] = useState([]);
    const [funcId, setFuncId] = useState(null);
    const [data, setData] = useState(new Date());
    const [motivo, setMotivo] = useState('SEM_JUSTA_CAUSA');
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);
    const [efetivando, setEfetivando] = useState(false);
    const [hist, setHist] = useState([]);
    const [loadingHist, setLoadingHist] = useState(false);

    const js = async (r) => {
        const j = await r.json().catch(() => null);
        return Array.isArray(j) ? j : (j?.data ?? j?.content ?? j ?? []);
    };

    useEffect(() => {
        apiFetch('/api/rh/funcionarios')
            .then(js)
            .then((l) => setFuncs((Array.isArray(l) ? l : []).filter((f) => f.ativo !== false && !f.dataDemissao)))
            .catch(() => {});
    }, []);

    const carregarHist = useCallback(async () => {
        setLoadingHist(true);
        try {
            const r = await apiFetch(BASE);
            setHist(await js(r));
        } catch {
            setHist([]);
        } finally {
            setLoadingHist(false);
        }
    }, []);

    useEffect(() => { carregarHist(); }, [carregarHist]);

    const calcular = async () => {
        if (!funcId) {
            toast.current?.show({ severity: 'warn', summary: 'Selecione o funcionário', life: 3000 });
            return;
        }
        setLoading(true);
        try {
            const iso = data ? data.toISOString().slice(0, 10) : new Date().toISOString().slice(0, 10);
            const r = await apiFetch(`${BASE}/calcular?funcionarioId=${funcId}&desligamento=${iso}&motivo=${encodeURIComponent(motivo)}`);
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.message || 'Falha no cálculo');
            }
            setRes(await r.json());
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
            setRes(null);
        } finally {
            setLoading(false);
        }
    };

    const efetivar = () => {
        if (!funcId || !res) return;
        confirmDialog({
            header: 'Efetivar rescisão',
            message: 'Confirma o desligamento? O funcionário será inativado e a rescisão gravada.',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Efetivar',
            rejectLabel: 'Cancelar',
            accept: async () => {
                setEfetivando(true);
                try {
                    const iso = data ? data.toISOString().slice(0, 10) : new Date().toISOString().slice(0, 10);
                    const r = await apiFetch(`${BASE}/efetivar`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ funcionarioId: funcId, desligamento: iso, motivo }),
                    });
                    if (!r.ok) {
                        const j = await r.json().catch(() => null);
                        throw new Error(j?.message || 'Falha ao efetivar');
                    }
                    toast.current?.show({ severity: 'success', summary: 'Rescisão efetivada', life: 3500 });
                    setRes(null);
                    setFuncs((prev) => prev.filter((f) => f.id !== funcId));
                    setFuncId(null);
                    carregarHist();
                } catch (e) {
                    toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4500 });
                } finally {
                    setEfetivando(false);
                }
            },
        });
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <ConfirmDialog />
            <div className="mb-3">
                <h2 className="m-0">Rescisão</h2>
                <span className="bc-muted">Cálculo e efetivação do desligamento</span>
            </div>

            <TabView>
                <TabPanel header="Calcular / Efetivar">
                    <div className="flex gap-2 flex-wrap align-items-end mb-3">
                        <span>
                            <label className="bc-label">Funcionário</label>
                            <Dropdown
                                value={funcId}
                                options={funcs.map((f) => ({
                                    label: `${f.matricula || f.id} — ${f.pessoaNome || f.nome || 'Colaborador'}`,
                                    value: f.id,
                                }))}
                                onChange={(e) => { setFuncId(e.value); setRes(null); }}
                                placeholder="Selecione"
                                filter
                                style={{ minWidth: '18rem' }}
                            />
                        </span>
                        <span>
                            <label className="bc-label">Desligamento</label>
                            <Calendar value={data} onChange={(e) => setData(e.value)} dateFormat={formatoData()} showIcon />
                        </span>
                        <span>
                            <label className="bc-label">Motivo</label>
                            <Dropdown value={motivo} options={MOTIVOS} onChange={(e) => setMotivo(e.value)} style={{ minWidth: '14rem' }} />
                        </span>
                        <Button label="Calcular" icon="pi pi-calculator" onClick={calcular} loading={loading} />
                    </div>

                    {res && (
                        <>
                            <div className="grid mb-3">
                                {[
                                    ['Saldo salário', res.saldoSalario],
                                    ['13º proporcional', res.decimoTerceiro],
                                    ['Férias', res.ferias],
                                    ['1/3 férias', res.tercoFerias],
                                    ['Aviso prévio', res.avisoPrevio],
                                    ['Multa 40% FGTS', res.multa40],
                                ].map(([l, v]) => (
                                    <div key={l} className="col-12 md:col-4">
                                        <Card>
                                            <small>{l}</small>
                                            <div className="text-xl font-bold">{fmt(v)}</div>
                                        </Card>
                                    </div>
                                ))}
                                <div className="col-12 md:col-4">
                                    <Card>
                                        <small>Total estimado</small>
                                        <div className="text-2xl font-bold text-primary">{fmt(res.total)}</div>
                                        <small className="bc-muted">{res.mesesTrabalhados} mês(es) · {res.motivo}</small>
                                    </Card>
                                </div>
                            </div>
                            <Button
                                label="Efetivar rescisão"
                                icon="pi pi-check"
                                severity="danger"
                                loading={efetivando}
                                onClick={efetivar}
                            />
                        </>
                    )}
                </TabPanel>

                <TabPanel header="Histórico">
                    <DataTable value={hist} loading={loadingHist} paginator rows={10} emptyMessage="Nenhuma rescisão registrada" dataKey="id">
                        <Column field="id" header="#" style={{ width: '5rem' }} />
                        <Column field="funcionarioId" header="Funcionário" body={(r) => `#${r.funcionarioId}`} />
                        <Column field="dataDesligamento" header="Desligamento" />
                        <Column field="motivo" header="Motivo" />
                        <Column field="total" header="Total" body={(r) => fmt(r.total)} />
                        <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity="success" />} />
                    </DataTable>
                </TabPanel>
            </TabView>
        </div>
    );
};

export default Rescisao;
