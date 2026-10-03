import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { apiFetch, desembrulharLista } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';

const BASE = '/api/cadastro/transportadoras';

const erroDe = (e, padrao) => {
    const d = e?.payload;
    if (d?.errors?.[0]?.message) return d.errors[0].message;
    if (d?.message) return d.message;
    return padrao;
};

const vazio = () => ({ pessoaId: null, codigo: '', registroAntt: '', ativo: true });

/**
 * Transportadoras.
 *
 * Esta tela consultava /api/cadastro/pessoas e nunca o controller de
 * transportadora: os 5 endpoints do CRUD estavam sem uso, e o nome da tela
 * nao correspondia ao que ela gerenciava. Agora fala com /transportadoras.
 *
 * A transportadora e uma pessoa (a razao social) mais os dados de transporte
 * — codigo interno e registro na ANTT. Por isso o combo de pessoa: o cadastro
 * de pessoa continua sendo a fonte da razao social, do CPF/CNPJ e do
 * endereco, como nos demais cadastros.
 */
export const Transportadora = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);

    const [rows, setRows] = useState([]);
    const [pessoas, setPessoas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [salvando, setSalvando] = useState(false);
    const [dialog, setDialog] = useState(false);
    const [editando, setEditando] = useState(null);
    const [form, setForm] = useState(vazio());
    const [busca, setBusca] = useState('');

    const carregar = async () => {
        setLoading(true);
        try {
            const [t, p] = await Promise.all([
                apiFetch(`${BASE}?page=0&size=200`),
                apiFetch(`/api/cadastro/pessoas?empresaId=${user?.empresaId ?? ''}&page=0&size=200`)
            ]);
            const lista = t.ok ? desembrulharLista(await t.json()).lista : [];
            const listaP = p.ok ? desembrulharLista(await p.json()).lista : [];
            setRows(Array.isArray(lista) ? lista : []);
            // o combo precisa de rotulo: razao social + documento, para o
            // operador distinguir duas empresas homonimas
            setPessoas((Array.isArray(listaP) ? listaP : []).map((x) => ({
                label: `${x.nome || '—'}${x.documento ? ' · ' + x.documento : ''}`,
                value: x.id
            })));
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Erro',
                detail: erroDe(e, 'Falha ao carregar transportadoras'), life: 5000
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { carregar(); }, [user?.empresaId]);

    const abrir = (linha) => {
        setEditando(linha || null);
        setForm(linha
            ? { pessoaId: linha.pessoa?.id ?? null, codigo: linha.codigo || '',
                registroAntt: linha.registroAntt || '', ativo: linha.ativo !== false }
            : vazio());
        setDialog(true);
    };

    const salvar = async () => {
        if (!form.pessoaId) {
            toast.current?.show({
                severity: 'warn', summary: 'Atenção',
                detail: 'Escolha a pessoa (razão social) da transportadora', life: 4000
            });
            return;
        }
        setSalvando(true);
        try {
            const url = editando ? `${BASE}/${editando.id}` : BASE;
            const r = await apiFetch(url, {
                method: editando ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form)
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.errors?.[0]?.message || 'Não foi possível salvar');
            }
            toast.current?.show({
                severity: 'success', summary: editando ? 'Atualizada' : 'Criada',
                detail: `Transportadora ${editando ? 'atualizada' : 'cadastrada'} com sucesso`, life: 3000
            });
            setDialog(false);
            carregar();
        } catch (e) {
            toast.current?.show({
                severity: 'error', summary: 'Erro',
                detail: erroDe(e, e.message || 'Falha ao salvar'), life: 5000
            });
        } finally {
            setSalvando(false);
        }
    };

    const excluir = (linha) => {
        confirmDialog.require({
            header: 'Desativar transportadora',
            message: `Desativar "${linha.pessoa?.nome || linha.codigo || linha.id}"? Ela deixa de aparecer nas seleções, mas o histórico de pedidos já feitos é preservado.`,
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Sim, desativar',
            rejectLabel: 'Cancelar',
            accept: async () => {
                try {
                    const r = await apiFetch(`${BASE}/${linha.id}`, { method: 'DELETE' });
                    if (!r.ok) throw new Error('Falhou');
                    toast.current?.show({
                        severity: 'success', summary: 'Desativada',
                        detail: 'Transportadora desativada', life: 3000
                    });
                    carregar();
                } catch (e) {
                    toast.current?.show({
                        severity: 'error', summary: 'Erro',
                        detail: erroDe(e, 'Não foi possível desativar'), life: 5000
                    });
                }
            }
        });
    };

    const filtradas = busca.trim()
        ? rows.filter((r) => `${r.pessoa?.nome ?? ''} ${r.codigo ?? ''} ${r.registroAntt ?? ''}`
            .toLowerCase().includes(busca.trim().toLowerCase()))
        : rows;

    return (
        <div className="p-4">
            <Toast ref={toast} />
            <ConfirmDialog />

            <div className="flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
                <div>
                    <h2 className="m-0">Transportadoras</h2>
                    <span className="bc-muted">Razão social, código interno e registro na ANTT</span>
                </div>
                <div className="flex gap-2">
                    <InputText placeholder="Buscar..." value={busca}
                               onChange={(e) => setBusca(e.target.value)} />
                    <Button label="Nova" icon="pi pi-plus" onClick={() => abrir(null)} />
                </div>
            </div>

            <DataTable
                value={filtradas}
                loading={loading}
                dataKey="id"
                paginator rows={10}
                emptyMessage="Nenhuma transportadora cadastrada."
                responsiveLayout="scroll"
            >
                <Column field="codigo" header="Código" style={{ width: '9rem' }} />
                <Column header="Razão social" body={(r) => r.pessoa?.nome || '—'} />
                <Column header="Documento" body={(r) => r.pessoa?.documento || '—'}
                        style={{ width: '12rem' }} />
                <Column field="registroAntt" header="Registro ANTT" />
                <Column header="Situação" body={(r) => (
                    <Tag value={r.ativo === false ? 'INATIVA' : 'ATIVA'}
                         severity={r.ativo === false ? 'secondary' : 'success'} />
                )} style={{ width: '9rem' }} />
                <Column header="" body={(r) => (
                    <div className="flex gap-1">
                        <Button icon="pi pi-pencil" rounded text tooltip="Editar"
                                onClick={() => abrir(r)} />
                        <Button icon="pi pi-trash" rounded text severity="danger" tooltip="Desativar"
                                disabled={r.ativo === false}
                                onClick={() => excluir(r)} />
                    </div>
                )} style={{ width: '6rem' }} />
            </DataTable>

            <Dialog
                visible={dialog}
                onHide={() => setDialog(false)}
                header={editando ? 'Editar transportadora' : 'Nova transportadora'}
                modal
                style={{ width: 'min(96vw, 560px)' }}
                breakpoints={{ '960px': '95vw' }}
                footer={
                    <div className="flex justify-end gap-2">
                        <Button label="Cancelar" severity="secondary" text
                                onClick={() => setDialog(false)} />
                        <Button label="Salvar" icon="pi pi-check" loading={salvando}
                                disabled={salvando} onClick={salvar} />
                    </div>
                }
            >
                <div className="grid p-fluid">
                    <div className="col-12">
                        <label className="bc-label" htmlFor="pes">Pessoa (razão social) *</label>
                        <Dropdown id="pes" value={form.pessoaId} options={pessoas}
                                  filter onFilter={(e) => { }}
                                  placeholder="Selecione a pessoa"
                                  onChange={(e) => setForm({ ...form, pessoaId: e.value })} />
                        {pessoas.length === 0 && (
                            <Message severity="warn" className="mt-2"
                                      text="Nenhuma pessoa cadastrada. Cadastre a razão social em Cadastros › Pessoas antes de criar a transportadora." />
                        )}
                    </div>
                    <div className="col-12 md:col-6">
                        <label className="bc-label" htmlFor="cod">Código</label>
                        <InputText id="cod" value={form.codigo}
                                   onChange={(e) => setForm({ ...form, codigo: e.target.value })} />
                    </div>
                    <div className="col-12 md:col-6">
                        <label className="bc-label" htmlFor="att">Registro ANTT</label>
                        <InputText id="att" value={form.registroAntt}
                                   onChange={(e) => setForm({ ...form, registroAntt: e.target.value })} />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default Transportadora;
