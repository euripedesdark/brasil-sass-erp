import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Tag } from 'primereact/tag';
import { Button } from 'primereact/button';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from 'react-i18next';
import ApiConfig, { apiFetch } from '../services/ApiConfig';
import './Dashboard.css';

export const Dashboard = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [indicadores, setIndicadores] = useState([]);
    // Auditoria recente. A rota existia desde sempre e nenhuma tela a usava: o
    // registro de quem mexeu em quê, e de onde, ficava gravado e invisível.
    const [recentes, setRecentes] = useState([]);
    const [carregandoRecentes, setCarregandoRecentes] = useState(true);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const load = async () => {
            if (!user?.empresaId) return;
            setLoading(true);
            try {
                const res = await apiFetch(
                    `${ApiConfig.BASE_URL}/api/bi/indicadores/dashboard`,
                    { credentials: 'include' }
                );
                if (res.ok) {
                    const json = await res.json();
                    const data = json?.data ?? json;
                    setIndicadores(Array.isArray(data) ? data : []);
                }
            } catch (e) {
                console.warn('Indicadores do dashboard indisponiveis', e);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [user?.empresaId]);

    const atalhos = [
        { label: t('dashboard.salesOrders'), icon: 'pi pi-shopping-cart', path: '/vendas', color: '#3b82f6' },
        { label: t('dashboard.purchaseOrders'), icon: 'pi pi-truck', path: '/compras', color: '#8b5cf6' },
        { label: t('menu.stockBalances'), icon: 'pi pi-box', path: '/estoque', color: '#10b981' },
        { label: t('nav.finance'), icon: 'pi pi-wallet', path: '/financeiro', color: '#f59e0b' },
        { label: t('menu.titles'), icon: 'pi pi-money-bill', path: '/financeiro/titulos', color: '#ef4444' },
        { label: t('menu.cash'), icon: 'pi pi-credit-card', path: '/financeiro/caixa', color: '#06b6d4' },
        { label: t('menu.taxEntries'), icon: 'pi pi-file', path: '/fiscal/entradas', color: '#6366f1' },
        { label: t('menu.serviceOrders'), icon: 'pi pi-wrench', path: '/ordens-servico', color: '#84cc16' },
        { label: t('nav.production'), icon: 'pi pi-cog', path: '/producao', color: '#64748b' },
        { label: t('menu.kpis'), icon: 'pi pi-chart-line', path: '/bi/kpis', color: '#ec4899' },
    ];

    useEffect(() => {
        // A empresa vem do token: sem empresaId a rota devolvia a auditoria de
        // todos os tenants, o que o backend agora impede.
        apiFetch('/api/core/recent/updates?limite=10')
            .then((r) => (r.ok ? r.json() : []))
            .then((j) => setRecentes(Array.isArray(j?.data) ? j.data : (Array.isArray(j) ? j : [])))
            .catch(() => setRecentes([]))
            .finally(() => setCarregandoRecentes(false));
    }, []);

    const quando = (v) => {
        if (!v) return '—';
        const d = new Date(v);
        if (Number.isNaN(d.getTime())) return String(v);
        const hoje = new Date().toDateString() === d.toDateString();
        return hoje
            ? d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
            : d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
    };

    return (
        <div className="dashboard-container">
            <Card title={t('dashboard.operationalTitle')} subTitle={t('dashboard.welcomeUser', { name: user?.nome || user?.username || '' })}>
                <p className="dashboard-intro">
                    {t('dashboard.description')}
                </p>

                <div className="dashboard-atalhos">
                    {atalhos.map((a) => (
                        <button
                            key={a.path}
                            type="button"
                            className="dashboard-atalho"
                            style={{ borderTopColor: a.color }}
                            onClick={() => navigate(a.path)}
                        >
                            <i className={a.icon} style={{ color: a.color }} />
                            <span>{a.label}</span>
                        </button>
                    ))}
                </div>

                <h3 className="dashboard-section-title">{t('dashboard.indicators')}</h3>
                {loading && <p>{t('common.loading')}</p>}
                {!loading && indicadores.length === 0 && (
                    <p className="dashboard-muted">
                        {t('dashboard.noIndicators')}
                    </p>
                )}
                <div className="dashboard-kpis">
                    {indicadores.slice(0, 8).map((ind) => (
                        <div key={ind.id || ind.codigo} className="dashboard-kpi-card">
                            <span className="dashboard-kpi-label">{ind.nome || ind.codigo}</span>
                            <span className="dashboard-kpi-value">
                                {ind.valorAtual != null
                                    ? Number(ind.valorAtual).toLocaleString('pt-BR')
                                    : '—'}
                            </span>
                            {ind.unidade && (
                                <span className="dashboard-kpi-unit">{ind.unidade}</span>
                            )}
                        </div>
                    ))}
                </div>

                <div className="dashboard-footer-actions">
                    <Button
                        label={t('dashboard.refreshIndicators')}
                        icon="pi pi-refresh"
                        className="p-button-outlined"
                        onClick={async () => {
                            if (!user?.empresaId) return;
                            try {
                                await apiFetch(
                                    `${ApiConfig.BASE_URL}/api/bi/indicadores/atualizar/valores`,
                                    { method: 'POST', credentials: 'include' }
                                );
                                const res = await apiFetch(
                                    `${ApiConfig.BASE_URL}/api/bi/indicadores/dashboard?empresaId=${user.empresaId}`,
                                    { credentials: 'include' }
                                );
                                if (res.ok) {
                                    const json = await res.json();
                                    setIndicadores(Array.isArray(json?.data) ? json.data : json || []);
                                }
                            } catch (e) {
                                console.warn(e);
                            }
                        }}
                    />
                </div>
            </Card>

            <Card title={t('dashboard.recentTitle')} className="mt-3"
                subTitle={t('dashboard.recentSubtitle')}>
                <DataTable value={recentes} loading={carregandoRecentes} dataKey="id"
                           size="small" paginator rows={5} rowsPerPageOptions={[5, 10, 20]}
                           emptyMessage={t('dashboard.noRecentUpdates')}
                           responsiveLayout="scroll">
                    <Column field="modulo" header={t('dashboard.module')} style={{ width: '9rem' }} />
                    <Column header={t('dashboard.what')} body={(r) => (
                        <span>{r.operacao || r.tipo} {r.idRegistro ? `#${r.idRegistro}` : ''}</span>
                    )} />
                    <Column field="usuarioId" header={t('dashboard.user')} style={{ width: '7rem' }}
                        body={(r) => (r.usuarioId ? `#${r.usuarioId}` : '—')} />
                    <Column field="ip" header="IP" style={{ width: '10rem' }}
                        body={(r) => r.ip || '—'} />
                    <Column field="data" header={t('dashboard.when')} sortable style={{ width: '11rem' }}
                        body={(r) => quando(r.data)} />
                </DataTable>
            </Card>
        </div>
    );
};
