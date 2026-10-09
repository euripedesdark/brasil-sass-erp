import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useNavigate } from 'react-router-dom';

export const GoLive = () => {
    const toast = useRef(null);
    const nav = useNavigate();
    const [data, setData] = useState(null);
    const carregar = async () => {
        const r = await apiFetch('/api/core/golive');
        if (!r.ok) throw new Error('HTTP ' + r.status);
        setData(await r.json());
    };
    useEffect(() => { carregar().catch((e) => toast.current?.show({ severity: 'error', detail: e.message })); }, []);
    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between mb-3">
                <div><h2 className="m-0">Go-live checklist</h2>
                    <span className="text-color-secondary">Pronto para operação (software)</span></div>
                <Button label="Atualizar" icon="pi pi-refresh" onClick={carregar} />
            </div>
            {data && (
                <>
                    <div className="grid mb-3">
                        <div className="col-4"><Card><small>OK</small><div className="text-2xl font-bold text-green-600">{data.ok}</div></Card></div>
                        <div className="col-4"><Card><small>Pendente</small><div className="text-2xl font-bold text-orange-500">{data.pendente}</div></Card></div>
                        <div className="col-4"><Card><small>Pronto</small><div className="text-2xl font-bold">{data.pronto ? 'SIM' : 'NÃO'}</div></Card></div>
                    </div>
                    <DataTable value={data.itens || []} size="small">
                        <Column field="nome" header="Item" />
                        <Column field="status" header="Status" body={(r) => <Tag value={r.status} severity={r.status === 'OK' ? 'success' : 'warning'} />} />
                        <Column body={(r) => <Button label="Abrir" text size="small" onClick={() => nav(r.rota)} />} />
                    </DataTable>
                    <p className="text-sm text-color-secondary mt-3">{data.observacao}</p>
                </>
            )}
        </div>
    );
};
export default GoLive;
