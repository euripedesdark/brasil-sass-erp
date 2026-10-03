package br.com.brasil_saas.qualidade.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_qual_nao_conformidade", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NaoConformidade extends TenantEntity {
    @Column(name="inspecao_id") private Long inspecaoId;
    @Column(nullable=false, length=50) private String numero;
    @Column(nullable=false, length=20) private String severidade = "MEDIA";
    @Column(nullable=false, length=30) private String status = "ABERTA";
    @Column(nullable=false, length=2000) private String descricao;
    @Column(name="causa_raiz", length=2000) private String causaRaiz;
    @Column(name="acao_corretiva", length=2000) private String acaoCorretiva;
    @Column(name="acao_preventiva", length=2000) private String acaoPreventiva;
    @Column(name="responsavel_id") private Long responsavelId;
    private LocalDate prazo;
    @Column(name="encerrada_em") private LocalDateTime encerradaEm;
}
