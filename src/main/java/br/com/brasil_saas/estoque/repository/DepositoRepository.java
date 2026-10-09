package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Deposito d where d.id = :id and d.empresaId = :empresaId and d.ativo = true and d.deletedAt is null")
    Optional<Deposito> findAtivoForUpdate(@org.springframework.data.repository.query.Param("id") Long id,
                                      @org.springframework.data.repository.query.Param("empresaId") Long empresaId);
    Optional<Deposito> findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(Long empresaId, String tipo);
    Optional<Deposito> findFirstByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(Long empresaId);
    List<Deposito> findByEmpresaIdAndAtivoTrueOrderByNomeAsc(Long empresaId);
    Optional<Deposito> findByEmpresaIdAndCodigoAndAtivoTrue(Long empresaId, String codigo);
    Optional<Deposito> findByIdAndEmpresaIdAndAtivoTrue(Long id, Long empresaId);
}
