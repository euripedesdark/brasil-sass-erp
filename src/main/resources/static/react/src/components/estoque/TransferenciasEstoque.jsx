import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
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
import { Checkbox } from 'primereact/checkbox';
import axios from 'axios';
import ProdutoService from '../../services/ProdutoService';

const msg = (e) =>
  e?.response?.data?.errors?.[0]?.message ||
  e?.response?.data?.message ||
  e?.response?.data?.error ||
  e?.message ||
  'Operação não realizada';

export default function TransferenciasEstoque() {
  const { t } = useTranslation();
  const toast = useRef(null);
  const [modoInterno, setModoInterno] = useState(false);
  const [endOrigem, setEndOrigem] = useState(null);
  const [endDestino, setEndDestino] = useState(null);
  const [enderecos, setEnderecos] = useState([]);
  const [loteId, setLoteId] = useState(null);
  const [lotes, setLotes] = useState([]);
  const [depositos, setDepositos] = useState([]);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [processando, setProcessando] = useState(false);
  const [origem, setOrigem] = useState(null);
  const [destino, setDestino] = useState(null);
  const [produto, setProduto] = useState(null);
  const [sug, setSug] = useState([]);
  const [qtd, setQtd] = useState(0);
  const [itens, setItens] = useState([]);
  const [obs, setObs] = useState('');
  const [error, setError] = useState('');
  const [itensHist, setItensHist] = useState({});
  const [expanded, setExpanded] = useState(null);

  const dep = (id) => {
    const d = depositos.find((x) => x.id === id);
    return d ? `${d.codigo} — ${d.nome}` : id;
  };

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [d, tr] = await Promise.all([
        axios.get('/api/estoque/depositos'),
        axios.get('/api/estoque/transferencias'),
      ]);
      setDepositos(d.data?.data ?? d.data ?? []);
      setRows(tr.data?.data ?? tr.data ?? []);
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  useEffect(() => {
    if (modoInterno && origem) {
      axios.get('/api/estoque/enderecos', { params: { depositoId: origem } })
        .then((r) => setEnderecos(r.data?.data ?? r.data ?? []))
        .catch(() => setEnderecos([]));
    } else {
      setEnderecos([]);
      setEndOrigem(null);
      setEndDestino(null);
    }
  }, [modoInterno, origem]);

  useEffect(() => {
    if (modoInterno && produto?.id && origem) {
      axios.get('/api/estoque/lotes', { params: { produtoId: produto.id, depositoId: origem, status: 'ATIVO' } })
        .then((r) => setLotes((r.data?.data ?? r.data ?? []).filter((x) => (x.status || 'ATIVO') === 'ATIVO')))
        .catch(() => setLotes([]));
    } else {
      setLotes([]);
      setLoteId(null);
    }
  }, [modoInterno, produto, origem]);

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

  const add = () => {
    if (!produto?.id || !qtd || qtd <= 0) {
      toast.current?.show({ severity: 'warn', summary: 'Item', detail: 'Informe produto e quantidade', life: 3000 });
      return;
    }
    setItens((x) => [
      ...x,
      {
        produtoId: produto.id,
        produtoNome: produto.nome || produto.codigo || String(produto.id),
        quantidade: qtd,
        loteId: modoInterno ? loteId : null,
      },
    ]);
    setProduto(null);
    setQtd(0);
    setLoteId(null);
  };

  const transfer = async () => {
    setProcessando(true);
    setError('');
    try {
      if (modoInterno) {
        if (!origem || !produto?.id || !endOrigem || !endDestino || !qtd) {
          throw new Error('Preencha depósito, produto, endereços e quantidade');
        }
        await axios.post('/api/estoque/transferencias/interna', {
          depositoId: origem,
          produtoId: produto.id,
          loteId: loteId || null,
          enderecoOrigemId: endOrigem,
          enderecoDestinoId: endDestino,
          quantidade: qtd,
          observacoes: obs || null,
        });
      } else {
        if (!origem || !destino || !itens.length) {
          throw new Error('Informe origem, destino e ao menos um item');
        }
        await axios.post('/api/estoque/transferencias', {
          depositoOrigemId: origem,
          depositoDestinoId: destino,
          itens: itens.map((i) => ({ produtoId: i.produtoId, quantidade: i.quantidade })),
          observacoes: obs || null,
        });
      }
      toast.current?.show({ severity: 'success', summary: 'Transferência concluída', life: 2500 });
      setItens([]);
      setObs('');
      setQtd(0);
      setProduto(null);
      await load();
    } catch (e) {
      const m = msg(e);
      setError(m);
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: m, life: 5000 });
    } finally {
      setProcessando(false);
    }
  };

  const verItens = async (id) => {
    try {
      const r = await axios.get(`/api/estoque/transferencias/${id}/itens`);
      setItensHist((x) => ({ ...x, [id]: r.data?.data ?? r.data ?? [] }));
      setExpanded(id);
    } catch (e) {
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    }
  };

  const depOpts = depositos.map((d) => ({ label: `${d.codigo} — ${d.nome}`, value: d.id }));

  return (
    <div>
      <Toast ref={toast} />
      <Card title={t('legacyUi.transferencias.title') || 'Transferências de estoque'}>
        <div className="flex align-items-center gap-2 mb-3">
          <Checkbox inputId="modoInterno" checked={modoInterno} onChange={(e) => setModoInterno(e.checked)} />
          <label htmlFor="modoInterno">Transferência interna (mesmo depósito, entre endereços)</label>
        </div>
        {error && <Message severity="error" text={error} className="mb-2" />}
        <div className="grid p-fluid">
          <div className="col-12 md:col-4 field">
            <label>Depósito origem *</label>
            <Dropdown value={origem} options={depOpts} onChange={(e) => setOrigem(e.value)} placeholder="Selecione" />
          </div>
          {!modoInterno && (
            <div className="col-12 md:col-4 field">
              <label>Depósito destino *</label>
              <Dropdown value={destino} options={depOpts.filter((d) => d.value !== origem)} onChange={(e) => setDestino(e.value)} placeholder="Selecione" />
            </div>
          )}
          {modoInterno && (
            <>
              <div className="col-12 md:col-4 field">
                <label>Endereço origem *</label>
                <Dropdown value={endOrigem} options={enderecos.map((e) => ({ label: `${e.codigo} — ${e.descricao || ''}`, value: e.id }))} onChange={(e) => setEndOrigem(e.value)} placeholder="Selecione" />
              </div>
              <div className="col-12 md:col-4 field">
                <label>Endereço destino *</label>
                <Dropdown value={endDestino} options={enderecos.filter((e) => e.id !== endOrigem).map((e) => ({ label: `${e.codigo} — ${e.descricao || ''}`, value: e.id }))} onChange={(e) => setEndDestino(e.value)} placeholder="Selecione" />
              </div>
            </>
          )}
          <div className="col-12 md:col-4 field">
            <label>Produto {modoInterno ? '*' : ''}</label>
            <AutoComplete
              value={produto}
              suggestions={sug}
              completeMethod={search}
              field="nome"
              itemTemplate={(p) => <div><strong>{p.nome}</strong><small className="ml-2">{p.codigo || p.id}</small></div>}
              onChange={(e) => setProduto(e.value)}
              placeholder="Nome ou código"
            />
          </div>
          {modoInterno && (
            <div className="col-12 md:col-2 field">
              <label>Lote (ATIVO)</label>
              <Dropdown value={loteId} options={lotes} optionLabel="codigo" optionValue="id" showClear onChange={(e) => setLoteId(e.value)} placeholder="Opcional" />
            </div>
          )}
          <div className="col-12 md:col-2 field">
            <label>Quantidade</label>
            <InputNumber value={qtd} min={0} minFractionDigits={3} onValueChange={(e) => setQtd(e.value || 0)} />
          </div>
          {!modoInterno && (
            <div className="col-12 md:col-2 field">
              <label>&nbsp;</label>
              <Button label="Adicionar item" icon="pi pi-plus" onClick={add} />
            </div>
          )}
          <div className="col-12 field">
            <label>Observações</label>
            <InputTextarea value={obs} rows={2} onChange={(e) => setObs(e.target.value)} />
          </div>
          <div className="col-12 md:col-3 field">
            <Button
              label={t('legacyUi.transferencias.transfer') || 'Transferir'}
              icon="pi pi-arrow-right-arrow-left"
              loading={processando}
              onClick={transfer}
            />
          </div>
        </div>
        {!modoInterno && (
          <DataTable value={itens} size="small" emptyMessage="Nenhum item na cesta" className="mt-2">
            <Column field="produtoNome" header="Produto" />
            <Column field="quantidade" header="Qtd" />
            <Column body={(_, m) => <Button icon="pi pi-trash" className="p-button-text p-button-danger" onClick={() => setItens((x) => x.filter((__, i) => i !== m.rowIndex))} />} style={{ width: '60px' }} />
          </DataTable>
        )}
      </Card>

      <Card title={t('legacyUi.transferencias.history') || 'Histórico'} className="mt-3">
        <DataTable value={rows} loading={loading} paginator rows={10} size="small" emptyMessage="Sem transferências">
          <Column field="id" header="#" style={{ width: '4rem' }} />
          <Column header="Origem" body={(r) => dep(r.depositoOrigemId)} />
          <Column header="Destino" body={(r) => dep(r.depositoDestinoId)} />
          <Column header="Status" body={(r) => <Tag value={r.status || '—'} severity={r.status === 'CONCLUIDA' ? 'success' : 'info'} />} />
          <Column field="dataTransferencia" header="Data" />
          <Column header="Itens" body={(r) => <Button icon="pi pi-list" text rounded tooltip="Ver itens" onClick={() => verItens(r.id)} />} />
        </DataTable>
        {expanded && itensHist[expanded] && (
          <div className="mt-3">
            <h4>Itens da transferência #{expanded}</h4>
            <DataTable value={itensHist[expanded]} size="small" emptyMessage="Sem itens">
              <Column field="produtoId" header="Produto" />
              <Column field="loteId" header="Lote" />
              <Column field="quantidade" header="Qtd" />
              <Column field="enderecoOrigemId" header="End. origem" />
              <Column field="enderecoDestinoId" header="End. destino" />
            </DataTable>
          </div>
        )}
      </Card>
    </div>
  );
}
