package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Manifestacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManifestacaoRepository extends JpaRepository<Manifestacao, Long> {
    List<Manifestacao> findByEmpresaIdAndTipo(Long empresaId, String tipo);
    List<Manifestacao> findByEmpresaId(Long empresaId);
}
