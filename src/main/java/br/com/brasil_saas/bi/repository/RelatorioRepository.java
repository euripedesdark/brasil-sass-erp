package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.Relatorio;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioRepository extends TenantRepository<Relatorio, Long> {

    List<Relatorio> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<Relatorio> findByEmpresaIdAndCategoria(Long empresaId, String categoria);

    List<Relatorio> findByEmpresaIdAndTipo(Long empresaId, String tipo);

    List<Relatorio> findByEmpresaIdAndAgendadoTrue(Long empresaId);

    List<Relatorio> findByEmpresaIdAndCriadoPor(Long empresaId, Long criadoPor);

    @Query("SELECT r FROM Relatorio r WHERE r.empresaId = :empresaId AND r.ativo = true ORDER BY r.nome")
    List<Relatorio> findAllActiveByEmpresaId(Long empresaId);

    @Query("SELECT COUNT(r) FROM Relatorio r WHERE r.empresaId = :empresaId AND r.ativo = true")
    long countByEmpresaId(Long empresaId);

    @Query("SELECT r FROM Relatorio r WHERE r.empresaId = :empresaId AND r.categoria = :categoria AND r.ativo = true")
    List<Relatorio> findByEmpresaIdAndCategoriaAndAtivo(Long empresaId, String categoria);
}
