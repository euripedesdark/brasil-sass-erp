package br.com.brasil_saas.enterprise.controller;

import br.com.brasil_saas.auth.domain.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class RiskTransportEnterpriseController {
    private final JdbcTemplate jdbc;

    @GetMapping("/grc/riscos")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> riscos(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select * from brasil_saas.bc_grc_risco where empresa_id=? order by status, nivel desc, id desc", u.getEmpresaId());
    }

    @PostMapping("/grc/riscos")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> risco(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Map<String,Object> b) {
        double p = number(b.get("probabilidade")), i = number(b.get("impacto"));
        jdbc.update("insert into brasil_saas.bc_grc_risco(empresa_id,codigo,descricao,categoria,probabilidade,impacto,nivel,status,responsavel,prazo) values(?,?,?,?,?,?,?,?,?,?)",
                u.getEmpresaId(), required(b,"codigo"), required(b,"descricao"), b.get("categoria"), p, i, p*i,
                b.getOrDefault("status","ABERTO"), b.get("responsavel"), b.get("prazo"));
        return Map.of("ok", true, "nivel", p*i);
    }

    @GetMapping("/grc/controles")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> controles(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select * from brasil_saas.bc_grc_controle where empresa_id=? order by codigo", u.getEmpresaId());
    }

    @PostMapping("/grc/controles")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> controle(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Map<String,Object> b) {
        jdbc.update("insert into brasil_saas.bc_grc_controle(empresa_id,codigo,descricao,tipo,frequencia,responsavel,status) values(?,?,?,?,?,?,?)",
                u.getEmpresaId(), required(b,"codigo"), required(b,"descricao"), b.getOrDefault("tipo","PREVENTIVO"),
                b.get("frequencia"), b.get("responsavel"), b.getOrDefault("status","ATIVO"));
        return Map.of("ok", true);
    }

    @GetMapping("/grc/testes")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> testes(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select t.*, c.codigo controle_codigo from brasil_saas.bc_grc_teste_controle t join brasil_saas.bc_grc_controle c on c.id=t.controle_id and c.empresa_id=t.empresa_id where t.empresa_id=? order by t.periodo desc,t.id desc", u.getEmpresaId());
    }

    @PostMapping("/grc/testes")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> teste(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Map<String,Object> b) {
        Long controle = longValue(b.get("controleId"));
        if (controle == null || jdbc.queryForObject("select count(*) from brasil_saas.bc_grc_controle where id=? and empresa_id=?", Integer.class, controle,u.getEmpresaId()) == 0)
            throw new IllegalArgumentException("Controle inválido para a empresa");
        jdbc.update("insert into brasil_saas.bc_grc_teste_controle(empresa_id,controle_id,periodo,resultado,observacao,testado_por,testado_em) values(?,?,?,?,?,?,now()) on conflict (empresa_id,controle_id,periodo) do update set resultado=excluded.resultado,observacao=excluded.observacao,testado_por=excluded.testado_por,testado_em=now()",
                u.getEmpresaId(), controle, required(b,"periodo"), b.getOrDefault("resultado","PENDENTE"), b.get("observacao"), b.get("testadoPor"));
        return Map.of("ok", true);
    }

    @GetMapping("/tms/ordens")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> ordens(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select * from brasil_saas.bc_tms_ordem where empresa_id=? order by id desc", u.getEmpresaId());
    }

    @PostMapping("/tms/ordens")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> ordem(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Map<String,Object> b) {
        jdbc.update("insert into brasil_saas.bc_tms_ordem(empresa_id,numero,origem,destino,transportadora_id,modalidade,status,data_prevista_saida,data_prevista_entrega,peso,volume,frete_previsto,frete_real) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                u.getEmpresaId(), required(b,"numero"), b.get("origem"), b.get("destino"), longValue(b.get("transportadoraId")),
                b.getOrDefault("modalidade","RODOVIARIO"), b.getOrDefault("status","PLANEJADA"),
                b.get("dataPrevistaSaida"), b.get("dataPrevistaEntrega"), number(b.get("peso")), number(b.get("volume")),
                number(b.get("fretePrevisto")), number(b.get("freteReal")));
        return Map.of("ok", true);
    }

    @GetMapping("/tms/eventos")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> eventos(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select e.*, o.numero from brasil_saas.bc_tms_evento e join brasil_saas.bc_tms_ordem o on o.id=e.ordem_id and o.empresa_id=e.empresa_id where e.empresa_id=? order by e.data_evento desc", u.getEmpresaId());
    }

    @PostMapping("/tms/eventos")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> evento(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Map<String,Object> b) {
        Long ordem = longValue(b.get("ordemId"));
        if (ordem == null || jdbc.queryForObject("select count(*) from brasil_saas.bc_tms_ordem where id=? and empresa_id=?", Integer.class, ordem,u.getEmpresaId()) == 0)
            throw new IllegalArgumentException("Ordem de transporte inválida para a empresa");
        jdbc.update("insert into brasil_saas.bc_tms_evento(empresa_id,ordem_id,tipo,data_evento,localizacao,descricao) values(?,?,?,?,?,?)",
                u.getEmpresaId(),ordem,required(b,"tipo"),b.get("dataEvento"),b.get("localizacao"),b.get("descricao"));
        if (b.get("status") != null) jdbc.update("update brasil_saas.bc_tms_ordem set status=? where id=? and empresa_id=?", b.get("status"),ordem,u.getEmpresaId());
        return Map.of("ok", true);
    }

    @GetMapping("/tms/tracking")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> tracking(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select t.*,o.numero,o.origem,o.destino,o.status ordem_status from brasil_saas.bc_tms_tracking t join brasil_saas.bc_tms_ordem o on o.id=t.ordem_id and o.empresa_id=t.empresa_id where t.empresa_id=? order by t.ultima_atualizacao desc nulls last",u.getEmpresaId());
    }

    @PostMapping("/tms/tracking")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> trackingSave(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b) {
        Long ordem=longValue(b.get("ordemId"));
        if(ordem==null || jdbc.queryForObject("select count(*) from brasil_saas.bc_tms_ordem where id=? and empresa_id=?",Integer.class,ordem,u.getEmpresaId())==0)
            throw new IllegalArgumentException("Ordem de transporte inválida para a empresa");
        jdbc.update("insert into brasil_saas.bc_tms_tracking(empresa_id,ordem_id,codigo,transportadora,ultimo_status,ultima_atualizacao) values(?,?,?,?,?,now()) on conflict (empresa_id,ordem_id) do update set codigo=excluded.codigo,transportadora=excluded.transportadora,ultimo_status=excluded.ultimo_status,ultima_atualizacao=now()",
                u.getEmpresaId(),ordem,required(b,"codigo"),b.get("transportadora"),b.get("ultimoStatus"));
        return Map.of("ok",true);
    }

    @GetMapping("/tms/fretes")
    @PreAuthorize("hasAuthority('enterprise:leitura')")
    public List<Map<String,Object>> fretes(@AuthenticationPrincipal AuthenticatedUser u) {
        return jdbc.queryForList("select f.*,o.numero from brasil_saas.bc_tms_frete f join brasil_saas.bc_tms_ordem o on o.id=f.ordem_id and o.empresa_id=f.empresa_id where f.empresa_id=? order by f.id desc",u.getEmpresaId());
    }

    @PostMapping("/tms/fretes")
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public Map<String,Object> frete(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Map<String,Object> b) {
        Long ordem=longValue(b.get("ordemId"));
        if(ordem==null || jdbc.queryForObject("select count(*) from brasil_saas.bc_tms_ordem where id=? and empresa_id=?",Integer.class,ordem,u.getEmpresaId())==0)
            throw new IllegalArgumentException("Ordem de transporte inválida para a empresa");
        jdbc.update("insert into brasil_saas.bc_tms_frete(empresa_id,ordem_id,componente,valor,documento,status) values(?,?,?,?,?,?)",
                u.getEmpresaId(),ordem,required(b,"componente"),number(b.get("valor")),b.get("documento"),b.getOrDefault("status","PENDENTE"));
        return Map.of("ok",true);
    }

    private static String required(Map<String,Object>b,String k){Object v=b.get(k);if(v==null||v.toString().isBlank())throw new IllegalArgumentException(k+" é obrigatório");return v.toString();}
    private static double number(Object v){return v==null?0:Double.parseDouble(v.toString());}
    private static Long longValue(Object v){return v==null?null:Long.valueOf(v.toString());}
}
