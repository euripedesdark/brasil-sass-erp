package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConferenciaFaturaCompraRepository extends JpaRepository<ConferenciaFaturaCompra, Long> {
    List<ConferenciaFaturaCompra> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);
    Optional<ConferenciaFaturaCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    // Reavaliar um documento substitui a conferencia anterior desse recebimento,
    // mas nao libera divergencias de outros documentos do mesmo pedido.
    @Query("""
        SELECT c FROM ConferenciaFaturaCompra c
        WHERE c.empresaId = :empresaId AND c.deletedAt IS NULL
          AND (c.tituloId = :tituloId OR c.pedidoId IN (
              SELECT p.id FROM PedidoCompra p
              WHERE p.empresaId = :empresaId AND p.tituloId = :tituloId
                AND p.deletedAt IS NULL))
          AND NOT EXISTS (
              SELECT posterior.id FROM ConferenciaFaturaCompra posterior
              WHERE posterior.empresaId = c.empresaId AND posterior.deletedAt IS NULL
                AND posterior.pedidoId = c.pedidoId AND posterior.id > c.id
                AND (posterior.nfeId = c.nfeId OR
                     (posterior.nfeId IS NULL AND c.nfeId IS NULL))
                AND (posterior.recebimentoId = c.recebimentoId OR
                     (posterior.recebimentoId IS NULL AND c.recebimentoId IS NULL)))
        """)
    List<ConferenciaFaturaCompra> findVigentesParaTitulo(
            @Param("empresaId") Long empresaId, @Param("tituloId") Long tituloId);

    // Conferencias aprovadas e vigentes que ja consumiram este recebimento com
    // outra NF, ou esta NF com outro recebimento. Uma reavaliacao do mesmo par
    // substitui a anterior e por isso nao entra aqui.
    @Query("""
        SELECT c FROM ConferenciaFaturaCompra c
        WHERE c.empresaId = :empresaId AND c.deletedAt IS NULL
          AND c.status = 'APROVADA'
          AND ((c.recebimentoId = :recebimentoId AND (c.nfeId IS NULL OR c.nfeId <> :nfeId))
            OR (c.nfeId = :nfeId AND (c.recebimentoId IS NULL OR c.recebimentoId <> :recebimentoId)))
          AND NOT EXISTS (
              SELECT posterior.id FROM ConferenciaFaturaCompra posterior
              WHERE posterior.empresaId = c.empresaId AND posterior.deletedAt IS NULL
                AND posterior.pedidoId = c.pedidoId AND posterior.id > c.id
                AND posterior.nfeId = c.nfeId AND posterior.recebimentoId = c.recebimentoId)
        """)
    List<ConferenciaFaturaCompra> findConsumosConflitantes(
            @Param("empresaId") Long empresaId,
            @Param("recebimentoId") Long recebimentoId,
            @Param("nfeId") Long nfeId);
}
