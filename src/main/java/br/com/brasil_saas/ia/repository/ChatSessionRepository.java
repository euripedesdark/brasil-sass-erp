package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    List<ChatSession> findByEmpresaIdAndIsActiveTrueOrderByCreatedAtDesc(Long empresaId);
    
    Page<ChatSession> findByEmpresaId(Long empresaId, Pageable pageable);
    
    Optional<ChatSession> findByIdAndEmpresaId(Long id, Long empresaId);
    
    Long countByEmpresaIdAndCreatedAtBetween(Long empresaId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(c.tokenCount), 0) FROM ChatSession c WHERE c.empresaId = :empresaId AND c.createdAt >= :startDate")
    Long sumTokenCountByEmpresaIdAndDate(Long empresaId, java.time.LocalDateTime startDate);
}
