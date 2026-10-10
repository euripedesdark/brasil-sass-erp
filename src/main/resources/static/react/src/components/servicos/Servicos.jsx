import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { InputTextarea } from 'primereact/inputtextarea';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';
import './Servicos.css';

export const Servicos = () => {
    const { user } = useAuth();
    const toast = useRef(null);
    const [servicos, setServicos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 10,
        sortField: 'nome',
        sortOrder: 1
    });

    const [novoServico, setNovoServico] = useState({
        id: null,
        codigo: '',
        nome: '',
        descricao: '',
        valorPadrao: 0,
        duracaoPadrao: 0,
        unidadeDuracao: 'HORA',
        ativo: true,
        categoria: 'MANUTENCAO'
    });

    const categoriaOptions = [
        { label: 'Manutencao', value: 'MANUTENCAO' },
        { label: 'Consultoria', value: 'CONSULTORIA' },
        { label: 'Instalacao', value: 'INSTALACAO' },
        { label: 'Treinamento', value: 'TREINAMENTO' },
        { label: 'Suporte', value: 'SUPORTE' },
        { label: 'Outros', value: 'OUTROS' }
    ];

    const unidadeOptions = [
        { label: 'Hora', value: 'HORA' },
        { label: 'Dia', value: 'DIA' },
        { label: 'Semana', value: 'SEMANA' },
        { label: 'Mes', value: 'MES' },
        { label: 'Unidade', value: 'UNIDADE' }
    ];

    useEffect(() => {
        fetchServicos();
    }, [lazyParams]);

    const fetchServicos = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/servicos?${params}`);
            if (response.ok) {
                const data = await response.json();
                const servicosData = data.data?.content || data.content || data || [];
                setServicos(servicosData);
                setTotalRecords(data.data?.totalElements || data.totalElements || servicosData.length || 0);
            } else {
                console.error('Erro ao carregar servicos');
            }
        } catch (err) {
            console.error('Erro ao carregar servicos', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar os servicos',
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
            sortField: event.sortField || 'nome',
            sortOrder: event.sortOrder || 1
        });
    };

    const formatarMoeda = (valor) => {
        if (valor === null || valor === undefined) return 'R$ 0,00';
        return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    const formatarDuracao = (valor, unidade) => {
        if (valor === null || valor === undefined) return '0';
        const unidades = {
            'HORA': 'h',
            'DIA': 'd',
            'SEMANA': 's',
            'MES': 'm',
            'UNIDADE': 'un'
        };
        return `${valor} ${unidades[unidade] || ''}`;
    };

    const salvarServico = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const servicoParaSalvar = {
                ...novoServico,
                empresaId: user?.empresaId
            };

            const method = servicoParaSalvar.id ? 'PUT' : 'POST';
            const url = servicoParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/cadastro/servicos/${servicoParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/cadastro/servicos`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(servicoParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Servico ${servicoParaSalvar.id ? 'atualizado' : 'criado'} com sucesso`,
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchServicos();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel salvar o servico',
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar servico');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao salvar servico',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirServico = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/servicos/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Servico excluido com sucesso',
                    life: 3000
                });
                fetchServicos();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel excluir o servico',
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao excluir servico',
                life: 3000
            });
        }
    };

    const abrirDialog = (servico = null) => {
        if (servico) {
            setNovoServico({
                id: servico.id,
                codigo: servico.codigo || '',
                nome: servico.nome || '',
                descricao: servico.descricao || '',
                valorPadrao: servico.valorPadrao || 0,
                duracaoPadrao: servico.duracaoPadrao || 0,
                unidadeDuracao: servico.unidadeDuracao || 'HORA',
                ativo: servico.ativo !== false,
                categoria: servico.categoria || 'MANUTENCAO'
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoServico({
            id: null,
            codigo: '',
            nome: '',
            descricao: '',
            valorPadrao: 0,
            duracaoPadrao: 0,
            unidadeDuracao: 'HORA',
            ativo: true,
            categoria: 'MANUTENCAO'
        });
        setError('');
        setSuccess(false);
    };

    const statusTemplate = (rowData) => {
        return (
            <Tag 
                value={rowData.ativo ? 'Ativo' : 'Inativo'}
                severity={rowData.ativo ? 'success' : 'danger'}
            />
        );
    };

    const categoriaTemplate = (rowData) => {
        const categoriaMap = {
            'MANUTENCAO': 'Manutencao',
            'CONSULTORIA': 'Consultoria',
            'INSTALACAO': 'Instalacao',
            'TREINAMENTO': 'Treinamento',
            'SUPORTE': 'Suporte',
            'OUTROS': 'Outros'
        };
        return <span>{categoriaMap[rowData.categoria] || rowData.categoria}</span>;
    };

    const duracaoTemplate = (rowData) => {
        return formatarDuracao(rowData.duracaoPadrao, rowData.unidadeDuracao);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="servicos-acoes">
                <Button
                    icon="pi pi-eye"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Visualizar"
                />
                <Button
                    icon="pi pi-pencil"
                    className="p-button-primary p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirServico(rowData.id)}
                    tooltip="Excluir"
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label="Cancelar"
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
            />
            <Button
                label="Salvar Servico"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarServico}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="servicos-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Modulo de Servicos" className="servicos-main-card">
                <div className="servicos-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="servicos-info">
                        <p className="text-muted m-0">Cadastro e gestao de servicos ofertados pela empresa.</p>
                    </div>
                    <Button
                        label="Novo Servico"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={servicos}
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
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} servicos"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum servico encontrado"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '100px' }} />
                    <Column field="nome" header="Nome" sortable style={{ width: '200px' }} />
                    <Column field="categoria" header="Categoria" body={categoriaTemplate} sortable style={{ width: '150px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '250px' }} />
                    <Column field="valorPadrao" header="Valor Padrao" body={formatarMoeda} sortable style={{ width: '120px' }} />
                    <Column field="duracaoPadrao" header="Duracao" body={duracaoTemplate} sortable style={{ width: '100px' }} />
                    <Column field="ativo" header="Status" body={statusTemplate} sortable style={{ width: '100px' }} />
                    <Column body={acoesTemplate} style={{ width: '150px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoServico.id ? `Editar Servico: ${novoServico.nome}` : 'Novo Servico'}
                visible={dialogVisible}
                style={{ width: '700px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Servico salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Codigo</label>
                            <InputText
                                value={novoServico.codigo}
                                onChange={(e) => setNovoServico({...novoServico, codigo: e.target.value})}
                                placeholder="SRV-001"
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Categoria</label>
                            <Dropdown
                                value={novoServico.categoria}
                                options={categoriaOptions}
                                onChange={(e) => setNovoServico({...novoServico, categoria: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Nome do Servico</label>
                            <InputText
                                value={novoServico.nome}
                                onChange={(e) => setNovoServico({...novoServico, nome: e.target.value})}
                                placeholder="Nome do servico"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputTextarea
                                value={novoServico.descricao}
                                onChange={(e) => setNovoServico({...novoServico, descricao: e.target.value})}
                                placeholder="Descricao detalhada do servico"
                                rows={3}
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Valor Padrao</label>
                            <InputNumber
                                value={novoServico.valorPadrao}
                                onValueChange={(e) => setNovoServico({...novoServico, valorPadrao: e.value})}
                                mode="currency"
                                currency="BRL"
                                locale="pt-BR"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Duracao Padrao</label>
                            <InputNumber
                                value={novoServico.duracaoPadrao}
                                onValueChange={(e) => setNovoServico({...novoServico, duracaoPadrao: e.value})}
                                min={0}
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Unidade Duracao</label>
                            <Dropdown
                                value={novoServico.unidadeDuracao}
                                options={unidadeOptions}
                                onChange={(e) => setNovoServico({...novoServico, unidadeDuracao: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 field">
                            <div className="p-checkbox p-col-12">
                                <input
                                    type="checkbox"
                                    id="ativo"
                                    checked={novoServico.ativo}
                                    onChange={(e) => setNovoServico({...novoServico, ativo: e.target.checked})}
                                    className="p-checkbox-input"
                                />
                                <label htmlFor="ativo" className="p-checkbox-label ml-2">
                                    <strong>Servico Ativo</strong>
                                </label>
                            </div>
                        </div>
                    </div>

                    <Divider />
                    <div className="servicos-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Valor: </span>
                            <span>{formatarMoeda(novoServico.valorPadrao)}</span>
                        </div>
                        <div>
                            <span className="font-bold">Duracao: </span>
                            <span>{formatarDuracao(novoServico.duracaoPadrao, novoServico.unidadeDuracao)}</span>
                        </div>
                        <div>
                            <span className="font-bold">Status: </span>
                            <Tag 
                                value={novoServico.ativo ? 'Ativo' : 'Inativo'}
                                severity={novoServico.ativo ? 'success' : 'danger'}
                            />
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Servicos;
