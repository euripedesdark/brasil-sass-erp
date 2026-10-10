package br.com.brasil_saas.plm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "bc_plm_produto_revisao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class PlmRevisao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false) private Long empresaId;
    @Column(name = "produto_id", nullable = false) private Long produtoId;
    @Column(nullable = false, length = 30) private String revisao;
    @Column(length = 500) private String descricao;
    @Column(nullable = false, length = 25) private String status = "EMDESENV";
    @Column(name = "vigente_desde") private LocalDate vigenteDesde;
    @Column(name = "vigente_ate") private LocalDate vigenteAte;
    @Column(length = 500) private String motivo;
    @Column(name = "documento_id", length = 120) private String documentoId;
    @Column(name = "criado_por") private Long criadoPor;
    @Column(name = "aprovado_por") private Long aprovadoPor;
    @Column(name = "aprovado_em") private LocalDateTime aprovadoEm;
}
