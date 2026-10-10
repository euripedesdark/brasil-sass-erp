import React, { useEffect, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Message } from 'primereact/message';
import { TabPanel, TabView } from 'primereact/tabview';
import { formatoData } from '../shared/LocaleData.js';

const brl = (v) => Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

/** CO-PA lite — rentabilidade por cliente e por mês (paridade SAP Controlling). */
export const Copa = () => {
    const [de, setDe] = useState(null);
    const [ate, setAte] = useState(null);
    const [cli, setCli] = useState(null);
    const [per, setPer] = useState(null);
    const [loading, setLoading] = useState(false);

    const carregar = async () => {
        setLoading(true);
        try {
            const qs = [];
            if (de) qs.push('de=' + de.toISOString().slice(0, 10));
            if (ate) qs.push('ate=' + ate.toISOString().slice(0, 10));
            const r1 = await apiFetch('/api/financeiro/copa/por-cliente' + (qs.length ? '?' + qs.join('&') : ''));
            const j1 = await r1.json();
            setCli(j1?.data ?? j1);

            const r2 = await apiFetch('/api/financeiro/copa/por-periodo?ano=' + new Date().getFullYear());
            const j2 = await r2.json();
            setPer(j2?.data ?? j2);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    return (
        <div className="p-3">
            <Card title="Análise de rentabilidade">
                <Message severity="info" className="w-full mb-3"
                    text="Custo estimado em 70% da receita até o CMV real do estoque." />
                <div className="flex flex-wrap gap-2 align-items-end mb-3">
                    <div>
                        <label className="bc-label">De</label>
                        <Calendar value={de} onChange={(e) => setDe(e.value)} dateFormat={formatoData()} showIcon />
                    </div>
                    <div>
                        <label className="bc-label">Até</label>
                        <Calendar value={ate} onChange={(e) => setAte(e.value)} dateFormat={formatoData()} showIcon />
                    </div>
                    <Button label="Atualizar" icon="pi pi-refresh" onClick={carregar} loading={loading} />
                </div>

                {cli && (
                    <div className="grid mb-3">
                        <div className="col-12 md:col-4"><strong>Receita:</strong> {brl(cli.totalReceita)}</div>
                        <div className="col-12 md:col-4"><strong>Custo est.:</strong> {brl(cli.totalCustoEstimado)}</div>
                        <div className="col-12 md:col-4"><strong>Margem:</strong> {brl(cli.totalMargem)}</div>
                    </div>
                )}

                <TabView>
                    <TabPanel header="Por cliente">
                        <DataTable value={cli?.clientes || []} loading={loading} paginator rows={15} size="small" emptyMessage="Sem vendas faturadas no período">
                            <Column field="clienteId" header="Cliente ID" />
                            <Column field="qtdePedidos" header="Pedidos" />
                            <Column field="receita" header="Receita" body={(r) => brl(r.receita)} />
                            <Column field="custoEstimado" header="Custo est." body={(r) => brl(r.custoEstimado)} />
                            <Column field="margem" header="Margem" body={(r) => brl(r.margem)} />
                            <Column field="margemPct" header="%" body={(r) => (r.margemPct != null ? r.margemPct + '%' : '—')} />
                        </DataTable>
                    </TabPanel>
                    <TabPanel header="Por mês">
                        <DataTable value={per?.meses || []} loading={loading} size="small" emptyMessage="Sem dados no ano">
                            <Column field="periodo" header="Período" />
                            <Column field="receita" header="Receita" body={(r) => brl(r.receita)} />
                            <Column field="custoEstimado" header="Custo est." body={(r) => brl(r.custoEstimado)} />
                            <Column field="margem" header="Margem" body={(r) => brl(r.margem)} />
                        </DataTable>
                    </TabPanel>
                </TabView>
            </Card>
        </div>
    );
};

export default Copa;
