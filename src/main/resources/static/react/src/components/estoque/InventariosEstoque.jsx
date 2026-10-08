import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dropdown } from 'primereact/dropdown';
import { AutoComplete } from 'primereact/autocomplete';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import ProdutoService from '../../services/ProdutoService';

const msg = (e) =>
  e?.response?.data?.errors?.[0]?.message ||
  e?.response?.data?.message ||
  e?.response?.data?.error ||
  e?.message ||
  'Operação não realizada';

const STATUS_FILTRO = [
  { label: 'Abertos', value: 'ABERTO' },
  { label: 'Fechados', value: 'FECHADO' },
  { label: 'Cancelados', value: 'CANCELADO' },
  { label: 'Todos', value: '' },
];

const severity = (s) => {
  switch (s) {
    case 'ABERTO': return 'info';
    case 'FECHADO': return 'success';
    case 'CANCELADO': return 'danger';
    default: return 'secondary';
  }
};

export default function InventariosEstoque() {
  const { t } = useTranslation();
  const toast = useRef(null);
  const [depositos, setDepositos] = useState([]);
  const [rows, setRows] = useState([]);
  const [inv, setInv] = useState(null);
  const [itens, setItens] = useState([]);
  const [produto, setProduto] = useState(null);
  const [sug, setSug] = useState([]);
  const [qtd, setQtd] = useState(0);
  const [obs, setObs] = useState('');
  const [enderecoId, setEnderecoId] = useState(null);
  const [enderecos, setEnderecos] = useState([]);
  const [loteId, setLoteId] = useState(null);
  const [lotes, setLotes] = useState([]);
  const [statusFiltro, setStatusFiltro] = useState('ABERTO');
  const [error, setError] = useState('');
  const [processando, setProcessando] = useState(false);
  const [depositoNovo, setDepositoNovo] = useState(null);

  const load = async () => {
    try {
      setError('');
      const [d, i] = await Promise.all([
        axios.get('/api/estoque/depositos'),
        axios.get('/api/estoque/inventarios'),
      ]);
      setDepositos(d.data?.data ?? d.data ?? []);
      let data = i.data?.data ?? i.data ?? [];
      if (statusFiltro) data = data.filter((x) => x.status === statusFiltro);
      setRows(data);
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    }
  };

  useEffect(() => { load(); }, [statusFiltro]);

  useEffect(() => {
    if (!inv?.depositoId) return;
    axios.get('/api/estoque/enderecos', { params: { depositoId: inv.depositoId } })
      .then((r) => setEnderecos(r.data?.data ?? r.data ?? []))
      .catch(() => setEnderecos([]));
  }, [inv]);

  useEffect(() => {
    if (!inv || !produto?.id) { setLotes([]); return; }
    axios.get('/api/estoque/lotes', { params: { produtoId: produto.id, depositoId: inv.depositoId, status: 'ATIVO' } })
      .then((r) => setLotes((r.data?.data ?? r.data ?? []).filter((x) => (x.status || 'ATIVO') === 'ATIVO')))
      .catch(() => setLotes([]));
  }, [inv, produto]);

  const search = async (e) => {
    if (!e.query) return setSug([]);
    try {
      const r = await ProdutoService.buscarPorNome(e.query, 0, 20);
      const d = r.data?.data?.content ?? r.data?.content ?? r.data?.data ?? r.data ?? [];
      setSug(Array.isArray(d) ? d : []);
    } catch {
      setSug([]);
    }
  };

  const criar = async () => {
    const dep = depositoNovo || inv?.depositoId;
    if (!dep) {
      toast.current?.show({ severity: 'warn', summary: 'Inventário', detail: t('legacyUi.inventarios.warehouseRequired') || 'Selecione o depósito', life: 3000 });
      return;
    }
    setProcessando(true);
    try {
      const r = await axios.post('/api/estoque/inventarios', { depositoId: dep, observacoes: obs });
      setInv(r.data?.data ?? r.data);
      setItens([]);
      setObs('');
      setDepositoNovo(null);
      toast.current?.show({ severity: 'success', summary: 'Inventário aberto', life: 2500 });
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const selecionar = async (row) => {
    if (!row) return;
    setInv(row);
    setProduto(null);
    setLoteId(null);
    setEnderecoId(null);
    setQtd(0);
    try {
      const r = await axios.get(`/api/estoque/inventarios/${row.id}/itens`);
      setItens(r.data?.data ?? r.data ?? []);
    } catch (e) {
      setItens([]);
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    }
  };

  const contar = async () => {
    if (!inv?.id || !produto?.id) return;
    setProcessando(true);
    try {
      await axios.post(`/api/estoque/inventarios/${inv.id}/contagens`, {
        produtoId: produto.id,
        loteId: loteId || null,
        enderecoId: enderecoId || null,
        quantidadeContada: qtd,
      });
      toast.current?.show({ severity: 'success', summary: 'Contagem registrada', life: 2500 });
      setProduto(null);
      setLoteId(null);
      setEnderecoId(null);
      setQtd(0);
      const r = await axios.get(`/api/estoque/inventarios/${inv.id}/itens`);
      setItens(r.data?.data ?? r.data ?? []);
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const fechar = async () => {
    if (!inv?.id) return;
    setProcessando(true);
    try {
      const r = await axios.post(`/api/estoque/inventarios/${inv.id}/fechar`);
      setInv(r.data?.data ?? r.data);
      toast.current?.show({ severity: 'success', summary: 'Inventário fechado — ajustes aplicados', life: 3500 });
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const cancelar = async () => {
    if (!inv?.id) return;
    setProcessando(true);
    try {
      const r = await axios.post(`/api/estoque/inventarios/${inv.id}/cancelar`);
      setInv(r.data?.data ?? r.data);
      toast.current?.show({ severity: 'success', summary: 'Inventário cancelado', life: 2500 });
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const diffBody = (row) => {
    const d = Number(row.diferenca || 0);
    const color = d > 0 ? 'var(--green-600)' : d < 0 ? 'var(--red-600)' : undefined;
    return <span style={{ color, fontWeight: d !== 0 ? 600 : 400 }}>{row.diferenca}</span>;
  };

  return (
    <div>
      <Toast ref={toast} />
      <Card title={t('legacyUi.inventarios.title') || 'Inventários'}>
        <div className="grid p-fluid">
          <div className="col-12 md:col-3 field">
            <label>Depósito</label>
            <Dropdown
              value={depositoNovo}
              options={depositos.map((d) => ({ label: `${d.codigo || ''} — ${d.nome}`, value: d.id }))}
              onChange={(e) => setDepositoNovo(e.value)}
              placeholder="Selecione"
            />
          </div>
          <div className="col-12 md:col-3 field">
            <label>Observações</label>
            <InputTextarea value={obs} rows={1} onChange={(e) => setObs(e.target.value)} />
          </div>
          <div className="col-12 md:col-3 field">
            <label>Filtro status</label>
            <Dropdown value={statusFiltro} options={STATUS_FILTRO} onChange={(e) => setStatusFiltro(e.value)} />
          </div>
          <div className="col-12 md:col-3 field">
            <label>&nbsp;</label>
            <Button label={t('legacyUi.inventarios.open') || 'Abrir inventário'} icon="pi pi-plus" onClick={criar} loading={processando} disabled={!depositoNovo} />
          </div>
        </div>
        {error && <Message severity="error" text={error} className="mb-2" />}
        <DataTable
          value={rows}
          size="small"
          paginator
          rows={8}
          selectionMode="single"
          selection={inv}
          onSelectionChange={(e) => selecionar(e.value)}
          emptyMessage="Nenhum inventário"
        >
          <Column field="id" header="#" style={{ width: '4rem' }} />
          <Column field="depositoId" header="Depósito" />
          <Column header="Status" body={(r) => <Tag value={r.status || '—'} severity={severity(r.status)} />} />
          <Column field="dataContagem" header="Data" />
          <Column field="observacoes" header="Obs." />
        </DataTable>

        {inv && (
          <Card title={`Contagem #${inv.id} — ${inv.status}`} className="mt-3">
            <div className="grid p-fluid">
              <div className="col-12 md:col-4 field">
                <label>Produto *</label>
                <AutoComplete
                  value={produto}
                  suggestions={sug}
                  completeMethod={search}
                  field="nome"
                  onChange={(e) => setProduto(e.value)}
                  placeholder="Nome ou código"
                  disabled={inv.status !== 'ABERTO'}
                />
              </div>
              <div className="col-12 md:col-2 field">
                <label>Lote</label>
                <Dropdown value={loteId} options={lotes} optionLabel="codigo" optionValue="id" showClear onChange={(e) => setLoteId(e.value)} placeholder="Opcional" disabled={inv.status !== 'ABERTO'} />
              </div>
              <div className="col-12 md:col-2 field">
                <label>Endereço</label>
                <Dropdown value={enderecoId} options={enderecos.map((e) => ({ label: e.codigo, value: e.id }))} showClear onChange={(e) => setEnderecoId(e.value)} placeholder="Opcional" disabled={inv.status !== 'ABERTO'} />
              </div>
              <div className="col-12 md:col-2 field">
                <label>Qtd contada</label>
                <InputNumber value={qtd} min={0} minFractionDigits={3} onValueChange={(e) => setQtd(e.value || 0)} disabled={inv.status !== 'ABERTO'} />
              </div>
              <div className="col-12 md:col-2 field">
                <label>&nbsp;</label>
                <Button label="Registrar" onClick={contar} loading={processando} disabled={inv.status !== 'ABERTO' || !produto?.id} />
              </div>
            </div>
            <DataTable value={itens} size="small" emptyMessage="Sem contagens">
              <Column field="produtoId" header="Produto" />
              <Column field="loteId" header="Lote" />
              <Column field="enderecoId" header="Endereço" />
              <Column field="quantidadeSistema" header="Sistema" />
              <Column field="quantidadeContada" header="Contado" />
              <Column header="Diferença" body={diffBody} />
            </DataTable>
            <div className="mt-3 flex gap-2 flex-wrap">
              <Button
                label={t('legacyUi.inventarios.close') || 'Fechar e ajustar'}
                icon="pi pi-check"
                severity="warning"
                onClick={fechar}
                loading={processando}
                disabled={inv.status !== 'ABERTO' || !itens.length}
              />
              <Button
                label="Cancelar inventário"
                icon="pi pi-times"
                severity="danger"
                outlined
                onClick={cancelar}
                loading={processando}
                disabled={inv.status !== 'ABERTO'}
              />
            </div>
          </Card>
        )}
      </Card>
    </div>
  );
}
