import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { Button } from 'primereact/button';
import { Tag } from 'primereact/tag';
import { EntradaNotaService } from '../../services/EntradaNotaService';
import { ImportarNotaXml } from './ImportarNotaXml';

export const EntradaNota = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [entradas, setEntradas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    // O campo de busca estava no cabecalho sem `value` e sem `onChange`, que
    // e o mesmo defeito do campo de busca de CFOP: o input aparecia, o
    // usuario digitava, e nada acontecia. Nao ha filtro no endpoint de
    // listagem, entao a busca aqui e no client, sobre a pagina carregada.
    const [busca, setBusca] = useState('');
    const [importando, setImportando] = useState(false);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 20,
        sortField: 'dataEmissao',
        sortOrder: -1
    });

    useEffect(() => {
        fetchEntradas();
    }, [lazyParams]);

    const fetchEntradas = async () => {
        setLoading(true);
        try {
            const data = await EntradaNotaService.listar(lazyParams.page, lazyParams.rows);
            setEntradas(data.content || data || []);
            setTotalRecords(data.totalElements || data.length || 0);
        } catch (err) {
            console.error('Erro ao carregar entradas de notas', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar as entradas de notas',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    // Filtra no client, sobre a pagina que esta na tela. Chamar isso de
    // "busca" e exagero: ele nao acha nota de outra pagina. Por isso o
    // placeholder diz "nesta pagina" e nao "no sistema" — a diferenca e
    // exatamente o que faz o usuario procurar a chave e nao achar.
    const norm = (v) => (v || '').toString().toLowerCase().replace(/\D/g, '');
    const listaFiltrada = entradas.filter((e) => {
        if (!busca) return true;
        const alvo = norm(busca);
        if (!alvo) return true;
        return norm(e.chaveAcesso).includes(alvo) || norm(e.status).includes(busca.toLowerCase());
    });

    const onLazyLoad = (event) => {
        setLazyParams({
            page: event.page,
            rows: event.rows,
            sortField: event.sortField || 'dataEmissao',
            sortOrder: event.sortOrder || -1
        });
    };

    const valorTemplate = (rowData) => {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(rowData.valorTotal || 0);
    };

    const statusTemplate = (rowData) => {
        const s = rowData.status;
        if (!s) return <Tag severity="warning" value="Pendente" />;
        // A distincao importa: IMPORTADA_XML tem os dados que a prefeitura
        // validou, DIGITADA sao numeros que alguem digitou. Um relatorio que
        // misturar os dois soma duas qualidades diferentes de informacao.
        return <Tag
            severity={s === 'IMPORTADA_XML' ? 'success' : s === 'CANCELADA' ? 'danger' : 'info'}
            value={s}
        />;
    };

    const header = (
        <div className="entradanota-header flex justify-content-between align-items-center flex-wrap gap-2">
            <span className="p-input-icon-left">
                <i className="pi pi-search" />
                <InputText
                    value={busca}
                    onChange={(e) => setBusca(e.target.value)}
                    placeholder="Buscar chave nesta pagina..."
                    className="w-full"
                />
            </span>
            <Button
                label="Importar XML"
                icon="pi pi-upload"
                onClick={() => setImportando(true)}
            />
        </div>
    );

    return (
        <div className="entradanota-enterprise-container">
            <Toast ref={toast} />

            <Card title="Entradas de Notas Fiscais" className="entradanota-main-card">
                <div className="entradanota-header-actions mb-4">
                    <p className="text-muted m-0">Registro de notas fiscais de entrada do sistema.</p>
                </div>

                <DataTable
                    value={listaFiltrada}
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
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhuma entrada de nota encontrada"
                >
                    <Column field="chaveAcesso" header="Chave de Acesso" sortable style={{ width: '250px' }} />
                    <Column field="pessoaId" header="Fornecedor ID" sortable style={{ width: '100px' }} />
                    <Column field="valorTotal" header="Valor Total" body={valorTemplate} sortable style={{ width: '150px' }} />
                    <Column field="status" header="Status" body={statusTemplate} sortable style={{ width: '150px' }} />
                    <Column field="dataEmissao" header="Data Emissao" sortable style={{ width: '150px' }} />
                </DataTable>
            </Card>
            <ImportarNotaXml
                visivel={importando}
                onHide={() => setImportando(false)}
                onConfirmado={() => {
                    setImportando(false);
                    setBusca('');
                    fetchEntradas();
                }}
            />
        </div>
    );
};

export default EntradaNota;
