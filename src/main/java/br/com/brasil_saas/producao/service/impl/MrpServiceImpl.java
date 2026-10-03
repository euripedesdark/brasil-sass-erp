package br.com.brasil_saas.producao.service.impl;

import br.com.brasil_saas.producao.model.EstruturaProduto;
import br.com.brasil_saas.producao.repository.EstruturaProdutoRepository;
import br.com.brasil_saas.producao.service.MrpRequest;
import br.com.brasil_saas.producao.service.MrpService;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MrpServiceImpl implements MrpService {
 private final EstruturaProdutoRepository estrutura;
 private final SaldoEstoqueRepository saldo;
 @Override public List<Map<String,Object>> simular(Long empresaId, MrpRequest request){
  if(request==null||request.produtoId()==null||request.quantidade()==null||request.quantidade().signum()<=0)
   throw new BusinessException("Produto e quantidade positiva são obrigatórios");
  Map<Long,BigDecimal> necessidades=new LinkedHashMap<>();
  explodir(empresaId,request.produtoId(),request.quantidade(),new HashSet<>(),necessidades);
  List<Map<String,Object>> out=new ArrayList<>();
  necessidades.forEach((produtoId,necessaria)->{
   BigDecimal disponivel=saldo.findByEmpresaIdAndProdutoId(empresaId,produtoId).map(s->s.getQuantidade()).orElse(BigDecimal.ZERO);
   BigDecimal liquida=necessaria.subtract(disponivel).max(BigDecimal.ZERO);
   Map<String,Object> row=new LinkedHashMap<>();
   row.put("produtoId",produtoId); row.put("necessidadeBruta",necessaria);
   row.put("estoqueDisponivel",disponivel); row.put("necessidadeLiquida",liquida);
   row.put("acao",liquida.signum()==0?"SEM_ACAO":"COMPRAR_OU_PRODUZIR");
   out.add(row);
  });
  return out;
 }
 private void explodir(Long empresaId,Long produtoId,BigDecimal qtd,Set<Long> caminho,Map<Long,BigDecimal> necessidades){
  if(!caminho.add(produtoId)) throw new BusinessException("Ciclo detectado na BOM envolvendo produto "+produtoId);
  List<EstruturaProduto> filhos=estrutura.findByEmpresaIdAndProdutoPaiIdAndDeletedAtIsNullOrderByNivelAscIdAsc(empresaId,produtoId);
  if(filhos.isEmpty()) necessidades.merge(produtoId,qtd,BigDecimal::add);
  else for(EstruturaProduto e:filhos) if(Boolean.TRUE.equals(e.getAtivo())){
   BigDecimal fator=e.getQuantidade().multiply(BigDecimal.ONE.add(e.getPerdaPercentual().divide(BigDecimal.valueOf(100))));
   explodir(empresaId,e.getProdutoFilhoId(),qtd.multiply(fator),new HashSet<>(caminho),necessidades);
  }
 }
}