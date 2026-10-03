package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.financeiro.dto.CaixaDtos.CaixaRequest;
import br.com.brasil_saas.financeiro.model.Caixa;
import br.com.brasil_saas.financeiro.repository.CaixaRepository;
import br.com.brasil_saas.financeiro.service.CaixaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CaixaServiceImpl implements CaixaService {

    private final CaixaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<Caixa> listar(Long empresaId, Pageable pageable, String termo) {
        if (termo == null || termo.isBlank()) {
            return repository.findByEmpresaIdAndDeletedAtIsNullOrderByNome(empresaId, pageable);
        }
        return repository.buscar(empresaId, termo.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Caixa buscarPorId(Long id, Long empresaId) {
        return repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Caixa nao encontrado"));
    }

    @Override
    @Transactional
    public Caixa criar(CaixaRequest request, Long empresaId) {
        String nome = request.nome().trim();
        if (repository.findByEmpresaIdAndNomeIgnoreCaseAndDeletedAtIsNull(empresaId, nome).isPresent()) {
            throw new BusinessException("Ja existe um caixa com esse nome");
        }

        Caixa caixa = new Caixa();
        caixa.setEmpresaId(empresaId);
        caixa.setNome(nome);
        caixa.setSaldo(request.saldo());
        caixa.setStatus(normaliza(request.status(), "ATIVO"));
        return repository.save(caixa);
    }

    @Override
    @Transactional
    public Caixa atualizar(Long id, CaixaRequest request, Long empresaId) {
        Caixa caixa = buscarPorId(id, empresaId);
        String nome = request.nome().trim();

        repository.findByEmpresaIdAndNomeIgnoreCaseAndDeletedAtIsNull(empresaId, nome)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> { throw new BusinessException("Ja existe um caixa com esse nome"); });

        caixa.setNome(nome);
        caixa.setSaldo(request.saldo());
        caixa.setStatus(normaliza(request.status(), caixa.getStatus()));
        return repository.save(caixa);
    }

    @Override
    @Transactional
    public void desativar(Long id, Long empresaId) {
        Caixa caixa = buscarPorId(id, empresaId);
        caixa.setStatus("INATIVO");
        caixa.setDeletedAt(LocalDateTime.now());
        repository.save(caixa);
    }

    private static String normaliza(String status, String padrao) {
        if (status == null || status.isBlank()) {
            return padrao == null || padrao.isBlank() ? "ATIVO" : padrao;
        }
        String s = status.trim().toUpperCase();
        if (!"ATIVO".equals(s) && !"INATIVO".equals(s)) {
            throw new BusinessException("Status invalido: use ATIVO ou INATIVO");
        }
        return s;
    }
}
