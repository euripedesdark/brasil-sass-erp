package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.RegraTributaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RegraTributariaRepository extends JpaRepository<RegraTributaria, Long> {
    @Query("""
        SELECT r FROM RegraTributaria r
        WHERE r.empresaId = :empresaId AND r.ativa = true
          AND (:ncm IS NULL OR r.ncm = :ncm)
          AND (:cfop IS NULL OR r.cfop = :cfop)
        ORDER BY r.prioridade DESC, r.id
    """)
    Optional<RegraTributaria> buscarRegra(@Param("empresaId") Long empresaId,
                                          @Param("ncm") String ncm,
                                          @Param("cfop") String cfop);
}
