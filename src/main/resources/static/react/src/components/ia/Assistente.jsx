import React, { useState, useRef, useEffect } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Message } from 'primereact/message';
import { Divider } from 'primereact/divider';
import { ProgressSpinner } from 'primereact/progressspinner';
import { AssistenteService } from '../../services/AssistenteService';
import './Assistente.css';

/**
 * Assistente ERP.
 *
 * Não é "Chat". O que responde é o ERP: o código, o motivo, a origem e a
 * confiança chegam calculados do servidor, e a tela só os exibe. A resposta do
 * modelo aparece separada, e quando ela não vem, a do ERP está inteira.
 *
 * <p>A separação não é vaidade. `22030000` sozinho é um palpite;
 * `22030000, motivo: palavra-chave cerveja, origem: vocabulário curado,
 * confiança: alta` é uma resposta em que a pessoa confia. E a confiança é uma
 * faixa — alta, média, baixa, nenhuma — porque um número pareceria mais preciso
 * do que é.
 */
export const Assistente = () => {
    const [pergunta, setPergunta] = useState('');
    const [resposta, setResposta] = useState(null);
    const [carregando, setCarregando] = useState(false);
    const [erro, setErro] = useState(null);
    const historico = useRef([]);

    const empresaId = (() => {
        try {
            return JSON.parse(localStorage.getItem('empresaSelecionada') || '{}')?.id;
        } catch {
            return null;
        }
    })();

    const exemplos = [
        'Qual NCM devo usar para cerveja?',
        'Qual código de serviço uso para suporte técnico?',
        'Qual CFOP devo usar?',
        'O que está faltando no cadastro deste produto?'
    ];

    useEffect(() => {
        if (!resposta?.resposta) return;
        historico.current.unshift({ pergunta: resposta.resposta });
        if (historico.current.length > 5) historico.current.pop();
    }, [resposta]);

    const perguntar = async () => {
        const texto = pergunta.trim();
        if (!texto || carregando) return;
        setCarregando(true);
        setErro(null);
        try {
            const dados = await AssistenteService.perguntar(empresaId, texto);
            setResposta(dados);
            setPergunta('');
        } catch (e) {
            setErro(e.message || 'Falha ao consultar o assistente');
        } finally {
            setCarregando(false);
        }
    };

    const aoTeclar = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            perguntar();
        }
    };

    return (
        <div className="assistente-page">
            <div className="assistente-cabecalho">
                <h2>Assistente ERP</h2>
                <p className="assistente-sub">
                    A resposta vem do ERP. O modelo explica, não decide.
                </p>
            </div>

            <Card className="assistente-entrada">
                <InputTextarea
                    value={pergunta}
                    onChange={(e) => setPergunta(e.target.value)}
                    onKeyDown={aoTeclar}
                    rows={3}
                    autoResize
                    placeholder="Qual NCM devo usar para cerveja?"
                    className="assistente-campo"
                />
                <div className="assistente-acoes">
                    <Button
                        label={carregando ? 'Consultando…' : 'Perguntar'}
                        icon={carregando ? 'pi pi-spin pi-spinner' : 'pi pi-send'}
                        onClick={perguntar}
                        disabled={carregando || !pergunta.trim()}
                    />
                    {carregando && <ProgressSpinner style={{ width: 22, height: 22 }} strokeWidth="4" />}
                </div>
                <div className="assistente-exemplos">
                    <span>Tente:</span>
                    {exemplos.map((e) => (
                        <button key={e} type="button" className="assistente-chip" onClick={() => setPergunta(e)}>
                            {e}
                        </button>
                    ))}
                </div>
            </Card>

            {erro && <Message severity="error" text={erro} className="assistente-erro" />}

            {resposta && (
                <div className="assistente-resultado">
                    <Card>
                        <div className="assistente-meta">
                            <Tag
                                severity={resposta.respondeuOModelo ? 'success' : 'warn'}
                                value={resposta.respondeuOModelo ? 'Explicado pelo modelo' : 'Resposta só do ERP'}
                            />
                            <span className="assistente-fontes">
                                fontes: {resposta.fontes || 'nenhuma'}
                            </span>
                            <span className="assistente-tempo">{resposta.duracaoMs} ms</span>
                        </div>

                        <h3>Resposta</h3>
                        <div className="assistente-texto">{resposta.resposta}</div>
                    </Card>

                    {resposta.achados?.length > 0 && (
                        <Card>
                            <h3>O que o ERP encontrou</h3>
                            <p className="assistente-nota">
                                Cada item traz o código, o motivo da correspondência, a
                                origem e a confiança. O motivo é o que separa um
                                resultado de um palpite.
                            </p>
                            {resposta.achados.map((a, i) => (
                                <div key={`${a.tipo}-${a.codigo}-${i}`} className="assistente-achado">
                                    <div className="assistente-achado-topo">
                                        {a.codigo && <code className="assistente-codigo">{a.codigo}</code>}
                                        <span className="assistente-titulo">{a.titulo}</span>
                                        <Tag
                                            severity={
                                                a.confianca === 'alta' ? 'success'
                                                    : a.confianca === 'media' ? 'info'
                                                        : a.confianca === 'baixa' ? 'warning' : 'danger'
                                            }
                                            value={a.confianca || 'media'}
                                        />
                                    </div>
                                    <dl className="assistente-campos">
                                        <dt>origem</dt>
                                        <dd>{a.origem}</dd>
                                        <dt>motivo</dt>
                                        <dd>{a.motivo}</dd>
                                        {a.referencia && (
                                            <>
                                                <dt>referência</dt>
                                                <dd>{a.referencia}</dd>
                                            </>
                                        )}
                                    </dl>
                                </div>
                            ))}
                            <Divider />
                            <p className="assistente-nota">
                                modelo: <code>{resposta.modelo}</code>
                            </p>
                        </Card>
                    )}
                </div>
            )}
        </div>
    );
};
