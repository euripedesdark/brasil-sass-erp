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
import { useAuth } from '../../contexts/AuthContext';
import { FolhaPagamentoService } from '../../services/FolhaPagamentoService';

export const FolhaPagamento = () => {
    const { user } = useAuth();
    const toast = useRef(null);
    // trava a linha durante a acao: processar gera titulo a pagar no financeiro,
    // e dois cliques criariam dois titulos para a mesma folha
    const [emAcao, setEmAcao] = useState(null);
    const [folhas, setFolhas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const statusOptions = [
        { label: 'ABERTA', value: 'ABERTA' },
        { label: 'FECHADA', value: 'FECHADA' },
        { label: 'PROCESSADA', value: 'PROCESSADA' },
        { label: 'CANCELADA', value: 'CANCELADA' }
    ];

    const [novaFolha, setNovaFolha] = useState({
        id: null,
        competencia: '',
        status: 'ABERTA',
        valorTotal: 0
    });

    useEffect(() => {
        fetchFolhas();
    }, []);

    const fetchFolhas = async () => {
        setLoading(true);
        try {
            const data = await FolhaPagamentoService.listar();
            setFolhas(data || []);
        } catch (err) {
            console.error('Erro ao carregar folhas de pagamento', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar as folhas de pagamento',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const salvarFolha = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const folhaParaSalvar = {
                ...novaFolha
            };

            if (folhaParaSalvar.id) {
                await FolhaPagamentoService.atualizar(folhaParaSalvar.id, folhaParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Folha de pagamento atualizada com sucesso',
                    life: 3000
                });
            } else {
                await FolhaPagamentoService.criar(folhaParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Folha de pagamento criada com sucesso',
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchFolhas();
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || 'Erro ao salvar folha de pagamento');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel salvar a folha de pagamento',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirFolha = async (id) => {
        try {
            await FolhaPagamentoService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: 'Sucesso',
                detail: 'Folha de pagamento excluida com sucesso',
                life: 3000
            });
            fetchFolhas();
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel excluir a folha de pagamento',
                life: 3000
            });
        }
    };

    const statusTemplate = (rowData) => {
        const severity = rowData.status === 'ABERTA' ? 'info' : rowData.status === 'FECHADA' ? 'success' : rowData.status === 'CANCELADA' ? 'danger' : 'warning';
        return <Tag value={rowData.status} severity={severity} />;
    };

    const valorTemplate = (rowData) => {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(rowData.valorTotal || 0);
    };

    const abrirDialog = (folha = null) => {
        if (folha) {
            setNovaFolha({
                id: folha.id,
                competencia: folha.competencia || '',
                status: folha.status || 'ABERTA',
                valorTotal: folha.valorTotal || 0
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovaFolha({
            id: null,
            competencia: '',
            status: 'ABERTA',
            valorTotal: 0
        });
        setError('');
        setSuccess(false);
    };

    const mensagemDe = async (erro, padrao) => {
        const j = erro?.response?.data;
        return j?.errors?.[0]?.message || j?.message || padrao;
    };

    /**
     * Processar a folha cria um título a pagar no Financeiro, com vencimento
     * para o dia 5. Sem esse aviso no texto da confirmação, o usuárioProcessa e
     * só depois descobre que mexeu no financeiro.
     */
    const processarFolha = (folha) => {
        if (!window.confirm(
            `Processar a folha ${folha.competencia}?\n\n` +
            'Isso cria um TÍTULO A PAGAR no Financeiro com o valor total da folha, ' +
            'vencimento para o dia 5, e a folha passa a PROCESSADA.'
        )) return;
        setEmAcao(folha.id);
        FolhaPagamentoService.processar(folha.id)
            .then(() => {
                toast.current?.show({
                    severity: 'success', summary: 'Folha processada',
                    detail: 'Título a pagar criado no Financeiro', life: 4500
                });
                fetchFolhas();
            })
            .catch(async (e) => toast.current?.show({
                severity: 'error', summary: 'Não foi possível processar',
                detail: await mensagemDe(e, 'Falha ao processar a folha'), life: 5000
            }))
            .finally(() => setEmAcao(null));
    };

    const [dlgEnc, setDlgEnc] = useState(false);
    const [enc, setEnc] = useState(null);
    const [aliq, setAliq] = useState({ inss: 20, fgts: 8, rat: 2 });
    const [encFolha, setEncFolha] = useState(null);
    const abrirEncargos = async (folha) => { setEncFolha(folha); setEnc(null); setDlgEnc(true); recalcEncargos(folha.id, { inss: 20, fgts: 8, rat: 2 }); };
    const recalcEncargos = async (id, a) => { try { setEnc(await FolhaPagamentoService.encargos(id, a)); } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha nos encargos', life: 4000 }); } };
    const cancelarFolha = (folha) => {
        if (!window.confirm(`Cancelar a folha ${folha.competencia}? Ela deixa de valer para pagamento.`)) return;
        setEmAcao(folha.id);
        FolhaPagamentoService.cancelar(folha.id)
            .then(() => {
                toast.current?.show({
                    severity: 'success', summary: 'Folha cancelada', life: 3500
                });
                fetchFolhas();
            })
            .catch(async (e) => toast.current?.show({
                severity: 'error', summary: 'Não foi possível cancelar',
                detail: await mensagemDe(e, 'Falha ao cancelar a folha'), life: 5000
            }))
            .finally(() => setEmAcao(null));
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="folhapagamento-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                    disabled={rowData.status === 'PAGA' || rowData.status === 'CANCELADA'}
                />
                <Button
                    icon="pi pi-play"
                    className="p-button-success p-button-sm p-button-text"
                    onClick={() => processarFolha(rowData)}
                    tooltip="Processar — gera título a pagar no Financeiro"
                    loading={emAcao === rowData.id}
                    // o backend só processa folha ABERTA; desabilitar aqui evita
                    // o clique para receber "Apenas folhas ABERTAS podem ser processadas"
                    disabled={emAcao === rowData.id || rowData.status !== 'ABERTA'}
                />
                <Button
                    icon="pi pi-ban"
                    className="p-button-warning p-button-sm p-button-text"
                    onClick={() => cancelarFolha(rowData)}
                    tooltip="Cancelar"
                    // PAGA é barrada pelo backend com mensagem própria
                    disabled={emAcao === rowData.id || rowData.status === 'CANCELADA' || rowData.status === 'PAGA'}
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirFolha(rowData.id)}
                    tooltip="Excluir"
                />
                <Button
                    icon="pi pi-calculator"
                    className="p-button-help p-button-sm p-button-text"
                    onClick={() => abrirEncargos(rowData)}
                    tooltip="Encargos"
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
                label="Salvar Folha"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarFolha}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="folhapagamento-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Folhas de Pagamento" className="folhapagamento-main-card">
                <div className="folhapagamento-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="folhapagamento-info">
                        <p className="text-muted m-0">Gestao de folhas de pagamento mensais.</p>
                    </div>
                    <Button
                        label="Nova Folha"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={folhas}
                    loading={loading}
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={folhas.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhuma folha de pagamento encontrada"
                >
                    <Column field="competencia" header="Competencia" sortable style={{ width: '150px' }} />
                    <Column field="status" header="Status" body={statusTemplate} sortable style={{ width: '150px' }} />
                    <Column field="valorTotal" header="Valor Total" body={valorTemplate} sortable style={{ width: '150px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novaFolha.id ? `Editar Folha: ${novaFolha.competencia}` : 'Nova Folha de Pagamento'}
                visible={dialogVisible}
                style={{ width: '500px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Folha salva com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Competencia *</label>
                            <InputText
                                value={novaFolha.competencia}
                                onChange={(e) => setNovaFolha({...novaFolha, competencia: e.target.value})}
                                placeholder="MM/AAAA"
                                required
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <Dropdown
                                value={novaFolha.status}
                                options={statusOptions}
                                onChange={(e) => setNovaFolha({...novaFolha, status: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Valor Total</label>
                            <InputNumber
                                value={novaFolha.valorTotal}
                                onChange={(e) => setNovaFolha({...novaFolha, valorTotal: e.value})}
                                mode="currency"
                                currency="BRL"
                                locale="pt-BR"
                            />
                        </div>
                    </div>

                    <Divider />
                    <div className="folhapagamento-resumo flex flex-wrap justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Competencia: </span>
                            <span>{novaFolha.competencia}</span>
                        </div>
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Status: </span>
                            <Tag value={novaFolha.status} severity={statusOptions.find(s => s.value === novaFolha.status)?.value === 'ABERTA' ? 'info' : 'success'} />
                        </div>
                        <div className="flex-1 min-w-max">
                            <span className="font-bold">Valor: </span>
                            <span>{new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(novaFolha.valorTotal || 0)}</span>
                        </div>
                    </div>
                </div>
            </Dialog>
            <Dialog visible={dlgEnc} onHide={() => setDlgEnc(false)} header='Encargos patronais' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-4'><label className='bc-label'>INSS %</label><InputNumber value={aliq.inss} onValueChange={(e) => { const a = { ...aliq, inss: e.value }; setAliq(a); if (encFolha) recalcEncargos(encFolha.id, a); }} suffix=' %' minFractionDigits={2} /></div>
                    <div className='bc-form-col-4'><label className='bc-label'>FGTS %</label><InputNumber value={aliq.fgts} onValueChange={(e) => { const a = { ...aliq, fgts: e.value }; setAliq(a); if (encFolha) recalcEncargos(encFolha.id, a); }} suffix=' %' minFractionDigits={2} /></div>
                    <div className='bc-form-col-4'><label className='bc-label'>RAT %</label><InputNumber value={aliq.rat} onValueChange={(e) => { const a = { ...aliq, rat: e.value }; setAliq(a); if (encFolha) recalcEncargos(encFolha.id, a); }} suffix=' %' minFractionDigits={2} /></div>
                </div>
                {enc && (<div className='mt-3'><p>Base: <strong>{Number(enc.base ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</strong></p><p>INSS: <strong>{Number(enc.inssPatronal ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</strong></p><p>FGTS: <strong>{Number(enc.fgts ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</strong></p><p>RAT: <strong>{Number(enc.rat ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</strong></p><p>Total: <strong>{Number(enc.total ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</strong></p></div>)}
                <div className='flex justify-end mt-3'><Button label='Fechar' text onClick={() => setDlgEnc(false)} /></div>
            </Dialog>
        </div>
    );
};

export default FolhaPagamento;
