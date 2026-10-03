package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.NfseRetorno;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NfseRetornoRepository extends JpaRepository<NfseRetorno, Long> {

    /**
     * A conversa inteira com a prefeitura sobre uma nota, da mais recente para
     * a mais antiga.
     *
     * <p>Filtra por empresa além do id da nota. A nota já é filtrada por tenant
     * em todo o resto do módulo, mas aqui a consulta parte do id e não do token:
     * sem o {@code empresa_id} no WHERE, um id adivinhado leria o retorno de
     * outra empresa.
     */
    @Query("""
            select r from NfseRetorno r
            where r.nfse.id = :nfseId and r.empresaId = :empresaId
            order by r.createdAt desc, r.id desc
            """)
    List<NfseRetorno> porNota(@Param("nfseId") Long nfseId, @Param("empresaId") Long empresaId);

    /**
     * As recusas de verdade, para a tela de "o que a prefeitura recusou".
     *
     * <p>Quando a chamada não chegou a ter resposta, {@code sucesso} fica nulo e
     * não entra aqui: não foi recusa, foi falha de infraestrutura, e juntar as
     * duas esconderia uma das duas.
     */
    @Query("""
            select r from NfseRetorno r
            where r.empresaId = :empresaId and r.sucesso = false
            order by r.createdAt desc, r.id desc
            """)
    List<NfseRetorno> recusas(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * Os casos em que não deu para saber se a nota saiu.
     *
     * <p>São os que exigem conferência na prefeitura. Ficam numa tela só
     * porque a pergunta que eles provocam é sempre a mesma: "saiu ou não?".
     */
    @Query("""
            select r from NfseRetorno r
            where r.empresaId = :empresaId and r.sucesso is null
            order by r.createdAt desc, r.id desc
            """)
    List<NfseRetorno> indecisos(@Param("empresaId") Long empresaId, Pageable pageable);
}
