package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.ContaBancaria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    List<ContaBancaria> findByEmpresaIdAndAtivaTrueAndDeletedAtIsNullOrderByBanco(Long empresaId);
    Optional<ContaBancaria> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ContaBancaria c where c.id = :id and c.empresaId = :empresaId and c.ativa = true and c.deletedAt is null")
    Optional<ContaBancaria> findForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);
}