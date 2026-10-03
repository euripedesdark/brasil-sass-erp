package br.com.brasil_saas.dms.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_dms_versao", schema="brasil_saas") @Getter @Setter
public class DmsVersao extends TenantEntity {
    @Column(name="documento_id", nullable=false) private Long documentoId;
    @Column(nullable=false) private Integer versao;
    @Column(name="arquivo_nome", length=300) private String arquivoNome;
    @Column(name="content_type", length=120) private String contentType;
    @Column(nullable=false) private Long tamanho = 0L;
    @Column(length=128) private String hash;
    @Column(name="conteudo_oid") private Long conteudoOid;
    @Column(columnDefinition="text") private String comentario;
}
