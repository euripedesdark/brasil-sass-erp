import { confirmDialog } from 'primereact/confirmdialog';
import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { InputText } from 'primereact/inputtext';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { RegrasComissao } from './RegrasComissao';
import { TabPanel, TabView } from 'primereact/tabview';
import { useAuth } from '../../contexts/AuthContext';

const moeda = (v) => `R$ ${Number(v ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;

export const Comissoes = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const [comissoes, setComissoes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState('');
    const [resumo, setResumo] = useState(null);
    const [aba, setAba] = useState(0);
    // trava a linha durante o pagamento: dois cliques registram a comissao
    // como paga duas vezes e o usuario ve dois avisos sem entender o motivo
    const [emAcao, setEmAcao] = useState(null);
    const toast = useRef(null);

    useEffect(() => {
        fetchComissoes();
    }, []);

    const fetchComissoes = async () => {
        setLoading(true);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/comissoes?empresaId=${user?.empresaId}`);
            const data = await response.json();
            setComissoes(Array.isArray(data) ? data : []);

            // totais vêm do backend: a tela calculava somando a lista inteira,
            // o que ignora comissão de vendedor filtro e de outro período
            try {
                const rr = await apiFetch('/api/financeiro/comissoes/resumo');
                if (rr.ok) setResumo((await rr.json())?.data ?? null);
            } catch { /* resumo é informativo; a lista segue valendo */ }
        } catch (err) {
            console.error(t('errors.load'), err);
        } finally {
            setLoading(false);
        }
    };

    // Pagar comissao move dinheiro e nao tem como voltar atras: o status vai
    // para PAGO e o pagamento e registrado com data. Sem confirmacao, um clique
    // acidental em "marcar como pago" descarrega o valor de um vendedor.
    const perguntarPagar = (row) => {
        confirmDialog.require({
            header: t('legacyUi.comissoesLegacy.pay'),
            message: t('legacyUi.comissoesLegacy.payConfirm',{name:row.funcionarioNome||t('legacyUi.comissoesLegacy.salesperson'),value:`R$ ${Number(row.valorComissao||0).toFixed(2)}`}),
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('legacyUi.comissoesLegacy.yesPay'),
            rejectLabel: t('common.cancel'),
            accept: () => markAsPaid(row.id)
        });
    };

    const markAsPaid = async (id) => {
        setEmAcao(id);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/comissoes/${id}/pagar`, {
                method: 'POST'
            });
            if (response.ok) {
                fetchComissoes();
            } else {
                toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('legacyUi.comissoesLegacy.payError'), life: 4000 });
            }
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('legacyUi.comissoesLegacy.communicationError'), life: 4000 });
        } finally {
            setEmAcao(null);
        }
    };

    const statusTemplate = (rowData) => {
        return <Tag value={rowData.status} severity={rowData.status === 'PAGO' ? 'success' : 'warning'} />;
    };

    const actionTemplate = (rowData) => {
        if (rowData.status === 'PAGO') return null;
        return <Button icon="pi pi-check" className="p-button-text p-button-success" disabled={emAcao === rowData.id} onClick={() => perguntarPagar(rowData)} />;
    };

    const totalPending = comissoes
        .filter(c => c.status === 'PENDENTE')
        .reduce((acc, curr) => acc + curr.valorComissao, 0);

    const totalPaid = comissoes
        .filter(c => c.status === 'PAGO')
        .reduce((acc, curr) => acc + curr.valorComissao, 0);

    return (
        <>
        <Toast ref={toast} />
        <div className="comissoes-container">
            <div className="grid mb-4">
                <div className="col-12 md:col-6 lg:col-3">
                    <Card className="stat-card pending">
                        <div className="stat-header">
                            <i className="pi pi-clock text-warning"></i>
                            <span className="stat-title">A Pagar</span>
                        </div>
                        <div className="stat-value">{moeda(resumo?.totalPendente ?? totalPending)}</div>
                    </Card>
                </div>
                <div className="col-12 md:col-6 lg:col-3">
                    <Card className="stat-card paid">
                        <div className="stat-header">
                            <i className="pi pi-check-circle text-success"></i>
                            <span className="stat-title">Pago</span>
                        </div>
                        <div className="stat-value">{moeda(resumo?.totalPago ?? totalPaid)}</div>
                    </Card>
                </div>
            </div>

            <TabView activeIndex={aba} onTabChange={(e) => setAba(e.index)}>
            <TabPanel header="Comissões" leftIcon="pi pi-money-bill">
            <Card title="Listagem de Comissões e Bônus">
                <div className="flex justify-content-between align-items-center mb-3">
                    <div className="p-input-icon-left">
                        <i className="pi pi-search" />
                        <InputText
                            placeholder="Filtrar por funcionário..."
                            value={filter}
                            onChange={(e) => setFilter(e.target.value)}
                        />
                    </div>
                    <Button label="Atualizar" icon="pi pi-refresh" onClick={fetchComissoes} />
                </div>

                <DataTable
                    value={comissoes.filter(c => c.funcionarioNome?.toLowerCase().includes(filter.toLowerCase()))}
                    loading={loading}
                    paginator
                    rows={10}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                >
                    <Column field="funcionarioNome" header={t('legacyUi.comissoesLegacy.salesperson')} sortable></Column>
                    <Column field="pedidoId" header={t('legacyUi.comissoesLegacy.order')} sortable></Column>
                    <Column field="valorVenda" header={t('legacyUi.comissoesLegacy.saleValue')} sortable></Column>
                    <Column field="percentual" header="%" sortable></Column>
                    <Column field="valorComissao" header={t('legacyUi.comissoesLegacy.commission')} sortable></Column>
                    <Column field="status" header="Status" body={statusTemplate} sortable></Column>
                    <Column header={t('legacyUi.comissoesLegacy.action')} body={actionTemplate} style={{ width: '80px' }}></Column>
                </DataTable>
            </Card>
            </TabPanel>
            <TabPanel header="Regras de comissão" leftIcon="pi pi-percentage">
                <Card>
                    <RegrasComissao />
                </Card>
            </TabPanel>
            </TabView>

        </div>
        </>
    );
};

export default Comissoes;
