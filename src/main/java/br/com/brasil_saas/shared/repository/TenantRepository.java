package br.com.brasil_saas.shared.repository;

import br.com.brasil_saas.shared.model.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface TenantRepository<T extends TenantEntity, ID> extends JpaRepository<T, ID> {
    // As entidades filhas declaram seus proprios metodos findByEmpresaId/findAllByEmpresaId;
    // aqui apenas garante que toda entidade tenant herde o padrao multi-tenant de BaseEntity/TenantEntity.
}
