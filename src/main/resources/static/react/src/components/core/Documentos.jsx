import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/documentos';

// Os módulos são os mesmos que appearcem no menu lateral. O documento é
// guardado no módulo informado, e a entrega do arquivo é filtrada por módulo —
// quem não tem acesso a um módulo nem chega a ver o que há nele.
const MODULOS = [
    { label: 'Cadastros', value: 'cadastro' },
    { label: 'Vendas', value: 'vendas' },
    { label: 'Compras', value: 'compras' },
    { label: 'Estoque', value: 'estoque' },
    { label: 'Financeiro', value: 'financeiro' },
    { label: 'Fiscal', value: 'fiscal' },
    { label: 'Produção', value: 'producao' },
    { label: 'Serviços', value: 'servicos' },
    { label: 'Recursos Humanos', value: 'rh' },
    { label: 'Business Intelligence', value: 'bi' },
    { label: 'Inteligência Artificial', value: 'ia' },
    { label: 'Core e Administração', value: 'core' }
];

const MODULO_ROTULO = Object.fromEntries(MODULOS.map((m) => [m.value, m.label]));

const tamanho = (bytes) => {
    const b = Number(bytes ?? 0);
    if (b < 1024) return `${b} B`;
    if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
    return `${(b / (1024 * 1024)).toFixed(1)} MB`;
};

const dataHora = (v) => (v ? new Date(v).toLocaleString('pt-BR') : '—');

const erroDe = (e, padrao) => {
    const d = e?.payload;
    if (d?.message) return d.message;
    if (d?.errors?.[0]?.message) return d.errors[0].message;
    return padrao;
};

const listaDe = (j) => {
    const d = j?.data ?? j;
    return Array.isArray(d) ? d : (d?.content ?? []);
};

/**
 * Documentos.
 *
 * Quatro rotas existiam sem tela: enviar, listar, baixar o conteúdo e remover.
 *
 * O arquivo vai para o MongoDB e a referência fica no Postgres, ligado por
 * módulo + tipo de entidade + id. É por isso que a listagem é por módulo: a
 * entrega do arquivo também é filtrada por módulo, então um documento de
 * Financeiro não aparece para quem não tem acesso a Financeiro.
 *
 * A exclusão não recebe o id do arquivo. Recebe o par (tipoEntidade,
 * entidadeId) e apaga o conjunto — o mesmo que a regra do NFS-e usa, em que o
 * documento pertence ao registro e não tem vida própria.
 */
