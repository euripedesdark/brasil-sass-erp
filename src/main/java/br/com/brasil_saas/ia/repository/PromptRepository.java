package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.Prompt;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptRepository extends TenantRepository<Prompt, Long> {

    List<Prompt> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<Prompt> findByEmpresaIdAndPublicoTrue(Long empresaId);

    List<Prompt> findByEmpresaIdAndCategoria(Long empresaId, String categoria);

    List<Prompt> findByEmpresaIdAndFavoritoTrue(Long empresaId);

    List<Prompt> findByEmpresaIdAndCriadoPor(Long empresaId, Long criadoPor);

    @Query("SELECT p FROM Prompt p WHERE p.empresaId = :empresaId AND p.ativo = true ORDER BY p.contadorUso DESC")
    List<Prompt> findMaisUsados(Long empresaId);

    @Query("SELECT p FROM Prompt p WHERE p.empresaId = :empresaId AND p.ativo = true AND (p.nome ILIKE %:termo% OR p.descricao ILIKE %:termo% OR p.conteudo ILIKE %:termo%)")
    List<Prompt> searchByTermo(Long empresaId, String termo);

    @Query("SELECT COUNT(p) FROM Prompt p WHERE p.empresaId = :empresaId AND p.ativo = true")
    long countByEmpresaId(Long empresaId);
}
