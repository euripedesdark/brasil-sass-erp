package br.com.brasil_saas.agenda.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_agd_evento", schema = "brasil_saas")
@Getter @Setter
public class EventoAgenda extends TenantEntity {
    @Column(length = 200, nullable = false)
    private String titulo;
    @Column(columnDefinition = "text")
    private String descricao;
    @Column(length = 40, nullable = false)
    private String tipo = "REUNIAO";
    @Column(nullable = false)
    private LocalDateTime inicio;
    private LocalDateTime fim;
    @Column(name = "local_evento", length = 200)
    private String localEvento;
    @Column(name = "lead_id")
    private Long leadId;
    @Column(name = "cliente_id")
    private Long clienteId;
    @Column(length = 150)
    private String responsavel;
    @Column(length = 30, nullable = false)
    private String status = "AGENDADO";
}
