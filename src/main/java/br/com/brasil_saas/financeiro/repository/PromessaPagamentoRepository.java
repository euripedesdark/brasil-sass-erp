package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.PromessaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromessaPagamentoRepository extends JpaRepository<PromessaPagamento, Long> {
    List<PromessaPagamento> findByEmpresaIdOrderByDataPrometidaAsc(Long empresaId);
    List<PromessaPagamento> findByEmpresaIdAndStatusOrderByDataPrometidaAsc(Long empresaId, String status);
    Optional<PromessaPagamento> findByIdAndEmpresaId(Long id, Long empresaId);
    List<PromessaPagamento> findByEmpresaIdAndTituloIdOrderByCreatedAtDesc(Long empresaId, Long tituloId);
}
