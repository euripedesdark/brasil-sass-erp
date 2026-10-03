package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.CentroCusto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CentroCustoRepository extends JpaRepository<CentroCusto, Long> {
    List<CentroCusto> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByCodigo(Long empresaId);
    Optional<CentroCusto> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
