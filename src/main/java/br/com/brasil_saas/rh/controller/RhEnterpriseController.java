package br.com.brasil_saas.rh.controller;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/rh/enterprise")
@RequiredArgsConstructor
public class RhEnterpriseController {
    private final JdbcTemplate jdbc;

    private Long empresa(AuthenticatedUser u){ return u.getEmpresaId(); }

    @GetMapping("/contratos")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> contratos(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_rh_contrato where empresa_id=? and deleted_at is null order by inicio desc", empresa(u));
    }
    @PostMapping("/contratos")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> contrato(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_rh_contrato
          (empresa_id,funcionario_id,tipo,numero,inicio,fim,salario,jornada_semanal,centro_custo_id,cargo_id,sindicato,regime,status)
          values (?,?,?,?,?,?,?,?,?,?,?,?,?)""", empresa(u), b.get("funcionarioId"), b.getOrDefault("tipo","CLT"), b.get("numero"),
          b.get("inicio"), b.get("fim"), b.getOrDefault("salario",0), b.getOrDefault("jornadaSemanal",44),
          b.get("centroCustoId"), b.get("cargoId"), b.get("sindicato"), b.getOrDefault("regime","MENSAL"), b.getOrDefault("status","ATIVO"));
        return Map.of("ok",true);
    }

    @GetMapping("/beneficios")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> beneficios(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_rh_beneficio where empresa_id=? and deleted_at is null order by descricao", empresa(u));
    }
    @PostMapping("/beneficios")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> beneficio(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_rh_beneficio
          (empresa_id,codigo,descricao,tipo,valor_empresa,valor_funcionario,periodicidade,fornecedor,ativo)
          values (?,?,?,?,?,?,?,?,?)""", empresa(u), b.get("codigo"), b.get("descricao"), b.get("tipo"),
          b.getOrDefault("valorEmpresa",0), b.getOrDefault("valorFuncionario",0), b.getOrDefault("periodicidade","MENSAL"),
          b.get("fornecedor"), b.getOrDefault("ativo",true));
        return Map.of("ok",true);
    }

    @PostMapping("/beneficios/{beneficioId}/vincular/{funcionarioId}")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> vincularBeneficio(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long beneficioId,@PathVariable Long funcionarioId,@RequestBody(required=false) Map<String,Object> b){
        b=b==null?Map.of():b;
        jdbc.update("""insert into brasil_saas.bc_rh_funcionario_beneficio
          (empresa_id,funcionario_id,beneficio_id,inicio,quantidade,valor_desconto,status)
          values (?,?,?,?,?,?,?)""",empresa(u),funcionarioId,beneficioId,b.getOrDefault("inicio", LocalDate.now()),
          b.getOrDefault("quantidade",1),b.getOrDefault("valorDesconto",0),b.getOrDefault("status","ATIVO"));
        return Map.of("ok",true);
    }

    @GetMapping("/dependentes/{funcionarioId}")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> dependentes(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long funcionarioId){
        return jdbc.queryForList("select * from brasil_saas.bc_rh_dependente where empresa_id=? and funcionario_id=? and deleted_at is null order by nome",empresa(u),funcionarioId);
    }
    @PostMapping("/dependentes/{funcionarioId}")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> dependente(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long funcionarioId,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_rh_dependente
          (empresa_id,funcionario_id,nome,cpf,nascimento,parentesco,dependente_ir,dependente_salario_familia)
          values (?,?,?,?,?,?,?,?)""",empresa(u),funcionarioId,b.get("nome"),b.get("cpf"),b.get("nascimento"),b.get("parentesco"),
          b.getOrDefault("dependenteIr",false),b.getOrDefault("dependenteSalarioFamilia",false));
        return Map.of("ok",true);
    }

    @GetMapping("/afastamentos")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> afastamentos(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_rh_afastamento where empresa_id=? and deleted_at is null order by inicio desc",empresa(u));
    }
    @PostMapping("/afastamentos")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> afastamento(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_rh_afastamento
          (empresa_id,funcionario_id,tipo,inicio,fim,dias,cid,motivo,remunerado,status)
          values (?,?,?,?,?,?,?,?,?,?)""",empresa(u),b.get("funcionarioId"),b.get("tipo"),b.get("inicio"),b.get("fim"),
          b.getOrDefault("dias",0),b.get("cid"),b.get("motivo"),b.getOrDefault("remunerado",false),b.getOrDefault("status","ABERTO"));
        return Map.of("ok",true);
    }

    @GetMapping("/banco-horas")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> bancoHoras(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam(required=false) String competencia){
        if(competencia==null) return jdbc.queryForList("select * from brasil_saas.bc_rh_banco_horas where empresa_id=? and deleted_at is null order by competencia desc",empresa(u));
        return jdbc.queryForList("select * from brasil_saas.bc_rh_banco_horas where empresa_id=? and competencia=? and deleted_at is null",empresa(u),competencia);
    }

    @PostMapping("/banco-horas/apurar")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> apurarBanco(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam String competencia){
        int n=jdbc.update("""insert into brasil_saas.bc_rh_banco_horas
          (empresa_id,funcionario_id,competencia,saldo_anterior,creditos,debitos,saldo_final)
          select ?,p.funcionario_id,?,coalesce((select saldo_final from brasil_saas.bc_rh_banco_horas x
          where x.empresa_id=? and x.funcionario_id=p.funcionario_id and x.competencia<?
          order by x.competencia desc limit 1),0),
          greatest(coalesce(sum(p.horas_trabalhadas),0)-160,0),greatest(160-coalesce(sum(p.horas_trabalhadas),0),0),
          coalesce((select saldo_final from brasil_saas.bc_rh_banco_horas x where x.empresa_id=? and x.funcionario_id=p.funcionario_id and x.competencia<?
          order by x.competencia desc limit 1),0)+greatest(coalesce(sum(p.horas_trabalhadas),0)-160,0)-greatest(160-coalesce(sum(p.horas_trabalhadas),0),0)
          from brasil_saas.bc_rh_ponto p where p.empresa_id=? and to_char(p.data,'YYYY-MM')=?
          group by p.funcionario_id
          on conflict (empresa_id,funcionario_id,competencia) do update set creditos=excluded.creditos,debitos=excluded.debitos,saldo_final=excluded.saldo_final""",
          empresa(u),competencia,empresa(u),competencia,empresa(u),competencia,empresa(u),competencia);
        return Map.of("ok",true,"registros",n,"competencia",competencia);
    }

    @GetMapping("/saude-seguranca")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> saude(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_rh_saude_seguranca where empresa_id=? and deleted_at is null order by vencimento nulls last",empresa(u));
    }
    @PostMapping("/saude-seguranca")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> saude(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_rh_saude_seguranca
          (empresa_id,funcionario_id,tipo,data_evento,vencimento,documento,resultado,risco,descricao,status)
          values (?,?,?,?,?,?,?,?,?,?)""",empresa(u),b.get("funcionarioId"),b.get("tipo"),b.getOrDefault("dataEvento",LocalDate.now()),
          b.get("vencimento"),b.get("documento"),b.get("resultado"),b.get("risco"),b.get("descricao"),b.getOrDefault("status","ATIVO"));
        return Map.of("ok",true);
    }

    @GetMapping("/treinamentos")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public List<Map<String,Object>> treinamentos(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_rh_treinamento where empresa_id=? and deleted_at is null order by titulo",empresa(u));
    }

    @PostMapping("/treinamentos")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> treinamento(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_rh_treinamento
          (empresa_id,codigo,titulo,tipo,carga_horaria,validade_meses,fornecedor,custo,status)
          values (?,?,?,?,?,?,?,?,?)""",empresa(u),b.get("codigo"),b.get("titulo"),b.get("tipo"),b.getOrDefault("cargaHoraria",0),
          b.get("validadeMeses"),b.get("fornecedor"),b.getOrDefault("custo",0),b.getOrDefault("status","ATIVO"));
        return Map.of("ok",true);
    }

    @PostMapping("/folha/{folhaId}/calcular")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> calcularFolha(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long folhaId){
        int n=jdbc.update("""insert into brasil_saas.bc_rh_folha_item
          (empresa_id,folha_id,funcionario_id,bruto,descontos,inss,irrf,fgts,beneficios,liquido,encargos)
          select ?,?,c.id,coalesce(c.salario,0),0,round(coalesce(c.salario,0)*0.075,2),
          0,round(coalesce(c.salario,0)*0.08,2),coalesce((select sum(fb.valor_desconto) from brasil_saas.bc_rh_funcionario_beneficio fb
          where fb.empresa_id=? and fb.funcionario_id=c.id and fb.status='ATIVO'),0),
          coalesce(c.salario,0)-round(coalesce(c.salario,0)*0.075,2)-
          coalesce((select sum(fb.valor_desconto) from brasil_saas.bc_rh_funcionario_beneficio fb where fb.empresa_id=? and fb.funcionario_id=c.id and fb.status='ATIVO'),0),
          round(coalesce(c.salario,0)*0.08,2)
          from brasil_saas.bc_rh_contrato c where c.empresa_id=? and c.status='ATIVO' and c.deleted_at is null
          on conflict (empresa_id,folha_id,funcionario_id) do update set bruto=excluded.bruto,descontos=excluded.descontos,
          inss=excluded.inss,fgts=excluded.fgts,beneficios=excluded.beneficios,liquido=excluded.liquido,encargos=excluded.encargos,status='CALCULADO'""",
          empresa(u),folhaId,empresa(u),empresa(u),empresa(u));
        return Map.of("ok",true,"registros",n);
    }

    @PostMapping("/folha/{folhaId}/fechar")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public Map<String,Object> fecharFolha(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long folhaId,@RequestParam String competencia){
        Map<String,Object> t=jdbc.queryForMap("""select coalesce(sum(bruto),0) bruto,coalesce(sum(descontos),0) descontos,
          coalesce(sum(liquido),0) liquido,coalesce(sum(encargos),0) encargos
          from brasil_saas.bc_rh_folha_item where empresa_id=? and folha_id=?""",empresa(u),folhaId);
        jdbc.update("""insert into brasil_saas.bc_rh_fechamento_folha
          (empresa_id,competencia,folha_id,etapa,status,total_bruto,total_descontos,total_liquido,total_encargos,fechado_em)
          values (?,?,?,'FOLHA','FECHADO',?,?,?,?,now())
          on conflict (empresa_id,competencia,etapa) do update set status='FECHADO',total_bruto=excluded.total_bruto,
          total_descontos=excluded.total_descontos,total_liquido=excluded.total_liquido,total_encargos=excluded.total_encargos,fechado_em=now()""",
          empresa(u),competencia,folhaId,t.get("bruto"),t.get("descontos"),t.get("liquido"),t.get("encargos"));
        return Map.of("ok",true,"folhaId",folhaId,"totais",t);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public Map<String,Object> dashboard(@AuthenticationPrincipal AuthenticatedUser u){
        Map<String,Object> r=new LinkedHashMap<>();
        r.put("funcionarios",count("select count(*) from brasil_saas.bc_rh_funcionario where empresa_id=?",empresa(u)));
        r.put("contratosAtivos",count("select count(*) from brasil_saas.bc_rh_contrato where empresa_id=? and status='ATIVO' and deleted_at is null",empresa(u)));
        r.put("feriasProgramadas",count("select count(*) from brasil_saas.bc_rh_ferias where empresa_id=? and status='PROGRAMADA' and deleted_at is null",empresa(u)));
        r.put("afastamentosAbertos",count("select count(*) from brasil_saas.bc_rh_afastamento where empresa_id=? and status='ABERTO' and deleted_at is null",empresa(u)));
        r.put("eventosEsocialPendentes",count("select count(*) from brasil_saas.bc_esocial_evento where empresa_id=? and status='PENDENTE' and deleted_at is null",empresa(u)));
        return r;
    }
    private long count(String sql,Object... a){ return jdbc.queryForObject(sql,Long.class,a); }
}
