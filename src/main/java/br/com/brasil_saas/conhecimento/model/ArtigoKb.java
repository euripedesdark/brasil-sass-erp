package br.com.brasil_saas.conhecimento.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_kb_artigo", schema = "brasil_saas")
@Getter @Setter
public class ArtigoKb extends TenantEntity {
    @Column(length = 200, nullable = false)
    private String titulo;
    @Column(length = 80)
    private String categoria;
    @Column(columnDefinition = "text")
    private String conteudo;
    @Column(length = 200)
    private String tags;
    @Column(nullable = false)
    private Boolean publicado = Boolean.TRUE;
}
