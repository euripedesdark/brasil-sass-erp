import React, { useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { Card } from 'primereact/card';

const BASE = '/api/enterprise/tms-planejamento';

export const TmsPlanejamento = () => {
    const toast = useRef(null);
    const [origem, setOrigem] = useState('CD');
    const [capPeso, setCapPeso] = useState(1000);
    const [capVol, setCapVol] = useState(40);
    const [transportadoraId, setTransportadoraId] = useState(null);
    const [entregas, setEntregas] = useState([
        { referenciaTipo: 'PEDIDO_VENDA', referenciaId: 1, destino: 'SP', peso: 100, volume: 2 },
    ]);
    const [resultado, setResultado] = useState([]);
    const [busy, setBusy] = useState(false);

    const planejar = async () => {
        setBusy(true);
        try {
            const r = await apiFetch(BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    origem,
                    capacidadePeso: capPeso,
                    capacidadeVolume: capVol,
                    transportadoraId: transportadoraId || undefined,
                    entregas: entregas.map((e) => ({
                        referenciaTipo: e.referenciaTipo || 'PEDIDO_VENDA',
                        referenciaId: Number(e.referenciaId),
                        destino: e.destino,
                        peso: Number(e.peso),
                        volume: Number(e.volume),
                    })),
                }),
            });
            if (!r.ok) {
                const err = await r.json().catch(() => ({}));
                throw new Error(err?.message || err?.erro || ('HTTP ' + r.status));
            }
            const j = await r.json();
            setResultado(Array.isArray(j) ? j : (j?.data ?? []));
            toast.current?.show({ severity: 'success', summary: 'Planejado', detail: (Array.isArray(j) ? j.length : 0) + ' ordem(ns)', life: 3000 });
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 5000 });
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0 mb-1">TMS — Planejamento de carga</h2>
            <p className="text-color-secondary mt-0">Agrupa entregas por destino respeitando peso/volume do veículo (sem roteirização por distância).</p>

            <Card className="mb-3">
                <div className="grid">
                    <div className="col-12 md:col-3">
                        <label className="block mb-1">Origem</label>
                        <InputText value={origem} onChange={(e) => setOrigem(e.target.value)} className="w-full" />
                    </div>
                    <div className="col-6 md:col-3">
                        <label className="block mb-1">Cap. peso</label>
                        <InputNumber value={capPeso} onValueChange={(e) => setCapPeso(e.value)} className="w-full" />
                    </div>
                    <div className="col-6 md:col-3">
                        <label className="block mb-1">Cap. volume</label>
                        <InputNumber value={capVol} onValueChange={(e) => setCapVol(e.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-3">
                        <label className="block mb-1">Transportadora ID</label>
                        <InputNumber value={transportadoraId} onValueChange={(e) => setTransportadoraId(e.value)} className="w-full" useGrouping={false} />
                    </div>
                </div>
            </Card>

            <h3>Entregas</h3>
            {entregas.map((e, i) => (
                <div key={i} className="grid align-items-end mb-2">
                    <div className="col-2"><InputText placeholder="Tipo" value={e.referenciaTipo} onChange={(ev) => { const a = [...entregas]; a[i] = { ...a[i], referenciaTipo: ev.target.value }; setEntregas(a); }} className="w-full" /></div>
                    <div className="col-2"><InputNumber placeholder="Ref ID" value={e.referenciaId} onValueChange={(ev) => { const a = [...entregas]; a[i] = { ...a[i], referenciaId: ev.value }; setEntregas(a); }} className="w-full" useGrouping={false} /></div>
                    <div className="col-2"><InputText placeholder="Destino" value={e.destino} onChange={(ev) => { const a = [...entregas]; a[i] = { ...a[i], destino: ev.target.value }; setEntregas(a); }} className="w-full" /></div>
                    <div className="col-2"><InputNumber placeholder="Peso" value={e.peso} onValueChange={(ev) => { const a = [...entregas]; a[i] = { ...a[i], peso: ev.value }; setEntregas(a); }} className="w-full" /></div>
                    <div className="col-2"><InputNumber placeholder="Volume" value={e.volume} onValueChange={(ev) => { const a = [...entregas]; a[i] = { ...a[i], volume: ev.value }; setEntregas(a); }} className="w-full" /></div>
                    <div className="col-2"><Button icon="pi pi-trash" severity="danger" text onClick={() => setEntregas(entregas.filter((_, j) => j !== i))} /></div>
                </div>
            ))}
            <div className="flex gap-2 mb-3">
                <Button label="Add entrega" icon="pi pi-plus" text onClick={() => setEntregas([...entregas, { referenciaTipo: 'PEDIDO_VENDA', referenciaId: null, destino: '', peso: 0, volume: 0 }])} />
                <Button label="Planejar" icon="pi pi-truck" onClick={planejar} loading={busy} />
            </div>

            <DataTable value={resultado} emptyMessage="Sem ordens ainda" responsiveLayout="scroll">
                <Column field="ordemId" header="Ordem" />
                <Column field="destino" header="Destino" />
                <Column field="pesoTotal" header="Peso" />
                <Column field="volumeTotal" header="Volume" />
                <Column field="paradas" header="Paradas" body={(r) => (r.paradas != null ? String(r.paradas) : (r.entregas?.length ?? '—'))} />
            </DataTable>
        </div>
    );
};
export default TmsPlanejamento;
