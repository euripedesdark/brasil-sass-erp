package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Caixa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {

    Page<Caixa> findByEmpresaIdAndDeletedAtIsNullOrderByNome(Long empresaId, Pageable pageable);

    Optional<Caixa> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    List<Caixa> findByEmpresaIdAndDeletedAtIsNullOrderByNome(Long empresaId);

    Optional<Caixa> findByEmpresaIdAndNomeIgnoreCaseAndDeletedAtIsNull(Long empresaId, String nome);

    /**
     * Busca por nome, ignorando acentos e caixa.
     *
     * O ILIKE simples nao acha "acucar" digitado como "açúcar".
     *
     * unaccent e funcao do Postgres, nao funcao HQL: tem que ser chamada
     * pelo function(), senao a query nem chega a ser validada.
     */
    @Query("""
            SELECT c FROM Caixa c
            WHERE c.empresaId = :empresaId
              AND c.deletedAt IS NULL
              AND cast(function('unaccent', lower(c.nome)) as String)
                  LIKE cast(function('unaccent', lower(concat('%', :termo, '%'))) as String)
            ORDER BY c.nome
            """)
    Page<Caixa> buscar(@Param("empresaId") Long empresaId,
                       @Param("termo") String termo,
                       Pageable pageable);
}
