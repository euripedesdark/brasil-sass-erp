package br.com.brasil_saas.compras.service.impl;
import br.com.brasil_saas.compras.model.*;
import br.com.brasil_saas.compras.repository.*;
import br.com.brasil_saas.compras.service.ComprasSupplyChainService;
import br.com.brasil_saas.shared.exception.*;
import br.com.brasil_saas.workflow.model.WkfDefinition;import br.com.brasil_saas.workflow.model.WkfInstance;import br.com.brasil_saas.workflow.model.WkfStage;import br.com.brasil_saas.workflow.model.WkfTask;import br.com.brasil_saas.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor
public class ComprasSupplyChainServiceImpl implements ComprasSupplyChainService {
 private final SolicitacaoCompraRepository solicitacaoRepo; private final CotacaoCompraRepository cotacaoRepo;
 private final CotacaoFornecedorRepository fornecedorRepo; private final CotacaoFornecedorItemRepository itemRepo;
 private final PedidoCompraRepository pedidoRepo;
 private final WorkflowService workflow;
 public List<SolicitacaoCompra> listarSolicitacoes(Long e){return solicitacaoRepo.findByEmpresaIdAndDeletedAtIsNullOrderByDataSolicitacaoDesc(e);}
 @Transactional public SolicitacaoCompra criarSolicitacao(Long e,Long u,LocalRequest r){
  if(r.itens()==null||r.itens().isEmpty())throw new BusinessException("A solicitação precisa ter ao menos um item");
  SolicitacaoCompra s=new SolicitacaoCompra();s.setEmpresaId(e);s.setSolicitanteId(u);s.setNumero(r.numero()!=null&&!r.numero().isBlank()?r.numero():"SC-"+System.currentTimeMillis());s.setStatus("PENDENTE_APROVACAO");s.setDataNecessidade(r.dataNecessidade());s.setObservacao(r.observacao());
  List<SolicitacaoCompraItem> is=new ArrayList<>();for(var x:r.itens()){if(x.produtoId()==null||x.quantidade()==null||x.quantidade().signum()<=0)throw new BusinessException("Produto e quantidade são obrigatórios");SolicitacaoCompraItem i=new SolicitacaoCompraItem();i.setSolicitacao(s);i.setEmpresaId(e);i.setProdutoId(x.produtoId());i.setQuantidade(x.quantidade());i.setObservacao(x.observacao());is.add(i);}s.setItens(is);return solicitacaoRepo.save(s);
 }
 @Transactional public SolicitacaoCompra aprovarSolicitacao(Long e,Long u,Long id){return decidirSolicitacao(e,u,id,true);} @Transactional public SolicitacaoCompra rejeitarSolicitacao(Long e,Long u,Long id){return decidirSolicitacao(e,u,id,false);} private SolicitacaoCompra decidirSolicitacao(Long e,Long u,Long id,boolean aprovar){SolicitacaoCompra s=solicitacaoRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,e).orElseThrow(()->new ResourceNotFoundException("Solicitação não encontrada"));if(!"PENDENTE_APROVACAO".equals(s.getStatus()))throw new BusinessException("Somente pendentes podem ser decididas");WkfDefinition d=workflow.definitions(e).stream().filter(x->"SOLICITACAO_COMPRA".equals(x.getEntidadeAlvo())&&Boolean.TRUE.equals(x.getAtivo())).findFirst().orElseGet(()->{WkfDefinition n=new WkfDefinition();n.setCodigo("SC-APROVACAO");n.setNome("Aprovacao de solicitacao de compra");n.setEntidadeAlvo("SOLICITACAO_COMPRA");n.setAtivo(true);n=workflow.salvarDefinition(e,u,n);WkfStage st=new WkfStage();st.setNome("Aprovacao");st.setTipo("APROVACAO");st.setSlaHoras(48);workflow.addStage(e,n.getId(),st);return n;});WkfInstance inst=workflow.instanciaPara(e,"SOLICITACAO_COMPRA",id).filter(x->"EM_ANDAMENTO".equals(x.getStatus())).orElseGet(()->workflow.abrir(e,u,"SOLICITACAO_COMPRA",id,d.getId(),s.getNumero()));java.util.List<WkfTask> pend=workflow.tasks(e,inst.getId()).stream().filter(x->"PENDENTE".equals(x.getStatus())).toList();if(pend.isEmpty())throw new BusinessException("Sem tarefa pendente no workflow");workflow.decidir(e,u,pend.get(0).getId(),aprovar,null);WkfInstance atual=workflow.instanciaPara(e,"SOLICITACAO_COMPRA",id).orElse(inst);if("REJEITADA".equals(atual.getStatus())){s.setStatus("REJEITADA");}else if("CONCLUIDA".equals(atual.getStatus())){s.setStatus("APROVADA");}return solicitacaoRepo.save(s);}
 @Transactional public CotacaoCompra criarCotacao(Long e,Long sid,LocalCotacao r){
  SolicitacaoCompra s=solicitacaoRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(sid,e).orElseThrow(()->new ResourceNotFoundException("Solicitação não encontrada"));if(!"APROVADA".equals(s.getStatus()))throw new BusinessException("A solicitação precisa estar APROVADA");if(r.fornecedores()==null||r.fornecedores().isEmpty())throw new BusinessException("Informe ao menos um fornecedor");
  CotacaoCompra c=new CotacaoCompra();c.setEmpresaId(e);c.setSolicitacaoId(sid);c.setNumero(r.numero()!=null&&!r.numero().isBlank()?r.numero():"COT-"+System.currentTimeMillis());c.setStatus("ABERTA");c.setDataLimite(r.dataLimite());c.setObservacao(r.observacao());c=cotacaoRepo.save(c);
  for(var f:r.fornecedores()){CotacaoFornecedor cf=new CotacaoFornecedor();cf.setEmpresaId(e);cf.setCotacaoId(c.getId());cf.setFornecedorId(f.fornecedorId());cf.setPrazoEntrega(f.prazoEntrega());cf.setCondicaoPagamentoId(f.condicaoPagamentoId());cf.setFrete(nz(f.frete()));cf.setDesconto(nz(f.desconto()));BigDecimal total=BigDecimal.ZERO;List<CotacaoFornecedorItem> cis=new ArrayList<>();if(f.itens()!=null)for(var x:f.itens()){if(x.produtoId()==null||x.quantidade()==null||x.valorUnitario()==null)throw new BusinessException("Item de cotação inválido");BigDecimal v=x.quantidade().multiply(x.valorUnitario());total=total.add(v);CotacaoFornecedorItem ci=new CotacaoFornecedorItem();ci.setEmpresaId(e);ci.setProdutoId(x.produtoId());ci.setQuantidade(x.quantidade());ci.setValorUnitario(x.valorUnitario());ci.setValorTotal(v);cis.add(ci);}cf.setValorTotal(total.subtract(nz(f.desconto())).add(nz(f.frete())));cf=fornecedorRepo.save(cf);for(var ci:cis)ci.setCotacaoFornecedorId(cf.getId());itemRepo.saveAll(cis);}
  s.setStatus("COTACAO");solicitacaoRepo.save(s);return c;
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> mapaComparativo(Long e,Long cid){ return mapaComparativo(e, cid, 70, 20, 10); }
 public List<Map<String,Object>> mapaComparativo(Long e,Long cid,int pesoPreco,int pesoPrazo,int pesoDesconto){cotacaoRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(cid,e).orElseThrow(()->new ResourceNotFoundException("Cotação não encontrada"));List<Map<String,Object>> out=new ArrayList<>();for(var f:fornecedorRepo.findByEmpresaIdAndCotacaoIdAndDeletedAtIsNull(e,cid)){Map<String,Object> m=new LinkedHashMap<>();m.put("cotacaoFornecedorId",f.getId());m.put("fornecedorId",f.getFornecedorId());m.put("status",f.getStatus());m.put("prazoEntrega",f.getPrazoEntrega());m.put("condicaoPagamentoId",f.getCondicaoPagamentoId());m.put("frete",f.getFrete());m.put("desconto",f.getDesconto());m.put("valorTotal",f.getValorTotal());m.put("itens",itemRepo.findByEmpresaIdAndCotacaoFornecedorIdAndDeletedAtIsNull(e,f.getId()));out.add(m);}
    int soma = Math.max(1, pesoPreco + pesoPrazo + pesoDesconto);
    java.math.BigDecimal menorTotal = out.stream().map(m -> (java.math.BigDecimal) m.getOrDefault("valorTotal", java.math.BigDecimal.ZERO)).min(java.util.Comparator.naturalOrder()).orElse(java.math.BigDecimal.ZERO);
    int menorPrazo = out.stream().mapToInt(m -> ((Number) m.getOrDefault("prazoEntrega", 0)).intValue()).min().orElse(0);
    java.math.BigDecimal maiorDesc = out.stream().map(m -> (java.math.BigDecimal) m.getOrDefault("desconto", java.math.BigDecimal.ZERO)).max(java.util.Comparator.naturalOrder()).orElse(java.math.BigDecimal.ZERO);
    String vencedor = null; double melhor = -1;
    for (Map<String,Object> m : out) {
        java.math.BigDecimal tot = (java.math.BigDecimal) m.getOrDefault("valorTotal", java.math.BigDecimal.ZERO);
        double nPreco = (menorTotal.signum() <= 0 || tot.signum() <= 0) ? 100.0 : menorTotal.doubleValue() / tot.doubleValue() * 100.0;
        int prazo = ((Number) m.getOrDefault("prazoEntrega", 0)).intValue();
        double nPrazo = (prazo <= 0 || menorPrazo <= 0) ? 100.0 : (double) menorPrazo / prazo * 100.0;
        java.math.BigDecimal desc = (java.math.BigDecimal) m.getOrDefault("desconto", java.math.BigDecimal.ZERO);
        double nDesc = (maiorDesc.signum() <= 0) ? 100.0 : desc.doubleValue() / maiorDesc.doubleValue() * 100.0;
        double score = (nPreco * pesoPreco + nPrazo * pesoPrazo + nDesc * pesoDesconto) / soma;
        score = Math.round(score * 10.0) / 10.0;
        m.put("pontuacao", score);
        if (score > melhor) { melhor = score; vencedor = String.valueOf(m.get("cotacaoFornecedorId")); }
    }
    for (Map<String,Object> m : out) m.put("vencedor", String.valueOf(m.get("cotacaoFornecedorId")).equals(vencedor));
    return out;}
 @Transactional public PedidoCompra gerarPedido(Long e,Long fid){CotacaoFornecedor f=fornecedorRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(fid,e).orElseThrow(()->new ResourceNotFoundException("Cotação do fornecedor não encontrada"));if("SELECIONADA".equals(f.getStatus()))throw new BusinessException("Esta cotação já gerou pedido");List<CotacaoFornecedorItem> xs=itemRepo.findByEmpresaIdAndCotacaoFornecedorIdAndDeletedAtIsNull(e,fid);if(xs.isEmpty())throw new BusinessException("Cotação sem itens");PedidoCompra p=new PedidoCompra();p.setEmpresaId(e);p.setFornecedorId(f.getFornecedorId());p.setNumero("PC-"+System.currentTimeMillis());p.setStatus("ABERTO");p.setDataEmissao(LocalDate.now());p.setCondicaoPagamentoId(f.getCondicaoPagamentoId());p.setValorFrete(nz(f.getFrete()));p.setValorDesconto(nz(f.getDesconto()));BigDecimal total=BigDecimal.ZERO;List<ItemPedidoCompra> pis=new ArrayList<>();int n=1;for(var x:xs){ItemPedidoCompra pi=new ItemPedidoCompra();pi.setPedido(p);pi.setEmpresaId(e);pi.setNumeroItem(n++);pi.setProdutoId(x.getProdutoId());pi.setQuantidade(x.getQuantidade());pi.setValorUnitario(x.getValorUnitario());pi.setValorDesconto(BigDecimal.ZERO);pi.setValorTotal(x.getValorTotal());pi.setDescricao("Item da cotação "+fid);pi.setCriadoEstoque(false);pis.add(pi);total=total.add(x.getValorTotal());}p.setItens(pis);p.setValorProdutos(total);p.setValorTotal(total.subtract(p.getValorDesconto()).add(p.getValorFrete()));p=pedidoRepo.save(p);f.setStatus("SELECIONADA");fornecedorRepo.save(f);return p;}
 private BigDecimal nz(BigDecimal x){return x==null?BigDecimal.ZERO:x;}
}