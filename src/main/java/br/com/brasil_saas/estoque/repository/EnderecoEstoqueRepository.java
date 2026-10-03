package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.EnderecoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnderecoEstoqueRepository extends JpaRepository<EnderecoEstoque, Long> {
    List<EnderecoEstoque> findByEmpresaIdAndDepositoIdAndAtivoTrueOrderByCodigoAsc(Long empresaId, Long depositoId);
    Optional<EnderecoEstoque> findByIdAndEmpresaIdAndAtivoTrue(Long id, Long empresaId);
    Optional<EnderecoEstoque> findByEmpresaIdAndDepositoIdAndCodigoAndAtivoTrue(Long empresaId, Long depositoId, String codigo);
}