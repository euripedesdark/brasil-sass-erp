package br.com.brasil_saas.dms.controller;
import br.com.brasil_saas.dms.model.*;
import br.com.brasil_saas.dms.service.DmsService;
import br.com.brasil_saas.dms.service.impl.DmsServiceImpl;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/dms") @RequiredArgsConstructor
public class DmsController {
    private final DmsService svc;
    private final DmsServiceImpl impl;
    public record AprovarReq(Integer versao, String aprovador) {}
    public record DecidirReq(Boolean aprovar, String comentario) {}
    @GetMapping("/documentos") @PreAuthorize("hasAuthority('dms:leitura')")
    public List<DmsDocumento> documentos(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String categoria, @RequestParam(required = false) String status) { return svc.documentos(u.getEmpresaId(), categoria, status); }
    @PostMapping("/documentos") @PreAuthorize("hasAuthority('dms:escrita')")
    public ResponseEntity<DmsDocumento> salvar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody DmsDocumento d) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvar(u.getEmpresaId(), d)); }
    @DeleteMapping("/documentos/{id}") @PreAuthorize("hasAuthority('dms:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { svc.excluir(u.getEmpresaId(), id); return ResponseEntity.noContent().build(); }
    @GetMapping("/documentos/{id}/versoes") @PreAuthorize("hasAuthority('dms:leitura')")
    public List<DmsVersao> versoes(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.versoes(u.getEmpresaId(), id); }
    @PostMapping(value = "/documentos/{id}/versoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize("hasAuthority('dms:escrita')")
    public ResponseEntity<DmsVersao> novaVersao(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestPart("arquivo") MultipartFile arquivo, @RequestParam(required = false) String comentario) throws IOException { return ResponseEntity.status(HttpStatus.CREATED).body(svc.novaVersao(u.getEmpresaId(), id, arquivo.getOriginalFilename(), arquivo.getContentType(), arquivo.getBytes(), comentario)); }
    @GetMapping("/versoes/{vid}/download") @PreAuthorize("hasAuthority('dms:leitura')")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long vid) { byte[] b = impl.bytes(u.getEmpresaId(), vid); Map<String, Object> meta = svc.download(u.getEmpresaId(), vid); return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + meta.get("arquivoNome")).contentType(MediaType.parseMediaType(String.valueOf(meta.getOrDefault("contentType", "application/octet-stream")))).body(b); }
    @PostMapping("/documentos/{id}/aprovacoes") @PreAuthorize("hasAuthority('dms:escrita')")
    public ResponseEntity<DmsAprovacao> solicitar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody AprovarReq r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.solicitarAprovacao(u.getEmpresaId(), id, r.versao(), r.aprovador())); }
    @GetMapping("/documentos/{id}/aprovacoes") @PreAuthorize("hasAuthority('dms:leitura')")
    public List<DmsAprovacao> aprovacoes(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.aprovacoes(u.getEmpresaId(), id); }
    @PostMapping("/aprovacoes/{aid}/decidir") @PreAuthorize("hasAuthority('dms:escrita')")
    public DmsAprovacao decidir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long aid, @RequestBody DecidirReq r) { return svc.decidir(u.getEmpresaId(), u.getId(), aid, Boolean.TRUE.equals(r.aprovar()), r.comentario()); }
    @GetMapping("/retencao") @PreAuthorize("hasAuthority('dms:leitura')")
    public List<DmsDocumento> retencao(@AuthenticationPrincipal AuthenticatedUser u) { return svc.retencao(u.getEmpresaId()); }
}
