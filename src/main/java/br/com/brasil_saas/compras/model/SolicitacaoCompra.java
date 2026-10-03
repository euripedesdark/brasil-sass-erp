package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@Entity @Table(name="bc_com_solicitacao", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SolicitacaoCompra extends TenantEntity {
    @Column(name="solicitante_id") private Long solicitanteId;
    @Column(nullable=false,length=20) private String numero;
    @Column(nullable=false,length=20) private String status="ABERTA";
    @Column(name="data_solicitacao",nullable=false) private LocalDate dataSolicitacao=LocalDate.now();
    @Column(name="data_necessidade") private LocalDate dataNecessidade;
    @Column(columnDefinition="TEXT") private String observacao;
    @OneToMany(mappedBy="solicitacao",cascade=CascadeType.ALL,orphanRemoval=true)
    private List<SolicitacaoCompraItem> itens=new ArrayList<>();
}