import React, { useState, useEffect } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import ApiConfig, { apiFetch } from '../../services/ApiConfig';
import { useAuth } from '../../contexts/AuthContext';

export const RH = () => {
    const { user } = useAuth();
    const [funcionarios, setFuncionarios] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [selectedFunc, setSelectedFunc] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [pessoas, setPessoas] = useState([]);
    const [carregandoPessoas, setCarregandoPessoas] = useState(false);

    const [form, setForm] = useState({
        pessoaId: null,
        nome: '',
        matricula: '',
        tipoColaborador: 'VENDEDOR',
        salario: 0,
        percentualComissao: 0,
        valorHora: 0
    });

    const tiposColaboradores = [
        { label: 'Vendedor', value: 'VENDEDOR' },
        { label: 'Técnico', value: 'TECNICO' },
        { label: 'Prestador', value: 'PRESTADOR' }
    ];

    useEffect(() => {
        fetchFuncionarios();
        fetchPessoas();
    }, [user?.empresaId]);

    const fetchPessoas = async (busca = '') => {
        setCarregandoPessoas(true);
        try {
            const query = busca ? '?busca=' + encodeURIComponent(busca) : '';
            const response = await apiFetch(ApiConfig.BASE_URL + '/api/rh/funcionarios/pessoas' + query);
            if (!response.ok) throw new Error('Não foi possível consultar as pessoas.');
            setPessoas(await response.json());
        } catch (err) {
            console.error('Erro ao carregar pessoas para RH', err);
        } finally {
            setCarregandoPessoas(false);
        }
    };

    const fetchFuncionarios = async () => {
        setLoading(true);
        try {
            const response = await apiFetch(`${ApiConfig.BASE_URL}/api/rh/funcionarios`);
            const data = await response.json();
            setFuncionarios(data);
        } catch (err) {
            console.error('Erro ao carregar funcionários', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        if (!form.pessoaId) {
            setError('Selecione uma Pessoa cadastrada para o colaborador.');
            return;
        }
        setLoading(true);
        setError('');
        setSuccess(false);
        try {
            const method = selectedFunc ? 'PUT' : 'POST';
            const url = selectedFunc
                ? `${ApiConfig.BASE_URL}/api/rh/funcionarios/${selectedFunc.id}`
                : `${ApiConfig.BASE_URL}/api/rh/funcionarios`;

            const response = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...form })
            });

            if (response.ok) {
                setSuccess(true);
                setTimeout(() => {
                    setDialogVisible(false);
                    fetchFuncionarios();
                }, 1500);
            } else {
                const err = await response.text();
                setError(err);
            }
        } catch (err) {
            setError('Erro ao salvar funcionário');
        } finally {
            setLoading(false);
        }
    };

    const openEdit = (f) => {
        setSelectedFunc(f);
        setForm({
            pessoaId: f.pessoaId || null,
            nome: f.nome || '',
            matricula: f.matricula || '',
            tipoColaborador: f.tipoColaborador || 'VENDEDOR',
            salario: f.salario || 0,
            percentualComissao: f.percentualComissao || 0,
            valorHora: f.valorHora || 0
        });
        setDialogVisible(true);
    };

    const openNew = () => {
        setSelectedFunc(null);
        setForm({ pessoaId: null, nome: '', matricula: '', tipoColaborador: 'VENDEDOR', salario: 0, percentualComissao: 0, valorHora: 0 });
        setDialogVisible(true);
    };

    const tipoTemplate = (rowData) => {
        const severity = {
            'VENDEDOR': 'success',
            'TECNICO': 'info',
            'PRESTADOR': 'warning'
        }[rowData.tipoColaborador] || 'info';

        return <Tag value={rowData.tipoColaborador} severity={severity} />;
    };

    return (
        <div className="rh-enterprise-container">
            <Card title="Gestão de Capital Humano" className="rh-main-card">
                <div className="rh-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="rh-stats">
                        <div className="stat-item">
                            <span className="stat-label">Total Colaboradores</span>
                            <span className="stat-value">{funcionarios.length}</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Ativos</span>
                            <span className="stat-value">{funcionarios.filter(f => f.ativo).length}</span>
                        </div>
                    </div>
                    <Button label="Novo Colaborador" icon="pi pi-plus" onClick={openNew} className="p-button-success" />
                </div>

                <DataTable
                    value={funcionarios}
                    loading={loading}
                    paginator
                    rows={10}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                >
                    <Column field="matricula" header="Matrícula" sortable style={{ width: '15%' }}></Column>
                    <Column field="pessoaId" header="Nome Completo" body={(row) => {
                        const pessoa = pessoas.find(p => p.id === row.pessoaId);
                        return pessoa?.nome || row.nome || '—';
                    }} sortable style={{ width: '30%' }}></Column>
                    <Column field="tipoColaborador" header="Função/Categoria" body={tipoTemplate} sortable style={{ width: '20%' }}></Column>
                    <Column field="salario" header="Salário Base" sortable style={{ width: '15%' }}></Column>
                    <Column
                        header="Ações"
                        body={(rowData) => (
                            <Button
                                icon="pi pi-pencil"
                                className="p-button-text p-button-sm"
                                onClick={() => openEdit(rowData)}
                            />
                        )}
                        style={{ width: '10%' }}
                    ></Column>
                </DataTable>
            </Card>

            <Dialog
                header={selectedFunc ? "Editar Colaborador" : "Novo Colaborador"}
                visible={dialogVisible}
                style={{ width: '450px' }}
                onHide={() => setDialogVisible(false)}
            >
                <div className="p-fluid">
                    <div className="field mb-3">
                        <label className="font-bold mb-2 block">Pessoa cadastrada *</label>
                        <Dropdown
                            value={form.pessoaId}
                            options={pessoas}
                            optionLabel="nome"
                            optionValue="id"
                            filter
                            filterBy="nome,documento"
                            showClear
                            loading={carregandoPessoas}
                            onChange={(e) => {
                                const pessoa = pessoas.find(p => p.id === e.value);
                                setForm({...form, pessoaId: e.value, nome: pessoa?.nome || form.nome});
                            }}
                            placeholder="Selecione uma Pessoa do cadastro"
                            emptyMessage="Nenhuma Pessoa encontrada"
                        />
                        <small className="text-muted">A Pessoa é uma consulta interna do cadastro; não é digitada livremente.</small>
                    </div>
                    <div className="field mb-3">
                        <label className="font-bold mb-2 block">Matrícula</label>
                        <InputText
                            value={form.matricula}
                            onChange={(e) => setForm({...form, matricula: e.target.value})}
                            placeholder="MAT-001"
                        />
                    </div>
                    <div className="field mb-3">
                        <label className="font-bold mb-2 block">Categoria Profissional</label>
                        <Dropdown
                            value={form.tipoColaborador}
                            options={tiposColaboradores}
                            onChange={(e) => setForm({...form, tipoColaborador: e.value})}
                            optionLabel="label"
                            optionValue="value"
                        />
                    </div>
                    <div className="field mb-3">
                        <label className="font-bold mb-2 block">Salário Base (R$)</label>
                        <InputNumber
                            value={form.salario}
                            onValueChange={(e) => setForm({...form, salario: e.value})}
                            mode="currency"
                            currency="BRL"
                            locale={localeAtivo()}
                        />
                    </div>
                    <div className="grid">
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Comissão (%)</label>
                            <InputNumber value={form.percentualComissao}
                                onValueChange={(e) => setForm({...form, percentualComissao: e.value ?? 0})}
                                min={0} max={100} suffix=" %" minFractionDigits={2} maxFractionDigits={2} />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Valor/hora (R$)</label>
                            <InputNumber value={form.valorHora}
                                onValueChange={(e) => setForm({...form, valorHora: e.value ?? 0})}
                                mode="currency" currency="BRL" locale={localeAtivo()} />
                        </div>
                    </div>

                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Funcionário salvo com sucesso!" className="w-full mb-3" />}

                    <div className="flex justify-content-end gap-2 mt-4">
                        <Button label="Cancelar" icon="pi pi-times" className="p-button-text" onClick={() => setDialogVisible(false)} />
                        <Button label="Salvar" icon="pi pi-save" onClick={handleSave} loading={loading} />
                    </div>
                </div>
            </Dialog>
        </div>
    );
};
