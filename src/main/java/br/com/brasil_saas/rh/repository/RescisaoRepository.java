package br.com.brasil_saas.rh.repository;

import br.com.brasil_saas.rh.model.Rescisao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RescisaoRepository extends JpaRepository<Rescisao, Long> {
    List<Rescisao> findByEmpresaIdAndDeletedAtIsNullOrderByDataDesligamentoDesc(Long empresaId);
    Optional<Rescisao> findByEmpresaIdAndFuncionarioIdAndDeletedAtIsNull(Long empresaId, Long funcionarioId);
}
