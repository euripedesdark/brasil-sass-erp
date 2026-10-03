package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_cad_fornecedor", schema = "brasil_saas")
@Getter @Setter
public class Fornecedor extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;
    @Column(length = 30)
    private String codigo;
    @Column(name = "prazo_medio_dias", nullable = false)
    private Integer prazoMedioDias = 0;
    @Column(precision = 4, scale = 2)
    private BigDecimal avaliacao;
    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;
    
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;
    
    @Column(name = "logo_tipo_conteudo", length = 50)
    private String logoTipoConteudo;
    
    @Column(name = "logo_tamanho")
    private Long logoTamanho;
}
