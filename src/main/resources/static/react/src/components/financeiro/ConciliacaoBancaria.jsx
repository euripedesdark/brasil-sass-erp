import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import ContaBancariaService from '../../services/ContaBancariaService';
import ConciliacaoService from '../../services/ConciliacaoService';

const iso = (d) => d ? new Date(d).toISOString().slice(0, 10) : null;
const brDate = (d) => d ? new Date(d).toLocaleDateString('pt-BR') : '';
const money = (v) => Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export default function ConciliacaoBancaria() {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [contas, setContas] = useState([]);
    const [conciliacoes, setConciliacoes] = useState([]);
    const [selecionada, setSelecionada] = useState(null);
    const [itens, setItens] = useState([]);
    const [pendentes, setPendentes] = useState([]);
    const [baixas, setBaixas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialog, setDialog] = useState(false);
    const [erro, setErro] = useState('');
    const [form, setForm] = useState({ contaBancariaId: null, dataInicio: new Date(new Date().getFullYear(), new Date().getMonth(), 1), dataFim: new Date() });
    const [baixaSelecionada, setBaixaSelecionada] = useState({});
    
    const contaLabel = (c) => c ? `${c.banco || ''} ${c.agencia || ''}/${c.conta || ''}${c.digito ? '-' + c.digito : ''}` : '';

    const carregarContas = async () => {
        const r = await ContaBancariaService.listar();
        const d = r?.data?.data ?? r?.data ?? [];
        setContas(Array.isArray(d) ? d : []);
        if (!form.contaBancariaId && d?.length) setForm(f => ({ ...f, contaBancariaId: d[0].id }));
    };

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await ConciliacaoService.listar();
            const d = r?.data?.data ?? r?.data ?? [];
            setConciliacoes(Array.isArray(d) ? d : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e?.response?.data?.message || 'Falha ao carregar conciliações', life: 3000 });
        } finally { setLoading(false); }
    };

    const selecionar = async (c) => {
        setSelecionada(c);
        setLoading(true);
        try {
            const [i, p, b] = await Promise.all([
                ConciliacaoService.itens(c.id),
                ConciliacaoService.pendentes(c.id),
                ConciliacaoService.baixas(c.id)
            ]);
            const unwrap = x => x?.data?.data ?? x?.data ?? [];
            setItens(Array.isArray(unwrap(i)) ? unwrap(i) : []);
            setPendentes(Array.isArray(unwrap(p)) ? unwrap(p) : []);
            setBaixas(Array.isArray(unwrap(b)) ? unwrap(b) : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e?.response?.data?.message || 'Falha ao carregar conciliação', life: 3000 });
        } finally { setLoading(false); }
    };

    useEffect(() => { carregarContas(); carregar(); }, []);

    const criar = async () => {
        setErro('');
        if (!form.contaBancariaId || !form.dataInicio || !form.dataFim) {
            setErro('Informe conta e período.');
            return;
        }
        setLoading(true);
        try {
            const r = await ConciliacaoService.criar({ contaBancariaId: form.contaBancariaId, dataInicio: iso(form.dataInicio), dataFim: iso(form.dataFim) });
            const c = r?.data?.data ?? r?.data;
            setDialog(false);
            await carregar();
            if (c?.id) await selecionar(c);
        } catch (e) {
            setErro(e?.response?.data?.message || e?.response?.data?.error || 'Não foi possível criar a conciliação.');
        } finally { setLoading(false); }
    };

    const vincular = async (extratoId) => {
        setLoading(true);
        try {
            await ConciliacaoService.vincular(selecionada.id, { extratoId, baixaId: baixaSelecionada[extratoId] || null });
            toast.current?.show({ severity: 'success', summary: 'Movimento conciliado', life: 2000 });
            await selecionar(selecionada);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e?.response?.data?.message || 'Não foi possível conciliar', life: 3000 });
        } finally { setLoading(false); }
    };

    const conciliarAutomatico = async () => {
        setLoading(true);
        try {
            const r = await ConciliacaoService.conciliarAutomatico(selecionada.id);
            toast.current?.show({ severity: 'success', summary: 'Conciliacao automatica', detail: (r?.data?.vinculados ?? 0) + ' vinculados, ' + (r?.data?.semMatch ?? 0) + ' sem match', life: 4000 });
            await selecionar(selecionada);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    };
        const fechar = async () => {
        setLoading(true);
        try {
            await ConciliacaoService.fechar(selecionada.id);
            toast.current?.show({ severity: 'success', summary: 'Conciliação fechada', life: 2000 });
            await carregar();
            await selecionar({ ...selecionada, status: 'FECHADA' });
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e?.response?.data?.message || 'Não foi possível fechar', life: 3000 });
        } finally { setLoading(false); }
    };

    const contaOpts = contas.map(c => ({ label: contaLabel(c), value: c.id }));
    const baixaOpts = (extrato) => baixas.map(b => ({
        label: `#${b.id} • ${brDate(b.dataBaixa)} • Título #${b.tituloId} • ${money(b.valorBaixa)}`,
        value: b.id
    }));

    return <div>
        <Toast ref={toast} />
        <Card title="Conciliação Bancária">
            <div className="flex flex-wrap gap-2 mb-3">
                <Button label="Nova conciliação" icon="pi pi-plus" onClick={() => { setErro(''); setDialog(true); }} />
                <Button label="Atualizar" icon="pi pi-refresh" outlined onClick={carregar} />
                <Button label="Conciliar automatico" icon="pi pi-bolt" outlined onClick={conciliarAutomatico} disabled={!selecionada || selecionada.status !== 'EM_ABERTO'} />
            </div>
            <DataTable value={conciliacoes} loading={loading} paginator rows={10} selection={selecionada} onSelectionChange={e => e.value && selecionar(e.value)} dataKey="id" emptyMessage="Nenhuma conciliação criada">
                <Column selectionMode="single" style={{ width: '3rem' }} />
                <Column field="contaBancariaId" header="Conta" />
                <Column field="dataInicio" header="Início" body={r => brDate(r.dataInicio)} />
                <Column field="dataFim" header="Fim" body={r => brDate(r.dataFim)} />
                <Column field="status" header="Status" body={r => <Tag value={r.status} severity={r.status === 'FECHADA' ? 'success' : 'warning'} />} />
            </DataTable>
        </Card>

        {selecionada && <Card title={`Conciliação #${selecionada.id}`} className="mt-3">
            <div className="flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
                <div><strong>Período:</strong> {brDate(selecionada.dataInicio)} até {brDate(selecionada.dataFim)} &nbsp; <Tag value={selecionada.status} /></div>
                <Button label="Fechar conciliação" icon="pi pi-lock" severity="success" disabled={selecionada.status !== 'EM_ABERTO'} onClick={fechar} />
            </div>
            <DataTable value={pendentes} loading={loading} paginator rows={10} emptyMessage="Nenhum movimento pendente no período">
                <Column field="dataMovimento" header="Data" body={r => brDate(r.dataMovimento)} />
                <Column field="descricao" header="Descrição" />
                <Column field="tipo" header="Tipo" />
                <Column field="valor" header="Valor" body={r => money(r.valor)} />
                <Column header="Baixa relacionada" body={r => <Dropdown value={baixaSelecionada[r.id] || null} options={baixaOpts(r)} onChange={e => setBaixaSelecionada(s => ({ ...s, [r.id]: e.value }))} placeholder="Opcional" filter className="w-full" disabled={selecionada.status !== 'EM_ABERTO'} />} />
                <Column header="Ação" body={r => <Button icon="pi pi-check" label="Conciliar" size="small" onClick={() => vincular(r.id)} disabled={selecionada.status !== 'EM_ABERTO'} />} />
            </DataTable>
            <div className="mt-3"><strong>Pendentes:</strong> {pendentes.length} &nbsp; <strong>Conciliados nesta sessão:</strong> {itens.length}</div>
        </Card>}

        <Dialog header="Nova conciliação bancária" visible={dialog} style={{ width: '500px' }} onHide={() => setDialog(false)} footer={<><Button label="Cancelar" text onClick={() => setDialog(false)} /><Button label="Criar" icon="pi pi-check" onClick={criar} loading={loading} /></>}>
            {erro && <Message severity="error" text={erro} className="w-full mb-3" />}
            <div className="p-fluid">
                <div className="field"><label className="font-bold">Conta *</label><Dropdown value={form.contaBancariaId} options={contaOpts} onChange={e => setForm({ ...form, contaBancariaId: e.value })} filter /></div>
                <div className="field"><label className="font-bold">Data inicial *</label><Calendar value={form.dataInicio} onChange={e => setForm({ ...form, dataInicio: e.value })} dateFormat="dd/mm/yy" showIcon /></div>
                <div className="field"><label className="font-bold">Data final *</label><Calendar value={form.dataFim} onChange={e => setForm({ ...form, dataFim: e.value })} dateFormat="dd/mm/yy" showIcon /></div>
            </div>
        </Dialog>
    </div>;
}
