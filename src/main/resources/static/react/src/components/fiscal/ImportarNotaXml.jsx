import React, { useState, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Tag } from 'primereact/tag';
import { Checkbox } from 'primereact/checkbox';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { EntradaNotaService } from '../../services/EntradaNotaService';
import { apiFetch } from '../../services/ApiConfig';
import ApiConfig from '../../services/ApiConfig';

/** Como o backend nomeia cada situacao de item, e o que a tela mostra. */
const SITUACAO = {
    CADASTRADO: { rotulo: 'Já cadastrado', cor: 'success', icone: 'pi pi-check' },
    CADASTRADO_COM_COMPLEMENTO: {
        rotulo: 'Cadastrado, falta o dado fiscal',
        cor: 'info', icone: 'pi pi-pencil'
    },
    A_CRIAR: { rotulo: 'Será criado', cor: 'warning', icone: 'pi pi-plus' },
    SEM_PRODUTO: { rotulo: 'Sem produto', cor: 'danger', icone: 'pi pi-times' }
};

const dinheiro = (v) => v === null || v === undefined
    ? '—'
    : new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v);

const soNumero = (v) => v === null || v === undefined ? '0'
    : new Intl.NumberFormat('pt-BR').format(v);

/**
 * Importa uma NFe de arquivo XML.
 *
 * <h3>Por que dois botões e nao um</h3>
 * Importar mexe no estoque, cria produto e grava documento fiscal. Num botao
 * so, o erro sai caro e nao tem volta facil. Aqui o arquivo e lido primeiro
 * e o plano aparece — os valores, a conta do desconto, quantos produtos vao
 * ser criados e quais itens ficaram sem produto. O estoque so muda depois que
 * o usuario leu isso.
 *
 * <h3>Por que a conta do desconto aparece separada</h3>
 * A soma dos itens e o total da nota sao numeros diferentes quando a nota tem
 * desconto, e so um dos dois e o que entra no estoque. Mostrar os tres
 * (soma, desconto, liquido) e o que impede a duvida de "qual valor foi para
 * o estoque" — que e exatamente a duvida que faz o usuario conferir a nota
 * de novo e nao confiar no sistema.
 */
