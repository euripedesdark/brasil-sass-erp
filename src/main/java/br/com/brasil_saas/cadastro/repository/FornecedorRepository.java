package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    Page<Fornecedor> findByDeletedAtIsNull(Pageable pageable);
    Page<Fornecedor> findByEmpresaIdAndDeletedAtIsNull(Long empresaId, Pageable pageable);
    Optional<Fornecedor> findByPessoaIdAndDeletedAtIsNull(Long pessoaId);
    Optional<Fornecedor> findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(Long empresaId, Long pessoaId);
    boolean existsByPessoaIdAndDeletedAtIsNull(Long pessoaId);
}
