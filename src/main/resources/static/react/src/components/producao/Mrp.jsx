import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Message } from 'primereact/message';
import { AutoComplete } from 'primereact/autocomplete';
import { apiFetch } from '../../services/ApiConfig';

export default function Mrp() {
    const [produto, setProduto] = useState(null);
    const [produtoSugestoes, setProdutoSugestoes] = useState([]);
    const [quantidade, setQuantidade] = useState(1);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [erro, setErro] = useState('');
    const [gerando, setGerando] = useState(false);
    const [resultado, setResultado] = useState(null);

    const buscarProdutos = async (event) => {
        try {
            const termo = event.query || '';
            const response = await apiFetch(
                '/api/cadastro/produtos?nome=' + encodeURIComponent(termo) + '&ativo=true&size=20'
            );
            if (!response.ok) {
                setProdutoSugestoes([]);
                return;
            }
            const data = await response.json();
            const page = data?.data ?? data;
            setProdutoSugestoes(Array.isArray(page) ? page : (page?.content || []));
        } catch {
            setProdutoSugestoes([]);
        }
    };

    const simular = async () => {
        if (!produto?.id) {
            setErro('Selecione um produto cadastrado.');
            return;
        }
        if (!(Number(quantidade) > 0)) {
            setErro('Informe uma quantidade planejada maior que zero.');
            return;
        }

        setLoading(true);
        setErro('');
        try {
            const response = await apiFetch('/api/producao/mrp/simular', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    produtoId: produto.id,
                    quantidade: Number(quantidade)
                })
            });
            if (!response.ok) {
                throw new Error((await response.text()) || 'Falha no MRP');
            }
            const data = await response.json();
            setRows(data?.data ?? data ?? []);
            setResultado(null);
        } catch (e) {
            setRows([]);
            setErro(e.message || 'Falha no MRP');
        } finally {
            setLoading(false);
        }
    };

    const gerarSugestoes = async () => {
        if (!produto?.id || !rows.some(r => r.acao !== 'SEM_ACAO')) return;
        setGerando(true);
        setErro('');
        try {
            const response = await apiFetch('/api/producao/mrp/gerar-sugestoes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ produtoId: produto.id, quantidade: Number(quantidade) })
            });
            if (!response.ok) {
                throw new Error((await response.text()) || 'Falha ao gerar sugestões');
            }
            const data = await response.json();
            setResultado(data?.data ?? data);
        } catch (e) {
            setErro(e.message || 'Falha ao gerar sugestões');
        } finally {
            setGerando(false);
        }
    };

    return (
        <div className="p-3">
            <Card
                title="MRP — Planejamento de Necessidades de Materiais"
                subTitle="Explosão multinível da BOM, estoque disponível e necessidade líquida"
            >
                <div className="grid p-fluid align-items-end">
                    <div className="col-12 md:col-5">
                        <label htmlFor="mrp-produto">Produto final</label>
                        <AutoComplete
                            id="mrp-produto"
                            value={produto}
                            suggestions={produtoSugestoes}
                            completeMethod={buscarProdutos}
                            field="nome"
                            dropdown
                            onChange={(e) => setProduto(e.value)}
                            placeholder="Consultar produto no cadastro"
                        />
                        <small className="bc-muted">
                            Consulta interna ao cadastro de produtos; nenhum ID precisa ser digitado.
                        </small>
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="mrp-qtd">Quantidade planejada</label>
                        <InputNumber
                            inputId="mrp-qtd"
                            value={quantidade}
                            onValueChange={e => setQuantidade(e.value ?? 0)}
                            min={0.0001}
                            maxFractionDigits={4}
                        />
                    </div>
                    <div className="col-12 md:col-4">
                        <Button
                            label="Executar MRP"
                            icon="pi pi-cog"
                            loading={loading}
                            onClick={simular}
                            disabled={!produto?.id}
                        />
                        <Button
                            label="Gerar OP / Solicitação"
                            icon="pi pi-send"
                            className="p-button-success ml-2"
                            loading={gerando}
                            onClick={gerarSugestoes}
                            disabled={!rows.some(r => r.acao !== 'SEM_ACAO')}
                        />
                    </div>
                </div>

                {resultado && (
                    <Message
                        severity="success"
                        className="w-full mt-3"
                        text={`Geradas ${resultado.ordensProducao?.length || 0} ordem(ns) de produção` +
                            (resultado.solicitacaoCompra
                                ? ` e a solicitação de compra ${resultado.solicitacaoCompra.numero}.`
                                : '; nada a comprar.')}
                    />
                )}
                {erro && <Message severity="error" text={erro} className="w-full mt-3" />}

                <DataTable
                    value={rows}
                    paginator
                    rows={15}
                    className="mt-4"
                    emptyMessage="Execute o MRP para ver as necessidades."
                    responsiveLayout="scroll"
                >
                    <Column field="produtoId" header="Produto" />
                    <Column field="nivel" header="Nível" />
                    <Column field="necessidadeBruta" header="Necessidade bruta" />
                    <Column field="estoqueDisponivel" header="Estoque disponível" />
                    <Column field="estoqueUtilizado" header="Estoque utilizado" />
                    <Column field="necessidadeLiquida" header="Necessidade líquida" />
                    <Column field="acao" header="Ação" />
                </DataTable>
            </Card>
        </div>
    );
}
