package br.com.brasil_saas.rh.model;

import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_rh_folha", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class FolhaPagamento extends AuditableEntity {
    @Column(nullable = false, length = 7) private String competencia;
    @Column(nullable = false) private String status = "ABERTA";
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2) private BigDecimal valorTotal = BigDecimal.ZERO;
    @Column(name = "titulo_id") private Long tituloId;
    @OneToMany(mappedBy = "folha", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemFolhaPagamento> itens = new ArrayList<>();
}
