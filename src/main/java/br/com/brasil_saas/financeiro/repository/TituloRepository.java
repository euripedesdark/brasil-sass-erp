package br.com.brasil_saas.financeiro.repository;
import br.com.brasil_saas.financeiro.model.Titulo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface TituloRepository extends JpaRepository<Titulo, Long> {
    List<Titulo> findByEmpresaIdAndDeletedAtIsNullOrderByDataVencimento(Long empresaId);
    List<Titulo> findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(Long empresaId, Long pessoaId);
    List<Titulo> findByEmpresaIdAndStatusAndDeletedAtIsNullOrderByDataVencimento(Long empresaId, String status);
    Optional<Titulo> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Titulo t where t.id = :id and t.empresaId = :empresaId and t.deletedAt is null")
    Optional<Titulo> findForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);
}
