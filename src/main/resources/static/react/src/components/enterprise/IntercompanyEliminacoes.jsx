import React, { useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { localeAtivo } from '../shared/LocaleData.js';

export const IntercompanyEliminacoes = () => {
    const toast = useRef(null);
    const [competencia, setCompetencia] = useState(() => {
        const d = new Date();
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
    });
    const [rows, setRows] = useState([]);
    const [totais, setTotais] = useState({});
    const [loading, setLoading] = useState(false);

    const formatarValor = (valor, moeda) => {
        const numero = Number(valor || 0);
        try {
            return numero.toLocaleString('pt-BR', { style: 'currency', currency: (moeda || 'BRL').trim() });
        } catch {
            return numero.toLocaleString(localeAtivo()) + ' ' + (moeda || '');
        }
    };
    const carregar = async () => {
        if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(competencia)) {
            toast.current?.show({ severity: 'error', summary: 'Competência inválida', detail: 'Use AAAA-MM.' });
            return;
        }
        setLoading(true);
        try {
            const r = await apiFetch('/api/corporativo/intercompany/eliminacoes?competencia=' + encodeURIComponent(competencia + '-01'), { headers: { Accept: 'application/json' } });
            if (!r.ok) throw new Error('HTTP ' + r.status);
            const j = await r.json();
            const elim = j?.eliminacoes ?? j?.data?.eliminacoes ?? j;
            const list = elim?.linhas ?? (Array.isArray(elim) ? elim : []);
            setRows(Array.isArray(list) ? list : []);
            setTotais(elim?.totaisPorMoeda || {});
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
            setRows([]);
            setTotais({});
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0">Eliminações intercompany</h2>
            <p className="text-color-secondary">Lançamentos / saldos a eliminar na consolidação (competência AAAA-MM).</p>
            <div className="flex gap-2 align-items-end mb-3">
                <div>
                    <label className="block mb-1">Competência</label>
                    <InputText value={competencia} onChange={(e) => setCompetencia(e.target.value)} placeholder="2026-10" className="w-10rem" />
                </div>
                <Button label="Carregar" icon="pi pi-search" onClick={carregar} loading={loading} />
            </div>
            <div className="flex gap-3 mb-3">
                {Object.entries(totais).map(([moeda, valor]) => <span key={moeda}>Total {moeda}: {formatarValor(valor, moeda)}</span>)}
            </div>
            <DataTable value={rows} loading={loading} emptyMessage="Sem eliminações" paginator rows={15} size="small">
                <Column field="numero" header="Número" />
                <Column field="empresa_parceira_id" header="Parceira" />
                <Column field="valor" header="Valor" body={(r) => formatarValor(r.valor, r.moeda)} />
                <Column field="moeda" header="Moeda" />
                <Column field="status" header="Status" />
                <Column field="reconciliado" header="Rec." body={(r) => (r.reconciliado ? 'Sim' : 'Não')} />
                <Column field="diferenca" header="Diferença" />
            </DataTable>
        </div>
    );
};
export default IntercompanyEliminacoes;
