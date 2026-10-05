package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.StripeCustomerMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeCustomerMappingRepository extends JpaRepository<StripeCustomerMapping, Long> {
    Optional<StripeCustomerMapping> findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(Long empresaId, Long pessoaId);
}
