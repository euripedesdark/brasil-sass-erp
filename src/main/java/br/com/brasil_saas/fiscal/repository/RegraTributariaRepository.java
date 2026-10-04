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
          AND (r.ufOrigem IS NULL OR r.ufOrigem = :ufOrigem)
          AND (r.ufDestino IS NULL OR r.ufDestino = :ufDestino)
        ORDER BY
          CASE WHEN r.ncm = :ncm THEN 1 ELSE 0 END DESC,
          CASE WHEN r.cfop = :cfop THEN 1 ELSE 0 END DESC,
          CASE WHEN r.ufOrigem = :ufOrigem THEN 1 ELSE 0 END DESC,
          CASE WHEN r.ufDestino = :ufDestino THEN 1 ELSE 0 END DESC,
          r.prioridade DESC, r.id
    """)
    Optional<RegraTributaria> buscarRegra(@Param("empresaId") Long empresaId,
                                          @Param("ncm") String ncm,
                                          @Param("cfop") String cfop,
                                          @Param("ufOrigem") String ufOrigem,
                                          @Param("ufDestino") String ufDestino);
}