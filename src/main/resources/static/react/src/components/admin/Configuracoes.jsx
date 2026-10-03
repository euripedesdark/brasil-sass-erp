import React, { useCallback, useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { TabView, TabPanel } from 'primereact/tabview';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { FileUpload } from 'primereact/fileupload';
import { Messages } from 'primereact/messages';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
export const Configuracoes = () => {
    const [activeTab, setActiveTab] = useState(0);
    const [configuracoes, setConfiguracoes] = useState({
        nomeSistema: 'Brasil SaaS ERP',
        smtpHost: '',
        smtpPort: '',
        smtpUser: ''
    });

    const messages = React.useRef(null);

    const handleSave = () => {
        messages.current.show({severity:'success', summary: 'Sucesso', detail: 'Configurações salvas com sucesso!'});
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
                messages.current.show({severity:'success', summary: 'Sucesso', detail: `Imagem de ${tipo} atualizada com sucesso!`});
                carregarPrevia();
            } else {
                const err = await response.text();
                messages.current.show({severity:'error', summary: 'Erro', detail: `Falha ao upload: ${err}`});
            }
        } catch (error) {
            messages.current.show({severity:'error', summary: 'Erro', detail: `Erro de conexão: ${error.message}`});
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
            <Card title="Configurações do Sistema">
                <TabView activeIndex={activeTab} onTabChange={(e) => setActiveTab(e.index)}>
                    <TabPanel header="Gerais">
                        <div className="p-field p-grid">
                            <label className="p-col-12 p-md-2">Nome do Sistema</label>
                            <div className="p-col-12 p-md-10">
                                <InputText
                                    value={configuracoes.nomeSistema}
                                    onChange={(e) => setConfiguracoes({...configuracoes, nomeSistema: e.target.value})}
                                    style={{ width: '100%' }}
                                />
                            </div>
                        </div>
                    </TabPanel>

                    <TabPanel header="Imagens">
                        <div className="p-field p-grid">
                            <label className="p-col-12 p-md-2">Imagem de Login</label>
                            <div className="p-col-12 p-md-10">
                                <FileUpload
                                    mode="basic"
                                    name="imagemLogin"
                                    accept="image/*"
                                    maxFileSize={10000000}
                                    onSelect={(e) => handleImageUpload(e, 'login')}
                                    label="Selecionar Imagem"
                                    auto
                                />
                                {previa.SISTEMA_LOGIN && (
                                    <img
                                        src={previa.SISTEMA_LOGIN}
                                        alt="Imagem de login atual"
                                        style={{ maxHeight: 120, marginTop: 8, borderRadius: 4 }}
                                    />
                                )}
                            </div>
                        </div>

                        <div className="p-field p-grid">
                            <label className="p-col-12 p-md-2">Imagem de Background</label>
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
                                        alt="Background atual"
                                        style={{ maxHeight: 120, marginTop: 8, borderRadius: 4 }}
                                    />
                                )}
                            </div>
                        </div>
                    </TabPanel>
                </TabView>

                <div className="p-grid p-justify-end p-pt-3">
                    <Button label="Salvar Configurações" icon="pi pi-save" onClick={handleSave} />
                </div>
            </Card>
        </div>
    );
};
