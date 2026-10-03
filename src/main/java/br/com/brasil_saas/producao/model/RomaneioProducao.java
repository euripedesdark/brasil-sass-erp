package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_prod_romaneio", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RomaneioProducao extends TenantEntity {

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name = "producao_id", nullable = false)
    private Producao producao;

    @Column(name = "data_romaneio", nullable = false)
    private LocalDate dataRomaneio = LocalDate.now();

    @Column(name = "destino", length = 150)
    private String destino;

    @Column(name = "responsavel_id")
    private Long responsavelId;

    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ABERTO";

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "romaneio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RomaneioProducaoItem> itens = new ArrayList<>();
}
