package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Municipio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {

    Optional<Municipio> findByCodigoIbge(String codigoIbge);

    List<Municipio> findByUfOrderByNome(String uf);

    Page<Municipio> findByNomeContainingIgnoreCaseOrderByNome(String nome, Pageable pageable);

    @Query("""
        SELECT m
        FROM Municipio m
        WHERE (:codigo IS NULL OR LOWER(m.codigoIbge) LIKE LOWER(CONCAT('%', CAST(:codigo AS String), '%')))
          AND (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS String), '%')))
        ORDER BY m.nome ASC
        """)
    Page<Municipio> buscarPorFiltros(
            @Param("codigo") String codigo,
            @Param("nome") String nome,
            Pageable pageable);
}
