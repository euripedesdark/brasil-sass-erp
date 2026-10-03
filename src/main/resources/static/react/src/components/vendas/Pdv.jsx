import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Dropdown } from 'primereact/dropdown';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dialog } from 'primereact/dialog';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { useAuth } from '../../contexts/AuthContext';
import PdvService from '../../services/PdvService';

import './Pdv.css';

const ZERO = 0;
const SEM_CLIENTE = { id: null, label: 'Consumidor não identificado' };

/** Só para exibição. O total NUNCA sai daqui — vem do backend. */
const brl = (v) =>
  (Number(v) || ZERO).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

/**
 * PDV — Fase 2A.
 *
 * Pesquisa, carrinho, subtotal, desconto, total e validação de estoque.
 * Fechamento avançado, formas múltiplas de pagamento, TEF, comanda e delivery
 * não entram aqui (docs/PDV-FLOW.md, seção 5).
 *
 * O que esta tela NÃO faz: somar. Não há reduce, não há multiplicação para
 * descobrir o total. Os cinco valores exibidos no rodapé são exatamente os
 * cinco que a API devolve. Se um dia o total da tela divergir do banco, é
 * porque alguémcalculou aqui.
 */
export const Pdv = () => {
  const { user } = useAuth();
  const empresaId = user?.empresaId;

  const [termo, setTermo] = useState('');
  const [resultados, setResultados] = useState([]);
  const [buscando, setBuscando] = useState(false);
  const [semResultado, setSemResultado] = useState(false);

  const [carrinho, setCarrinho] = useState([]);
  const [saldo, setSaldo] = useState({});
  const [clientes, setClientes] = useState([SEM_CLIENTE]);
  const [cliente, setCliente] = useState(SEM_CLIENTE);
  const [vendedor, setVendedor] = useState(null);

  const [descontoPedido, setDescontoPedido] = useState(0);
  const [descontoPedidoPct, setDescontoPedidoPct] = useState(0);
  const [descontoItemEditando, setDescontoItemEditando] = useState(null);
  const [valorDescontoItem, setValorDescontoItem] = useState(0);

  const [formas, setFormas] = useState([]);
  const [formaEscolhida, setFormaEscolhida] = useState(null);
  // Uma venda pode ser paga em varias formas. Cada linha e' uma baixa separada
  // no titulo. `pago` e' a soma que a tela mantem para mostrar o que falta
  // — o saldo autoritativo e' o do backend, lido de cada resposta.
  const [pagamentos, setPagamentos] = useState([]);
  const [valorParcela, setValorParcela] = useState(0);
  const [showFechamento, setShowFechamento] = useState(false);
  const [fechando, setFechando] = useState(false);
  const [vendaFechada, setVendaFechada] = useState(null);

  const [erro, setErro] = useState('');
  const [faturando, setFaturando] = useState(false);
  const [concluido, setConcluido] = useState(null);
  const [showConcluido, setShowConcluido] = useState(false);

  const debounce = useRef(null);

  // ---------------------------------------------------------------- pesquisa
  const pesquisar = useCallback(async (texto) => {
    if (!empresaId) return;
    setBuscando(true);
    try {
      const lista = await PdvService.buscarProdutos(texto, 0, 20);
      setResultados(lista);
      setSemResultado(lista.length === 0);
    } catch (e) {
      setResultados([]);
      setSemResultado(false);
      setErro('Falha ao buscar produtos: ' + (e?.message || 'erro desconhecido'));
    } finally {
      setBuscando(false);
    }
  }, [empresaId]);

  useEffect(() => {
    clearTimeout(debounce.current);
    if (!termo.trim()) {
      setResultados([]);
      setSemResultado(false);
      return;
    }
    // 250 ms: sem isso, cada tecla vira uma chamada; com isso, vira uma por pausa.
    debounce.current = setTimeout(() => pesquisar(termo), 250);
    return () => clearTimeout(debounce.current);
  }, [termo, pesquisar]);

  const buscarClientes = useCallback(async () => {
    try {
      const lista = await PdvService.listarClientes(0, 50);
      setClientes([SEM_CLIENTE, ...lista.map((c) => ({
        id: c.id,
        label: c.nome || `Cliente ${c.id}`,
        limite: Number(c.limiteCredito || ZERO),
        raw: c,
      }))]);
    } catch {
      setClientes([SEM_CLIENTE]);
    }
  }, []);

  const carregarFormas = useCallback(async () => {
    try {
      const lista = await PdvService.listarFormasPagamento();
      const ativos = lista.filter((f) => f.ativo !== false);
      setFormas(ativos);
      // Nao escolhe nenhuma: o caixa escolhe. Assumir a primeira seria o
      // frontend decidindo forma de pagamento por conta propria.
      setFormaEscolhida(null);
    } catch {
      setFormas([]);
      setFormaEscolhida(null);
    }
  }, []);

  useEffect(() => {
    if (empresaId) {
      buscarClientes();
      carregarFormas();
      pesquisar('');
    }
  }, [empresaId, buscarClientes, carregarFormas, pesquisar]);

  // ---------------------------------------------------------------- carrinho
  const carregarSaldos = useCallback(async (ids) => {
    if (!ids.length) return;
    try {
      const s = await PdvService.saldos(ids);
      setSaldo((atual) => ({ ...atual, ...s }));
    } catch {
      /* sem saldo conhecido: a linha fica sem selo e o backend decide no faturar */
    }
  }, []);

  const adicionar = async (produto) => {
    setErro('');
    setCarrinho((atual) => {
      const existente = atual.find(
        (i) => i.produtoId === produto.id && i.unidade === (produto.unidadeSigla || 'UN')
      );
      const proximo = existente
        ? atual.map((i) =>
            i === existente
              ? { ...i, quantidade: Number((Number(i.quantidade) + 1).toFixed(3)) }
              : i
          )
        : [...atual, {
            produtoId: produto.id,
            servicoId: null,
            descricao: produto.nome,
            codigo: produto.codigo,
            quantidade: 1,
            unidade: produto.unidadeSigla || 'UN',
            valorUnitario: Number(produto.precoVenda || ZERO),
            valorDesconto: ZERO,
            numeroItem: atual.length + 1,
          }];
      carregarSaldos(proximo.map((i) => i.produtoId).filter(Boolean));
      return proximo;
    });
  };

  const alterarQuantidade = (produtoId, quantidade) => {
    const q = Number(quantidade);
    setCarrinho((atual) => {
      if (!q || q <= 0) return atual.filter((i) => i.produtoId !== produtoId);
      return atual.map((i) =>
        i.produtoId === produtoId ? { ...i, quantidade: Number(q.toFixed(3)) } : i
      );
    });
  };

  const remover = (produtoId) =>
    setCarrinho((atual) =>
      atual
        .filter((i) => i.produtoId !== produtoId)
        .map((i, idx) => ({ ...i, numeroItem: idx + 1 }))
    );

  const abrirDescontoItem = (linha) => {
    setDescontoItemEditando(linha);
    setValorDescontoItem(Number(linha.valorDesconto || ZERO));
  };

  const aplicarDescontoItem = () => {
    const linha = descontoItemEditando;
    if (!linha) return;
    const bruto = Number(linha.quantidade) * Number(linha.valorUnitario);
    const v = Number(valorDescontoItem) || ZERO;
    if (v < 0) {
      setErro('O desconto não pode ser negativo.');
      return;
    }
    if (v > bruto) {
      setErro(`O desconto (${brl(v)}) é maior que a linha (${brl(bruto)}).`);
      return;
    }
    setCarrinho((atual) =>
      atual.map((i) => (i.produtoId === linha.produtoId ? { ...i, valorDesconto: v } : i))
    );
    setDescontoItemEditando(null);
    setErro('');
  };

  // ------------------------------------------------------------------ totais
  // Só o texto do rodapé, para o caixa enxergar o efeito do desconto do pedido
  // antes de finalizar. O TOTAL exibido é o do backend, nunca este número.
  const rotuloDescontoPedido =
    descontoPedidoPct > 0
      ? `${descontoPedidoPct}% aplicado pelo backend`
      : descontoPedido > 0
        ? `${brl(descontoPedido)} aplicado pelo backend`
        : null;

  // ---------------------------------------------------------------- finalizar
  const finalizar = async () => {
    if (!carrinho.length || faturando) return;
    setErro('');
    setFaturando(true);
    try {
      const pedido = await PdvService.criarPedido({
        empresaId,
        clienteId: cliente?.id,
        vendedorId: vendedor?.id,
        condicaoPagamentoId: cliente?.raw?.condicaoPagamentoId,
        valorDesconto: descontoPedidoPct > 0 ? ZERO : descontoPedido,
        percentualDesconto: descontoPedidoPct > 0 ? descontoPedidoPct : ZERO,
        itens: carrinho,
      });
      await PdvService.faturar(pedido.id);
      const final = await PdvService.buscarPedido(pedido.id);
      setConcluido(final);
      // A venda esta criada, faturada e com titulo. Falta o dinheiro:
      // e o que o passo de fechamento registra.
      setVendaFechada(final);
      setShowFechamento(true);
      setCarrinho([]);
      setSaldo({});
      setDescontoPedido(0);
      setDescontoPedidoPct(0);
      setTermo('');
      setResultados([]);
    } catch (e) {
      // A mensagem do backend vai palavra por palavra. "Estoque insuficiente
      // para o produto X" é informação, não erro de digitação.
      setErro(e?.response?.data?.errors?.[0]?.message || e?.message || 'Não foi possível finalizar a venda.');
    } finally {
      setFaturando(false);
    }
  };

  /**
   * Fechamento com varias formas: cada linha e' uma baixa no titulo.
   *
   * O backend aceita baixa parcial — o `valorSaldo` desce a cada pagamento e o
   * status so vira BAIXADO quando chega a zero. A tela nao calcula o total da
   * venda, mas precisa saber quanto ja foi pago para mostrar "quanto falta".
   * Esse "quanto falta" vem do `saldoRestante` que o backend devolve em cada
   * baixa, nunca de uma conta da tela.
   */
  const registrarPagamento = async () => {
    const tituloId = vendaFechada?.tituloId;
    if (!tituloId || fechando) {
      setErro('A venda não gerou título. Não há o que baixar.');
      return;
    }
    if (!formaEscolhida) {
      setErro('Escolha a forma de pagamento.');
      return;
    }
    const valor = Number(valorParcela) || 0;
    if (valor <= 0) {
      setErro('O valor do pagamento deve ser maior que zero.');
      return;
    }

    setErro('');
    setFechando(true);
    try {
      const baixa = await PdvService.baixarTituloParcial(tituloId, {
        valorBaixa: valor,
        tipoPagamentoId: formaEscolhida,
      });
      // saldoRestante veio do servidor. Guardamos ele, nao recalculamos.
      const saldoRestante = Number(baixa?.saldoRestante ?? 0);
      const quitado = baixa?.statusTitulo === 'BAIXADO' || saldoRestante === 0;

      setPagamentos((atual) => [
        ...atual,
        {
          forma: formas.find((f) => f.id === formaEscolhida)?.descricao || 'Forma',
          valor,
          saldoRestante,
        },
      ]);
      setValorParcela(0);
      setVendaFechada((v) => ({ ...v, baixa, quitado }));
      if (quitado) {
        setShowFechamento(false);
        setShowConcluido(true);
      }
    } catch (e) {
      setErro(
        e?.response?.data?.errors?.[0]?.message ||
          e?.message ||
          'Não foi possível registrar o pagamento.'
      );
    } finally {
      setFechando(false);
    }
  };

  /** Encerra o pagamento e deixa o título como está. */
  const adiarFechamento = () => {
    setShowFechamento(false);
    setShowConcluido(true);
  };

  // ------------------------------------------------------------------- corpo
  const produtoTemplate = (p) => (
    <div className="pdv-produto">
      <span className="pdv-produto__nome">{p.nome}</span>
      <span className="pdv-produto__meta">
        {p.codigo} · {p.unidadeSigla || 'UN'}
      </span>
    </div>
  );

  const precoTemplate = (p) => <span className="pdv-preco">{brl(p.precoVenda)}</span>;

  const acaoTemplate = (p) => {
    const qtd = Number(saldo[p.id]);
    const zerado = qtd === 0;
    return (
      <Button
        icon="pi pi-plus"
        size="small"
        outlined
        severity={zerado ? 'warning' : 'secondary'}
        aria-label={`Adicionar ${p.nome}`}
        onClick={() => adicionar(p)}
        tooltip={zerado ? 'Sem estoque — o faturamento vai recusar' : 'Adicionar ao carrinho'}
        tooltipOptions={{ position: 'left' }}
      />
    );
  };

  const qtdTemplate = (linha) => (
    <InputNumber
      value={linha.quantidade}
      onValueChange={(e) => alterarQuantidade(linha.produtoId, e.value)}
      minFractionDigits={0}
      maxFractionDigits={3}
      inputStyle={{ width: '5.5rem' }}
      inputId={`pdv-qtd-${linha.produtoId}`}
      aria-label={`Quantidade de ${linha.descricao}`}
    />
  );

  const unitTemplate = (linha) => brl(linha.valorUnitario);

  const descontoTemplate = (linha) => (
    <div className="pdv-desconto">
      <span>{brl(linha.valorDesconto)}</span>
      <Button
        icon="pi pi-pencil"
        size="small"
        text
        rounded
        aria-label={`Desconto de ${linha.descricao}`}
        onClick={() => abrirDescontoItem(linha)}
      />
    </div>
  );

  const acaoLinhaTemplate = (linha) => (
    <Button
      icon="pi pi-trash"
      size="small"
      text
      severity="danger"
      rounded
      aria-label={`Remover ${linha.descricao}`}
      onClick={() => remover(linha.produtoId)}
    />
  );

  const rodapeVenda = (linha) => {
    const qtd = Number(saldo[linha.produtoId]);
    if (qtd === undefined) return <span className="pdv-saldo pdv-saldo--nada">—</span>;
    if (qtd === 0) return <Tag value="sem estoque" severity="danger" />;
    if (qtd < Number(linha.quantidade))
      return <Tag value={`só ${brl(qtd)}`} severity="warning" />;
    return <span className="pdv-saldo">{brl(qtd)}</span>;
  };

  // Os cinco valores do rodapé vêm do último pedido devolvido pela API.
  // Enquanto não houver venda, os campos do backend não existem: o rodapé
  // mostra "—" em vez de estimar. Inventar número aqui é o que a regra proíbe.
  const [ultimaConta, setUltimaConta] = useState(null);
  useEffect(() => {
    if (!concluido) return;
    setUltimaConta({
      valorProdutos: concluido.valorProdutos,
      valorDescontoItens: concluido.valorDescontoItens,
      valorDescontoPedido: concluido.valorDescontoPedido,
      valorDescontoTotal: concluido.valorDescontoTotal,
      valorTotal: concluido.valorTotal,
    });
  }, [concluido]);

  return (
    <div className="pdv">
      <header className="pdv-topo">
        <div className="pdv-topo__busca">
          <span className="p-input-icon-left pdv-busca">
            <i className="pi pi-search" />
            <InputText
              value={termo}
              onChange={(e) => setTermo(e.target.value)}
              placeholder="Buscar produto por nome ou código"
              autoComplete="off"
              inputId="pdv-busca"
              aria-label="Buscar produto"
            />
          </span>
        </div>
        <div className="pdv-topo__cliente">
          <Dropdown
            value={cliente}
            options={clientes}
            onChange={(e) => setCliente(e.value)}
            optionLabel="label"
            optionValue="label"
            placeholder="Cliente"
            inputId="pdv-cliente"
            aria-label="Cliente"
            style={{ minWidth: '18rem' }}
          />
          {cliente?.limite > 0 && (
            <small className="pdv-limite">limite {brl(cliente.limite)}</small>
          )}
        </div>
      </header>

      {erro && (
        <Message severity="error" text={erro} className="pdv-erro" role="alert" />
      )}

      <div className="pdv-corpo">
        <section className="pdv-painel pdv-painel--busca" aria-label="Produtos">
          {termo.trim() && semResultado && (
            <p className="pdv-vazio">Nenhum produto encontrado para “{termo.trim()}”.</p>
          )}
          {!termo.trim() && (
            <p className="pdv-vazio">
              Digite para buscar. Produto sem saldo aparece com aviso em vez de sumir:
              esconder da lista deixa o caixa descobrir o problema só no fechamento.
            </p>
          )}
          <DataTable
            value={resultados}
            loading={buscando}
            dataKey="id"
            size="small"
            scrollable
            scrollHeight="flex"
            emptyMessage=" "
            className="pdv-tabela"
          >
            <Column header="Produto" body={produtoTemplate} style={{ minWidth: '16rem' }} />
            <Column header="Preço" body={precoTemplate} style={{ width: '7rem' }} />
            <Column body={acaoTemplate} style={{ width: '3.5rem' }} />
          </DataTable>
        </section>

        <section className="pdv-painel pdv-painel--carrinho" aria-label="Carrinho">
          <DataTable
            value={carrinho}
            dataKey="produtoId"
            size="small"
            emptyMessage="Carrinho vazio. Busque um produto ao lado."
            className="pdv-tabela"
          >
            <Column header="Qtd" body={qtdTemplate} style={{ width: '7rem' }} />
            <Column field="descricao" header="Item" style={{ minWidth: '12rem' }} />
            <Column header="Unit." body={unitTemplate} style={{ width: '6rem' }} />
            <Column header="Desconto" body={descontoTemplate} style={{ width: '8rem' }} />
            <Column header="Disponível" body={rodapeVenda} style={{ width: '7rem' }} />
            <Column body={acaoLinhaTemplate} style={{ width: '3.5rem' }} />
          </DataTable>

          <div className="pdv-desconto-pedido">
            <label htmlFor="pdv-desc-valor">Desconto do pedido</label>
            <InputNumber
              inputId="pdv-desc-valor"
              value={descontoPedidoPct > 0 ? null : descontoPedido}
              onValueChange={(e) => {
                setDescontoPedido(Number(e.value) || ZERO);
                setDescontoPedidoPct(0);
              }}
              mode="currency"
              currency="BRL"
              locale="pt-BR"
              disabled={descontoPedidoPct > 0}
              placeholder="R$ 0,00"
            />
            <span className="pdv-ou">ou</span>
            <InputNumber
              inputId="pdv-desc-pct"
              value={descontoPedidoPct > 0 ? descontoPedidoPct : null}
              onValueChange={(e) => {
                setDescontoPedidoPct(Math.min(100, Math.max(0, Number(e.value) || ZERO)));
                setDescontoPedido(0);
              }}
              suffix="%"
              minFractionDigits={0}
              maxFractionDigits={2}
              disabled={descontoPedido > 0}
              placeholder="0%"
            />
            {rotuloDescontoPedido && (
              <small className="pdv-dica">
                {rotuloDescontoPedido}. O valor em reais é calculado pelo servidor.
              </small>
            )}
          </div>

          <div className="pdv-total">
            <div className="pdv-total__linha">
              <span>Subtotal</span>
              <span>{ultimaConta ? brl(ultimaConta.valorProdutos) : '—'}</span>
            </div>
            <div className="pdv-total__linha">
              <span>Desconto das linhas</span>
              <span>{ultimaConta ? brl(ultimaConta.valorDescontoItens) : '—'}</span>
            </div>
            <div className="pdv-total__linha">
              <span>Desconto do pedido</span>
              <span>{ultimaConta ? brl(ultimaConta.valorDescontoPedido) : '—'}</span>
            </div>
            <div className="pdv-total__linha pdv-total__linha--soma">
              <span>Desconto total</span>
              <span>{ultimaConta ? brl(ultimaConta.valorDescontoTotal) : '—'}</span>
            </div>
            <div className="pdv-total__linha pdv-total__linha--total">
              <span>Total</span>
              <span data-testid="pdv-total">
                {ultimaConta ? brl(ultimaConta.valorTotal) : '—'}
              </span>
            </div>
            <p className="pdv-total__nota">
              Estes valores são os do servidor. Enquanto a venda não for criada
              não há total — a tela não estima.
            </p>
          </div>

          <Button
            label={faturando ? 'Finalizando…' : 'Finalizar venda'}
            icon="pi pi-check"
            onClick={finalizar}
            disabled={!carrinho.length || faturando}
            loading={faturando}
            className="pdv-finalizar"
          />
        </section>
      </div>

      <Dialog
        header="Fechar venda"
        visible={showFechamento}
        onHide={() => !fechando && adiarFechamento()}
        style={{ width: '32rem' }}
        modal
        closable={!fechando}
      >
        {vendaFechada && (
          <>
            <p className="pdv-dialogo-item">
              Pedido {vendaFechada.numero} — total{' '}
              <strong>{brl(vendaFechada.valorTotal)}</strong>
            </p>

            {pagamentos.length > 0 && (
              <div className="pdv-pagamentos">
                <span className="pdv-pagamentos__titulo">Pagamentos</span>
                {pagamentos.map((pg, idx) => (
                  <div key={idx} className="pdv-pagamento">
                    <span>{pg.forma}</span>
                    <span>{brl(pg.valor)}</span>
                  </div>
                ))}
                <div className="pdv-pagamento pdv-pagamento--falta">
                  <span>Falta</span>
                  <span>{brl(pagamentos[pagamentos.length - 1].saldoRestante)}</span>
                </div>
              </div>
            )}

            <label className="pdv-forma-label" htmlFor="pdv-forma">
              Forma deste pagamento
            </label>
            <Dropdown
              inputId="pdv-forma"
              value={formaEscolhida}
              options={formas}
              onChange={(e) => setFormaEscolhida(e.value)}
              optionLabel="descricao"
              optionValue="id"
              placeholder="Selecione a forma"
              style={{ width: '100%' }}
              disabled={fechando}
            />

            <label className="pdv-forma-label" htmlFor="pdv-valor-pag">
              Valor a receber agora
            </label>
            <InputNumber
              inputId="pdv-valor-pag"
              value={valorParcela || null}
              onValueChange={(e) => setValorParcela(e.value)}
              mode="currency"
              currency="BRL"
              locale="pt-BR"
              placeholder={brl(
                pagamentos.length
                  ? pagamentos[pagamentos.length - 1].saldoRestante
                  : vendaFechada.valorTotal
              )}
              inputStyle={{ width: '100%' }}
              disabled={fechando}
            />

            <p className="pdv-dialogo-base pdv-dialogo-base--fechamento">
              Pode receber em quantas formas quiser. O servidor baixa uma de
              cada vez e diz quanto ainda falta; a tela não faz essa conta.
            </p>
            <div className="pdv-dialogo-acoes">
              <Button
                label="Pagar depois"
                text
                onClick={adiarFechamento}
                disabled={fechando}
              />
              <Button
                label={fechando ? 'Registrando…' : 'Registrar pagamento'}
                icon="pi pi-check"
                onClick={registrarPagamento}
                loading={fechando}
                disabled={!formaEscolhida || !(Number(valorParcela) > 0)}
              />
            </div>
          </>
        )}
      </Dialog>

      <Dialog
        header="Desconto da linha"
        visible={!!descontoItemEditando}
        onHide={() => setDescontoItemEditando(null)}
        style={{ width: '24rem' }}
        modal
      >
        {descontoItemEditando && (
          <>
            <p className="pdv-dialogo-item">{descontoItemEditando.descricao}</p>
            <p className="pdv-dialogo-base">
              Linha: {brl(Number(descontoItemEditando.quantidade) * Number(descontoItemEditando.valorUnitario))}
            </p>
            <span className="p-input-icon-left">
              <i className="pi pi-tag" />
              <InputNumber
                value={valorDescontoItem}
                onValueChange={(e) => setValorDescontoItem(e.value)}
                mode="currency"
                currency="BRL"
                locale="pt-BR"
                inputStyle={{ width: '100%' }}
                inputId="pdv-desconto-item"
                aria-label="Valor do desconto da linha"
              />
            </span>
            <div className="pdv-dialogo-acoes">
              <Button
                label="Cancelar"
                text
                onClick={() => setDescontoItemEditando(null)}
              />
              <Button
                label="Aplicar"
                icon="pi pi-check"
                onClick={aplicarDescontoItem}
              />
            </div>
          </>
        )}
      </Dialog>

      <Dialog
        header="Venda concluída"
        visible={showConcluido}
        onHide={() => setShowConcluido(false)}
        style={{ width: '26rem' }}
        modal
      >
        {concluido && (
          <>
            <Message
              severity="success"
              text={
                vendaFechada?.quitado
                  ? `Pedido ${concluido.numero} faturado e pago.`
                  : vendaFechada?.baixa
                    ? `Pedido ${concluido.numero} faturado, pagamento parcial.`
                    : `Pedido ${concluido.numero} faturado. Pagamento pendente.`
              }
              role="status"
            />
            <div className="pdv-concluido">
              <div><span>Subtotal</span><span>{brl(concluido.valorProdutos)}</span></div>
              <div><span>Desconto das linhas</span><span>{brl(concluido.valorDescontoItens)}</span></div>
              <div><span>Desconto do pedido</span><span>{brl(concluido.valorDescontoPedido)}</span></div>
              <div><span>Desconto total</span><span>{brl(concluido.valorDescontoTotal)}</span></div>
              <div className="pdv-concluido__total"><span>Total</span><span>{brl(concluido.valorTotal)}</span></div>
            </div>
            <Button
              label="Nova venda"
              icon="pi pi-plus"
              onClick={() => setShowConcluido(false)}
            />
          </>
        )}
      </Dialog>
    </div>
  );
};

export default Pdv;
