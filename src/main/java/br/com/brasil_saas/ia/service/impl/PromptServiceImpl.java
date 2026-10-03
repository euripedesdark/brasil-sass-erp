package br.com.brasil_saas.ia.service.impl;

import br.com.brasil_saas.ia.dto.PromptRequest;
import br.com.brasil_saas.ia.model.Prompt;
import br.com.brasil_saas.ia.repository.PromptRepository;
import br.com.brasil_saas.ia.service.PromptService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;

    @Override
    @Transactional
    public Prompt criar(Long empresaId, PromptRequest request) {
        Prompt prompt = new Prompt();
        prompt.setEmpresaId(empresaId);
        prompt.setNome(request.nome());
        prompt.setDescricao(request.descricao());
        prompt.setConteudo(request.conteudo());
        prompt.setCategoria(request.categoria());
        prompt.setTipo(request.tipo());
        prompt.setTemperatura(request.temperatura());
        prompt.setMaxTokens(request.maxTokens());
        prompt.setAtivo(request.ativo());
        prompt.setPublico(request.publico());
        prompt.setFavorito(request.favorito());
        prompt.setContadorUso(0);
        prompt.setCriadoPor(request.criadoPor());
        prompt.setDataCriacao(LocalDateTime.now());
        prompt.setDataAtualizacao(LocalDateTime.now());

        return promptRepository.save(prompt);
    }

    @Override
    @Transactional
    public Prompt atualizar(Long empresaId, Long id, PromptRequest request) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt nao encontrado"));

        if (!prompt.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Prompt nao pertence a esta empresa");
        }

        prompt.setNome(request.nome());
        prompt.setDescricao(request.descricao());
        prompt.setConteudo(request.conteudo());
        prompt.setCategoria(request.categoria());
        prompt.setTipo(request.tipo());
        prompt.setTemperatura(request.temperatura());
        prompt.setMaxTokens(request.maxTokens());
        prompt.setAtivo(request.ativo());
        prompt.setPublico(request.publico());
        prompt.setFavorito(request.favorito());
        prompt.setDataAtualizacao(LocalDateTime.now());

        return promptRepository.save(prompt);
    }

    @Override
    @Transactional(readOnly = true)
    public Prompt buscarPorId(Long empresaId, Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt nao encontrado"));

        if (!prompt.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Prompt nao pertence a esta empresa");
        }

        return prompt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prompt> listarPorEmpresa(Long empresaId) {
        return promptRepository.findByEmpresaIdAndAtivoTrue(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prompt> listarPublicos(Long empresaId) {
        return promptRepository.findByEmpresaIdAndPublicoTrue(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prompt> listarPorCategoria(Long empresaId, String categoria) {
        return promptRepository.findByEmpresaIdAndCategoria(empresaId, categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prompt> listarFavoritos(Long empresaId) {
        return promptRepository.findByEmpresaIdAndFavoritoTrue(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prompt> listarMaisUsados(Long empresaId) {
        return promptRepository.findMaisUsados(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prompt> buscarPorTermo(Long empresaId, String termo) {
        return promptRepository.searchByTermo(empresaId, "%" + termo + "%");
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt nao encontrado"));

        if (!prompt.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Prompt nao pertence a esta empresa");
        }

        promptRepository.delete(prompt);
    }

    @Override
    @Transactional
    public void alternarFavorito(Long empresaId, Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt nao encontrado"));

        if (!prompt.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Prompt nao pertence a esta empresa");
        }

        prompt.setFavorito(!prompt.getFavorito());
        promptRepository.save(prompt);
    }

    @Override
    @Transactional
    public void incrementarContadorUso(Long empresaId, Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt nao encontrado"));

        if (!prompt.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Prompt nao pertence a esta empresa");
        }

        prompt.setContadorUso(prompt.getContadorUso() + 1);
        promptRepository.save(prompt);
    }
}
