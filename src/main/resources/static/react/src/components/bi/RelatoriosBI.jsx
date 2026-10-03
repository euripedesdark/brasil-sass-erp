import { useTranslation } from 'react-i18next';
import React, { useEffect, useRef, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { Checkbox } from 'primereact/checkbox';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { useAuth } from '../../contexts/AuthContext';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';

const vazio = {
    id: null, nome: '', descricao: '', tipo: 'SQL', categoria: 'VENDAS',
    sqlQuery: '', parametros: {}, ativo: true, agendado: false,
    frequencia: 'DIARIO', emailDestinatarios: '', formatoExportacao: 'PDF'
};
const categorias = ['VENDAS', 'FINANCEIRO', 'ESTOQUE', 'PRODUCAO', 'RH', 'FISCAL', 'GERAL'].map(value => ({ label: value, value }));
const formatos = ['PDF', 'EXCEL', 'CSV'].map(value => ({ label: value, value }));

export const RelatoriosBI = () => {
  const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const empresaId = user?.empresaId;
    const [relatorios, setRelatorios] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [form, setForm] = useState(vazio);

    const carregar = async () => {
        if (!empresaId) return;
        setLoading(true);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios?empresaId=${empresaId}`);
            if (!response.ok) throw new Error(t('errors.load'));
            const data = await response.json();
            const lista = data.data || data;
            setRelatorios(Array.isArray(lista) ? lista : []);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'BI', detail: e.message, life: 4000 });
        } finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, [empresaId]);

    const abrirNovo = () => { setForm({ ...vazio, parametros: {} }); setDialogVisible(true); };
    const editar = (row) => { setForm({ ...vazio, ...row, parametros: row.parametros || {} }); setDialogVisible(true); };

    const salvar = async () => {
        if (!form.nome.trim() || !form.sqlQuery.trim()) {
            toast.current?.show({ severity: 'warn', summary: t('common.warning'), detail: t('legacyUi.relatorioBi.validation'), life: 3500 });
            return;
        }
        setLoading(true);
        try {
            const payload = {
                nome: form.nome.trim(), descricao: form.descricao, tipo: form.tipo, categoria: form.categoria,
                sqlQuery: form.sqlQuery, parametros: form.parametros || {}, ativo: form.ativo,
                agendado: form.agendado, frequencia: form.frequencia,
                emailDestinatarios: form.emailDestinatarios, formatoExportacao: form.formatoExportacao
            };
            const url = form.id
                ? `${ApiConfig.BASE_URL}/api/bi/relatorios/${form.id}?empresaId=${empresaId}`
                : `${ApiConfig.BASE_URL}/api/bi/relatorios?empresaId=${empresaId}`;
            const response = await apiFetch(url, {
                method: form.id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error(await response.text() || 'Não foi possível salvar o relatório');
            toast.current?.show({ severity: 'success', summary: 'BI', detail: 'Relatório salvo', life: 3000 });
            setDialogVisible(false);
            await carregar();
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'BI', detail: e.message, life: 4000 });
        } finally { setLoading(false); }
    };

    const excluir = (row) => {
        confirmDialog({
            message: `Excluir o relatório "${row.nome}"?`, header: 'Confirmar exclusão',
            icon: 'pi pi-exclamation-triangle', acceptClassName: 'p-button-danger',
            acceptLabel: t('legacyUi.relatorioBi.delete'), rejectLabel: t('legacyUi.relatorioBi.cancel'),
            accept: async () => {
                const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios/${row.id}?empresaId=${empresaId}`, { method: 'DELETE' });
                if (response.ok) { toast.current?.show({ severity: 'success', summary: 'BI', detail: 'Relatório excluído', life: 3000 }); carregar(); }
                else toast.current?.show({ severity: 'error', summary: 'BI', detail: 'Não foi possível excluir', life: 4000 });
            }
        });
    };

    const executar = async (row) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios/${row.id}/executar?empresaId=${empresaId}`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(row.parametros || {})
            });
            if (!response.ok) throw new Error(await response.text() || 'Falha na execução');
            const result = await response.json();
            const total = result.data?.resumo?.totalRegistros ?? result.resumo?.totalRegistros ?? 0;
            toast.current?.show({ severity: 'success', summary: t('legacyUi.relatorioBi.executed'), detail: `${total} registro(s) retornado(s)`, life: 3500 });
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Execução', detail: e.message, life: 4500 });
        }
    };

    const exportar = async (row, formato) => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios/${row.id}/${formato.toLowerCase()}?empresaId=${empresaId}`);
            if (!response.ok) throw new Error('Falha ao gerar arquivo');
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${row.nome.replace(/[^a-z0-9_-]/gi, '_')}.${formato === 'EXCEL' ? 'xlsx' : formato.toLowerCase()}`;
            document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Exportação', detail: e.message, life: 4000 });
        }
    };

    const acoes = row => (
        <div className="flex gap-1 flex-wrap">
            <Button icon="pi pi-play" text rounded severity="success" tooltip={t('legacyUi.relatorioBi.run')} onClick={() => executar(row)} />
            <Button icon="pi pi-file-pdf" text rounded severity="danger" tooltip="PDF" onClick={() => exportar(row, 'PDF')} />
            <Button icon="pi pi-file-excel" text rounded severity="success" tooltip="Excel" onClick={() => exportar(row, 'EXCEL')} />
            <Button icon="pi pi-download" text rounded tooltip="CSV" onClick={() => exportar(row, 'CSV')} />
            <Button icon="pi pi-pencil" text rounded tooltip={t('legacyUi.relatorioBi.edit')} onClick={() => editar(row)} />
            <Button icon="pi pi-trash" text rounded severity="danger" tooltip="Excluir" onClick={() => excluir(row)} />
        </div>
    );

    const header = (
        <div className="flex justify-content-between align-items-center flex-wrap gap-2">
            <div><div className="font-bold text-xl">Relatórios BI</div><small className="text-muted">Consultas gerenciais com execução e exportação</small></div>
            <Button label={t('legacyUi.relatorioBi.new')} icon="pi pi-plus" onClick={abrirNovo} />
        </div>
    );

    return (
        <div className="bi-relatorios-container">
            <Toast ref={toast} /><ConfirmDialog />
            <Card header={header}>
                <DataTable value={relatorios} loading={loading} paginator rows={10} stripedRows size="small" dataKey="id" emptyMessage="Nenhum relatório BI cadastrado.">
                    <Column field="nome" header={t('legacyUi.relatorioBi.name')} sortable />
                    <Column field="categoria" header={t('legacyUi.relatorioBi.category')} body={r => <Tag value={r.categoria || 'GERAL'} severity="info" />} sortable />
                    <Column field="tipo" header={t('legacyUi.relatorioBi.type')} />
                    <Column field="formatoExportacao" header={t('legacyUi.relatorioBi.format')} />
                    <Column field="agendado" header="Agendado" body={r => <Tag value={r.agendado ? 'Sim' : 'Não'} severity={r.agendado ? 'success' : 'secondary'} />} />
                    <Column field="ativo" header="Status" body={r => <Tag value={r.ativo !== false ? 'Ativo' : 'Inativo'} severity={r.ativo !== false ? 'success' : 'danger'} />} />
                    <Column body={acoes} header="Ações" style={{ minWidth: '300px' }} />
                </DataTable>
            </Card>
            <Dialog visible={dialogVisible} modal dismissableMask onHide={() => setDialogVisible(false)}
                style={{ width: 'min(900px, 95vw)' }} header={form.id ? `${t('legacyUi.relatorioBi.edit')} relatório BI` : `${t('legacyUi.relatorioBi.new')} BI`}
                footer={<div className="flex justify-content-end gap-2">
                    <Button label="Cancelar" text onClick={() => setDialogVisible(false)} />
                    <Button label="Salvar" icon="pi pi-check" loading={loading} onClick={salvar} />
                </div>}>
                <div className="grid p-fluid">
                    <div className="col-12 md:col-8 field"><label htmlFor="bi-rel-nome">Nome *</label>
                        <InputText id="bi-rel-nome" value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} /></div>
                    <div className="col-12 md:col-4 field"><label htmlFor="bi-rel-categoria">Categoria</label>
                        <Dropdown id="bi-rel-categoria" value={form.categoria} options={categorias} onChange={e => setForm({ ...form, categoria: e.value })} /></div>
                    <div className="col-12 field"><label htmlFor="bi-rel-descricao">Descrição</label>
                        <InputTextarea id="bi-rel-descricao" rows={2} value={form.descricao} onChange={e => setForm({ ...form, descricao: e.target.value })} /></div>
                    <div className="col-12 field"><label htmlFor="bi-rel-sql">Consulta SQL *</label>
                        <InputTextarea id="bi-rel-sql" rows={8} value={form.sqlQuery} placeholder="SELECT ... WHERE empresa_id = :empresaId"
                            onChange={e => setForm({ ...form, sqlQuery: e.target.value })} />
                        <small>Parâmetros podem usar chaves simples, chaves duplas ou prefixo $.</small></div>
                    <div className="col-12 md:col-4 field"><label htmlFor="bi-rel-formato">Formato padrão</label>
                        <Dropdown id="bi-rel-formato" value={form.formatoExportacao} options={formatos} onChange={e => setForm({ ...form, formatoExportacao: e.value })} /></div>
                    <div className="col-12 md:col-4 field flex align-items-center gap-2"><Checkbox inputId="bi-rel-ativo" checked={form.ativo} onChange={e => setForm({ ...form, ativo: e.checked })} /><label htmlFor="bi-rel-ativo">Ativo</label></div>
                    <div className="col-12 md:col-4 field flex align-items-center gap-2"><Checkbox inputId="bi-rel-agendado" checked={form.agendado} onChange={e => setForm({ ...form, agendado: e.checked })} /><label htmlFor="bi-rel-agendado">Agendado</label></div>
                </div>
            </Dialog>
        </div>
    );
};

export default RelatoriosBI;
