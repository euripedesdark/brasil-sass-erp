import { useTranslation } from 'react-i18next';
import React, { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'primereact/dropdown';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { AutoComplete } from 'primereact/autocomplete';
import ProdutoService from '../../services/ProdutoService';

const msg = (e) =>
  e?.response?.data?.errors?.[0]?.message ||
  e?.response?.data?.message ||
  e?.response?.data?.error ||
  e?.message ||
  'Operação não realizada';

const STATUS_OPTS = [
  { label: 'Ativos', value: 'ATIVO' },
  { label: 'Quarentena', value: 'QUARENTENA' },
  { label: 'Bloqueados', value: 'BLOQUEADO' },
  { label: 'Vencidos', value: 'VENCIDO' },
  { label: 'Inativos', value: 'INATIVO' },
  { label: 'Todos', value: '' },
];

const severityStatus = (s) => {
  switch (s) {
    case 'ATIVO': return 'success';
    case 'QUARENTENA': return 'warning';
    case 'BLOQUEADO': return 'danger';
    case 'VENCIDO': return 'danger';
    default: return 'secondary';
  }
};

export default function LotesEstoque() {
  const { t } = useTranslation();
  const toast = useRef(null);
  const [produtoId, setProdutoId] = useState('');
  const [produto, setProduto] = useState(null);
  const [produtos, setProdutos] = useState([]);
  const [codigo, setCodigo] = useState('');
  const [quantidade, setQuantidade] = useState(0);
  const [fabricacao, setFabricacao] = useState(null);
  const [validade, setValidade] = useState(null);
  const [depositos, setDepositos] = useState([]);
  const [depositoId, setDepositoId] = useState(null);
  const [rows, setRows] = useState([]);
  const [statusFiltro, setStatusFiltro] = useState('ATIVO');
  const [error, setError] = useState('');
  const [editando, setEditando] = useState(null);
  const [processando, setProcessando] = useState(false);

  const load = async () => {
    try {
      setError('');
      const params = {};
      if (statusFiltro) params.status = statusFiltro;
      if (depositoId) params.depositoId = depositoId;
      const r = await axios.get('/api/estoque/lotes', { params });
      let data = r.data?.data ?? r.data ?? [];
      if (statusFiltro) data = data.filter((x) => (x.status || 'ATIVO') === statusFiltro);
      setRows(data);
    } catch (e) {
      setError(msg(e));
    }
  };

  useEffect(() => {
    axios.get('/api/estoque/depositos').then((r) => setDepositos(r.data?.data ?? r.data ?? [])).catch(() => {});
  }, []);

  useEffect(() => {
    load();
  }, [statusFiltro, depositoId]);

  const buscarProdutos = async (e) => {
    const q = (e.query || '').trim();
    if (!q) return setProdutos([]);
    try {
      const r = await ProdutoService.buscarPorNome(q, 0, 20);
      const d = r.data?.content ?? r.data?.data ?? r.data ?? [];
      setProdutos(Array.isArray(d) ? d : []);
    } catch {
      setProdutos([]);
    }
  };

  const limpar = () => {
    setEditando(null);
    setProdutoId('');
    setProduto(null);
    setCodigo('');
    setQuantidade(0);
    setFabricacao(null);
    setValidade(null);
  };

  const editar = (r) => {
    setEditando(r);
    setProdutoId(r.produtoId);
    setProduto({ id: r.produtoId, nome: String(r.produtoId) });
    setCodigo(r.codigo || '');
    setQuantidade(Number(r.quantidade) || 0);
    setFabricacao(r.dataFabricacao ? new Date(r.dataFabricacao + 'T00:00:00') : null);
    setValidade(r.dataValidade ? new Date(r.dataValidade + 'T00:00:00') : null);
    setDepositoId(r.depositoId || null);
  };

  const toIso = (d) => {
    if (!d) return null;
    const x = d instanceof Date ? d : new Date(d);
    if (Number.isNaN(x.getTime())) return null;
    return x.toISOString().slice(0, 10);
  };

  const salvar = async () => {
    if (!produtoId || !codigo) return;
    setProcessando(true);
    setError('');
    try {
      const body = {
        produtoId: Number(produtoId),
        codigo: codigo.trim(),
        quantidade,
        dataFabricacao: toIso(fabricacao),
        dataValidade: toIso(validade),
        depositoId: depositoId || null,
        status: editando?.status || 'ATIVO',
      };
      if (editando) {
        await axios.put('/api/estoque/lotes/' + editando.id, body);
        toast.current?.show({ severity: 'success', summary: 'Lote atualizado', life: 2500 });
      } else {
        await axios.post('/api/estoque/lotes', body);
        toast.current?.show({ severity: 'success', summary: 'Lote cadastrado', life: 2500 });
      }
      limpar();
      await load();
    } catch (e) {
      setError(msg(e));
    } finally {
      setProcessando(false);
    }
  };

  const excluir = (r) => {
    confirmDialog({
      message: `Inativar o lote ${r.codigo}?`,
      header: 'Confirmar',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Inativar',
      rejectLabel: 'Cancelar',
      accept: async () => {
        try {
          await axios.delete('/api/estoque/lotes/' + r.id);
          toast.current?.show({ severity: 'success', summary: 'Lote inativado', life: 2500 });
          await load();
        } catch (e) {
          setError(msg(e));
        }
      },
    });
  };

  const acao = async (r, path, label) => {
    setProcessando(true);
    setError('');
    try {
      await axios.post(`/api/estoque/lotes/${r.id}/${path}`, { motivo: label });
      toast.current?.show({ severity: 'success', summary: label, life: 2500 });
      await load();
    } catch (e) {
      setError(msg(e));
      toast.current?.show({ severity: 'error', summary: 'Erro', detail: msg(e), life: 4000 });
    } finally {
      setProcessando(false);
    }
  };

  const vencerExpirados = async () => {
    setProcessando(true);
    setError('');
    try {
      const r = await axios.post('/api/estoque/lotes/vencer-expirados');
      const lista = r.data?.data ?? r.data ?? [];
      toast.current?.show({
        severity: 'success',
        summary: `${Array.isArray(lista) ? lista.length : 0} lote(s) marcado(s) como VENCIDO`,
        life: 3000,
      });
      await load();
    } catch (e) {
      setError(msg(e));
    } finally {
      setProcessando(false);
    }
  };

  const acoes = (r) => {
    const st = r.status || 'ATIVO';
    return (
      <div className="flex gap-1 flex-wrap">
        {st === 'ATIVO' && (
          <>
            <Button icon="pi pi-shield" rounded text severity="warning" tooltip="Quarentena"
              disabled={processando} onClick={() => acao(r, 'quarentena', 'Lote em quarentena')} />
            <Button icon="pi pi-ban" rounded text severity="danger" tooltip="Bloquear"
              disabled={processando} onClick={() => acao(r, 'bloquear', 'Lote bloqueado')} />
          </>
        )}
        {(st === 'QUARENTENA' || st === 'BLOQUEADO') && (
          <Button icon="pi pi-check" rounded text severity="success" tooltip="Liberar"
            disabled={processando} onClick={() => acao(r, 'liberar', 'Lote liberado')} />
        )}
        <Button icon="pi pi-pencil" rounded text tooltip="Editar" onClick={() => editar(r)} />
        {st !== 'INATIVO' && (
          <Button icon="pi pi-trash" rounded text severity="danger" tooltip="Inativar" onClick={() => excluir(r)} />
        )}
      </div>
    );
  };

  const vencidoHint = (r) => {
    if (!r.dataValidade) return null;
    const d = new Date(r.dataValidade + 'T00:00:00');
    if (Number.isNaN(d.getTime())) return null;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    if (d < hoje && r.status === 'ATIVO') {
      return <Tag value="Validade vencida" severity="danger" className="ml-1" />;
    }
    const diff = (d - hoje) / (1000 * 60 * 60 * 24);
    if (diff >= 0 && diff <= 30 && r.status === 'ATIVO') {
      return <Tag value={`${Math.ceil(diff)}d`} severity="warning" className="ml-1" />;
    }
    return null;
  };

  return (
    <div>
      <Toast ref={toast} />
      <ConfirmDialog />
      <Card title={t('legacyUi.lotes.title') || 'Lotes de estoque'}>
        <div className="p-fluid grid">
          <div className="field col-12 md:col-3">
            <label>Produto *</label>
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
              selectedItemTemplate={(p) => (p ? `${p.nome || p.descricao || ''} · ${p.codigo || p.id}` : '')}
              disabled={!!editando}
              onChange={(e) => {
                setProduto(e.value);
                setProdutoId(e.value?.id || '');
              }}
              placeholder="Pesquisar produto"
            />
          </div>
          <div className="field col-12 md:col-2">
            <label>Lote *</label>
            <InputText value={codigo} onChange={(e) => setCodigo(e.target.value)} />
          </div>
          <div className="field col-12 md:col-2">
            <label>Quantidade</label>
            <InputNumber value={quantidade} onValueChange={(e) => setQuantidade(e.value || 0)} minFractionDigits={3} />
          </div>
          <div className="field col-12 md:col-2">
            <label>Fabricação</label>
            <Calendar value={fabricacao} onChange={(e) => setFabricacao(e.value)} dateFormat="dd/mm/yy" />
          </div>
          <div className="field col-12 md:col-2">
            <label>Validade</label>
            <Calendar value={validade} onChange={(e) => setValidade(e.value)} dateFormat="dd/mm/yy" />
          </div>
          <div className="field col-12 md:col-2">
            <label>Status (filtro)</label>
            <Dropdown value={statusFiltro} options={STATUS_OPTS} onChange={(e) => setStatusFiltro(e.value)} />
          </div>
          <div className="field col-12 md:col-2">
            <label>Depósito</label>
            <Dropdown
              value={depositoId}
              options={depositos}
              optionLabel="nome"
              optionValue="id"
              showClear
              onChange={(e) => setDepositoId(e.value)}
              placeholder="Todos"
            />
          </div>
          <div className="col-12 flex gap-2 flex-wrap">
            <Button
              label={editando ? 'Salvar alterações' : 'Cadastrar lote'}
              icon="pi pi-check"
              onClick={salvar}
              loading={processando}
              disabled={!produtoId || !codigo}
            />
            {editando && (
              <Button label={t('legacyUi.lotes.cancel') || 'Cancelar'} severity="secondary" text icon="pi pi-times" onClick={limpar} />
            )}
            <Button
              label="Marcar vencidos"
              icon="pi pi-calendar-times"
              severity="warning"
              outlined
              onClick={vencerExpirados}
              loading={processando}
            />
          </div>
        </div>
        {error && <Message severity="error" text={error} className="mt-2" />}
        <DataTable value={rows} className="mt-3" paginator rows={12} stripedRows emptyMessage="Nenhum lote">
          <Column field="produtoId" header="Produto" />
          <Column field="codigo" header="Lote" body={(r) => (
            <span>{r.codigo}{vencidoHint(r)}</span>
          )} />
          <Column field="quantidade" header="Saldo" />
          <Column field="dataFabricacao" header="Fabricação" />
          <Column field="dataValidade" header="Validade" />
          <Column field="depositoId" header="Depósito" />
          <Column
            header="Status"
            body={(r) => <Tag value={r.status || '—'} severity={severityStatus(r.status)} />}
          />
          <Column header="Ações" body={acoes} style={{ minWidth: '10rem' }} />
        </DataTable>
      </Card>
    </div>
  );
}
