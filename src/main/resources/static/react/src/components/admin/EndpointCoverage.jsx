import { useTranslation } from 'react-i18next';
import React, { useEffect, useMemo, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { apiFetch } from '../../services/ApiConfig';

const UI_HINTS = [
    { prefix: '/api/cadastro/pessoas', route: '/cadastro/pessoas', label: 'Pessoas' },
    { prefix: '/api/cadastro/clientes', route: '/cadastro/clientes', label: 'Clientes' },
    { prefix: '/api/cadastro/fornecedores', route: '/cadastro/fornecedores', label: 'Fornecedores' },
    { prefix: '/api/cadastro/produtos', route: '/cadastro/produtos', label: 'Produtos' },
    { prefix: '/api/fiscal/ncm', route: '/fiscal/ncm', label: 'NCM' },
    { prefix: '/api/fiscal/cfop', route: '/fiscal/cfop', label: 'CFOP' },
    { prefix: '/api/fiscal/cest', route: '/fiscal/cest', label: 'CEST' },
    { prefix: '/api/fiscal/issqn', route: '/fiscal/issqn', label: 'ISSQN' },
    { prefix: '/api/fiscal/impostos', route: '/fiscal/impostos', label: 'Impostos' },
    { prefix: '/api/fiscal/entradas', route: '/fiscal/entradas', label: 'Entradas de NF' },
    { prefix: '/api/fiscal/sefaz', route: '/fiscal/sefaz', label: 'Consulta SEFAZ' },
    { prefix: '/api/financeiro/comissoes', route: '/financeiro/comissoes', label: 'Comissões' },
    { prefix: '/api/financeiro/caixas', route: '/financeiro/caixa', label: 'Caixa' },
    { prefix: '/api/bi/relatorios', route: '/bi/relatorios', label: 'Relatórios BI' },
    { prefix: '/api/rh/cargos', route: '/rh/cargos', label: 'Cargos' },
    { prefix: '/api/rh/folhas', route: '/rh/folha', label: 'Folha de Pagamento' }
];

const METHOD_SEVERITY = { GET: 'info', POST: 'success', PUT: 'warning', PATCH: 'warning', DELETE: 'danger' };
const normalizePath = (path) => path.replace(/\{[^}]+\}/g, '{id}');
const resolveHint = (path) => {
    const normalized = normalizePath(path);
    return UI_HINTS.find(h => normalized === h.prefix || normalized.startsWith(h.prefix + '/')) || null;
};

