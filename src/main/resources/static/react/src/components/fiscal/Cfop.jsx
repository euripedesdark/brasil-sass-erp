import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';
import { CfopService } from '../../services/CfopService';
import './Cfop.css';

export const Cfop = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [cfopList, setCfopList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [tipoOperacao, setTipoOperacao] = useState('');
    const [search, setSearch] = useState('');

    const tipoOptions = [
        { label: 'Todos', value: '' },
        { label: 'Entrada', value: 'ENTRADA' },
        { label: 'Saida', value: 'SAIDA' }
    ];

    useEffect(() => {
        fetchCfop();
    }, [tipoOperacao]);

    const fetchCfop = async () => {
        setLoading(true);
        try {
            const data = await CfopService.listar(tipoOperacao);
            setCfopList(data || []);
        } catch (err) {
            console.error('Erro ao carregar CFOP', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar a tabela CFOP',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    // Filtra codigo e descricao no cliente.
    //
    // Antes o campo de busca estava no template sem `value` e sem `onChange`:
    // digitava-se nele e nada acontecia, a tabela mostrava as 619 linhas
    // sempre. O mesmo bug da tela de CEST, que ainda tem `searchCodigo` morto
    // no historico do git.
    //
    // Aqui da para filtrar no cliente porque sao 619 linhas e o endpoint nao
    // pagina — o server inteiro cabe em memoria e o filtro responde na hora.
    // A tela de CEST e o caso oposto: 1.043 linhas paginadas de 50, e la o
    // filtro precisou ir para o servidor.
    const filtrados = useMemo(() => {
        const termo = search.trim().toLowerCase();
        if (!termo) return cfopList;
        return cfopList.filter((c) => {
            const codigo = String(c.codigo || '');
            const descricao = String(c.descricao || '').toLowerCase();
            return codigo.includes(termo) || descricao.includes(termo);
        });
    }, [cfopList, search]);

    const header = (
        <div className="cfop-header flex justify-content-between align-items-center">
            <span className="p-input-icon-left flex-1">
                <i className="pi pi-search" />
                <InputText
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Buscar por codigo ou descricao..."
                    className="w-full"
                />
            </span>
            {search && (
                <span className="text-muted text-sm ml-2" style={{ whiteSpace: 'nowrap' }}>
                    {filtrados.length} de {cfopList.length}
                </span>
            )}
            <span className="ml-3">
                <Dropdown
                    value={tipoOperacao}
                    options={tipoOptions}
                    onChange={(e) => setTipoOperacao(e.value)}
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Filtrar por tipo"
                />
            </span>
        </div>
    );

    return (
        <div className="cfop-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="CFOP - Codigo Fiscal de Operacoes e Prestacoes" className="cfop-main-card">
                <div className="cfop-header-actions mb-4">
                    <p className="text-muted m-0">Codigos fiscais para operacoes de entrada e saida.</p>
                </div>

                <DataTable
                    value={filtrados}
                    header={header}
                    loading={loading}
                    paginator
                    rows={20}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={filtrados.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={search ? `Nenhum CFOP para "${search}"` : 'Nenhum CFOP encontrado'}
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '100px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '500px' }} />
                    <Column field="tipoOperacao" header="Tipo" sortable style={{ width: '150px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Cfop;
