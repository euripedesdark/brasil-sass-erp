package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Cest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CestRepository extends JpaRepository<Cest, Long> {
    Page<Cest> findByDescricaoContainingIgnoreCaseOrderByCodigo(String descricao, Pageable pageable);
    Optional<Cest> findByCodigo(String codigo);
    List<Cest> findByNcmOrderByCodigo(String ncm);

    /**
     * Busca unica por codigo, descricao OU NCM.
     *
     * <p>Por que uma query e nao tres metodos: a tela tem UM campo de busca e a
     * pessoa nao sabe de antemao em qual dos tres o CEST vai aparecer. Sao
     * 1.043 linhas paginadas de 50 em 50, entao filtrar no cliente so
     * mostraria o que estivesse na pagina atual — e a tela viraria "nao achei"
     * para o CEST que existe, na pagina 7. Isso ja aconteceu: a busca existia
     * e nao filtrava nada.
     *
     * <p>O NCM e comparado sem pontuacao dos dois lados. A coluna guarda 8
     * digitos ({@code 84712000}, como o ERP ja guardava em bc_fis_ncm) e quem
     * digita digita do jeito da nota ({@code 8471.20.00}). Comparar o que esta
     * gravado com o que foi digitado, sem normalizar, nunca acha.
     *
     * <p>E ha a segunda metade: 407 dos 1.043 CEST estao gravados com NCM
     * INCOMPLETO, porque o campo {@code ncms} do OCA lista em varias
     * precisoes — {@code 847130}, {@code 84714}, e ate {@code 33}. Sao 2 a 7
     * digitos, nunca 8. Completar com zero a esquerda NAO resolve e nem pode:
     * conferir os 404 contra a tabela de NCM, so 16 casariam, e {@code 33} viraria
     * {@code 00000033}, que nao e NCM de nada. O OCA nao truncou por engano,
     * registrou o NCM no nivel da familia.
     *
     * <p>Por isso a busca tambem aceita o NCM gravado como PREFIXO do termo.
     * Quem busca {@code 84713000} — o codigo real, com 8 digitos — acha o CEST
     * que vale para a familia {@code 847130}. Sem essa metade da busca, esses
     * 407 CEST sao inalcancaveis pelo NCM: e ninguem vai digitar
     * {@code 847130} quando o NCM da nota tem 8 digitos.
     *
     * <p>A familia exige {@code ncm} nao vazio de proposito. Sem essa guarda,
     * os 3 CEST que a origem traz sem NCM listavam em toda busca: com o campo
     * vazio, o padrao montado vira {@code '%'}, que casa com qualquer termo.
     * E o pior tipo de bug de busca: a pessoa ve resultado que nao tem nada a
     * ver com o que digitou, e acredita.
     */
    @Query("SELECT c FROM Cest c WHERE "
            + "UPPER(c.codigo) LIKE UPPER(CONCAT('%', :busca, '%')) "
            + "OR UPPER(c.descricao) LIKE UPPER(CONCAT('%', :busca, '%')) "
            // NCM exato ou por familia: o gravado casa com o digitado nos dois
            // sentidos, porque um dos dois e o prefixo do outro.
            + "OR REPLACE(REPLACE(REPLACE(COALESCE(c.ncm, ''), '.', ''), '-', ''), '/', '') "
            + "   LIKE CONCAT('%', "
            + "              REPLACE(REPLACE(REPLACE(:busca, '.', ''), '-', ''), '/', ''), "
            + "              '%') "
            + "OR (:buscaNormalizada <> '' AND COALESCE(c.ncm, '') <> '' "
            + "    AND :buscaNormalizada LIKE "
            + "        CONCAT(REPLACE(REPLACE(REPLACE(COALESCE(c.ncm, ''), '.', ''), '-', ''), '/', ''), '%') "
            + "    AND :buscaNormalizada <> REPLACE(REPLACE(REPLACE(COALESCE(c.ncm, ''), '.', ''), '-', ''), '/', ''))")
    Page<Cest> buscar(@Param("busca") String busca,
                      @Param("buscaNormalizada") String buscaNormalizada,
                      Pageable pageable);
}
