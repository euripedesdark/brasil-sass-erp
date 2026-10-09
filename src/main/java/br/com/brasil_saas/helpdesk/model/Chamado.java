package br.com.brasil_saas.helpdesk.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_hdp_chamado", schema = "brasil_saas")
@Getter @Setter
public class Chamado extends TenantEntity {
    @Column(length = 30, nullable = false)
    private String numero;
    @Column(length = 200, nullable = false)
    private String titulo;
    @Column(columnDefinition = "text")
    private String descricao;
    @Column(length = 20, nullable = false)
    private String prioridade = "MEDIA";
    @Column(length = 30, nullable = false)
    private String status = "ABERTO";
    @Column(length = 60)
    private String categoria;
    @Column(length = 150)
    private String solicitante;
    @Column(length = 150)
    private String responsavel;
    @Column(name = "cliente_id")
    private Long clienteId;
    @Column(name = "aberto_em", nullable = false)
    private LocalDateTime abertoEm = LocalDateTime.now();
    @Column(name = "fechado_em")
    private LocalDateTime fechadoEm;
}
