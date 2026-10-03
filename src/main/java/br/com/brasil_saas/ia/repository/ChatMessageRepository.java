package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    
    Long countBySessionId(Long sessionId);
    
    List<ChatMessage> findBySession_SessionType(String sessionType);
}
