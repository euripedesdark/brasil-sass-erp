package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.ReservaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReservaEstoqueRepository extends JpaRepository<ReservaEstoque, Long> {
    List<ReservaEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataReservaDesc(Long empresaId);
    List<ReservaEstoque> findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(Long empresaId, Long pedidoVendaId);
    Optional<ReservaEstoque> findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndDeletedAtIsNull(
            Long empresaId, Long pedidoVendaId, Long depositoId, Long produtoId);

    Optional<ReservaEstoque> findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndLoteIdAndDeletedAtIsNull(
            Long empresaId, Long pedidoVendaId, Long depositoId, Long produtoId, Long loteId);

    @Query("select coalesce(sum(r.quantidade),0) from ReservaEstoque r where r.empresaId = :empresaId and r.depositoId = :depositoId and r.produtoId = :produtoId and r.loteId = :loteId and r.deletedAt is null and r.status in ('RESERVADA','SEPARACAO')")
    BigDecimal sumAtivasPorLote(@Param("empresaId") Long empresaId, @Param("depositoId") Long depositoId,
                                @Param("produtoId") Long produtoId, @Param("loteId") Long loteId);

    @Query("select coalesce(sum(r.quantidade),0) from ReservaEstoque r where r.empresaId = :empresaId and r.depositoId = :depositoId and r.produtoId = :produtoId and r.deletedAt is null and r.status in ('RESERVADA','SEPARACAO')")
    BigDecimal sumAtivas(@Param("empresaId") Long empresaId, @Param("depositoId") Long depositoId, @Param("produtoId") Long produtoId);

    @Query("select coalesce(sum(r.quantidade),0) from ReservaEstoque r where r.empresaId = :empresaId and r.depositoId = :depositoId and r.produtoId = :produtoId and r.enderecoId = :enderecoId and r.deletedAt is null and r.status in ('RESERVADA','SEPARACAO')")
    BigDecimal sumAtivasPorEndereco(@Param("empresaId") Long empresaId, @Param("depositoId") Long depositoId,
                                    @Param("produtoId") Long produtoId, @Param("enderecoId") Long enderecoId);

    @Query("select coalesce(sum(r.quantidade),0) from ReservaEstoque r where r.empresaId = :empresaId and r.depositoId = :depositoId and r.produtoId = :produtoId and r.enderecoId = :enderecoId and r.loteId = :loteId and r.deletedAt is null and r.status in ('RESERVADA','SEPARACAO')")
    BigDecimal sumAtivasPorEnderecoELote(@Param("empresaId") Long empresaId, @Param("depositoId") Long depositoId,
                                         @Param("produtoId") Long produtoId, @Param("enderecoId") Long enderecoId,
                                         @Param("loteId") Long loteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservaEstoque r where r.id = :id and r.empresaId = :empresaId and r.deletedAt is null")
    Optional<ReservaEstoque> findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(@Param("id") Long id, @Param("empresaId") Long empresaId);
}