import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { PixQrCode } from './shared/ApoiePix';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Message } from 'primereact/message';
import { Dropdown } from 'primereact/dropdown';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from 'react-i18next';
import i18n, { supportedLanguages } from '../i18n';
import ApiConfig, { apiFetch } from '../services/ApiConfig';
import './Login.css';

/**
 * Fallback local usado apenas quando o MongoDB nao devolve a imagem
 * (SISTEMA_LOGIN). Antes apontava para /images/tela-login.jpeg, arquivo que
 * nao existe mais na pasta — o fallback era um 404 silencioso.
 */
const STATIC_LOGIN_IMG = '/images/tela-inicial.jpeg';

export const Login = () => {
    const { t } = useTranslation();
    const [credentials, setCredentials] = useState({
        username: '',
        password: ''
    });
    const [loading, setLoading] = useState(false);
    const [authMode, setAuthMode] = useState('ad');
    const [error, setError] = useState('');
    const [bgStyle, setBgStyle] = useState({
        backgroundImage: `url(${STATIC_LOGIN_IMG})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat'
    });
    const navigate = useNavigate();
    const { login } = useAuth();

    useEffect(() => {
        let objectUrl = null;

        const applyImageUrl = (url) => {
            setBgStyle({
                backgroundImage: `url(${url})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
                backgroundRepeat: 'no-repeat'
            });
        };

        const loadBg = async () => {
            try {
                const response = await apiFetch(`${ApiConfig.BASE_URL}/api/superadmin/assets/system/SISTEMA_LOGIN`);
                if (response.ok && response.status !== 204) {
                    const blob = await response.blob();
                    if (blob.size > 0) {
                        objectUrl = URL.createObjectURL(blob);
                        applyImageUrl(objectUrl);
                        return;
                    }
                }
            } catch (err) {
                console.warn('Imagem de login (Mongo) indisponível', err);
            }

            const img = new Image();
            img.onload = () => applyImageUrl(STATIC_LOGIN_IMG);
            img.onerror = () => {
                setBgStyle({
                    background: 'linear-gradient(160deg, #0d0d0d 0%, #2a2a2a 50%, #4a4a4a 100%)'
                });
            };
            img.src = STATIC_LOGIN_IMG;
        };

        loadBg();

        return () => {
            if (objectUrl) URL.revokeObjectURL(objectUrl);
        };
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const result = await login(credentials, authMode);
            if (result.success) {
                navigate('/inicio', { replace: true });
            } else {
                setError(result.message || t('login.invalidCredentials'));
            }
        } catch (err) {
            setError(err.message || t('errors.network'));
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCredentials(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const [apoieVisivel, setApoieVisivel] = useState(false);

    return (
        <div className="login-screen" style={bgStyle}>
            <div className="login-overlay">
                <div className="login-box login-card">
                    <div className="login-header">
                        <div className="login-logo">BS</div>
                        <h2>{t('app.name')}</h2>
                        <p>{t('login.subtitle')}</p>
                    </div>

                    {error && (
                        <Message
                            severity="error"
                            text={error}
                            className="w-full mb-3"
                        />
                    )}

                    <form onSubmit={handleSubmit} className="login-form">
                        <div className="input-group">
                            <label>{t('login.username')}</label>
                            <div className="input-wrapper">
                                <InputText
                                    name="username"
                                    value={credentials.username}
                                    onChange={handleChange}
                                    placeholder={t('login.usernamePlaceholder')}
                                    required
                                    autoFocus
                                />
                                <i className="pi pi-user"></i>
                            </div>
                        </div>

                        <div className="input-group">
                            <label>{t('login.password')}</label>
                            <div className="input-wrapper">
                                <Password
                                    name="password"
                                    value={credentials.password}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    required
                                    feedback={false}
                                    toggleMask
                                />
                            </div>
                        </div>

                        <div className="input-group login-auth-mode">
                            <label htmlFor="login-auth-mode">{t('login.authMode')}</label>
                            <Dropdown
                                inputId="login-auth-mode"
                                value={authMode}
                                options={[
                                    { label: t('login.managedAd'), value: 'ad' },
                                    { label: t('login.unmanagedDatabase'), value: 'database' }
                                ]}
                                optionLabel="label"
                                optionValue="value"
                                onChange={(event) => setAuthMode(event.value)}
                                className="login-auth-mode-selector"
                            />
                            <small className="login-auth-mode-help">
                                {authMode === 'ad'
                                    ? t('login.managedAdHelp')
                                    : t('login.unmanagedDatabaseHelp')}
                            </small>
                        </div>

                        <Button
                            label={loading ? t('common.loading') : t('login.submit')}
                            icon="pi pi-sign-in"
                            className="login-btn"
                            type="submit"
                            loading={loading}
                            disabled={loading}
                        />
                    </form>

                    <div className="login-apoie">
                        <Button
                            label={t('donate.button')}
                            icon="pi pi-heart-fill"
                            className="p-button-text"
                            onClick={() => setApoieVisivel(true)}
                        />
                    </div>

                    <div className="login-footer">
                        <div className="flex justify-content-between align-items-center gap-2 flex-wrap">
                            <span>© {new Date().getFullYear()} {t('app.name')}</span>
                            <Dropdown
                                value={i18n.language}
                                options={supportedLanguages}
                                optionLabel="label"
                                optionValue="code"
                                aria-label={t('login.language')}
                                onChange={(event) => i18n.changeLanguage(event.value)}
                                className="login-language"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <Dialog
                header={t('donate.title')}
                visible={apoieVisivel}
                onHide={() => setApoieVisivel(false)}
                modal
                style={{ width: '24rem', maxWidth: '92vw' }}
                className="login-apoie-dialog"
            >
                <p>{t('donate.body')}</p>
                <PixQrCode size={180} />
            </Dialog>
        </div>
    );
};
