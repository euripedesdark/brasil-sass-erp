import React, { useState } from 'react';
import { QRCodeCanvas } from 'qrcode.react';
import { Button } from 'primereact/button';
import { useTranslation } from 'react-i18next';

/**
 * A chave PIX do projeto, em um lugar so.
 *
 * <p>Ela aparece no rodape do sistema, na tela de login e na pagina Sobre. Sao
 * tres lugares, entao a chave mora aqui: trocar a chave e trocar uma linha, e
 * nao tres. O QRCode sai da chave por geracao, entao os tres quadricodigos saem
 * iguais sem ninguem precisar subir imagem.
 */
export const CHAVE_PIX = '24adc62c-b073-4587-974d-03fe35f6733f';

/**
 * Copia a chave e diz que copiou.
 *
 * <p>{@code navigator.clipboard} so existe em contexto seguro: HTTPS ou
 * localhost. Rodando em HTTP num IP da rede, {@code writeText} nao existe e o
 * clique pareceria funcionar sem copiar. Por isso o {@code try}: sem secure
 * context, o texto e selecionavel para copiar na mao, e a UI avisa em vez de
 * fingir.
 */
export function useCopiarPix() {
    const [copiado, setCopiado] = useState(false);

    const copiar = async () => {
        try {
            if (!navigator.clipboard || !navigator.clipboard.writeText) {
                throw new Error('sem secure context');
            }
            await navigator.clipboard.writeText(CHAVE_PIX);
            setCopiado(true);
            setTimeout(() => setCopiado(false), 2500);
            return true;
        } catch {
            setCopiado(false);
            return false;
        }
    };

    return { copiado, copiar };
}

/** O QRCode da chave, no tamanho pedido. */
export function PixQrCode({ size = 160 }) {
    return (
        // width e height fixos no estilo: o <canvas> nasce quadrado, e o CSS
        // em .pix-qrcode canvas mantem isso mesmo dentro de um container largo.
        <div className="pix-qrcode" style={{ width: size + 16, height: size + 16 }}>
            <QRCodeCanvas
                value={CHAVE_PIX}
                size={size}
                level="M"
                includeMargin
                bgColor="#ffffff"
                fgColor="#000000"
            />
        </div>
    );
}

/**
 * A chave e o botao de copiar.
 *
 * <p>A chave fica tambem em {@code <code>} selecionavel. O botao e um meio, nao
 * o unico: se o clipboard falhar, copiar e colar na mao ainda funciona.
 */
export function PixChaveComBotao({ tamanho = 160, mostrarQrCode = true }) {
    const { copiado, copiar } = useCopiarPix();
    const { t } = useTranslation();

    return (
        <div className="pix-apoie">
            {mostrarQrCode && <PixQrCode size={tamanho} />}

            <div className="pix-linha">
                <code className="pix-chave">{CHAVE_PIX}</code>
                <Button
                    label={copiado ? t('footer.copied') : t('footer.copyPix')}
                    icon={copiado ? 'pi pi-check' : 'pi pi-copy'}
                    size="small"
                    severity={copiado ? 'success' : 'secondary'}
                    outlined
                    onClick={copiar}
                    aria-label={t('footer.copyPix')}
                />
            </div>

            {copiado && <small className="pix-ok">{t('footer.copied')}</small>}
        </div>
    );
}

export default PixChaveComBotao;