export const ImportarNotaXml = ({ visivel, onHide, onConfirmado }) => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [arquivo, setArquivo] = useState(null);
    const [plano, setPlano] = useState(null);
    const [analisando, setAnalisando] = useState(false);
    const [confirmando, setConfirmando] = useState(false);
    const [criarProdutos, setCriarProdutos] = useState(false);
    const [fornecedores, setFornecedores] = useState([]);
    const [pessoaId, setPessoaId] = useState(null);

    React.useEffect(() => {
        if (!visivel) return;
        setArquivo(null); setPlano(null); setCriarProdutos(false); setPessoaId(null);
        // A lista de fornecedores e longa e raramente muda. Buscar uma vez por
        // abertura e melhor do que no componente, que buscaria a cada render.
        //
        // O /cadastro/fornecedores devolve o fornecedor com a pessoa aninhada
        // ({ pessoa: { id, nome } }), nao com razaoSocial plano. Ler
        // `f.razaoSocial` aqui daria "undefined" em todas as 200 opcoes, e o
        // dropdown mostraria 200 linhas em branco. O que interessa aqui e o
        // id da PESSOA, porque e pessoaId que a nota guarda.
        apiFetch(`${ApiConfig.API_BASE_URL}/cadastro/fornecedores?size=200`)
            .then((r) => (r.ok ? r.json() : null))
            .then((d) => {
                const itens = d?.data?.content || d?.content || d?.data || d || [];
                setFornecedores(Array.isArray(itens)
                    ? itens
                        .filter((f) => f?.pessoa?.id)
                        .map((f) => ({
                            label: `${f.pessoa.nome || '(sem nome)'}${f.codigo ? ' · ' + f.codigo : ''}`,
                            value: f.pessoa.id
                        }))
                    : []);
            })
            .catch(() => setFornecedores([]));
    }, [visivel]);

    const avisar = (severity, summary, detail) =>
        toast.current?.show({ severity, summary, detail, life: 7000 });

    const escolher = (e) => {
        const f = e.target.files?.[0] || null;
        setArquivo(f);
        setPlano(null); // plano de outro arquivo nao vale para este
    };

    const analisar = async () => {
        if (!arquivo) return avisar('warn', 'Sem arquivo', 'Escolha o XML da nota primeiro.');
        setAnalisando(true);
        setPlano(null);
        try {
            const p = await EntradaNotaService.analisarImportacao(arquivo, pessoaId, criarProdutos);
            setPlano(p);
        } catch (e) {
            avisar('error', 'Nao deu para ler', e.message);
        } finally {
            setAnalisando(false);
        }
    };

    const confirmar = async () => {
        if (!plano) return;
        setConfirmando(true);
        try {
            const r = await EntradaNotaService.confirmarImportacao(arquivo, pessoaId, criarProdutos);
            avisar('success', 'Entrada registrada',
                `Nota ${r.chave?.slice(-12) || ''} · ${r.itensGravados} item(ns) · `
                + `${r.produtosCriados} produto(s) criado(s) · `
                + `${r.itensComEntrada} com entrada no estoque`);
            onConfirmado?.(r);
            onHide();
        } catch (e) {
            avisar('error', 'Nao foi possivel confirmar', e.message);
        } finally {
            setConfirmando(false);
        }
    };

    const situacaoBody = (item) => {
        const s = SITUACAO[item.situacao] || SITUACAO.SEM_PRODUTO;
        return (
            <div>
                <Tag severity={s.cor} icon={s.icone} value={s.rotulo} />
                {item.produtoId && (
                    <div className="text-color-secondary text-xs mt-1">
                        {item.produtoNome}
                    </div>
                )}
            </div>
        );
    };

    const rodape = (
        <div className="flex justify-content-between align-items-center flex-wrap gap-2">
            {!plano ? (
                <span className="text-color-secondary text-sm">
                    Nada foi gravado ainda. Ler o arquivo não mexe no estoque.
                </span>
            ) : (
                <span className="text-color-secondary text-sm">
                    {plano.itens?.length || 0} item(ns) ·
                    {' '}{dinheiro(plano.valorLiquido)} entra no estoque
                </span>
            )}
            <div className="flex gap-2">
                <Button label="Fechar" severity="secondary" text onClick={onHide} />
                {!plano ? (
                    <Button label="Ler o arquivo" icon="pi pi-search"
                        loading={analisando} disabled={!arquivo} onClick={analisar} />
                ) : (
                    <Button label="Confirmar entrada" icon="pi pi-check"
                        severity="success" loading={confirmando} onClick={confirmar} />
                )}
            </div>
        </div>
    );

    return (
        <Dialog
            header="Importar nota fiscal por XML"
            visible={visivel}
            onHide={onHide}
            style={{ width: '92vw', maxWidth: '1200px' }}
            modal
            footer={rodape}
        >
            <Toast ref={toast} />

            <div className="grid">
                <div className="col-12 md:col-5">
                    <label className="bc-muted small block mb-2">Arquivo XML da nota</label>
                    <input type="file" accept=".xml,text/xml,application/xml,.gz"
                        onChange={escolher} className="w-full" />
                    <div className="text-color-secondary text-xs mt-2">
                        É o XML que a prefeitura manda, não o PDF da DANFE.
                        Arquivos .gz (compactados) são lidos.
                    </div>
                </div>

                <div className="col-12 md:col-4">
                    <label className="bc-muted small block mb-2">Fornecedor</label>
                    <Dropdown
                        value={pessoaId}
                        options={fornecedores}
                        onChange={(e) => { setPessoaId(e.value); setPlano(null); }}
                        placeholder="Descobrir pelo CNPJ do emitente"
                        filter
                        showClear
                        className="w-full"
                    />
                    <div className="text-color-secondary text-xs mt-2">
                        Se ficar vazio, o ERP procura o emitente pelo CNPJ.
                    </div>
                </div>

                <div className="col-12 md:col-3">
                    <label className="bc-muted small block mb-2">Produto que não casou</label>
                    <div className="flex align-items-center" style={{ paddingTop: '6px' }}>
                        <Checkbox inputId="criarProdutos" checked={criarProdutos}
                            onChange={(e) => { setCriarProdutos(e.checked); setPlano(null); }} />
                        <label htmlFor="criarProdutos" className="ml-2 cursor-pointer">
                            Criar produto
                        </label>
                    </div>
                    <div className="text-color-secondary text-xs mt-2">
                        Desligado, o item fica sem entrada no estoque e a nota
                        entra assim mesmo.
                    </div>
                </div>
            </div>

            {plano && (
                <>
                    <Message severity="info" className="my-3"
                        text="Abaixo está o que vai acontecer. Nada foi gravado ainda — o estoque só muda em 'Confirmar entrada'." />

                    <div className="grid mb-3">
                        {[
                            ['Emitente', `${plano.emitenteNome || '—'} · ${plano.emitenteCnpj || 'sem CNPJ'}`],
                            ['Destinatário', `${plano.destinatarioNome || '—'} · ${plano.destinatarioCnpj || '—'}`],
                            ['Número', `${plano.numero || '—'} série ${plano.serie || '—'}`],
                            ['Emissão', (plano.emissao || '').slice(0, 10) || '—'],
                            ['Chave', plano.chave || 'ausente no XML'],
                            ['Natureza', plano.naturezaOperacao || '—'],
                            ['Soma dos itens', dinheiro(plano.somaItens)],
                            ['Desconto da nota', dinheiro(plano.valorDesconto)],
                            ['Entra no estoque', dinheiro(plano.valorLiquido)],
                            ['Total da nota (vNF)', dinheiro(plano.valorTotal)],
                            ['ICMS', dinheiro(plano.valorIcms)],
                            ['IPI / PIS / COFINS',
                                `${dinheiro(plano.valorIpi)} · ${dinheiro(plano.valorPis)} · ${dinheiro(plano.valorCofins)}`]
                        ].map(([r, v]) => (
                            <div className="col-12 md:col-6 xl:col-4" key={r}>
                                <div className="bc-muted small">{r}</div>
                                <div className="text-truncate" title={String(v)}>{v}</div>
                            </div>
                        ))}
                    </div>

                    {plano.avisos?.length > 0 && (
                        <div className="mb-3">
                            {plano.avisos.map((a, i) => (
                                <Message key={i} severity="warn" className="mb-2" text={a} />
                            ))}
                        </div>
                    )}

                    <DataTable value={plano.itens || []} dataKey="numeroItem"
                        size="small" className="p-datatable-sm"
                        emptyMessage="A nota não tem itens">
                        <Column field="numeroItem" header="#" style={{ width: '40px' }} />
                        <Column field="descricao" header="Produto"
                            body={(i) => <span title={i.descricao}>{i.descricao || '—'}</span>} />
                        <Column field="cProd" header="Cód. fornecedor" style={{ width: '120px' }} />
                        <Column field="ean" header="GTIN" style={{ width: '130px' }}
                            body={(i) => i.ean || <span className="text-color-secondary">sem GTIN</span>} />
                        <Column field="ncm" header="NCM" style={{ width: '95px' }} />
                        <Column field="cest" header="CEST" style={{ width: '95px' }} />
                        <Column field="cfop" header="CFOP" style={{ width: '80px' }} />
                        <Column field="quantidade" header="Qtd" style={{ width: '70px' }}
                            body={(i) => soNumero(i.quantidade)} />
                        <Column field="valorTotal" header="Valor" style={{ width: '110px' }}
                            body={(i) => dinheiro(i.valorTotal)} />
                        <Column header="Situação" style={{ width: '200px' }}
                            body={situacaoBody} />
                    </DataTable>
                </>
            )}
        </Dialog>
    );
};

export default ImportarNotaXml;
