package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.AtivoMovimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtivoMovimentoRepository extends JpaRepository<AtivoMovimento, Long> {
    List<AtivoMovimento> findAllByEmpresaIdAndAtivoIdAndDeletedAtIsNullOrderByDataMovimentoAscIdAsc(Long empresaId, Long ativoId);
    List<AtivoMovimento> findAllByExecucaoIdAndDeletedAtIsNull(Long execucaoId);
    List<AtivoMovimento> findAllByEmpresaIdAndDataMovimentoBetweenAndDeletedAtIsNullOrderByDataMovimentoAscIdAsc(Long empresaId, java.time.LocalDate de, java.time.LocalDate ate);
}
