package br.com.brasil_saas.fiscal.controller;
import br.com.brasil_saas.fiscal.entrada.NfeImportacaoService;
import br.com.brasil_saas.fiscal.entrada.NfeImportacaoService.Plano;
import br.com.brasil_saas.fiscal.service.EntradaNotaService;
import br.com.brasil_saas.fiscal.service.EntradaNotaService.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.web.PageResponse;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable; import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/fiscal/entradas") @RequiredArgsConstructor
public class EntradaNotaController {
    private final EntradaNotaService svc;
    private final NfeImportacaoService importacao;

    // ---------------------------------------------------------------- //
    // Importacao por XML
    //
    // Dois endpoints, e a separacao e o que impede entrada errada no
    // estoque: o primeiro so le e mostra o plano, o segundo grava. Quem
    // chama so o primeiro nao mexe em nada — nem no estoque, nem no
    // cadastro de produto, nem na base.
    // ---------------------------------------------------------------- //

    /**
     * Le o XML e devolve o plano, sem gravar nada.
     *
     * <p>E um POST porque envia arquivo, e a resposta e o plano. Serve para a
     * tela mostrar os valores e a situacao de cada item antes de mexer no
     * estoque.
     *
     * @param pessoaId      fornecedor escolhido na tela. Opcional: se vier
     *                      vazio, o servico tenta achar pelo CNPJ do emitente,
     *                      que e o caminho comum.
     * @param criarProdutos se os itens que nao casarem devem ser cadastrados
     *                      como produto novo, ou se ficam sem entrada. O
     *                      padrao e {@code false}: criar produto com o nome do
     *                      XML suja o cadastro, entao e opcao e nao padrao.
     */
    @PostMapping("/importar/analisar")
    @PreAuthorize("hasAuthority('fiscal:entrada:leitura')")
    public Plano analisar(@AuthenticationPrincipal AuthenticatedUser u,
                           @RequestParam("arquivo") MultipartFile arquivo,
                           @RequestParam(value = "pessoaId", required = false) Long pessoaId,
                           @RequestParam(value = "criarProdutos", defaultValue = "false") boolean criarProdutos) {
        return importacao.analisar(u.getEmpresaId(), arquivo, pessoaId, criarProdutos);
    }

    /**
     * Grava a nota, cria o que foi marcado e da entrada no estoque.
     *
     * <p>Devolve 201 com o que aconteceu em numeros, e nao a nota: quem chama
     * isto quer saber quantos produtos foram criados e quantos itens ficaram
     * sem entrada, para o usuario conferir em vez de supor.
     */
    @PostMapping("/importar/confirmar")
    @PreAuthorize("hasAuthority('fiscal:entrada:escrita')")
    public ResponseEntity<NfeImportacaoService.Confirmado> confirmar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "pessoaId", required = false) Long pessoaId,
            @RequestParam(value = "criarProdutos", defaultValue = "false") boolean criarProdutos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(importacao.confirmar(u.getEmpresaId(), arquivo, pessoaId, criarProdutos));
    }

    // ---------------------------------------------------------------- //
    // Entrada digitada, como estava
    // ---------------------------------------------------------------- //

    @PostMapping @PreAuthorize("hasAuthority('fiscal:entrada:escrita')")
    public ResponseEntity<EntradaResp> registrar(@AuthenticationPrincipal AuthenticatedUser u, @Valid @RequestBody EntradaReq r){
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.registrar(u.getEmpresaId(), r)); }
    @PostMapping("/{id}/manifestacao") @PreAuthorize("hasAuthority('fiscal:manifestacao:escrita')")
    public EntradaResp manifestar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @Valid @RequestBody ManifReq r){ return svc.manifestar(u.getEmpresaId(), id, r); }
    @GetMapping("/chave/{chave}") @PreAuthorize("hasAuthority('fiscal:entrada:leitura')")
    public EntradaResp porChave(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable String chave){ return svc.porChave(u.getEmpresaId(), chave); }
    @GetMapping @PreAuthorize("hasAuthority('fiscal:entrada:leitura')")
    public PageResponse<EntradaResp> listar(@AuthenticationPrincipal AuthenticatedUser u, @PageableDefault(size=20) Pageable p){ return svc.listar(u.getEmpresaId(), p); }
}
