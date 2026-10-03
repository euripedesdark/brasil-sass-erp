package br.com.brasil_saas.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Catalogo de bancos brasileiros, por codigo COMPE.
 *
 * Dado de referencia, nao entidade de negocio: e a lista oficial das 513
 * instituicoes, carregada da V96. Serve para o cadastro de conta bancaria e
 * para o emissor de boleto nao aceitar codigo inventado.
 */
@Entity
@Table(name = "bc_core_banco", schema = "brasil_saas")
@Getter
@Setter
public class Banco {

    @Id
    @Column(name = "compe", length = 10)
    private String compe;

    @Column(length = 20)
    private String ispb;

    @Column(length = 20)
    private String cnpj;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "nome_curto", length = 100)
    private String nomeCurto;

    @Column(length = 60)
    private String tipo;

    /** Se a instituicao participa do PIX, segundo o catalogo. */
    @Column(name = "aceita_pix", nullable = false)
    private Boolean aceitaPix = false;

    @Column(nullable = false)
    private Boolean ativo = true;

    /**
     * Lombok so gera isX() para boolean primitivo; em Boolean o metodo e
     * getX(). Este atalho devolve o valor ja desemaranhado, porque os callers
     * usam no filtro da busca e no JSON.
     */
    public boolean isAceitaPix() {
        return Boolean.TRUE.equals(aceitaPix);
    }

    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }
}
