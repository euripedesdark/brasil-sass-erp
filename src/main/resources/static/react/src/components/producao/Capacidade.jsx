import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { AutoComplete } from 'primereact/autocomplete';
import { Dropdown } from 'primereact/dropdown';
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

const lerErro = async (r, padrao) => {
    try {
        const j = await r.json();
        return j?.message || j?.error || padrao;
    } catch {
        return padrao;
    }
};

const lista = (data) => {
    const x = data?.data ?? data;
    return Array.isArray(x) ? x : (x?.content || []);
};

export default function Capacidade() {
    const [produto, setProduto] = useState(null);
    const [sugestoes, setSugestoes] = useState([]);
    const [quantidade, setQuantidade] = useState(1);
    const [inicio, setInicio] = useState(new Date());
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);
    const [erro, setErro] = useState('');

    // calendario de carga
    const [centros, setCentros] = useState([]);
    const [centroId, setCentroId] = useState(null);
    const [de, setDe] = useState(new Date());
    const [ate, setAte] = useState(() => { const d = new Date(); d.setDate(d.getDate() + 29); return d; });
    const [calendario, setCalendario] = useState([]);
    const [erroCal, setErroCal] = useState('');

    // agendamento de OP
    const [ordens, setOrdens] = useState([]);
    const [ordemId, setOrdemId] = useState(null);
    const [msgOrdem, setMsgOrdem] = useState(null);

    useEffect(() => {
        (async () => {
            try {
                const [c, o] = await Promise.all([
                    apiFetch('/api/producao/centros-trabalho'),
                    apiFetch('/api/producao')
                ]);
                if (c.ok) setCentros(lista(await c.json()).filter(x => x.ativo !== false));
                if (o.ok) {
                    setOrdens(lista(await o.json()).filter(x => ['ABERTO', 'EM_PROCESSO'].includes(x.status)));
                }
            } catch { /* listas ficam vazias; telas mostram o estado vazio */ }
        })();
    }, []);

    const carregarCalendario = async () => {
        if (!centroId) { setErroCal('Selecione um centro de trabalho.'); return; }
        setErroCal('');
        try {
            const r = await apiFetch(`/api/producao/capacidade/calendario?centroId=${centroId}&de=${toIso(de)}&ate=${toIso(ate)}`);
            if (!r.ok) throw new Error(await lerErro(r, 'Falha ao carregar o calendário'));
            setCalendario(lista(await r.json()));
        } catch (e) {
            setCalendario([]);
            setErroCal(e.message);
        }
    };

    const agendarOrdem = async () => {
        if (!ordemId) return;
        setMsgOrdem(null);
        try {
            const r = await apiFetch(`/api/producao/capacidade/ordens/${ordemId}/agendar`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ dataInicio: toIso(inicio) })
            });
            if (!r.ok) throw new Error(await lerErro(r, 'Falha ao agendar a ordem'));
            const d = await r.json();
            const x = d?.data ?? d;
            setMsgOrdem({ sev: 'success', txt: `Ordem agendada: ${Number(x.horasReservadas).toFixed(2)} h reservadas, término em ${fmtData(x.dataTermino)}.` });
            if (centroId) carregarCalendario();
        } catch (e) {
            setMsgOrdem({ sev: 'error', txt: e.message });
        }
    };

    const liberarOrdem = async () => {
        if (!ordemId) return;
        setMsgOrdem(null);
        try {
            const r = await apiFetch(`/api/producao/capacidade/ordens/${ordemId}/agendamento`, { method: 'DELETE' });
            if (!r.ok) throw new Error(await lerErro(r, 'Falha ao liberar a reserva'));
            const d = await r.json();
            const x = d?.data ?? d;
            setMsgOrdem({ sev: 'info', txt: `${x.linhasLiberadas} reserva(s) liberada(s).` });
            if (centroId) carregarCalendario();
        } catch (e) {
            setMsgOrdem({ sev: 'error', txt: e.message });
        }
    };

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
            <Card className="mt-3" title="Agendamento e Calendário de Carga"
                  subTitle="Reserva horas dos centros de trabalho para uma OP e mostra a ocupação dia a dia">
                <div className="grid p-fluid align-items-end">
                    <div className="col-12 md:col-6">
                        <label htmlFor="cap-ordem">Ordem de produção (aberta / em processo)</label>
                        <Dropdown inputId="cap-ordem" value={ordemId} options={ordens}
                                  optionValue="id"
                                  optionLabel="numero"
                                  itemTemplate={(o) => `${o.numero} — produto ${o.produtoFinalId} × ${o.quantidadePlanejada}`}
                                  onChange={(e) => setOrdemId(e.value)}
                                  placeholder="Selecione a OP" emptyMessage="Nenhuma OP aberta" />
                        <small className="bc-muted">Usa o início desejado informado acima.</small>
                    </div>
                    <div className="col-12 md:col-3">
                        <Button label="Agendar OP" icon="pi pi-lock" disabled={!ordemId} onClick={agendarOrdem} />
                    </div>
                    <div className="col-12 md:col-3">
                        <Button label="Liberar reserva" icon="pi pi-unlock" severity="secondary"
                                className="p-button-secondary" disabled={!ordemId} onClick={liberarOrdem} />
                    </div>
                </div>
                {msgOrdem && <Message severity={msgOrdem.sev} text={msgOrdem.txt} className="w-full mt-3" />}

                <div className="grid p-fluid align-items-end mt-4">
                    <div className="col-12 md:col-4">
                        <label htmlFor="cal-centro">Centro de trabalho</label>
                        <Dropdown inputId="cal-centro" value={centroId} options={centros} optionValue="id"
                                  optionLabel="nome"
                                  itemTemplate={(c) => `${c.codigo} — ${c.nome}`}
                                  onChange={(e) => setCentroId(e.value)} placeholder="Selecione"
                                  emptyMessage="Cadastre centros em Roteiros" />
                    </div>
                    <div className="col-6 md:col-2">
                        <label htmlFor="cal-de">De</label>
                        <Calendar inputId="cal-de" value={de} onChange={(e) => setDe(e.value)} dateFormat="dd/mm/yy" />
                    </div>
                    <div className="col-6 md:col-2">
                        <label htmlFor="cal-ate">Até</label>
                        <Calendar inputId="cal-ate" value={ate} onChange={(e) => setAte(e.value)} dateFormat="dd/mm/yy" />
                    </div>
                    <div className="col-12 md:col-4">
                        <Button label="Ver calendário" icon="pi pi-table" onClick={carregarCalendario} />
                    </div>
                </div>
                {erroCal && <Message severity="error" text={erroCal} className="w-full mt-3" />}
                <DataTable value={calendario} className="mt-3" responsiveLayout="scroll" paginator rows={15}
                           emptyMessage="Escolha o centro e o período."
                           rowClassName={(r) => (r.sobrecarga ? 'bg-red-100' : (!r.diaUtil ? 'bg-gray-100' : ''))}>
                    <Column header="Data" body={(r) => fmtData(r.data)} />
                    <Column header="Dia útil" body={(r) => (r.diaUtil ? 'Sim' : 'Não')} />
                    <Column header="Capacidade (h)" body={(r) => Number(r.capacidadeHoras).toFixed(2)} />
                    <Column header="Alocado (h)" body={(r) => Number(r.alocadoHoras).toFixed(2)} />
                    <Column header="Livre (h)" body={(r) => Number(r.livreHoras).toFixed(2)} />
                    <Column header="Situação" body={(r) => (r.sobrecarga ? 'Sobrecarga' : (r.alocadoHoras > 0 ? 'Ocupado' : '—'))} />
                </DataTable>
            </Card>
        </div>
    );
}
