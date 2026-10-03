package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Nfe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NfeRepository extends JpaRepository<Nfe, Long> {
    Optional<Nfe> findByEmpresaIdAndChaveAcesso(Long empresaId, String chaveAcesso);
    Page<Nfe> findByEmpresaIdAndStatusOrderByDataEmissaoDesc(Long empresaId, String status, Pageable pageable);
    Page<Nfe> findByEmpresaIdOrderByDataEmissaoDesc(Long empresaId, Pageable pageable);
    java.util.Optional<br.com.brasil_saas.fiscal.model.Nfe> findByEmpresaIdAndChaveAcessoAndDeletedAtIsNull(Long empresaId, String chaveAcesso);
    org.springframework.data.domain.Page<br.com.brasil_saas.fiscal.model.Nfe> findByEmpresaIdAndTipoOperacaoAndDeletedAtIsNull(Long empresaId, String tipoOperacao, org.springframework.data.domain.Pageable pageable);
    java.util.Optional<br.com.brasil_saas.fiscal.model.Nfe> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
