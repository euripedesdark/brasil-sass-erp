import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { BuscaFiscalService } from '../../services/BuscaFiscalService';
import { useTranslation } from 'react-i18next';

const catalogos = [
    { label: 'Todos os catálogos', value: '' },
    { label: 'NCM', value: 'ncm' },
    { label: 'CFOP', value: 'cfop' },
    { label: 'ISSQN', value: 'issqn' }
];

export const BuscaFiscal = () => {
    const { t } = useTranslation();
    const [termo, setTermo] = useState('');
    const [tabela, setTabela] = useState('');
    const [resultados, setResultados] = useState([]);
    const [loading, setLoading] = useState(false);
    const [erro, setErro] = useState('');

    const buscar = async (event) => {
        event?.preventDefault();
        if (!termo.trim()) {
            setResultados([]);
            setErro(t('fiscal.searchRequired'));
            return;
        }
        setLoading(true);
        setErro('');
        try {
            const resposta = await BuscaFiscalService.buscar(termo.trim(), tabela);
            setResultados(Array.isArray(resposta) ? resposta : []);
        } catch (error) {
            setResultados([]);
            setErro(error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card title={t('fiscal.searchTitle')} className="mb-3">
            <p className="text-color-secondary mt-0">{t('fiscal.searchDescription')}</p>
            <form onSubmit={buscar} className="flex align-items-end gap-2 flex-wrap">
                <div className="flex-1 min-w-0">
                    <label htmlFor="busca-fiscal-termo" className="block mb-2">{t('fiscal.codeOrDescription')}</label>
                    <InputText id="busca-fiscal-termo" value={termo} onChange={(event) => setTermo(event.target.value)} placeholder={t('fiscal.searchPlaceholder')} className="w-full" />
                </div>
                <div>
                    <label htmlFor="busca-fiscal-tabela" className="block mb-2">{t('fiscal.catalog')}</label>
                    <Dropdown id="busca-fiscal-tabela" value={tabela} options={catalogos} onChange={(event) => setTabela(event.value)} />
                </div>
                <Button type="submit" label={t('common.search')} icon="pi pi-search" loading={loading} />
            </form>
            {erro && <small className="p-error block mt-2">{erro}</small>}
            <DataTable value={resultados} loading={loading} emptyMessage={t('common.noResults')} className="p-datatable-sm mt-3" responsiveLayout="scroll">
                <Column field="tabela" header={t('fiscal.catalog')} body={(row) => <Tag value={row.tabela?.toUpperCase()} severity="info" />} />
                <Column field="codigo" header={t('common.code')} />
                <Column field="descricao" header={t('common.description')} />
                <Column field="motivo" header={t('fiscal.match')} />
            </DataTable>
        </Card>
    );
};

export default BuscaFiscal;
