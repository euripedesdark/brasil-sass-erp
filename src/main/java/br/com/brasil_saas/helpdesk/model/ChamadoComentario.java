package br.com.brasil_saas.helpdesk.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_hdp_comentario", schema = "brasil_saas")
@Getter @Setter
public class ChamadoComentario extends TenantEntity {
    @Column(name = "chamado_id", nullable = false)
    private Long chamadoId;
    @Column(length = 150)
    private String autor;
    @Column(columnDefinition = "text", nullable = false)
    private String texto;
}
