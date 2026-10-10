import { useTranslation } from 'react-i18next';
import { localeAtivo } from '../shared/LocaleData.js';
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Dialog } from 'primereact/dialog';
import { ImagensProduto } from '../shared/ImagensProduto';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { ProgressBar } from 'primereact/progressbar';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';
import { PrintButton } from '../shared/PrintButton';

const vazio = {
    id: null,
    codigo: '',
    nome: '',
    descricao: '',
    urlProduto: '',
    categoriaId: null,
    marcaId: null,
    unidadeMedidaId: null,
    ncm: '',
    cfopPadrao: '',
    cest: '',
    codigoBarras: '',
    precoCusto: 0,
    precoVenda: 0,
    estoqueMinimo: 0,
    estoqueMaximo: 0,
    peso: null,
    tipo: 'PRODUTO',
    ativo: true
};

const formatarMoeda = (v) =>
    v === null || v === undefined
        ? '-'
        : Number(v).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });

export const CadastroProdutos = () => {
    const { t } = useTranslation();
    const TIPOS = [
        { label: t('productScreen.typeProduct'), value: 'PRODUTO' },
        { label: t('productScreen.typeService'), value: 'SERVICO' },
        { label: t('productScreen.typeKit'), value: 'KIT' }
    ];
    const STATUS = [
        { label: t('productScreen.active'), value: true },
        { label: t('productScreen.inactive'), value: false }
    ];
    const { user } = useAuth();
    const toast = useRef(null);
    const tabelaRef = useRef(null);

    const [produtos, setProdutos] = useState([]);
    const [totalRecords, setTotalRecords] = useState(0);
    const [loading, setLoading] = useState(false);
    const [salvando, setSalvando] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [form, setForm] = useState(vazio);
    const [erro, setErro] = useState('');
    const [sucesso, setSucesso] = useState(false);

    const [filtros, setFiltros] = useState({ nome: '', codigo: '', ativo: null });
    const [buscaTexto, setBuscaTexto] = useState('');

    const [categorias, setCategorias] = useState([]);
    const [marcas, setMarcas] = useState([]);
    const [unidades, setUnidades] = useState([]);

    const [lazyParams, setLazyParams] = useState({
        page: 0, rows: 10, sortField: 'nome', sortOrder: 1
    });

    // ---- carregamento de listas auxiliares (para os dropdowns do formulario)
    useEffect(() => {
        const empresaId = user?.empresaId ?? '';
        Promise.all([
            apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/categorias?size=500`).then((r) => (r.ok ? r.json() : [])),
            apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/marcas?size=500`).then((r) => (r.ok ? r.json() : [])),
            apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/unidades-medida?size=500`).then((r) => (r.ok ? r.json() : []))
        ]).then(([cats, marcasRes, unis]) => {
            setCategorias(desembrulhar(cats));
            setMarcas(desembrulhar(marcasRes));
            setUnidades(desembrulhar(unis));
        }).catch((e) => console.error('Falha ao carregar listas auxiliares', e));
    }, [user?.empresaId]);

    const fetchProdutos = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: lazyParams.page,
                size: lazyParams.rows,
                sort: lazyParams.sortField || 'nome',
                direction: lazyParams.sortOrder === 1 ? 'asc' : 'desc'
            });
            if (filtros.nome) params.append('nome', filtros.nome);
            if (filtros.codigo) params.append('codigo', filtros.codigo);
            if (filtros.ativo !== null) params.append('ativo', filtros.ativo);

            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/cadastro/produtos?${params}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);

            const json = await response.json();
            const conteudo = json?.data?.content ?? json?.content ?? json?.data ?? [];

            setProdutos(Array.isArray(conteudo) ? conteudo : []);
            setTotalRecords(
                json?.data?.totalElements ?? json?.totalElements ?? (Array.isArray(conteudo) ? conteudo.length : 0)
            );
        } catch (err) {
            console.error('Erro ao carregar produtos', err);
            toast.current?.show({
                severity: 'error', summary: t('common.error'),
                detail: t('errors.load'), life: 3000
            });
        } finally {
            setLoading(false);
        }
    }, [lazyParams, filtros]);

    useEffect(() => { fetchProdutos(); }, [fetchProdutos]);

    const onLazyLoad = (event) => {
        setLazyParams({
            page: event.page ?? 0,
            rows: event.rows ?? 10,
            sortField: event.sortField || 'nome',
            sortOrder: event.sortOrder ?? 1
        });
    };

    const abrirDialog = (produto = null) => {
        setErro('');
        setSucesso(false);
        setForm(produto ? { ...vazio, ...produto } : vazio);
        setDialogVisible(true);
    };

    const salvar = async () => {
        setSalvando(true);
        setErro('');
        setSucesso(false);
        try {
            const payload = {
                codigo: form.codigo?.trim(),
                nome: form.nome?.trim(),
                descricao: form.descricao?.trim() || null,
                urlProduto: form.urlProduto?.trim() || null,
                categoriaId: form.categoriaId ?? null,
                marcaId: form.marcaId ?? null,
                unidadeMedidaId: form.unidadeMedidaId ?? null,
                ncm: form.ncm?.trim() || null,
                cfopPadrao: form.cfopPadrao?.trim() || null,
                cest: form.cest?.trim() || null,
                codigoBarras: form.codigoBarras?.trim() || null,
                precoCusto: form.precoCusto ?? 0,
                precoVenda: form.precoVenda ?? 0,
                estoqueMinimo: form.estoqueMinimo ?? 0,
                estoqueMaximo: form.estoqueMaximo ?? 0,
                peso: form.peso ?? null,
                tipo: form.tipo || 'PRODUTO',
                ativo: form.ativo !== false,
                variacoes: [],
                itensKit: []
            };

            const metodo = form.id ? 'PUT' : 'POST';
            const url = form.id
                ? `${ApiConfig.BASE_URL}/api/cadastro/produtos/${form.id}`
                : `${ApiConfig.BASE_URL}/api/cadastro/produtos`;

            const response = await apiFetch(url, {
                method: metodo,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                setErro(await extrairErro(response, t('productScreen.saveError')));
                return;
            }

            setSucesso(true);
            toast.current?.show({
                severity: 'success', summary: t('productScreen.success'),
                detail: t('productScreen.saveDetail', { state: form.id ? t('productScreen.updated') : t('productScreen.created') }), life: 3000
            });
            setTimeout(() => {
                setDialogVisible(false);
                fetchProdutos();
            }, 600);
        } catch (err) {
            setErro(err.message || t('productScreen.connectionError'));
        } finally {
            setSalvando(false);
        }
    };

    const excluir = (produto) => {
        confirmDialog({
            message: t('productScreen.confirmDelete', { name: produto.nome }),
            header: t('productScreen.confirmDeleteTitle'),
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: t('productScreen.delete'),
            rejectLabel: t('common.cancel'),
            acceptClassName: 'p-button-danger',
            accept: async () => {
                try {
                    const response = await apiFetch(
                        `${ApiConfig.BASE_URL}/api/cadastro/produtos/${produto.id}`,
                        { method: 'DELETE' }
                    );
                    if (!response.ok) throw new Error(await extrairErro(response, t('productScreen.deleteError')));
                    toast.current?.show({
                        severity: 'success', summary: t('productScreen.success'),
                        detail: t('productScreen.deleted'), life: 3000
                    });
                    fetchProdutos();
                } catch (err) {
                    toast.current?.show({
                        severity: 'error', summary: 'Erro',
                        detail: err.message, life: 4000
                    });
                }
            }
        });
    };

    const toggleAtivo = async (produto) => {
        try {
            const response = await apiFetch(
                `${ApiConfig.BASE_URL}/api/cadastro/produtos/${produto.id}`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ...produto, ativo: !produto.ativo })
                }
            );
            if (!response.ok) throw new Error(await extrairErro(response, t('productScreen.statusError')));
            fetchProdutos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: t('common.error'), detail: err.message, life: 4000 });
        }
    };

    // ---- filtros
    const aplicarBusca = () => {
        const termo = buscaTexto.trim();
        setFiltros((f) => ({ ...f, nome: termo, codigo: termo }));
        setLazyParams((p) => ({ ...p, page: 0 }));
    };

    const limparFiltros = () => {
        setBuscaTexto('');
        setFiltros({ nome: '', codigo: '', ativo: null });
        setLazyParams((p) => ({ ...p, page: 0 }));
    };

    // ---- colunas
    const precoTemplate = (row) => <span className="font-bold">{formatarMoeda(row.precoVenda)}</span>;
    const margemTemplate = (row) => {
        const margem = calcularMargem(row.precoCusto, row.precoVenda);
        const cor = margem === null ? 'danger' : margem >= 30 ? 'success' : 'warning';
        return <Tag value={margem === null ? '-' : `${margem.toFixed(1)}%`} severity={cor} />;
    };
    const statusTemplate = (row) => (
        <Tag value={row.ativo ? t('productScreen.active') : t('productScreen.inactive')} severity={row.ativo ? 'success' : 'danger'} />
    );
    const imagensTemplate = (row) => {
        const imagens = Array.isArray(row.imagens) ? row.imagens : [];
        if (!imagens.length) return <span className="text-muted">-</span>;
        return (
            <img
                src={imagens[0]}
                alt={row.nome}
                style={{ width: '40px', height: '40px', objectFit: 'cover', borderRadius: '4px' }}
            />
        );
    };
    const acoesTemplate = (row) => (
        <div className="flex gap-1">
            <Button
                icon="pi pi-pencil" className="p-button-info p-button-sm p-button-text"
                onClick={() => abrirDialog(row)} tooltip={t('productScreen.edit')}
            />
            <Button
                icon={row.ativo ? 'pi pi-eye-slash' : 'pi pi-eye'}
                className="p-button-warning p-button-sm p-button-text"
                onClick={() => toggleAtivo(row)}
                tooltip={row.ativo ? t('productScreen.deactivate') : t('productScreen.activate')}
            />
            <Button
                icon="pi pi-trash" className="p-button-danger p-button-sm p-button-text"
                onClick={() => excluir(row)} tooltip={t('productScreen.delete')}
            />
        </div>
    );

    return (
        <div className="produtos-card">
            <Toast ref={toast} />
            <ConfirmDialog />

            <Card title={t('productScreen.title')} className="mb-4">
                <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                    <div className="flex gap-2 align-items-center flex-wrap">
                        <span className="p-input-icon-left">
                            <i className="pi pi-search" />
                            <InputText
                                value={buscaTexto}
                                onChange={(e) => setBuscaTexto(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && aplicarBusca()}
                                placeholder={t('productScreen.search')}
                            />
                        </span>
                        <Button label={t('common.search')} icon="pi pi-search" onClick={aplicarBusca} className="p-button-outlined" />
                        <Button label={t('productScreen.clear')} icon="pi pi-filter-slash" onClick={limparFiltros} className="p-button-text" />
                    </div>
                    <div className="flex gap-2">
                        <PrintButton alvoRef={tabelaRef} titulo={t('productScreen.title')} />
                        <Button
                            label={t('productScreen.new')} icon="pi pi-plus"
                            className="p-button-success" onClick={() => abrirDialog()}
                        />
                    </div>
                </div>

                <div ref={tabelaRef}>
                    <DataTable
                        value={produtos}
                        loading={loading}
                        dataKey="id"
                        paginator
                        lazy
                        rows={lazyParams.rows}
                        first={lazyParams.page * lazyParams.rows}
                        totalRecords={totalRecords}
                        onPage={onLazyLoad}
                        onSort={onLazyLoad}
                        sortField={lazyParams.sortField}
                        sortOrder={lazyParams.sortOrder}
                        rowsPerPageOptions={[10, 20, 50, 100]}
                        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        currentPageReportTemplate={t('productScreen.report')}
                        responsiveLayout="scroll"
                        className="p-datatable-sm"
                        emptyMessage={t('productScreen.empty')}
                    >
                        <Column header="" body={imagensTemplate} style={{ width: '60px' }} />
                        <Column field="codigo" header={t('productScreen.code')} sortable style={{ width: '110px' }} />
                        <Column field="nome" header={t('productScreen.name')} sortable style={{ minWidth: '220px' }} />
                        <Column field="categoriaNome" header={t('productScreen.category')} style={{ width: '140px' }} />
                        <Column field="marcaNome" header={t('productScreen.brand')} style={{ width: '120px' }} />
                        <Column field="unidadeSigla" header={t('productScreen.unit')} style={{ width: '70px' }} />
                        <Column field="precoCusto" header={t('productScreen.cost')} body={(r) => formatarMoeda(r.precoCusto)} style={{ width: '110px' }} />
                        <Column field="precoVenda" header={t('productScreen.sale')} body={precoTemplate} sortable style={{ width: '110px' }} />
                        <Column header={t('productScreen.margin')} body={margemTemplate} style={{ width: '90px' }} />
                        <Column field="ativo" header={t('common.status')} body={statusTemplate} sortable style={{ width: '100px' }} />
                        <Column body={acoesTemplate} header={t('productScreen.actions')} style={{ width: '150px' }} />
                    </DataTable>
                </div>
            </Card>

            <Dialog
                header={form.id ? t('productScreen.editTitle', { name: form.nome }) : t('productScreen.newTitle')}
                visible={dialogVisible}
                onHide={() => setDialogVisible(false)}
                style={{ width: '760px' }}
                maximizable
                modal
                footer={
                    <div>
                        <Button
                            label={t('common.cancel')} icon="pi pi-times"
                            className="p-button-text" onClick={() => setDialogVisible(false)}
                        />
                        <Button
                            label={t('common.save')} icon="pi pi-save"
                            className="p-button-success" onClick={salvar} loading={salvando}
                        />
                    </div>
                }
            >
                <div className="p-fluid">
                {/* Imagens do produto. O binario vai para o MongoDB e o
                    Postgres guarda so a referencia — mesmo desenho do NFS-e. */}
                <div className="col-12 mt-3">
                    <label className="bc-label">{t('productScreen.images')}</label>
                    <ImagensProduto produtoId={form.id} />
                </div>


                    {erro && <Message severity="error" text={erro} className="w-full mb-3" />}
                    {sucesso && <Message severity="success" text={t('productScreen.saved')} className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('productScreen.code')} *</label>
                            <InputText value={form.codigo} maxLength={30}
                                onChange={(e) => setForm({ ...form, codigo: e.target.value })} />
                        </div>
                        <div className="col-12 md:col-8 field">
                            <label className="font-bold mb-2 block">{t('productScreen.name')} *</label>
                            <InputText value={form.nome} maxLength={200}
                                onChange={(e) => setForm({ ...form, nome: e.target.value })} />
                        </div>

                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{t('productScreen.description')}</label>
                            <InputTextarea value={form.descricao} rows={2}
                                onChange={(e) => setForm({ ...form, descricao: e.target.value })} />
                        </div>

                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('productScreen.category')}</label>
                            <Dropdown value={form.categoriaId} options={categorias} optionLabel="descricao"
                                optionValue="id" showFilter filter
                                onChange={(e) => setForm({ ...form, categoriaId: e.value })}
                                placeholder={t('productScreen.select')} />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('productScreen.brand')}</label>
                            <Dropdown value={form.marcaId} options={marcas} optionLabel="nome"
                                optionValue="id" showFilter filter
                                onChange={(e) => setForm({ ...form, marcaId: e.value })}
                                placeholder={t('productScreen.select')} />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('productScreen.unit')}</label>
                            <Dropdown value={form.unidadeMedidaId} options={unidades} optionLabel="nome"
                                optionValue="id" showFilter filter
                                onChange={(e) => setForm({ ...form, unidadeMedidaId: e.value })}
                                placeholder={t('productScreen.select')} />
                        </div>

                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('menu.ncm')}</label>
                            <InputText value={form.ncm} maxLength={8}
                                onChange={(e) => setForm({ ...form, ncm: e.target.value })} />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('productScreen.defaultCfop')}</label>
                            <InputText value={form.cfopPadrao} maxLength={4}
                                onChange={(e) => setForm({ ...form, cfopPadrao: e.target.value })} />
                        </div>
                          <div className="col-12 md:col-4 field">
                              <label className="font-bold mb-2 block">{t('menu.cest')}</label>
                              <InputText value={form.cest} maxLength={7}
                                  placeholder={t('productScreen.taxReplacement')}
                                  onChange={(e) => setForm({ ...form, cest: e.target.value })} />
                          </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">{t('productScreen.barcode')}</label>
                            <InputText value={form.codigoBarras} maxLength={30}
                                onChange={(e) => setForm({ ...form, codigoBarras: e.target.value })} />
                        </div>

                        <div className="col-12 md:col-3 field">
                            <label className="font-bold mb-2 block">{t('productScreen.costPrice')}</label>
                            <InputNumber value={form.precoCusto} mode="currency" locale={localeAtivo()} currency="BRL"
                                onValueChange={(e) => setForm({ ...form, precoCusto: e.value })} />
                        </div>
                        <div className="col-12 md:col-3 field">
                            <label className="font-bold mb-2 block">{t('productScreen.salePrice')}</label>
                            <InputNumber value={form.precoVenda} mode="currency" locale={localeAtivo()} currency="BRL"
                                onValueChange={(e) => setForm({ ...form, precoVenda: e.value })} />
                        </div>
                        <div className="col-12 md:col-2 field">
                            <label className="font-bold mb-2 block">{t('productScreen.minStock')}</label>
                            <InputNumber value={form.estoqueMinimo} locale={localeAtivo()}
                                onValueChange={(e) => setForm({ ...form, estoqueMinimo: e.value })} />
                        </div>
                        <div className="col-12 md:col-2 field">
                            <label className="font-bold mb-2 block">{t('productScreen.maxStock')}</label>
                            <InputNumber value={form.estoqueMaximo} locale={localeAtivo()}
                                onValueChange={(e) => setForm({ ...form, estoqueMaximo: e.value })} />
                        </div>
                        <div className="col-12 md:col-2 field">
                            <label className="font-bold mb-2 block">{t('productScreen.weight')}</label>
                            <InputNumber value={form.peso} locale={localeAtivo()} maxFractionDigits={3}
                                onValueChange={(e) => setForm({ ...form, peso: e.value })} />
                        </div>

                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">{t('productScreen.type')}</label>
                            <Dropdown value={form.tipo} options={TIPOS} optionLabel="label" optionValue="value"
                                onChange={(e) => setForm({ ...form, tipo: e.value })} />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">{t('common.status')}</label>
                            <Dropdown value={form.ativo !== false} options={STATUS} optionLabel="label" optionValue="value"
                                onChange={(e) => setForm({ ...form, ativo: e.value })} />
                        </div>

                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{t('productScreen.url')}</label>
                            <InputText value={form.urlProduto} maxLength={500}
                                onChange={(e) => setForm({ ...form, urlProduto: e.target.value })} />
                        </div>
                    </div>

                    <Divider />
                    <div className="flex justify-content-between align-items-center p-3 bg-gray-100 border-round flex-wrap gap-2">
                        <div>
                            <span className="font-bold" >{t('productScreen.price')}: </span>
                            <span>{formatarMoeda(form.precoVenda)}</span>
                        </div>
                        <div>
                            <span className="font-bold" >{t('productScreen.margin')}: </span>
                            <span>
                                {(() => {
                                    const m = calcularMargem(form.precoCusto, form.precoVenda);
                                    return m === null ? '-' : `${m.toFixed(1)}%`;
                                })()}
                            </span>
                        </div>
                        <div>
                            <span className="font-bold" >{t('common.status')}: </span>
                            <Tag value={form.ativo !== false ? t('productScreen.activeStatus') : t('productScreen.inactiveStatus')}
                                severity={form.ativo !== false ? 'success' : 'danger'} />
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

const desembrulhar = (resposta) => {
    if (Array.isArray(resposta)) return resposta;
    return resposta?.content ?? resposta?.data?.content ?? resposta?.data ?? [];
};

const calcularMargem = (custo, venda) => {
    const c = Number(custo ?? 0);
    const v = Number(venda ?? 0);
    if (!c || v <= 0) return null;
    return ((v - c) / v) * 100;
};

const extrairErro = async (response, padrao) => {
    try {
        const body = await response.text();
        if (!body) return padrao;
        try {
            const json = JSON.parse(body);
            return json.message || json.error || padrao;
        } catch {
            return body.slice(0, 200);
        }
    } catch {
        return padrao;
    }
};

export default CadastroProdutos;
