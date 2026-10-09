import React, { useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';

export const IntercompanyEliminacoes = () => {
    const toast = useRef(null);
    const [competencia, setCompetencia] = useState(() => {
        const d = new Date();
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
    });
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch('/api/corporativo/intercompany/eliminacoes?competencia=' + encodeURIComponent(competencia));
            if (!r.ok) throw new Error('HTTP ' + r.status);
            const j = await r.json();
            const elim = j?.eliminacoes ?? j?.data?.eliminacoes ?? j;
            const list = elim?.linhas ?? (Array.isArray(elim) ? elim : []);
            setRows(Array.isArray(list) ? list : []);
            if (elim?.total != null) toast.current?.show({ severity: 'info', summary: 'Total', detail: String(elim.total), life: 2500 });
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
            setRows([]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0">Eliminações intercompany</h2>
            <p className="text-color-secondary">Paques / saldos a eliminar na consolidação (competência AAAA-MM).</p>
            <div className="flex gap-2 align-items-end mb-3">
                <div>
                    <label className="block mb-1">Competência</label>
                    <InputText value={competencia} onChange={(e) => setCompetencia(e.target.value)} placeholder="2026-10" className="w-10rem" />
                </div>
                <Button label="Carregar" icon="pi pi-search" onClick={carregar} loading={loading} />
            </div>
            <DataTable value={rows} loading={loading} emptyMessage="Sem eliminações" paginator rows={15} size="small">
                <Column field="numero" header="Número" />
                <Column field="empresa_parceira_id" header="Parceira" />
                <Column field="valor" header="Valor" body={(r) => Number(r.valor || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} />
                <Column field="moeda" header="Moeda" />
                <Column field="status" header="Status" />
                <Column field="reconciliado" header="Rec." body={(r) => (r.reconciliado ? 'Sim' : 'Não')} />
                <Column field="diferenca" header="Diferença" />
            </DataTable>
        </div>
    );
};
export default IntercompanyEliminacoes;
