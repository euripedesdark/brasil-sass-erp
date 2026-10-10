package br.com.brasil_saas.contabilidade.controller;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/contabilidade/enterprise") @RequiredArgsConstructor
public class ContabilidadeEnterpriseController {
 private final JdbcTemplate jdbc;
 @GetMapping("/regras") @PreAuthorize("hasAuthority('enterprise:leitura')") public List<Map<String,Object>> regras(@AuthenticationPrincipal AuthenticatedUser u){return jdbc.queryForList("select * from brasil_saas.bc_cont_regra_lancamento where empresa_id=? order by codigo",u.getEmpresaId());}
 @PostMapping("/regras") @PreAuthorize("hasAuthority('enterprise:escrita')") public Map<String,Object> regra(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object>b){jdbc.update("""
insert into brasil_saas.bc_cont_regra_lancamento(empresa_id,codigo,nome,origem,conta_debito_id,conta_credito_id,centro_custo_id,historico,configuracao) values(?,?,?,?,?,?,?,?,?::jsonb)
 on conflict(empresa_id,codigo) do update set nome=excluded.nome,origem=excluded.origem,conta_debito_id=excluded.conta_debito_id,conta_credito_id=excluded.conta_credito_id,centro_custo_id=excluded.centro_custo_id,historico=excluded.historico,configuracao=excluded.configuracao,updated_at=now()""",u.getEmpresaId(),b.get("codigo"),b.get("nome"),b.get("origem"),b.get("contaDebitoId"),b.get("contaCreditoId"),b.get("centroCustoId"),b.get("historico"),b.getOrDefault("configuracao","{}"));return Map.of("ok",true);}
 @PostMapping("/lancamentos/{id}/validar") @PreAuthorize("hasAuthority('enterprise:escrita')") public Map<String,Object> validar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
   Map<String,Object> x=jdbc.queryForMap("select coalesce(sum(case when tipo='D' then valor else 0 end),0) debitos,coalesce(sum(case when tipo='C' then valor else 0 end),0) creditos from brasil_saas.bc_fin_lancamento_partida p join brasil_saas.bc_fin_lancamento_contabil l on l.id=p.lancamento_id where l.empresa_id=? and l.id=?",u.getEmpresaId(),id);
   boolean ok=Objects.equals(String.valueOf(x.get("debitos")),String.valueOf(x.get("creditos"))); return Map.of("ok",ok,"lancamentoId",id,"totais",x);
 }
 @GetMapping("/balancete") @PreAuthorize("hasAuthority('enterprise:leitura')") public List<Map<String,Object>> balancete(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam Long periodoId){
   return jdbc.queryForList("""
select p.id conta_id,p.codigo,p.descricao,coalesce(sum(case when lp.tipo='D' then lp.valor else 0 end),0) debito,
    coalesce(sum(case when lp.tipo='C' then lp.valor else 0 end),0) credito,
    coalesce(sum(case when lp.tipo='D' then lp.valor else -lp.valor end),0) saldo
    from brasil_saas.bc_fin_plano_contas p left join brasil_saas.bc_fin_lancamento_partida lp on lp.plano_contas_id=p.id
    left join brasil_saas.bc_fin_lancamento_contabil l on l.id=lp.lancamento_id
    where p.empresa_id=? and p.deleted_at is null and (l.id is null or l.periodo_contabil_id=?)
    group by p.id,p.codigo,p.descricao order by p.codigo""",u.getEmpresaId(),periodoId);
 }
 @GetMapping("/dre") @PreAuthorize("hasAuthority('enterprise:leitura')") public List<Map<String,Object>> dre(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam Long periodoId){
   return jdbc.queryForList("""
select p.codigo,p.descricao,coalesce(sum(case when lp.tipo='C' then lp.valor else -lp.valor end),0) saldo
    from brasil_saas.bc_fin_plano_contas p join brasil_saas.bc_fin_lancamento_partida lp on lp.plano_contas_id=p.id
    join brasil_saas.bc_fin_lancamento_contabil l on l.id=lp.lancamento_id
    where p.empresa_id=? and l.periodo_contabil_id=? and p.deleted_at is null
    group by p.codigo,p.descricao order by p.codigo""",u.getEmpresaId(),periodoId);
 }
 @PostMapping("/periodos/{id}/fechar") @PreAuthorize("hasAuthority('enterprise:escrita')") public Map<String,Object> fechar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
   Long empresa=u.getEmpresaId(); int n=jdbc.update("update brasil_saas.bc_ent_periodo_contabil set status='FECHADO',fechamento_contabil=true,fechado_em=now(),fechado_por=?,updated_at=now() where empresa_id=? and id=? and status<>'FECHADO'",u.getId(),empresa,id);
   if(n==0)throw new NoSuchElementException("Período inexistente ou já fechado"); return jdbc.queryForMap("select * from brasil_saas.bc_ent_periodo_contabil where empresa_id=? and id=?",empresa,id);
 }
 @GetMapping("/fechamento/{periodoId}") @PreAuthorize("hasAuthority('enterprise:leitura')") public List<Map<String,Object>> fechamento(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long periodoId){return jdbc.queryForList("select * from brasil_saas.bc_cont_fechamento_check where empresa_id=? and periodo_id=? order by id",u.getEmpresaId(),periodoId);}
 @PostMapping("/rateio") @PreAuthorize("hasAuthority('enterprise:escrita')") public Map<String,Object> rateio(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object>b){jdbc.update("insert into brasil_saas.bc_cont_rateio(empresa_id,lancamento_id,centro_custo_id,plano_contas_id,percentual,valor,competencia) values(?,?,?,?,?,?,?)",u.getEmpresaId(),b.get("lancamentoId"),b.get("centroCustoId"),b.get("planoContasId"),b.get("percentual"),b.get("valor"),b.get("competencia"));return Map.of("ok",true);}
}