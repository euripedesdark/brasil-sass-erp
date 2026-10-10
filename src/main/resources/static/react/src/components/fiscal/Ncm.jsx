import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';
import { NcmService } from '../../services/NcmService';
import './Ncm.css';

export const Ncm = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [ncmList, setNcmList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchDescricao, setSearchDescricao] = useState('');
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 50,
        sortField: 'codigo',
        sortOrder: 1
    });

    useEffect(() => {
        fetchNcm();
    }, [lazyParams, searchDescricao]);

    const fetchNcm = async () => {
        setLoading(true);
        try {
            const data = await NcmService.listar(lazyParams.page, lazyParams.rows, searchDescricao);
            setNcmList(data.content || data || []);
            setTotalRecords(data.totalElements || data.length || 0);
        } catch (err) {
            console.error('Erro ao carregar NCM', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar a tabela NCM',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const onLazyLoad = (event) => {
        setLazyParams({
            page: event.page,
            rows: event.rows,
            sortField: event.sortField || 'codigo',
            sortOrder: event.sortOrder || 1
        });
    };

    const onSearch = () => {
        setLazyParams({ ...lazyParams, page: 0 });
    };

    const aliquotaIpiTemplate = (rowData) => {
        return rowData.aliquotaIpi ? `${rowData.aliquotaIpi}%` : '0%';
    };

    const header = (
        <div className="ncm-header flex justify-content-between align-items-center">
            <span className="p-input-icon-left">
                <i className="pi pi-search" />
                <InputText
                    value={searchDescricao}
                    onChange={(e) => setSearchDescricao(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && onSearch()}
                    placeholder="Buscar por descricao..."
                />
            </span>
        </div>
    );

    return (
        <div className="ncm-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="NCM - Nomenclatura Comum do Mercosul" className="ncm-main-card">
                <div className="ncm-header-actions mb-4">
                    <p className="text-muted m-0">Tabela oficial de classificacao fiscal de produtos.</p>
                </div>

                <DataTable
                    value={ncmList}
                    header={header}
                    loading={loading}
                    paginator
                    lazy
                    first={lazyParams.page * lazyParams.rows}
                    rows={lazyParams.rows}
                    totalRecords={totalRecords}
                    onPage={onLazyLoad}
                    onSort={onLazyLoad}
                    sortField={lazyParams.sortField}
                    sortOrder={lazyParams.sortOrder}
                    paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} registros"
                    rowsPerPageOptions={[20, 50, 100, 200]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum NCM encontrado"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '100px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '500px' }} />
                    <Column field="aliquotaIpi" header="Aliquota IPI" body={aliquotaIpiTemplate} sortable style={{ width: '120px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Ncm;
