package br.com.brasil_saas.fiscal.cte;

import br.com.brasil_saas.fiscal.mdfe.RespostaMdfePadrao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CT-e. Por enquanto so o que da para provar sem certificado A1 da SEFAZ.
 *
 * <p>Usa o mesmo registro de resposta do MDF-e, {@code RespostaMdfePadrao}, e
 * o nome do contrato continua sendo {@code contrato_mdfe}. Nao e copia e cola
 * por preguiça: e o mesmo formato de contrato, com os mesmos tres estados de
 * {@code sucesso} — {@code true}, {@code false}, {@code null} para "nao deu
 * para saber". MDF-e e CT-e compartilham autorizadora, certificado, o fluxo de
 * recibo e protocolo, e a forma de responder e a mesma.
 */
@Tag(name = "Fiscal - CT-e")
@RestController
@RequestMapping("/api/fiscal/cte")
@RequiredArgsConstructor
public class CteController {

    private final CteEmissaoService emissao;

    @Operation(summary = "Status do serviço CT-e na SVRS (não exige certificado)")
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('fiscal:cte:leitura')")
    public Map<String, Object> status() {
        RespostaMdfePadrao r = emissao.statusServico();
        Map<String, Object> saida = new LinkedHashMap<>();
        saida.put("contrato_mdfe", "contrato_mdfe");
        saida.put("documento", "CT-e 4.00");
        saida.put("sucesso", r.sucesso());
        saida.put("cStat", r.cStat());
        saida.put("xMotivo", r.xMotivo());
        saida.put("operacao", r.operacao());
        saida.put("tempoMs", r.tempoMs());
        saida.put("dados", r.dados());
        saida.put("certificadoConfigurado", emissao.certificadoConfigurado());
        return saida;
    }
}
