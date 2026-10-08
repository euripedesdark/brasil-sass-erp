package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.ManutencaoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManutencaoMaterialRepository extends JpaRepository<ManutencaoMaterial, Long> {
    List<ManutencaoMaterial> findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderById(Long empresaId, Long manutencaoId);
}
