import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { AutoComplete } from 'primereact/autocomplete';
import { apiFetch } from '../../services/ApiConfig';

const fmtData = (d) => {
    if (!d) return '';
    const [y, m, dia] = String(d).split('-');
    return `${dia}/${m}/${y}`;
};

const toIso = (d) => {
    if (!d) return null;
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mm}-${dd}`;
};

export default function Capacidade() {
    const [produto, setProduto] = useState(null);
    const [sugestoes, setSugestoes] = useState([]);
    const [quantidade, setQuantidade] = useState(1);
    const [inicio, setInicio] = useState(new Date());
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);
    const [erro, setErro] = useState('');

    const buscarProdutos = async (event) => {
        try {
            const r = await apiFetch('/api/cadastro/produtos?nome=' + encodeURIComponent(event.query || '') + '&ativo=true&size=20');
            if (!r.ok) { setSugestoes([]); return; }
            const data = await r.json();
            const page = data?.data ?? data;
            setSugestoes(Array.isArray(page) ? page : (page?.content || []));
        } catch {
            setSugestoes([]);
        }
    };

    const simular = async () => {
        if (!produto?.id) { setErro('Selecione um produto cadastrado.'); return; }
        if (!(Number(quantidade) > 0)) { setErro('Informe uma quantidade maior que zero.'); return; }
        setLoading(true);
        setErro('');
        try {
            const r = await apiFetch('/api/producao/capacidade/simular', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ produtoId: produto.id, quantidade: Number(quantidade), dataInicio: toIso(inicio) })
            });
            if (!r.ok) {
                let msg = 'Falha ao simular capacidade';
                try { const j = await r.json(); msg = j?.message || j?.error || msg; } catch { /* corpo nao JSON */ }
                throw new Error(msg);
            }
            const data = await r.json();
            setRes(data?.data ?? data);
        } catch (e) {
            setRes(null);
            setErro(e.message || 'Falha ao simular capacidade');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-3">
            <Card title="Capacidade e Prazo de Produção"
                  subTitle="Roteiro vigente × centros de trabalho: horas, dias úteis e data de término">
                <div className="grid p-fluid align-items-end">
                    <div className="col-12 md:col-4">
                        <label htmlFor="cap-produto">Produto</label>
                        <AutoComplete id="cap-produto" value={produto} suggestions={sugestoes}
                                      completeMethod={buscarProdutos} field="nome" dropdown
                                      onChange={(e) => setProduto(e.value)}
                                      placeholder="Consultar produto no cadastro" />
                    </div>
                    <div className="col-12 md:col-2">
                        <label htmlFor="cap-qtd">Quantidade</label>
                        <InputNumber inputId="cap-qtd" value={quantidade}
                                     onValueChange={(e) => setQuantidade(e.value ?? 0)}
                                     min={0.0001} maxFractionDigits={4} />
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="cap-inicio">Início desejado</label>
                        <Calendar inputId="cap-inicio" value={inicio} onChange={(e) => setInicio(e.value)}
                                  dateFormat="dd/mm/yy" showIcon />
                    </div>
                    <div className="col-12 md:col-3">
                        <Button label="Simular" icon="pi pi-calendar" loading={loading}
                                onClick={simular} disabled={!produto?.id} />
                    </div>
                </div>

                {erro && <Message severity="error" text={erro} className="w-full mt-3" />}

                {res && (
                    <>
                        <div className="mt-4">
                            <b>Roteiro:</b> {res.roteiro} (v{res.versao}) &nbsp;|&nbsp;
                            <b>Horas totais:</b> {Number(res.horasTotais).toFixed(2)} h &nbsp;|&nbsp;
                            <b>Início:</b> {fmtData(res.dataInicio)} &nbsp;|&nbsp;
                            <b>Término:</b> {fmtData(res.dataTermino)} &nbsp;|&nbsp;
                            <b>Lead time:</b> {res.leadTimeDiasUteis} dia(s) útil(eis)
                        </div>
                        {(res.alertas || []).map((a, i) => (
                            <Message key={i} severity="warn" text={a} className="w-full mt-2" />
                        ))}
                        <DataTable value={res.operacoes || []} className="mt-3" responsiveLayout="scroll"
                                   emptyMessage="Sem operações.">
                            <Column field="sequencia" header="Seq." />
                            <Column field="codigo" header="Código" />
                            <Column field="operacao" header="Operação" />
                            <Column field="centroTrabalho" header="Centro" body={(r) => r.centroTrabalho || '—'} />
                            <Column header="Horas" body={(r) => Number(r.horas).toFixed(2)} />
                            <Column header="Cap. h/dia" body={(r) => r.capacidadeHorasDia ?? '—'} />
                            <Column field="dias" header="Dias" />
                            <Column header="Início" body={(r) => fmtData(r.inicio)} />
                            <Column header="Fim" body={(r) => fmtData(r.fim)} />
                        </DataTable>
                    </>
                )}
            </Card>
        </div>
    );
}
