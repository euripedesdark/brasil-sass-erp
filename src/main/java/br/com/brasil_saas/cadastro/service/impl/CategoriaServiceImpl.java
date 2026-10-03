package br.com.brasil_saas.cadastro.service.impl;

import br.com.brasil_saas.cadastro.dto.CategoriaRequest;
import br.com.brasil_saas.cadastro.dto.CategoriaResponse;
import br.com.brasil_saas.cadastro.model.Categoria;
import br.com.brasil_saas.cadastro.repository.CategoriaRepository;
import br.com.brasil_saas.cadastro.service.CategoriaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaResponse criar(CategoriaRequest request, Long empresaId) {
        if (categoriaRepository.existsByNomeIgnoreCaseAndDeletedAtIsNull(request.nome())) {
            throw new BusinessException("CATEGORIA_DUPLICADA", "Categoria já cadastrada");
        }
        Categoria categoria = new Categoria();
        // Sem isso o INSERT fica com empresa_id nulo e o banco recusa com
        // violacao de FK, devolvida como 409 — que parece duplicidade mas e'
        // falta do tenant. Criar categoria nunca funcionou por causa deste
        // campo.
        categoria.setEmpresaId(empresaId);
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setTipo(request.tipo() != null ? request.tipo() : "PRODUTO");
        if (request.categoriaPaiId() != null) {
            categoriaRepository.findById(request.categoriaPaiId()).ifPresent(categoria::setCategoriaPai);
        }
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setTipo(request.tipo());
        if (request.categoriaPaiId() != null) {
            categoriaRepository.findById(request.categoriaPaiId()).ifPresent(categoria::setCategoriaPai);
        }
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return CategoriaResponse.from(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findByDeletedAtIsNullOrderByNome().stream()
            .map(CategoriaResponse::from).toList();
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        categoria.setDeletedAt(LocalDateTime.now());
        categoriaRepository.save(categoria);
    }
}
