package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.financeiro.dto.CaixaDtos.CaixaRequest;
import br.com.brasil_saas.financeiro.dto.CaixaDtos.MovimentoRequest;
import br.com.brasil_saas.financeiro.model.Caixa;
import br.com.brasil_saas.financeiro.model.MovimentoCaixa;
import br.com.brasil_saas.financeiro.repository.CaixaRepository;
import br.com.brasil_saas.financeiro.repository.MovimentoCaixaRepository;
import br.com.brasil_saas.financeiro.service.CaixaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaixaServiceImpl implements CaixaService {

    private final CaixaRepository repository;
    private final MovimentoCaixaRepository movimentoRepository;

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
        Caixa c = new Caixa();
        c.setEmpresaId(empresaId);
        c.setNome(request.nome().trim());
        c.setSaldo(request.saldo() != null ? request.saldo() : BigDecimal.ZERO);
        c.setStatus(request.status() != null && !request.status().isBlank() ? request.status() : "ATIVO");
        return repository.save(c);
    }

    @Override
    @Transactional
    public Caixa atualizar(Long id, CaixaRequest request, Long empresaId) {
        Caixa c = buscarPorId(id, empresaId);
        c.setNome(request.nome().trim());
        if (request.status() != null && !request.status().isBlank()) {
            c.setStatus(request.status());
        }
        return repository.save(c);
    }

    @Override
    @Transactional
    public void desativar(Long id, Long empresaId) {
        Caixa c = buscarPorId(id, empresaId);
        c.setStatus("INATIVO");
        c.setDeletedAt(LocalDateTime.now());
        repository.save(c);
    }

    @Override
    @Transactional
    public MovimentoCaixa movimentar(Long empresaId, Long caixaId, MovimentoRequest request) {
        String tipo = request.tipo() == null ? "" : request.tipo().trim().toUpperCase();
        if (!"SANGRIA".equals(tipo) && !"SUPRIMENTO".equals(tipo)) {
            throw new BusinessException("Tipo deve ser SANGRIA ou SUPRIMENTO");
        }
        if (request.valor() == null || request.valor().signum() <= 0) {
            throw new BusinessException("Valor do movimento deve ser positivo");
        }

        Caixa c = buscarPorId(caixaId, empresaId);
        if (!c.isAtivo()) {
            throw new BusinessException("Caixa inativo nao aceita movimentos");
        }

        BigDecimal anterior = c.getSaldo() == null ? BigDecimal.ZERO : c.getSaldo();
        BigDecimal posterior;
        if ("SANGRIA".equals(tipo)) {
            if (anterior.compareTo(request.valor()) < 0) {
                throw new BusinessException("Saldo insuficiente para sangria");
            }
            posterior = anterior.subtract(request.valor());
        } else {
            posterior = anterior.add(request.valor());
        }

        c.setSaldo(posterior);
        repository.save(c);

        MovimentoCaixa m = new MovimentoCaixa();
        m.setEmpresaId(empresaId);
        m.setCaixaId(caixaId);
        m.setTipo(tipo);
        m.setValor(request.valor());
        m.setSaldoAnterior(anterior);
        m.setSaldoPosterior(posterior);
        m.setObservacao(request.observacao());
        return movimentoRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentoCaixa> listarMovimentos(Long empresaId, Long caixaId) {
        buscarPorId(caixaId, empresaId);
        return movimentoRepository.findByEmpresaIdAndCaixaIdOrderByCreatedAtDesc(empresaId, caixaId);
    }
}
