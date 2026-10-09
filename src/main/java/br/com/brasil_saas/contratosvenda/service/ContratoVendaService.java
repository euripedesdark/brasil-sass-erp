package br.com.brasil_saas.contratosvenda.service;

import br.com.brasil_saas.contratosvenda.model.ContratoVenda;
import br.com.brasil_saas.contratosvenda.repository.ContratoVendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoVendaService {
    private final ContratoVendaRepository repository;

    public List<ContratoVenda> listar(Long empresaId, String status) {
        if (status != null && !status.isBlank()) {
            return repository.findByEmpresaIdAndStatusAndDeletedAtIsNullOrderByIdDesc(empresaId, status.trim().toUpperCase());
        }
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId);
    }

    public ContratoVenda buscar(Long empresaId, Long id) {
        return repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado"));
    }

    @Transactional
    public ContratoVenda salvar(Long empresaId, ContratoVenda c) {
        if (c.getTitulo() == null || c.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título obrigatório");
        }
        if (c.getClienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente obrigatório");
        }
        if (c.getValor() == null) c.setValor(BigDecimal.ZERO);
        if (c.getId() != null) {
            ContratoVenda a = buscar(empresaId, c.getId());
            if (!"RASCUNHO".equals(a.getStatus()) && !"ATIVO".equals(a.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Só edita RASCUNHO ou ATIVO");
            }
            a.setTitulo(c.getTitulo());
            a.setClienteId(c.getClienteId());
            a.setValor(c.getValor());
            a.setInicio(c.getInicio());
            a.setFim(c.getFim());
            a.setRenovacaoAuto(Boolean.TRUE.equals(c.getRenovacaoAuto()));
            a.setObservacao(c.getObservacao());
            return repository.save(a);
        }
        c.setEmpresaId(empresaId);
        if (c.getUuid() == null) c.setUuid(UUID.randomUUID());
        c.setNumero("CV-" + (System.currentTimeMillis() % 10000000));
        c.setStatus("RASCUNHO");
        if (c.getRenovacaoAuto() == null) c.setRenovacaoAuto(false);
        return repository.save(c);
    }

    @Transactional
    public ContratoVenda ativar(Long empresaId, Long id) {
        ContratoVenda c = buscar(empresaId, id);
        if (!"RASCUNHO".equals(c.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Só ativa a partir de RASCUNHO");
        }
        c.setStatus("ATIVO");
        return repository.save(c);
    }

    @Transactional
    public ContratoVenda encerrar(Long empresaId, Long id) {
        ContratoVenda c = buscar(empresaId, id);
        c.setStatus("ENCERRADO");
        return repository.save(c);
    }

    @Transactional
    public ContratoVenda cancelar(Long empresaId, Long id) {
        ContratoVenda c = buscar(empresaId, id);
        if ("ENCERRADO".equals(c.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já encerrado");
        }
        c.setStatus("CANCELADO");
        c.setDeletedAt(LocalDateTime.now());
        return repository.save(c);
    }

    public Map<String, Object> resumo(Long empresaId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rascunho", repository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "RASCUNHO"));
        m.put("ativos", repository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "ATIVO"));
        m.put("encerrados", repository.countByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, "ENCERRADO"));
        return m;
    }
}
