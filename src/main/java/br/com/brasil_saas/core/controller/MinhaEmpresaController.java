package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.service.EmpresaDoUsuarioService;
import br.com.brasil_saas.shared.model.ApiResponse;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Empresa do usuario.
 *
 * Num ERP multiempresa, empresa e tenant: sem ela nao ha dado de nobody para
 * mostrar. O fluxo e: o primeiro acesso cadastra a empresa, e so depois disso o
 * resto do sistema abre.
 *
 * {@code GET} e publico para autenticados porque e a consulta que a tela de
 * configuracao usa para decidir o que mostrar. {@code POST} so funciona sem
 * empresa — havendo empresa ja cadastrada, criar outra por este caminho seria
 * um caminho para o usuario trocar de tenant digitando no navegador.
 */
@RestController
@RequestMapping("/api/core/minha-empresa")
@RequiredArgsConstructor
public class MinhaEmpresaController {

    private final EmpresaDoUsuarioService service;

    public record EmpresaRequest(
            @NotBlank(message = "Informe a razao social")
            @Size(max = 200, message = "Razao social deve ter ate 200 caracteres")
            String razaoSocial,

            @Size(max = 200)
            String nomeFantasia,

            @NotBlank(message = "Informe o CNPJ")
            // Aceita com ou sem mascara: ninguem digita 14 digitos crus, e
            // recusar "12.345.678/0001-99" so faria a pessoa tirar a mascara
            // na mao. O service limpa os nao-digitos antes de gravar.
            @Pattern(regexp = "[\\d.\\-/]{11,18}", message = "CNPJ invalido")
            String cnpj,

            @Size(max = 30) String inscricaoEstadual,
            @Size(max = 30) String inscricaoMunicipal,
            @Size(max = 20) String regimeTributario,
            @Size(max = 7) String codigoIbge,
            @Size(max = 200) String endereco,
            @Size(max = 20) String numero,
            @Size(max = 100) String complemento,
            @Size(max = 100) String bairro,
            @Size(max = 2) String uf,
            @Size(max = 10) String cep,
            @Size(max = 30) String telefone) {}

    public record SituacaoResponse(
            boolean cadastrada,
            Long empresaId,
            String razaoSocial,
            String cnpj) {}

    @GetMapping
    public ResponseEntity<?> situacao(@AuthenticationPrincipal AuthenticatedUser user) {
        SituacaoResponse s = service.situacao(user);
        return ResponseEntity.ok(ApiResponse.success(s));
    }

    /**
     * Cadastra a empresa do usuario. Depois disso ele passa a estar ligado a
     * ela e o sistema inteiro abre.
     */
    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.cadastrar(request, user)));
    }

    /**
     * Atualiza a empresa que o usuario ja tem. Nao troca o tenant: o
     * usuario continua na mesma empresa, so os dados dela mudam.
     */
    @PutMapping
    public ResponseEntity<?> atualizar(
            @Valid @RequestBody EmpresaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(service.atualizar(request, user)));
    }
}
