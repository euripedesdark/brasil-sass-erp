import React, { useRef, useState } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';

export const IcmsSt = () => {
    const toast = useRef(null);
    const [base, setBase] = useState(1000);
    const [inter, setInter] = useState(12);
    const [interna, setInterna] = useState(18);
    const [mva, setMva] = useState(40);
    const [red, setRed] = useState(0);
    const [res, setRes] = useState(null);
    const [busy, setBusy] = useState(false);
    const money = (v) => (v == null ? '—' : Number(v).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' }));

    const calcular = async () => {
        if ([base, inter, interna, mva].some((v) => v == null || v < 0) || inter > 100 || interna > 100 || (red != null && (red < 0 || red > 100))) {
            setRes(null);
            toast.current?.show({ severity: 'error', summary: 'Dados inválidos', detail: 'Informe base, alíquotas e MVA; alíquotas e redução devem estar entre 0 e 100%.' });
            return;
        }
        setRes(null);
        setBusy(true);
        try {
            const r = await apiFetch('/api/fiscal/icms-st/calcular', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
                body: JSON.stringify({
                    baseOperacao: base,
                    aliqInterestadual: inter,
                    aliqInterna: interna,
                    mva,
                    reducaoBasePct: red,
                }),
            });
            if (!r.ok) throw new Error('HTTP ' + r.status);
            const json = await r.json();
            setRes(json?.data ?? json);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0">ICMS-ST (MVA)</h2>
            <p className="text-color-secondary">Substituição tributária por MVA. Validar protocolo ICMS / CEST da UF.</p>
            <Card className="mb-3">
                <div className="grid">
                    <div className="col-6 md:col-4"><label className="block mb-1">Base operação</label><InputNumber value={base} min={0} onValueChange={(e) => setBase(e.value)} mode="currency" currency="BRL" locale="pt-BR" className="w-full" /></div>
                    <div className="col-6 md:col-2"><label className="block mb-1">Alíq inter %</label><InputNumber value={inter} min={0} max={100} onValueChange={(e) => setInter(e.value)} className="w-full" /></div>
                    <div className="col-6 md:col-2"><label className="block mb-1">Alíq interna %</label><InputNumber value={interna} min={0} max={100} onValueChange={(e) => setInterna(e.value)} className="w-full" /></div>
                    <div className="col-6 md:col-2"><label className="block mb-1">MVA %</label><InputNumber value={mva} min={0} onValueChange={(e) => setMva(e.value)} className="w-full" /></div>
                    <div className="col-6 md:col-2"><label className="block mb-1">Red. base %</label><InputNumber value={red} min={0} max={100} onValueChange={(e) => setRed(e.value)} className="w-full" /></div>
                </div>
                <Button label="Calcular" icon="pi pi-calculator" className="mt-3" onClick={calcular} loading={busy} />
            </Card>
            {res && (
                <Card title="Resultado">
                    <div className="grid">
                        <div className="col-6 md:col-3"><b>ICMS operação</b><div>{money(res.icmsOperacao)}</div></div>
                        <div className="col-6 md:col-3"><b>Base ST</b><div>{money(res.baseSt)}</div></div>
                        <div className="col-6 md:col-3"><b>ICMS-ST</b><div>{money(res.icmsSt)}</div></div>
                        <div className="col-6 md:col-3"><b>Total ICMS</b><div>{money(res.totalIcms)}</div></div>
                    </div>
                    <p className="text-sm text-color-secondary mt-3">{res.observacao}</p>
                </Card>
            )}
        </div>
    );
};
export default IcmsSt;
