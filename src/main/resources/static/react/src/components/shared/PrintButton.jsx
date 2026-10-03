import React, { useRef } from 'react';
import { Button } from 'primereact/button';
import { imprimirElemento } from '../../services/downloadService';

/**
 * Botao de impressao reutilizavel.
 *
 * Imprime o conteudo do `alvo` (por padrão a tabela da própria tela).
 * Quando o backend oferece PDF, prefira passar `onDownload` para baixar
 * o PDF do servidor em vez de gerar no navegador.
 */
export const PrintButton = ({
    label = 'Imprimir',
    icon = 'pi pi-print',
    alvoRef,
    titulo = 'Relatorio',
    onDownload,
    loading = false,
    disabled = false,
    className = 'p-button-outlined p-button-sm'
}) => {
    const fallbackRef = useRef(null);

    const imprimir = async () => {
        if (onDownload) {
            await onDownload();
            return;
        }
        imprimirElemento(alvoRef?.current || fallbackRef.current, titulo);
    };

    return (
        <span ref={fallbackRef}>
            <Button
                label={label}
                icon={icon}
                className={className}
                onClick={imprimir}
                loading={loading}
                disabled={disabled}
            />
        </span>
    );
};

export default PrintButton;
