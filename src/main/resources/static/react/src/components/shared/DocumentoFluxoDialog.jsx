import React, { useState, useEffect } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Tag } from 'primereact/tag';

export const DocumentoFluxoDialog = ({ visible, onHide, tipo, id }) => {
    const [ligacoes, setLigacoes] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!visible || !tipo || !id) return;
        setLoading(true);
        apiFetch(`/api/core/documento-fluxo?tipo=${encodeURIComponent(tipo)}&id=${id}`)
            .then(async (r) => {
                const j = await r.json().catch(() => []);
                setLigacoes(Array.isArray(j) ? j : (j?.data ?? []));
            })
            .catch(() => setLigacoes([]))
            .finally(() => setLoading(false));
    }, [visible, tipo, id]);

    return (
        <Dialog
            visible={visible}
            onHide={onHide}
            header={`Fluxo de documentos — ${tipo || ''} #${id || ''}`}
            modal
            style={{ width: 'min(720px, 96vw)' }}
        >
            <p className="bc-muted mb-3">
                Rastreia a cadeia de documentos gerados a partir deste.
            </p>
            <DataTable value={ligacoes} loading={loading} emptyMessage="Nenhuma ligação registrada" size="small">
                <Column header="De" body={(r) => (
                    <span>{r.origemTipo} <Tag value={`#${r.origemId}`} /> {r.origemNumero ? `(${r.origemNumero})` : ''}</span>
                )} />
                <Column field="relacao" header="Relação" body={(r) => <Tag value={r.relacao} severity="info" />} style={{ width: '7rem' }} />
                <Column header="Para" body={(r) => (
                    <span>{r.destinoTipo} <Tag value={`#${r.destinoId}`} severity="success" /> {r.destinoNumero ? `(${r.destinoNumero})` : ''}</span>
                )} />
                <Column field="createdAt" header="Quando" body={(r) => r.createdAt ? new Date(r.createdAt).toLocaleString('pt-BR') : '—'} style={{ width: '10rem' }} />
            </DataTable>
            <div className="flex justify-end mt-3">
                <Button label="Fechar" text onClick={onHide} />
            </div>
        </Dialog>
    );
};

export default DocumentoFluxoDialog;
