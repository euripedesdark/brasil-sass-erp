import React, { useRef, useState } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';

export const Difal = () => {
    const toast = useRef(null);
    const [base, setBase] = useState(1000);
    const [inter, setInter] = useState(12);
    const [interna, setInterna] = useState(18);
    const [fcp, setFcp] = useState(2);
    const [res, setRes] = useState(null);
    const [busy, setBusy] = useState(false);

    const calcular = async () => {
        setBusy(true);
        try {
            const r = await apiFetch('/api/fiscal/difal/calcular', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    base,
                    aliqInterestadual: inter,
                    aliqInternaDestino: interna,
                    aliqFcp: fcp,
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

    const money = (v) => (v == null ? '—' : Number(v).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' }));

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0">ICMS DIFAL + FCP</h2>
            <p className="text-color-secondary">Calculadora EC 87/2015 — partilha 100% UF destino. Não substitui a apuração oficial.</p>
            <Card className="mb-3">
                <div className="grid">
                    <div className="col-6 md:col-3"><label className="block mb-1">Base</label><InputNumber value={base} onValueChange={(e) => setBase(e.value)} mode="currency" currency="BRL" locale={localeAtivo()} className="w-full" /></div>
                    <div className="col-6 md:col-3"><label className="block mb-1">Alíq. interestadual %</label><InputNumber value={inter} onValueChange={(e) => setInter(e.value)} className="w-full" minFractionDigits={0} maxFractionDigits={2} /></div>
                    <div className="col-6 md:col-3"><label className="block mb-1">Alíq. interna destino %</label><InputNumber value={interna} onValueChange={(e) => setInterna(e.value)} className="w-full" /></div>
                    <div className="col-6 md:col-3"><label className="block mb-1">FCP %</label><InputNumber value={fcp} onValueChange={(e) => setFcp(e.value)} className="w-full" /></div>
                </div>
                <Button label="Calcular" icon="pi pi-calculator" className="mt-3" onClick={calcular} loading={busy} />
            </Card>
            {res && (
                <Card title="Resultado">
                    <div className="grid">
                        <div className="col-6 md:col-3"><b>ICMS origem</b><div>{money(res.icmsOrigem)}</div></div>
                        <div className="col-6 md:col-3"><b>DIFAL</b><div>{money(res.difal)}</div></div>
                        <div className="col-6 md:col-3"><b>FCP</b><div>{money(res.fcp)}</div></div>
                        <div className="col-6 md:col-3"><b>Total UF destino</b><div>{money(res.totalUfDestino)}</div></div>
                    </div>
                    <p className="text-sm text-color-secondary mt-3">{res.observacao}</p>
                </Card>
            )}
        </div>
    );
};
export default Difal;
