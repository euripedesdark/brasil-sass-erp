package br.com.brasil_saas.rh.controller;
import br.com.brasil_saas.rh.model.Funcionario;
import br.com.brasil_saas.rh.repository.FuncionarioRepository;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.repository.PessoaRepository;
import br.com.brasil_saas.core.service.EsocialService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/rh/esocial") @RequiredArgsConstructor
public class EsocialController {
    private final EsocialService esocial;
    private final FuncionarioRepository funcionarios;
    private final PessoaRepository pessoas;
    public record EnviarReq(String tipoEvento, Long funcionarioId) {}
    @PostMapping("/eventos") @PreAuthorize("hasAuthority('rh:funcionario:escrita')")
    public ResponseEntity<Map<String, Object>> enviar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody EnviarReq r) {
        Funcionario f = funcionarios.findById(r.funcionarioId()).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario inexistente"));
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("funcionarioId", f.getId());
        dados.put("matricula", f.getMatricula());
        dados.put("dataAdmissao", f.getDataAdmissao() == null ? null : f.getDataAdmissao().toString());
        Pessoa pess = null;
        if (f.getPessoaId() != null) pess = pessoas.findById(f.getPessoaId()).orElse(null);
        if (pess != null) {
            dados.put("nome", pess.getNome());
            dados.put("documento", pess.getDocumento());
            dados.put("email", pess.getEmail());
        }
        if (f.getCargo() != null) dados.put("cargo", f.getCargo().getNome());
        dados.put("salario", f.getSalario());
        String recibo = esocial.enviarEvento(r.tipoEvento(), dados);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("recibo", recibo);
        return ResponseEntity.ok(m);
    }
    @GetMapping("/eventos/status") @PreAuthorize("hasAuthority('rh:funcionario:leitura')")
    public List<Map<String, Object>> status(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long funcionarioId) {
        funcionarios.findById(funcionarioId).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Funcionario inexistente"));
        return esocial.consultarStatusEventos(funcionarioId);
    }
}
