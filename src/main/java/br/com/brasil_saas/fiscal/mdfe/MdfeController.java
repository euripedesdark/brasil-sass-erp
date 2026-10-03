package br.com.brasil_saas.fiscal.mdfe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MDF-e. Por enquanto so o que da para provar sem certificado A1 da SEFAZ.
 *
 * <p>O endpoint de status e o unico seguro para chamar sem certificado, e e o
 * unico que faz sentido expor agora. A emissao depende de certificado que o
 * ERP ainda nao tem, e expor um endpoint que falha por configuracao seria um
 * erro de verdade: quem chama recebe 500 e nao sabe se e o sistema ou o
 * certificado.
 */
@Tag(name = "Fiscal - MDF-e")
@RestController
@RequestMapping("/api/fiscal/mdfe")
@RequiredArgsConstructor
public class MdfeController {

    private final MdfeEmissaoService emissao;

    /**
     * Status do servico na SVRS. Nao grava nada e nao depende de certificado.
     *
     * <p>E o health check do MDF-e, e o unico ponto que pode ser consultado
     * sem certificado -- por isso ele responde mesmo com a emissao fora do ar.
     */
    @Operation(summary = "Status do serviço MDF-e na SVRS (não exige certificado)")
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public Map<String, Object> status() {
        RespostaMdfePadrao r = emissao.statusServico();
        Map<String, Object> saida = new LinkedHashMap<>();
        saida.put("contrato_mdfe", "contrato_mdfe");
        saida.put("sucesso", r.sucesso());
        saida.put("cStat", r.cStat());
        saida.put("xMotivo", r.xMotivo());
        saida.put("operacao", r.operacao());
        saida.put("tempoMs", r.tempoMs());
        saida.put("dados", r.dados());
        saida.put("certificadoConfigurado", emissao.certificadoConfigurado());
        return saida;
    }

    /**
     * A segunda chamada do MDF-e: do recibo para a chave e o protocolo.
     *
     * <p>Existe porque o MDF-e nao devolve protocolo junto do envio. A resposta
     * do envio traz so o recibo; a autorizacao vem aqui. Quem tratar a emissao
     * como se devolvesse protocolo vai concluir que o documento foi autorizado
     * quando ele so foi enfileirado.
     */
    @Operation(summary = "Consulta o recibo e obtém chave e protocolo")
    @GetMapping("/recibo")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public RespostaMdfePadrao recibo(@RequestParam String numero) {
        return emissao.consultarRecibo(numero);
    }
}
