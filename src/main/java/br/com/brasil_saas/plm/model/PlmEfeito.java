package br.com.brasil_saas.plm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "bc_plm_efeito_mudanca", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class PlmEfeito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false) private Long empresaId;
    @Column(name = "mudanca_id", nullable = false) private Long mudancaId;
    @Column(name = "entidade_tipo", nullable = false, length = 50) private String entidadeTipo;
    @Column(name = "entidade_id", nullable = false) private Long entidadeId;
    @Column(nullable = false, length = 30) private String acao;
    @Column(name = "revisao_anterior", length = 30) private String revisaoAnterior;
    @Column(name = "revisao_nova", length = 30) private String revisaoNova;
    @Column(name = "efetiva_em") private LocalDate efetivaEm;
    @Column(nullable = false, length = 30) private String status = "PENDENTE";
    @Column(columnDefinition = "TEXT") private String observacao;
    @Column(name = "ordem_execucao", nullable = false) private Integer ordemExecucao = 1;
    @Column(nullable = false) private Boolean obrigatorio = true;
    @Column(name = "aplicado_em") private LocalDateTime aplicadoEm;
    @Column(name = "aplicado_por") private Long aplicadoPor;
    @Column(name = "erro_implementacao", length = 2000) private String erroImplementacao;
}
