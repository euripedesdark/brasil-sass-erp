import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const msg = (e) =>
  e?.response?.data?.errors?.[0]?.message ||
  e?.response?.data?.message ||
  e?.response?.data?.error ||
  e?.message ||
  'Operação não realizada';

const STATUS_FILTRO = [
  { label: 'Em aberto / operação', value: 'ATIVAS' },
  { label: 'Aberta', value: 'ABERTA' },
  { label: 'Separação', value: 'SEPARACAO' },
  { label: 'Embalagem', value: 'EMBALAGEM' },
  { label: 'Expedida', value: 'EXPEDIDA' },
  { label: 'Todas', value: '' },
];

const severity = (s) => {
  switch (s) {
    case 'ABERTA': return 'info';
    case 'SEPARACAO': return 'warning';
    case 'EMBALAGEM': return 'warning';
    case 'EXPEDIDA': return 'success';
    default: return 'secondary';
  }
};

export default function ExpedicoesEstoque() {
  const { t } = useTranslation();
  const toast = useRef(null);
  const [depositos, setDepositos] = useState([]);
  const [depositoId, setDepositoId] = useState(null);
  const [pedidoVendaId, setPedidoVendaId] = useState('');
  const [rows, setRows] = useState([]);
  const [items, setItems] = useState({});
  const [statusFiltro, setStatusFiltro] = useState('ATIVAS');
  const [error, setError] = useState('');
  const [processando, setProcessando] = useState(false);
  const [expanded, setExpanded] = useState(null);

  const load = async () => {
    try {
      setError('');
      const r = await axios.get('/api/estoque/expedicoes');
      let data = r.data?.data ?? r.data ?? [];
      if (statusFiltro === 'ATIVAS') {
        data = data.filter((x) => ['ABERTA', 'SEPARACAO', 'EMBALAGEM'].includes(x.status));
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

  const abrir = async () => {
    if (!pedidoVendaId || !depositoId) return;
    setProcessando(true);
    setError('');
    try {
      await axios.post('/api/estoque/expedicoes', {
        pedidoVendaId: Number(pedidoVendaId),
        depositoId,
      });
      toast.current?.show({ severity: 'success', summary: 'Expedição aberta', life: 2500 });
      setPedidoVendaId('');
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const showItems = async (id) => {
    try {
      const r = await axios.get(`/api/estoque/expedicoes/${id}/itens`);
      setItems((x) => ({ ...x, [id]: r.data?.data ?? r.data ?? [] }));
      setExpanded(id);
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    }
  };

  const action = async (id, path, label) => {
    setProcessando(true);
    setError('');
    try {
      await axios.post(`/api/estoque/expedicoes/${id}/${path}`);
      toast.current?.show({ severity: 'success', summary: label || path, life: 2500 });
      await load();
      await showItems(id);
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const actions = (r) => (
    <div className="flex gap-1 flex-wrap">
      <Button icon="pi pi-box" text rounded tooltip="Separar" disabled={processando || r.status !== 'ABERTA'} onClick={() => action(r.id, 'separar', 'Em separação')} />
      <Button icon="pi pi-inbox" text rounded tooltip="Embalar" disabled={processando || r.status !== 'SEPARACAO'} onClick={() => action(r.id, 'embalar', 'Em embalagem')} />
      <Button icon="pi pi-send" text rounded tooltip="Expedir" disabled={processando || r.status !== 'EMBALAGEM'} onClick={() => action(r.id, 'expedir', 'Expedida')} />
      <Button icon="pi pi-list" text rounded tooltip="Ver itens" onClick={() => showItems(r.id)} />
    </div>
  );

  return (
    <div>
      <Toast ref={toast} />
      <Card title={t('legacyUi.expedicoes.title') || 'Expedições'}>
        <div className="p-fluid grid">
          <div className="field col-12 md:col-3">
            <label>Pedido de venda ID</label>
            <InputText value={pedidoVendaId} onChange={(e) => setPedidoVendaId(e.target.value)} />
          </div>
          <div className="field col-12 md:col-3">
            <label>Depósito</label>
            <Dropdown value={depositoId} options={depositos} optionLabel="nome" optionValue="id" onChange={(e) => setDepositoId(e.value)} />
          </div>
          <div className="field col-12 md:col-3">
            <label>Filtro status</label>
            <Dropdown value={statusFiltro} options={STATUS_FILTRO} onChange={(e) => setStatusFiltro(e.value)} />
          </div>
          <div className="col-12 md:col-3 flex align-items-end">
            <Button label={t('legacyUi.expedicoes.ship') || 'Abrir expedição'} icon="pi pi-plus" onClick={abrir} loading={processando} disabled={!pedidoVendaId || !depositoId} />
          </div>
        </div>
        {error && <Message severity="error" text={error} className="mt-2" />}
        <DataTable value={rows} paginator rows={10} stripedRows className="mt-3" emptyMessage="Nenhuma expedição">
          <Column field="id" header="#" style={{ width: '4rem' }} />
          <Column field="pedidoVendaId" header="Pedido" />
          <Column field="depositoId" header="Depósito" />
          <Column header="Status" body={(r) => <Tag value={r.status || '—'} severity={severity(r.status)} />} />
          <Column field="dataAbertura" header="Abertura" />
          <Column field="dataExpedicao" header="Expedida em" />
          <Column header="Fluxo" body={actions} style={{ minWidth: '11rem' }} />
        </DataTable>
        {expanded && items[expanded] && (
          <div className="mt-3">
            <h4>Itens da expedição #{expanded}</h4>
            <DataTable value={items[expanded]} size="small" emptyMessage="Sem itens">
              <Column field="produtoId" header="Produto" />
              <Column field="loteId" header="Lote" />
              <Column field="quantidade" header="Quantidade" />
              <Column header="Status" body={(i) => <Tag value={i.status || '—'} severity={severity(i.status)} />} />
            </DataTable>
          </div>
        )}
      </Card>
    </div>
  );
}
