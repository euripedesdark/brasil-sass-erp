import React from 'react';

export const NotificationService = {
    showSuccess: (message) => {
        window.dispatchEvent(new CustomEvent('bc-toast', {
            detail: { severity: 'success', summary: 'Sucesso', detail: message }
        }));
    },
    showError: (message) => {
        window.dispatchEvent(new CustomEvent('bc-toast', {
            detail: { severity: 'error', summary: 'Erro', detail: message }
        }));
    },
    showWarn: (message) => {
        window.dispatchEvent(new CustomEvent('bc-toast', {
            detail: { severity: 'warn', summary: 'Atenção', detail: message }
        }));
    }
};
