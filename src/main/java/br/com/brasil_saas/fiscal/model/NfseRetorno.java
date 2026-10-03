package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * O que a prefeitura respondeu em uma chamada.
 *
 * <p>Uma linha por chamada, e não uma coluna na nota, porque uma nota conversa
 * com a prefeitura mais de uma vez: emissão e, depois, cancelamento. Se o
 * cancelamento sobrescrevesse a emissão, o registro apagado seria justamente o
 * que serve de prova.
 *
 * <p>O corpo da resposta não fica aqui. Fica no Mongo, e esta linha guarda o
 * ponteiro — o mesmo arranjo do XML e do PDF da nota.
 */
@Entity
@Table(name = "bc_fis_nfse_retorno", schema = "brasil_saas")
@Getter @Setter
public class NfseRetorno extends TenantEntity {

    public static final String OPERACAO_EMISSAO = "EMISSAO";
    public static final String OPERACAO_CANCELAMENTO = "CANCELAMENTO";
    public static final String OPERACAO_CONSULTA = "CONSULTA";

    /**
     * A nota da conversa. Sem {@code LAZY} porque a tela lista os retornos de
     * uma nota e a consulta por id traz sempre uma linha só — o lazy aqui
     * custaria uma query a mais sem ganhar nada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nfse_id")
    private Nfse nfse;

    @Column(length = 20, nullable = false)
    private String operacao;

    /**
     * {@code null} quando não deu para saber se a nota saiu.
     *
     * <p>Não é descuido, é o estado que impede duplicidade. A prefeitura
     * recusa com HTTP 422 e um corpo: houve resposta, e a resposta foi não —
     * isso é {@code false}, e reemitir é seguro. Já falta de transporte, ou
     * resposta fora do contrato, não tem como saber: o ERP não pode dizer que
     * nada foi emitido, porque pode ter emitido. Nessas horas a pessoa tem de
     * conferir na prefeitura antes de tentar de novo.
     */
    private Boolean sucesso;

    private Integer httpStatus;

    /** Quanto a chamada levou. Vale mais para timeout do que para relatório. */
    private Long duracaoMs;

    /** Texto da prefeitura, literal. É o que a pessoa precisa ler para corrigir. */
    @Column(columnDefinition = "TEXT")
    private String mensagem;

    /**
     * Código de erro da prefeitura quando ela manda.
     *
     * <p>A Prefeitura de São Paulo escreve {@code [1001] XML não compatível com
     * Schema...}. O número vem entre colchetes no começo, então dá para
     * extrair sem adivinhar posição. A mensagem mede centenas de caracteres e
     * muda conforme o campo que falhou; o código não.
     */
    @Column(length = 20)
    private String codigo;

    /** Alertas que a prefeitura devolveu, um por linha. */
    @Column(columnDefinition = "TEXT")
    private String alertas;

    /**
     * Id do JSON bruto da resposta no MongoDB. Guarda o campo como veio,
     * inclusive o que o ERP ainda não sabe ler.
     */
    @Column(name = "documento_id", length = 50)
    private String documentoId;
}
