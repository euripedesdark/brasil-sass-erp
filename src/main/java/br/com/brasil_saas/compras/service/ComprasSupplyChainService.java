package br.com.brasil_saas.compras.service;
import br.com.brasil_saas.compras.model.*;
import java.math.BigDecimal;
import java.util.*;
public interface ComprasSupplyChainService {
 List<SolicitacaoCompra> listarSolicitacoes(Long empresaId);
 SolicitacaoCompra criarSolicitacao(Long empresaId,Long solicitanteId,LocalRequest request);
 SolicitacaoCompra aprovarSolicitacao(Long empresaId,Long userId,Long id);
 SolicitacaoCompra rejeitarSolicitacao(Long empresaId,Long userId,Long id);
 CotacaoCompra criarCotacao(Long empresaId,Long solicitacaoId,LocalCotacao request);
 List<Map<String,Object>> mapaComparativo(Long empresaId,Long cotacaoId);
 PedidoCompra gerarPedido(Long empresaId,Long cotacaoFornecedorId);
 record LocalItem(Long produtoId,BigDecimal quantidade,String observacao){}
 record LocalRequest(String numero,java.time.LocalDate dataNecessidade,String observacao,List<LocalItem> itens){}
 record LocalFornecedor(Long fornecedorId,Integer prazoEntrega,Long condicaoPagamentoId,BigDecimal frete,BigDecimal desconto,List<LocalItemPreco> itens){}
 record LocalItemPreco(Long produtoId,BigDecimal quantidade,BigDecimal valorUnitario){}
 record LocalCotacao(String numero,java.time.LocalDate dataLimite,String observacao,List<LocalFornecedor> fornecedores){}
}