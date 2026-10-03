package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.TituloParcela;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TituloParcelaRepository extends JpaRepository<TituloParcela, Long> {
    List<TituloParcela> findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(Long tituloId);
    Optional<TituloParcela> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from TituloParcela p where p.id = :id and p.empresaId = :empresaId and p.deletedAt is null")
    Optional<TituloParcela> findForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);
    List<TituloParcela> findByEmpresaIdAndStatusAndDataVencimentoBetweenAndDeletedAtIsNullOrderByDataVencimento(
            Long empresaId, String status, LocalDate inicio, LocalDate fim);
}
