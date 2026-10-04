package br.com.brasil_saas.fiscal.obrigacao;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_fis_obrigacao", schema="brasil_saas") @Getter @Setter
public class FisObrigacao extends TenantEntity {
    @Column(nullable=false, length=200) private String nome;
    @Column(length=60) private String orgao;
    @Column(nullable=false, length=20) private String periodicidade = "MENSAL";
    @Column(name="dia_vencimento", nullable=false) private Integer diaVencimento = 20;
    @Column(columnDefinition="text") private String descricao;
    @Column(nullable=false) private Boolean ativa = true;
}
