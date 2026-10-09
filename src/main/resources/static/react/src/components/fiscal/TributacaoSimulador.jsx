import React, { useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Checkbox } from 'primereact/checkbox';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';

export const TributacaoSimulador = () => {
    const toast = useRef(null);
    const [base, setBase] = useState(1000);
    const [ncm, setNcm] = useState('');
    const [cfop, setCfop] = useState('5102');
    const [ufO, setUfO] = useState('SP');
    const [ufD, setUfD] = useState('RJ');
    const [cf, setCf] = useState(true);
    const [contrib, setContrib] = useState(false);
    const [res, setRes] = useState(null);
    const [busy, setBusy] = useState(false);
    const money = (v) => (v == null ? '—' : Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }));

    const simularLote = async () => {
        setBusy(true);
        try {
            const r = await apiFetch('/api/fiscal/tributacao/simular-itens', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ufOrigem: ufO, ufDestino: ufD, consumidorFinal: cf, contribuinte: contrib,
                    itens: [
                        { ref: '1', base, ncm, cfop },
                        { ref: '2', base: Number(base || 0) * 0.5, ncm, cfop },
                    ],
                }),
            });
            if (!r.ok) throw new Error('HTTP ' + r.status);
            setRes(await r.json());
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setBusy(false);
        }
    };

    const simular = async () => {
        setBusy(true);
        try {
            const r = await apiFetch('/api/fiscal/tributacao/simular', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    base, ncm, cfop, ufOrigem: ufO, ufDestino: ufD,
                    consumidorFinal: cf, contribuinte: contrib,
                }),
            });
            if (!r.ok) throw new Error('HTTP ' + r.status);
            setRes(await r.json());
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0">Simulador de tributação</h2>
            <p className="text-color-secondary">Resolve regra (NCM/CFOP/UF) e calcula ICMS, PIS, COFINS, DIFAL e ST.</p>
            <Card className="mb-3">
                <div className="grid">
                    <div className="col-6 md:col-3"><label className="block mb-1">Base</label><InputNumber value={base} onValueChange={(e) => setBase(e.value)} mode="currency" currency="BRL" locale="pt-BR" className="w-full" /></div>
                    <div className="col-6 md:col-2"><label className="block mb-1">NCM</label><InputText value={ncm} onChange={(e) => setNcm(e.target.value)} className="w-full" /></div>
                    <div className="col-6 md:col-2"><label className="block mb-1">CFOP</label><InputText value={cfop} onChange={(e) => setCfop(e.target.value)} className="w-full" /></div>
                    <div className="col-3 md:col-1"><label className="block mb-1">UF ori</label><InputText value={ufO} onChange={(e) => setUfO(e.target.value)} className="w-full" maxLength={2} /></div>
                    <div className="col-3 md:col-1"><label className="block mb-1">UF dest</label><InputText value={ufD} onChange={(e) => setUfD(e.target.value)} className="w-full" maxLength={2} /></div>
                    <div className="col-6 md:col-3 flex align-items-center gap-3 mt-3">
                        <span className="flex align-items-center gap-2"><Checkbox checked={cf} onChange={(e) => setCf(e.checked)} /><label>Cons. final</label></span>
                        <span className="flex align-items-center gap-2"><Checkbox checked={contrib} onChange={(e) => setContrib(e.checked)} /><label>Contribuinte</label></span>
                    </div>
                </div>
                <div className="flex gap-2 mt-3">
                  <Button label="Simular linha" icon="pi pi-calculator" onClick={simular} loading={busy} />
                  <Button label="Simular lote (2 itens)" icon="pi pi-list" outlined onClick={simularLote} loading={busy} />
                </div>
            </Card>
            {res && res.totalImpostos != null && (
                <Card title="Totais do lote" className="mb-3">
                    <div className="grid">
                        <div className="col-6 md:col-2"><b>ICMS</b><div>{money(res.totalIcms)}</div></div>
                        <div className="col-6 md:col-2"><b>PIS</b><div>{money(res.totalPis)}</div></div>
                        <div className="col-6 md:col-2"><b>COFINS</b><div>{money(res.totalCofins)}</div></div>
                        <div className="col-6 md:col-2"><b>DIFAL</b><div>{money(res.totalDifal)}</div></div>
                        <div className="col-6 md:col-2"><b>ST</b><div>{money(res.totalIcmsSt)}</div></div>
                        <div className="col-6 md:col-2"><b>Total</b><div>{money(res.totalImpostos)}</div></div>
                    </div>
                </Card>
            )}
            {res && (
                <Card title={res.regraEncontrada ? ('Regra: ' + (res.regraNome || res.regraId)) : 'Sem regra'}>
                    {!res.regraEncontrada && <p>{res.mensagem}</p>}
                    {res.regraEncontrada && (
                        <div className="grid">
                            <div className="col-6 md:col-3"><b>ICMS</b><div>{money(res.icms)}</div></div>
                            <div className="col-6 md:col-3"><b>IPI</b><div>{money(res.ipi)}</div></div>
                            <div className="col-6 md:col-3"><b>PIS</b><div>{money(res.pis)}</div></div>
                            <div className="col-6 md:col-3"><b>COFINS</b><div>{money(res.cofins)}</div></div>
                            {res.difal && (
                                <div className="col-12"><b>DIFAL</b> {money(res.difal.difal)} + FCP {money(res.difal.fcp)}</div>
                            )}
                            {res.icmsSt && (
                                <div className="col-12"><b>ICMS-ST</b> base {money(res.icmsSt.baseSt)} → ST {money(res.icmsSt.icmsSt)}</div>
                            )}
                        </div>
                    )}
                </Card>
            )}
        </div>
    );
};
export default TributacaoSimulador;
