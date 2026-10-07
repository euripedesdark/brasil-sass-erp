package br.com.brasil_saas.enterprise.controller;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class EnterpriseOperationsController {
    private final JdbcTemplate jdbc;

    private static final Map<String,String> TABLES = Map.of(
        "contratos","bc_ent_contrato",
        "qualificacoes","bc_ent_fornecedor_qualificacao",
        "metas","bc_ent_meta_comercial",
        "periodos","bc_ent_periodo_contabil",
        "orcamentos","bc_ent_orcamento",
        "tesouraria","bc_ent_tesouraria_previsao",
        "tributacao","bc_ent_cenario_tributario"
    );

    @GetMapping("/{resource}")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> listar(@AuthenticationPrincipal AuthenticatedUser u,
                                            @PathVariable String resource) {
        String table = table(resource);
        return jdbc.queryForList("select * from brasil_saas."+table+" where empresa_id=?"
            + " and coalesce(deleted_at, null) is null order by id desc", u.getEmpresaId());
    }

    @PostMapping("/{resource}")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> criar(@AuthenticationPrincipal AuthenticatedUser u,
                                    @PathVariable String resource,
                                    @RequestBody Map<String,Object> body) {
        String table = table(resource);
        Map<String,Object> data = sanitize(body);
        data.put("empresa_id", u.getEmpresaId());
        data.put("created_by", u.getId());
        data.putIfAbsent("created_at", new Date(System.currentTimeMillis()));
        data.remove("id"); data.remove("uuid"); data.remove("updated_at");
        if (data.isEmpty()) throw new IllegalArgumentException("Nenhum campo informado");

        List<String> columns = new ArrayList<>(data.keySet());
        List<Object> values = columns.stream().map(data::get).toList();
        String sql = "insert into brasil_saas."+table+" ("+String.join(",",columns)+") values ("
            + String.join(",", Collections.nCopies(columns.size(),"?")) + ") returning *";
        return jdbc.queryForMap(sql, values.toArray());
    }

    @PutMapping("/{resource}/{id}")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> atualizar(@AuthenticationPrincipal AuthenticatedUser u,
                                         @PathVariable String resource,
                                         @PathVariable Long id,
                                         @RequestBody Map<String,Object> body) {
        String table = table(resource);
        Map<String,Object> data = sanitize(body);
        data.remove("id"); data.remove("uuid"); data.remove("empresa_id");
        data.remove("created_at"); data.remove("created_by");
        data.put("updated_at", new Date(System.currentTimeMillis()));
        data.put("updated_by", u.getId());
        List<String> columns = new ArrayList<>(data.keySet());
        if (columns.isEmpty()) throw new IllegalArgumentException("Nenhum campo informado");
        String set = String.join(",", columns.stream().map(c -> c+"=?").toList());
        List<Object> values = columns.stream().map(data::get).collect(java.util.stream.Collectors.toList());
        values.add(u.getEmpresaId()); values.add(id);
        String sql = "update brasil_saas."+table+" set "+set
            +" where empresa_id=? and id=? and (deleted_at is null) returning *";
        List<Map<String,Object>> rows = jdbc.queryForList(sql, values.toArray());
        if (rows.isEmpty()) throw new NoSuchElementException("Registro não encontrado");
        return rows.get(0);
    }

    @DeleteMapping("/{resource}/{id}")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public void excluir(@AuthenticationPrincipal AuthenticatedUser u,
                        @PathVariable String resource, @PathVariable Long id) {
        String table = table(resource);
        int n = jdbc.update("update brasil_saas."+table
            +" set deleted_at=?,updated_at=?,updated_by=? where empresa_id=? and id=?",
            Date.valueOf(LocalDate.now()), new Date(System.currentTimeMillis()), u.getId(),
            u.getEmpresaId(), id);
        if (n == 0) throw new NoSuchElementException("Registro não encontrado");
    }

    @PostMapping("/periodos/{id}/fechar")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> fecharPeriodo(@AuthenticationPrincipal AuthenticatedUser u,
                                             @PathVariable Long id,
                                             @RequestParam(defaultValue="contabil") String etapa) {
        String campo = switch (etapa.toLowerCase(Locale.ROOT)) {
            case "financeiro" -> "fechamento_financeiro";
            case "fiscal" -> "fechamento_fiscal";
            case "contabil" -> "fechamento_contabil";
            default -> throw new IllegalArgumentException("Etapa de fechamento inválida");
        };
        int n = jdbc.update("update brasil_saas.bc_ent_periodo_contabil set "+campo
            +"=true,updated_at=?,fechado_em=?,fechado_por=?,status=case when "
            +"fechamento_financeiro and fechamento_fiscal and "+campo+" then 'FECHADO' else 'EM_FECHAMENTO' end "
            +"where empresa_id=? and id=?",
            new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()),u.getId(),u.getEmpresaId(),id);
        if(n==0) throw new NoSuchElementException("Período não encontrado");
        return jdbc.queryForMap("select * from brasil_saas.bc_ent_periodo_contabil where empresa_id=? and id=?",
            u.getEmpresaId(),id);
    }

    private String table(String resource) {
        String table = TABLES.get(resource);
        if(table==null) throw new IllegalArgumentException("Recurso empresarial inválido");
        return table;
    }

    private Map<String,Object> sanitize(Map<String,Object> source) {
        Map<String,Object> out = new LinkedHashMap<>();
        source.forEach((k,v) -> {
            if(k != null && k.matches("[a-z][a-z0-9_]*") && !Set.of(
                "empresa_id","created_by","updated_by","deleted_at","id","uuid").contains(k)) out.put(k,v);
        });
        return out;
    }
}
