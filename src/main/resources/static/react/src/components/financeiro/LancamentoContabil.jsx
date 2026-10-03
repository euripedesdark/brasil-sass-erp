import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';

export const LancamentoContabil = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [lancamentos, setLancamentos] = useState([]);
    const [partidas, setPartidas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [partidasDialogVisible, setPartidasDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        page: 0,
        rows: 10,
        sortField: 'dataLancamento',
        sortOrder: 1
    });

    const [novoLancamento, setNovoLancamento] = useState({
        id: null,
        descricao: '',
        dataLancamento: new Date(),
        valorTotal: 0,
        planoContasId: null,
        planoContasDescricao: '',
        centroCustoId: null,
        centroCustoDescricao: '',
        historico: '',
        pontidas: []
    });

    const [novaPartida, setNovaPartida] = useState({
        contaId: null,
        contaDescricao: '',
        centroCustoId: null,
        tipo: 'DEBITO',
        valor: 0,
        descricao: ''
    });

    const tipoPartidaOptions = [
        { label: 'Débito', value: 'DEBITO' },
        { label: 'Crédito', value: 'CREDITO' }
    ];

    useEffect(() => {
        fetchLancamentos();
    }, [lazyParams]);

    const fetchLancamentos = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField,
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/lancamentos?${params}`);
            if (response.ok) {
                const data = await response.json();
                setLancamentos(data.content || data);
                setTotalRecords(data.totalElements || data.length || 0);
            } else {
                console.error('Erro ao carregar lancamentos');
            }
        } catch (err) {
            console.error('Erro ao carregar lancamentos', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar os lancamentos contabeis',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const fetchPartidas = async (lancamentoId) => {
        setLoading(true);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/lancamentos/${lancamentoId}/partidas`);
            if (response.ok) {
                const data = await response.json();
                setPartidas(data);
                setPartidasDialogVisible(true);
            } else {
                console.error('Erro ao carregar partidas');
            }
        } catch (err) {
            console.error('Erro ao carregar partidas', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar as partidas',
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
            sortField: event.sortField || 'dataLancamento',
            sortOrder: event.sortOrder || 1
        });
    };

    const formatarMoeda = (valor) => {
        if (valor === null || valor === undefined) return 'R$ 0,00';
        return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    const formatarData = (data) => {
        if (!data) return '';
        return new Date(data).toLocaleDateString('pt-BR');
    };

    const tipoTemplate = (rowData) => {
        const tipoMap = {
            'DEBITO': 'Débito',
            'CREDITO': 'Crédito'
        };
        const severity = rowData.tipo === 'DEBITO' ? 'danger' : 'success';
        return <Tag value={tipoMap[rowData.tipo] || rowData.tipo} severity={severity} />;
    };

    const calcularTotalPartidas = (partidas) => {
        return partidas.reduce((sum, p) => {
            return sum + (p.valor || 0) * (p.tipo === 'CREDITO' ? -1 : 1);
        }, 0);
    };

    const adicionarPartida = () => {
        if (!novaPartida.contaId || novaPartida.valor <= 0) {
            setError('Conta e valor sao obrigatorios');
            return;
        }
        setNovoLancamento({
            ...novoLancamento,
            partidas: [...novoLancamento.partidas, novaPartida]
        });
        setNovaPartida({
            contaId: null,
            contaDescricao: '',
            centroCustoId: null,
            tipo: 'DEBITO',
            valor: 0,
            descricao: ''
        });
        setError('');
    };

    const removerPartida = (index) => {
        const novasPartidas = [...novoLancamento.partidas];
        novasPartidas.splice(index, 1);
        setNovoLancamento({ ...novoLancamento, partidas: novasPartidas });
    };

    const salvarLancamento = async () => {
        if (novoLancamento.partidas.length === 0) {
            setError('Adicione ao menos uma partida');
            return;
        }
        
        const totalDebitos = novoLancamento.partidas
            .filter(p => p.tipo === 'DEBITO')
            .reduce((sum, p) => sum + (p.valor || 0), 0);
        const totalCreditos = novoLancamento.partidas
            .filter(p => p.tipo === 'CREDITO')
            .reduce((sum, p) => sum + (p.valor || 0), 0);
        
        if (totalDebitos !== totalCreditos) {
            setError('Partidas desequilibradas: Soma dos débitos deve ser igual a soma dos créditos');
            return;
        }

        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const lancamentoParaSalvar = {
                ...novoLancamento,
                dataLancamento: new Date(novoLancamento.dataLancamento).toISOString(),
                empresaId: user?.empresaId,
                usuarioId: user?.id,
                partidas: novoLancamento.partidas.map(p => ({
                    ...p,
                    valor: Number(p.valor)
                }))
            };

            const method = lancamentoParaSalvar.id ? 'PUT' : 'POST';
            const url = lancamentoParaSalvar.id 
                ? `${ApiConfig.BASE_URL}/api/financeiro/lancamentos/${lancamentoParaSalvar.id}`
                : `${ApiConfig.BASE_URL}/api/financeiro/lancamentos`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(lancamentoParaSalvar)
            });

            if (response.ok) {
                setSuccess(true);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Lancamento ${lancamentoParaSalvar.id ? 'atualizado' : 'criado'} com sucesso`,
                    life: 3000
                });
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchLancamentos();
                    resetForm();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel salvar o lancamento',
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar lancamento');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao salvar lancamento',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const estornarLancamento = async (id) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/financeiro/lancamentos/${id}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Lancamento estornado com sucesso',
                    life: 3000
                });
                fetchLancamentos();
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || 'Nao foi possivel estornar o lancamento',
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexao ao estornar lancamento',
                life: 3000
            });
        }
    };

    const abrirDialog = (lancamento = null) => {
        if (lancamento) {
            setNovoLancamento({
                id: lancamento.id,
                descricao: lancamento.descricao || '',
                dataLancamento: lancamento.dataLancamento ? new Date(lancamento.dataLancamento) : new Date(),
                valorTotal: lancamento.valorTotal || 0,
                planoContasId: lancamento.planoContas?.id,
                planoContasDescricao: lancamento.planoContas?.descricao || '',
                centroCustoId: lancamento.centroCusto?.id,
                centroCustoDescricao: lancamento.centroCusto?.descricao || '',
                historico: lancamento.historico || '',
                partidas: lancamento.partidas || []
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoLancamento({
            id: null,
            descricao: '',
            dataLancamento: new Date(),
            valorTotal: 0,
            planoContasId: null,
            planoContasDescricao: '',
            centroCustoId: null,
            centroCustoDescricao: '',
            historico: '',
            partidas: []
        });
        setNovaPartida({
            contaId: null,
            contaDescricao: '',
            centroCustoId: null,
            tipo: 'DEBITO',
            valor: 0,
            descricao: ''
        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="lancamento-acoes">
                <Button
                    icon="pi pi-eye"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Visualizar"
                />
                <Button
                    icon="pi pi-list"
                    className="p-button-primary p-button-sm p-button-text"
                    onClick={() => fetchPartidas(rowData.id)}
                    tooltip="Ver Partidas"
                />
                <Button
                    icon="pi pi-times"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => estornarLancamento(rowData.id)}
                    tooltip="Estornar"
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
                label="Salvar Lancamento"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarLancamento}
                loading={loading}
            />
        </div>
    );

    const partidasDialogFooter = (
        <div>
            <Button
                label="Fechar"
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => setPartidasDialogVisible(false)}
            />
        </div>
    );

    return (
        <div className="lancamento-contabil-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Lancamentos Contabeis" className="lancamento-main-card">
                <div className="lancamento-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="lancamento-info">
                        <p className="text-muted m-0">Gestao de lancamentos contabeis com partidas dobradas.</p>
                    </div>
                    <Button
                        label="Novo Lancamento"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={lancamentos}
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
                    currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} lancamentos"
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum lancamento encontrado"
                >
                    <Column field="descricao" header="Descricao" sortable style={{ width: '200px' }} />
                    <Column field="dataLancamento" header="Data" body={formatarData} sortable style={{ width: '120px' }} />
                    <Column field="valorTotal" header="Valor Total" body={formatarMoeda} sortable style={{ width: '120px' }} />
                    <Column field="planoContas.descricao" header="Plano de Contas" sortable style={{ width: '200px' }} />
                    <Column field="centroCusto.descricao" header="Centro de Custo" sortable style={{ width: '200px' }} />
                    <Column field="historico" header="Historico" sortable style={{ width: '200px' }} />
                    <Column body={acoesTemplate} style={{ width: '180px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoLancamento.id ? `Editar Lancamento: ${novoLancamento.descricao}` : 'Novo Lancamento Contabil'}
                visible={dialogVisible}
                style={{ width: '900px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Lancamento salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputText
                                value={novoLancamento.descricao}
                                onChange={(e) => setNovoLancamento({...novoLancamento, descricao: e.target.value})}
                                placeholder="Descricao do lancamento"
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Data do Lancamento</label>
                            <Calendar
                                value={novoLancamento.dataLancamento}
                                onChange={(e) => setNovoLancamento({...novoLancamento, dataLancamento: e.value})}
                                dateFormat="dd/mm/yy"
                                showIcon
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Historico</label>
                            <InputText
                                value={novoLancamento.historico}
                                onChange={(e) => setNovoLancamento({...novoLancamento, historico: e.target.value})}
                                placeholder="Historico do lancamento"
                            />
                        </div>
                    </div>

                    <Divider />
                    <h4>Partidas Contabeis</h4>
                    
                    <div className="partidas-grid grid mb-4">
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Conta ID</label>
                            <InputNumber
                                value={novaPartida.contaId}
                                onValueChange={(e) => setNovaPartida({...novaPartida, contaId: e.value})}
                                placeholder="ID da conta"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Conta Descricao</label>
                            <InputText
                                value={novaPartida.contaDescricao}
                                onChange={(e) => setNovaPartida({...novaPartida, contaDescricao: e.target.value})}
                                placeholder="Descricao da conta"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Tipo</label>
                            <Dropdown
                                value={novaPartida.tipo}
                                options={tipoPartidaOptions}
                                onChange={(e) => setNovaPartida({...novaPartida, tipo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Valor</label>
                            <InputNumber
                                value={novaPartida.valor}
                                onValueChange={(e) => setNovaPartida({...novaPartida, valor: e.value})}
                                mode="currency"
                                currency="BRL"
                                locale="pt-BR"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Centro Custo ID</label>
                            <InputNumber
                                value={novaPartida.centroCustoId}
                                onValueChange={(e) => setNovaPartida({...novaPartida, centroCustoId: e.value})}
                                placeholder="ID do centro de custo"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Descricao Partida</label>
                            <InputText
                                value={novaPartida.descricao}
                                onChange={(e) => setNovaPartida({...novaPartida, descricao: e.target.value})}
                                placeholder="Descricao da partida"
                            />
                        </div>
                        <div className="col-12 field">
                            <Button
                                label="Adicionar Partida"
                                icon="pi pi-plus"
                                className="p-button-success"
                                onClick={adicionarPartida}
                            />
                        </div>
                    </div>

                    {novoLancamento.partidas.length > 0 && (
                        <div className="partidas-list mb-4">
                            <h5>Partidas Adicionadas</h5>
                            <DataTable
                                value={novoLancamento.partidas}
                                className="p-datatable-sm"
                                emptyMessage="Nenhuma partida adicionada"
                            >
                                <Column field="contaDescricao" header="Conta" style={{ width: '200px' }} />
                                <Column field="tipo" header="Tipo" body={tipoTemplate} style={{ width: '100px' }} />
                                <Column field="valor" header="Valor" body={formatarMoeda} style={{ width: '120px' }} />
                                <Column field="descricao" header="Descricao" style={{ width: '200px' }} />
                                <Column
                                    body={(rowData, { rowIndex }) => (
                                        <Button
                                            icon="pi pi-trash"
                                            className="p-button-danger p-button-sm"
                                            onClick={() => removerPartida(rowIndex)}
                                            tooltip="Remover"
                                        />
                                    )}
                                    style={{ width: '80px' }}
                                />
                            </DataTable>
                            <div className="mt-3">
                                <strong>Total: {formatarMoeda(calcularTotalPartidas(novoLancamento.partidas))}</strong>
                                {calcularTotalPartidas(novoLancamento.partidas) !== 0 && (
                                    <Tag 
                                        value="Desequilibrado" 
                                        severity="danger" 
                                        className="ml-3"
                                    />
                                )}
                                {calcularTotalPartidas(novoLancamento.partidas) === 0 && novoLancamento.partidas.length > 0 && (
                                    <Tag 
                                        value="Equilibrado" 
                                        severity="success" 
                                        className="ml-3"
                                    />
                                )}
                            </div>
                        </div>
                    )}

                    <Divider />
                    <div className="lancamento-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Data: </span>
                            <span>{formatarData(novoLancamento.dataLancamento)}</span>
                        </div>
                        <div>
                            <span className="font-bold">Partidas: </span>
                            <span>{novoLancamento.partidas.length}</span>
                        </div>
                        <div>
                            <span className="font-bold">Status: </span>
                            {calcularTotalPartidas(novoLancamento.partidas) === 0 && novoLancamento.partidas.length > 0 ? (
                                <Tag value="Equilibrado" severity="success" />
                            ) : (
                                <Tag value="Desequilibrado" severity="danger" />
                            )}
                        </div>
                    </div>
                </div>
            </Dialog>

            <Dialog
                header="Partidas do Lancamento"
                visible={partidasDialogVisible}
                style={{ width: '800px' }}
                onHide={() => setPartidasDialogVisible(false)}
                footer={partidasDialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    <DataTable
                        value={partidas}
                        loading={loading}
                        paginator
                        rows={10}
                        responsiveLayout="scroll"
                        className="p-datatable-sm"
                        emptyMessage="Nenhuma partida encontrada"
                    >
                        <Column field="conta.descricao" header="Conta" style={{ width: '200px' }} />
                        <Column field="tipo" header="Tipo" body={tipoTemplate} style={{ width: '100px' }} />
                        <Column field="valor" header="Valor" body={formatarMoeda} style={{ width: '120px' }} />
                        <Column field="descricao" header="Descricao" style={{ width: '200px' }} />
                        <Column field="centroCusto.descricao" header="Centro Custo" style={{ width: '150px' }} />
                    </DataTable>
                </div>
            </Dialog>
        </div>
    );
};

export default LancamentoContabil;
