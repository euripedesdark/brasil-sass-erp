import React, { useEffect, useRef, useState } from 'react';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';
import { Checkbox } from 'primereact/checkbox';
import { Tag } from 'primereact/tag';
import { apiFetch } from '../../services/ApiConfig';
import { useTranslation } from 'react-i18next';
import './ModuloSelector.css';

/**
 * Seletor de modulos por usuario.
 *
 * Antes o acesso era so por perfil, e quem trabalhava em Financeiro e Estoque
 * precisava de um perfil "hifenizado" so para isso. Aqui a pessoa marca os
 * modulos que pode acessar — pode marcar varios, e cada um pode ser somente
 * leitura.
 *
 * O conjunto enviado SUBSTITUI o anterior: o que a tela mostra e o que fica.
 */
export const ModuloSelector = ({ visible, usuario, onHide, onSalvo }) => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [modulos, setModulos] = useState([]);
    const [selecionados, setSelecionados] = useState({});
    const [somenteLeitura, setSomenteLeitura] = useState({});
    const [carregando, setCarregando] = useState(false);
    const [salvando, setSalvando] = useState(false);
    const [erro, setErro] = useState('');

    useEffect(() => {
        if (!visible || !usuario?.id) return;
        setCarregando(true);
        setErro('');
        (async () => {
            try {
                const r = await apiFetch(`/api/superadmin/usuarios/${usuario.id}/modulos`);
                if (!r.ok) throw new Error((await r.json())?.errors?.[0]?.message || `HTTP ${r.status}`);
                const d = await r.json();
                setModulos(d.modulos || []);
                const sel = {};
                const leitura = {};
                (d.modulos || []).forEach((m) => {
                    sel[m.id] = !!m.liberado;
                    leitura[m.id] = !!m.somenteLeitura;
                });
                setSelecionados(sel);
                setSomenteLeitura(leitura);
            } catch (e) {
                setErro(e.message);
            } finally {
                setCarregando(false);
            }
        })();
    }, [visible, usuario?.id]);

    const alternar = (id) => {
        setSelecionados((s) => ({ ...s, [id]: !s[id] }));
        if (selecionados[id]) {
            setSomenteLeitura((s) => ({ ...s, [id]: false }));
        }
    };

    const alternarLeitura = (id) => {
        if (!selecionados[id]) return;
        setSomenteLeitura((s) => ({ ...s, [id]: !s[id] }));
    };

    const marcarTodos = (marcar) => {
        const todos = {};
        modulos.forEach((m) => { todos[m.id] = marcar; });
        setSelecionados(todos);
        if (!marcar) setSomenteLeitura({});
    };

    const salvar = async () => {
        const moduloIds = Object.entries(selecionados)
            .filter(([, v]) => v)
            .map(([k]) => Number(k));
        const leitura = {};
        moduloIds.forEach((id) => { leitura[id] = !!somenteLeitura[id]; });

        setSalvando(true);
        setErro('');
        try {
            const r = await apiFetch(`/api/superadmin/usuarios/${usuario.id}/modulos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ moduloIds, somenteLeitura: leitura })
            });
            if (!r.ok) throw new Error((await r.json())?.errors?.[0]?.message || `HTTP ${r.status}`);

            const total = moduloIds.length;
            toast.current?.show({
                severity: 'success',
                summary: t('legacyUi.moduleSelector.updated'),
                detail: total === 0
                    ? t('legacyUi.moduleSelector.noAccess', { username: usuario.username })
                    : t('legacyUi.moduleSelector.accessCount', { username: usuario.username, count: total }),
                life: 3500
            });
            onSalvo?.();
        } catch (e) {
            setErro(e.message);
        } finally {
            setSalvando(false);
        }
    };

    const marcados = Object.values(selecionados).filter(Boolean).length;

    return (
        <Dialog
            visible={visible}
            onHide={onHide}
            modal
            draggable={false}
            style={{ width: 'min(680px, 94vw)' }}
            header={t('legacyUi.moduleSelector.header', { username: usuario?.username || '' })}
            footer={
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.5rem', alignItems: 'center' }}>
                    <span className="text-muted" style={{ fontSize: '0.82rem' }}>
                        {t('legacyUi.moduleSelector.selected', { selected: marcados, total: modulos.length })}
                    </span>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <Button label={t('common.cancel')} severity="secondary" text onClick={onHide} />
                        <Button label={t('common.save')} icon="pi pi-check" loading={salvando} onClick={salvar} />
                    </div>
                </div>
            }
        >
            <Toast />

            {erro && <Message severity="error" text={erro} className="mb-3" onLifeEnd={() => setErro('')} />}

            {carregando ? (
                <Message severity="info" text={t('legacyUi.moduleSelector.loading')} />
            ) : (
                <>
                    <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.9rem' }}>
                        <Button
                            label={t('legacyUi.moduleSelector.markAll')}
                            size="small" text icon="pi pi-check-square"
                            onClick={() => marcarTodos(true)}
                        />
                        <Button
                            label={t('legacyUi.moduleSelector.unmarkAll')}
                            size="small" text icon="pi pi-stop"
                            severity="secondary"
                            onClick={() => marcarTodos(false)}
                        />
                    </div>

                    <div className="modulo-selector">
                        {modulos.map((m) => (
                            <div
                                key={m.id}
                                className={`modulo-selector__item ${selecionados[m.id] ? 'is-on' : ''}`}
                            >
                                <div className="modulo-selector__main">
                                    <Checkbox
                                        inputId={`mod-${m.id}`}
                                        checked={!!selecionados[m.id]}
                                        onChange={() => alternar(m.id)}
                                    />
                                    <label htmlFor={`mod-${m.id}`} className="modulo-selector__label">
                                        <i className={m.icone || 'pi pi-box'} />
                                        <span>{m.nome}</span>
                                        {m.exigeSuperuser && (
                                            <Tag severity="danger" value={t('legacyUi.moduleSelector.restricted')} />
                                        )}
                                    </label>
                                </div>

                                <div className="modulo-selector__desc">{m.descricao}</div>

                                {selecionados[m.id] && !m.exigeSuperuser && (
                                    <label className="modulo-selector__somente">
                                        <Checkbox
                                            inputId={`somente-${m.id}`}
                                            checked={!!somenteLeitura[m.id]}
                                            onChange={() => alternarLeitura(m.id)}
                                        />
                                        <span>{t('legacyUi.moduleSelector.readOnly')}</span>
                                    </label>
                                )}

                                {m.exigeSuperuser && (
                                    <small className="modulo-selector__aviso">
                                        {t('legacyUi.moduleSelector.sensitiveWarning')}
                                    </small>
                                )}
                            </div>
                        ))}
                    </div>

                    <Message
                        severity="info"
                        text={t('legacyUi.moduleSelector.unmarkHelp')}
                        className="mt-3"
                    />
                </>
            )}
        </Dialog>
    );
};

export default ModuloSelector;
