package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.ChatSessaoRequest;
import br.com.brasil_saas.ia.model.ChatSessao;

import java.util.List;

public interface ChatSessaoService {

    ChatSessao criar(Long empresaId, ChatSessaoRequest request);

    ChatSessao atualizar(Long empresaId, Long id, ChatSessaoRequest request);

    ChatSessao buscarPorId(Long empresaId, Long id);

    List<ChatSessao> listarPorEmpresa(Long empresaId);

    List<ChatSessao> listarPorUsuario(Long empresaId, Long usuarioId);

    List<ChatSessao> listarFavoritos(Long empresaId, Long usuarioId);

    List<ChatSessao> listarRecentes(Long empresaId, Long usuarioId);

    void excluir(Long empresaId, Long id);

    void alternarFavorito(Long empresaId, Long id);

    void limparHistorico(Long empresaId, Long id);
}
