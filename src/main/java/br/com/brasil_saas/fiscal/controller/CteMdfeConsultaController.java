package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Cte;
import br.com.brasil_saas.fiscal.model.Mdfe;
import br.com.brasil_saas.fiscal.repository.CteRepository;
import br.com.brasil_saas.fiscal.repository.MdfeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consulta dos CT-e e MDF-e ja gravados no ERP.
 *
 * <p>Os repositorios {@code CteRepository} e {@code MdfeRepository} existiam
 * sem nenhum consumidor: nao havia como listar nem abrir um documento. Este
 * controller so le. Emissao, encerramento e cancelamento dependem do
 * certificado A1 e da lib fincatto e nao fazem parte daqui.
 *
 * <p>O filtro por empresa vem do {@code @TenantId} da {@code TenantEntity};
 * nenhuma consulta precisa filtrar {@code empresa_id} a mao. A listagem nao
 * devolve o XML (pode ser grande); o detalhe devolve.
 */
@Tag(name = "Fiscal - CT-e/MDF-e (consulta)")
@RestController
@RequiredArgsConstructor
public class CteMdfeConsultaController {

    private static final int TAMANHO_MAXIMO = 100;

    private final CteRepository cteRepository;
    private final MdfeRepository mdfeRepository;

    @Operation(summary = "Lista os CT-e gravados (sem o XML)")
    @GetMapping("/api/fiscal/cte")
    @PreAuthorize("hasAuthority('fiscal:cte:leitura')")
    public Page<Map<String, Object>> listarCte(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return cteRepository.findAll(paginar(page, size)).map(c -> resumoCte(c, false));
    }

    @Operation(summary = "Detalhe de um CT-e, com o XML")
    @GetMapping("/api/fiscal/cte/{id}")
    @PreAuthorize("hasAuthority('fiscal:cte:leitura')")
    public Map<String, Object> buscarCte(@PathVariable Long id) {
        Cte cte = cteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CT-e nao encontrado"));
        return resumoCte(cte, true);
    }

    @Operation(summary = "Lista os MDF-e gravados (sem o XML)")
    @GetMapping("/api/fiscal/mdfe")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public Page<Map<String, Object>> listarMdfe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return mdfeRepository.findAll(paginar(page, size)).map(m -> resumoMdfe(m, false));
    }

    @Operation(summary = "Detalhe de um MDF-e, com o XML")
    @GetMapping("/api/fiscal/mdfe/{id}")
    @PreAuthorize("hasAuthority('fiscal:mdfe:leitura')")
    public Map<String, Object> buscarMdfe(@PathVariable Long id) {
        Mdfe mdfe = mdfeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MDF-e nao encontrado"));
        return resumoMdfe(mdfe, true);
    }

    private PageRequest paginar(int page, int size) {
        int pagina = Math.max(page, 0);
        int tamanho = Math.min(Math.max(size, 1), TAMANHO_MAXIMO);
        return PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "id"));
    }

    private Map<String, Object> resumoCte(Cte c, boolean comXml) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("numero", c.getNumero());
        m.put("serie", c.getSerie());
        m.put("chaveAcesso", c.getChaveAcesso());
        m.put("dataEmissao", c.getDataEmissao());
        m.put("status", c.getStatus());
        m.put("tipoOperacao", c.getTipoOperacao());
        m.put("pessoaId", c.getPessoaId());
        m.put("valorCarga", c.getValorCarga());
        m.put("valorFrete", c.getValorFrete());
        if (comXml) {
            m.put("xml", c.getXml());
        }
        return m;
    }

    private Map<String, Object> resumoMdfe(Mdfe d, boolean comXml) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("numero", d.getNumero());
        m.put("serie", d.getSerie());
        m.put("chaveAcesso", d.getChaveAcesso());
        m.put("dataEmissao", d.getDataEmissao());
        m.put("status", d.getStatus());
        m.put("ufInicio", d.getUfInicio());
        m.put("ufFim", d.getUfFim());
        if (comXml) {
            m.put("xml", d.getXml());
        }
        return m;
    }
}
