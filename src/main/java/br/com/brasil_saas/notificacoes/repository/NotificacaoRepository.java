package br.com.brasil_saas.notificacoes.repository;

import br.com.brasil_saas.notificacoes.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
    List<Notificacao> findByEmpresaIdAndUsuarioIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId, Long usuarioId);
    List<Notificacao> findByEmpresaIdAndUsuarioIdAndLidaFalseAndDeletedAtIsNullOrderByIdDesc(Long empresaId, Long usuarioId);
    Optional<Notificacao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    long countByEmpresaIdAndUsuarioIdAndLidaFalseAndDeletedAtIsNull(Long empresaId, Long usuarioId);
}
