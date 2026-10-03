package br.com.brasil_saas.producao.repository;

import br.com.brasil_saas.producao.model.AlocacaoCapacidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AlocacaoCapacidadeRepository extends JpaRepository<AlocacaoCapacidade, Long> {
    List<AlocacaoCapacidade> findByEmpresaIdAndCentroTrabalhoIdAndDataBetweenAndDeletedAtIsNull(
            Long empresaId, Long centroTrabalhoId, LocalDate de, LocalDate ate);
    List<AlocacaoCapacidade> findByEmpresaIdAndOrdemProducaoIdAndDeletedAtIsNull(Long empresaId, Long ordemProducaoId);
    Optional<AlocacaoCapacidade> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
