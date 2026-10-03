package br.com.brasil_saas.cadastro.service.impl;

import br.com.brasil_saas.cadastro.dto.UnidadeMedidaRequest;
import br.com.brasil_saas.cadastro.dto.UnidadeMedidaResponse;
import br.com.brasil_saas.cadastro.model.UnidadeMedida;
import br.com.brasil_saas.cadastro.repository.UnidadeMedidaRepository;
import br.com.brasil_saas.cadastro.service.UnidadeMedidaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadeMedidaServiceImpl implements UnidadeMedidaService {

    private final UnidadeMedidaRepository unidadeMedidaRepository;

    @Override
    @Transactional
    public UnidadeMedidaResponse criar(UnidadeMedidaRequest request, Long empresaId) {
        if (unidadeMedidaRepository.existsBySiglaAndDeletedAtIsNull(request.sigla())) {
            throw new BusinessException("SIGLA_DUPLICADA", "Sigla já cadastrada");
        }
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setEmpresaId(empresaId);
        // Sem isso o INSERT fica com empresa_id nulo e o banco recusa com
        // violacao de FK, devolvida como 409 — que parece duplicidade mas e'
        // falta do tenant.
        unidade.setSigla(request.sigla());
        unidade.setNome(request.nome());
        unidade.setTipo(request.tipo() != null ? request.tipo() : "UNIDADE");
        return UnidadeMedidaResponse.from(unidadeMedidaRepository.save(unidade));
    }

    @Override
    @Transactional
    public UnidadeMedidaResponse atualizar(Long id, UnidadeMedidaRequest request) {
        UnidadeMedida unidade = unidadeMedidaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida não encontrada"));
        unidade.setSigla(request.sigla());
        unidade.setNome(request.nome());
        unidade.setTipo(request.tipo());
        return UnidadeMedidaResponse.from(unidadeMedidaRepository.save(unidade));
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadeMedidaResponse buscarPorId(Long id) {
        UnidadeMedida unidade = unidadeMedidaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida não encontrada"));
        return UnidadeMedidaResponse.from(unidade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadeMedidaResponse> listar() {
        return unidadeMedidaRepository.findByDeletedAtIsNullOrderByNome().stream()
            .map(UnidadeMedidaResponse::from).toList();
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        UnidadeMedida unidade = unidadeMedidaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Unidade de medida não encontrada"));
        unidade.setDeletedAt(LocalDateTime.now());
        unidadeMedidaRepository.save(unidade);
    }
}
