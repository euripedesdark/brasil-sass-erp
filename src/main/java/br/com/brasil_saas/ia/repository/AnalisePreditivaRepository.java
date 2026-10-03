package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.AnalisePreditiva;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalisePreditivaRepository extends TenantRepository<AnalisePreditiva, Long> {

    List<AnalisePreditiva> findByEmpresaId(Long empresaId);

    List<AnalisePreditiva> findByEmpresaIdAndTipo(Long empresaId, String tipo);

    List<AnalisePreditiva> findByEmpresaIdAndEntidadeAndEntidadeId(Long empresaId, String entidade, Long entidadeId);

    List<AnalisePreditiva> findByEmpresaIdAndStatus(Long empresaId, String status);

    List<AnalisePreditiva> findByEmpresaIdAndDataProximaAnaliseBefore(Long empresaId, LocalDate data);

    @Query("SELECT a FROM AnalisePreditiva a WHERE a.empresaId = :empresaId AND a.tipo = :tipo AND a.confianca >= 0.7 ORDER BY a.dataAnalise DESC")
    List<AnalisePreditiva> findHighConfidenceByTipo(Long empresaId, String tipo);

    @Query("SELECT a FROM AnalisePreditiva a WHERE a.empresaId = :empresaId AND a.dataProximaAnalise <= CURRENT_DATE ORDER BY a.dataProximaAnalise")
    List<AnalisePreditiva> findPendentesByEmpresa(Long empresaId);

    @Query("SELECT COUNT(a) FROM AnalisePreditiva a WHERE a.empresaId = :empresaId AND a.tipo = :tipo")
    long countByTipo(Long empresaId, String tipo);
}
