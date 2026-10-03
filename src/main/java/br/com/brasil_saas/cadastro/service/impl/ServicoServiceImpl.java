package br.com.brasil_saas.cadastro.service.impl;

import br.com.brasil_saas.cadastro.dto.*;
import br.com.brasil_saas.cadastro.model.Servico;
import br.com.brasil_saas.cadastro.repository.ServicoRepository;
import br.com.brasil_saas.cadastro.repository.UnidadeMedidaRepository;
import br.com.brasil_saas.cadastro.service.ServicoService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicoServiceImpl implements ServicoService {

    private final ServicoRepository servicoRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;

    @Override
    @Transactional
    public ServicoResponse criar(ServicoRequest request, Long empresaId) {
        if (servicoRepository.existsByCodigoAndDeletedAtIsNull(request.codigo())) {
            throw new BusinessException("CODIGO_DUPLICADO", "Código de serviço já cadastrado");
        }
        Servico servico = new Servico();
        servico.setEmpresaId(empresaId);
        // Sem isso o INSERT fica com empresa_id nulo e o banco recusa com
        // violacao de FK, devolvida como 409 — que parece duplicidade mas e'
        // falta do tenant.
        mapearCampos(servico, request);
        Servico saved = servicoRepository.save(servico);
        return ServicoResponse.from(saved);
    }

    @Override
    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = servicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        if (servicoRepository.existsByCodigoAndIdNotAndDeletedAtIsNull(request.codigo(), id)) {
            throw new BusinessException("CODIGO_DUPLICADO", "Código de serviço já cadastrado");
        }
        mapearCampos(servico, request);
        Servico saved = servicoRepository.save(servico);
        return ServicoResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(Long id) {
        Servico servico = servicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        return ServicoResponse.from(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServicoResponse> listar(String nome, String codigo, Boolean ativo, Pageable pageable) {
        Page<Servico> page = servicoRepository.buscar(nome, codigo, ativo, pageable);
        return PageResponse.from(page, ServicoResponse::from);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Servico servico = servicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        servico.setDeletedAt(LocalDateTime.now());
        servicoRepository.save(servico);
    }

    private void mapearCampos(Servico servico, ServicoRequest request) {
        servico.setCodigo(request.codigo());
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setLc116Codigo(request.lc116Codigo());
        // Codigo municipal de São Paulo (4 digitos). Vazio vira null para nao
        // bater no indice unico com varias linhas em branco.
        String municipal = request.codigoTributacaoMunicipal();
        servico.setCodigoTributacaoMunicipal(
                municipal == null || municipal.isBlank() ? null : municipal.trim());
        servico.setNbs(request.nbs());
        // aliquota_iss e' NOT NULL e o default do campo (ZERO) nao sobrevive a
        // um set explicito com null — e o mapper sobrescreve sempre. Sem isto
        // o INSERT vai com a coluna nula e o banco recusa com violacao de
        // not-null, devolvida como 409, que parece duplicidade mas e' campo
        // obrigatorio faltando. Mesma defesa do 'ativo' logo abaixo.
        servico.setAliquotaIss(request.aliquotaIss() != null ? request.aliquotaIss() : BigDecimal.ZERO);
        servico.setPreco(request.preco());
        servico.setAtivo(request.ativo() != null ? request.ativo() : true);
        if (request.unidadeMedidaId() != null) {
            unidadeMedidaRepository.findById(request.unidadeMedidaId()).ifPresent(servico::setUnidadeMedida);
        }
    }
}
