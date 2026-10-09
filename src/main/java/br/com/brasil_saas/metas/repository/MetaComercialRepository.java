package br.com.brasil_saas.metas.repository;

import br.com.brasil_saas.metas.model.MetaComercial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaComercialRepository extends JpaRepository<MetaComercial, Long> {
    List<MetaComercial> findByEmpresaIdAndDeletedAtIsNullOrderByAnoDescMesDesc(Long empresaId);
    List<MetaComercial> findByEmpresaIdAndAnoAndMesAndDeletedAtIsNull(Long empresaId, Integer ano, Integer mes);
    Optional<MetaComercial> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
