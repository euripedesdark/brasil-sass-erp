package br.com.brasil_saas.vendas.controller;

import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.vendas.model.TabelaPreco;
import br.com.brasil_saas.vendas.model.TabelaPrecoItem;
import br.com.brasil_saas.vendas.repository.TabelaPrecoItemRepository;
import br.com.brasil_saas.vendas.repository.TabelaPrecoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vendas/tabelas-preco")
@RequiredArgsConstructor
public class TabelaPrecoController {
    private final TabelaPrecoRepository tabelaRepository;
    private final TabelaPrecoItemRepository itemRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('vendas:tabela-preco:leitura')")
    public List<TabelaPreco> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        return tabelaRepository.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByNomeAsc(user.getEmpresaId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('vendas:tabela-preco:leitura')")
    public ResponseEntity<TabelaPrecoDetalhe> buscar(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        TabelaPreco tabela = tabelaRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tabela de preço não encontrada"));
        return ResponseEntity.ok(new TabelaPrecoDetalhe(tabela, itens(user.getEmpresaId(), id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('vendas:tabela-preco:escrita')")
    public ResponseEntity<TabelaPrecoDetalhe> criar(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @Valid @RequestBody TabelaPrecoRequest request) {
        TabelaPreco tabela = new TabelaPreco();
        preencher(tabela, request);
        tabela.setEmpresaId(user.getEmpresaId());
        tabela.setAtivo(true);
        tabela = tabelaRepository.save(tabela);
        salvarItens(user.getEmpresaId(), tabela.getId(), request.itens());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TabelaPrecoDetalhe(tabela, itens(user.getEmpresaId(), tabela.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('vendas:tabela-preco:escrita')")
    public ResponseEntity<TabelaPrecoDetalhe> atualizar(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody TabelaPrecoRequest request) {
        TabelaPreco tabela = tabelaRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tabela de preço não encontrada"));
        preencher(tabela, request);
        tabelaRepository.save(tabela);
        if (request.itens() != null) {
            itemRepository.findByEmpresaIdAndTabelaPrecoIdAndAtivoTrue(user.getEmpresaId(), id)
                    .forEach(item -> { item.setAtivo(false); itemRepository.save(item); });
            salvarItens(user.getEmpresaId(), id, request.itens());
        }
        return ResponseEntity.ok(new TabelaPrecoDetalhe(tabela, itens(user.getEmpresaId(), id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('vendas:tabela-preco:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        TabelaPreco tabela = tabelaRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tabela de preço não encontrada"));
        tabela.setAtivo(false);
        tabela.setDeletedAt(java.time.LocalDateTime.now());
        tabelaRepository.save(tabela);
        return ResponseEntity.noContent().build();
    }

    private void preencher(TabelaPreco t, TabelaPrecoRequest r) {
        t.setNome(r.nome());
        t.setCodigo(r.codigo());
        t.setDescricao(r.descricao());
        t.setMoeda(r.moeda() == null || r.moeda().isBlank() ? "BRL" : r.moeda().toUpperCase());
        t.setVigenciaInicio(r.vigenciaInicio());
        t.setVigenciaFim(r.vigenciaFim());
        t.setPercentualDescontoMaximo(r.percentualDescontoMaximo() == null ? BigDecimal.ZERO : r.percentualDescontoMaximo());
    }

    private List<TabelaPrecoItem> itens(Long empresaId, Long tabelaId) {
        return itemRepository.findByEmpresaIdAndTabelaPrecoIdAndAtivoTrue(empresaId, tabelaId);
    }

    private void salvarItens(Long empresaId, Long tabelaId, List<ItemRequest> itens) {
        if (itens == null) return;
        for (ItemRequest r : itens) {
            TabelaPrecoItem item = new TabelaPrecoItem();
            item.setEmpresaId(empresaId);
            item.setTabelaPrecoId(tabelaId);
            item.setProdutoId(r.produtoId());
            item.setPreco(r.preco());
            item.setPrecoMinimo(r.precoMinimo());
            item.setPercentualDescontoMaximo(r.percentualDescontoMaximo() == null ? BigDecimal.ZERO : r.percentualDescontoMaximo());
            item.setVigenciaInicio(r.vigenciaInicio());
            item.setVigenciaFim(r.vigenciaFim());
            item.setAtivo(true);
            itemRepository.save(item);
        }
    }

    public record TabelaPrecoRequest(@NotBlank String nome, String codigo, String descricao, String moeda,
                                     LocalDate vigenciaInicio, LocalDate vigenciaFim,
                                     @DecimalMin("0") BigDecimal percentualDescontoMaximo,
                                     List<ItemRequest> itens) {}
    public record ItemRequest(@NotNull Long produtoId, @NotNull @DecimalMin("0") BigDecimal preco,
                              @DecimalMin("0") BigDecimal precoMinimo,
                              @DecimalMin("0") BigDecimal percentualDescontoMaximo,
                              LocalDate vigenciaInicio, LocalDate vigenciaFim) {}
    public record TabelaPrecoDetalhe(TabelaPreco tabela, List<TabelaPrecoItem> itens) {}
}
