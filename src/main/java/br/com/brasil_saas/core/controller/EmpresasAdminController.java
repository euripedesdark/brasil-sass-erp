package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.core.service.EmpresaStripeService;
import br.com.brasil_saas.fiscal.service.CertificadoDigitalService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lista empresas (matriz/filial) com status de Stripe e certificado.
 * SUPERUSER/SUPERADMIN vê todas; demais só a empresa do token.
 */
@RestController
@RequestMapping("/api/core/empresas")
@RequiredArgsConstructor
public class EmpresasAdminController {

    private final EmpresaRepository empresaRepository;
    private final EmpresaStripeService stripeService;
    private final CertificadoDigitalService certificadoDigitalService;

    @GetMapping("/com-credenciais")
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> listarComCredenciais(@AuthenticationPrincipal AuthenticatedUser u) {
        List<Empresa> empresas;
        if (ehAdminGlobal(u)) {
            empresas = empresaRepository.findAll().stream()
                    .filter(e -> e.getDeletedAt() == null)
                    .toList();
        } else {
            if (u == null || u.getEmpresaId() == null) {
                return List.of();
            }
            empresas = empresaRepository.findById(u.getEmpresaId())
                    .filter(e -> e.getDeletedAt() == null)
                    .map(List::of)
                    .orElse(List.of());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Empresa e : empresas) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", e.getId());
            row.put("razaoSocial", e.getRazaoSocial());
            row.put("nomeFantasia", e.getNomeFantasia());
            row.put("cnpj", e.getCnpj());
            row.put("uf", e.getUf());
            boolean stripeOk = false;
            boolean stripeHab = false;
            try {
                var s = stripeService.obter(e.getId());
                stripeOk = s.configurada();
                stripeHab = s.habilitada();
            } catch (Exception ignored) {
            }
            row.put("stripeConfigurada", stripeOk);
            row.put("stripeHabilitada", stripeHab);
            int certs = 0;
            try {
                var list = certificadoDigitalService.certificadosSalvos(e.getId());
                certs = list == null ? 0 : list.size();
            } catch (Exception ignored) {
            }
            row.put("certificados", certs);
            row.put("certificadoOk", certs > 0);
            out.add(row);
        }
        return out;
    }

    private static boolean ehAdminGlobal(AuthenticatedUser u) {
        if (u == null) return false;
        for (GrantedAuthority a : u.getAuthorities()) {
            String r = a.getAuthority();
            if (r == null) continue;
            String x = r.startsWith("ROLE_") ? r.substring(5) : r;
            if ("SUPERUSER".equals(x) || "SUPERADMIN".equals(x) || "ADMIN".equals(x)) {
                return true;
            }
        }
        return false;
    }
}
