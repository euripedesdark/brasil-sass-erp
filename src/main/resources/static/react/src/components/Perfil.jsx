import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { Password } from 'primereact/password';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useAuth } from '../contexts/AuthContext';
import { apiFetch } from '../services/ApiConfig';
import { ImagemRegistro } from './shared/ImagemRegistro';

const API = '/api/core/perfil';

export const Perfil = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);

    const [perfil, setPerfil] = useState({
        id: null, nome: '', username: '', email: '', empresaId: null, fotoUrl: null, perfis: []
    });
    const [senha, setSenha] = useState({ atual: '', nova: '', confirmacao: '' });
    const [carregando, setCarregando] = useState(false);
    const [trocandoSenha, setTrocandoSenha] = useState(false);
    const [erroDados, setErroDados] = useState('');
    const [erroSenha, setErroSenha] = useState('');

    useEffect(() => {
        carregar();
    }, []);

    const carregar = async () => {
        setCarregando(true);
        try {
            const response = await apiFetch(`${API}`);
            if (!response.ok) throw new Error(await extrairErro(response, t('legacyUi.perfil.loadError')));
            const json = await response.json();
            setPerfil(json?.data ?? json ?? {});
        } catch (err) {
            setErroDados(err.message);
        } finally {
            setCarregando(false);
        }
    };

    const salvarDados = async () => {
        setCarregando(true);
        setErroDados('');
        try {
            const response = await apiFetch(API, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nome: perfil.nome, email: perfil.email })
            });
            if (!response.ok) throw new Error(await extrairErro(response, t('legacyUi.perfil.saveError')));
            toast.current?.show({
                severity: 'success', summary: t('messages.success'),
                detail: t('legacyUi.perfil.saved'), life: 3000
            });
            carregar();
        } catch (err) {
            setErroDados(err.message);
        } finally {
            setCarregando(false);
        }
    };

    const salvarSenha = async () => {
        setErroSenha('');

        if (senha.nova !== senha.confirmacao) {
            setErroSenha(t('legacyUi.perfil.confirmMismatch'));
            return;
        }
        if (senha.nova && senha.nova.length < 8) {
            setErroSenha(t('legacyUi.perfil.passwordLength'));
            return;
        }

        setTrocandoSenha(true);
        try {
            const response = await apiFetch(`${API}/senha`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    senhaAtual: senha.atual,
                    novaSenha: senha.nova,
                    confirmarSenha: senha.confirmacao
                })
            });
            if (!response.ok) throw new Error(await extrairErro(response, t('legacyUi.perfil.passwordError')));

            setSenha({ atual: '', nova: '', confirmacao: '' });
            toast.current?.show({
                severity: 'success', summary: t('messages.success'),
                detail: t('legacyUi.perfil.passwordSaved'), life: 4000
            });
        } catch (err) {
            setErroSenha(err.message);
        } finally {
            setTrocandoSenha(false);
        }
    };

    return (
        <div className="perfil-container">
            <Toast ref={toast} />
            <Card title={t('legacyUi.perfil.profileTitle')}>
                {erroDados && <Message severity="error" text={erroDados} className="w-full mb-3" />}

                <div className="p-grid p-formgrid">
                    {/* Foto do usuário. Três rotas existiam sem tela: bytes no
                        MongoDB e só a referência no Postgres, como a imagem do
                        produto. */}
                    {perfil.id && (
                        <div className="p-field p-col-12">
                            <ImagemRegistro
                                registroId={perfil.id}
                                lerUrl={`/api/core/usuarios/foto/${perfil.id}`}
                                enviarUrl={`/api/core/usuarios/${perfil.id}/foto`}
                                removerUrl={`/api/core/usuarios/${perfil.id}/foto`}
                                campo="foto"
                                rotulo={t('legacyUi.perfil.photo')}
                            />
                        </div>
                    )}

                    <div className="p-field p-col-12">
                        <label htmlFor="nome">{t('legacyUi.perfil.fullName')}</label>
                        <InputText
                            id="nome"
                            value={perfil.nome || ''}
                            maxLength={150}
                            onChange={(e) => setPerfil({ ...perfil, nome: e.target.value })}
                            style={{ width: '100%' }}
                        />
                    </div>

                    <div className="p-field p-col-12 p-md-6">
                        <label htmlFor="username">{t('legacyUi.perfil.username')}</label>
                        <InputText id="username" value={perfil.username || ''} disabled style={{ width: '100%' }} />
                    </div>

                    <div className="p-field p-col-12 p-md-6">
                        <label htmlFor="email">Email</label>
                        <InputText
                            id="email"
                            value={perfil.email || ''}
                            maxLength={150}
                            onChange={(e) => setPerfil({ ...perfil, email: e.target.value })}
                            style={{ width: '100%' }}
                        />
                    </div>

                    <div className="p-field p-col-12 p-md-6">
                        <label>{t('legacyUi.perfil.roles')}</label>
                        <div className="flex gap-2 flex-wrap mt-2">
                            {(perfil.perfis || []).map((p) => (
                                <Tag key={p} value={p} severity={p === 'ADMIN' ? 'danger' : 'info'} />
                            ))}
                            {(!perfil.perfis || perfil.perfis.length === 0) && (
                                <span className="text-muted">{t('legacyUi.perfil.noRoles')}</span>
                            )}
                        </div>
                    </div>
                </div>

                <div className="p-grid p-justify-end">
                    <Button
                        label={t('common.saveChanges')}
                        icon="pi pi-save"
                        onClick={salvarDados}
                        loading={carregando}
                    />
                </div>

                <Divider align="left"><b>{t('legacyUi.perfil.changePasswordTitle')}</b></Divider>

                {erroSenha && <Message severity="error" text={erroSenha} className="w-full mb-3" />}

                <div className="p-grid p-formgrid">
                    <div className="p-field p-col-12">
                        <label htmlFor="senhaAtual">{t('legacyUi.perfil.currentPassword')}</label>
                        <Password
                            id="senhaAtual"
                            value={senha.atual}
                            onChange={(e) => setSenha({ ...senha, atual: e.target.value })}
                            style={{ width: '100%' }}
                            feedback={false}
                            toggleMask
                        />
                    </div>

                    <div className="p-field p-col-12">
                        <label htmlFor="novaSenha">{t('legacyUi.perfil.newPasswordHelp')}</label>
                        <Password
                            id="novaSenha"
                            value={senha.nova}
                            onChange={(e) => setSenha({ ...senha, nova: e.target.value })}
                            style={{ width: '100%' }}
                            feedback
                            toggleMask
                        />
                    </div>

                    <div className="p-field p-col-12">
                        <label htmlFor="confirmacaoSenha">{t('legacyUi.perfil.confirmNewPassword')}</label>
                        <Password
                            id="confirmacaoSenha"
                            value={senha.confirmacao}
                            onChange={(e) => setSenha({ ...senha, confirmacao: e.target.value })}
                            style={{ width: '100%' }}
                            feedback={false}
                            toggleMask
                        />
                    </div>
                </div>

                <div className="p-grid p-justify-end">
                    <Button
                        label={t('legacyUi.perfil.changePasswordButton')}
                        icon="pi pi-key"
                        onClick={salvarSenha}
                        loading={trocandoSenha}
                        disabled={!senha.atual || !senha.nova || !senha.confirmacao}
                    />
                </div>
            </Card>
        </div>
    );
};

const extrairErro = async (response, padrao) => {
    try {
        const body = await response.text();
        if (!body) return padrao;
        try {
            const json = JSON.parse(body);
            return json.message || json.error || padrao;
        } catch {
            return body.slice(0, 200);
        }
    } catch {
        return padrao;
    }
};

export default Perfil;
