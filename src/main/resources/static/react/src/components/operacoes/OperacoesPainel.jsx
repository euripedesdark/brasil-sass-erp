import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useNavigate } from 'react-router-dom';

export const OperacoesPainel = () => {
    const toast = useRef(null);
    const nav = useNavigate();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch('/api/operacoes/painel');
            if (!r.ok) throw new Error('HTTP ' + r.status);
            setData(await r.json());
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    const hd = data?.helpdesk || {};

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 className="m-0">Operações — painel multi-módulo</h2>
                    <span className="text-color-secondary">CRM, Projetos, DMS, Workflow, Qualidade, Ativos, Helpdesk, Agenda</span>
                </div>
                <Button label="Atualizar" icon="pi pi-refresh" onClick={carregar} loading={loading} />
            </div>
            {data && (
                <>
                    <div className="grid mb-3">
                        <div className="col-6 md:col-3"><Card><small>Helpdesk abertos</small><div className="text-2xl font-bold">{hd.abertos ?? 0}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Helpdesk em andamento</small><div className="text-2xl font-bold">{hd.emAndamento ?? 0}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Agenda (agendados)</small><div className="text-2xl font-bold">{data.agendaAgendados ?? 0}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Agenda hoje</small><div className="text-2xl font-bold">{data.agendaHoje ?? 0}</div></Card></div>
                    </div>
                    <div className="grid">
                        {(data.modulos || []).map((m) => (
                            <div className="col-12 md:col-6 xl:col-3" key={m.id}>
                                <Card className="h-full">
                                    <div className="flex justify-content-between align-items-start">
                                        <h3 className="mt-0 mb-2">{m.nome}</h3>
                                        <Tag value={m.status} severity={m.status === 'ATIVO' ? 'success' : 'secondary'} />
                                    </div>
                                    <div className="text-2xl font-bold mb-3">{m.registros < 0 ? '—' : m.registros}</div>
                                    <Button label="Abrir" icon="pi pi-arrow-right" outlined size="small" onClick={() => nav(m.rota)} />
                                </Card>
                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};
export default OperacoesPainel;
