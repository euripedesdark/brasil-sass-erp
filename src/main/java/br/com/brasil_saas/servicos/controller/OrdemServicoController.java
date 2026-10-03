package br.com.brasil_saas.servicos.controller;
import lombok.extern.slf4j.Slf4j;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.core.service.report.PdfGeneratorService;
import br.com.brasil_saas.servicos.service.OrdemServicoService;
import br.com.brasil_saas.servicos.service.OrdemServicoService.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.web.PageResponse;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable; import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition; import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus; import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList; import java.util.HashMap; import java.util.List; import java.util.Map;
@Slf4j @RestController @RequestMapping("/api/servicos/os") @RequiredArgsConstructor
public class OrdemServicoController {
    private final OrdemServicoService svc;
    private final PdfGeneratorService pdfGenerator;
    private final ClienteRepository clienteRepository;
    @PostMapping @PreAuthorize("hasAuthority('servicos:os:escrita')")
    public ResponseEntity<OsResp> abrir(@AuthenticationPrincipal AuthenticatedUser u, @Valid @RequestBody AberturaReq r){
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.abrir(u.getEmpresaId(), u.getId(), r));
    }
    @PostMapping("/{id}/itens") @PreAuthorize("hasAuthority('servicos:os:escrita')")
    public OsResp addItem(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @Valid @RequestBody ItemReq r){
        return svc.addItem(u.getEmpresaId(), id, r);
    }
    @PostMapping("/{id}/apontamentos") @PreAuthorize("hasAuthority('servicos:os:escrita')")
    public OsResp apontar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @Valid @RequestBody ApontReq r){
        return svc.apontar(u.getEmpresaId(), u.getId(), id, r);
    }
    @PostMapping("/{id}/fechar") @PreAuthorize("hasAuthority('servicos:os:escrita')")
    public OsResp fechar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam(required=false) String laudo){
        return svc.fechar(u.getEmpresaId(), id, laudo);
    }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('servicos:os:leitura')")
    public OsResp buscar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id){ return svc.buscar(u.getEmpresaId(), id); }
    @GetMapping("/{id}/itens") @PreAuthorize("hasAuthority('servicos:os:leitura')")
    public List<OsItemResp> listarItens(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id){
        return svc.listarItens(u.getEmpresaId(), id);
    }
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('servicos:os:escrita')")
    public OsResp atualizar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id,
                            @Valid @RequestBody EdicaoReq r){
        return svc.atualizar(u.getEmpresaId(), id, r);
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('servicos:os:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id){
        svc.excluir(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
    /** Impressão da Ordem de Serviço em PDF. */
    @GetMapping("/{id}/pdf") @PreAuthorize("hasAuthority('servicos:os:leitura')")
    public ResponseEntity<byte[]> imprimir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id){
        OsResp os = svc.buscar(u.getEmpresaId(), id);
        List<OsItemResp> itens = svc.listarItens(u.getEmpresaId(), id);

        // nome vem em uma consulta so: percorrer o proxy da Pessoa aqui rodava
        // fora de sessao e a impressao da OS falhava com 500
        String clienteNome = clienteRepository.nomeDaPessoaPorClienteId(os.clienteId())
            .filter(n -> !n.isBlank())
            .orElse("Cliente #" + os.clienteId());

        Map<String,Object> osData = new HashMap<>();
        osData.put("numero", os.numero());
        osData.put("cliente", clienteNome);
        osData.put("dataInicio", os.aberturaAt() != null ? os.aberturaAt().toLocalDate() : null);
        osData.put("dataFim", os.fechamentoAt() != null ? os.fechamentoAt().toLocalDate() : null);
        osData.put("status", os.status());
        osData.put("descricao", os.equipamento());
        osData.put("valorTotal", os.valorTotal());
        osData.put("subtotal", os.valorTotal());
        osData.put("descontos", java.math.BigDecimal.ZERO);

        List<Map<String,Object>> itensPdf = new ArrayList<>();
        for (OsItemResp i : itens) {
            Map<String,Object> row = new HashMap<>();
            row.put("codigo", i.servicoId() != null ? "SRV-" + i.servicoId() : "PROD-" + i.produtoId());
            row.put("descricao", i.servicoId() != null ? "Serviço #" + i.servicoId() : "Produto #" + i.produtoId());
            row.put("quantidade", i.quantidade());
            row.put("valorUnitario", i.valorUnitario());
            itensPdf.add(row);
        }

        try {
            byte[] pdf = pdfGenerator.generateOrdemServicoPdf(osData, itensPdf, "");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                .filename("os-" + os.numero() + ".pdf", StandardCharsets.UTF_8).build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            // sem log, um PDF que nao sai e um 500 sem pista nenhuma
            log.error("Falha ao gerar o PDF da OS {}", os.id(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping @PreAuthorize("hasAuthority('servicos:os:leitura')")
    public PageResponse<OsResp> listar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required=false) String status, @PageableDefault(size=20) Pageable p){
        return svc.listar(u.getEmpresaId(), status, p);
    }
}
