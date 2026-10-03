import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { ImagemRegistro } from '../shared/ImagemRegistro';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { InputMask } from 'primereact/inputmask';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { useAuth } from '../../contexts/AuthContext';
import { ClienteService } from '../../services/ClienteService';
import { desembrulharLista } from '../../services/ApiConfig';
import { useTranslation } from 'react-i18next';

export const Cliente = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const toast = useRef(null);
    const [clientes, setNovoClientes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [first, setFirst] = useState(0);
    const [rows, setRows] = useState(20);

    const tipoPessoaOptions = [
        { label: 'FISICA', value: 'FISICA' },
        { label: 'JURIDICA', value: 'JURIDICA' }
    ];

    const [novoCliente, setNovoCliente] = useState({
        id: null,
        nome: '',
        cpfCnpj: '',
        rgIe: '',
        tipoPessoa: 'FISICA',
        telefone: '',
        email: '',
        endereco: '',
        limiteCredito: 0
    });

    useEffect(() => {
        fetchClientes(0, rows);
    }, []);

    const fetchClientes = async (page, size) => {
        setLoading(true);
        try {
            const { lista, total } = desembrulharLista(await ClienteService.listar(page, size));
            setNovoClientes(lista);
            setTotalRecords(total);
        } catch (err) {
            console.error('Erro ao carregar clientes', err);
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('customer.loadError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const onPageChange = async (event) => {
        setFirst(event.first);
        setRows(event.rows);
        await fetchClientes(event.page, event.rows);
    };

    const salvarCliente = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const clienteParaSalvar = {
                ...novoCliente,
                empresaId: user?.empresaId
            };

            if (clienteParaSalvar.id) {
                const response = await ClienteService.atualizar(clienteParaSalvar.id, clienteParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('customer.updated'),
                    life: 3000
                });
            } else {
                const response = await ClienteService.criar(clienteParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('customer.created'),
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchClientes(first / rows, rows);
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || t('customer.saveError'));
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: err.message || t('customer.saveError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirCliente = async (id) => {
        try {
            await ClienteService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('customer.deleted'),
                life: 3000
            });
            fetchClientes(first / rows, rows);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: err.message || t('customer.deleteError'),
                life: 3000
            });
        }
    };

    const abrirDialog = (cliente = null) => {
        if (cliente) {
            setNovoCliente({
                id: cliente.id,
                nome: cliente.nome || '',
                cpfCnpj: cliente.cpfCnpj || '',
                rgIe: cliente.rgIe || '',
                tipoPessoa: cliente.tipoPessoa || 'FISICA',
                telefone: cliente.telefone || '',
                email: cliente.email || '',
                endereco: cliente.endereco || '',
                limiteCredito: cliente.limiteCredito || 0
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoCliente({
            id: null,
            nome: '',
            cpfCnpj: '',
            rgIe: '',
            tipoPessoa: 'FISICA',
            telefone: '',
            email: '',
            endereco: '',
            limiteCredito: 0
        });
        setError('');
        setSuccess(false);
    };

    const tipoPessoaTemplate = (rowData) => {
        const option = tipoPessoaOptions.find(op => op.value === rowData.tipoPessoa);
        return option ? option.label : rowData.tipoPessoa;
    };

    const limiteCreditoTemplate = (rowData) => {
        return (
            <span className="valor-formatado">
                {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(rowData.limiteCredito || 0)}
            </span>
        );
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="cliente-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip={t('common.edit')}
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirCliente(rowData.id)}
                    tooltip={t('common.delete')}
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label={t('common.cancel')}
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
            />
            <Button
                label={t('customer.save')}
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarCliente}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="cliente-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('customer.title')} className="cliente-main-card">
                <div className="cliente-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="cliente-info">
                        <p className="text-muted m-0">{t('customer.description')}</p>
                    </div>
                    <Button
                        label={t('customer.new')}
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={clientes}
                    loading={loading}
                    paginator
                    first={first}
                    rows={rows}
                    totalRecords={totalRecords}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    onPageChange={onPageChange}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum cliente encontrado"
                >
                    <Column field="nome" header="Nome/Razao Social" sortable style={{ width: '250px' }} />
                    <Column field="cpfCnpj" header="CPF/CNPJ" sortable style={{ width: '150px' }} />
                    <Column body={tipoPessoaTemplate} header="Tipo" sortable style={{ width: '100px' }} />
                    <Column field="telefone" header="Telefone" sortable style={{ width: '130px' }} />
                    <Column field="email" header="Email" sortable style={{ width: '200px' }} />
                    <Column body={limiteCreditoTemplate} header="Limite Credito" sortable style={{ width: '150px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoCliente.id ? `Editar Cliente: ${novoCliente.nome}` : 'Novo Cliente'}
                visible={dialogVisible}
                style={{ width: 'min(94vw, 980px)' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                
                <div className="bc-form-stack">
                    {novoCliente.id && (
                        <section className="bc-form-section">
                            <div className="bc-form-section-title">
                                <i className="pi pi-image" aria-hidden="true" />
                                <div>
                                    <h3>Identidade visual</h3>
                                    <span>Logo vinculada ao cadastro de cliente.</span>
                                </div>
                            </div>
                            <div className="col-12">
                            <ImagemRegistro
                                registroId={novoCliente.id}
                                rotulo="Logo"
                                lerUrl={`/api/cadastro/clientes/logo/${novoCliente.id}`}
                                enviarUrl={`/api/cadastro/clientes/logo?id=${novoCliente.id}`}
                                removerUrl={`/api/cadastro/clientes/logo?id=${novoCliente.id}`}
                            />
                        </div>
                        </section>
                    )}

                    {(error || success) && (
                        <div>
                            {error && <Message severity="error" text={error} className="w-full mb-3" />}
                            {success && <Message severity="success" text="Cliente salvo com sucesso!" className="w-full mb-3" />}
                        </div>
                    )}

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-id-card" aria-hidden="true" />
                            <div>
                                <h3>Identificação</h3>
                                <span>Dados cadastrais principais.</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            <div className="col-12 md:col-8">
                                <label className="bc-label">Nome / Razão Social *</label>
                                <InputText
                                    value={novoCliente.nome}
                                    onChange={(e) => setNovoCliente({...novoCliente, nome: e.target.value})}
                                    placeholder="Nome ou razão social"
                                    required
                                />
                            </div>
                            <div className="col-12 md:col-4">
                                <label className="bc-label">Tipo de pessoa *</label>
                                <Dropdown
                                    value={novoCliente.tipoPessoa}
                                    options={tipoPessoaOptions}
                                    onChange={(e) => setNovoCliente({...novoCliente, tipoPessoa: e.value})}
                                    optionLabel="label"
                                    placeholder="Selecione"
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">CPF/CNPJ *</label>
                                <InputMask
                                    value={novoCliente.cpfCnpj}
                                    onChange={(e) => setNovoCliente({...novoCliente, cpfCnpj: e.value})}
                                    mask={novoCliente.tipoPessoa === 'FISICA' ? '999.999.999-99' : '99.999.999/9999-99'}
                                    placeholder={novoCliente.tipoPessoa === 'FISICA' ? '000.000.000-00' : '00.000.000/0000-00'}
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">RG / Inscrição Estadual</label>
                                <InputText
                                    value={novoCliente.rgIe}
                                    onChange={(e) => setNovoCliente({...novoCliente, rgIe: e.target.value})}
                                    placeholder="RG ou inscrição estadual"
                                />
                            </div>
                        </div>
                    </section>

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-address-book" aria-hidden="true" />
                            <div>
                                <h3>Contato</h3>
                                <span>Canais usados para atendimento e comunicação.</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            <div className="col-12 md:col-6">
                                <label className="bc-label">Telefone</label>
                                <InputMask
                                    value={novoCliente.telefone}
                                    onChange={(e) => setNovoCliente({...novoCliente, telefone: e.value})}
                                    mask="(99) 99999-9999"
                                    placeholder="(00) 00000-0000"
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">Email</label>
                                <InputText
                                    value={novoCliente.email}
                                    onChange={(e) => setNovoCliente({...novoCliente, email: e.target.value})}
                                    placeholder="nome@empresa.com.br"
                                    type="email"
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">Limite de crédito (R$)</label>
                                <InputNumber
                                    value={novoCliente.limiteCredito}
                                    onChange={(e) => setNovoCliente({...novoCliente, limiteCredito: e.value || 0})}
                                    mode="currency"
                                    currency="BRL"
                                    locale="pt-BR"
                                    placeholder="0,00"
                                />
                            </div>
                        </div>
                    </section>

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-map-marker" aria-hidden="true" />
                            <div>
                                <h3>Endereço</h3>
                                <span>Localização principal do cadastro.</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            <div className="col-12">
                                <label className="bc-label">Endereço</label>
                                <InputText
                                    value={novoCliente.endereco}
                                    onChange={(e) => setNovoCliente({...novoCliente, endereco: e.target.value})}
                                    placeholder="Rua, número, complemento, bairro, cidade/UF"
                                />
                            </div>
                        </div>
                    </section>

                    

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-eye" aria-hidden="true" />
                            <div>
                                <h3>Resumo</h3>
                                <span>Confira os dados principais antes de salvar.</span>
                            </div>
                        </div>
                        <div className="cliente-resumo flex justify-content-between align-items-center p-3 border-round flex-wrap gap-3">
                            <div><span className="font-bold">Nome: </span><span>{novoCliente.nome || '—'}</span></div>
                            <div><span className="font-bold">CPF/CNPJ: </span><span>{novoCliente.cpfCnpj || '—'}</span></div>
                            <div><span className="font-bold">Tipo: </span><span>{novoCliente.tipoPessoa || '—'}</span></div>
                        </div>
                    </section>
                </div>
            </Dialog>
        </div>
    );
};

export default Cliente;
