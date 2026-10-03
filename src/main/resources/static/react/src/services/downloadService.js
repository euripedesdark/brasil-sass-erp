import { api } from './ApiConfig';
import { apiFetch } from './ApiConfig';

/**
 * Download autenticado de arquivos (PDF, Excel, CSV).
 *
 * Nao use window.open() para baixar: ele nao envia o header Authorization
 * e o download cai em 401. Aqui o download passa pelo axios, que ja carrega
 * o interceptor de Bearer, e vira um Blob(objectURL).
 */
export const downloadAuthenticated = async (url, fallbackFileName = 'download') => {
    const token = localStorage.getItem('brasil-saas_token');

    const response = await apiFetch(url, {
        method: 'GET',
        headers: token ? { Authorization: `Bearer ${token}` } : {}
    });

    if (!response.ok) {
        let detalhe = `Falha ao gerar arquivo (HTTP ${response.status})`;
        try {
            const body = await response.text();
            if (body) {
                try {
                    const json = JSON.parse(body);
                    detalhe = json.message || json.error || detalhe;
                } catch {
                    detalhe = body.slice(0, 200);
                }
            }
        } catch {
            /* corpo ilegivel, mantem mensagem padrao */
        }
        throw new Error(detalhe);
    }

    const blob = await response.blob();
    const fileName = extrairNomeArquivo(response.headers.get('Content-Disposition'))
        || fallbackFileName;

    salvarBlob(blob, fileName);
};

export const salvarBlob = (blob, fileName) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    // Revogar imediatamente quebra o download em alguns navegadores
    setTimeout(() => window.URL.revokeObjectURL(url), 2000);
};

const extrairNomeArquivo = (contentDisposition) => {
    if (!contentDisposition) return null;
    const utf8 = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8) return decodeURIComponent(utf8[1]);
    const simples = contentDisposition.match(/filename="?([^";]+)"?/i);
    return simples ? simples[1] : null;
};

/**
 * Imprime apenas o conteudo de um elemento, sem a casca da aplicacao
 * (menu lateral, botoes, dialogs). Usa um iframe isolado para nao
 * interferir no layout da tela.
 */
export const imprimirElemento = (elemento, titulo = 'Relatorio') => {
    if (!elemento) {
        window.print();
        return;
    }

    const iframe = document.createElement('iframe');
    iframe.setAttribute('aria-hidden', 'true');
    iframe.style.position = 'fixed';
    iframe.style.right = '0';
    iframe.style.bottom = '0';
    iframe.style.width = '0';
    iframe.style.height = '0';
    iframe.style.border = '0';
    document.body.appendChild(iframe);

    const doc = iframe.contentDocument;
    doc.open();
    doc.write(gerarHtmlImpressao(elemento, titulo));
    doc.close();

    const executar = () => {
        try {
            iframe.contentWindow.focus();
            iframe.contentWindow.print();
        } finally {
            setTimeout(() => iframe.remove(), 1000);
        }
    };

    if (doc.readyState === 'complete') {
        setTimeout(executar, 150);
    } else {
        iframe.onload = () => setTimeout(executar, 150);
    }
};

const gerarHtmlImpressao = (elemento, titulo) => {
    const estilos = Array.from(document.querySelectorAll('style, link[rel="stylesheet"]'))
        .map((node) => node.outerHTML)
        .join('\n');

    return `<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="utf-8" />
    <title>${escaparHtml(titulo)}</title>
    ${estilos}
    <style>
        body { background: #fff; padding: 0; margin: 0; font-family: Arial, Helvetica, sans-serif; }
        .bc-print-header {
            display: none;
        }
        .bc-print-container {
            padding: 12px;
        }
        .p-datatable-wrapper { overflow: visible !important; }
        .p-paginator, .p-datatable-header, .p-dialog, .p-toast, .no-print { display: none !important; }
        table { width: 100% !important; border-collapse: collapse; font-size: 11px; }
        th, td { border: 1px solid #ccc; padding: 4px 6px; text-align: left; }
        th { background: #f0f0f0; }
        .p-button, button { display: none !important; }
        @page { margin: 12mm; }
    </style>
</head>
<body>
    <div class="bc-print-container">${elemento.outerHTML}</div>
</body>
</html>`;
};

const escaparHtml = (valor) =>
    String(valor ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
