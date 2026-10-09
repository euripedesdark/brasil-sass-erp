import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useNavigate } from 'react-router-dom';

export const FiscalPainel = () => {
    const toast = useRef(null);
    const nav = useNavigate();
    const [comp, setComp] = useState(() => {
        const d = new Date();
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
    });
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch('/api/fiscal/painel?competencia=' + encodeURIComponent(comp));
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
                    <h2 className="m-0">Painel fiscal</h2>
                    <span className="text-color-secondary">Apuração, REINF, regras e documentos do período</span>
                </div>
                <div className="flex gap-2 align-items-end">
                    <div>
                        <label className="block mb-1">Competência</label>
                        <InputText value={comp} onChange={(e) => setComp(e.target.value)} className="w-8rem" />
                    </div>
                    <Button label="Atualizar" icon="pi pi-refresh" onClick={carregar} loading={loading} />
                </div>
            </div>
            {data && (
                <>
                    <div className="grid mb-3">
                        <div className="col-6 md:col-3"><Card><small>Apurações</small><div className="text-2xl font-bold">{data.apuracoesCount}</div><small>{data.apuracoesEncerradas} encerrada(s)</small></Card></div>
                        <div className="col-6 md:col-3"><Card><small>REINF</small><div className="text-2xl font-bold">{data.reinfCount}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>Regras ativas</small><div className="text-2xl font-bold">{data.regrasAtivas}</div></Card></div>
                        <div className="col-6 md:col-3"><Card><small>NFe / NFS-e</small><div className="text-2xl font-bold">{data.nfePeriodo ?? '—'} / {data.nfsePeriodo ?? '—'}</div></Card></div>
                    </div>
                    <div className="flex flex-wrap gap-2 mb-3">
                        <Button label="Apurações" outlined size="small" onClick={() => nav('/fiscal/apuracoes')} />
                        <Button label="REINF" outlined size="small" onClick={() => nav('/fiscal/reinf')} />
                        <Button label="Simulador" outlined size="small" onClick={() => nav('/fiscal/simulador')} />
                        <Button label="Regras" outlined size="small" onClick={() => nav('/fiscal/regras-tributarias')} />
                        <Button label="SPED" outlined size="small" onClick={() => nav('/fiscal/sped')} />
                        <Button label="DIFAL" outlined size="small" onClick={() => nav('/fiscal/difal')} />
                        <Button label="ICMS-ST" outlined size="small" onClick={() => nav('/fiscal/icms-st')} />
                    </div>
                    <h3>Apurações</h3>
                    <DataTable value={data.apuracoes || []} size="small" className="mb-3" emptyMessage="—">
                        <Column field="imposto" header="Imposto" />
                        <Column field="status" header="Status" body={(r) => <Tag value={r.status} />} />
                        <Column field="valorDebito" header="Débito" />
                        <Column field="valorCredito" header="Crédito" />
                        <Column field="valorRecolher" header="Recolher" />
                    </DataTable>
                    <h3>REINF</h3>
                    <DataTable value={data.reinf || []} size="small" emptyMessage="—">
                        <Column field="evento" header="Evento" />
                        <Column field="status" header="Status" body={(r) => <Tag value={r.status} />} />
                        <Column field="totalDocs" header="Docs" />
                        <Column field="valorTotal" header="Valor" />
                        <Column field="protocolo" header="Protocolo" />
                    </DataTable>
                </>
            )}
        </div>
    );
};
export default FiscalPainel;
