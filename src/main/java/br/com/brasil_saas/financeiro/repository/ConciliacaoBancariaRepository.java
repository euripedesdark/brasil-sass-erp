package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.ConciliacaoBancaria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConciliacaoBancariaRepository extends JpaRepository<ConciliacaoBancaria, Long> {
    List<ConciliacaoBancaria> findByEmpresaIdAndDeletedAtIsNullOrderByDataFimDesc(Long empresaId);
    Optional<ConciliacaoBancaria> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}