package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.PlanoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanoManutencaoRepository extends JpaRepository<PlanoManutencao, Long> {
    List<PlanoManutencao> findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(Long empresaId);
}
