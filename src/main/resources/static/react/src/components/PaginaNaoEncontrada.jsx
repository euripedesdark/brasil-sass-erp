import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { useTranslation } from 'react-i18next';

/**
 * Rota inexistente.
 *
 * Antes o `path="*"` mandava para /inicio. Como /inicio era uma rota sem
 * elemento, o resultado era uma pagina em branco — o usuario achava que a
 * aplicacao tinha quebrado, sem nenhuma pista de que o link estava errado.
 * Aqui a situacao fica explicita e ha saida.
 */
export const PaginaNaoEncontrada = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    return (
        <div className="bc-404">
            <div className="bc-404-card">
                <div className="bc-404-codigo">404</div>
                <h1 className="bc-404-titulo">{t('notFound.title')}</h1>
                <p className="bc-404-texto">
                    {t('notFound.message')}
                </p>
                <div className="bc-404-acoes">
                    <Button
                        label={t('notFound.home')}
                        icon="pi pi-home"
                        onClick={() => navigate('/dashboard', { replace: true })}
                    />
                    <Button
                        label={t('common.back')}
                        icon="pi pi-arrow-left"
                        severity="secondary"
                        text
                        onClick={() => navigate(-1)}
                    />
                </div>
            </div>
        </div>
    );
};

export default PaginaNaoEncontrada;
