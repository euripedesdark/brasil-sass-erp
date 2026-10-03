import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { AutoComplete } from 'primereact/autocomplete';
import { Message } from 'primereact/message';
import { useAuth } from '../../contexts/AuthContext';
import ProdutoService from '../../services/ProdutoService';
import EstruturaProdutoService from '../../services/EstruturaProdutoService';

export const EstruturaProduto = () => {
    const { user } = useAuth();
    const [produtoPai, setProdutoPai] = useState(null);
    const [produtoFilho, setProdutoFilho] = useState(null);
    const [paiSugestoes, setPaiSugestoes] = useState([]);
    const [filhoSugestoes, setFilhoSugestoes] = useState([]);
    const [itens, setItens] = useState([]);
    const [quantidade, setQuantidade] = useState(1);
    const [perda, setPerda] = useState(0);
    const [nivel, setNivel] = useState(1);
    const [observacao, setObservacao] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const buscar = async (query, setSuggestions) => {
        try {
            const response = query
                ? await ProdutoService.buscarPorNome(query, 0, 20)
                : await ProdutoService.listarTodos(0, 20);
            const data = response.data?.content || response.data || [];
            setSuggestions(Array.isArray(data) ? data : []);
        } catch {
            setSuggestions([]);
        }
    };

    const carregar = async (produto = produtoPai) => {
        if (!produto?.id) {
            setItens([]);
            return;
        }
        try {
            const response = await EstruturaProdutoService.listar(produto.id);
            setItens(response.data || []);
        } catch (e) {
            setError(e.response?.data?.message || 'Não foi possível carregar a estrutura.');
        }
    };

    useEffect(() => { carregar(); }, [produtoPai?.id]);

    const adicionar = async () => {
        if (!produtoPai?.id || !produtoFilho?.id || quantidade <= 0) {
            setError('Selecione o produto pai, o componente e informe uma quantidade positiva.');
            return;
        }
        setLoading(true);
        setError('');
        try {
            await EstruturaProdutoService.criar({
                produtoPaiId: produtoPai.id,
                produtoFilhoId: produtoFilho.id,
                quantidade,
                perdaPercentual: perda || 0,
                nivel: nivel || 1,
                ativo: true,
                observacao
            });
            setProdutoFilho(null);
            setQuantidade(1);
            setPerda(0);
            setNivel(1);
            setObservacao('');
            await carregar();
        } catch (e) {
            setError(e.response?.data?.message || 'Erro ao salvar componente.');
        } finally {
            setLoading(false);
        }
    };

    const excluir = async (row) => {
        try {
            await EstruturaProdutoService.excluir(row.id);
            await carregar();
        } catch (e) {
            setError(e.response?.data?.message || 'Erro ao excluir componente.');
        }
    };

    const produtoLabel = (item) => item ? `${item.codigo ? item.codigo + ' - ' : ''}${item.nome || item.descricao || 'Produto #' + item.id}` : '';

    return (
        <div className="p-fluid">
            <Card title="Engenharia do Produto — Estrutura / BOM">
                <Message severity="info" text="Defina os componentes necessários para fabricar cada produto. A ordem de produção poderá usar esta estrutura automaticamente." className="w-full mb-3" />
                <div className="grid">
                    <div className="col-12 md:col-6 field">
                        <label>Produto acabado / Pai</label>
                        <AutoComplete
                            value={produtoPai}
                            suggestions={paiSugestoes}
                            completeMethod={(e) => buscar(e.query, setPaiSugestoes)}
                            onChange={(e) => setProdutoPai(e.value)}
                            itemTemplate={produtoLabel}
                            selectedItemTemplate={produtoLabel}
                            field="nome"
                            placeholder="Pesquisar produto..."
                            dropdown
                        />
                    </div>
                    <div className="col-12 md:col-6 field">
                        <label>Componente / Matéria-prima</label>
                        <AutoComplete
                            value={produtoFilho}
                            suggestions={filhoSugestoes}
                            completeMethod={(e) => buscar(e.query, setFilhoSugestoes)}
                            onChange={(e) => setProdutoFilho(e.value)}
                            itemTemplate={produtoLabel}
                            selectedItemTemplate={produtoLabel}
                            field="nome"
                            placeholder="Pesquisar componente..."
                            dropdown
                        />
                    </div>
                    <div className="col-12 md:col-3 field">
                        <label>Quantidade</label>
                        <InputNumber value={quantidade} onValueChange={(e) => setQuantidade(e.value)} minFractionDigits={3} />
                    </div>
                    <div className="col-12 md:col-3 field">
                        <label>Perda (%)</label>
                        <InputNumber value={perda} onValueChange={(e) => setPerda(e.value)} minFractionDigits={2} maxFractionDigits={4} />
                    </div>
                    <div className="col-12 md:col-3 field">
                        <label>Nível</label>
                        <InputNumber value={nivel} onValueChange={(e) => setNivel(e.value)} min={1} max={99} />
                    </div>
                    <div className="col-12 md:col-3 field">
                        <label>Observação</label>
                        <InputText value={observacao} onChange={(e) => setObservacao(e.target.value)} />
                    </div>
                    <div className="col-12 flex justify-content-end">
                        <Button label="Adicionar componente" icon="pi pi-plus" onClick={adicionar} loading={loading} />
                    </div>
                </div>

                {error && <Message severity="error" text={error} className="w-full mb-3" />}

                <DataTable value={itens} emptyMessage="Nenhum componente cadastrado." stripedRows paginator rows={10}>
                    <Column field="produtoFilhoId" header="Componente" />
                    <Column field="quantidade" header="Qtd." />
                    <Column field="perdaPercentual" header="Perda %" />
                    <Column field="nivel" header="Nível" />
                    <Column field="observacao" header="Observação" />
                    <Column header="" body={(row) => <Button icon="pi pi-trash" text severity="danger" onClick={() => excluir(row)} />} />
                </DataTable>
            </Card>
        </div>
    );
};

export default EstruturaProduto;
