package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Imposto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImpostoRepository extends JpaRepository<Imposto, Long> {
    List<Imposto> findByEmpresaIdAndTipoAndDeletedAtIsNullOrderBySigla(Long empresaId, String tipo);
    List<Imposto> findByEmpresaIdAndDeletedAtIsNullOrderBySigla(Long empresaId);
    Optional<Imposto> findByEmpresaIdAndSiglaAndDeletedAtIsNull(Long empresaId, String sigla);
}
