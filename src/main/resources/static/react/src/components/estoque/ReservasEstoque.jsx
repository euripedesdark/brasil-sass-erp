import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { Card } from 'primereact/card';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { AutoComplete } from 'primereact/autocomplete';
import ProdutoService from '../../services/ProdutoService';

const msg = (e) =>
  e?.response?.data?.errors?.[0]?.message ||
  e?.response?.data?.message ||
  e?.response?.data?.error ||
  e?.message ||
  'Operação não realizada';

const STATUS_FILTRO = [
  { label: 'Ativas (reservada/separação)', value: 'ATIVAS' },
  { label: 'Reservada', value: 'RESERVADA' },
  { label: 'Separação', value: 'SEPARACAO' },
  { label: 'Liberada', value: 'LIBERADA' },
  { label: 'Consumida', value: 'CONSUMIDA' },
  { label: 'Cancelada', value: 'CANCELADA' },
  { label: 'Todas', value: '' },
];

const severity = (s) => {
  switch (s) {
    case 'RESERVADA': return 'info';
    case 'SEPARACAO': return 'warning';
    case 'LIBERADA': return 'success';
    case 'CONSUMIDA': return 'success';
    case 'CANCELADA': return 'danger';
    default: return 'secondary';
  }
};

export default function ReservasEstoque() {
  const { t } = useTranslation();
  const toast = useRef(null);
  const [depositos, setDepositos] = useState([]);
  const [depositoId, setDepositoId] = useState(null);
  const [produtoId, setProdutoId] = useState('');
  const [produto, setProduto] = useState(null);
  const [produtos, setProdutos] = useState([]);
  const [pedidoVendaId, setPedidoVendaId] = useState('');
  const [loteId, setLoteId] = useState(null);
  const [lotes, setLotes] = useState([]);
  const [enderecoId, setEnderecoId] = useState(null);
  const [enderecos, setEnderecos] = useState([]);
  const [enderecosDisponiveis, setEnderecosDisponiveis] = useState([]);
  const [quantidade, setQuantidade] = useState(1);
  const [rows, setRows] = useState([]);
  const [statusFiltro, setStatusFiltro] = useState('ATIVAS');
  const [error, setError] = useState('');
  const [processando, setProcessando] = useState(false);

  const load = async () => {
    try {
      setError('');
      const r = await axios.get('/api/estoque/reservas');
      let data = r.data?.data ?? r.data ?? [];
      if (statusFiltro === 'ATIVAS') {
        data = data.filter((x) => ['RESERVADA', 'SEPARACAO'].includes(x.status));
      } else if (statusFiltro) {
        data = data.filter((x) => x.status === statusFiltro);
      }
      setRows(data);
    } catch (e) {
      setError(msg(e));
    }
  };

  useEffect(() => {
    axios.get('/api/estoque/depositos').then((r) => {
      const d = r.data?.data ?? r.data ?? [];
      setDepositos(d);
      if (d?.length) setDepositoId(d[0].id);
    }).catch(() => {});
  }, []);

  useEffect(() => { load(); }, [statusFiltro]);

  useEffect(() => {
    if (!depositoId) { setEnderecos([]); return; }
    axios.get('/api/estoque/enderecos', { params: { depositoId } })
      .then((r) => setEnderecos(r.data?.data ?? r.data ?? []))
      .catch(() => setEnderecos([]));
  }, [depositoId]);

  const carregarEnderecos = async (id, lote) => {
    if (!id || !depositoId) { setEnderecosDisponiveis([]); return; }
    try {
      const r = await axios.get('/api/estoque/reservas/picking-sugestoes', {
        params: { depositoId, produtoId: id, ...(lote ? { loteId: lote } : {}) },
      });
      const ids = new Set((r.data || []).filter((x) => Number(x.quantidade) > 0).map((x) => x.enderecoId));
      setEnderecosDisponiveis(enderecos.filter((e) => ids.has(e.id)));
    } catch {
      setEnderecosDisponiveis([]);
    }
  };

  useEffect(() => {
    if (produtoId) carregarEnderecos(produtoId, loteId);
  }, [produtoId, loteId, depositoId, enderecos]);

  const carregarLotes = async (id) => {
    if (!id) {
      setLotes([]); setLoteId(null); setEnderecoId(null); setEnderecosDisponiveis([]);
      return;
    }
    try {
      const r = await axios.get('/api/estoque/lotes', { params: { produtoId: id, depositoId, status: 'ATIVO' } });
      const data = (r.data?.data ?? r.data ?? []).filter((x) => (x.status || 'ATIVO') === 'ATIVO');
      setLotes(data);
    } catch {
      setLotes([]);
    }
  };

  const buscarProdutos = async (e) => {
    try {
      const r = await ProdutoService.buscarPorNome((e.query || '').trim(), 0, 20);
      const d = r?.data?.data?.content ?? r?.data?.content ?? r?.data?.data ?? r?.data ?? [];
      setProdutos(Array.isArray(d) ? d : []);
    } catch {
      setProdutos([]);
    }
  };

  const save = async () => {
    if (!depositoId || !produtoId) return;
    setProcessando(true);
    setError('');
    try {
      await axios.post('/api/estoque/reservas', {
        depositoId,
        produtoId: Number(produtoId),
        pedidoVendaId: pedidoVendaId ? Number(pedidoVendaId) : null,
        loteId: loteId || null,
        enderecoId: enderecoId || null,
        quantidade,
      });
      toast.current?.show({ severity: 'success', summary: 'Reserva criada', life: 2500 });
      setPedidoVendaId('');
      setQuantidade(1);
      setLoteId(null);
      setEnderecoId(null);
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const action = async (id, path, label) => {
    setProcessando(true);
    setError('');
    try {
      await axios.post(`/api/estoque/reservas/${id}/${path}`);
      toast.current?.show({ severity: 'success', summary: label || path, life: 2500 });
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const acoes = (r) => (
    <div className="flex gap-1 flex-wrap">
      <Button
        icon="pi pi-box"
        text
        rounded
        tooltip={t('legacyUi.reservas.separate') || 'Separar'}
        disabled={processando || r.status !== 'RESERVADA'}
        onClick={() => action(r.id, 'separar', 'Em separação')}
      />
      <Button
        icon="pi pi-unlock"
        text
        rounded
        tooltip={t('legacyUi.reservas.release') || 'Liberar'}
        disabled={processando || !['RESERVADA', 'SEPARACAO'].includes(r.status)}
        onClick={() => action(r.id, 'liberar', 'Reserva liberada')}
      />
      <Button
        icon="pi pi-times"
        text
        rounded
        severity="danger"
        tooltip="Cancelar"
        disabled={processando || r.status !== 'RESERVADA'}
        onClick={() => action(r.id, 'cancelar', 'Reserva cancelada')}
      />
    </div>
  );

  return (
    <div>
      <Toast ref={toast} />
      <Card title={t('legacyUi.reservas.title') || 'Reservas de estoque'}>
        <div className="p-fluid grid">
          <div className="field col-12 md:col-2">
            <label>Depósito</label>
            <Dropdown value={depositoId} options={depositos} optionLabel="nome" optionValue="id" onChange={(e) => setDepositoId(e.value)} />
          </div>
          <div className="field col-12 md:col-3">
            <label>{t('legacyUi.reservas.product') || 'Produto'}</label>
            <AutoComplete
              value={produto}
              suggestions={produtos}
              completeMethod={buscarProdutos}
              field="nome"
              itemTemplate={(p) => (
                <div>
                  <strong>{p.nome || p.descricao}</strong>
                  <small className="ml-2">{p.codigo || p.id}</small>
                </div>
              )}
              selectedItemTemplate={(p) => (p ? `${p.nome || p.descricao} · ${p.codigo || p.id}` : '')}
              onChange={(e) => {
                setProduto(e.value);
                setProdutoId(e.value?.id || '');
                carregarLotes(e.value?.id);
              }}
              placeholder="Pesquisar produto"
            />
          </div>
          <div className="field col-12 md:col-2">
            <label>{t('legacyUi.reservas.lot') || 'Lote'}</label>
            <Dropdown value={loteId} options={lotes} optionLabel="codigo" optionValue="id" showClear onChange={(e) => setLoteId(e.value)} placeholder="Somente ATIVO" />
          </div>
          <div className="field col-12 md:col-2">
            <label>{t('legacyUi.reservas.address') || 'Endereço'}</label>
            <Dropdown value={enderecoId} options={enderecosDisponiveis} optionLabel="codigo" optionValue="id" showClear onChange={(e) => setEnderecoId(e.value)} placeholder="Sugestão picking" />
          </div>
          <div className="field col-12 md:col-2">
            <label>{t('legacyUi.reservas.order') || 'Pedido venda'}</label>
            <InputText value={pedidoVendaId} onChange={(e) => setPedidoVendaId(e.target.value)} />
          </div>
          <div className="field col-12 md:col-1">
            <label>{t('legacyUi.reservas.quantity') || 'Qtd'}</label>
            <InputNumber value={quantidade} onValueChange={(e) => setQuantidade(e.value || 1)} minFractionDigits={3} min={0.001} />
          </div>
          <div className="field col-12 md:col-2">
            <label>Filtro status</label>
            <Dropdown value={statusFiltro} options={STATUS_FILTRO} onChange={(e) => setStatusFiltro(e.value)} />
          </div>
          <div className="col-12 md:col-2 flex align-items-end">
            <Button label={t('legacyUi.reservas.save') || 'Reservar'} icon="pi pi-lock" onClick={save} loading={processando} disabled={!depositoId || !produtoId || !(quantidade > 0)} />
          </div>
        </div>
        {error && <Message severity="error" text={error} className="mt-2" />}
        <DataTable value={rows} className="mt-3" paginator rows={12} stripedRows emptyMessage="Nenhuma reserva">
          <Column field="id" header="#" style={{ width: '4rem' }} />
          <Column field="pedidoVendaId" header="Pedido" />
          <Column field="produtoId" header="Produto" />
          <Column field="loteId" header="Lote" />
          <Column field="enderecoId" header="Endereço" />
          <Column field="depositoId" header="Depósito" />
          <Column field="quantidade" header="Qtd." />
          <Column header="Status" body={(r) => <Tag value={r.status || '—'} severity={severity(r.status)} />} />
          <Column field="dataReserva" header="Reserva em" />
          <Column header="Ações" body={acoes} style={{ minWidth: '9rem' }} />
        </DataTable>
      </Card>
    </div>
  );
}
