import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const MAX_BYTES = 2 * 1024 * 1024;

/**
 * Galeria de imagens do produto.
 *
 * O binário da imagem vive no MongoDB; o Postgres guarda só a referência
 * (id, ordem, url). Por isso a listagem vem de GET /{id}/imagens e cada
 * imagem é lida por GET /{id}/imagens/{imagemId}, que devolve os bytes crus.
 * A tela monta um objectURL para cada uma e revoga ao trocar de produto, senao
 * o navegador segura os arquivos em memoria.
 *
 * Diferente do logo, aqui NAO existe DELETE de imagem no backend. Trocar a
 * principal e enviar outra sao as operacoes disponiveis, e a tela nao finge
 * o contrario.
 */
export const ImagensProduto = ({ produtoId }) => {
    const toast = useRef(null);
    const input = useRef(null);

    const [imagens, setImagens] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [enviando, setEnviando] = useState(false);
    const [urls, setUrls] = useState({});

    const carregar = useCallback(async () => {
        if (!produtoId) { setImagens([]); setCarregando(false); return; }
        setCarregando(true);
        try {
            const r = await apiFetch(`/api/cadastro/produtos/${produtoId}/imagens`);
            const lista = r.ok
                ? ((await r.json())?.data ?? [])
                : [];

            // busca os bytes de cada uma. Falhar uma nao pode derrubar a
            // galeria inteira: mostra a imagem como indisponivel.
            const mapa = {};
            await Promise.all(lista.map(async (img) => {
                try {
                    const bin = await apiFetch(`/api/cadastro/produtos/${produtoId}/imagens/${img.id}`);
                    if (bin.ok) {
                        const blob = await bin.blob();
                        if (blob.size) mapa[img.id] = URL.createObjectURL(blob);
                    }
                } catch { /* imagem corrompida ou ausente */ }
            }));

            setImagens(lista);
            setUrls(mapa);
        } catch {
            setImagens([]);
        } finally {
            setCarregando(false);
        }
    }, [produtoId]);

    // revoga os objectURL anteriores antes de buscar de novo
    useEffect(() => {
        return () => { Object.values(urls).forEach((u) => URL.revokeObjectURL(u)); };
    }, [urls]);

    useEffect(() => { carregar(); }, [carregar]);

    const enviar = async (evento, principal) => {
        const arquivo = evento.target.files?.[0];
        evento.target.value = '';
        if (!arquivo) return;

        if (arquivo.size > MAX_BYTES) {
            toast.current?.show({
                severity: 'warn', summary: 'Arquivo muito grande',
                detail: `A imagem tem ${(arquivo.size / 1024 / 1024).toFixed(1)} MB. O limite é 2 MB.`,
                life: 5000
            });
            return;
        }

        setEnviando(true);
        try {
            const dados = new FormData();
            dados.append('arquivo', arquivo);
            const r = await apiFetch(
                `/api/cadastro/produtos/${produtoId}/imagens?principal=${principal ? 'true' : 'false'}`,
                { method: 'POST', body: dados });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || 'Falha no envio');
            }
            toast.current?.show({
                severity: 'success', summary: 'Imagem enviada',
                detail: 'Guardada no MongoDB', life: 3000
            });
            carregar();
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Erro no envio',
                detail: e.message || 'Não foi possível enviar', life: 5000
            });
        } finally {
            setEnviando(false);
        }
    };

    if (!produtoId) {
        return (
            <Message severity="info"
                     text="Salve o produto antes de enviar imagens — o envio usa o id." />
        );
    }

    return (
        <div className="bc-galeria">
            <Toast ref={toast} />
            <input ref={input} type="file" accept="image/*" hidden
                   onChange={(e) => enviar(e, true)} />

            <div className="flex gap-2 flex-wrap">
                <Button
                    label={enviando ? 'Enviando...' : 'Enviar imagem'}
                    icon="pi pi-upload" size="small" outlined
                    loading={enviando} disabled={enviando}
                    onClick={() => input.current?.click()}
                />
                <Button
                    label="Enviar como principal" icon="pi pi-star" size="small" text
                    disabled={enviando}
                    onClick={() => {
                        const i2 = document.createElement('input');
                        i2.type = 'file'; i2.accept = 'image/*'; i2.hidden = true;
                        i2.onchange = (e) => enviar(e, true);
                        i2.click();
                    }}
                />
            </div>

            {carregando ? (
                <div className="bc-galeria-vazia"><span className="bc-muted">Carregando...</span></div>
            ) : imagens.length === 0 ? (
                <div className="bc-galeria-vazia">
                    <i className="pi pi-image" style={{ fontSize: '1.6rem' }} />
                    <span className="bc-muted">Nenhuma imagem</span>
                </div>
            ) : (
                <div className="bc-galeria-grade">
                    {imagens.map((img) => (
                        <figure key={img.id} className="bc-galeria-item">
                            {urls[img.id]
                                ? <img src={urls[img.id]} alt={`Imagem ${img.ordem}`} />
                                : <div className="bc-galeria-erro">
                                    <i className="pi pi-exclamation-triangle" />
                                  </div>}
                            {img.principal && <Tag value="Principal" severity="success" />}
                        </figure>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ImagensProduto;
