package br.com.brasil_saas.conhecimento.service;

import br.com.brasil_saas.conhecimento.model.ArtigoKb;
import br.com.brasil_saas.conhecimento.repository.ArtigoKbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConhecimentoService {
    private final ArtigoKbRepository repository;

    public List<ArtigoKb> listar(Long empresaId, String q) {
        if (q != null && !q.isBlank()) {
            return repository.buscar(empresaId, q.trim());
        }
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderByTituloAsc(empresaId);
    }

    public ArtigoKb buscar(Long empresaId, Long id) {
        return repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artigo não encontrado"));
    }

    @Transactional
    public ArtigoKb salvar(Long empresaId, ArtigoKb a) {
        if (a.getTitulo() == null || a.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título obrigatório");
        }
        if (a.getId() != null) {
            ArtigoKb x = buscar(empresaId, a.getId());
            x.setTitulo(a.getTitulo());
            x.setCategoria(a.getCategoria());
            x.setConteudo(a.getConteudo());
            x.setTags(a.getTags());
            x.setPublicado(a.getPublicado() == null || a.getPublicado());
            return repository.save(x);
        }
        a.setEmpresaId(empresaId);
        if (a.getUuid() == null) a.setUuid(UUID.randomUUID());
        if (a.getPublicado() == null) a.setPublicado(true);
        return repository.save(a);
    }

    @Transactional
    public void excluir(Long empresaId, Long id) {
        ArtigoKb a = buscar(empresaId, id);
        a.setDeletedAt(LocalDateTime.now());
        a.setPublicado(false);
        repository.save(a);
    }
}
