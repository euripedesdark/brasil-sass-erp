package br.com.brasil_saas.producao.controller;

import br.com.brasil_saas.producao.model.*;
import br.com.brasil_saas.producao.repository.*;
import br.com.brasil_saas.shared.exception.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/producao")
@RequiredArgsConstructor
public class RoteiroProducaoController {
 private final CentroTrabalhoRepository centroRepo;
 private final RoteiroProducaoRepository roteiroRepo;
 private final OperacaoRoteiroRepository operacaoRepo;

 @GetMapping("/centros-trabalho")
 @PreAuthorize("hasAuthority('producao:estrutura:leitura')")
 public List<CentroTrabalho> centros(@AuthenticationPrincipal AuthenticatedUser u){
  return centroRepo.findByEmpresaIdAndDeletedAtIsNullOrderByCodigo(u.getEmpresaId());
 }
 @PostMapping("/centros-trabalho")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public CentroTrabalho criarCentro(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody CentroTrabalho b){
  b.setId(null); b.setEmpresaId(u.getEmpresaId()); b.setDeletedAt(null);
  if(b.getCapacidadeHorasDia()==null||b.getCapacidadeHorasDia().signum()<=0)b.setCapacidadeHorasDia(BigDecimal.valueOf(8));
  b.setAtivo(b.getAtivo()==null||b.getAtivo()); return centroRepo.save(b);
 }
 @PutMapping("/centros-trabalho/{id}")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public CentroTrabalho atualizarCentro(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody CentroTrabalho b){
  CentroTrabalho e=centroRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Centro de trabalho não encontrado"));
  e.setCodigo(b.getCodigo());e.setNome(b.getNome());e.setCapacidadeHorasDia(b.getCapacidadeHorasDia());e.setAtivo(b.getAtivo());return centroRepo.save(e);
 }
 @DeleteMapping("/centros-trabalho/{id}")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public void excluirCentro(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
  CentroTrabalho e=centroRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Centro de trabalho não encontrado"));
  e.setDeletedAt(LocalDateTime.now());e.setAtivo(false);centroRepo.save(e);
 }
 @GetMapping("/roteiros")
 @PreAuthorize("hasAuthority('producao:estrutura:leitura')")
 public List<RoteiroProducao> roteiros(@AuthenticationPrincipal AuthenticatedUser u,@RequestParam(required=false) Long produtoId){
  return produtoId==null?roteiroRepo.findByEmpresaIdAndDeletedAtIsNullOrderByProdutoIdAscCodigoAscVersaoDesc(u.getEmpresaId()):roteiroRepo.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByVersaoDesc(u.getEmpresaId(),produtoId);
 }
 @PostMapping("/roteiros")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public RoteiroProducao criarRoteiro(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody RoteiroProducao b){
  if(b.getProdutoId()==null||b.getCodigo()==null||b.getCodigo().isBlank()||b.getNome()==null||b.getNome().isBlank())throw new BusinessException("Produto, código e nome do roteiro são obrigatórios");
  b.setId(null);b.setEmpresaId(u.getEmpresaId());b.setDeletedAt(null);b.setVersao(b.getVersao()==null||b.getVersao()<1?1:b.getVersao());b.setAtivo(b.getAtivo()==null||b.getAtivo());return roteiroRepo.save(b);
 }
 @PutMapping("/roteiros/{id}")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public RoteiroProducao atualizarRoteiro(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody RoteiroProducao b){
  RoteiroProducao e=roteiroRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Roteiro não encontrado"));
  e.setProdutoId(b.getProdutoId());e.setCodigo(b.getCodigo());e.setNome(b.getNome());e.setVersao(b.getVersao());e.setVigenciaInicio(b.getVigenciaInicio());e.setVigenciaFim(b.getVigenciaFim());e.setAtivo(b.getAtivo());e.setObservacao(b.getObservacao());return roteiroRepo.save(e);
 }
 @DeleteMapping("/roteiros/{id}")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public void excluirRoteiro(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
  RoteiroProducao e=roteiroRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Roteiro não encontrado"));e.setDeletedAt(LocalDateTime.now());e.setAtivo(false);roteiroRepo.save(e);
 }
 @GetMapping("/roteiros/{id}/operacoes")
 @PreAuthorize("hasAuthority('producao:estrutura:leitura')")
 public List<OperacaoRoteiro> operacoes(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
  roteiroRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Roteiro não encontrado"));return operacaoRepo.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(u.getEmpresaId(),id);
 }
 @PostMapping("/roteiros/{id}/operacoes")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public OperacaoRoteiro criarOperacao(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody OperacaoRoteiro b){
  roteiroRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Roteiro não encontrado"));
  if(b.getSequencia()==null||b.getSequencia()<1||b.getCodigo()==null||b.getCodigo().isBlank()||b.getNome()==null||b.getNome().isBlank())throw new BusinessException("Sequência, código e nome da operação são obrigatórios");
  b.setId(null);b.setEmpresaId(u.getEmpresaId());b.setRoteiroId(id);b.setDeletedAt(null);b.setAtivo(b.getAtivo()==null||b.getAtivo());
  if(b.getSetupMinutos()==null)b.setSetupMinutos(BigDecimal.ZERO);if(b.getMaquinaMinutos()==null)b.setMaquinaMinutos(BigDecimal.ZERO);if(b.getHomemMinutos()==null)b.setHomemMinutos(BigDecimal.ZERO);
  return operacaoRepo.save(b);
 }
 @PutMapping("/roteiros/operacoes/{id}")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public OperacaoRoteiro atualizarOperacao(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody OperacaoRoteiro b){
  OperacaoRoteiro e=operacaoRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Operação não encontrada"));
  e.setSequencia(b.getSequencia());e.setCodigo(b.getCodigo());e.setNome(b.getNome());e.setCentroTrabalhoId(b.getCentroTrabalhoId());e.setSetupMinutos(b.getSetupMinutos()==null?BigDecimal.ZERO:b.getSetupMinutos());e.setMaquinaMinutos(b.getMaquinaMinutos()==null?BigDecimal.ZERO:b.getMaquinaMinutos());e.setHomemMinutos(b.getHomemMinutos()==null?BigDecimal.ZERO:b.getHomemMinutos());e.setInstrucoes(b.getInstrucoes());e.setAtivo(b.getAtivo());return operacaoRepo.save(e);
 }
 @DeleteMapping("/roteiros/operacoes/{id}")
 @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
 public void excluirOperacao(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
  OperacaoRoteiro e=operacaoRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id,u.getEmpresaId()).orElseThrow(()->new ResourceNotFoundException("Operação não encontrada"));e.setDeletedAt(LocalDateTime.now());e.setAtivo(false);operacaoRepo.save(e);
 }
}
