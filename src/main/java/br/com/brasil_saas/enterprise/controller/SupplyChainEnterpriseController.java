package br.com.brasil_saas.enterprise.controller;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class SupplyChainEnterpriseController {
 private final JdbcTemplate jdbc;
 private static final Map<String,String> TABLES=Map.of(
  "reposicao","bc_scm_politica_reposicao","transportes","bc_scm_ordem_transporte",
  "revisoes-produto","bc_plm_produto_revisao","mudancas-engenharia","bc_plm_mudanca",
  "ehs","bc_ehs_ocorrencia","contratos-servico","bc_srv_contrato", "plm-documentos","bc_plm_documento", "plm-efeitos","bc_plm_efeito_mudanca", "plm-aprovacoes","bc_plm_aprovacao", "ehs-riscos","bc_ehs_risco", "ehs-inspecoes","bc_ehs_inspecao", "ehs-acoes","bc_ehs_acao", "ehs-permissoes","bc_ehs_permissao_trabalho"
 );
 @GetMapping("/{resource}")
 @PreAuthorize("isAuthenticated()")
 public List<Map<String,Object>> listar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable String resource){
  String t=table(resource);
  return jdbc.queryForList("select * from brasil_saas."+t+" where empresa_id=? and deleted_at is null order by id desc",u.getEmpresaId());
 }
 @PostMapping("/{resource}")
 @PreAuthorize("isAuthenticated()")
 public Map<String,Object> criar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable String resource,@RequestBody Map<String,Object> body){
  String t=table(resource); Map<String,Object> d=sanitize(body); d.put("empresa_id",u.getEmpresaId()); d.put("created_by",u.getId());
  List<String> c=new ArrayList<>(d.keySet()); if(c.isEmpty()) throw new IllegalArgumentException("Nenhum campo informado");
  String sql="insert into brasil_saas."+t+" ("+String.join(",",c)+") values ("+String.join(",",Collections.nCopies(c.size(),"?"))+") returning *";
  return jdbc.queryForMap(sql,c.stream().map(d::get).toArray());
 }
 @PutMapping("/{resource}/{id}")
 @PreAuthorize("isAuthenticated()")
 public Map<String,Object> atualizar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable String resource,@PathVariable Long id,@RequestBody Map<String,Object> body){
  String t=table(resource); Map<String,Object> d=sanitize(body);
  d.remove("empresa_id"); d.remove("created_by"); d.remove("created_at"); d.remove("id"); d.remove("uuid");
  d.put("updated_at",new Date(System.currentTimeMillis())); d.put("updated_by",u.getId());
  List<String> c=new ArrayList<>(d.keySet()); if(c.isEmpty()) throw new IllegalArgumentException("Nenhum campo informado");
  List<Object> v=c.stream().map(d::get).collect(java.util.stream.Collectors.toList()); v.add(u.getEmpresaId()); v.add(id);
  List<Map<String,Object>> r=jdbc.queryForList("update brasil_saas."+t+" set "+String.join(",",c.stream().map(x->x+"=?").toList())+" where empresa_id=? and id=? and deleted_at is null returning *",v.toArray());
  if(r.isEmpty()) throw new NoSuchElementException("Registro não encontrado"); return r.get(0);
 }
 @DeleteMapping("/{resource}/{id}")
 @PreAuthorize("isAuthenticated()")
 public void excluir(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable String resource,@PathVariable Long id){
  String t=table(resource);
  int n=jdbc.update("update brasil_saas."+t+" set deleted_at=?,updated_at=?,updated_by=? where empresa_id=? and id=?",new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()),u.getId(),u.getEmpresaId(),id);
  if(n==0) throw new NoSuchElementException("Registro não encontrado");
 }
 @PostMapping("/plm/mudancas/{id}/aprovar")
 @PreAuthorize("hasAuthority('plm:escrita')")
 public Map<String,Object> aprovarMudanca(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody Map<String,Object> b){
  ensure("select count(*) from brasil_saas.bc_plm_mudanca where id=? and empresa_id=?",id,u.getEmpresaId(),"Mudança não encontrada");
  String decisao=String.valueOf(b.getOrDefault("decisao","APROVADO")); int etapa=Integer.parseInt(String.valueOf(b.getOrDefault("etapa",1)));
  jdbc.update("insert into brasil_saas.bc_plm_aprovacao(empresa_id,mudanca_id,etapa,aprovador_id,decisao,observacao,decidido_em) values(?,?,?,?,?,?,now()) on conflict(empresa_id,mudanca_id,etapa) do update set decisao=excluded.decisao,aprovador_id=excluded.aprovador_id,observacao=excluded.observacao,decidido_em=now()",u.getEmpresaId(),id,etapa,u.getId(),decisao,b.get("observacao"));
  if("APROVADO".equals(decisao)) jdbc.update("update brasil_saas.bc_plm_mudanca set status='APROVADA',aprovador_id=?,aprovado_em=now(),updated_at=now() where id=? and empresa_id=?",u.getId(),id,u.getEmpresaId());
  return Map.of("ok",true,"status",decisao);
 }
 @PostMapping("/plm/mudancas/{id}/implementar")
 @PreAuthorize("hasAuthority('plm:escrita')")
 public Map<String,Object> implementarMudanca(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
  ensure("select count(*) from brasil_saas.bc_plm_mudanca where id=? and empresa_id=? and status='APROVADA'",id,u.getEmpresaId(),"Mudança não aprovada");
  jdbc.update("update brasil_saas.bc_plm_mudanca set status='IMPLEMENTADA',implementado_em=now(),updated_at=now() where id=? and empresa_id=?",id,u.getEmpresaId());
  jdbc.update("update brasil_saas.bc_plm_efeito_mudanca set status='APLICADA',efetiva_em=current_date where mudanca_id=? and empresa_id=?",id,u.getEmpresaId());
  return Map.of("ok",true);
 }
 @PostMapping("/ehs/riscos/{id}/reavaliar")
 @PreAuthorize("hasAuthority('ehs:escrita')")
 public Map<String,Object> reavaliarRisco(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody Map<String,Object> b){
  double p=number(b.get("probabilidade")),i=number(b.get("impacto"));
  ensure("select count(*) from brasil_saas.bc_ehs_risco where id=? and empresa_id=?",id,u.getEmpresaId(),"Risco EHS não encontrado");
  jdbc.update("update brasil_saas.bc_ehs_risco set probabilidade=?,impacto=?,nivel=?,revisado_em=now(),responsavel=coalesce(?,responsavel) where id=? and empresa_id=?",p,i,p*i,b.get("responsavel"),id,u.getEmpresaId());
  return Map.of("ok",true,"nivel",p*i);
 }
 @PostMapping("/ehs/inspecoes/{id}/encerrar")
 @PreAuthorize("hasAuthority('ehs:escrita')")
 public Map<String,Object> encerrarInspecao(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody Map<String,Object> b){
  ensure("select count(*) from brasil_saas.bc_ehs_inspecao where id=? and empresa_id=?",id,u.getEmpresaId(),"Inspeção não encontrada");
  jdbc.update("update brasil_saas.bc_ehs_inspecao set status='ENCERRADA',resultado=?,observacao=coalesce(?,observacao) where id=? and empresa_id=?",b.getOrDefault("resultado","CONFORME"),b.get("observacao"),id,u.getEmpresaId());
  return Map.of("ok",true);
 }
 @PostMapping("/ehs/permissoes/{id}/aprovar")
 @PreAuthorize("hasAuthority('ehs:escrita')")
 public Map<String,Object> aprovarPermissao(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
  ensure("select count(*) from brasil_saas.bc_ehs_permissao_trabalho where id=? and empresa_id=?",id,u.getEmpresaId(),"Permissão não encontrada");
  jdbc.update("update brasil_saas.bc_ehs_permissao_trabalho set status='APROVADA',aprovada_por=?,aprovada_em=now() where id=? and empresa_id=?",u.getId(),id,u.getEmpresaId());
  return Map.of("ok",true);
 }
 private String table(String r){String t=TABLES.get(r);if(t==null)throw new IllegalArgumentException("Recurso inválido");return t;}
 private Map<String,Object> sanitize(Map<String,Object> s){
  Map<String,Object> d=new LinkedHashMap<>(); s.forEach((k,v)->{if(k!=null&&k.matches("[a-z][a-z0-9_]*")&&!Set.of("id","uuid","empresa_id","created_by","updated_by","deleted_at").contains(k))d.put(k,v);});return d;
 }
}
