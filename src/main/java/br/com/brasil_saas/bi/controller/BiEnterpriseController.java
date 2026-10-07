package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/bi/enterprise")
@RequiredArgsConstructor
public class BiEnterpriseController {
    private final JdbcTemplate jdbc;
    private static final Pattern IDENT = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.]*");

    @PostMapping("/dw/dimensao-tempo")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN')")
    public Map<String,Object> dimensaoTempo(@RequestParam(defaultValue="2015-01-01") LocalDate inicio,
                                            @RequestParam(defaultValue="2035-12-31") LocalDate fim){
        int n=jdbc.update("""insert into brasil_saas.bc_bi_dim_tempo(data,ano,trimestre,mes,mes_nome,semana,dia,dia_semana,fim_mes)
          select d::date,extract(year from d)::int,extract(quarter from d)::int,extract(month from d)::int,
          to_char(d,'TMMonth'),extract(week from d)::int,extract(day from d)::int,extract(isodow from d)::int,
          d::date= (date_trunc('month',d)+interval '1 month - 1 day')::date
          from generate_series(?::date,?::date,interval '1 day') d
          on conflict(data) do nothing""",inicio,fim);
        return Map.of("ok",true,"registros",n);
    }

    @PostMapping("/etl/rh")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN')")
    public Map<String,Object> etlRh(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam String competencia){
        Long empresa=u.getEmpresaId();
        long id=jdbc.queryForObject("insert into brasil_saas.bc_bi_etl_execucao(empresa_id,processo, parametros) values (?,'RH_FOLHA',?::jsonb) returning id",
                Long.class,empresa,"{\"competencia\":\""+competencia+"\"}");
        try{
            int n=jdbc.update("""insert into brasil_saas.bc_bi_fato_rh
              (empresa_id,data,funcionario_id,folha_id,bruto,descontos,liquido,encargos)
              select empresa_id,to_date(?||'-01','YYYY-MM'),funcionario_id,folha_id,bruto,descontos,liquido,encargos
              from brasil_saas.bc_rh_folha_item where empresa_id=? and status<>'CANCELADO'
              on conflict do nothing""",competencia,empresa);
            jdbc.update("update brasil_saas.bc_bi_etl_execucao set fim=now(),status='SUCESSO',registros=? where id=?",n,id);
            return Map.of("ok",true,"registros",n,"execucaoId",id);
        }catch(Exception e){
            jdbc.update("update brasil_saas.bc_bi_etl_execucao set fim=now(),status='ERRO',erro=? where id=?",e.getMessage(),id);
            throw e;
        }
    }

    @PostMapping("/etl/tabela")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN')")
    public Map<String,Object> etlTabela(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam String tabela){
        if(!IDENT.matcher(tabela).matches()) throw new IllegalArgumentException("Tabela invalida");
        String base=tabela.contains(".")?tabela:"brasil_saas."+tabela;
        Boolean existe=jdbc.queryForObject("select exists(select 1 from information_schema.tables where table_schema=? and table_name=?)",
                Boolean.class,base.substring(0,base.indexOf('.')),base.substring(base.indexOf('.')+1));
        if(!Boolean.TRUE.equals(existe)) throw new IllegalArgumentException("Tabela nao encontrada");
        Long id=jdbc.queryForObject("insert into brasil_saas.bc_bi_etl_execucao(empresa_id,processo,parametros) values (?,'SNAPSHOT_TABELA',?::jsonb) returning id",
                Long.class,u.getEmpresaId(),"{\"tabela\":\""+base+"\"}");
        Integer n=jdbc.queryForObject("select count(*) from "+base,Integer.class);
        jdbc.update("update brasil_saas.bc_bi_etl_execucao set fim=now(),status='SUCESSO',registros=? where id=?",n,id);
        return Map.of("ok",true,"tabela",base,"registros",n,"execucaoId",id);
    }

    @GetMapping("/control-tower")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN','GERENTE','DIRETORIA')")
    public Map<String,Object> controlTower(@AuthenticationPrincipal AuthenticatedUser u){
        Long e=u.getEmpresaId(); Map<String,Object> r=new LinkedHashMap<>();
        r.put("vendas",one("select coalesce(sum(valor_liquido),0) from brasil_saas.bc_bi_fato_vendas where empresa_id=?",e));
        r.put("margem",one("select coalesce(sum(margem),0) from brasil_saas.bc_bi_fato_vendas where empresa_id=?",e));
        r.put("receber",one("select coalesce(sum(saldo),0) from brasil_saas.bc_bi_fato_financeiro where empresa_id=? and tipo='RECEBER'",e));
        r.put("pagar",one("select coalesce(sum(saldo),0) from brasil_saas.bc_bi_fato_financeiro where empresa_id=? and tipo='PAGAR'",e));
        r.put("folha",one("select coalesce(sum(bruto),0) from brasil_saas.bc_bi_fato_rh where empresa_id=?",e));
        r.put("producao",one("select coalesce(sum(quantidade_produzida),0) from brasil_saas.bc_bi_fato_producao where empresa_id=?",e));
        return r;
    }

    @GetMapping("/etl/execucoes")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN')")
    public List<Map<String,Object>> execucoes(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_bi_etl_execucao where empresa_id=? order by inicio desc limit 100",u.getEmpresaId());
    }

    @GetMapping("/metricas")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN','GERENTE','DIRETORIA')")
    public List<Map<String,Object>> metricas(@AuthenticationPrincipal AuthenticatedUser u){
        return jdbc.queryForList("select * from brasil_saas.bc_bi_metrica where empresa_id=? or empresa_id is null order by categoria,nome",u.getEmpresaId());
    }

    @PostMapping("/metricas")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN')")
    public Map<String,Object> criarMetrica(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        jdbc.update("""insert into brasil_saas.bc_bi_metrica
          (empresa_id,codigo,nome,categoria,unidade,formula_sql,meta,alerta_min,alerta_max,configuracao)
          values (?,?,?,?,?,?,?,?,?,?::jsonb)
          on conflict(empresa_id,codigo) do update set nome=excluded.nome,categoria=excluded.categoria,
          unidade=excluded.unidade,formula_sql=excluded.formula_sql,meta=excluded.meta,alerta_min=excluded.alerta_min,
          alerta_max=excluded.alerta_max,configuracao=excluded.configuracao,updated_at=now()""",
          u.getEmpresaId(),b.get("codigo"),b.get("nome"),b.get("categoria"),b.get("unidade"),b.get("formulaSql"),
          b.get("meta"),b.get("alertaMin"),b.get("alertaMax"),b.getOrDefault("configuracao","{}"));
        return Map.of("ok",true);
    }

    @PostMapping("/consulta")
    @PreAuthorize("hasAnyRole('SUPERUSER','SUPERADMIN','ADMIN')")
    public List<Map<String,Object>> consulta(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b){
        String sql=Objects.toString(b.get("sql"),"").trim();
        if(!sql.regionMatches(true,0,"select",0,6) || sql.contains(";") || sql.matches("(?is).*\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke)\\b.*"))
            throw new IllegalArgumentException("Somente SELECT simples e sem comandos de escrita");
        return jdbc.queryForList(sql);
    }

    private Object one(String sql,Object... args){ return jdbc.queryForObject(sql,Object.class,args); }
}
