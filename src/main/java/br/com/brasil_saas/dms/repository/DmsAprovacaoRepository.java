package br.com.brasil_saas.dms.repository;
import br.com.brasil_saas.dms.model.DmsAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DmsAprovacaoRepository extends JpaRepository<DmsAprovacao, Long> {
    Optional<DmsAprovacao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<DmsAprovacao> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<DmsAprovacao> findByDocumentoIdAndEmpresaIdAndDeletedAtIsNull(Long documentoId, Long empresaId);
}
