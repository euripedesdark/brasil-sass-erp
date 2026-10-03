package br.com.brasil_saas.fiscal.controller;
import br.com.brasil_saas.fiscal.sefaz.SefazConsultaService;
import br.com.brasil_saas.fiscal.sefaz.SefazProperties;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.schemas.RetDistDFeInt;
import br.com.swconsultoria.nfe.schemas.TRetConsSitNFe;
import br.com.swconsultoria.nfe.schemas.TRetConsStatServ;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Endpoints de consulta a SEFAZ. Com sefaz.enabled=false retornam 503 + mensagem clara
 * (nao quebram o boot nem o restante do sistema).
 */
@RestController @RequestMapping("/api/fiscal/sefaz") @RequiredArgsConstructor
public class SefazConsultaController {
    private final SefazConsultaService sefaz;
    private final SefazProperties props;

    @GetMapping("/status") @PreAuthorize("hasAuthority('fiscal:consulta:leitura')")
    public ResponseEntity<?> status(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam String uf) throws Exception {
        if (!props.isEnabled()) return disabled();
        TRetConsStatServ r = sefaz.statusServico(u.getEmpresaId(), EstadosEnum.valueOf(uf.toUpperCase()));
        return ResponseEntity.ok(Map.of("cStat", r.getCStat(), "xMotivo", r.getXMotivo(), "dhRetorno", String.valueOf(r.getDhRetorno())));
    }

    @GetMapping("/consultar") @PreAuthorize("hasAuthority('fiscal:consulta:leitura')")
    public ResponseEntity<?> consultar(@AuthenticationPrincipal AuthenticatedUser u,
                                       @RequestParam String uf, @RequestParam String chave) throws Exception {
        if (!props.isEnabled()) return disabled();
        TRetConsSitNFe r = sefaz.consultarPorChave(u.getEmpresaId(), EstadosEnum.valueOf(uf.toUpperCase()), chave);
        return ResponseEntity.ok(Map.of("cStat", r.getCStat(), "xMotivo", r.getXMotivo(), "chNFe", String.valueOf(r.getChNFe())));
    }

    @GetMapping("/distribuicao") @PreAuthorize("hasAuthority('fiscal:consulta:leitura')")
    public ResponseEntity<?> distribuicao(@AuthenticationPrincipal AuthenticatedUser u,
                                          @RequestParam String ufAutor, @RequestParam String cnpj,
                                          @RequestParam(required = false) String ultNsu,
                                          @RequestParam(required = false) String chave) throws Exception {
        if (!props.isEnabled()) return disabled();
        RetDistDFeInt r = sefaz.distribuicaoDFe(u.getEmpresaId(), EstadosEnum.valueOf(ufAutor.toUpperCase()), cnpj, ultNsu, chave);
        return ResponseEntity.ok(Map.of("cStat", r.getCStat(), "xMotivo", r.getXMotivo(),
                "ultNSU", String.valueOf(r.getUltNSU()), "maxNSU", String.valueOf(r.getMaxNSU())));
    }

    private ResponseEntity<Map<String, Object>> disabled() {
        return ResponseEntity.status(503).body(Map.of(
            "erro", "SEFAZ desabilitada",
            "mensagem", "Configure o certificado em bc_fis_certificado_digital e set brasil-saas.fiscal.sefaz.enabled=true no application-dev.yml",
            "disponivelEm", "30 dias (certificado pendente)"));
    }
}
