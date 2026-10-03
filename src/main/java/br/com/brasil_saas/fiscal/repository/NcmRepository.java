package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Ncm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NcmRepository extends JpaRepository<Ncm, Long> {
    Page<Ncm> findByDescricaoContainingIgnoreCaseOrderByCodigo(String descricao, Pageable pageable);
    Page<Ncm> findAllByOrderByCodigo(Pageable pageable);
    Optional<Ncm> findByCodigo(String codigo);
}
