package br.com.brasil_saas.notificacoes.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_core_notificacao", schema = "brasil_saas")
@Getter @Setter
public class Notificacao extends TenantEntity {
    @Column(name = "usuario_id")
    private Long usuarioId;
    @Column(length = 200, nullable = false)
    private String titulo;
    @Column(columnDefinition = "text")
    private String mensagem;
    @Column(length = 40, nullable = false)
    private String tipo = "INFO";
    @Column(nullable = false)
    private Boolean lida = Boolean.FALSE;
    @Column(length = 300)
    private String link;
}