export default function EndpointCoverage() {
    const { t } = useTranslation();
    const [document, setDocument] = useState(null);
    const [loading, setLoading] = useState(true);
    const [erro, setErro] = useState('');
    const [busca, setBusca] = useState('');
    const [metodo, setMetodo] = useState('TODOS');
    const [vinculo, setVinculo] = useState('TODOS');

    useEffect(() => {
        let ativo = true;
        (async () => {
            try {
                const response = await apiFetch('/v3/api-docs');
                if (!response.ok) throw new Error(t('legacyUi.endpoint.error'));
                const json = await response.json();
                if (ativo) setDocument(json);
            } catch (e) {
                if (ativo) setErro(e.message || 'Falha ao carregar a API.');
            } finally {
                if (ativo) setLoading(false);
            }
        })();
        return () => { ativo = false; };
    }, []);

    const endpoints = useMemo(() => {
        const paths = document?.paths || {};
        return Object.entries(paths).flatMap(([path, operations]) =>
            Object.entries(operations)
                .filter(([method]) => ['get', 'post', 'put', 'patch', 'delete'].includes(method.toLowerCase()))
                .map(([method, operation]) => {
                    const hint = resolveHint(path);
                    return {
                        id: method + ':' + path, method: method.toUpperCase(), path,
                        summary: operation?.summary || operation?.description || 'Sem descrição',
                        tag: operation?.tags?.[0] || 'Sem módulo', hint,
                        status: hint ? 'COM TELA' : 'SEM VÍNCULO DE TELA'
                    };
                })
        );
    }, [document]);

    const filtered = useMemo(() => endpoints.filter(e => {
        const texto = [e.path, e.summary, e.tag, e.hint?.label || ''].join(' ').toLowerCase();
        return (!busca || texto.includes(busca.toLowerCase()))
            && (metodo === 'TODOS' || e.method === metodo)
            && (vinculo === 'TODOS' || (vinculo === 'COM TELA' && e.hint) || (vinculo === 'SEM TELA' && !e.hint));
    }), [endpoints, busca, metodo, vinculo]);

    const methods = useMemo(() => ['TODOS', ...new Set(endpoints.map(e => e.method))], [endpoints]);
    const comTela = endpoints.filter(e => e.hint).length;
    const semTela = endpoints.length - comTela;
    const statusTemplate = row => <Tag value={row.status} severity={row.hint ? 'success' : 'warning'} />;
    const methodTemplate = row => <Tag value={row.method} severity={METHOD_SEVERITY[row.method] || 'info'} />;
    const telaTemplate = row => row.hint
        ? <span><strong>{row.hint.label}</strong><br /><small>{row.hint.route}</small></span>
        : <span className='bc-muted'>Backlog: decidir tela/ação ou classificar como técnico/integração</span>;

    return (
        <div className='p-3'>
            <Card title={t('legacyUi.endpoint.title')}>
                <p className='mt-0 mb-1'>Inventário carregado do OpenAPI do próprio ERP. O catálogo transforma endpoints sem vínculo de tela em backlog rastreável.</p>
                <small className='bc-muted'>Sem vínculo de tela não significa endpoint quebrado: integrações, callbacks, autenticação e operações internas podem legitimamente não ter tela própria.</small>
                {erro && <Message severity='error' text={erro} className='w-full mb-3' />}
                <div className='grid mt-3'>
                    <div className='col-12 md:col-4'><Card className='h-full'><small>{t('legacyUi.endpoint.published')}</small><div className='text-3xl font-bold mt-2'>{endpoints.length}</div></Card></div>
                    <div className='col-12 md:col-4'><Card className='h-full'><small>{t('legacyUi.endpoint.linked')}</small><div className='text-3xl font-bold mt-2'>{comTela}</div></Card></div>
                    <div className='col-12 md:col-4'><Card className='h-full'><small>{t('legacyUi.endpoint.unlinked')}</small><div className='text-3xl font-bold mt-2'>{semTela}</div></Card></div>
                </div>
                <div className='bc-action-bar mt-4 mb-3'>
                    <div className='bc-action-group' style={{ justifyContent: 'flex-start', flex: 1 }}>
                        <span className='p-input-icon-left' style={{ minWidth: 'min(30rem, 100%)' }}><i className='pi pi-search' />
                            <InputText value={busca} onChange={e => setBusca(e.target.value)} placeholder={t('legacyUi.endpoint.search')} className='w-full' />
                        </span>
                        <Dropdown value={metodo} options={methods.map(m => ({ label: m, value: m }))} onChange={e => setMetodo(e.value)} placeholder={t('legacyUi.endpoint.method')} />
                        <Dropdown value={vinculo} options={[{ label: 'Todos', value: 'TODOS' }, { label: 'Com tela', value: 'COM TELA' }, { label: 'Sem tela', value: 'SEM TELA' }]} onChange={e => setVinculo(e.value)} placeholder={t('legacyUi.endpoint.coverage')} />
                    </div>
                    <Button label={t('legacyUi.endpoint.swagger')} icon='pi pi-external-link' outlined onClick={() => window.open('/swagger-ui.html', '_blank', 'noopener,noreferrer')} />
                </div>
                <DataTable value={filtered} loading={loading} paginator rows={15} rowsPerPageOptions={[15, 30, 60]} dataKey='id' responsiveLayout='scroll' stripedRows emptyMessage='Nenhum endpoint corresponde aos filtros.'>
                    <Column field='method' header='Método' body={methodTemplate} style={{ width: '8rem' }} />
                    <Column field='path' header='Endpoint' style={{ minWidth: '26rem' }} />
                    <Column field='tag' header='Módulo' style={{ width: '12rem' }} />
                    <Column field='summary' header='Operação' style={{ minWidth: '24rem' }} />
                    <Column header='Vínculo de tela' body={statusTemplate} style={{ width: '13rem' }} />
                    <Column header='Tela / próximo passo' body={telaTemplate} style={{ minWidth: '22rem' }} />
                </DataTable>
            </Card>
        </div>
    );
}
