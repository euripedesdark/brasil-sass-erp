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
  "ehs","bc_ehs_ocorrencia","contratos-servico","bc_srv_contrato"
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
 private String table(String r){String t=TABLES.get(r);if(t==null)throw new IllegalArgumentException("Recurso inválido");return t;}
 private Map<String,Object> sanitize(Map<String,Object> s){
  Map<String,Object> d=new LinkedHashMap<>(); s.forEach((k,v)->{if(k!=null&&k.matches("[a-z][a-z0-9_]*")&&!Set.of("id","uuid","empresa_id","created_by","updated_by","deleted_at").contains(k))d.put(k,v);});return d;
 }
}
