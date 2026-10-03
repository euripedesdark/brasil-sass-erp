package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.ChatRequest;
import br.com.brasil_saas.ia.dto.ChatResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ChatService {
    
    ChatResponse sendMessage(ChatRequest request);
    
    ChatResponse startNewSession(String firstMessage, Long userId, Long empresaId);
    
    PageResponse<ChatResponse> listSessions(Long empresaId, Pageable pageable);
    
    void deleteSession(Long sessionId, Long empresaId);
    
    ChatResponse getSessionHistory(Long sessionId, Long empresaId);
    
    String generateText(String prompt, String model, Long empresaId);
    
    String analyzeData(String data, String analysisType, Long empresaId);
    
    ChatResponse chat(Long empresaId, ChatRequest request);
    
    ChatResponse chatSimples(Long empresaId, Long usuarioId, String mensagem);
    
    ChatResponse chatComContexto(Long empresaId, Long usuarioId, String mensagem, java.util.List<String> contexto);
    
    String gerarResposta(Long empresaId, String prompt, String modelo, Double temperatura, Integer maxTokens);
    
    String gerarRespostaComHistorico(Long empresaId, java.util.List<String> historico, String mensagem, String modelo, Double temperatura, Integer maxTokens);
    
    String resumirTexto(Long empresaId, String texto, Integer maxTokens);
    
    String traduzirTexto(Long empresaId, String texto, String idiomaDestino);
    
    String corrigirTexto(Long empresaId, String texto);
    
    String extrairInformacoes(Long empresaId, String texto, String template);
}
