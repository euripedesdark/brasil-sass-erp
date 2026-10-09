package br.com.brasil_saas.conhecimento.repository;

import br.com.brasil_saas.conhecimento.model.ArtigoKb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtigoKbRepository extends JpaRepository<ArtigoKb, Long> {
    List<ArtigoKb> findByEmpresaIdAndDeletedAtIsNullOrderByTituloAsc(Long empresaId);
    Optional<ArtigoKb> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    @Query("""
        SELECT a FROM ArtigoKb a WHERE a.empresaId = :empresaId AND a.deletedAt IS NULL
          AND a.publicado = true
          AND (:q IS NULL OR LOWER(a.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(a.conteudo,'')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(a.tags,'')) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY a.titulo
        """)
    List<ArtigoKb> buscar(@Param("empresaId") Long empresaId, @Param("q") String q);
}
