package br.com.brasil_saas.core.repository;

import br.com.brasil_saas.core.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BancoRepository extends JpaRepository<Banco, String> {

    List<Banco> findTop50ByAtivoTrueOrderByNome();

    /**
     * Busca por nome ou codigo, ignorando acentos.
     *
     * A pessoa digita "itau", "Itaú" ou "341" e espera achar a mesma coisa.
     * Sem o unaccent, "itau" nao acha "Itaú" — e o cadastro de conta
     * bancaria perderia o banco mais usado do pais na busca.
     */
    @Query("""
            SELECT b FROM Banco b
            WHERE b.ativo = true
              AND (b.compe = :termo
                   OR cast(function('unaccent', lower(b.nome)) as String)
                      LIKE cast(function('unaccent', lower(concat('%', :termo, '%'))) as String))
            ORDER BY b.nome
            """)
    List<Banco> buscar(@Param("termo") String termo);
}
