import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';

export const Impostos = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [impostos, setImpostos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [tipo, setTipo] = useState('');

    const carregar = async () => {
        setLoading(true);
        try {
            const params = tipo.trim() ? `?tipo=${encodeURIComponent(tipo.trim())}` : '';
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/fiscal/impostos${params}`);
            if (!response.ok) throw new Error(await response.text());
            setImpostos(await response.json());
        } catch (err) {
            console.error('Erro ao carregar impostos', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Não foi possível carregar os impostos',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        carregar();
    }, []);

    const aliquota = (row) =>
        row.aliquotaPadrao == null ? '-' : `${Number(row.aliquotaPadrao).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })}%`;

    const status = (row) => (
        <Tag value={row.ativo ? 'Ativo' : 'Inativo'} severity={row.ativo ? 'success' : 'danger'} />
    );

    return (
        <div className="fiscal-impostos-container">
            <Toast ref={toast} />
            <Card title="Impostos" subTitle="Tributos utilizados na configuração fiscal da empresa">
                <div className="flex gap-2 mb-4 align-items-center">
                    <InputText
                        value={tipo}
                        onChange={(e) => setTipo(e.target.value)}
                        placeholder="Filtrar por tipo: FEDERAL, ESTADUAL..."
                        onKeyDown={(e) => e.key === 'Enter' && carregar()}
                    />
                    <Button label="Pesquisar" icon="pi pi-search" onClick={carregar} loading={loading} />
                    <Button label="Limpar" icon="pi pi-filter-slash" text onClick={() => { setTipo(''); setTimeout(carregar, 0); }} />
                </div>

                <DataTable
                    value={impostos}
                    loading={loading}
                    paginator
                    rows={20}
                    rowsPerPageOptions={[20, 50, 100]}
                    responsiveLayout="scroll"
                    emptyMessage="Nenhum imposto encontrado"
                    className="p-datatable-sm"
                >
                    <Column field="sigla" header="Sigla" sortable style={{ width: '100px' }} />
                    <Column field="nome" header="Nome" sortable />
                    <Column field="tipo" header="Tipo" sortable style={{ width: '160px' }} />
                    <Column header="Alíquota padrão" body={aliquota} sortable style={{ width: '170px' }} />
                    <Column header="Status" body={status} style={{ width: '120px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Impostos;
