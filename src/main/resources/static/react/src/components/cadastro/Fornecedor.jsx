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
import { FornecedorService } from '../../services/FornecedorService';
import { desembrulharLista } from '../../services/ApiConfig';
import { useTranslation } from 'react-i18next';

export const Fornecedor = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const toast = useRef(null);
    const [fornecedores, setNovoFornecedores] = useState([]);
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

    const [novoFornecedor, setNovoFornecedor] = useState({
        id: null,
        nome: '',
        cpfCnpj: '',
        rgIe: '',
        tipoPessoa: 'JURIDICA',
        telefone: '',
        email: '',
        endereco: '',
        site: '',
        observacao: ''
    });

    useEffect(() => {
        fetchFornecedores(0, rows);
    }, []);

    const fetchFornecedores = async (page, size) => {
        setLoading(true);
        try {
            const { lista, total } = desembrulharLista(await FornecedorService.listar(page, size));
            setNovoFornecedores(lista);
            setTotalRecords(total);
        } catch (err) {
            console.error('Erro ao carregar fornecedores', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: t('supplier.loadError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const onPageChange = async (event) => {
        setFirst(event.first);
        setRows(event.rows);
        await fetchFornecedores(event.page, event.rows);
    };

    const salvarFornecedor = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const fornecedorParaSalvar = {
                ...novoFornecedor,
                empresaId: user?.empresaId
            };

            if (fornecedorParaSalvar.id) {
                const response = await FornecedorService.atualizar(fornecedorParaSalvar.id, fornecedorParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('supplier.updated'),
                    life: 3000
                });
            } else {
                const response = await FornecedorService.criar(fornecedorParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('supplier.created'),
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchFornecedores(first / rows, rows);
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || t('supplier.saveError'));
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: err.message || t('supplier.saveError'),
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirFornecedor = async (id) => {
        try {
            await FornecedorService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('supplier.deleted'),
                life: 3000
            });
            fetchFornecedores(first / rows, rows);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: t('common.error'),
                detail: err.message || t('supplier.deleteError'),
                life: 3000
            });
        }
    };

    const abrirDialog = (fornecedor = null) => {
        if (fornecedor) {
            setNovoFornecedor({
                id: fornecedor.id,
                nome: fornecedor.nome || '',
                cpfCnpj: fornecedor.cpfCnpj || '',
                rgIe: fornecedor.rgIe || '',
                tipoPessoa: fornecedor.tipoPessoa || 'JURIDICA',
                telefone: fornecedor.telefone || '',
                email: fornecedor.email || '',
                endereco: fornecedor.endereco || '',
                site: fornecedor.site || '',
                observacao: fornecedor.observacao || ''
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoFornecedor({
            id: null,
            nome: '',
            cpfCnpj: '',
            rgIe: '',
            tipoPessoa: 'JURIDICA',
            telefone: '',
            email: '',
            endereco: '',
            site: '',
            observacao: ''
        });
        setError('');
        setSuccess(false);
    };

    const tipoPessoaTemplate = (rowData) => {
        const option = tipoPessoaOptions.find(op => op.value === rowData.tipoPessoa);
        return option ? option.label : rowData.tipoPessoa;
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="fornecedor-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip={t('common.edit')}
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirFornecedor(rowData.id)}
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
                label={t('supplier.save')}
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarFornecedor}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="fornecedor-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('supplier.title')} className="fornecedor-main-card">
                <div className="fornecedor-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="fornecedor-info">
                        <p className="text-muted m-0">{t('supplier.description')}</p>
                    </div>
                    <Button
                        label={t('supplier.new')}
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={fornecedores}
                    loading={loading}
                    paginator
                    first={first}
                    rows={rows}
                    totalRecords={totalRecords}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    onPageChange={onPageChange}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage={t('supplier.empty')}
                >
                    <Column field="nome" header="Nome/Razao Social" sortable style={{ width: '250px' }} />
                    <Column field="cpfCnpj" header="CPF/CNPJ" sortable style={{ width: '150px' }} />
                    <Column body={tipoPessoaTemplate} header="Tipo" sortable style={{ width: '100px' }} />
                    <Column field="telefone" header="Telefone" sortable style={{ width: '130px' }} />
                    <Column field="email" header="Email" sortable style={{ width: '200px' }} />
                    <Column field="site" header="Site" sortable style={{ width: '200px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoFornecedor.id ? `Editar Fornecedor: ${novoFornecedor.nome}` : 'Novo Fornecedor'}
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
                    {novoFornecedor.id && (
                        <section className="bc-form-section">
                            <div className="bc-form-section-title">
                                <i className="pi pi-image" aria-hidden="true" />
                                <div>
                                    <h3>Identidade visual</h3>
                                    <span>Logo vinculada ao cadastro de fornecedor.</span>
                                </div>
                            </div>
                            <div className="col-12">
                            <ImagemRegistro
                                registroId={novoFornecedor.id}
                                rotulo="Logo"
                                lerUrl={`/api/cadastro/fornecedores/logo/${novoFornecedor.id}`}
                                enviarUrl={`/api/cadastro/fornecedores/logo?id=${novoFornecedor.id}`}
                                removerUrl={`/api/cadastro/fornecedores/logo?id=${novoFornecedor.id}`}
                            />
                        </div>
                        </section>
                    )}

                    {(error || success) && (
                        <div>
                            {error && <Message severity="error" text={error} className="w-full mb-3" />}
                            {success && <Message severity="success" text="Fornecedor salvo com sucesso!" className="w-full mb-3" />}
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
                                    value={novoFornecedor.nome}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, nome: e.target.value})}
                                    placeholder="Nome ou razão social"
                                    required
                                />
                            </div>
                            <div className="col-12 md:col-4">
                                <label className="bc-label">Tipo de pessoa *</label>
                                <Dropdown
                                    value={novoFornecedor.tipoPessoa}
                                    options={tipoPessoaOptions}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, tipoPessoa: e.value})}
                                    optionLabel="label"
                                    placeholder="Selecione"
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">CPF/CNPJ *</label>
                                <InputMask
                                    value={novoFornecedor.cpfCnpj}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, cpfCnpj: e.value})}
                                    mask={novoFornecedor.tipoPessoa === 'FISICA' ? '999.999.999-99' : '99.999.999/9999-99'}
                                    placeholder={novoFornecedor.tipoPessoa === 'FISICA' ? '000.000.000-00' : '00.000.000/0000-00'}
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">RG / Inscrição Estadual</label>
                                <InputText
                                    value={novoFornecedor.rgIe}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, rgIe: e.target.value})}
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
                                    value={novoFornecedor.telefone}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, telefone: e.value})}
                                    mask="(99) 99999-9999"
                                    placeholder="(00) 00000-0000"
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">Email</label>
                                <InputText
                                    value={novoFornecedor.email}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, email: e.target.value})}
                                    placeholder="nome@empresa.com.br"
                                    type="email"
                                />
                            </div>
                            <div className="col-12 md:col-6">
                                <label className="bc-label">Site</label>
                                <InputText
                                    value={novoFornecedor.site}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, site: e.target.value})}
                                    placeholder="www.empresa.com.br"
                                    type="url"
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
                                    value={novoFornecedor.endereco}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, endereco: e.target.value})}
                                    placeholder="Rua, número, complemento, bairro, cidade/UF"
                                />
                            </div>
                        </div>
                    </section>

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-file-edit" aria-hidden="true" />
                            <div>
                                <h3>Observações</h3>
                                <span>Informações adicionais para operação.</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            <div className="col-12">
                                <label className="bc-label">Observações</label>
                                <InputText
                                    value={novoFornecedor.observacao}
                                    onChange={(e) => setNovoFornecedor({...novoFornecedor, observacao: e.target.value})}
                                    placeholder="Observações adicionais"
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
                        <div className="fornecedor-resumo flex justify-content-between align-items-center p-3 border-round flex-wrap gap-3">
                            <div><span className="font-bold">Nome: </span><span>{novoFornecedor.nome || '—'}</span></div>
                            <div><span className="font-bold">CPF/CNPJ: </span><span>{novoFornecedor.cpfCnpj || '—'}</span></div>
                            <div><span className="font-bold">Tipo: </span><span>{novoFornecedor.tipoPessoa || '—'}</span></div>
                        </div>
                    </section>
                </div>
            </Dialog>
        </div>
    );
};

export default Fornecedor;
