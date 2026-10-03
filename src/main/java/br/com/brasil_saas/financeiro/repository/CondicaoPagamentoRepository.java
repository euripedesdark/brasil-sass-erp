package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.CondicaoPagamento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CondicaoPagamentoRepository extends JpaRepository<CondicaoPagamento, Long> {
    List<CondicaoPagamento> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByDescricao(Long empresaId);
    Optional<CondicaoPagamento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
