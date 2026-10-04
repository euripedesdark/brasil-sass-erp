import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { AutoComplete } from 'primereact/autocomplete';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { useAuth } from '../../contexts/AuthContext';
import { ServicoCadastroService } from '../../services/ServicoCadastroService';

// A API pode devolver a lista nua ou embrulhada em { data: { content } }.
// O util de listar ja normaliza, mas o backend mudou de forma antes e o cadastro
// nao pode quebrar por isso — a sugestao de nome e conforto, e conforto que
// some e pior que ausente.
const normalizarSugestoes = (dados) => {
    if (!dados) return [];
    if (Array.isArray(dados)) return dados;
    if (Array.isArray(dados.content)) return dados.content;
    if (Array.isArray(dados.data)) return dados.data;
    if (Array.isArray(dados.data?.content)) return dados.data.content;
    return [];
};

export const ServicoCadastro = () => {
    const { t } = useTranslation();
    const { user } = useAuth();

    // Cache por empresa. O dicionario de sugestoes e por termo, e termo e igual
    // em qualquer empresa — sem limpar na troca, o autocomplete de uma empresa
    // sugere servico da outra. Ja aconteceu com o token, nao seria diferente
    // aqui so porque o dado e menos sensivel.
    useEffect(() => {
        cacheSugestoes.current.clear();
        setSugestoesNome([]);
        setSugestoesAviso('');
    }, [user?.empresaId]);
    const toast = useRef(null);
    const [servicos, setServicos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);

    // Sugestoes de servico por nome, no proprio dialogo.
    //
    // O motivo de existir: servico com nome parecido e codigo municipal diferente
    // e a causa mais comum de recusa na prefeitura. A pessoa cadastra "Suporte
    // tecnico" com 2919, outra pessoa cadastra "Suporte tecnico em informacao"
    // com 2921, e a segunda nota vai recusada sem ninguem saber por que.
    const [sugestoesNome, setSugestoesNome] = useState([]);
    const [buscandoSugestoes, setBuscandoSugestoes] = useState(false);
    const [sugestoesAviso, setSugestoesAviso] = useState('');

    // Guarda o que ja veio do servidor, porque o AutoComplete refaz a busca a
    // cada tecla e sem isto a tela pisca e repete chamada.
    const cacheSugestoes = useRef(new Map());
    const requisicaoSugestoes = useRef(0);

    const buscarSugestoes = async (e) => {
        const termo = (e?.query || '').trim();
        if (termo.length < 2) {
            setSugestoesNome([]);
            setSugestoesAviso('');
            return;
        }

        const chave = termo.toLowerCase();
        if (cacheSugestoes.current.has(chave)) {
            aplicarSugestoes(cacheSugestoes.current.get(chave), novoServico);
            return;
        }

        // Numero da chamada: o AutoComplete dispara varias por segundo e as
        // respostas chegam fora de ordem. Sem checar, o resultado antigo
        // sobrescreve o novo e a lista mostra servico de outra busca.
        const numero = ++requisicaoSugestoes.current;
        setBuscandoSugestoes(true);
        try {
            const dados = await ServicoCadastroService.listar(0, 8, termo, '', null);
            if (numero !== requisicaoSugestoes.current) return;
            const lista = normalizarSugestoes(dados);
            cacheSugestoes.current.set(chave, lista);
            aplicarSugestoes(lista, novoServico);
        } catch (e) {
            if (numero !== requisicaoSugestoes.current) return;
            // Falha na sugestao nunca impede o cadastro. O campo segue editavel e
            // a pessoa digita o nome — a sugestao e conforto, nao validacao.
            setSugestoesNome([]);
            setSugestoesAviso('');
        } finally {
            if (numero === requisicaoSugestoes.current) setBuscandoSugestoes(false);
        }
    };

    const aplicarSugestoes = (lista, atual) => {
        setSugestoesNome(lista);
        if (!lista || lista.length === 0) {
            setSugestoesAviso('Nenhum servico com esse nome. Se e novo, siga.');
            return;
        }
        const nomeAtual = (atual?.nome || '').trim().toLowerCase();
        const exato = lista.find(s => (s.nome || '').trim().toLowerCase() === nomeAtual);
        const semCodigo = lista.filter(s => !s.codigoTributacaoMunicipal);
        if (exato) {
            setSugestoesAviso(`Ja existe o servico "${exato.nome}" (${exato.codigo}). `
                + 'Selecione na lista para trazer o codigo municipal.');
        } else if (semCodigo.length > 0) {
            setSugestoesAviso(`${semCodigo.length} servico(s) parecido(s) sem codigo municipal. `
                + 'Sem ele a prefeitura recusa com erro 306.');
        } else {
            setSugestoesAviso(`${lista.length} servico(s) parecido(s). Verifique se e o mesmo.`);
        }
    };
    const [first, setFirst] = useState(0);
    const [rows, setRows] = useState(20);

    const [novoServico, setNovoServico] = useState({
        id: null,
        nome: '',
        codigo: '',
        descricao: '',
        valorUnitario: 0,
        // Codigos fiscais. O municipal de São Paulo e o que vai no RPS e na
        // assinatura; sem ele a prefeitura recusa a nota (erro 306).
        lc116Codigo: '',
        codigoTributacaoMunicipal: '',
        nbs: '',
        codigoTributacaoNacional: '',
        aliquotaIss: 0,
        ativo: true
    });

    useEffect(() => {
        fetchServicos(0, rows);
    }, []);

    const fetchServicos = async (page, size) => {
        setLoading(true);
        try {
            const data = await ServicoCadastroService.listar(page, size, novoServico.nome, novoServico.codigo, novoServico.ativo);
            setServicos(data.content || data || []);
            setTotalRecords(data.totalElements || data.length || 0);
        } catch (err) {
            console.error('Erro ao carregar servicos', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar os servicos',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const onPageChange = async (event) => {
        setFirst(event.first);
        setRows(event.rows);
        await fetchServicos(event.page, event.rows);
    };

    const salvarServico = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            const servicoParaSalvar = {
                ...novoServico
            };

            if (servicoParaSalvar.id) {
                const response = await ServicoCadastroService.atualizar(servicoParaSalvar.id, servicoParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Servico atualizado com sucesso',
                    life: 3000
                });
            } else {
                const response = await ServicoCadastroService.criar(servicoParaSalvar);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Servico criado com sucesso',
                    life: 3000
                });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchServicos(first / rows, rows);
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || 'Erro ao salvar servico');
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel salvar o servico',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const excluirServico = async (id) => {
        try {
            await ServicoCadastroService.excluir(id);
            toast.current?.show({
                severity: 'success',
                summary: 'Sucesso',
                detail: 'Servico excluido com sucesso',
                life: 3000
            });
            fetchServicos(first / rows, rows);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel excluir o servico',
                life: 3000
            });
        }
    };

    const abrirDialog = (servico = null) => {
        if (servico) {
            setNovoServico({
                id: servico.id,
                nome: servico.nome || '',
                codigo: servico.codigo || '',
                descricao: servico.descricao || '',
                valorUnitario: servico.valorUnitario || 0,
                ativo: servico.ativo !== undefined ? servico.ativo : true
            });
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNovoServico({
            id: null,
            nome: '',
            codigo: '',
            descricao: '',
            valorUnitario: 0,
            lc116Codigo: '',
            codigoTributacaoMunicipal: '',
            nbs: '',
            aliquotaIss: 0,
            ativo: true
        });
        setError('');
        setSuccess(false);
    };

    const statusTemplate = (rowData) => {
        return (
            <Tag
                value={rowData.ativo ? 'ATIVO' : 'INATIVO'}
                severity={rowData.ativo ? 'success' : 'warning'}
                className={rowData.ativo ? 'tag-ativo' : 'tag-inativo'}
            />
        );
    };

    const valorTemplate = (rowData) => {
        return (
            <span className="valor-formatado">
                {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(rowData.valorUnitario || 0)}
            </span>
        );
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="servico-cadastro-acoes">
                <Button
                    icon="pi pi-pencil"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Editar"
                />
                <Button
                    icon="pi pi-trash"
                    className="p-button-danger p-button-sm p-button-text"
                    onClick={() => excluirServico(rowData.id)}
                    tooltip="Excluir"
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label="Cancelar"
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
            />
            <Button
                label="Salvar Servico"
                icon="pi pi-save"
                className="p-button-success"
                onClick={salvarServico}
                loading={loading}
            />
        </div>
    );

    return (
        <div className="servico-cadastro-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Cadastro de Servicos" className="servico-cadastro-main-card">
                <div className="servico-cadastro-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="servico-cadastro-info">
                        <p className="text-muted m-0">Gestao de servicos para ordens de servico e vendas.</p>
                    </div>
                    <Button
                        label="Novo Servico"
                        icon="pi pi-plus"
                        onClick={() => abrirDialog()}
                        className="p-button-success"
                    />
                </div>

                <DataTable
                    value={servicos}
                    loading={loading}
                    paginator
                    first={first}
                    rows={rows}
                    totalRecords={totalRecords}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    onPageChange={onPageChange}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum servico encontrado"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '120px' }} />
                    <Column field="nome" header="Nome" sortable style={{ width: '250px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '400px' }} />
                    <Column body={valorTemplate} header="Valor Unitario" sortable style={{ width: '150px' }} />
                    <Column field="lc116Codigo" header="LC 116" sortable style={{ width: '100px' }} />
                    <Column field="codigoTributacaoMunicipal" header="Cod. SP" sortable style={{ width: '100px' }} />
                    <Column field="codigoTributacaoNacional" header="Trib. Nacional" sortable style={{ width: '130px' }} />
                    <Column body={statusTemplate} header="Status" sortable style={{ width: '120px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={novoServico.id ? `Editar Servico: ${novoServico.nome}` : 'Novo Servico'}
                visible={dialogVisible}
                style={{ width: '600px' }}
                onHide={() => {
                    setDialogVisible(false);
                    resetForm();
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Servico salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Codigo *</label>
                            <InputText
                                value={novoServico.codigo}
                                onChange={(e) => setNovoServico({...novoServico, codigo: e.target.value})}
                                placeholder="COD001"
                                required
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Nome *</label>
                            {/* Sugere servico ja cadastrado enquanto a pessoa digita.
                                Sem isto, dois cadastros com o mesmo servico quase
                                nao nome caem no banco, e o codigo municipal diverge
                                entre eles — a prefeitura recusa o que estiver
                                errado, e a correcao nao fica obvia. */}
                            <AutoComplete
                                value={novoServico.nome}
                                suggestions={sugestoesNome}
                                completeMethod={buscarSugestoes}
                                field="nome"
                                minLength={2}
                                delay={300}
                                loading={buscandoSugestoes}
                                placeholder="Nome do servico"
                                dropdownClassName="p-autocomplete-dropdown"
                                itemTemplate={(s) => (
                                    <div>
                                        <strong>{s.nome}</strong>
                                        <small className="ml-2 text-color-secondary">
                                            {s.codigo}
                                            {s.codigoTributacaoMunicipal ? ` · cod. ${s.codigoTributacaoMunicipal}` : ' · SEM CODIGO MUNICIPAL'}
                                            {!s.ativo ? ' · inativo' : ''}
                                        </small>
                                    </div>
                                )}
                                onSelect={(e) => {
                                    const s = e.value;
                                    if (!s || typeof s === 'string') return;
                                    // Traz codigo e aliquota junto, que e o que a
                                    // pessoa nao lembra e a prefeitura exige.
                                    setNovoServico(prev => ({
                                        ...prev,
                                        nome: s.nome,
                                        codigo: s.codigo || prev.codigo,
                                        lc116Codigo: s.lc116Codigo || prev.lc116Codigo,
                                        codigoTributacaoMunicipal: s.codigoTributacaoMunicipal || '',
                                        aliquotaIss: s.aliquotaIss != null ? s.aliquotaIss : prev.aliquotaIss,
                                        // A resposta usa valorUnitario; a coluna
                                        // do banco se chama preco. Aceitar os dois
                                        // porque o nome ja mudou uma vez e o
                                        // silencio seria pior que a redundancia.
                                        valorUnitario: (s.valorUnitario ?? s.preco) != null
                                            ? (s.valorUnitario ?? s.preco)
                                            : prev.valorUnitario,
                                    }));
                                }}
                                onChange={(e) => setNovoServico({...novoServico, nome: e.value})}
                                required
                            />
                            {sugestoesAviso && (
                                <small className="text-color-secondary">{sugestoesAviso}</small>
                            )}
                        </div>
                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">Descricao</label>
                            <InputTextarea
                                value={novoServico.descricao}
                                onChange={(e) => setNovoServico({...novoServico, descricao: e.target.value})}
                                placeholder="Descricao detalhada do servico"
                                rows={3}
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Valor Unitario (R$)</label>
                            <InputNumber
                                value={novoServico.valorUnitario}
                                onChange={(e) => setNovoServico({...novoServico, valorUnitario: e.value || 0})}
                                mode="currency"
                                currency="BRL"
                                locale="pt-BR"
                                placeholder="0,00"
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Aliquota ISS</label>
                            <InputNumber
                                value={novoServico.aliquotaIss}
                                onChange={(e) => setNovoServico({...novoServico, aliquotaIss: e.value || 0})}
                                mode="percent"
                                locale="pt-BR"
                                placeholder="0,00"
                            />
                        </div>

                        <div className="col-12">
                            <Message
                                severity="info"
                                text="Codigos fiscais. O codigo municipal de São Paulo e o que vai no RPS da NFS-e; sem ele a prefeitura recusa a emissao. Para servico de TI: 01.07 (LC 116) = 2919 (municipal)."
                                className="w-full mb-3"
                            />
                        </div>

                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Codigo LC 116</label>
                            <InputText
                                value={novoServico.lc116Codigo}
                                onChange={(e) => setNovoServico({...novoServico, lc116Codigo: e.target.value})}
                                placeholder="01.07"
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">Codigo municipal SP *</label>
                            <InputText
                                value={novoServico.codigoTributacaoMunicipal}
                                onChange={(e) => setNovoServico({...novoServico, codigoTributacaoMunicipal: e.target.value})}
                                placeholder="2919"
                                maxLength={10}
                            />
                        </div>
                        <div className="col-12 md:col-4 field">
                            <label className="font-bold mb-2 block">NBS</label>
                            <InputText
                                value={novoServico.nbs}
                                onChange={(e) => setNovoServico({...novoServico, nbs: e.target.value})}
                                placeholder="000000000"
                                maxLength={12}
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Status</label>
                            <div className="p-inputswitch">
                                <Button
                                    label={novoServico.ativo ? 'ATIVO' : 'INATIVO'}
                                    icon={novoServico.ativo ? 'pi pi-check' : 'pi pi-times'}
                                    className={novoServico.ativo ? 'p-button-success' : 'p-button-warning'}
                                    onClick={() => setNovoServico({...novoServico, ativo: !novoServico.ativo})}
                                />
                            </div>
                        </div>
                    </div>

                    <Divider />
                    <div className="servico-cadastro-resumo flex justify-content-between align-items-center p-3 bg-gray-100 border-round">
                        <div>
                            <span className="font-bold">Codigo: </span>
                            <span>{novoServico.codigo}</span>
                        </div>
                        <div>
                            <span className="font-bold">Nome: </span>
                            <span>{novoServico.nome}</span>
                        </div>
                        <div>
                            <span className="font-bold">Valor: </span>
                            <span>{new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(novoServico.valorUnitario || 0)}</span>
                        </div>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default ServicoCadastro;
