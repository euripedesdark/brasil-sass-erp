package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.Classificacao;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassificacaoRepository extends TenantRepository<Classificacao, Long> {

    List<Classificacao> findByEmpresaIdAndTipo(Long empresaId, String tipo);

    List<Classificacao> findByEmpresaIdAndEntidadeId(Long empresaId, Long entidadeId);

    List<Classificacao> findByEmpresaIdAndStatus(Long empresaId, String status);

    List<Classificacao> findByEmpresaIdAndTipoAndStatus(Long empresaId, String tipo, String status);
    
    List<Classificacao> findByEmpresaId(Long empresaId);

    @Query("SELECT c FROM Classificacao c WHERE c.empresaId = :empresaId AND c.tipo = :tipo AND c.confianca >= 0.7")
    List<Classificacao> findHighConfidenceByTipo(Long empresaId, String tipo);

    @Query("SELECT c FROM Classificacao c WHERE c.empresaId = :empresaId AND c.status = 'PENDENTE' AND c.tipo = :tipo")
    List<Classificacao> findPendentesByTipo(Long empresaId, String tipo);

    @Query("SELECT COUNT(c) FROM Classificacao c WHERE c.empresaId = :empresaId AND c.tipo = :tipo")
    long countByTipo(Long empresaId, String tipo);

    @Query("SELECT AVG(c.confianca) FROM Classificacao c WHERE c.empresaId = :empresaId AND c.tipo = :tipo")
    Double avgConfiancaByTipo(Long empresaId, String tipo);
}
