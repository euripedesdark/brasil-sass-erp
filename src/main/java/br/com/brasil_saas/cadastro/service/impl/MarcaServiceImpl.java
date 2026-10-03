package br.com.brasil_saas.cadastro.service.impl;

import br.com.brasil_saas.cadastro.dto.MarcaRequest;
import br.com.brasil_saas.cadastro.dto.MarcaResponse;
import br.com.brasil_saas.cadastro.model.Marca;
import br.com.brasil_saas.cadastro.repository.MarcaRepository;
import br.com.brasil_saas.cadastro.service.MarcaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;

    @Override
    @Transactional
    public MarcaResponse criar(MarcaRequest request, Long empresaId) {
        if (marcaRepository.existsByNomeIgnoreCaseAndDeletedAtIsNull(request.nome())) {
            throw new BusinessException("MARCA_DUPLICADA", "Marca já cadastrada");
        }
        Marca marca = new Marca();
        marca.setEmpresaId(empresaId);
        // Sem isso o INSERT fica com empresa_id nulo e o banco recusa com
        // violacao de FK, devolvida como 409 — que parece duplicidade mas e'
        // falta do tenant.
        marca.setNome(request.nome());
        marca.setDescricao(request.descricao());
        return MarcaResponse.from(marcaRepository.save(marca));
    }

    @Override
    @Transactional
    public MarcaResponse atualizar(Long id, MarcaRequest request) {
        Marca marca = marcaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada"));
        marca.setNome(request.nome());
        marca.setDescricao(request.descricao());
        return MarcaResponse.from(marcaRepository.save(marca));
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaResponse buscarPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada"));
        return MarcaResponse.from(marca);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaResponse> listar() {
        return marcaRepository.findByDeletedAtIsNullOrderByNome().stream()
            .map(MarcaResponse::from).toList();
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Marca marca = marcaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada"));
        marca.setDeletedAt(LocalDateTime.now());
        marcaRepository.save(marca);
    }
}
