import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useNavigate } from 'react-router-dom';

const sev = (s) => {
    if (s === 'OK') return 'success';
    if (s === 'PENDENTE' || s === 'INFO') return 'warning';
    if (s === 'ERRO') return 'danger';
    return 'info';
};

const links = {
    certificado: '/configurar-empresa',
    regras: '/fiscal/regras-tributarias',
    reinf: '/fiscal/reinf',
    apuracao: '/fiscal/apuracoes',
    stripe: '/financeiro/stripe',
    cnab: '/financeiro/boletos',
};

export const FiscalProntidao = () => {
    const toast = useRef(null);
    const nav = useNavigate();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch('/api/fiscal/prontidao');
            if (!r.ok) throw new Error('HTTP ' + r.status);
            setData(await r.json());
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Prontidão fiscal / cobrança</h2>
                    <span className="text-color-secondary">Checklist do que o ERP consegue verificar sozinho</span>
                </div>
                <Button label="Atualizar" icon="pi pi-refresh" onClick={carregar} loading={loading} />
            </div>
            {data && (
                <>
                    <div className="grid mb-3">
                        <div className="col-6 md:col-3"><Card><small>OK</small><div className="text-2xl font-bold text-green-600">{data.ok}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Atenção</small><div className="text-2xl font-bold text-orange-500">{data.warn}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Falha</small><div className="text-2xl font-bold text-red-500">{data.fail}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Operacional</small><div className="text-2xl font-bold">{data.prontoOperacional ? 'Sim' : 'Não'}</div></Card></div>
                    </div>
                    <DataTable value={data.itens || []} loading={loading} size="small">
                        <Column field="nome" header="Item" />
                        <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                        <Column field="detalhe" header="Detalhe" />
                        <Column header="" body={(r) => links[r.id] ? (
                            <Button label="Abrir" size="small" text icon="pi pi-arrow-right" onClick={() => nav(links[r.id])} />
                        ) : null} />
                    </DataTable>
                    <p className="text-color-secondary text-sm mt-3">{data.observacao}</p>
                </>
            )}
        </div>
    );
};
export default FiscalProntidao;
