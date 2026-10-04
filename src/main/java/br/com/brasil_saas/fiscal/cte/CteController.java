package br.com.brasil_saas.fiscal.cte;

import br.com.brasil_saas.fiscal.mdfe.RespostaMdfePadrao;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Fiscal - CT-e")
@RestController
@RequestMapping("/api/fiscal/cte")
@RequiredArgsConstructor
public class CteController {
    private final CteEmissaoService emissao;
    private final CteOperacaoService operacao;

    public record XmlRequest(String xml) {}
    public record CancelRequest(String motivo) {}

    @Operation(summary = "Status do serviço CT-e")
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('fiscal:cte:leitura')")
    public Map<String,Object> status() {
        RespostaMdfePadrao r=emissao.statusServico();
        Map<String,Object> saida=new LinkedHashMap<>();
        saida.put("contrato_mdfe","contrato_mdfe");
        saida.put("documento","CT-e 4.00");
        saida.put("sucesso",r.sucesso()); saida.put("cStat",r.cStat()); saida.put("xMotivo",r.xMotivo());
        saida.put("operacao",r.operacao()); saida.put("tempoMs",r.tempoMs()); saida.put("dados",r.dados());
        saida.put("certificadoConfigurado",emissao.certificadoConfigurado());
        return saida;
    }

    @Operation(summary = "Emite CT-e 4.00 a partir do XML fiscal")
    @PostMapping("/emitir")
    @PreAuthorize("hasAuthority('fiscal:cte:escrita')")
    public Map<String,Object> emitir(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody XmlRequest request) {
        return operacao.emitir(empresa(u),request==null?null:request.xml());
    }

    @Operation(summary = "Consulta CT-e na SEFAZ")
    @GetMapping("/consultar/{chave}")
    @PreAuthorize("hasAuthority('fiscal:cte:leitura')")
    public Map<String,Object> consultar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable String chave) {
        return operacao.consultar(empresa(u),chave);
    }

    @Operation(summary = "Cancela CT-e autorizado")
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('fiscal:cte:escrita')")
    public Map<String,Object> cancelar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody CancelRequest request) {
        return operacao.cancelar(empresa(u),id,request==null?null:request.motivo());
    }

    private Long empresa(AuthenticatedUser u) {
        if(u==null||u.getEmpresaId()==null) throw new IllegalStateException("Usuário sem empresa no token.");
        return u.getEmpresaId();
    }
}
