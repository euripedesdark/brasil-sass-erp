import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { ScrollPanel } from 'primereact/scrollpanel';
import IaService from '../../services/IaService';
import { useAuth } from '../../contexts/AuthContext';

export const IA = () => {
  const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [chats, setChats] = useState([]);
    const [mensagens, setMensagens] = useState([]);
    const [loading, setLoading] = useState(false);
    const [sending, setSending] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    
    const [novoChat, setNovoChat] = useState({
        id: null,
        titulo: '',
        tipo: 'GERAL',
        ativo: true
    });

    const [mensagemAtual, setMensagemAtual] = useState('');
    const [chatSelecionado, setChatSelecionado] = useState(null);

    const tipoOptions = [
        { label: t('legacyUi.ia.general'), value: 'GERAL' },
        { label: t('legacyUi.ia.finance'), value: 'FINANCEIRO' },
        { label: t('legacyUi.ia.sales'), value: 'VENDAS' },
        { label: t('legacyUi.ia.inventory'), value: 'ESTOQUE' },
        { label: t('legacyUi.ia.tax'), value: 'FISCAL' },
        { label: 'RH', value: 'RH' },
        { label: t('legacyUi.ia.production'), value: 'PRODUCAO' }
    ];

    const promptOptions = [
        { label: 'Analisar faturamento do mês', value: 'Analise o faturamento do mês atual e compare com o mês anterior' },
        { label: 'Sugerir produtos para comprar', value: 'Com base no estoque atual, sugira produtos para repor' },
        { label: 'Resumir vendas do dia', value: 'Resuma as vendas realizadas hoje' },
        { label: 'Analisar margem de lucro', value: 'Analise a margem de lucro dos últimos pedidos' },
        { label: 'Previsão de fluxo de caixa', value: 'Faça uma previsão de fluxo de caixa para os próximos 30 dias' }
    ];

    useEffect(() => {
        fetchChats();
    }, []);

    const fetchChats = async () => {
        if (!user?.empresaId) return;
        setLoading(true);
        try {
            const response = await IaService.listarSessoes(user.empresaId);
            if (response.ok) setChats(await response.json());
        } catch (err) {
            console.error(t('legacyUi.ia.loadChats'), err);
        } finally { setLoading(false); }
    };

    const fetchMensagens = async (chatId) => {
        if (!user?.empresaId || !chatId) return;
        setLoading(true);
        try {
            const response = await IaService.listarMensagens(user.empresaId, chatId);
            if (response.ok) setMensagens(await response.json());
        } catch (err) {
            console.error(t('legacyUi.ia.loadMessages'), err);
        } finally { setLoading(false); }
    };

    const enviarMensagem = async () => {
        if (!mensagemAtual.trim() || !chatSelecionado) return;
        
        setSending(true);
        setError('');
        
        try {
            const request = {
                sessionId: chatSelecionado.id,
                message: mensagemAtual,
                model: chatSelecionado.modeloIa || 'default',
                temperature: chatSelecionado.temperatura ?? 0.7,
                maxTokens: chatSelecionado.maxTokens ?? 4096,
                history: mensagens.map(msg => ({
                    role: msg.tipo === 'USUARIO' ? 'user' : 'assistant',
                    content: msg.conteudo || msg.mensagem || msg.resposta || ''
                }))
            };
            const response = await IaService.enviarMensagem(user.empresaId, request);
            if (response.ok) {
                const data = await response.json();
                const history = Array.isArray(data.history) ? data.history : [];
                setMensagens(prev => [
                    ...prev,
                    ...history.slice(-2).map((msg, index) => ({
                        id: Date.now() + index,
                        sessaoId: chatSelecionado.id,
                        tipo: msg.role === 'user' ? 'USUARIO' : 'ASSISTENTE',
                        conteudo: msg.content,
                        dataEnvio: data.timestamp || new Date().toISOString()
                    }))
                ]);
                setMensagemAtual('');
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || t('legacyUi.ia.sendError'),
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao enviar mensagem');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexão ao enviar mensagem',
                life: 3000
            });
        } finally {
            setSending(false);
        }
    };

    const salvarChat = async () => {
        setLoading(true);
        setError('');
        
        try {
            const chatParaSalvar = {
                titulo: novoChat.titulo,
                usuarioId: user?.id,
                modeloIa: 'default',
                temperatura: 0.7,
                maxTokens: 4096,
                ativo: novoChat.ativo,
                favorito: false
            };
            const response = novoChat.id
                ? await IaService.atualizarSessao(user.empresaId, novoChat.id, chatParaSalvar)
                : await IaService.criarSessao(user.empresaId, chatParaSalvar);

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: `Chat ${novoChat.id ? 'atualizado' : 'criado'} com sucesso`,
                    life: 3000
                });
                setDialogVisible(false);
                fetchChats();
                resetForm();
            } else {
                const err = await response.text();
                setError(err);
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || t('legacyUi.ia.saveError'),
                    life: 3000
                });
            }
        } catch (err) {
            setError('Erro ao salvar chat');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexão ao salvar chat',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirChat = async (id) => {
        try {
            const response = await IaService.excluirSessao(user.empresaId, id);

            if (response.ok) {
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: t('legacyUi.ia.deleted'),
                    life: 3000
                });
                fetchChats();
                if (chatSelecionado?.id === id) {
                    setChatSelecionado(null);
                    setMensagens([]);
                }
            } else {
                const err = await response.text();
                toast.current?.show({
                    severity: 'error',
                    summary: 'Erro',
                    detail: err || t('legacyUi.ia.deleteError'),
                    life: 3000
                });
            }
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Erro de conexão ao excluir chat',
                life: 3000
            });
        }
    };

    const abrirDialog = (chat = null) => {
        if (chat) {
            setNovoChat({
                id: chat.id,
                titulo: chat.titulo || '',
                tipo: chat.tipo || 'GERAL',
                ativo: chat.ativo !== false
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoChat({
            id: null,
            titulo: '',
            tipo: 'GERAL',
            ativo: true
        });
        setError('');
    };

    const selecionarChat = (chat) => {
        setChatSelecionado(chat);
        fetchMensagens(chat.id);
    };

    const usarPrompt = (prompt) => {
        setMensagemAtual(prompt);
    };

    const formatarData = (data) => {
        if (!data) return '';
        return new Date(data).toLocaleString('pt-BR');
    };

    const tipoTemplate = (rowData) => {
        const tipoMap = {
            'GERAL': t('legacyUi.ia.general'),
            'FINANCEIRO': t('legacyUi.ia.finance'),
            'VENDAS': t('legacyUi.ia.sales'),
            'ESTOQUE': t('legacyUi.ia.inventory'),
            'FISCAL': t('legacyUi.ia.tax'),
            'RH': 'RH',
            'PRODUCAO': t('legacyUi.ia.production')
        };
        const tipoColors = {
            'GERAL': 'primary',
            'FINANCEIRO': 'success',
            'VENDAS': 'info',
            'ESTOQUE': 'warning',
            'FISCAL': 'help',
            'RH': 'pink',
            'PRODUCAO': 'purple'
        };
        return <Tag value={tipoMap[rowData.tipo] || rowData.tipo} severity={tipoColors[rowData.tipo] || 'info'} />;
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="ia-acoes">
                <Button
                    icon="pi pi-comments"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => selecionarChat(rowData)}
                    tooltip="Abrir Chat"
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
                    onClick={() => excluirChat(rowData.id)}
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
                label="Salvar Chat"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarChat}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="ia-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Inteligência Artificial - Brasil SaaS ERP" className="ia-main-card">
                <div className="ia-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="ia-info">
                        <p className="text-muted m-0">Assistente virtual com Spring AI + OpenAI para análise inteligente de dados.</p>
                    </div>
                    <Button
                        label="Novo Chat"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <div className="ia-layout grid">
                    {/* Lista de Chats */}
                    <div className="col-12 md:col-4">
                        <Card title="Meus Chats" className="ia-chats-card">
                            <DataTable
                                value={chats}
                                loading={loading}
                                paginator
                                rows={10}
                                rowsPerPageOptions={[10, 20, 50]}
                                responsiveLayout="scroll"
                                className="p-datatable-sm"
                                emptyMessage="Nenhum chat encontrado"
                                selectionMode="single"
                                selection={chatSelecionado}
                                onSelectionChange={(e) => selecionarChat(e.value)}
                            >
                                <Column field="titulo" header="Título" sortable style={{ width: '150px' }} />
                                <Column field="tipo" header="Tipo" body={tipoTemplate} sortable style={{ width: '100px' }} />
                                <Column field="dataCriacao" header="Criado em" body={formatarData} sortable style={{ width: '150px' }} />
                                <Column body={acoesTemplate} style={{ width: '100px' }} />
                            </DataTable>
                        </Card>

                        {/* Prompts Rápidos */}
                        <Card title="Prompts Rápidos" className="ia-prompts-card mt-3">
                            <div className="ia-prompts-grid">
                                {promptOptions.map((prompt, index) => (
                                    <Button
                                        key={index}
                                        label={prompt.label}
                                        icon="pi pi-bolt"
                                        className="p-button-outlined p-button-sm ia-prompt-button"
                                        onClick={() => usarPrompt(prompt.value)}
                                        tooltip={prompt.value}
                                    />
                                ))}
                            </div>
                        </Card>
                    </div>

                    {/* Área de Chat */}
                    <div className="col-12 md:col-8">
                        <Card title={chatSelecionado ? `Chat: ${chatSelecionado.titulo}` : 'Selecione um Chat'} className="ia-chat-card">
                            {chatSelecionado ? (
                                <>
                                    <ScrollPanel style={{ height: '400px', width: '100%' }}>
                                        <div className="ia-messages-container">
                                            {mensagens.length === 0 ? (
                                                <div className="ia-empty-chat">
                                                    <i className="pi pi-comments" style={{ fontSize: '3rem', color: '#9ca3af' }}></i>
                                                    <p>Nenhuma mensagem ainda. Inicie uma conversa!</p>
                                                </div>
                                            ) : (
                                                mensagens.map((msg, index) => (
                                                    <div key={index} className={`ia-message ${msg.tipo === 'USUARIO' ? 'ia-message-user' : 'ia-message-ai'}`}>
                                                        <div className="ia-message-content">
                                                            <strong>{msg.tipo === 'USUARIO' ? 'Você' : 'IA Assistente'}</strong>
                                                            <p>{msg.conteudo || msg.mensagem || msg.resposta}</p>
                                                            <small className="ia-message-time">{formatarData(msg.dataEnvio)}</small>
                                                        </div>
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    </ScrollPanel>

                                    <Divider />

                                    <div className="ia-message-input">
                                        <InputTextarea
                                            value={mensagemAtual}
                                            onChange={(e) => setMensagemAtual(e.target.value)}
                                            placeholder="Digite sua mensagem..."
                                            rows={3}
                                            autoFocus
                                            onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && enviarMensagem()}
                                        />
                                        <Button
                                            label="Enviar"
                                            icon="pi pi-send"
                                            className="p-button-primary mt-2"
                                            onClick={enviarMensagem}
                                            loading={sending}
                                            disabled={!mensagemAtual.trim()}
                                        />
                                    </div>
                                </>
                            ) : (
                                <div className="ia-empty-chat">
                                    <i className="pi pi-comments" style={{ fontSize: '4rem', color: '#9ca3af' }}></i>
                                    <h3>Bem-vindo à IA Assistente</h3>
                                    <p>Selecione um chat existente ou crie um novo para começar.</p>
                                    <p className="text-muted">A IA pode ajudar com:</p>
                                    <ul className="ia-features-list">
                                        <li>Análise de faturamento e vendas</li>
                                        <li>Sugestões de compras e estoque</li>
                                        <li>Previsões financeiras</li>
                                        <li>Classificação de produtos (NCM)</li>
                                        <li>Resumo de relatórios</li>
                                    </ul>
                                </div>
                            )}
                        </Card>
                    </div>
                </div>
            </Card>

            {/* Dialog de Chat */}
            <Dialog
                header={novoChat.id ? `Editar Chat: ${novoChat.titulo}` : 'Novo Chat'}
                visible={dialogVisible}
                style={{ width: '500px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Título do Chat</label>
                            <InputText
                                value={novoChat.titulo}
                                onChange={(e) => setNovoChat({...novoChat, titulo: e.target.value})}
                                placeholder="Análise de {t('legacyUi.ia.sales')} - Setembro"
                            />
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Tipo de Chat</label>
                            <Dropdown
                                value={novoChat.tipo}
                                options={tipoOptions}
                                onChange={(e) => setNovoChat({...novoChat, tipo: e.value})}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 field">
                            <div className="p-checkbox p-col-12">
                                <input
                                    type="checkbox"
                                    id="ativo"
                                    checked={novoChat.ativo}
                                    onChange={(e) => setNovoChat({...novoChat, ativo: e.target.checked})}
                                    className="p-checkbox-input"
                                />
                                <label htmlFor="ativo" className="p-checkbox-label ml-2">
                                    <strong>Chat Ativo</strong>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default IA;
