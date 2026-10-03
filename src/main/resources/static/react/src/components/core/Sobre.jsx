import React from 'react';
import { useTranslation } from 'react-i18next';
import { PixChaveComBotao } from '../shared/ApoiePix';
import './Sobre.css';

const TECNOLOGIAS = [
    'Java 21',
    'Spring Boot 3.3.5',
    'React 19',
    'Vite',
    'PrimeReact',
    'PostgreSQL 18',
    'Spring AI',
    'Samba AD / LDAP',
];

/**
 * Quem usa o projeto, a licenca, de onde veio, o que vem depois e como ajudar.
 *
 * <p>Tudo visivel aqui passa por {@code t()}. O projeto tem quatro idiomas e uma
 * pagina em portugues num sistema que fala quatro linguas e um texto errado
 * aparece na tela sem aviso nenhum.
 *
 * <p>Os itens do roadmap sao os mesmos nos quatro idiomas de proposito: eles
 * descrevem o que existe no codigo. Traduzir os rotulos e obrigatorio; inventar
 * um item so para um idioma, nao.
 */
export default function Sobre() {
    const { t } = useTranslation();

    const fases = [
        { chave: 'roadmapNow', itens: t('about.roadmapNowItems', { returnObjects: true }) },
        { chave: 'roadmapWip', itens: t('about.roadmapWipItems', { returnObjects: true }) },
        { chave: 'roadmapTodo', itens: t('about.roadmapTodoItems', { returnObjects: true }) },
    ];

    return (
        <div className="sobre-page">
            <header className="sobre-hero">
                <div className="sobre-logo">BC</div>
                <div>
                    <h1>{t('about.title', { defaultValue: 'Sobre o Projeto' })}</h1>
                    <p>{t('about.subtitle')}</p>
                </div>
            </header>

            <section className="sobre-card">
                <h2><i className="pi pi-file" /> {t('about.license')}</h2>
                <span className="sobre-licenca">
                    <i className="pi pi-shield" />
                    GNU AGPL v3
                </span>
                <p style={{ marginTop: '0.75rem' }}>{t('about.licenseBody')}</p>
            </section>

            <section className="sobre-card">
                <h2><i className="pi pi-book" /> {t('about.history')}</h2>
                <p>{t('about.historyP1')}</p>
                <p>{t('about.historyP2')}</p>
                <p>{t('about.historyP3')}</p>
            </section>

            <section className="sobre-card">
                <h2><i className="pi pi-map" /> {t('about.roadmap')}</h2>
                {fases.map((fase) => (
                    <div key={fase.chave}>
                        <h3>{t(`about.${fase.chave}`)}</h3>
                        <ul>
                            {(fase.itens || []).map((item) => (
                                <li key={item}>{item}</li>
                            ))}
                        </ul>
                    </div>
                ))}
            </section>

            <section className="sobre-card">
                <h2><i className="pi pi-wrench" /> {t('about.tech')}</h2>
                <ul className="sobre-tecnologias">
                    {TECNOLOGIAS.map((tec) => <li key={tec}>{tec}</li>)}
                </ul>
            </section>

            <section className="sobre-card">
                <h2><i className="pi pi-users" /> {t('about.credits')}</h2>
                <p>{t('about.creditsBody')}</p>
            </section>

            <section className="sobre-card">
                <h2>
                    <i className="pi pi-heart-fill" style={{ color: '#e11d48' }} />{' '}
                    {t('about.support')}
                </h2>
                <div className="sobre-apoie">
                    <div className="sobre-apoie-texto">
                        <p>{t('about.supportP1')}</p>
                        <p>{t('about.supportP2')}</p>
                    </div>
                    <PixChaveComBotao tamanho={160} />
                </div>
            </section>
        </div>
    );
}
