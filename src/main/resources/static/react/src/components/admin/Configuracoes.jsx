import React, { useCallback, useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { TabView, TabPanel } from 'primereact/tabview';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { FileUpload } from 'primereact/fileupload';
import { Messages } from 'primereact/messages';
import { useTranslation } from 'react-i18next';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
export const Configuracoes = () => {
    const { t } = useTranslation();
    const [activeTab, setActiveTab] = useState(0);
    const [configuracoes, setConfiguracoes] = useState({
        nomeSistema: 'Brasil SaaS ERP',
        smtpHost: '',
        smtpPort: '',
        smtpUser: ''
    });

    const messages = React.useRef(null);

    const handleSave = () => {
        messages.current.show({severity:'success', summary: t('common.success'), detail: t('legacyUi.settings.saved')});
    };

    const handleImageUpload = async (event, tipo) => {
        const file = event.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        const endpoint = tipo === 'login' ? '/api/superadmin/assets/login' : '/api/superadmin/assets/background';

        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}${endpoint}`, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                messages.current.show({severity:'success', summary: t('common.success'), detail: t('legacyUi.settings.imageUpdated', { type: tipo === 'login' ? t('legacyUi.settings.loginImage') : t('legacyUi.settings.backgroundImage') })});
                carregarPrevia();
            } else {
                const err = await response.text();
                messages.current.show({severity:'error', summary: t('common.error'), detail: t('legacyUi.settings.uploadFailed', { error: err })});
            }
        } catch (error) {
            messages.current.show({severity:'error', summary: t('common.error'), detail: t('legacyUi.settings.connectionError', { error: error.message })});
        }
    };

    // Prévia da imagem que está no Mongo. A tela só tinha o envio: sem o GET
    // assets/system/{tipo} não havia como conferir se a troca pegou, e o admin
    // só descobria o resultado ao deslogar e olhar a tela de login.
    const [previa, setPrevia] = useState({});

    const carregarPrevia = useCallback(async () => {
        const proxima = {};
        for (const tipo of ['SISTEMA_LOGIN', 'SISTEMA_BACKGROUND']) {
            try {
                const r = await apiFetch(`/api/superadmin/assets/system/${tipo}`);
                if (r.status === 204 || !r.ok) continue;
                const blob = await r.blob();
                if (blob.size) {
                    proxima[tipo] = URL.createObjectURL(blob);
                }
            } catch { /* sem imagem cadastrada para este tipo */ }
        }
        setPrevia((atual) => {
            Object.values(atual).forEach((u) => URL.revokeObjectURL(u));
            return proxima;
        });
    }, []);

    useEffect(() => { carregarPrevia(); }, [carregarPrevia]);

    // revoga ao sair, senão cada troca de tela deixa o blob vivo na memória
    useEffect(() => () => {
        Object.values(previa).forEach((u) => URL.revokeObjectURL(u));
    }, [previa]);

    return (
        <div className="configuracoes-card">
            <Messages ref={messages} />
            <Card title={t('legacyUi.settings.title')}>
                <TabView activeIndex={activeTab} onTabChange={(e) => setActiveTab(e.index)}>
                    <TabPanel header={t('legacyUi.settings.generalTab)}>
                        <div className="p-field p-grid">
                            <label className="p-col-12 p-md-2">{t('legacyUi.settings.systemName')}</label>
                            <div className="p-col-12 p-md-10">
                                <InputText
                                    value={configuracoes.nomeSistema}
                                    onChange={(e) => setConfiguracoes({...configuracoes, nomeSistema: e.target.value})}
                                    style={{ width: '100%' }}
                                />
                            </div>
                        </div>
                    </TabPanel>

                    <TabPanel header={t('legacyUi.settings.imagesTab)}>
                        <div className="p-field p-grid">
                            <label className="p-col-12 p-md-2">{t('legacyUi.settings.loginImage')}</label>
                            <div className="p-col-12 p-md-10">
                                <FileUpload
                                    mode="basic"
                                    name="imagemLogin"
                                    accept="image/*"
                                    maxFileSize={10000000}
                                    onSelect={(e) => handleImageUpload(e, 'login')}
                                    label={t('legacyUi.settings.selectImage')}
                                    auto
                                />
                                {previa.SISTEMA_LOGIN && (
                                    <img
                                        src={previa.SISTEMA_LOGIN}
                                        alt={t('legacyUi.settings.currentLoginImage')}
                                        style={{ maxHeight: 120, marginTop: 8, borderRadius: 4 }}
                                    />
                                )}
                            </div>
                        </div>

                        <div className="p-field p-grid">
                            <label className="p-col-12 p-md-2">{t('legacyUi.settings.backgroundImage')}</label>
                            <div className="p-col-12 p-md-10">
                                <FileUpload
                                    mode="basic"
                                    name="imagemBackground"
                                    accept="image/*"
                                    maxFileSize={10000000}
                                    onSelect={(e) => handleImageUpload(e, 'background')}
                                    label="Selecionar Imagem"
                                    auto
                                />
                                {previa.SISTEMA_BACKGROUND && (
                                    <img
                                        src={previa.SISTEMA_BACKGROUND}
                                        alt={t('legacyUi.settings.currentBackgroundImage')}
                                        style={{ maxHeight: 120, marginTop: 8, borderRadius: 4 }}
                                    />
                                )}
                            </div>
                        </div>
                    </TabPanel>
                </TabView>

                <div className="p-grid p-justify-end p-pt-3">
                    <Button label={t('legacyUi.settings.saveButton')} icon="pi pi-save" onClick={handleSave} />
                </div>
            </Card>
        </div>
    );
};
