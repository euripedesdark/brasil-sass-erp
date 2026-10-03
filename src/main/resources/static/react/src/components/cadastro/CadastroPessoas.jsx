import React, { useState, useEffect, useRef } from 'react';
import { apiFetch, desembrulharLista } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputMask } from 'primereact/inputmask';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';
import { useTranslation } from 'react-i18next';
import CepService from '../../services/CepService';

const BASE = '/api/cadastro/pessoas';

const TIPO_OPCOES = [
    { label: 'Pessoa Física', value: 'FISICA' },
    { label: 'Pessoa Jurídica', value: 'JURIDICA' }
];

const STATUS_OPCOES = [
    { label: 'Ativo', value: 'ATIVO' },
    { label: 'Inativo', value: 'INATIVO' }
];

const vazio = () => ({
    tipo: 'FISICA', nome: '', documento: '', email: '', telefone: '',
    status: 'ATIVO', observacao: '',
    fisica: { cpf: '', rg: '', orgaoExpedidor: '', dataNascimento: '', sexo: '', estadoCivil: '' },
    juridica: { cnpj: '', inscricaoEstadual: '', inscricaoMunicipal: '', dataAbertura: '', porte: '', naturezaJuridica: '' },
    enderecos: []
});

const erroDe = (e, padrao) => {
    const d = e?.payload;
    if (d?.errors?.[0]?.message) return d.errors[0].message;
    if (d?.message) return d.message;
    return padrao;
};

/**
 * Pessoas — o cadastro base.
 *
 * A tela listava pessoas, mas o diálogo criava um USUÁRIO
 * (/api/core/usuarios): o nome não batia com o que ela gravava, e não havia
 * como editar, excluir nem abrir uma pessoa. Como cliente, fornecedor e
 * transportadora apontam para pessoa, uma pessoa errada aqui contamina os
 * três.
 *
 * O documento e o campo que muda conforme o tipo: CPF (11 dígitos) para
 * física, CNPJ (14) para jurídica. A máscara acompanha, e o que é gravado é
 * sempre o documento, o mesmo campo no backend.
 */
