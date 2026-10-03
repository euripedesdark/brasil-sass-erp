package br.com.brasil_saas.financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs de comissao.
 *
 * A tela existente consome uma lista simples e um POST /{id}/pagar. O
 * controller devolvia 404 para os dois porque nunca foi escrito — a entidade
 * e o repositorio existiam, mas nao havia service nem controller, entao a
 * comissao era gravada no faturamento e nunca podia ser consultada nem paga.
 */
public class ComissaoDtos {

    public record ComissaoResponse(
            Long id,
            Long funcionarioId,
            String funcionarioNome,
            Long pedidoId,
            BigDecimal valorVenda,
            BigDecimal percentual,
            BigDecimal valorComissao,
            String status,
            LocalDateTime dataPagamento) {

        public static ComissaoResponse from(ComissaoResponse base) {
            return base;
        }
    }

    /**
     * Regra de comissao.
     *
     * Sem nenhuma regra cadastrada, calcularPercentualComissao cai no
     * {@code orElse(BigDecimal.ZERO)} e toda comissao e gravada como 0,00% —
     * sem erro e sem aviso. Esta tela e o que impede isso de continuar.
     */
    public record RegraRequest(
            @NotNull(message = "Informe o nome da regra")
            @Size(max = 100, message = "Nome deve ter ate 100 caracteres")
            String nome,

            Long vendedorId,

            LocalDateTime vigenciaInicio,
            LocalDateTime vigenciaFim,

            @DecimalMin(value = "0.00", message = "Meta nao pode ser negativa")
            BigDecimal metaValor,

            @DecimalMin(value = "0.00", message = "Faixa minima nao pode ser negativa")
            BigDecimal faixaValorMin,

            BigDecimal faixaValorMax,

            // Estritamente maior que zero, e nao apenas "nao negativo".
            // Uma regra de 0% reproduz exatamente o problema que estas rotas
            // existem para resolver: PedidoVendaServiceImpl cairia no
            // percentual zero e gravaria a comissao como 0,00% sem aviso.
            @NotNull(message = "Informe o percentual")
            @DecimalMin(value = "0.0001", message = "O percentual deve ser maior que zero")
            BigDecimal percentual,

            @Size(max = 30)
            String baseCalculo,

            Boolean ativo) {}

    public record RegraResponse(
            Long id,
            String nome,
            Long vendedorId,
            LocalDateTime vigenciaInicio,
            LocalDateTime vigenciaFim,
            BigDecimal metaValor,
            BigDecimal faixaValorMin,
            BigDecimal faixaValorMax,
            BigDecimal percentual,
            String baseCalculo,
            Boolean ativo) {}
}
