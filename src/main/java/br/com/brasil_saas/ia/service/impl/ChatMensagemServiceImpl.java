package br.com.brasil_saas.ia.service.impl;

import br.com.brasil_saas.ia.dto.ChatMensagemRequest;
import br.com.brasil_saas.ia.model.ChatMensagem;
import br.com.brasil_saas.ia.model.ChatSessao;
import br.com.brasil_saas.ia.repository.ChatMensagemRepository;
import br.com.brasil_saas.ia.repository.ChatSessaoRepository;
import br.com.brasil_saas.ia.service.ChatMensagemService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMensagemServiceImpl implements ChatMensagemService {

    private final ChatMensagemRepository mensagemRepository;
    private final ChatSessaoRepository sessaoRepository;

    @Override
    @Transactional
    public ChatMensagem criar(Long empresaId, ChatMensagemRequest request) {
        ChatSessao sessao = sessaoRepository.findById(request.sessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessao de chat nao encontrada"));

        if (!sessao.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Sessao nao pertence a esta empresa");
        }

        ChatMensagem mensagem = new ChatMensagem();
        mensagem.setEmpresaId(empresaId);
        mensagem.setSessao(sessao);
        mensagem.setConteudo(request.conteudo());
        mensagem.setTipo(request.tipo());
        mensagem.setModeloIa(request.modeloIa());
        mensagem.setDataEnvio(LocalDateTime.now());

        return mensagemRepository.save(mensagem);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMensagem buscarPorId(Long empresaId, Long id) {
        ChatMensagem mensagem = mensagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem de chat nao encontrada"));

        if (!mensagem.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Mensagem nao pertence a esta empresa");
        }

        return mensagem;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMensagem> listarPorSessao(Long empresaId, Long sessaoId) {
        return mensagemRepository.findByEmpresaIdAndSessaoId(empresaId, sessaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMensagem> listarPorSessaoOrdered(Long empresaId, Long sessaoId) {
        return mensagemRepository.findBySessaoOrdered(empresaId, sessaoId);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        ChatMensagem mensagem = mensagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem de chat nao encontrada"));

        if (!mensagem.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Mensagem nao pertence a esta empresa");
        }

        mensagemRepository.delete(mensagem);
    }

    @Override
    @Transactional
    public void excluirPorSessao(Long empresaId, Long sessaoId) {
        List<ChatMensagem> mensagens = mensagemRepository.findByEmpresaIdAndSessaoId(empresaId, sessaoId);
        mensagemRepository.deleteAll(mensagens);
    }

    @Override
    @Transactional
    public void classificarMensagem(Long empresaId, Long id, Integer classificacao, String feedback) {
        ChatMensagem mensagem = mensagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem de chat nao encontrada"));

        if (!mensagem.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Mensagem nao pertence a esta empresa");
        }

        mensagem.setClassificacao(classificacao);
        mensagem.setFeedback(feedback);
        mensagemRepository.save(mensagem);
    }
}
