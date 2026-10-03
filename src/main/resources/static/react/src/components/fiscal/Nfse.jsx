import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/fiscal/nfse';

// Tributação do RPS. T é o padrão de São Paulo: tributado no município de
// ocorrência. F (fora), A (ADI), B (analfabeto), I (isento), P (prefeitura).
const TRIBUTACOES = [
    { label: 'T — Tributado em SP', value: 'T' },
    { label: 'F — Fora do município', value: 'F' },
    { label: 'A —ADI', value: 'A' },
    { label: 'B — Analfabeto', value: 'B' },
    { label: 'I — Isento', value: 'I' },
    { label: 'P — Tributado na prefeitura', value: 'P' }
];

const ISO = (d) => (d ? d.toISOString().slice(0, 10) : null);
const num = (v) => Number(v ?? 0);
const money = (v) => Number(v ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const dataBr = (v) => (v ? new Date(v).toLocaleDateString('pt-BR') : '—');

const erroDe = (e, padrao) => {
    const d = e?.payload;
    if (d?.errors?.[0]?.message) return d.errors[0].message;
    if (d?.message) return d.message;
    return padrao;
};

const listaDe = (j) => {
    const d = j?.data ?? j;
    return Array.isArray(d) ? d : (d?.content ?? []);
};

const vazio = () => ({
    servicoId: null, clienteId: null, cpfCnpjTomador: '', razaoSocialTomador: '',
    emailTomador: '', inscricaoMunicipalTomador: '', valorServicos: 0, valorDeducoes: 0,
    aliquota: 5, discriminacao: '', dataEmissao: null, serieRps: 'RPS',
    numeroRps: null, tributacaoRps: 'T', issRetido: false
});

/**
 * Nota Fiscal de Serviço eletrônica.
 *
 * O backend foi validado em produção — 15 emissões e cancelamentos, XML no
 * MongoDB por 5 anos e PDF por 60 — mas não havia tela, rota nem item de menu:
 * a emissão era possível por curl e por nada mais.
 *
 * A emissão lê o SERVIÇO do cadastro, não o produto. O que define o código da
 * prefeitura é o codigo_tributacao_municipal do serviço; o `01.07` da LC 116
 * passa no XSD e é recusado na tabela municipal. Por isso o formulário mostra o
 * serviço escolhido com o código de tributação ao lado, e avisa quando está
 * vazio — emitir assim só voltaria com recusa da prefeitura.
 */
export const Nfse = () => {
    const { t } = useTranslation();
    const toast = useRef(null);

    const [servicos, setServicos] = useState([]);
    const [clientes, setClientes] = useState([]);
    const [lista, setLista] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [emitindo, setEmitindo] = useState(false);
    const [ocupado, setOcupado] = useState(null);
    const [erro, setErro] = useState('');
    const [dialogo, setDialogo] = useState(false);
    const [form, setForm] = useState(vazio());
    const [ultima, setUltima] = useState(null);

    // Retornos da prefeitura.
    //
    // A tabela bc_fis_nfse_retorno guarda uma linha por chamada, com o motivo
    // da recusa, o httpStatus e o codigo, e o JSON bruto da prefeitura no
    // Mongo. Isso existe para o motivo sobreviver ao fechamento da tela. Sem
    // esta aba o dado e um log: existe, e ninguem ve.
    const [aba, setAba] = useState('notas');
    const [retornos, setRetornos] = useState([]);
    const [carregandoRetornos, setCarregandoRetornos] = useState(false);
    const [bruto, setBruto] = useState(null);

    const carregarRetornos = useCallback(async (filtro) => {
        setCarregandoRetornos(true);
        try {
            // 'para-conferir' e o filtro que importa: sao as chamadas que
            // sairam e nao voltaram. Reemitir essas sem conferir e o caminho
            // da nota duplicada, e por isso o estado null do contrato existe.
            const url = filtro === 'para-conferir'
                ? `${BASE}/retornos/para-conferir`
                : filtro === 'recusas'
                    ? `${BASE}/retornos/recusas`
                    : BASE;
            const r = await apiFetch(url);
            if (r.ok) {
                const corpo = await r.json();
                setRetornos(listaDe(corpo));
            } else {
                setRetornos([]);
            }
        } catch (e) {
            setRetornos([]);
        } finally {
            setCarregandoRetornos(false);
        }
    }, []);

    const verBruto = async (notaId, retornoId) => {
        try {
            const r = await apiFetch(`${BASE}/${notaId}/retornos/${retornoId}/bruto`);
            setBruto(r.ok ? await r.json() : null);
        } catch (e) {
            setBruto(null);
        }
    };

    const aviso = (severity, summary, detail) =>
        toast.current?.show({ severity, summary, detail, life: 6000 });

    const carregar = useCallback(async () => {
        setCarregando(true);
        try {
            const r = await apiFetch(BASE);
            if (r.ok) setLista(listaDe(await r.json()));
            else setLista([]);
            setErro('');
        } catch (e) {
            setLista([]);
            setErro(erroDe(e, 'Não foi possível carregar as notas'));
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    // servicos e clientes: a emissao nasce de um deles, e digitar id na mao
    // e a forma mais rapida de emitir a nota errada
    useEffect(() => {
        apiFetch('/api/cadastro/servicos?size=200')
            .then((r) => (r.ok ? r.json() : []))
            .then((d) => setServicos(listaDe(d)))
            .catch(() => setServicos([]));
        apiFetch('/api/cadastro/clientes?size=200')
            .then((r) => (r.ok ? r.json() : []))
            .then((d) => setClientes(listaDe(d)))
            .catch(() => setClientes([]));
    }, []);

    const servico = useMemo(
        () => servicos.find((s) => s.id === form.servicoId) || null, [servicos, form.servicoId]);
    const cliente = useMemo(
        () => clientes.find((c) => c.id === form.clienteId) || null, [clientes, form.clienteId]);

    // o servico cadastrado ja traz valor e aliquota: repetir a digitacao é
    // chance de divergir do cadastro e a prefeitura recusar
    useEffect(() => {
        if (!servico) return;
        setForm((f) => ({
            ...f,
            valorServicos: f.valorServicos || num(servico.precoVenda),
            aliquota: f.aliquota || num(servico.aliquotaIss),
            discriminacao: f.discriminacao || servico.nome || ''
        }));
    }, [servico]);

    const semCodigoTributacao = !!servico && !String(servico.codigoTributacaoMunicipal ?? '').trim();

    const abrir = () => {
        setErro('');
        setForm(vazio());
        setDialogo(true);
    };

    const emitir = async () => {
        if (!form.servicoId) { setErro('Escolha o serviço'); return; }
        if (!(num(form.valorServicos) > 0)) { setErro('Informe o valor dos serviços'); return; }
        if (!form.razaoSocialTomador.trim()) { setErro('Informe a razão social do tomador'); return; }
        if (!form.cpfCnpjTomador.trim()) { setErro('Informe o CPF ou CNPJ do tomador, sem máscara'); return; }

        setEmitindo(true);
        setErro('');
        try {
            const corpo = {
                ...form,
                valorServicos: num(form.valorServicos),
                valorDeducoes: num(form.valorDeducoes),
                aliquota: num(form.aliquota),
                dataEmissao: ISO(form.dataEmissao) || undefined
            };
            if (form.clienteId) corpo.clienteId = form.clienteId;

            const r = await apiFetch(`${BASE}/emitir`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(corpo)
            });
            const j = await r.json().catch(() => null);
            if (!r.ok) throw new Error(erroDe({ payload: j }, 'A prefeitura recusou a emissão'));

            const d = j?.data ?? j;
            setUltima(d);
            aviso('success', `NFS-e ${d.numeroNfse || ''} emitida`.trim(),
                d.codigoVerificacao ? `Verificação: ${d.codigoVerificacao}` : 'Guarde o XML: ele fica disponível por 5 anos');
            setDialogo(false);
            carregar();
        } catch (e) {
            setErro(e.message || 'Falha ao emitir');
        } finally {
            setEmitindo(false);
        }
    };

    const cancelar = (nota) => {
        confirmDialog.require({
            header: 'Cancelar NFS-e',
            message: `Cancelar a nota ${nota.numeroNfse || nota.id}? `
                + 'O cancelamento é definitivo e a prefeitura é avisada.',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Sim, cancelar a nota',
            rejectLabel: 'Voltar',
            accept: async () => {
                setOcupado(nota.id);
                try {
                    const r = await apiFetch(`${BASE}/${nota.id}/cancelar`, { method: 'POST' });
                    const j = await r.json().catch(() => null);
                    if (!r.ok) throw new Error(erroDe({ payload: j }, 'A prefeitura recusou o cancelamento'));
                    aviso('success', 'Nota cancelada');
                    carregar();
                } catch (e) {
                    aviso('error', 'Não foi possível cancelar', e.message);
                } finally {
                    setOcupado(null);
                }
            }
        });
    };

    // XML e PDF vem por fetch e nao por window.open: as rotas exigem o token, e
    // abrir a URL direto devolveria 401 com o JSON de erro no lugar do arquivo
    const baixar = async (nota, tipo) => {
        setOcupado(nota.id);
        try {
            const r = await apiFetch(`${BASE}/${nota.id}/${tipo}`);
            if (!r.ok) {
                if (tipo === 'pdf' && r.status === 404) {
                    throw new Error('O PDF expira em 60 dias e já não está mais disponível. O XML fica por 5 anos.');
                }
                throw new Error(tipo === 'pdf' ? 'PDF indisponível' : 'XML indisponível');
            }
            const blob = await r.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `nfse-${nota.numeroNfse || nota.id}.${tipo}`;
            a.click();
            URL.revokeObjectURL(url);
        } catch (e) {
            aviso('info', 'Download', e.message);
        } finally {
            setOcupado(null);
        }
    };

    const statusCor = (s) => ({
        EMITIDA: 'success', AUTORIZADA: 'success', CANCELADA: 'danger',
        REJEITADA: 'danger', PENDENTE: 'warning'
    }[s] || 'info');

    const servicosOpts = servicos.map((s) => ({
        label: `${s.codigo || s.id} — ${s.nome || s.descricao || 'sem nome'}`,
        value: s.id
    }));
    const clientesOpts = clientes.map((c) => ({
        label: `${c.nome || c.razaoSocial || `#${c.id}`}`,
        value: c.id
    }));

    const campo = (id, rotulo, children, w) => (
        <div className={w || 'col-12 md:col-6'}>
            <label className="bc-label" htmlFor={id}>{rotulo}</label>
            {children}
        </div>
    );

    return (
        <div>
            <Toast ref={toast} />
            <ConfirmDialog />

            <div className="flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <span className="bc-muted">
                    Emissão para a prefeitura de São Paulo. O XML fica guardado 5 anos, o PDF 60 dias.
                </span>
                <Button label="Emitir NFS-e" icon="pi pi-send" onClick={abrir} />
            </div>

            {erro && <Message severity="error" text={erro} className="mb-3" />}

            <div className="flex gap-2 mb-3 flex-wrap">
                {[
                    ['notas', 'Notas emitidas'],
                    ['recusas', 'Recusas da prefeitura'],
                    ['para-conferir', 'Para conferir na prefeitura']
                ].map(([id, rotulo]) => (
                    <Button
                        key={id}
                        label={rotulo}
                        severity={aba === id ? undefined : 'secondary'}
                        outlined={aba !== id}
                        size="small"
                        onClick={() => {
                            setAba(id);
                            if (id !== 'notas') carregarRetornos(id);
                        }}
                    />
                ))}
            </div>

            {aba !== 'notas' && (
                <Card title={
                    aba === 'recusas'
                        ? 'Todas as vezes que a prefeitura recusou'
                        : 'Chamadas que saíram e não voltaram'
                } className="mb-3">
                    {aba === 'para-conferir' && (
                        <Message
                            severity="warn"
                            className="mb-3"
                            text="Cada linha aqui é uma chamada cuja resposta não chegou. Reemitir sem conferir na prefeitura é o caminho da nota duplicada — a prefeitura não devolve o mesmo número duas vezes."
                        />
                    )}
                    <DataTable
                        value={retornos}
                        loading={carregandoRetornos}
                        dataKey="id"
                        paginator
                        rows={10}
                        emptyMessage="Nenhum registro neste filtro"
                        className="p-datatable-sm"
                    >
                        <Column field="criadoEm" header="Quando" sortable
                            body={(r) => new Date(r.criadoEm).toLocaleString('pt-BR')}
                            style={{ width: '170px' }} />
                        <Column field="operacao" header="Operação"
                            style={{ width: '110px' }} />
                        <Column field="codigo" header="Cód." style={{ width: '80px' }}
                            body={(r) => (
                                <Tag
                                    severity={r.sucesso ? 'success' : 'danger'}
                                    value={r.codigo || r.httpStatus || '—'}
                                />
                            )} />
                        <Column field="mensagem" header="O que a prefeitura respondeu"
                            body={(r) => (
                                <span
                                    title={r.mensagem || ''}
                                    style={{ display: 'block', maxWidth: '620px', whiteSpace: 'normal' }}
                                >
                                    {r.mensagem || '—'}
                                </span>
                            )} />
                        <Column header="" style={{ width: '110px' }}
                            body={(r) => (
                                r.nfseId ? (
                                    <Button
                                        label="Ver JSON"
                                        size="small"
                                        text
                                        onClick={() => verBruto(r.nfseId, r.id)}
                                    />
                                ) : null
                            )} />
                    </DataTable>
                </Card>
            )}

            <Dialog
                header="Resposta bruta da prefeitura"
                visible={!!bruto}
                style={{ width: '70vw' }}
                modal
                onHide={() => setBruto(null)}
                footer={<Button label="Fechar" onClick={() => setBruto(null)} />}
            >
                <pre style={{
                    maxHeight: '60vh', overflow: 'auto', whiteSpace: 'pre-wrap',
                    fontSize: '12px', background: '#1e1e1e', color: '#e0e0e0',
                    padding: '12px', borderRadius: '4px'
                }}>
                    {typeof bruto === 'string' ? bruto : JSON.stringify(bruto, null, 2)}
                </pre>
            </Dialog>

            {ultima && (
                <Card className="mb-3" title="Última nota emitida">
                    <div className="grid">
                        {[
                            ['Número', ultima.numeroNfse],
                            ['Verificação', ultima.codigoVerificacao],
                            ['Chave nacional', ultima.chaveNotaNacional],
                            ['Inscrição municipal', ultima.inscricaoMunicipal],
                            ['Status', ultima.status]
                        ].map(([r, v]) => (
                            <div className="col-12 md:col" key={r}>
                                <div className="bc-muted small">{r}</div>
                                <div className="text-truncate" title={String(v || '')}>{v || '—'}</div>
                            </div>
                        ))}
                    </div>
                </Card>
            )}

            {aba === 'notas' && (
            <Card>
                <DataTable
                    value={lista}
                    loading={carregando}
                    dataKey="id"
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 25, 50]}
                    responsiveLayout="scroll"
                    emptyMessage="Nenhuma NFS-e emitida"
                    className="p-datatable-sm"
                >
                    <Column field="numeroNfse" header="Número" sortable />
                    <Column field="dataEmissao" header="Emissão" sortable body={(n) => dataBr(n.dataEmissao)} />
                    <Column field="valorServicos" header="Valor" sortable align="right"
                        body={(n) => `R$ ${money(n.valorServicos)}`} />
                    <Column field="aliquota" header="Alíquota" sortable align="right"
                        body={(n) => `${money(n.aliquota)}%`} style={{ width: '8rem' }} />
                    <Column field="tomadorRazaoSocial" header="Tomador"
                        body={(n) => n.tomadorRazaoSocial || n.razaoSocialTomador || '—'} />
                    <Column field="status" header="Status" sortable style={{ width: '10rem' }}
                        body={(n) => <Tag value={n.status || '—'} severity={statusCor(n.status)} />} />
                    <Column header="" style={{ width: '13rem' }} body={(n) => (
                        <div className="flex gap-1">
                            <Button icon="pi pi-code" rounded text tooltip="Baixar XML"
                                    loading={ocupado === n.id}
                                    onClick={() => baixar(n, 'xml')} />
                            <Button icon="pi pi-file-pdf" rounded text tooltip="Baixar PDF (60 dias)"
                                    loading={ocupado === n.id}
                                    onClick={() => baixar(n, 'pdf')} />
                            <Button icon="pi pi-times" rounded text severity="danger" tooltip="Cancelar"
                                    disabled={n.status === 'CANCELADA' || ocupado === n.id}
                                    onClick={() => cancelar(n)} />
                        </div>
                    )} />
                </DataTable>
            </Card>
            )}

            <Dialog
                visible={dialogo}
                onHide={() => setDialogo(false)}
                header="Emitir NFS-e"
                modal
                style={{ width: 'min(96vw, 760px)' }}
                breakpoints={{ '960px': '95vw' }}
                footer={
                    <div className="flex justify-end gap-2">
                        <Button label="Cancelar" severity="secondary" text onClick={() => setDialogo(false)} />
                        <Button label="Emitir" icon="pi pi-send" loading={emitindo}
                                disabled={emitindo} onClick={emitir} />
                    </div>
                }
            >
                <div className="grid p-fluid">
                    {campo('serv', 'Serviço *',
                        <Dropdown id="serv" value={form.servicoId} options={servicosOpts}
                                  placeholder="Escolha o serviço cadastrado" filter
                                  onChange={(e) => setForm({ ...form, servicoId: e.value })} />, 'col-12')}

                    {servico && (
                        <div className="col-12">
                            {semCodigoTributacao ? (
                                <Message severity="warn"
                                    text={`"${servico.nome}" não tem código de tributação municipal. A prefeitura recusa a emissão sem ele — ${'01.07'} da LC 116 passa no XSD e não existe na tabela municipal.`} />
                            ) : (
                                <Message severity="info"
                                    text={`Cód. tributação municipal: ${servico.codigoTributacaoMunicipal}`} />
                            )}
                        </div>
                    )}

                    {campo('cli', 'Cliente (opcional)',
                        <Dropdown id="cli" value={form.clienteId} options={clientesOpts}
                                  placeholder="Escolha o cliente" filter showClear
                                  onChange={(e) => setForm({
                                      ...form,
                                      clienteId: e.value ?? null,
                                      razaoSocialTomador: e.value
                                          ? (clientes.find((c) => c.id === e.value)?.nome ?? form.razaoSocialTomador)
                                          : form.razaoSocialTomador
                                  })} />, 'col-12')}

                    {campo('razao', 'Razão social do tomador *',
                        <InputText id="razao" value={form.razaoSocialTomador}
                                   onChange={(e) => setForm({ ...form, razaoSocialTomador: e.target.value })} />, 'col-12')}

                    {campo('doc', 'CPF / CNPJ do tomador *',
                        // Aceita CPF (11 dígitos) ou CNPJ (14). O CNPJ pode ter
                        // letra nas 12 primeiras posições (NT Conjunta 2025.001,
                        // produção desde 06/07/2026), então aqui também não se pode
                        // usar replace(/\D/g, ''): apagaria a letra e a prefeitura
                        // recusaria a nota por documento do tomador inválido.
                        <InputText id="doc" value={form.cpfCnpjTomador} placeholder="sem máscara"
                                   onChange={(e) => setForm({ ...form, cpfCnpjTomador: e.target.value.replace(/[.\s-]/g, '').toUpperCase() })} />)}

                    {campo('insc', 'Inscrição municipal do tomador',
                        <InputText id="insc" value={form.inscricaoMunicipalTomador}
                                   onChange={(e) => setForm({ ...form, inscricaoMunicipalTomador: e.target.value })} />)}

                    {campo('email', 'E-mail do tomador',
                        <InputText id="email" type="email" value={form.emailTomador}
                                   onChange={(e) => setForm({ ...form, emailTomador: e.target.value })} />)}

                    {campo('valor', 'Valor dos serviços *',
                        <InputNumber id="valor" value={form.valorServicos} mode="currency" locale="pt-BR"
                                     onValueChange={(e) => setForm({ ...form, valorServicos: e.value ?? 0 })} />)}

                    {campo('ded', 'Deduções',
                        <InputNumber id="ded" value={form.valorDeducoes} mode="currency" locale="pt-BR"
                                     onValueChange={(e) => setForm({ ...form, valorDeducoes: e.value ?? 0 })} />)}

                    {campo('aliq', 'Alíquota (%)',
                        <InputNumber id="aliq" value={form.aliquota} mode="decimal"
                                     minFractionDigits={2} maxFractionDigits={2} suffix=" %"
                                     onValueChange={(e) => setForm({ ...form, aliquota: e.value ?? 0 })} />)}

                    {campo('trib', 'Tributação do RPS',
                        <Dropdown id="trib" value={form.tributacaoRps} options={TRIBUTACOES}
                                  onChange={(e) => setForm({ ...form, tributacaoRps: e.value })} />)}

                    {campo('serie', 'Série do RPS',
                        <InputText id="serie" value={form.serieRps}
                                   onChange={(e) => setForm({ ...form, serieRps: e.target.value })} />)}

                    {campo('num', 'Número do RPS',
                        <InputNumber id="num" value={form.numeroRps} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, numeroRps: e.value })} />)}

                    {campo('dt', 'Data de emissão',
                        <Calendar id="dt" value={form.dataEmissao} dateFormat="dd/mm/yy"
                                  onChange={(e) => setForm({ ...form, dataEmissao: e.value })} />)}

                    {campo('disc', 'Discriminação dos serviços',
                        <InputTextarea id="disc" value={form.discriminacao} rows={3} autoResize
                                       onChange={(e) => setForm({ ...form, discriminacao: e.target.value })} />, 'col-12')}
                </div>
            </Dialog>
        </div>
    );
};

export default Nfse;
