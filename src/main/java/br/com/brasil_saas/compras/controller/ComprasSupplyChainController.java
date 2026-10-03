package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.*;
import br.com.brasil_saas.compras.service.ComprasSupplyChainService;
import br.com.brasil_saas.compras.service.ComprasSupplyChainService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import java.util.*;

@RestController @RequestMapping("/api/compras/supply-chain") @RequiredArgsConstructor
public class ComprasSupplyChainController {
 private final ComprasSupplyChainService service;

 @GetMapping("/solicitacoes")
 @PreAuthorize("hasAuthority('compras:solicitacao:leitura')")
 public List<SolicitacaoCompra> listarSolicitacoes(@AuthenticationPrincipal AuthenticatedUser u){return service.listarSolicitacoes(u.getEmpresaId());}

 @PostMapping("/solicitacoes")
 @PreAuthorize("hasAuthority('compras:solicitacao:escrita')")
 public ResponseEntity<SolicitacaoCompra> criarSolicitacao(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam(required=false) Long solicitanteId,@RequestBody LocalRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.criarSolicitacao(u.getEmpresaId(),solicitanteId,request));}

 @PostMapping("/solicitacoes/{id}/aprovar")
 @PreAuthorize("hasAuthority('compras:solicitacao:aprovar')")
 public SolicitacaoCompra aprovar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){return service.aprovarSolicitacao(u.getEmpresaId(),id);}

 @PostMapping("/cotacoes")
 @PreAuthorize("hasAuthority('compras:cotacao:escrita')")
 public ResponseEntity<CotacaoCompra> criarCotacao(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam Long solicitacaoId,@RequestBody LocalCotacao request){return ResponseEntity.status(HttpStatus.CREATED).body(service.criarCotacao(u.getEmpresaId(),solicitacaoId,request));}

 @GetMapping("/cotacoes/{id}/mapa")
 @PreAuthorize("hasAuthority('compras:cotacao:leitura')")
 public List<Map<String,Object>> mapa(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){return service.mapaComparativo(u.getEmpresaId(),id);}

 @PostMapping("/cotacoes/fornecedor/{cotacaoFornecedorId}/gerar-pedido")
 @PreAuthorize("hasAuthority('compras:cotacao:selecionar')")
 public ResponseEntity<PedidoCompra> gerarPedido(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long cotacaoFornecedorId){return ResponseEntity.status(HttpStatus.CREATED).body(service.gerarPedido(u.getEmpresaId(),cotacaoFornecedorId));}
}