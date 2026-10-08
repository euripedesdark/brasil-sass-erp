package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.DepreciacaoExecucao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepreciacaoExecucaoRepository extends JpaRepository<DepreciacaoExecucao, Long> {
    List<DepreciacaoExecucao> findAllByEmpresaIdAndDeletedAtIsNullOrderByPeriodoDescIdDesc(Long empresaId);
}