export const CadastroPessoas = () => {
    const { user } = useAuth();
    const { t } = useTranslation();
    const toast = useRef(null);

    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [salvando, setSalvando] = useState(false);
    const [dialog, setDialog] = useState(false);
    const [editando, setEditando] = useState(null);
    const [detalhe, setDetalhe] = useState(null);
    const [form, setForm] = useState(vazio());
    const [erro, setErro] = useState('');
    const [busca, setBusca] = useState('');
    const [consultandoCep, setConsultandoCep] = useState(false);
    const [consultandoMunicipio, setConsultandoMunicipio] = useState(false);

    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch(`${BASE}?empresaId=${user?.empresaId ?? ''}&page=0&size=200`);
            setRows(r.ok ? desembrulharLista(await r.json()).lista : []);
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: t('common.error'),
                detail: erroDe(e, t('cadastro.people.loadError')), life: 5000
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, [user?.empresaId]);

    const abrir = (pessoa) => {
        setErro('');
        if (pessoa) {
            setEditando(pessoa);
            setForm({
                ...vazio(),
                ...pessoa,
                documento: pessoa.documento ?? '',
                fisica: { ...vazio().fisica, ...(pessoa.fisica ?? {}) },
                juridica: { ...vazio().juridica, ...(pessoa.juridica ?? {}) },
                enderecos: pessoa.enderecos ?? []
            });
        } else {
            setEditando(null);
            setForm(vazio());
        }
        setDialog(true);
    };

    const endereco = form.enderecos?.[0] ?? {
        tipo: 'PRINCIPAL', logradouro: '', numero: '', complemento: '',
        bairro: '', cep: '', municipioId: null, uf: '', principal: true
    };

    const atualizarEndereco = (campoEndereco, valor) => {
        const atual = { ...endereco, [campoEndereco]: valor };
        setForm({ ...form, enderecos: [atual] });
    };

    const consultarCep = async () => {
        const cep = endereco.cep || '';
        setConsultandoCep(true);
        try {
            const data = await CepService.consultar(cep);
            let municipioId = endereco.municipioId ?? null;

            // Consulta externa preenche os dados postais. O município continua
            // sendo resolvido contra o catálogo interno do ERP, para gravar a
            // FK local e não depender de texto vindo da internet.
            if (data.localidade) {
                setConsultandoMunicipio(true);
                try {
                    const response = await apiFetch('/api/municipios/buscar?nome=' +
                        encodeURIComponent(data.localidade) + '&page=0&size=20');
                    if (response.ok) {
                        const page = await response.json();
                        const municipios = Array.isArray(page?.content) ? page.content : [];
                        const encontrado = municipios.find((m) =>
                            String(m.uf || '').toUpperCase() === String(data.uf || '').toUpperCase()
                        ) || municipios[0];
                        municipioId = encontrado?.id ?? null;
                    }
                } finally {
                    setConsultandoMunicipio(false);
                }
            }

            setForm({
                ...form,
                enderecos: [{
                    ...endereco,
                    logradouro: data.logradouro || endereco.logradouro,
                    bairro: data.bairro || endereco.bairro,
                    uf: data.uf || endereco.uf,
                    municipioId
                }]
            });
            toast.current?.show({
                severity: 'success',
                summary: 'CEP consultado',
                detail: 'Endereço preenchido pelo serviço externo; município resolvido no cadastro interno.',
                life: 4000
            });
        } catch (e) {
            toast.current?.show({
                severity: 'warn',
                summary: 'Consulta de CEP',
                detail: e.message || 'Não foi possível consultar o CEP.',
                life: 5000
            });
        } finally {
            setConsultandoCep(false);
        }
    };

    const salvar = async () => {
        if (!form.nome?.trim()) {
            setErro(t('cadastro.people.nameRequired'));
            return;
        }
        setErro('');
        setSalvando(true);
        try {
            // só manda o bloco que pertence ao tipo escolhido: mandar os dois
            // confuse a validação do backend, que valida um ou outro
            const corpo = {
                ...form,
                documento: (form.documento || '').replace(/\D/g, ''),
                enderecos: (form.enderecos || []).map((e) => ({
                    ...e,
                    cep: (e.cep || '').replace(/\D/g, ''),
                    uf: (e.uf || '').toUpperCase()
                }))
            };
            if (corpo.tipo === 'FISICA') delete corpo.juridica; else delete corpo.fisica;
            if (!corpo.documento) delete corpo.documento;

            const url = editando ? `${BASE}/${editando.id}` : BASE;
            const r = await apiFetch(url, {
                method: editando ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpo)
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || t('cadastro.people.saveError'));
            }
            toast.current?.show({
                severity: 'success', summary: editando ? t('common.updated') : t('common.created'),
                detail: `${form.nome} ${editando ? t('common.updatedFemale') : t('common.createdFemale')}`, life: 3000
            });
            setDialog(false);
            carregar();
        } catch (e) {
            setErro(e.message || t('cadastro.people.saveError'));
        } finally {
            setSalvando(false);
        }
    };

    const abrirDetalhe = async (pessoa) => {
        try {
            const r = await apiFetch(`${BASE}/${pessoa.id}`);
            if (r.ok) {
                const j = await r.json();
                setDetalhe(j?.data ?? j);
            } else {
                toast.current?.show({
                    severity: 'error', summary: t('common.error'),
                    detail: t('cadastro.people.openError'), life: 4000
                });
            }
        } catch {
            toast.current?.show({
                severity: 'error', summary: t('common.error'),
                detail: t('common.communicationError'), life: 4000
            });
        }
    };

    const excluir = (pessoa) => {
        confirmDialog.require({
            header: t('cadastro.people.deleteTitle'),
            message: t('cadastro.people.deleteConfirm', { name: pessoa.nome }),
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('cadastro.people.confirmDelete'),
            rejectLabel: t('common.cancel'),
            accept: async () => {
                try {
                    const r = await apiFetch(`${BASE}/${pessoa.id}`, { method: 'DELETE' });
                    if (r.status === 409) {
                        throw new Error(t('cadastro.people.linkedDeleteError'));
                    }
                    if (!r.ok) throw new Error(t('cadastro.people.deleteError'));
                    toast.current?.show({
                        severity: 'success', summary: t('common.deletedFemale'),
                        detail: pessoa.nome, life: 3000
                    });
                    carregar();
                } catch (e) {
                    toast.current?.show({
                        severity: 'error', summary: t('common.error'),
                        detail: erroDe(e, e.message || t('cadastro.people.deleteError')), life: 6000
                    });
                }
            }
        });
    };

    const filtradas = busca.trim()
        ? rows.filter((p) => `${p.nome ?? ''} ${p.documento ?? ''} ${p.email ?? ''}`
            .toLowerCase().includes(busca.trim().toLowerCase()))
        : rows;

    const campo = (id, rotulo, children, largura) => (
        <div className={largura || 'col-12 md:col-6'}>
            <label className="bc-label" htmlFor={id}>{rotulo}</label>
            {children}
        </div>
    );

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <ConfirmDialog />

            <div className="flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">{t('cadastro.people.title')}</h2>
                    <span className="bc-muted">{t('cadastro.people.description')}</span>
                </div>
                <div className="flex gap-2">
                    <InputText placeholder={t('common.searchPlaceholder')} value={busca}
                               onChange={(e) => setBusca(e.target.value)} />
                    <Button label={t('common.newFemale')} icon="pi pi-plus" onClick={() => abrir(null)} />
                </div>
            </div>

            <DataTable
                value={filtradas}
                loading={loading}
                dataKey="id"
                paginator rows={10}
                emptyMessage={t('cadastro.people.empty')}
                responsiveLayout="scroll"
            >
                <Column field="nome" header={t('cadastro.people.name')} sortable />
                <Column header={t('common.document')} body={(p) => p.documento || '—'} style={{ width: '14rem' }} />
                <Column header={t('common.type')} body={(p) => (
                    <Tag value={p.tipo === 'JURIDICA' ? t('cadastro.people.legal') : t('cadastro.people.individual')}
                         severity={p.tipo === 'JURIDICA' ? 'info' : 'success'} />
                )} style={{ width: '9rem' }} />
                <Column field="email" header={t('common.email')} />
                <Column field="telefone" header={t('common.phone')} style={{ width: '12rem' }} />
                <Column header={t('common.status')} body={(p) => (
                    <Tag value={p.status ?? 'ATIVO'} severity={p.status === 'INATIVO' ? 'secondary' : 'success'} />
                )} style={{ width: '9rem' }} />
                <Column header="" body={(p) => (
                    <div className="flex gap-1">
                        <Button icon="pi pi-search" rounded text tooltip={t('common.view')} 
                                onClick={() => abrirDetalhe(p)} />
                        <Button icon="pi pi-pencil" rounded text tooltip={t('common.edit')}
                                onClick={() => abrir(p)} />
                        <Button icon="pi pi-trash" rounded text severity="danger" tooltip={t('common.delete')}
                                onClick={() => excluir(p)} />
                    </div>
                )} style={{ width: '8rem' }} />
            </DataTable>

            {/* edicao / criacao */}
            <Dialog
                visible={dialog}
                onHide={() => setDialog(false)}
                header={editando ? `${t('common.edit')}: ${editando.nome}` : t('cadastro.people.new')}
                modal
                maximizable
                style={{ width: 'min(94vw, 980px)' }}
                breakpoints={{ '960px': '94vw', '640px': '98vw' }}
                contentClassName="bc-dialog-form-content"
                footer={
                    <div className="bc-form-actions">
                        <Button label={t('common.cancel')} severity="secondary" text
                                onClick={() => setDialog(false)} />
                        <Button label={editando ? t('common.saveChanges') : t('cadastro.people.register')} icon="pi pi-check"
                                loading={salvando} disabled={salvando} onClick={salvar} />
                    </div>
                }
            >
                <div className="bc-form-stack">
                    {erro && <Message severity="error" text={erro} />}

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-id-card" aria-hidden="true" />
                            <div>
                                <h3>{t('cadastro.people.identification')}</h3>
                                <span>{t('cadastro.people.identificationHelp')}</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            {campo('tipo', `${t('common.type')} *`,
                                <Dropdown id="tipo" value={form.tipo} options={TIPO_OPCOES}
                                          onChange={(e) => setForm({ ...form, tipo: e.value, documento: '' })}
                                          aria-label={t('cadastro.people.typeAria')} />)}
                            {campo('nome', `${t('cadastro.people.name')} *`,
                                <InputText id="nome" value={form.nome}
                                           onChange={(e) => setForm({ ...form, nome: e.target.value })}
                                           autoFocus />)}
                            {form.tipo === 'FISICA'
                                ? campo('doc', 'CPF',
                                    <InputMask id="doc" mask="999.999.999-99" value={form.documento || ''}
                                               onChange={(e) => setForm({ ...form, documento: e.target.value })}
                                               placeholder="000.000.000-00" />)
                                : campo('doc', 'CNPJ',
                                    <InputMask id="doc" mask="99.999.999/9999-99" value={form.documento || ''}
                                               onChange={(e) => setForm({ ...form, documento: e.target.value })}
                                               placeholder="00.000.000/0000-00" />)}
                            {campo('status', t('common.status'),
                                <Dropdown id="status" value={form.status || 'ATIVO'} options={STATUS_OPCOES}
                                          onChange={(e) => setForm({ ...form, status: e.value })} />)}
                        </div>
                    </section>

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-address-book" aria-hidden="true" />
                            <div>
                                <h3>{t('cadastro.people.contact')}</h3>
                                <span>{t('cadastro.people.contactHelp')}</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            {campo('email', t('common.email'),
                                <InputText id="email" type="email" value={form.email || ''}
                                           onChange={(e) => setForm({ ...form, email: e.target.value })}
                                           placeholder="nome@empresa.com.br" />)}
                            {campo('telefone', t('common.phone'),
                                <InputMask id="telefone" mask="(99) 99999-9999" value={form.telefone || ''}
                                           onChange={(e) => setForm({ ...form, telefone: e.target.value })}
                                           placeholder="(00) 00000-0000" />)}
                        </div>
                    </section>

                    {form.tipo === 'JURIDICA' && (
                        <section className="bc-form-section">
                            <div className="bc-form-section-title">
                                <i className="pi pi-building" aria-hidden="true" />
                                <div>
                                    <h3>{t('cadastro.people.legalData')}</h3>
                                    <span>{t('cadastro.people.legalDataHelp')}</span>
                                </div>
                            </div>
                            <div className="bc-form-grid">
                                {campo('ie', t('cadastro.people.stateRegistration'),
                                    <InputText id="ie" value={form.juridica?.inscricaoEstadual || ''}
                                               onChange={(e) => setForm({ ...form, juridica: { ...form.juridica, inscricaoEstadual: e.target.value } })} />)}
                                {campo('im', t('cadastro.people.cityRegistration'),
                                    <InputText id="im" value={form.juridica?.inscricaoMunicipal || ''}
                                               onChange={(e) => setForm({ ...form, juridica: { ...form.juridica, inscricaoMunicipal: e.target.value } })} />)}
                            </div>
                        </section>
                    )}

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-map-marker" aria-hidden="true" />
                            <div>
                                <h3>Endereço principal</h3>
                                <span>CEP é consultado externamente; município é selecionado do catálogo interno do ERP.</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            {campo('cep', 'CEP',
                                <div className="p-inputgroup">
                                    <InputMask id="cep" mask="99999-999" value={endereco.cep || ''}
                                        onChange={(e) => atualizarEndereco('cep', e.value || '')}
                                        placeholder="00000-000" />
                                    <Button type="button" icon="pi pi-search" outlined
                                        loading={consultandoCep || consultandoMunicipio}
                                        onClick={consultarCep} tooltip="Consultar CEP externamente" />
                                </div>)}
                            {campo('logradouro', 'Logradouro',
                                <InputText id="logradouro" value={endereco.logradouro || ''}
                                    onChange={(e) => atualizarEndereco('logradouro', e.target.value)} />)}
                            {campo('numero', 'Número',
                                <InputText id="numero" value={endereco.numero || ''}
                                    onChange={(e) => atualizarEndereco('numero', e.target.value)} />)}
                            {campo('complemento', 'Complemento',
                                <InputText id="complemento" value={endereco.complemento || ''}
                                    onChange={(e) => atualizarEndereco('complemento', e.target.value)} />)}
                            {campo('bairro', 'Bairro',
                                <InputText id="bairro" value={endereco.bairro || ''}
                                    onChange={(e) => atualizarEndereco('bairro', e.target.value)} />)}
                            {campo('uf', 'UF',
                                <InputText id="uf" value={endereco.uf || ''} maxLength={2}
                                    onChange={(e) => atualizarEndereco('uf', e.target.value.toUpperCase())} />)}
                            {campo('municipioId', 'Município (cadastro interno)',
                                <InputText id="municipioId"
                                    value={endereco.municipioId ? String(endereco.municipioId) : ''}
                                    readOnly
                                    placeholder="Preenchido após consulta do CEP" />)}
                            <div className="col-12">
                                <small className="bc-muted">
                                    Fonte externa: ViaCEP. Fonte interna: /api/municipios. Se a consulta externa falhar, os campos continuam editáveis manualmente.
                                </small>
                            </div>
                        </div>
                    </section>

                    <section className="bc-form-section">
                        <div className="bc-form-section-title">
                            <i className="pi pi-file-edit" aria-hidden="true" />
                            <div>
                                <h3>{t('common.notes')}</h3>
                                <span>{t('cadastro.people.notesHelp')}</span>
                            </div>
                        </div>
                        <div className="bc-form-grid">
                            {campo('obs', t('common.note'),
                                <InputText id="obs" value={form.observacao || ''}
                                           onChange={(e) => setForm({ ...form, observacao: e.target.value })}
                                           placeholder="Opcional" />,
                                'col-12')}
                        </div>
                    </section>
                </div>
            </Dialog>

            {/* detalhe */}
            <Dialog
                visible={!!detalhe}
                onHide={() => setDetalhe(null)}
                header={detalhe ? detalhe.nome : ''}
                modal
                style={{ width: 'min(96vw, 560px)' }}
                breakpoints={{ '960px': '95vw' }}
            >
                {detalhe && (
                    <div className="grid p-fluid">
                        {campo('d1', 'Tipo', <span>{detalhe.tipo === 'JURIDICA' ? 'Pessoa Jurídica' : 'Pessoa Física'}</span>)}
                        {campo('d2', 'Documento', <span>{detalhe.documento || '—'}</span>)}
                        {campo('d3', 'Email', <span>{detalhe.email || '—'}</span>)}
                        {campo('d4', 'Telefone', <span>{detalhe.telefone || '—'}</span>)}
                        {campo('d5', 'Situação', <span>{detalhe.status || '—'}</span>)}
                        {campo('d6', 'ID interno', <span>{detalhe.id}</span>)}
                        {detalhe.observacao && campo('d7', 'Observação', <span>{detalhe.observacao}</span>, 'col-12')}
                    </div>
                )}
            </Dialog>
        </div>
    );
};

export default CadastroPessoas;
