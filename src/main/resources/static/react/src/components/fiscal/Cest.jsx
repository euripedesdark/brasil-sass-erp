import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';
import { CestService } from '../../services/CestService';
import './Cest.css';

export const Cest = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [cestList, setCestList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState('');
    const [debounced, setDebounced] = useState('');
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 50,
        sortField: 'codigo',
        sortOrder: 1
    });

    // Espera a digitacao parar antes de ir ao banco. Sem isto, cada tecla
    // dispara uma consulta com o LIKE e o Postgres responde a 1.043 linhas
    // para 7 consultas que ninguem vai ler.
    useEffect(() => {
        const t = setTimeout(() => setDebounced(search), 400);
        return () => clearTimeout(t);
    }, [search]);

    useEffect(() => {
        fetchCest(lazyParams.page, lazyParams.rows, debounced);
        // debounced e o que manda: search muda a cada tecla e nao deve
        // consultar. sortField/sortOrder entram para que ordenar por outra
        // coluna recarregue do servidor em vez de ordenar so a pagina.
    }, [lazyParams, debounced]);

    // Paginacao no servidor, filtro no servidor tambem.
    //
    // Antes eram 1.043 linhas carregadas de uma vez e o filtro aplicado aqui.
    // Com a tabela paginada pelo endpoint em 50 por pagina, o filtro de cliente
    // so via sobre a pagina atual: quem buscava um CEST que estava na pagina 7
    // recebia "nenhum encontrado", com a linha existindo no banco. Alem disso o
    // endpoint devolve 50 e a tela achava que eram 50, enquanto o rodape dizia
    // "1-50 de 50" para 1.043 registros.
    const fetchCest = async (page, rows, termo) => {
        setLoading(true);
        try {
            const data = await CestService.listar(page, rows, termo);
            setCestList(data.content || data || []);
            setTotalRecords(data.totalElements || (Array.isArray(data) ? data.length : 0));
        } catch (err) {
            console.error('Erro ao carregar CEST', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar a tabela CEST',
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

    // Volta para a primeira pagina ao mudar o termo. Sem isso, quem busca
    // estando na pagina 5 fica em "nenhum encontrado" mesmo com o registro
    // existente — o filtro rodou, mas a pagina 5 e da tabela inteira.
    //
    // So recria o objeto quando a pagina realmente muda: recriar com os mesmos
    // valores dispara o efeito de novo e faz uma consulta repetida, porque o
    // efeito compara por identidade, nao por conteudo.
    const onSearch = () => {
        if (lazyParams.page !== 0) {
            setLazyParams({ ...lazyParams, page: 0 });
        }
    };

    const header = (
        <div className="cest-header flex justify-content-between align-items-center">
            <span className="p-input-icon-left">
                <i className="pi pi-search" />
                <InputText
                    value={search}
                    onChange={(e) => {
                        setSearch(e.target.value);
                        setDebounced(e.target.value);
                    }}
                    onKeyPress={(e) => e.key === 'Enter' && onSearch()}
                    placeholder="Buscar por codigo, descricao ou NCM..."
                />
            </span>
            <span className="text-muted text-sm ml-3">
                {totalRecords} CEST
            </span>
        </div>
    );

    return (
        <div className="cest-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="CEST - Código Especificador da Substituição Tributária" className="cest-main-card">
                <div className="cest-header-actions mb-4">
                    <p className="text-muted m-0">Tabela de codigos CEST para substituicao tributaria.</p>
                </div>

                <DataTable
                    value={cestList}
                    header={header}
                    loading={loading}
                    lazy
                    paginator
                    first={lazyParams.page * lazyParams.rows}
                    rows={lazyParams.rows}
                    onPage={onLazyLoad}
                    sortField={lazyParams.sortField}
                    sortOrder={lazyParams.sortOrder}
                    totalRecords={totalRecords}
                    rowsPerPageOptions={[50, 100, 200]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={debounced ? `Nenhum CEST para "${debounced}"` : 'Nenhum CEST encontrado'}
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '100px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '400px' }} />
                    <Column field="ncm" header="NCM Relacionado" sortable style={{ width: '150px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Cest;
