package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.ContratoFornecimento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContratoFornecimentoRepository extends JpaRepository<ContratoFornecimento, Long> {
    Optional<ContratoFornecimento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<ContratoFornecimento> findByEmpresaIdAndDeletedAtIsNullOrderByVigenciaFimAsc(Long empresaId);
    List<ContratoFornecimento> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status);
    boolean existsByEmpresaIdAndNumeroAndDeletedAtIsNull(Long empresaId, String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContratoFornecimento c WHERE c.id = :id AND c.empresaId = :empresaId AND c.deletedAt IS NULL")
    Optional<ContratoFornecimento> findByIdForUpdate(Long id, Long empresaId);
}
