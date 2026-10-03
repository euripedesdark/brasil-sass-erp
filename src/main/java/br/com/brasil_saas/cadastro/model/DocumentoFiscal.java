package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_cad_documento_fiscal", schema = "brasil_saas")
@Getter @Setter
public class DocumentoFiscal extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;
    @Column(length = 10, nullable = false)
    private String modelo;
    @Column(length = 10)
    private String serie;
    @Column(length = 20, nullable = false)
    private String numero;
    @Column(name = "chave_acesso", length = 50)
    private String chaveAcesso;
    @Column(name = "emissao_at")
    private LocalDateTime emissaoAt;
    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;
}
