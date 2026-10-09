package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.CobrancaAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CobrancaAcaoRepository extends JpaRepository<CobrancaAcao, Long> {
    List<CobrancaAcao> findByEmpresaIdAndTituloIdOrderByCreatedAtDesc(Long empresaId, Long tituloId);
    List<CobrancaAcao> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);

    @Query("select coalesce(max(c.nivel), 0) from CobrancaAcao c where c.empresaId = :empresaId and c.tituloId = :tituloId")
    Integer maxNivel(@Param("empresaId") Long empresaId, @Param("tituloId") Long tituloId);
}
