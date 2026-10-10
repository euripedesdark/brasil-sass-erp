import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';

const MAX_BYTES = 2 * 1024 * 1024; // 2 MB
const TIPOS = ['image/png', 'image/jpeg', 'image/webp'];

/**
 * Envio e troca de imagem de um registro.
 *
 * Serve a logo do cliente, a do fornecedor e as imagens do produto: os tres
 * casos usam o mesmo par de rotas (subir em POST com multipart e ler em GET
 * devolvendo bytes crus), entao repetir a mesma tela tres vezes so criaria
 * tres lugares para dar problema.
 *
 * A leitura e por `GET` com o id e devolve os bytes direto — nao ha URL
 * publica. Por isso o componente busca o blob e monta um objectURL, e revoga
 * ao trocar, senao o navegador segura o arquivo em memoria ate a aba fechar.
 */
export const ImagemRegistro = ({
    registroId,
    lerUrl,
    enviarUrl,
    removerUrl,
    rotulo = 'Imagem',
    // nome do campo no multipart. O logo usa "logo"; imagem de produto usa
    // "arquivo". Parametrizado porque os dois casos usam o mesmo componente.
    campo = 'logo',
    accept = 'image/*',
    aviso = null
}) => {
    const toast = useRef(null);
    const input = useRef(null);
    const [url, setUrl] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [enviando, setEnviando] = useState(false);
    const [temImagem, setTemImagem] = useState(false);
    // contador para recarregar o blob depois do upload: a API devolve metadado,
    // nao a imagem, entao so bustedar o id nao faria o efeito rodar de novo
    const [revisar, setRevisar] = useState(0);

    useEffect(() => {
        let objetoUrl = null;
        let cancelado = false;

        if (!registroId) {
            setUrl(null); setTemImagem(false); setCarregando(false);
            return undefined;
        }

        setCarregando(true);
        apiFetch(lerUrl)
            .then(async (r) => {
                if (!r.ok) throw new Error('sem imagem');
                const blob = await r.blob();
                if (cancelado || !blob.size) return;
                objetoUrl = URL.createObjectURL(blob);
                setUrl(objetoUrl);
                setTemImagem(true);
            })
            .catch(() => { if (!cancelado) { setTemImagem(false); setUrl(null); } })
            .finally(() => { if (!cancelado) setCarregando(false); });

        return () => {
            cancelado = true;
            // revogar evita segurar o arquivo em memoria a cada troca de linha
            if (objetoUrl) URL.revokeObjectURL(objetoUrl);
        };
    }, [registroId, lerUrl, revisar]);

    const escolher = () => input.current?.click();

    const enviar = async (evento) => {
        const arquivo = evento.target.files?.[0];
        evento.target.value = ''; // permite reenviar o mesmo arquivo
        if (!arquivo) return;

        if (arquivo.size > MAX_BYTES) {
            toast.current?.show({
                severity: 'warn', summary: 'Arquivo muito grande',
                detail: `A imagem tem ${(arquivo.size / 1024 / 1024).toFixed(1)} MB. O limite é 2 MB.`,
                life: 5000
            });
            return;
        }
        if (TIPOS.includes(arquivo.type) === false && !arquivo.type.startsWith('image/')) {
            toast.current?.show({
                severity: 'warn', summary: 'Formato inválido',
                detail: 'Envie uma imagem (PNG, JPG ou WebP).', life: 5000
            });
            return;
        }

        setEnviando(true);
        try {
            const dados = new FormData();
            dados.append(campo, arquivo);
            const r = await apiFetch(enviarUrl, { method: 'POST', body: dados });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || 'Falha no envio');
            }
            toast.current?.show({
                severity: 'success', summary: 'Imagem salva', life: 3000
            });
            setRevisar(v => v + 1);
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Erro no envio',
                detail: e.message || 'Não foi possível enviar a imagem', life: 5000
            });
        } finally {
            setEnviando(false);
        }
    };

    const remover = () => {
        confirmDialog.require({
            header: `Remover ${rotulo.toLowerCase()}`,
            message: 'Remover a imagem deste registro?',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Sim, remover',
            rejectLabel: 'Cancelar',
            accept: async () => {
                try {
                    const r = await apiFetch(removerUrl, { method: 'DELETE' });
                    if (!r.ok && r.status !== 404) throw new Error('Falhou');
                    setUrl(null); setTemImagem(false);
                    toast.current?.show({
                        severity: 'success', summary: 'Removida', life: 3000
                    });
                } catch {
                    toast.current?.show({
                        severity: 'error', summary: 'Erro',
                        detail: 'Não foi possível remover', life: 5000
                    });
                }
            }
        });
    };

    return (
        <div className="bc-imagem">
            <Toast ref={toast} />
            <ConfirmDialog />
            <input ref={input} type="file" accept={accept} hidden onChange={enviar} />

            <div className="bc-imagem-area">
                {carregando ? (
                    <span className="bc-muted">Carregando...</span>
                ) : temImagem && url ? (
                    <img src={url} alt={rotulo} className="bc-imagem-preview" />
                ) : (
                    <div className="bc-imagem-vazia">
                        <i className="pi pi-image" style={{ fontSize: '1.6rem' }} />
                        <span className="bc-muted">Sem {rotulo.toLowerCase()}</span>
                    </div>
                )}
            </div>

            <div className="flex gap-2 flex-wrap">
                <Button
                    label={temImagem ? 'Trocar' : 'Enviar'}
                    icon="pi pi-upload"
                    size="small"
                    outlined
                    loading={enviando}
                    disabled={enviando || !registroId}
                    onClick={escolher}
                />
                {temImagem && (
                    <Button
                        label="Remover"
                        icon="pi pi-trash"
                        size="small"
                        outlined
                        severity="danger"
                        disabled={!registroId}
                        onClick={remover}
                    />
                )}
            </div>

            {aviso && <Message severity="info" className="mt-2" text={aviso} />}
            {!registroId && (
                <Message severity="warn" className="mt-2"
                          text="Salve o registro antes de enviar a imagem — o envio usa o id." />
            )}
        </div>
    );
};

export default ImagemRegistro;
