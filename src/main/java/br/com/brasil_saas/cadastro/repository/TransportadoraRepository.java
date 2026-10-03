package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Transportadora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransportadoraRepository extends JpaRepository<Transportadora, Long> {
    Page<Transportadora> findByDeletedAtIsNull(Pageable pageable);
    Page<Transportadora> findByEmpresaIdAndDeletedAtIsNull(Long empresaId, Pageable pageable);
    Optional<Transportadora> findByPessoaIdAndDeletedAtIsNull(Long pessoaId);
    Optional<Transportadora> findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(Long empresaId, Long pessoaId);
    boolean existsByPessoaIdAndDeletedAtIsNull(Long pessoaId);
}
