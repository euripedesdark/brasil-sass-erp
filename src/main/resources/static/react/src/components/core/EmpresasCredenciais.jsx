import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useNavigate } from 'react-router-dom';

/**
 * Visão das empresas (matriz/filial) e se Stripe/certificado estão ok.
 * A configuração em si é feita em /configurar-empresa na empresa logada.
 */
export const EmpresasCredenciais = () => {
    const toast = useRef(null);
    const nav = useNavigate();
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch('/api/core/empresas/com-credenciais');
            if (!r.ok) throw new Error('HTTP ' + r.status);
            const j = await r.json();
            setRows(Array.isArray(j) ? j : (j?.data || []));
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, []);

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Empresas — Stripe e certificado</h2>
                    <span className="text-color-secondary">
                        Cada matriz/filial tem as próprias credenciais. Configure na empresa em que estiver logado.
                    </span>
                </div>
                <div className="flex gap-2">
                    <Button label="Configurar esta empresa" icon="pi pi-building" onClick={() => nav('/configurar-empresa')} />
                    <Button label="Atualizar" icon="pi pi-refresh" outlined onClick={carregar} loading={loading} />
                </div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} dataKey="id" emptyMessage="Nenhuma empresa">
                <Column field="id" header="ID" style={{ width: '4rem' }} />
                <Column field="razaoSocial" header="Razão social" />
                <Column field="cnpj" header="CNPJ" />
                <Column field="uf" header="UF" style={{ width: '4rem' }} />
                <Column header="Stripe" body={(r) => (
                    <Tag
                        value={r.stripeHabilitada ? 'Ativo' : (r.stripeConfigurada ? 'Configurado' : 'Pendente')}
                        severity={r.stripeHabilitada ? 'success' : (r.stripeConfigurada ? 'warning' : 'danger')}
                    />
                )} />
                <Column header="Certificado" body={(r) => (
                    <Tag
                        value={r.certificadoOk ? `${r.certificados} arquivo(s)` : 'Pendente'}
                        severity={r.certificadoOk ? 'success' : 'danger'}
                    />
                )} />
            </DataTable>
        </div>
    );
};
export default EmpresasCredenciais;
