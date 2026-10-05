package br.com.brasil_saas.core.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidade multi-tenant raiz. bc_core_empresa NÃO tem empresa_id (ela é o tenant).
 */
@Entity
@Table(name = "bc_core_empresa", schema = "brasil_saas")
@Getter
@Setter
public class Empresa extends BaseEntity {

    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    @Column(length = 14)
    private String cnpj;

    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;

    @Column(length = 200)
    private String endereco;

    @Column(length = 100)
    private String bairro;

    @Column(length = 8)
    private String cep;

    @Column(name = "uf", length = 2, columnDefinition = "bpchar(2)")
    private String uf;

    @Column(length = 20)
    private String telefone;

    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Column(name = "regime_tributario", length = 20)
    private String regimeTributario;

    @Column(name = "codigo_ibge", length = 7)
    private String codigoIbge;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 20)
    private String status;
    
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;
    
    @Column(name = "logo_tipo_conteudo", length = 50)
    private String logoTipoConteudo;
    
    @Column(name = "logo_tamanho")
    private Long logoTamanho;

    /** Credenciais Stripe da própria empresa, armazenadas cifradas. */
    @Column(name = "stripe_secret_key_encrypted", columnDefinition = "TEXT")
    private String stripeSecretKeyEncrypted;

    @Column(name = "stripe_webhook_secret_encrypted", columnDefinition = "TEXT")
    private String stripeWebhookSecretEncrypted;

    @Column(name = "stripe_habilitada", nullable = false)
    private Boolean stripeHabilitada = false;

    @Column(name = "stripe_account_id", length = 64)
    private String stripeAccountId;
}
