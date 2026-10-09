import React, { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import PedidoVendaService from '../../services/PedidoVendaService';

/** Dialog ATP (Available-to-Promise) antes de confirmar orçamento. */
export function AtpConfirmDialog({ visible, pedidoId, onHide, onConfirm, getError }) {
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState(null);
    const [confirming, setConfirming] = useState(false);

    useEffect(() => {
        if (!visible || !pedidoId) return;
        setLoading(true);
        PedidoVendaService.atp(pedidoId)
            .then((r) => setData(r?.data?.data ?? r?.data ?? null))
            .catch((err) => {
                setData(null);
                if (getError) getError(err);
            })
            .finally(() => setLoading(false));
    }, [visible, pedidoId]);

    const confirmar = async () => {
        setConfirming(true);
        try {
            await onConfirm(pedidoId);
        } finally {
            setConfirming(false);
        }
    };

    return (
        <Dialog visible={visible} onHide={onHide} header="Disponibilidade (ATP)" modal style={{ width: 'min(720px, 96vw)' }}>
            {loading && <p>Consultando estoque…</p>}
            {!loading && data && (
                <>
                    <Message
                        severity={data.disponivelTotal ? 'success' : 'warn'}
                        text={data.disponivelTotal
                            ? 'Estoque suficiente para todos os itens. Pode confirmar.'
                            : 'Há item(ns) sem estoque disponível. Confirmar pode falhar na reserva.'}
                        className="w-full mb-3"
                    />
                    <DataTable value={data.itens || []} size="small" emptyMessage="Sem itens de produto">
                        <Column field="produtoId" header="Produto" />
                        <Column field="descricao" header="Descrição" />
                        <Column field="solicitado" header="Solicitado" />
                        <Column field="saldo" header="Saldo" />
                        <Column field="reservado" header="Reservado" />
                        <Column field="disponivel" header="Disponível" />
                        <Column header="OK" body={(r) => <Tag value={r.ok ? 'Sim' : 'Não'} severity={r.ok ? 'success' : 'danger'} />} />
                    </DataTable>
                </>
            )}
            <div className="flex justify-end gap-2 mt-3">
                <Button label="Fechar" text severity="secondary" onClick={onHide} />
                <Button
                    label="Confirmar orçamento"
                    icon="pi pi-check"
                    severity={data && !data.disponivelTotal ? 'warning' : 'success'}
                    loading={confirming}
                    disabled={!pedidoId || loading}
                    onClick={confirmar}
                />
            </div>
        </Dialog>
    );
}

export default AtpConfirmDialog;
