import React, { useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';

const TIPOS = [
    'PEDIDO_VENDA', 'PEDIDO_COMPRA', 'CONTRATO_COMPRA', 'NFE', 'NFSE',
    'ORDEM_PRODUCAO', 'ORDEM_SERVICO', 'EXPEDICAO', 'TITULO', 'RECEBIMENTO',
].map((t) => ({ label: t, value: t }));

export const DocumentoFluxoPage = () => {
    const toast = useRef(null);
    const [tipo, setTipo] = useState('PEDIDO_VENDA');
    const [id, setId] = useState(null);
    const [ligacoes, setLigacoes] = useState([]);
    const [arvore, setArvore] = useState([]);
    const [loading, setLoading] = useState(false);

    const buscar = async () => {
        if (!tipo || !id) {
            toast.current?.show({ severity: 'warn', summary: 'Informe tipo e id', life: 2500 });
            return;
        }
        setLoading(true);
        try {
            const [a, b] = await Promise.all([
                apiFetch(`/api/core/documento-fluxo?tipo=${encodeURIComponent(tipo)}&id=${id}`).then((r) => r.json()),
                apiFetch(`/api/core/documento-fluxo/arvore?tipo=${encodeURIComponent(tipo)}&id=${id}`).then((r) => r.json()),
            ]);
            setLigacoes(Array.isArray(a) ? a : (a?.data ?? []));
            setArvore(Array.isArray(b) ? b : (b?.data ?? []));
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <h2 className="m-0">Fluxo de documentos</h2>
            <p className="text-color-secondary">Rastreia a origem e o destino dos documentos encadeados.</p>
            <div className="flex flex-wrap gap-2 align-items-end mb-3">
                <div>
                    <label className="block mb-1">Tipo</label>
                    <Dropdown value={tipo} options={TIPOS} onChange={(e) => setTipo(e.value)} className="w-16rem" />
                </div>
                <div>
                    <label className="block mb-1">ID</label>
                    <InputNumber value={id} onValueChange={(e) => setId(e.value)} useGrouping={false} className="w-10rem" />
                </div>
                <Button label="Buscar" icon="pi pi-search" onClick={buscar} loading={loading} />
            </div>
            <h3>Ligações</h3>
            <DataTable value={ligacoes} emptyMessage="Sem ligações" size="small" className="mb-4">
                <Column field="origemTipo" header="Origem tipo" />
                <Column field="origemId" header="Origem id" />
                <Column field="origemNumero" header="Origem nº" />
                <Column field="destinoTipo" header="Destino tipo" />
                <Column field="destinoId" header="Destino id" />
                <Column field="destinoNumero" header="Destino nº" />
                <Column field="relacao" header="Relação" />
            </DataTable>
            <h3>Árvore</h3>
            <DataTable value={arvore} emptyMessage="Sem árvore" size="small">
                <Column field="tipo" header="Tipo" />
                <Column field="id" header="Id" />
                <Column field="numero" header="Número" />
                <Column field="relacao" header="Relação" />
            </DataTable>
        </div>
    );
};
export default DocumentoFluxoPage;