export const Documentos = () => {
    const toast = useRef(null);
    const input = useRef(null);

    const [modulo, setModulo] = useState('financeiro');
    const [docs, setDocs] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState('');

    const [form, setForm] = useState({
        arquivo: null, tipoEntidade: '', entidadeId: null, modulo: 'financeiro'
    });

    const carregar = useCallback(async (mod) => {
        setCarregando(true);
        try {
            const r = await apiFetch(`${BASE}?modulo=${encodeURIComponent(mod)}`);
            if (!r.ok) throw new Error('sem lista');
            setDocs(listaDe(await r.json()));
            setErro('');
        } catch (e) {
            setDocs([]);
            setErro(erroDe(e, 'Não foi possível carregar os documentos'));
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => { carregar(modulo); }, [modulo, carregar]);

    const mostrar = (severity, summary, detail) =>
        toast.current?.show({ severity, summary, detail, life: 5000 });

    const enviar = async () => {
        if (!form.arquivo) { setErro('Escolha um arquivo'); return; }
        if (!form.tipoEntidade.trim()) { setErro('Informe o tipo de entidade'); return; }
        if (!Number(form.entidadeId)) { setErro('Informe o id do registro a que o documento pertence'); return; }

        setEnviando(true);
        setErro('');
        try {
            const fd = new FormData();
            fd.append('file', form.arquivo);
            fd.append('tipoEntidade', form.tipoEntidade.trim());
            fd.append('modulo', form.modulo);
            fd.append('entidadeId', String(form.entidadeId));

            const r = await apiFetch(BASE, { method: 'POST', body: fd });
            const j = await r.json().catch(() => null);
            if (!r.ok) {
                // 503 significa que o Mongo está fora: o arquivo foi preservado
                // em disco e o caminho vem na mensagem, então o usuário não
                // perde o que enviou
                throw new Error(j?.message || 'Não foi possível enviar o documento');
            }
            mostrar('success', 'Documento enviado',
                `${form.arquivo.name} · ${tamanho(j?.data?.tamanho)}`);
            setForm({ arquivo: null, tipoEntidade: '', entidadeId: null, modulo: form.modulo });
            carregar(form.modulo);
        } catch (e) {
            setErro(erroDe(e, 'Falha ao enviar o documento'));
        } finally {
            setEnviando(false);
        }
    };

    const baixar = async (doc) => {
        try {
            const r = await apiFetch(`${BASE}/${encodeURIComponent(doc.id)}/conteudo`);
            if (!r.ok) throw new Error('sem conteúdo');
            const blob = await r.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = doc.nomeArquivo || `documento-${doc.id}`;
            a.click();
            URL.revokeObjectURL(url);
        } catch (e) {
            mostrar('error', 'Download', erroDe(e, 'Não foi possível baixar o documento'));
        }
    };

    const remover = (doc) => {
        confirmDialog.require({
            header: 'Remover documentos',
            message: `Remover os documentos de "${doc.tipoEntidade}" #${doc.entidadeId}? `
                + 'A remoção é do conjunto ligado ao registro, não de um arquivo só.',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Sim, remover',
            rejectLabel: 'Cancelar',
            accept: async () => {
                try {
                    const qs = `tipoEntidade=${encodeURIComponent(doc.tipoEntidade)}`
                        + `&entidadeId=${encodeURIComponent(doc.entidadeId)}`;
                    const r = await apiFetch(`${BASE}?${qs}`, { method: 'DELETE' });
                    if (!r.ok) {
                        const j = await r.json().catch(() => null);
                        throw new Error(j?.message || 'Não foi possível remover');
                    }
                    mostrar('success', 'Documentos removidos');
                    carregar(modulo);
                } catch (e) {
                    mostrar('error', 'Falha ao remover', erroDe(e, 'Não foi possível remover'));
                }
            }
        });
    };

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

            <Card title="Enviar documento" className="mb-3">
                <div className="grid p-fluid">
                    {campo('dmod', 'Módulo',
                        <Dropdown id="dmod" value={form.modulo} options={MODULOS}
                                  onChange={(e) => setForm({ ...form, modulo: e.value })} />, 'col-12 md:col-3')}

                    {campo('dtipo', 'Tipo de entidade *',
                        <InputText id="dtipo" value={form.tipoEntidade} placeholder="ex.: titulo, nota-fiscal"
                                   onChange={(e) => setForm({ ...form, tipoEntidade: e.target.value })} />, 'col-12 md:col-3')}

                    {campo('dent', 'ID do registro *',
                        <InputNumber id="dent" value={form.entidadeId} useGrouping={false}
                                     onValueChange={(e) => setForm({ ...form, entidadeId: e.value })} />, 'col-12 md:col-3')}

                    {campo('darq', 'Arquivo *',
                        <div>
                            <Button label={form.arquivo ? form.arquivo.name : 'Escolher arquivo'}
                                    icon="pi pi-upload" outlined
                                    onClick={() => input.current?.click()} />
                            <input ref={input} type="file" style={{ display: 'none' }}
                                   onChange={(e) => setForm({ ...form, arquivo: e.target.files?.[0] || null })} />
                        </div>, 'col-12 md:col-3')}

                    <div className="col-12">
                        <Button label="Enviar" icon="pi pi-check" loading={enviando}
                                disabled={enviando || !form.arquivo} onClick={enviar} />
                    </div>
                </div>
            </Card>

            <Card title="Documentos">
                <div className="mb-3" style={{ maxWidth: 320 }}>
                    <label className="bc-label" htmlFor="fmod">Módulo</label>
                    <Dropdown id="fmod" value={modulo} options={MODULOS}
                              onChange={(e) => setModulo(e.value)} />
                </div>

                {erro && <Message severity="error" text={erro} className="mb-3" />}

                <DataTable
                    value={docs}
                    loading={carregando}
                    dataKey="id"
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 25, 50]}
                    responsiveLayout="scroll"
                    emptyMessage={`Nenhum documento em ${MODULO_ROTULO[modulo] || modulo}`}
                    className="p-datatable-sm"
                >
                    <Column field="nomeArquivo" header="Arquivo" />
                    <Column field="tipoEntidade" header="Tipo" sortable />
                    <Column field="entidadeId" header="Registro" sortable style={{ width: '100px' }} />
                    <Column field="tamanho" header="Tamanho" sortable
                        body={(d) => tamanho(d.tamanho)} style={{ width: '110px' }} />
                    <Column field="contentType" header="Tipo" style={{ width: '160px' }}
                        body={(d) => <Tag value={d.contentType || '—'} />} />
                    <Column field="modulo" header="Módulo" style={{ width: '170px' }}
                        body={(d) => MODULO_ROTULO[d.modulo] || d.modulo || '—'} />
                    <Column field="dataUpload" header="Enviado" sortable
                        body={(d) => dataHora(d.dataUpload || d.criadoEm)} />
                    <Column header="" style={{ width: '8rem' }} body={(d) => (
                        <div className="flex gap-1">
                            <Button icon="pi pi-download" rounded text tooltip="Baixar" onClick={() => baixar(d)} />
                            <Button icon="pi pi-trash" rounded text severity="danger" tooltip="Remover do registro"
                                    onClick={() => remover(d)} />
                        </div>
                    )} />
                </DataTable>
            </Card>
        </div>
    );
};

export default Documentos;
