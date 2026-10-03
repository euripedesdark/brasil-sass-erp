package br.com.brasil_saas.dms.repository;
import br.com.brasil_saas.dms.model.DmsVersao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DmsVersaoRepository extends JpaRepository<DmsVersao, Long> {
    Optional<DmsVersao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<DmsVersao> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<DmsVersao> findByDocumentoIdAndEmpresaIdAndDeletedAtIsNull(Long documentoId, Long empresaId);
}
