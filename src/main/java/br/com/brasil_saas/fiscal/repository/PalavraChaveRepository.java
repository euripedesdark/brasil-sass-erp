package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.PalavraChave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PalavraChaveRepository extends JpaRepository<PalavraChave, Long> {

    /** Palavra exata. É o caminho mais quente da busca. */
    @Query("select p from PalavraChave p where p.palavra = :termo")
    List<PalavraChave> porPalavraExata(@Param("termo") String termo);

    /** Prefixo da palavra: o usuário digita "cervej" e o índice devolve "cerveja". */
    @Query("select p from PalavraChave p where p.palavra like concat(:prefixo, '%') "
            + "and p.palavra <> :prefixo")
    List<PalavraChave> porPrefixo(@Param("prefixo") String termo);
}
