import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { apiFetch, desembrulharLista } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { TabPanel, TabView } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/financeiro/boletos';

// A lista de bancos vem do catalogo oficial (513 instituicoes, V96).
// Antes era uma constante escrita a mao aqui, com 7 entradas e um erro:
// Sicredi e Sicoob estavam com o mesmo codigo COMPE (756 e o do Sicoob; o
// Sicredi e o 748). Codigo errado no boleto manda o pagamento para o banco
// errado, e o sistema aceitava qualquer numero sem conferir se existe.
const rotuloBanco = (b) => `${b.nomeCurto || b.nome} (${b.compe})`;

const money = (v) => `R$ ${Number(v ?? 0).toFixed(2)}`;
const dataBr = (v) => (v ? new Date(v).toLocaleDateString('pt-BR') : '—');

/**
 * Boleto.
 *
 * O backend estava pronto e completo (10 endpoints: emitir, nosso numero,
 * validar, arquivo, remessas e retornos) e nao havia tela, rota nem item de
 * menu — a capacidade existia e nao tinha por onde ser acessada.
 *
 * A emissao depende do microsservico boleto-cnab-api. Se ele estiver fora,
 * o backend responde 503 com CNAB_INDISPONIVEL, e a tela diz isso explicitamente
 * em vez de mostrar um erro generico.
 */
