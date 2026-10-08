package br.com.brasil_saas.vendas.devolucao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public interface VenDevolucaoRepository extends JpaRepository<VenDevolucao, Long> {
    Optional<VenDevolucao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<VenDevolucao> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
    List<VenDevolucao> findByPedidoIdAndEmpresaIdAndStatusInAndDeletedAtIsNull(
            Long pedidoId, Long empresaId, Collection<String> status);

    // Trava a devolucao para que dois recebimentos simultaneos nao somem o estoque duas vezes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM VenDevolucao d WHERE d.id = :id AND d.empresaId = :empresaId AND d.deletedAt IS NULL")
    Optional<VenDevolucao> findByIdForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
