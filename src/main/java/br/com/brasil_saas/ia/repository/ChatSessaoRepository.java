package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.ChatSessao;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatSessaoRepository extends TenantRepository<ChatSessao, Long> {

    List<ChatSessao> findByEmpresaIdAndUsuarioId(Long empresaId, Long usuarioId);

    List<ChatSessao> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<ChatSessao> findByEmpresaIdAndFavoritoTrue(Long empresaId);

    List<ChatSessao> findByEmpresaIdAndDataCriacaoAfter(Long empresaId, LocalDateTime data);

    @Query("SELECT s FROM ChatSessao s WHERE s.empresaId = :empresaId AND s.usuarioId = :usuarioId ORDER BY s.dataAtualizacao DESC")
    List<ChatSessao> findRecentByUsuario(Long empresaId, Long usuarioId);

    @Query("SELECT COUNT(s) FROM ChatSessao s WHERE s.empresaId = :empresaId AND s.usuarioId = :usuarioId")
    long countByUsuario(Long empresaId, Long usuarioId);

    @Query("SELECT s FROM ChatSessao s WHERE s.empresaId = :empresaId AND s.ativo = true AND s.dataCriacao >= :dataInicial ORDER BY s.dataCriacao DESC")
    List<ChatSessao> findRecentByEmpresa(Long empresaId, LocalDateTime dataInicial);
}
