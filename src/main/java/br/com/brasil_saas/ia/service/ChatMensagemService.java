package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.ChatMensagemRequest;
import br.com.brasil_saas.ia.model.ChatMensagem;

import java.util.List;

public interface ChatMensagemService {

    ChatMensagem criar(Long empresaId, ChatMensagemRequest request);

    ChatMensagem buscarPorId(Long empresaId, Long id);

    List<ChatMensagem> listarPorSessao(Long empresaId, Long sessaoId);

    List<ChatMensagem> listarPorSessaoOrdered(Long empresaId, Long sessaoId);

    void excluir(Long empresaId, Long id);

    void excluirPorSessao(Long empresaId, Long sessaoId);

    void classificarMensagem(Long empresaId, Long id, Integer classificacao, String feedback);
}
