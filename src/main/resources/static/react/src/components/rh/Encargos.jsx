import React, { useState, useEffect, useRef } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';

const fmt = (v) => Number(v ?? 0).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });

export const Encargos = () => {
    const toast = useRef(null);
    const [folhas, setFolhas] = useState([]);
    const [folhaId, setFolhaId] = useState(null);
    const [aliqInss, setAliqInss] = useState(20);
    const [aliqFgts, setAliqFgts] = useState(8);
    const [aliqRat, setAliqRat] = useState(2);
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        apiFetch('/api/rh/folhas')
            .then(async (r) => {
                const j = await r.json().catch(() => []);
                const list = Array.isArray(j) ? j : (j?.data ?? j?.content ?? []);
                setFolhas(list);
            })
            .catch(() => {});
    }, []);

    const calcular = async () => {
        if (!folhaId) {
            toast.current?.show({ severity: 'warn', summary: 'Selecione a folha', life: 3000 });
            return;
        }
        setLoading(true);
        try {
            const qs = new URLSearchParams({
                aliqInss: String(aliqInss ?? 20),
                aliqFgts: String(aliqFgts ?? 8),
                aliqRat: String(aliqRat ?? 2),
            });
            const r = await apiFetch(`/api/rh/encargos/folha/${folhaId}?${qs}`);
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.message || 'Falha no cálculo');
            }
            setRes(await r.json());
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
            setRes(null);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="mb-3">
                <h2 className="m-0">Encargos sobre a folha</h2>
                <span className="bc-muted">INSS patronal, FGTS e RAT estimados</span>
            </div>
            <div className="flex gap-2 flex-wrap align-items-end mb-3">
                <span>
                    <label className="bc-label">Folha</label>
                    <Dropdown
                        value={folhaId}
                        options={folhas.map((f) => ({
                            label: `${f.competencia || f.id} (${f.status || ''})`,
                            value: f.id,
                        }))}
                        onChange={(e) => { setFolhaId(e.value); setRes(null); }}
                        placeholder="Selecione"
                        filter
                        style={{ minWidth: '16rem' }}
                    />
                </span>
                <span>
                    <label className="bc-label">INSS %</label>
                    <InputNumber value={aliqInss} onValueChange={(e) => setAliqInss(e.value)} min={0} max={100} suffix=" %" />
                </span>
                <span>
                    <label className="bc-label">FGTS %</label>
                    <InputNumber value={aliqFgts} onValueChange={(e) => setAliqFgts(e.value)} min={0} max={100} suffix=" %" />
                </span>
                <span>
                    <label className="bc-label">RAT %</label>
                    <InputNumber value={aliqRat} onValueChange={(e) => setAliqRat(e.value)} min={0} max={100} suffix=" %" />
                </span>
                <Button label="Calcular" icon="pi pi-calculator" onClick={calcular} loading={loading} />
            </div>
            {res && (
                <div className="grid">
                    {[
                        ['Base (proventos)', res.base],
                        ['INSS patronal', res.inssPatronal],
                        ['FGTS', res.fgts],
                        ['RAT', res.rat],
                        ['Total encargos', res.total],
                    ].map(([l, v]) => (
                        <div key={l} className="col-12 md:col-4">
                            <Card>
                                <small>{l}</small>
                                <div className="text-xl font-bold">{fmt(v)}</div>
                            </Card>
                        </div>
                    ))}
                    <div className="col-12">
                        <small className="bc-muted">Competência: {res.competencia || '—'} · Folha #{res.folhaId}</small>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Encargos;
