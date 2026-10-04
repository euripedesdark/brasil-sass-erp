package br.com.brasil_saas.fiscal.mdfe;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Fiscal - MDF-e")
@RestController
@RequestMapping("/api/fiscal/mdfe")
@RequiredArgsConstructor
public class MdfeController {
    private final MdfeEmissaoService emissao;
    private final MdfeOperacaoService operacao;

    public record XmlRequest(String xml) {}
    public record CancelRequest(String motivo) {}
    public record EncerrarRequest(String codigoMunicipio, String uf) {}

    @Operation(summary = "Status do serviço MDF-e")
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public Map<String,Object> status() {
        RespostaMdfePadrao r=emissao.statusServico();
        Map<String,Object> saida=new LinkedHashMap<>();
        saida.put("contrato_mdfe","contrato_mdfe");
        saida.put("documento","MDF-e 3.00");
        saida.put("sucesso",r.sucesso()); saida.put("cStat",r.cStat()); saida.put("xMotivo",r.xMotivo());
        saida.put("operacao",r.operacao()); saida.put("tempoMs",r.tempoMs()); saida.put("dados",r.dados());
        saida.put("certificadoConfigurado",emissao.certificadoConfigurado());
        return saida;
    }

    @Operation(summary = "Emite MDF-e 3.00 a partir do XML fiscal")
    @PostMapping("/emitir")
    @PreAuthorize("hasAuthority('fiscal:mdfe:escrita')")
    public Map<String,Object> emitir(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody XmlRequest request) {
        return operacao.emitir(empresa(u),request==null?null:request.xml());
    }

    @Operation(summary = "Consulta MDF-e na SEFAZ")
    @GetMapping("/consultar/{chave}")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public Map<String,Object> consultar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable String chave) {
        return operacao.consultar(empresa(u),chave);
    }

    @Operation(summary = "Consulta recibo de processamento do MDF-e")
    @GetMapping("/recibo")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public RespostaMdfePadrao recibo(@RequestParam String numero) {
        return emissao.consultarRecibo(numero);
    }

    @Operation(summary = "Cancela MDF-e autorizado")
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('fiscal:mdfe:escrita')")
    public Map<String,Object> cancelar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody CancelRequest request) {
        return operacao.cancelar(empresa(u),id,request==null?null:request.motivo());
    }

    @Operation(summary = "Encerra MDF-e autorizado")
    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasAuthority('fiscal:mdfe:escrita')")
    public Map<String,Object> encerrar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody EncerrarRequest request) {
        if(request==null) throw new IllegalArgumentException("Dados de encerramento obrigatórios.");
        return operacao.encerrar(empresa(u),id,request.codigoMunicipio(),request.uf());
    }

    private Long empresa(AuthenticatedUser u) {
        if(u==null||u.getEmpresaId()==null) throw new IllegalStateException("Usuário sem empresa no token.");
        return u.getEmpresaId();
    }
}
