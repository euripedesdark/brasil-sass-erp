package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.ManutencaoApontamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManutencaoApontamentoRepository extends JpaRepository<ManutencaoApontamento, Long> {
    List<ManutencaoApontamento> findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderByDataApontamentoAscIdAsc(Long empresaId, Long manutencaoId);
}
