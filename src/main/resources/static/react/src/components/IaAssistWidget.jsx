import React, { useState, useEffect, useRef } from 'react';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { ScrollPanel } from 'primereact/scrollpanel';
import { Badge } from 'primereact/badge';
import { Toast } from 'primereact/toast';
import { IconButton } from 'primereact/iconbutton';
import { Card } from 'primereact/card';
import { apiFetch } from '../services/ApiConfig';
import { useTranslation } from 'react-i18next';

/**
 * Widget de IA Assistiva - Interface conversacional embutida
 * Acessível em qualquer tela do sistema
 */
const IaAssistWidget = ({ usuarioId, moduloAtual, telaAtual }) => {
    const { t } = useTranslation();
    const [visible, setVisible] = useState(false);
    const [pergunta, setPergunta] = useState('');
    const [mensagens, setMensagens] = useState([]);
    const [recomendacoes, setRecomendacoes] = useState([]);
    const [carregando, setCarregando] = useState(false);
    const [qtdNaoLidas, setQtdNaoLidas] = useState(0);
    const toast = useRef(null);
    const scrollRef = useRef(null);

    // Carregar recomendações ao montar
    useEffect(() => {
        if (usuarioId) {
            carregarRecomendacoes();
        }
    }, [usuarioId]);

    // Capturar contexto quando mudar de tela
    useEffect(() => {
        if (visible && moduloAtual) {
            capturarContexto();
        }
    }, [moduloAtual, telaAtual, visible]);

    const carregarRecomendacoes = async () => {
        try {
            const response = await apiFetch(`/api/ia/recomendacoes/pendentes/${usuarioId}`);
            if (response.ok) {
                const data = await response.json();
                setRecomendacoes(data);
                setQtdNaoLidas(data.length);
                
                // Adicionar mensagens iniciais
                if (data.length > 0) {
                    const msgIA = {
                        tipo: 'ia',
                           texto: `${t('assistant.greeting')} ${t('assistant.recommendationCount', { count: data.length })}:\n\n` +
                               data.map(r => `• ${r.titulo}`).join('\n'),
                        timestamp: new Date()
                    };
                    setMensagens([msgIA]);
                }
            }
        } catch (error) {
            console.error('Erro ao carregar recomendações:', error);
        }
    };

    const capturarContexto = async () => {
        try {
            const dadosContexto = {
                tela: telaAtual,
                acao: 'VISUALIZANDO',
                dataHora: new Date().toISOString()
            };

            const response = await apiFetch(`/api/ia/contexto?usuarioId=${usuarioId}&modulo=${moduloAtual}&tela=${telaAtual || ''}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dadosContexto)
            });

            if (response.ok) {
                const data = await response.json();
                if (data.recomendacoes && data.recomendacoes.length > 0) {
                    setRecomendacoes(prev => [...prev, ...data.recomendacoes]);
                    setQtdNaoLidas(prev => prev + data.recomendacoes.length);
                }
            }
        } catch (error) {
            console.error('Erro ao capturar contexto:', error);
        }
    };

    const enviarPergunta = async () => {
        if (!pergunta.trim()) return;

        const perguntaUsuario = {
            tipo: 'usuario',
            texto: pergunta,
            timestamp: new Date()
        };

        setMensagens(prev => [...prev, perguntaUsuario]);
        setPergunta('');
        setCarregando(true);

        try {
            const response = await apiFetch(`/api/ia/pergunta?usuarioId=${usuarioId}&pergunta=${encodeURIComponent(pergunta)}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const data = await response.json();
                const respostaIA = {
                    tipo: 'ia',
                    texto: data.resposta,
                    timestamp: new Date()
                };
                setMensagens(prev => [...prev, respostaIA]);
            }
        } catch (error) {
            const erroIA = {
                tipo: 'ia',
                texto: 'Desculpe, ocorreu um erro ao processar sua pergunta. Tente novamente.',
                timestamp: new Date()
            };
            setMensagens(prev => [...prev, erroIA]);
        } finally {
            setCarregando(false);
        }
    };

    const registrarFeedback = async (recomendacaoId, foiUtil) => {
        try {
            await apiFetch(`/api/ia/feedback?recomendacaoId=${recomendacaoId}&foiUtil=${foiUtil}`, {
                method: 'POST'
            });
            
            toast.current.show({
                severity: 'success',
                summary: t('assistant.thanks'),
                detail: t('assistant.feedbackRegistered')
            });

            carregarRecomendacoes(); // Atualizar lista
        } catch (error) {
            console.error('Erro ao registrar feedback:', error);
        }
    };

    const getCorCriticidade = (nivel) => {
        switch (nivel) {
            case 'CRITICA': return '#dc3545';
            case 'ALTA': return '#fd7e14';
            case 'MEDIA': return '#ffc107';
            default: return '#28a745';
        }
    };

    const header = (
        <div className="flex align-items-center gap-2">
            <img
                src="/images/brasil-saas-person.jpeg"
                alt="Assistente Brasil SaaS"
                style={{ width: '38px', height: '38px', objectFit: 'cover', objectPosition: '57% 50%', borderRadius: '50%' }}
            />
            <span className="font-bold text-lg">{t('assistant.title')}</span>
            {qtdNaoLidas > 0 && (
                <Badge value={qtdNaoLidas} severity="danger" />
            )}
        </div>
    );

    return (
        <>
            <Toast ref={toast} />
            
            {/* Botão flutuante */}
            <Button 
                onClick={() => setVisible(true)}
                className="p-button-rounded p-button-primary fixed"
                aria-label={t('assistant.open')}
                style={{ 
                    bottom: '20px', 
                    right: '20px',
                    width: '60px',
                    height: '60px',
                    fontSize: '24px',
                    zIndex: 1000
                }}
            >
                <img
                    src="/images/brasil-saas-person.jpeg"
                    alt=""
                    aria-hidden="true"
                    style={{ width: '54px', height: '54px', objectFit: 'cover', objectPosition: '57% 50%', borderRadius: '50%' }}
                />
                {qtdNaoLidas > 0 && (
                    <Badge value={qtdNaoLidas} severity="danger" 
                           style={{ position: 'absolute', top: '-5px', right: '-5px' }} />
                )}
            </Button>

            {/* Dialog da IA */}
            <Dialog 
                header={header} 
                visible={visible} 
                maximizable
                style={{ width: '50vw', maxWidth: '600px' }}
                onHide={() => setVisible(false)}
                footer={null}
            >
                <div className="flex flex-column h-20rem">
                    {/* Área de mensagens */}
                    <ScrollPanel style={{ height: '400px', marginBottom: '1rem' }} ref={scrollRef}>
                        {mensagens.map((msg, idx) => (
                            <Card 
                                key={idx}
                                className={`mb-3 ${msg.tipo === 'usuario' ? 'bg-blue-50' : 'bg-gray-50'}`}
                                title={msg.tipo === 'usuario' ? t('assistant.you') : t('assistant.title')}
                                subTitle={msg.timestamp.toLocaleTimeString()}
                            >
                                <p className="m-0">{msg.texto}</p>
                            </Card>
                        ))}
                        
                        {carregando && (
                            <Card className="mb-3 bg-gray-50">
                                <p className="m-0"><i className="pi pi-spin pi-spinner"></i> {t('assistant.typing')}</p>
                            </Card>
                        )}
                    </ScrollPanel>

                    {/* Recomendações ativas */}
                    {recomendacoes.length > 0 && (
                        <div className="mb-3">
                            <h5 className="mt-0 mb-2">{t('assistant.recommendations')}</h5>
                            {recomendacoes.slice(0, 3).map((rec, idx) => (
                                <Card 
                                    key={idx}
                                    className="mb-2"
                                    style={{ borderLeft: `4px solid ${getCorCriticidade(rec.criticidade)}` }}
                                    title={rec.titulo}
                                    subTitle={`Criticidade: ${rec.criticidade}`}
                                >
                                    <p className="m-0 mb-2">{rec.mensagem}</p>
                                    <small className="text-color-secondary">{rec.justificativa}</small>
                                    <div className="flex gap-2 mt-2">
                                        <Button 
                                            label={t('assistant.useful')}
                                            icon="pi pi-check" 
                                            size="small"
                                            onClick={() => registrarFeedback(rec.id, true)}
                                        />
                                        <Button 
                                            label={t('assistant.notUseful')}
                                            icon="pi pi-times" 
                                            size="small"
                                            severity="secondary"
                                            onClick={() => registrarFeedback(rec.id, false)}
                                        />
                                    </div>
                                </Card>
                            ))}
                        </div>
                    )}

                    {/* Input de pergunta */}
                    <div className="flex gap-2">
                        <InputText 
                            value={pergunta}
                            onChange={(e) => setPergunta(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && enviarPergunta()}
                            placeholder={t('assistant.questionPlaceholder')}
                            className="flex-1"
                        />
                        <Button 
                            label={t('common.send')}
                            icon="pi pi-send"
                            onClick={enviarPergunta}
                            disabled={carregando || !pergunta.trim()}
                        />
                    </div>
                </div>
            </Dialog>
        </>
    );
};

export default IaAssistWidget;
