package br.com.brasil_saas.producao.repository;

import br.com.brasil_saas.producao.model.Producao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProducaoRepository extends JpaRepository<Producao, Long> {
    Optional<Producao> findByNumeroAndEmpresaId(String numero, Long empresaId);

    /**
     * Ordens de uma empresa, com os itens na mesma consulta.
     *
     * O service antes fazia findAll() e filtrava em Java: trazia as ordens de
     * TODOS os tenants para a memoria do processo e devolvia a lista filtrada.
     * Alem do custo, os itens sao lazy e a sessao ja tinha fechado quando o
     * Jackson ia serializar — com uma ordem cadastrada, a listagem devolvia 500
     * (LazyInitializationException). O EntityGraph traz os itens na consulta.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "itens")
    java.util.List<Producao> findByEmpresaIdOrderByDataInicioDesc(Long empresaId);
}
