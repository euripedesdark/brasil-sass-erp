import React, { useEffect, useRef, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';
import { confirmDialog } from 'primereact/confirmdialog';
import { apiFetch } from '../../services/ApiConfig';
import PedidoVendaService from '../../services/PedidoVendaService';
import { useAuth } from '../../contexts/AuthContext';

const BASE = '/api/fiscal/nfe';

export function NFe() {
    const { user } = useAuth();
    const toast = useRef(null);
    const [pedidos, setPedidos] = useState([]);
    const [notas, setNotas] = useState([]);
    const [pedidoId, setPedidoId] = useState(null);
    const [chave, setChave] = useState('');
    const [consulta, setConsulta] = useState(null);
    const [loading, setLoading] = useState(false);
    const [emitindo, setEmitindo] = useState(null);
    const [erro, setErro] = useState('');

    const empresaId = user?.empresaId;

    const carregarPedidos = async () => {
        if (!empresaId) return;
        try {
            const r = await PedidoVendaService.listarPorEmpresa(empresaId);
            const data = r?.data?.data ?? r?.data ?? [];
            setPedidos((Array.isArray(data) ? data : []).filter(p => p.status === 'ABERTO' && p.tipo === 'PEDIDO'));
        } catch {
            setPedidos([]);
        }
    };

    useEffect(() => { carregarPedidos(); }, [empresaId]);

    const emitir = async () => {
        if (!pedidoId) {
            setErro('Selecione um pedido de venda.');
            return;
        }
        setErro('');
        setEmitindo(pedidoId);
        try {
            const r = await apiFetch(`${BASE}/emitir/${pedidoId}`, { method: 'POST' });
            const body = await r.json().catch(() => ({}));
            if (!r.ok) throw new Error(body.message || body.erro || 'A SEFAZ recusou a emissão.');
            toast.current?.show({ severity: 'success', summary: 'NF-e transmitida', detail: `Protocolo: ${body.protocolo || 'retornado pela SEFAZ'}`, life: 6000 });
            setPedidoId(null);
            carregarPedidos();
        } catch (e) {
            setErro(e.message || 'Falha ao transmitir NF-e.');
        } finally {
            setEmitindo(null);
        }
    };

    const consultar = async () => {
        const k = chave.replace(/\D/g, '');
        if (k.length !== 44) {
            setErro('Informe uma chave de acesso NF-e com 44 dígitos.');
            return;
        }
        setLoading(true);
        setErro('');
        try {
            const r = await apiFetch(`${BASE}/documento/${k}`);
            const body = await r.json().catch(() => null);
            if (!r.ok) throw new Error(body?.message || body?.erro || 'NF-e não encontrada nesta empresa.');
            setConsulta(body?.data ?? body);
        } catch (e) {
            setConsulta(null);
            setErro(e.message || 'Falha na consulta.');
        } finally {
            setLoading(false);
        }
    };

    const consultarSefaz = async () => {
        const k = chave.replace(/\D/g, '');
        if (k.length !== 44) {
            setErro('Informe uma chave de acesso NF-e com 44 dígitos.');
            return;
        }
        setLoading(true);
        setErro('');
        try {
            const r = await apiFetch(`${BASE}/consultar/${k}`);
            const body = await r.text();
            if (!r.ok) throw new Error(body || 'Falha na consulta à SEFAZ.');
            setConsulta({ chaveAcesso: k, retornoSefazXml: body });
        } catch (e) {
            setErro(e.message || 'Falha na consulta à SEFAZ.');
        } finally {
            setLoading(false);
        }
    };

    const cancelar = () => {
        const k = consulta?.chaveAcesso || chave.replace(/\D/g, '');
        if (k.length !== 44) {
            setErro('Consulte primeiro uma NF-e válida.');
            return;
        }
        confirmDialog.require({
            message: 'O cancelamento será transmitido à SEFAZ. Informe o motivo na próxima etapa.',
            header: 'Cancelar NF-e',
            icon: 'pi pi-exclamation-triangle',
            accept: async () => {
                const motivo = window.prompt('Motivo do cancelamento (15 a 255 caracteres):', '');
                if (!motivo || motivo.trim().length < 15) return;
                try {
                    const r = await apiFetch(`${BASE}/cancelar`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ chaveAcesso: k, motivo: motivo.trim() })
                    });
                    const body = await r.json().catch(() => ({}));
                    if (!r.ok) throw new Error(body.message || body.erro || 'Cancelamento recusado.');
                    toast.current?.show({ severity: 'success', summary: 'NF-e cancelada', detail: `Protocolo: ${body.protocolo || 'retornado pela SEFAZ'}`, life: 6000 });
                    consultar();
                } catch (e) {
                    setErro(e.message || 'Falha no cancelamento.');
                }
            }
        });
    };

    const pedidoOptions = pedidos.map(p => ({
        label: `Pedido ${p.numero || p.id} — R$ ${Number(p.valorTotal || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
        value: p.id
    }));

    return <div>
        <Toast ref={toast} />
        {erro && <Message severity="error" text={erro} className="mb-3" />}
        <div className="grid">
            <div className="col-12 xl:col-7">
                <Card title="Emissão de NF-e">
                    <p className="text-color-secondary">Transmita a NF-e a partir de um pedido de venda. O certificado e as regras fiscais são definidos por empresa.</p>
                    <div className="flex gap-2 align-items-end flex-wrap">
                        <div className="flex-1 min-w-0">
                            <label className="block mb-2" htmlFor="nfe-pedido">Pedido de venda</label>
                            <Dropdown id="nfe-pedido" value={pedidoId} options={pedidoOptions}
                                onChange={e => setPedidoId(e.value)} placeholder="Selecione um pedido faturável"
                                className="w-full" filter showClear />
                        </div>
                        <Button label="Transmitir NF-e" icon="pi pi-send" onClick={emitir}
                            loading={emitindo === pedidoId} disabled={!pedidoId} />
                    </div>
                </Card>
            </div>
            <div className="col-12 xl:col-5">
                <Card title="Consulta por chave">
                    <label className="block mb-2" htmlFor="nfe-chave">Chave de acesso</label>
                    <div className="flex gap-2">
                        <InputText id="nfe-chave" value={chave} onChange={e => setChave(e.target.value.replace(/\D/g, '').slice(0, 44))}
                            placeholder="44 dígitos" className="w-full" maxLength={44} />
                        <Button icon="pi pi-search" onClick={consultar} loading={loading} tooltip="Consultar documento no ERP" />
                        <Button icon="pi pi-cloud" severity="secondary" outlined onClick={consultarSefaz} loading={loading} tooltip="Consultar situação na SEFAZ" />
                    </div>
                </Card>
            </div>
        </div>
        {consulta && <Card title="Documento NF-e" className="mt-3">
            <div className="grid">
                <div className="col-12 md:col-3"><small>Número</small><div>{consulta.numero || '—'}</div></div>
                <div className="col-12 md:col-2"><small>Série</small><div>{consulta.serie || '—'}</div></div>
                <div className="col-12 md:col-3"><small>Status</small><div>{consulta.status || '—'}</div></div>
                <div className="col-12 md:col-4"><small>Protocolo</small><div>{consulta.protocolo || '—'}</div></div>
                <div className="col-12"><small>Chave</small><div className="font-mono">{consulta.chaveAcesso || chave || '—'}</div></div>
            </div>
            <div className="flex gap-2 mt-3">
                {consulta.status === 'AUTORIZADA' && <Button label="Cancelar NF-e" icon="pi pi-times" severity="danger" outlined onClick={cancelar} />}
                {consulta.retornoSefazXml && <Dialog visible header="Retorno SEFAZ" modal style={{ width: '75vw' }}
                    onHide={() => setConsulta({ ...consulta, retornoSefazXml: null })}>
                    <pre style={{ maxHeight: '60vh', overflow: 'auto', whiteSpace: 'pre-wrap' }}>{consulta.retornoSefazXml}</pre>
                </Dialog>}
            </div>
        </Card>}
        <Card title="Pedidos disponíveis para emissão" className="mt-3">
            <DataTable value={pedidos} loading={!empresaId} paginator rows={10} size="small" emptyMessage="Nenhum pedido aberto disponível para NF-e.">
                <Column field="numero" header="Pedido" />
                <Column field="clienteId" header="Cliente" />
                <Column field="valorTotal" header="Total" body={r => Number(r.valorTotal || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} />
                <Column body={r => <Button icon="pi pi-send" text tooltip="Selecionar para emissão" onClick={() => setPedidoId(r.id)} />} />
            </DataTable>
        </Card>
    </div>;
}
