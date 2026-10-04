package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Extrato;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtratoRepository extends JpaRepository<Extrato, Long> {
    List<Extrato> findByContaBancariaIdAndDeletedAtIsNullOrderByDataMovimento(Long contaBancariaId);
    List<Extrato> findByEmpresaIdAndConciliadoFalseAndDeletedAtIsNull(Long empresaId);
    boolean existsByEmpresaIdAndContaBancariaIdAndFitidAndDeletedAtIsNull(Long empresaId, Long contaBancariaId, String fitid);
    Optional<Extrato> findTopByContaBancariaIdAndDeletedAtIsNullOrderByDataMovimentoDescIdDesc(Long contaBancariaId);
}