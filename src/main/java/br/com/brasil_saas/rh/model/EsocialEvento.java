package br.com.brasil_saas.rh.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_esocial_evento", schema="brasil_saas") @Getter @Setter
public class EsocialEvento extends TenantEntity {
    @Column(nullable=false, length=20) private String tipo;
    @Column(name="funcionario_id") private Long funcionarioId;
    @Column(columnDefinition="text") private String payload;
    @Column(nullable=false, length=20) private String status = "PENDENTE";
    @Column(length=200) private String recibo;
    @Column(length=200) private String protocolo;
    @Column(columnDefinition="text") private String erro;
}
