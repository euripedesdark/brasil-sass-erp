package br.com.brasil_saas.plm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "bc_plm_aprovacao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class PlmAprovacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false) private Long empresaId;
    @Column(name = "mudanca_id", nullable = false) private Long mudancaId;
    @Column(nullable = false) private Integer etapa;
    @Column(name = "aprovador_id") private Long aprovadorId;
    @Column(nullable = false, length = 30) private String decisao = "PENDENTE";
    @Column(columnDefinition = "TEXT") private String observacao;
    @Column(name = "decidido_em") private LocalDateTime decididoEm;
    @Column(nullable = false) private Boolean obrigatoria = true;
}