export const Boletos = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const navigate = useNavigate();

    const [aba, setAba] = useState(0);
    const [boletos, setBoletos] = useState([]);
    const [remessas, setRemessas] = useState([]);
    const [retornos, setRetornos] = useState([]);
    const [servico, setServico] = useState(null);
    const [bancos, setBancos] = useState([]);
    // o Dropdown filtra sobre esta copia; a lista original fica intacta para
    // a proxima busca mostrar tudo de novo
    const [bancosFiltrados, setBancosFiltrados] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [emitindo, setEmitindo] = useState(false);
    const [dialogo, setDialogo] = useState(false);

    const [form, setForm] = useState({
        tituloId: null, bank: '001', formato: 'pdf',
        nossoNumero: '', valor: 0, vencimento: '', pagador: '',
        documentoPagador: '', enderecoPagador: ''
    });

    // Mesmo corpo das tres rotas: validar, nosso-numero e emitir recebem os
    // dados do sacado no corpo e o banco na query. Dividar aqui evita que uma
    // das tres mande um campo a menos e a remessa saia rejeitada.
    const corpoDoSacado = () => ({
        nossoNumero: form.nossoNumero || undefined,
        valor: Number(form.valor),
        vencimento: form.vencimento,
        pagador: { nome: form.pagador, documento: form.documentoPagador || undefined },
        enderecoPagador: form.enderecoPagador || undefined
    });

    /**
     * Detalhes do banco selecionado.
     *
     * O dropdown mostra nome e código, e nao dá para conferir se o banco
     * escolhido é o que emite para a empresa. A rota por código existe para
     * isso e estava sem uso.
     */
    const [bancoDetalhe, setBancoDetalhe] = useState(null);
    useEffect(() => {
        if (!form.bank) { setBancoDetalhe(null); return undefined; }
        let cancelado = false;
        apiFetch(`/api/core/bancos/${encodeURIComponent(form.bank)}`)
            .then((r) => (r.ok ? r.json() : null))
            .then((d) => { if (!cancelado) setBancoDetalhe(d?.data ?? d ?? null); })
            .catch(() => { if (!cancelado) setBancoDetalhe(null); });
        return () => { cancelado = true; };
    }, [form.bank]);

    const erro = (e, padrao) =>
        e?.message ? String(e.message).replace(/^.*?"message":"/, '').split('"')[0] : padrao;

    const carregar = useCallback(async () => {
        setCarregando(true);
        try {
            const [b, r, t, s] = await Promise.all([
                apiFetch(`${BASE}`),
                apiFetch(`${BASE}/remessas`),
                apiFetch(`${BASE}/retornos`),
                apiFetch(`${BASE}/servico`)
            ]);
            setBoletos(desembrulharLista(await b.json()).lista);
            setRemessas(desembrulharLista(await r.json()).lista);
            setRetornos(desembrulharLista(await t.json()).lista);
            setServico(s.ok ? await s.json() : null);

            const rb = await apiFetch('/api/core/bancos');
            const lista = rb.ok ? await rb.json() : [];
            setBancos(lista);
            setBancosFiltrados(lista);
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Erro',
                detail: erro(e, t('errors.load')), life: 5000
            });
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    const semCamposObrigatorios = useMemo(
        () => !form.pagador?.trim() || !form.vencimento || !(Number(form.valor) > 0),
        [form]);

    /**
     * Confere o sacado com o banco emissor antes de gerar.
     *
     * Sem isto a remessa vai inteira e volta com o retorno apontando problema em
     * um item especifico — depois de todo o trabalho. Validando antes, o usuário
     * corrige aqui. O 503 (CNAB fora) nao bloqueia: a validação é só um
     *planejamento, o banco pode subir entre a checagem e a emissão.
     */
    const validar = async () => {
        if (semCamposObrigatorios) {
            toast.current?.show({
                severity: 'warn', summary: t('common.warning'),
                detail: t('legacyUi.boletos.warning'), life: 4000
            });
            return null;
        }
        try {
            const r = await apiFetch(`${BASE}/validar?bank=${encodeURIComponent(form.bank)}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpoDoSacado())
            });
            const j = await r.json().catch(() => null);
            if (r.status === 503) {
                toast.current?.show({
                    severity: 'info', summary: 'Validação indisponível',
                    detail: 'O serviço do banco não está no ar. Você pode emitir mesmo assim.', life: 5000
                });
                return null;
            }
            if (!r.ok) {
                throw new Error(j?.errors?.[0]?.message || j?.message || 'Falha ao validar');
            }
            const valido = (j?.data)?.valido !== false;
            toast.current?.show({
                severity: valido ? 'success' : 'error',
                summary: valido ? t('legacyUi.boletos.validateOk') : t('legacyUi.boletos.validateFail'),
                detail: valido ? 'O banco aceita estes dados' : t('legacyUi.boletos.validateFailDetail'),
                life: 5000
            });
            return valido;
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Falha ao validar',
                detail: erro(e, 'Não foi possível validar'), life: 6000
            });
            return null;
        }
    };

    const gerarNossoNumero = async () => {
        if (semCamposObrigatorios) {
            toast.current?.show({
                severity: 'warn', summary: t('common.warning'),
                detail: t('legacyUi.boletos.warning'), life: 4000
            });
            return;
        }
        try {
            const r = await apiFetch(`${BASE}/nosso-numero?bank=${encodeURIComponent(form.bank)}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpoDoSacado())
            });
            const j = await r.json().catch(() => null);
            if (r.status === 503) {
                toast.current?.show({
                    severity: 'info', summary: 'Serviço do banco indisponível',
                    detail: 'Não foi possível sortear o nosso número agora.', life: 5000
                });
                return;
            }
            if (!r.ok) throw new Error(j?.errors?.[0]?.message || j?.message || 'Falha ao gerar');
            const nn = (j?.data)?.nosso_numero;
            if (!nn) {
                toast.current?.show({
                    severity: 'warn', summary: 'Sem nosso número',
                    detail: 'O banco não devolveu um número — deixe em branco para o ERP usar o título.', life: 5000
                });
                return;
            }
            setForm((f) => ({ ...f, nossoNumero: String(nn) }));
            toast.current?.show({
                severity: 'success', summary: 'Nosso número gerado', life: 3000
            });
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Falha ao gerar',
                detail: erro(e, 'Não foi possível gerar o nosso número'), life: 6000
            });
        }
    };

    const emitir = async () => {
        if (semCamposObrigatorios) {
            toast.current?.show({
                severity: 'warn', summary: t('common.warning'),
                detail: t('legacyUi.boletos.warning'), life: 4000
            });
            return;
        }
        setEmitindo(true);
        try {
            // valida primeiro: recusado aqui e muito mais barato do que uma
            // remessa inteira devolvida
            const ok = await validar();
            if (ok === false) {
                setEmitindo(false);
                return;
            }

            // Query com tituloId opcional: os demais dados vao no corpo.
            const qs = new URLSearchParams({ bank: form.bank, formato: form.formato });
            if (form.tituloId) qs.set('tituloId', String(form.tituloId));

            const resposta = await apiFetch(`${BASE}/emitir?${qs}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpoDoSacado())
            });

            const json = await resposta.json();
            if (!resposta.ok) {
                const mensagem = json?.errors?.[0]?.message
                    || json?.message || t('legacyUi.boletos.emitError');
                throw new Error(mensagem);
            }

            toast.current?.show({
                severity: 'success', summary: 'Boleto emitido',
                detail: 'Agora está na lista para download', life: 4000
            });
            setDialogo(false);
            carregar();
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Falha ao emitir',
                detail: erro(e, t('legacyUi.boletos.emitError')), life: 6000
            });
        } finally {
            setEmitindo(false);
        }
    };

    const baixarArquivo = async (b) => {
        try {
            const resposta = await apiFetch(`${BASE}/${b.id}/arquivo`);
            if (!resposta.ok) {
                throw new Error('Arquivo ainda não foi gerado para este boleto');
            }
            const blob = await resposta.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `boleto-${b.id}.pdf`;
            a.click();
            URL.revokeObjectURL(url);
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Download', detail: erro(e, 'Falha ao baixar'), life: 5000
            });
        }
    };

    const cnabFora = servico && servico.disponivel === false;

    return (
        <div className="p-4">
            <Toast ref={toast} />

            <div className="flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Boletos</h2>
                    <span className="bc-muted">Emissão, remessa e retorno bancário</span>
                </div>
                <Button
                    label={t('legacyUi.boletos.issue')}
                    icon="pi pi-plus"
                    onClick={() => setDialogo(true)}
                    disabled={cnabFora}
                    tooltip={cnabFora ? t('legacyUi.boletos.unavailable') : undefined}
                />
            </div>

            {cnabFora && (
                <Message severity="warn" className="mb-3" text={`${t('legacyUi.boletos.unavailable')} em ${servico.url}. A emissão fica bloqueada até o serviço voltar.`} />
            )}

            <TabView activeIndex={aba} onTabChange={(e) => setAba(e.index)}>
                <TabPanel header={`Emitidos (${boletos.length})`} leftIcon="pi pi-file">
                    <DataTable
                        value={boletos}
                        loading={carregando}
                        dataKey="id"
                        paginator rows={10}
                        emptyMessage={t('legacyUi.boletos.empty')}
                        responsiveLayout="scroll"
                    >
                        <Column field="id" header="#" style={{ width: '4rem' }} />
                        <Column field="numero" header="Número" />
                        <Column field="nossoNumero" header="Nosso número" />
                        <Column field="valor" header="Valor" body={(b) => money(b.valor)} />
                        <Column field="vencimento" header="Vencimento" body={(b) => dataBr(b.vencimento)} />
                        <Column field="status" header="Status" body={(b) => (
                            <Tag severity={b.status === 'REGISTRADO' ? 'success' : 'info'}
                                 value={b.status ?? '—'} />
                        )} />
                        <Column header="" body={(b) => (
                            <Button icon="pi pi-download" rounded text tooltip={t('legacyUi.boletos.download')}
                                    onClick={() => baixarArquivo(b)} />
                        )} style={{ width: '4rem' }} />
                    </DataTable>
                </TabPanel>

                <TabPanel header={`Remessas (${remessas.length})`} leftIcon="pi pi-upload">
                    <DataTable
                        value={remessas}
                        loading={carregando}
                        dataKey="id"
                        paginator rows={10}
                        emptyMessage={t('legacyUi.boletos.noRemittances')}
                        responsiveLayout="scroll"
                    >
                        <Column field="id" header="#" style={{ width: '4rem' }} />
                        <Column field="arquivo" header="Arquivo" />
                        <Column field="quantidade" header="Boletos" />
                        <Column field="dataEnvio" header="Envio" body={(r) => dataBr(r.dataEnvio)} />
                        <Column field="status" header="Status" body={(r) => (
                            <Tag severity={r.status === 'ENVIADA' ? 'success' : 'warning'} value={r.status ?? '—'} />
                        )} />
                    </DataTable>
                </TabPanel>

                <TabPanel header={`Retornos (${retornos.length})`} leftIcon="pi pi-download">
                    <DataTable
                        value={retornos}
                        loading={carregando}
                        dataKey="id"
                        paginator rows={10}
                        emptyMessage="Nenhum retorno processado."
                        responsiveLayout="scroll"
                    >
                        <Column field="id" header="#" style={{ width: '4rem' }} />
                        <Column field="nossoNumero" header="Nosso número" />
                        <Column field="status" header="Situação" />
                        <Column field="dataRetorno" header="Processado em" body={(r) => dataBr(r.dataRetorno)} />
                    </DataTable>
                </TabPanel>
            </TabView>

            <Dialog
                visible={dialogo}
                onHide={() => setDialogo(false)}
                header={t('legacyUi.boletos.issue')}
                modal
                style={{ width: 'min(96vw, 640px)' }}
                breakpoints={{ '960px': '95vw' }}
                footer={
                    <div className="flex justify-content-between gap-2 flex-wrap">
                        <div className="flex gap-2">
                            <Button label="Validar" icon="pi pi-verified" outlined
                                    disabled={emitindo} onClick={validar} />
                            <Button label="Gerar nosso número" icon="pi pi-hashtag" outlined
                                    disabled={emitindo} onClick={gerarNossoNumero} />
                        </div>
                        <div className="flex gap-2">
                            <Button label="Cancelar" severity="secondary" text
                                    onClick={() => setDialogo(false)} />
                            <Button label="Emitir" icon="pi pi-check" loading={emitindo}
                                    disabled={emitindo} onClick={emitir} />
                        </div>
                    </div>
                }
            >
                <div className="grid p-fluid">
                    <div className="col-12 md:col-6">
                        <label className="bc-label" htmlFor="bk">Banco</label>
                        <Dropdown id="bk" value={form.bank}
                                  options={bancosFiltrados.map((b) => ({ label: rotuloBanco(b), value: b.compe }))}
                                  filter onFilter={(e) => {
                                      const t = e.filter.toLowerCase();
                                      // busca por nome OU pelo proprio codigo
                                      setBancosFiltrados(bancos.filter(
                                          (b) => rotuloBanco(b).toLowerCase().includes(t)));
                                  }}
                                  onChange={(e) => setForm({ ...form, bank: e.value })} />
                    </div>
                    {bancoDetalhe && (
                        <div className="col-12">
                            <Message severity="info"
                                text={`${bancoDetalhe.nome || bancoDetalhe.nomeCurto || 'Banco'} · código ${bancoDetalhe.compe}`
                                    + (bancoDetalhe.ispb ? ` · ISPB ${bancoDetalhe.ispb}` : '')} />
                        </div>
                    )}
                    <div className="col-12 md:col-6">
                        <label className="bc-label" htmlFor="tt">Título (opcional)</label>
                        <InputNumber id="tt" value={form.tituloId} useGrouping={false}
                                     placeholder="id do título"
                                     onValueChange={(e) => setForm({ ...form, tituloId: e.value })} />
                    </div>
                    <div className="col-12 md:col-6">
                        <label className="bc-label" htmlFor="nn">Nosso número</label>
                        <InputText id="nn" value={form.nossoNumero}
                                   onChange={(e) => setForm({ ...form, nossoNumero: e.target.value })} />
                    </div>
                    <div className="col-12 md:col-6">
                        <label className="bc-label" htmlFor="vl">Valor</label>
                        <InputNumber id="vl" value={form.valor} mode="currency" currency="BRL" locale="pt-BR"
                                     onValueChange={(e) => setForm({ ...form, valor: e.value ?? 0 })} />
                    </div>
                    <div className="col-12">
                        <label className="bc-label" htmlFor="vc">Vencimento</label>
                        <InputText id="vc" type="date" value={form.vencimento}
                                   onChange={(e) => setForm({ ...form, vencimento: e.target.value })} />
                    </div>
                    <div className="col-12">
                        <label className="bc-label" htmlFor="pg">Pagador</label>
                        <InputText id="pg" value={form.pagador}
                                   onChange={(e) => setForm({ ...form, pagador: e.target.value })} />
                    </div>
                    <div className="col-12">
                        <label className="bc-label" htmlFor="dp">CPF/CNPJ do pagador</label>
                        <InputText id="dp" value={form.documentoPagador}
                                   onChange={(e) => setForm({ ...form, documentoPagador: e.target.value })} />
                    </div>
                    {semCamposObrigatorios && (
                        <div className="col-12">
                            <Message severity="info" text="Pagador, vencimento e valor são obrigatórios." />
                        </div>
                    )}
                </div>
            </Dialog>
        </div>
    );
};

export default Boletos;
