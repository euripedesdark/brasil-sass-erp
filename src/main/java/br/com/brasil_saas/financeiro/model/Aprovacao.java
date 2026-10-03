package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_fin_aprovacao", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class Aprovacao extends AuditableEntity {
    @Column(name="titulo_id", nullable=false) private Long tituloId;
    @Column(name="fluxo_aprovacao_id") private Long fluxoAprovacaoId;
    @Column(name="usuario_aprovador_id") private Long usuarioAprovadorId;
    @Column(name="tipo_documento", nullable=false, length=30) private String tipoDocumento = "TITULO";
    @Column(name="documento_id") private Long documentoId;
    @Column(nullable=false) private Integer nivel = 1;
    @Column(name="usuario_solicitante_id") private Long usuarioSolicitanteId;
    @Column(name="data_solicitacao") private LocalDateTime dataSolicitacao;
    @Column(name="data_rejeicao") private LocalDateTime dataRejeicao;
    @Column(name="numero_documento", length=60) private String numeroDocumento;
    @Column(length=20) private String status = "PENDENTE";
    @Column(name="data_aprovacao") private LocalDateTime dataAprovacao;
    @Column(columnDefinition="text") private String observacao;
}
