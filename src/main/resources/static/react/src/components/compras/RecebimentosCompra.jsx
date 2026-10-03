import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { Tag } from 'primereact/tag';
import ConferenciaCompraService from '../../services/ConferenciaCompraService';

export default function RecebimentosCompra() {
        const { t } = useTranslation();
    const [rows, setRows] = useState([]);
    const [itens, setItens] = useState([]);
    const [visible, setVisible] = useState(false);
    const [loading, setLoading] = useState(false);

    const carregar = async () => {
        setLoading(true);
        try { setRows((await ConferenciaCompraService.recebimentos()).data || []); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, []);

    const abrir = async (row) => {
        setItens((await ConferenciaCompraService.itensRecebimento(row.id)).data || []);
        setVisible(true);
    };

    return <Card title={t('legacyUi.recebimentos.title')}>
        <DataTable value={rows} loading={loading} paginator rows={15} stripedRows emptyMessage={t('legacyUi.recebimentos.empty')}>
            <Column field="numero" header={t('legacyUi.recebimentos.receipt')} sortable />
            <Column field="pedidoId" header={t('legacyUi.recebimentos.order')} sortable />
            <Column field="dataRecebimento" header={t('legacyUi.recebimentos.date')} sortable />
            <Column field="valorTotal" header={t('legacyUi.recebimentos.value')} body={r => Number(r.valorTotal || 0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'})} />
            <Column field="status" header={t('legacyUi.recebimentos.status')} body={r => <Tag value={r.status} severity={r.status === 'RECEBIDO' ? 'success' : 'info'} />} />
            <Column header="" body={r => <Button icon="pi pi-eye" text onClick={() => abrir(r)} />} />
        </DataTable>
        <Dialog header={t('legacyUi.recebimentos.items')} visible={visible} onHide={() => setVisible(false)} style={{width:'70rem'}}>
            <DataTable value={itens} stripedRows>
                <Column field="produtoId" header={t('legacyUi.recebimentos.product')} />
                <Column field="quantidade" header={t('legacyUi.recebimentos.quantity')} />
                <Column field="valorUnitario" header={t('legacyUi.recebimentos.unitPrice')} />
                <Column field="valorTotal" header={t('legacyUi.recebimentos.total')} />
            </DataTable>
        </Dialog>
    </Card>;
}
