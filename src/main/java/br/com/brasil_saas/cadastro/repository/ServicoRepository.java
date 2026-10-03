package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Page<Servico> findByDeletedAtIsNull(Pageable pageable);

    /**
     * Busca com filtros opcionais.
     *
     * <p>Os {@code cast(... as String)} nao sao cosmeticos: sem eles o
     * Postgres nao consegue inferir o tipo do parametro dentro de
     * {@code LOWER(?)} e assume {@code bytea}, o que derruba a consulta com
     * {@code function lower(bytea) does not exist}. O erro aparece como 500
     * em qualquer listagem com filtro por nome.
     */
    @Query("SELECT s FROM Servico s WHERE s.deletedAt IS NULL AND " +
           "(:nome IS NULL OR LOWER(s.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS String), '%'))) AND " +
           "(:codigo IS NULL OR s.codigo = :codigo) AND " +
           "(:ativo IS NULL OR s.ativo = :ativo)")
    Page<Servico> buscar(@Param("nome") String nome, @Param("codigo") String codigo,
                         @Param("ativo") Boolean ativo, Pageable pageable);

    boolean existsByCodigoAndDeletedAtIsNull(String codigo);
    boolean existsByCodigoAndIdNotAndDeletedAtIsNull(String codigo, Long id);
}
