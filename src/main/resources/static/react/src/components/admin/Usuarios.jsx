import React, { useState, useEffect } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Message } from 'primereact/message';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { SuperAdminService } from '../../services/SuperAdminService';
import { ModuloSelector } from './ModuloSelector';
import { useTranslation } from 'react-i18next';
import './ModuloSelector.css';

const FORMULARIO_VAZIO = {
    username: '',
    nome: '',
    email: '',
    senha: '',
    perfil: null,
    ativo: true,
};

export const Usuarios = () => {
    const { t } = useTranslation();
    const [usuarios, setUsuarios] = useState([]);
    const [perfis, setPerfis] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [moduloVisible, setModuloVisible] = useState(false);
    const [moduloUser, setModuloUser] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Cadastro e edicao. Antes these dois estados nao existiam: o botao "Novo
    // Usuario" nao tinha onClick e nao havia dialogo de cadastro, entao nao
    // havia como criar usuario pela tela.
    const [formVisible, setFormVisible] = useState(false);
    const [editando, setEditando] = useState(null);
    const [form, setForm] = useState(FORMULARIO_VAZIO);
    const [formErro, setFormErro] = useState('');

    useEffect(() => {
        fetchUsuarios();
        fetchPerfis();
    }, []);

    const fetchUsuarios = async () => {
        setLoading(true);
        try {
            setUsuarios(await SuperAdminService.listarUsuarios());
        } catch (err) {
            setError(err.message || t('legacyUi.users.loadError'));
        } finally {
            setLoading(false);
        }
    };

    /**
     * Os nomes de perfil vem do banco. A versao anterior pedia
     * /api/core/perfis e, se falhasse, caia numa lista fixa que inventava os
     * ids (1 = ADMIN, 2 = DIRETORIA...) — ids que nao batem com o banco, e o
     * perfil errado era aceito em silencio.
     */
    const fetchPerfis = async () => {
        try {
            setPerfis(await SuperAdminService.perfisDisponiveis());
        } catch (err) {
            setPerfis([]);
        }
    };

    const abrirNovo = () => {
        setEditando(null);
        setForm(FORMULARIO_VAZIO);
        setFormErro('');
        setFormVisible(true);
    };

    const abrirEdicao = (user) => {
        setEditando(user);
        setForm({
            username: user.username || '',
            nome: user.nome || '',
            email: user.email || '',
            senha: '',
            perfil: user.perfis?.[0]?.nome || null,
            ativo: user.ativo !== false,
        });
        setFormErro('');
        setFormVisible(true);
    };

    const mudarCampo = (campo) => (e) => {
        const valor = e.target ? e.target.value : e.value;
        setForm((f) => ({ ...f, [campo]: valor }));
    };

    /**
     * Grava o formulario. No cadastro a senha e' obrigatoria; na edicao o
     * campo some e a senha nao muda — trocar senha e' acao separada. Misturar as
     * duas faz o formulario exigir senha para quem so estava trocando o
     * e-mail, e a tela enche de erro sem motivo.
     */
    const salvarUsuario = async () => {
        setLoading(true);
        setFormErro('');

        if (!form.username.trim()) {
            setFormErro(t('legacyUi.users.usernameRequired'));
            setLoading(false);
            return;
        }
        if (!editando && form.senha.length < 6) {
            setFormErro(t('legacyUi.users.passwordMin') );
            setLoading(false);
            return;
        }

        try {
            if (editando) {
                await SuperAdminService.atualizarUsuario(editando.id, {
                    nome: form.nome,
                    email: form.email,
                    ativo: form.ativo,
                    perfil: form.perfil,
                });
            } else {
                await SuperAdminService.criarUsuario({
                    username: form.username.trim(),
                    nome: form.nome.trim() || form.username.trim(),
                    email: form.email.trim() || undefined,
                    senha: form.senha,
                    perfil: form.perfil || undefined,
                    ativo: form.ativo,
                });
            }
            setFormVisible(false);
            setSuccess(editando ? t('legacyUi.users.userSaved') : t('legacyUi.users.userCreated'));
            setTimeout(() => setSuccess(''), 3000);
            await fetchUsuarios();
        } catch (err) {
            setFormErro(err.message || t('legacyUi.users.saveError'));
        } finally {
            setLoading(false);
        }
    };

    const confirmarRemocao = (user) => {
        confirmDialog({
            message: t('legacyUi.users.deactivateConfirm', { name: user.nome || user.username }),
            header: t('legacyUi.users.deactivateTitle'),
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('legacyUi.users.confirmDeactivate'),
            rejectLabel: t('common.cancel'),
            accept: async () => {
                try {
                    await SuperAdminService.removerUsuario(user.id);
                    setSuccess(t('legacyUi.users.userDeactivated'));
                    setTimeout(() => setSuccess(''), 3000);
                    await fetchUsuarios();
                } catch (err) {
                    setError(err.message || t('legacyUi.users.deactivateError'));
                }
            },
        });
    };

    const openEditDialog = (user) => {
        setSelectedUser(user);
        setDialogVisible(true);
        setError('');
        setSuccess('');
    };

    const handleSavePermission = async (perfil) => {
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            await SuperAdminService.updatePermission({
                usuarioId: selectedUser.id,
                perfilId: perfil.id,
                adicionar: true,
                executorId: selectedUser.id,
            });
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchUsuarios();
            }, 1500);
        } catch (err) {
            setError(err.message || t('legacyUi.users.permissionError'));
        } finally {
            setLoading(false);
        }
    };

    /**
     * A coluna "Perfil Atual" mostrava `rowData.perfil`, campo que a entidade
     * Usuario nao tem — o perfil vive em `perfis`, uma lista. Vinha vazio em
     * toda linha da tabela.
     */
    const perfilTemplate = (rowData) => {
        const nomes = (rowData.perfis || []).map((p) => p.nome);
        if (nomes.length === 0) {
            return <span className="text-muted">{t('legacyUi.users.noRole')}</span>;
        }
        return nomes.join(', ');
    };

    const acoesTemplate = (rowData) => (
        <div className="flex gap-1">
            <Button
                icon="pi pi-pencil"
                className="p-button-text p-button-sm"
                tooltip={t('legacyUi.users.editUser')}
                onClick={() => abrirEdicao(rowData)}
            />
            <Button
                icon="pi pi-trash"
                className="p-button-text p-button-sm p-button-danger"
                tooltip={t('legacyUi.users.deactivateUser')}
                onClick={() => confirmarRemocao(rowData)}
            />
            <Button
                icon="pi pi-shield"
                className="p-button-text p-button-sm"
                tooltip={t('legacyUi.users.roles')}
                onClick={() => openEditDialog(rowData)}
            />
        </div>
    );

    const permissionTemplate = (rowData) => {
        return (
            <Button
                icon="pi pi-shield"
                className="p-button-text p-button-sm"
                tooltip={t('legacyUi.users.changeRoles')}
                onClick={() => openEditDialog(rowData)}
            />
        );
    };

    // Atalho para o seletor de modulos: e a parte que a pessoa usa no dia a dia.
    const modulosTemplate = (rowData) => (
        <Button
            icon="pi pi-th-large"
            className="p-button-text p-button-sm"
            tooltip={t('legacyUi.users.setModules')}
            onClick={() => { setModuloUser(rowData); setModuloVisible(true); }}
        />
    );

    return (
        <div className="usuarios-admin-container">
            <ConfirmDialog />
            <Card title={t('legacyUi.users.title')}>
                <div className="mb-3 flex justify-content-between align-items-center">
                    <p className="text-muted m-0">{t('legacyUi.users.subtitle')}</p>
                    <Button
                        icon="pi pi-plus"
                        label={t('legacyUi.users.newUser')}
                        className="p-button-success"
                        onClick={abrirNovo}
                    />
                </div>

                {error && <Message severity="error" text={error} className="w-full mb-3" />}
                {success && typeof success === 'string' && success && (
                    <Message severity="success" text={success} className="w-full mb-3" />
                )}

                <DataTable
                    value={usuarios}
                    dataKey="id"
                    paginator
                    rows={10}
                    rowsPerPageOptions={[5, 10, 25]}
                    loading={loading}
                    tableStyle={{ minWidth: '60rem' }}
                >
                    <Column field="id" header="ID" sortable style={{ width: '5%' }}></Column>
                    <Column field="nome" header={t('common.name')} sortable style={{ width: '18%' }}></Column>
                    <Column field="username" header={t('common.username')} sortable style={{ width: '14%' }}></Column>
                    <Column field="email" header={t('common.email')} sortable style={{ width: '18%' }}></Column>
                    <Column header={t('legacyUi.users.role')} body={perfilTemplate} style={{ width: '13%' }}></Column>
                    <Column header={t('common.active')} sortable field="ativo" style={{ width: '7%' }}
                        body={(r) => (r.ativo === false
                            ? <span className="text-muted">{t('common.no')}</span>
                            : <span>{t('common.yes')}</span>)} />
                    <Column header={t('legacyUi.users.modules')} body={modulosTemplate} style={{ width: '8%' }}></Column>
                    <Column header={t('common.actions')} body={acoesTemplate} style={{ width: '17%' }}></Column>
                </DataTable>

                <ModuloSelector
                    visible={moduloVisible}
                    usuario={moduloUser}
                    onHide={() => setModuloVisible(false)}
                    onSalvo={fetchUsuarios}
                />

                <Dialog
                    header={editando ? t('legacyUi.users.editUser') : t('legacyUi.users.newUser')}
                    visible={formVisible}
                    style={{ width: '440px' }}
                    onHide={() => setFormVisible(false)}
                >
                    <div className="p-fluid">
                        {formErro && (
                            <Message severity="error" text={formErro} className="w-full mb-3" />
                        )}

                        <div className="field mb-3">
                            <label htmlFor="username">{t('common.username')}</label>
                            <InputText
                                id="username"
                                value={form.username}
                                onChange={mudarCampo('username')}
                                disabled={!!editando}
                                autoComplete="off"
                            />
                        </div>

                        <div className="field mb-3">
                            <label htmlFor="nome">{t('common.name')}</label>
                            <InputText
                                id="nome"
                                value={form.nome}
                                onChange={mudarCampo('nome')}
                            />
                        </div>

                        <div className="field mb-3">
                            <label htmlFor="email">{t('common.email')}</label>
                            <InputText
                                id="email"
                                value={form.email}
                                onChange={mudarCampo('email')}
                            />
                        </div>

                        {!editando && (
                            <div className="field mb-3">
                                <label htmlFor="senha">{t('common.password')}</label>
                                <Password
                                    id="senha"
                                    value={form.senha}
                                    onChange={mudarCampo('senha')}
                                    feedback={false}
                                    toggleMask={true}
                                    autoComplete="new-password"
                                />
                                <small className="text-muted">{t('legacyUi.users.passwordHint')}</small>
                            </div>
                        )}

                        <div className="field mb-3">
                            <label htmlFor="perfil">{t('legacyUi.users.role')}</label>
                            <Dropdown
                                id="perfil"
                                value={form.perfil}
                                options={perfis}
                                onChange={mudarCampo('perfil')}
                                placeholder={t('legacyUi.users.selectRole')}
                                disabled={loading}
                            />
                        </div>

                        <div className="field mb-4 flex align-items-center gap-2">
                            <input
                                type="checkbox"
                                id="ativo"
                                checked={form.ativo}
                                onChange={(e) => setForm((f) => ({ ...f, ativo: e.target.checked }))}
                            />
                            <label htmlFor="ativo">{t('legacyUi.users.activeUser')}</label>
                        </div>

                        <div className="flex justify-end gap-2">
                            <Button
                                label={t('common.cancel')}
                                className="p-button-text"
                                onClick={() => setFormVisible(false)}
                                disabled={loading}
                            />
                            <Button
                                label={editando ? t('common.save') : t('legacyUi.users.createUser')}
                                icon="pi pi-check"
                                onClick={salvarUsuario}
                                loading={loading}
                            />
                        </div>
                    </div>
                </Dialog>

                <Dialog
                    header={t('legacyUi.users.changeAccessLevel')}
                    visible={dialogVisible}
                    style={{ width: '400px' }}
                    onHide={() => setDialogVisible(false)}
                >
                    <div className="p-fluid">
                        <div className="field mb-4">
                            <label className="font-bold block mb-2">{t('common.user')}: {selectedUser?.nome}</label>
                            <p className="text-sm text-muted">{t('legacyUi.users.roleHelp')}</p>
                        </div>

                        {error && <Message severity="error" text={error} className="w-full mb-3" />}
                        {success === true && <Message severity="success" text={t('legacyUi.users.permissionUpdated')} className="w-full mb-3" />}

                        <div className="field">
                            <label htmlFor="perfil">{t('legacyUi.users.newRole')}</label>
                            <Dropdown
                                id="perfil"
                                value={perfis.find((p) => p === selectedUser?.perfis?.[0]?.nome) || null}
                                options={perfis.map((nome) => ({ id: nome, nome }))}
                                onChange={(e) => handleSavePermission({ id: e.value })}
                                optionLabel="nome"
                                optionValue="id"
                                placeholder="Selecione o perfil"
                                disabled={loading}
                            />
                        </div>
                    </div>
                </Dialog>
            </Card>
        </div>
    );
};
