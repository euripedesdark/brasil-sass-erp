import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';
import { IssqnService } from '../../services/IssqnService';
import './Issqn.css';

export const Issqn = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [issqnList, setIssqnList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [ufFiltro, setUfFiltro] = useState('');

    const ufOptions = [
        { label: t('legacyUi.issqn.selectState'), value: '' },
        { label: 'AC', value: 'AC' },
        { label: 'AL', value: 'AL' },
        { label: 'AP', value: 'AP' },
        { label: 'AM', value: 'AM' },
        { label: 'BA', value: 'BA' },
        { label: 'CE', value: 'CE' },
        { label: 'DF', value: 'DF' },
        { label: 'ES', value: 'ES' },
        { label: 'GO', value: 'GO' },
        { label: 'MA', value: 'MA' },
        { label: 'MT', value: 'MT' },
        { label: 'MS', value: 'MS' },
        { label: 'MG', value: 'MG' },
        { label: 'PA', value: 'PA' },
        { label: 'PB', value: 'PB' },
        { label: 'PR', value: 'PR' },
        { label: 'PE', value: 'PE' },
        { label: 'PI', value: 'PI' },
        { label: 'RJ', value: 'RJ' },
        { label: 'RN', value: 'RN' },
        { label: 'RS', value: 'RS' },
        { label: 'RO', value: 'RO' },
        { label: 'RR', value: 'RR' },
        { label: 'SC', value: 'SC' },
        { label: 'SP', value: 'SP' },
        { label: 'SE', value: 'SE' },
        { label: 'TO', value: 'TO' }
    ];

    useEffect(() => {
        if (ufFiltro) {
            fetchIssqn();
        }
    }, [ufFiltro]);

    const fetchIssqn = async () => {
        setLoading(true);
        try {
            const data = await IssqnService.listarPorUf(ufFiltro);
            setIssqnList(data || []);
        } catch (err) {
            console.error('Erro ao carregar ISSQN', err);
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.issqn.loadError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const aliquotaTemplate = (rowData) => {
        return rowData.aliquota ? `${(rowData.aliquota * 100).toFixed(2)}%` : '0%';
    };

    const header = (
        <div className="issqn-header flex justify-content-between align-items-center">
            <span className="p-input-icon-left flex-1">
                <i className="pi pi-search" />
                <InputText placeholder={t('legacyUi.issqn.search')} className="w-full" />
            </span>
            <span className="ml-3">
                <Dropdown
                    value={ufFiltro}
                    options={ufOptions}
                    onChange={(e) => setUfFiltro(e.value)}
                    optionLabel="label"
                    optionValue="value"
                    placeholder={t('legacyUi.issqn.selectState')}
                />
            </span>
        </div>
    );

    return (
        <div className="issqn-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('legacyUi.issqn.title')} className="issqn-main-card">
                <div className="issqn-header-actions mb-4">
                    <p className="text-muted m-0">Tabela de aliquotas de ISS por municipio. Selecione uma UF para carregar os dados.</p>
                </div>

                <DataTable
                    value={issqnList}
                    header={header}
                    loading={loading}
                    paginator
                    rows={20}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={issqnList.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={ufFiltro ? t('legacyUi.issqn.empty') : t('legacyUi.issqn.selectStateHint')}
                >
                    <Column field="codigo" header={t('legacyUi.issqn.code')} sortable style={{ width: '100px' }} />
                    <Column field="municipio" header={t('legacyUi.issqn.city')} sortable style={{ width: '200px' }} />
                    <Column field="descricao" header={t('legacyUi.issqn.description')} sortable style={{ width: '300px' }} />
                    <Column field="aliquota" header={t('legacyUi.issqn.rate')} body={aliquotaTemplate} sortable style={{ width: '120px' }} />
                    <Column field="codIbge" header={t('legacyUi.issqn.ibge')} sortable style={{ width: '100px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Issqn;
