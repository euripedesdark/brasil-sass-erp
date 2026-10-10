import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import RelatorioAgendadoService from '../../services/RelatorioAgendadoService';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';
import { localeAtivo } from '../shared/LocaleData.js';

export const RelatoriosAgendados = () => {
    const { user } = useAuth();
    const toast = useRef(null);
    const [agendamentos, setAgendamentos] = useState([]);
    const [relatorios, setRelatorios] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);

    const vazio = {
        id: null,
        relatorioId: null,
        nome: '',
        descricao: '',
        frequencia: 'DIARIA',
        intervaloDias: 1,
        proximaExecucao: null,
        ativo: true,
        emailDestinatarios: '',
        formato: 'PDF'
    };

    const [form, setForm] = useState(vazio);

    const frequenciaOptions = [
        { label: 'Diária', value: 'DIARIA' },
        { label: 'Semanal', value: 'SEMANAL' },
        { label: 'Quinzenal', value: 'QUINZENAL' },
        { label: 'Mensal', value: 'MENSAL' },
        { label: 'Trimestral', value: 'TRIMESTRAL' },
        { label: 'Anual', value: 'ANUAL' }
    ];

    const formatoOptions = [
        { label: 'PDF', value: 'PDF' },
        { label: 'Excel', value: 'EXCEL' },
        { label: 'CSV', value: 'CSV' }
    ];

    const fetchAgendamentos = async () => {
        setLoading(true);
        try {
            const data = await RelatorioAgendadoService.listar(user?.empresaId || '');
            setAgendamentos(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Erro ao carregar relatórios agendados', err);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Não foi possível carregar os agendamentos', life: 3000 });
        } finally {
            setLoading(false);
        }
    };

    const fetchRelatorios = async () => {
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios?empresaId=${user?.empresaId || ''}`);
            if (response.ok) {
                const data = await response.json();
                const lista = data.data || data;
                setRelatorios(Array.isArray(lista) ? lista : []);
            }
        } catch (err) {
            console.error('Erro ao carregar relatórios disponíveis', err);
        }
    };

    useEffect(() => {
        fetchAgendamentos();
        fetchRelatorios();
    }, []);

    const abrirNovo = () => {
        setForm(vazio);
        setDialogVisible(true);
    };

    const abrirEdicao = (ag) => {
        setForm({
            id: ag.id,
            relatorioId: ag.relatorioId ?? ag.relatorio?.id ?? null,
            nome: ag.nome || '',
            descricao: ag.descricao || '',
            frequencia: ag.frequencia || 'DIARIA',
            intervaloDias: ag.intervaloDias ?? 1,
            proximaExecucao: ag.proximaExecucao ? new Date(ag.proximaExecucao) : null,
            ativo: ag.ativo !== false,
            emailDestinatarios: ag.emailDestinatarios || '',
            formato: ag.formato || 'PDF'
        });
        setDialogVisible(true);
    };

    const salvar = async () => {
        if (!form.nome?.trim()) {
            toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o nome do agendamento', life: 3000 });
            return;
        }
        try {
            const payload = {
                ...form,
                proximaExecucao: form.proximaExecucao
                    ? new Date(form.proximaExecucao).toISOString()
                    : undefined
            };
            delete payload.id;
            if (form.id) {
                await RelatorioAgendadoService.atualizar(user?.empresaId, form.id, payload);
            } else {
                await RelatorioAgendadoService.criar(user?.empresaId, payload);
            }
            toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: 'Agendamento salvo', life: 3000 });
            setDialogVisible(false);
            fetchAgendamentos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const excluir = async (ag) => {
        try {
            await RelatorioAgendadoService.excluir(user?.empresaId, ag.id);
            toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: 'Agendamento excluído', life: 3000 });
            fetchAgendamentos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const executar = async (ag) => {
        try {
            await RelatorioAgendadoService.executar(user?.empresaId, ag.id);
            toast.current?.show({ severity: 'success', summary: 'Executado', detail: `Relatório "${ag.nome}" executado`, life: 3000 });
            fetchAgendamentos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const executarPendentes = async () => {
        try {
            await RelatorioAgendadoService.executarTodos(user?.empresaId);
            toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: 'Relatórios pendentes executados', life: 3000 });
            fetchAgendamentos();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 4000 });
        }
    };

    const formatarData = (value) => {
        if (!value) return '-';
        try {
            return new Date(value).toLocaleString(localeAtivo());
        } catch {
            return value;
        }
    };

    const statusTemplate = (rowData) => (
        <Tag severity={rowData.ativo !== false ? 'success' : 'danger'}
             value={rowData.ativo !== false ? 'Ativo' : 'Pausado'} />
    );

    const acoesTemplate = (rowData) => (
        <div className="flex gap-2">
            <Button icon="pi pi-play" size="small" text rounded severity="success"
                    tooltip="Executar agora" onClick={() => executar(rowData)} />
            <Button icon="pi pi-pencil" size="small" text rounded onClick={() => abrirEdicao(rowData)} />
            <Button icon="pi pi-trash" size="small" text rounded severity="danger"
                    onClick={() => excluir(rowData)} />
        </div>
    );

    const header = (
        <div className="flex justify-content-between align-items-center flex-wrap gap-2">
            <span className="font-bold text-lg">Relatórios Agendados</span>
            <div className="flex gap-2">
                <Button label="Executar pendentes" icon="pi pi-play" severity="secondary" outlined
                        loading={loading} onClick={executarPendentes} />
                <Button label="Novo agendamento" icon="pi pi-plus" onClick={abrirNovo} />
            </div>
        </div>
    );

    const dialogFooter = (
        <div className="flex justify-content-end gap-2">
            <Button label="Cancelar" icon="pi pi-times" text onClick={() => setDialogVisible(false)} />
            <Button label="Salvar" icon="pi pi-check" onClick={salvar} autoFocus />
        </div>
    );

    return (
        <div className="relatorios-agendados-container">
            <Toast ref={toast} />
            <Card header={header}>
                <DataTable
                    value={agendamentos}
                    loading={loading}
                    paginator
                    rows={10}
                    dataKey="id"
                    emptyMessage="Nenhum relatório agendado."
                    size="small"
                    stripedRows
                >
                    <Column field="nome" header="Nome" sortable />
                    <Column field="frequencia" header="Frequência" body={(r) => <Tag value={r.frequencia} severity="info" />} sortable />
                    <Column field="formato" header="Formato" />
                    <Column field="proximaExecucao" header="Próxima execução" body={(r) => formatarData(r.proximaExecucao)} sortable />
                    <Column field="ultimaExecucao" header="Última execução" body={(r) => formatarData(r.ultimaExecucao)} />
                    <Column field="ativo" header="Status" body={statusTemplate} />
                    <Column body={acoesTemplate} header="Ações" style={{ width: '140px' }} />
                </DataTable>
            </Card>

            <Dialog visible={dialogVisible} style={{ width: '560px' }}
                    header={form.id ? 'Editar agendamento' : 'Novo agendamento'}
                    modal dismissableMask onHide={() => setDialogVisible(false)} footer={dialogFooter}>
                <div className="grid p-fluid">
                    <div className="col-12 field">
                        <label htmlFor="ag-nome">Nome *</label>
                        <InputText id="ag-nome" value={form.nome}
                                   onChange={(e) => setForm({ ...form, nome: e.target.value })} />
                    </div>
                    <div className="col-12 field">
                        <label htmlFor="ag-relatorio">Relatório</label>
                        <Dropdown id="ag-relatorio" value={form.relatorioId}
                                 options={relatorios.map(r => ({ label: r.nome || r.name || `#${r.id}`, value: r.id }))}
                                 placeholder="Selecione o relatório" showClear
                                 onChange={(e) => setForm({ ...form, relatorioId: e.value })} />
                    </div>
                    <div className="col-12 field">
                        <label htmlFor="ag-desc">Descrição</label>
                        <InputTextarea id="ag-desc" rows={2} value={form.descricao}
                                       onChange={(e) => setForm({ ...form, descricao: e.target.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="ag-freq">Frequência</label>
                        <Dropdown id="ag-freq" value={form.frequencia} options={frequenciaOptions}
                                  onChange={(e) => setForm({ ...form, frequencia: e.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="ag-formato">Formato</label>
                        <Dropdown id="ag-formato" value={form.formato} options={formatoOptions}
                                  onChange={(e) => setForm({ ...form, formato: e.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="ag-intervalo">Intervalo (dias)</label>
                        <InputNumber id="ag-intervalo" value={form.intervaloDias} useGrouping={false} min={1}
                                     onValueChange={(e) => setForm({ ...form, intervaloDias: e.value })} />
                    </div>
                    <div className="col-6 field">
                        <label htmlFor="ag-proxima">Próxima execução</label>
                        <Calendar id="ag-proxima" value={form.proximaExecucao} showTime
                                  onChange={(e) => setForm({ ...form, proximaExecucao: e.value })} />
                    </div>
                    <div className="col-12 field">
                        <label htmlFor="ag-emails">E-mails destinatários</label>
                        <InputText id="ag-emails" value={form.emailDestinatarios} placeholder="email1@x.com;email2@y.com"
                                   onChange={(e) => setForm({ ...form, emailDestinatarios: e.target.value })} />
                    </div>
                    <div className="col-12 field flex align-items-center gap-3">
                        <label htmlFor="ag-ativo">Ativo</label>
                        <input id="ag-ativo" type="checkbox" checked={form.ativo}
                               onChange={(e) => setForm({ ...form, ativo: e.target.checked })} />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default RelatoriosAgendados;
