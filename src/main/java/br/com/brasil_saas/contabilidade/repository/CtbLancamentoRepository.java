package br.com.brasil_saas.contabilidade.repository;
import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface CtbLancamentoRepository extends JpaRepository<CtbLancamento, Long> {
    Optional<CtbLancamento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<CtbLancamento> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<CtbLancamento> findByEmpresaIdAndPeriodoAndDeletedAtIsNullOrderByDataDescIdDesc(Long empresaId, String periodo);
    List<CtbLancamento> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status);
    List<CtbLancamento> findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
            Long empresaId, String origemTipo, Long origemId, Collection<String> status);

    // Serializa estornos concorrentes do mesmo lancamento.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM CtbLancamento l WHERE l.id = :id AND l.empresaId = :empresaId AND l.deletedAt IS NULL")
    Optional<CtbLancamento> findByIdForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
