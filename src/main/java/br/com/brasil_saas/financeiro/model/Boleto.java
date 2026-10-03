package br.com.brasil_saas.financeiro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Boleto emitido via boleto_cnab_api. Ver migration V90. */
@Entity
@Table(name = "bc_fin_boleto", schema = "brasil_saas")
@Getter
@Setter
public class Boleto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "titulo_id")
    private Long tituloId;

    @Column(name = "nosso_numero", length = 50)
    private String nossoNumero;

    /**
     * Nosso numero so com digitos, para casar com o retorno do banco.
     *
     * A API emite "175/12345678-4", mas o retorno CNAB traz "00000011" — o
     * banco nao devolve a barra nem o DV. Comparar os dois como texto nunca
     * casa, e a conciliacao ficaria sempre com divergencia. Guardar as duas
     * formas resolve: esta e a chave de busca, `nossoNumero` serve para exibir.
     */
    @Column(name = "nosso_numero_chave", length = 50)
    private String nossoNumeroChave;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "banco", length = 10, nullable = false)
    private String banco;

    @Column(name = "agencia", length = 10)
    private String agencia;

    @Column(name = "conta", length = 20)
    private String conta;

    @Column(name = "digito_agencia", length = 2)
    private String digitoAgencia;

    @Column(name = "digito_conta", length = 2)
    private String digitoConta;

    @Column(name = "valor", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "vencimento", nullable = false)
    private LocalDate vencimento;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "EMITIDO";

    @Column(name = "documento_id", length = 64)
    private String documentoId;

    @Column(name = "documento_hash", length = 64)
    private String documentoHash;

    @Column(name = "criado_em", insertable = false, updatable = false)
    @Immutable
    private LocalDateTime criadoEm;
}
