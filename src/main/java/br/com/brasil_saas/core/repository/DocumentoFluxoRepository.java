package br.com.brasil_saas.core.repository;

import br.com.brasil_saas.core.model.DocumentoFluxo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentoFluxoRepository extends JpaRepository<DocumentoFluxo, Long> {
    List<DocumentoFluxo> findByEmpresaIdAndOrigemTipoAndOrigemIdOrderByCreatedAtAsc(
            Long empresaId, String origemTipo, Long origemId);

    List<DocumentoFluxo> findByEmpresaIdAndDestinoTipoAndDestinoIdOrderByCreatedAtAsc(
            Long empresaId, String destinoTipo, Long destinoId);

    @Query("""
        select f from DocumentoFluxo f
        where f.empresaId = :empresaId
          and ((f.origemTipo = :tipo and f.origemId = :id)
            or (f.destinoTipo = :tipo and f.destinoId = :id))
        order by f.createdAt asc
        """)
    List<DocumentoFluxo> porDocumento(@Param("empresaId") Long empresaId,
                                      @Param("tipo") String tipo,
                                      @Param("id") Long id);
}
