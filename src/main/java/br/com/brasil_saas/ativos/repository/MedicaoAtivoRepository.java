package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.MedicaoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicaoAtivoRepository extends JpaRepository<MedicaoAtivo, Long> {
    List<MedicaoAtivo> findAllByEmpresaIdAndAtivoIdAndDeletedAtIsNullOrderByDataMedicaoDescIdDesc(Long empresaId, Long ativoId);
    List<MedicaoAtivo> findAllByEmpresaIdAndDeletedAtIsNullOrderByDataMedicaoDescIdDesc(Long empresaId);
}
