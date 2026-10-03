import axios from 'axios';
import ApiConfig from './ApiConfig';

const base = () => `${ApiConfig.BASE_URL || ''}`;

/**
 * Servico do PDV.
 *
 * REGRA QUE ESTE ARQUIVO NAO QUEBRA: o carrinho nao soma nada. Este arquivo
 * so transporta. Todo numero da tela — subtotal, desconto de linha, desconto
 * do pedido, desconto total, total — vem do backend, no corpo da resposta.
 * Se alguem precisar calcular algo aqui, o bug do desconto contado duas vezes
 * volta. Ver docs/PDV-FLOW.md, secao 1.
 */
class PdvService {

  /**
   * Pesquisa de produto. O backend filtra por nome OU por codigo exato, e
   * pelos dois ao mesmo tempo: a tela manda o texto no campo `nome` e, se
   * parecer codigo, tambem no `codigo`. O nome e' parcial e case-insensitive;
   * o codigo e' igualdade, entao manda-lo errado simplesmente nao acha nada.
   */
  async buscarProdutos(termo, page = 0, size = 20) {
    const params = { page, size, ativo: true };
    const limpo = (termo || '').trim();
    if (limpo) {
      params.nome = limpo;
      // so Numbers.isNaN passa: codigo de barra e' so digito, "SUP-001" nao e
      if (/^\d+$/.test(limpo)) params.codigo = limpo;
    }
    const { data } = await axios.get(`${base()}/api/cadastro/produtos`, { params });
    return data?.data?.content || [];
  }

  /** Cliente para o select do carrinho. "Consumidor nao identificado" e' o padrao. */
  async listarClientes(page = 0, size = 50) {
    const { data } = await axios.get(`${base()}/api/cadastro/clientes`, {
      params: { page, size },
    });
    return data?.data?.content || [];
  }

  /**
   * Saldo por produto. Vem em uma chamada so, com a lista de ids, para a tela
   * nao fazer um GET por linha do carrinho. Se o endpoint nao aceitar o
   * filtro, cai para uma chamada por produto — a tela funciona igual, so que
   * mais lenta.
   */
  async saldos(produtoIds) {
    if (!produtoIds?.length) return {};
    try {
      const { data } = await axios.get(`${base()}/api/estoque/saldos`, {
        params: { produtoIds: produtoIds.join(',') },
      });
      const lista = data?.data;
      const arr = Array.isArray(lista) ? lista : lista?.content || [];
      if (arr.length) {
        return arr.reduce((acc, s) => {
          acc[s.produtoId] = Number(s.quantidade ?? 0);
          return acc;
        }, {});
      }
      throw new Error('vazio');
    } catch {
      const entradas = await Promise.all(produtoIds.map(async (id) => {
        try {
          const { data: d } = await axios.get(`${base()}/api/estoque/saldos/${id}`);
          return [id, Number(d?.data?.quantidade ?? 0)];
        } catch {
          return [id, 0];
        }
      }));
      return Object.fromEntries(entradas);
    }
  }

  /**
   * Cria o pedido. O corpo carrega quantidade, preco unitario e os descontos;
   * a RESPOSTA carrega a conta. `itens[].valorDesconto` e' o desconto da
   * linha; `valorDesconto` OU `percentualDesconto` (nunca os dois) e' o do
   * pedido. Manda-se tambem o valor que o caixa digitou, porque o backend nao
   * recalcula a partir do percentual.
   */
  async criarPedido({ empresaId, clienteId, vendedorId, condicaoPagamentoId, valorDesconto, percentualDesconto, itens }) {
    const corpo = {
      empresaId,
      clienteId,
      tipo: 'PEDIDO',
      status: 'ABERTO',
      canalVenda: 'PDV',
      origem: 'PDV',
      dataEmissao: new Date().toISOString().slice(0, 10),
      condicaoPagamentoId: condicaoPagamentoId || null,
      itens: itens.map((i) => ({
        numeroItem: i.numeroItem,
        produtoId: i.produtoId ?? null,
        servicoId: i.servicoId ?? null,
        descricao: i.descricao,
        quantidade: i.quantidade,
        unidade: i.unidade,
        valorUnitario: i.valorUnitario,
        valorDesconto: i.valorDesconto || 0,
      })),
    };
    if (vendedorId) corpo.vendedorId = vendedorId;
    if (valorDesconto > 0) corpo.valorDesconto = valorDesconto;
    if (percentualDesconto > 0) corpo.percentualDesconto = percentualDesconto;

    const { data } = await axios.post(`${base()}/api/vendas/pedidos`, corpo);
    return data?.data || data;
  }

  /**
   * Formas de pagamento da empresa. A tela escolhe desta lista e envia o
   * `id`; o PDV nunca assume uma forma padrao, porque o que existe depende
   * do segmento (a farmacia tem CONVENIO, o armazem tem BOLETO).
   */
  async listarFormasPagamento() {
    const { data } = await axios.get(`${base()}/api/financeiro/tipos-pagamento`);
    return Array.isArray(data) ? data : data?.data || [];
  }

  /**
   * Baixa PARCIAL. O backend aceita varias baixas no mesmo titulo: o
   * `valorSaldo` vai descendo e o status so vira BAIXADO quando chega a zero.
   * Uma baixa que passe do saldo e' recusada com 422.
   *
   * `valorBaixa` e' o valor DESTA forma, nao o total. A tela envia o que o
   * caixa digitou neste pagamento; quem garante que a soma das formas fecha
   * com o total da venda e' o backend, no ultimo pagamento.
   */
  async baixarTituloParcial(tituloId, { valorBaixa, tipoPagamentoId, dataBaixa }) {
    const corpo = {
      valorBaixa,
      tipoPagamentoId: tipoPagamentoId || null,
      dataBaixa: dataBaixa || new Date().toISOString().slice(0, 10),
    };
    const { data } = await axios.post(`${base()}/api/financeiro/titulos/${tituloId}/baixar`, corpo);
    return data?.data || data;
  }

  /** Parcelas do titulo: o fechamento parcial precisa saber qual esta aberta. */
  async listarParcelas(tituloId) {
    const { data } = await axios.get(`${base()}/api/financeiro/titulos/${tituloId}/parcelas`);
    return data?.data || data || [];
  }

  /** Fatura: baixa estoque, gera titulo com parcelas e comissao. */
  async faturar(pedidoId) {
    const { data } = await axios.post(`${base()}/api/vendas/pedidos/${pedidoId}/faturar`);
    return data?.data || data;
  }

  async buscarPedido(id) {
    const { data } = await axios.get(`${base()}/api/vendas/pedidos/${id}`);
    return data?.data || data;
  }
}

export default new PdvService();
