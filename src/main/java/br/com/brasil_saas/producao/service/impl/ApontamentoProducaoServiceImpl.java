package br.com.brasil_saas.producao.service.impl;

import br.com.brasil_saas.producao.model.ApontamentoProducao;
import br.com.brasil_saas.producao.model.ItemProducao;
import br.com.brasil_saas.producao.model.Producao;
import br.com.brasil_saas.producao.repository.ApontamentoProducaoRepository;
import br.com.brasil_saas.producao.repository.ItemProducaoRepository;
import br.com.brasil_saas.producao.repository.ProducaoRepository;
import br.com.brasil_saas.producao.service.ApontamentoProducaoRequest;
import br.com.brasil_saas.producao.service.ApontamentoProducaoService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApontamentoProducaoServiceImpl implements ApontamentoProducaoService {

    private final ApontamentoProducaoRepository apontamentoRepository;
    private final ProducaoRepository producaoRepository;
    private final ItemProducaoRepository itemRepository;

    @Override
    @Transactional
    public ApontamentoProducao criar(Long empresaId, ApontamentoProducaoRequest request) {
        Producao producao = producaoRepository.findById(request.producaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producao nao encontrada"));

        if (!producao.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("Producao nao pertence a esta empresa");
        }

        if (!"ABERTO".equals(producao.getStatus()) && !"EM_PROCESSO".equals(producao.getStatus())) {
            throw new BusinessException("Nao e possivel adicionar apontamentos a uma producao finalizada ou cancelada");
        }

        ItemProducao itemProducao = null;
        if (request.itemProducaoId() != null) {
            itemProducao = itemRepository.findById(request.itemProducaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item de producao nao encontrado"));
        }

        ApontamentoProducao apontamento = new ApontamentoProducao();
        apontamento.setEmpresaId(empresaId);
        apontamento.setProducao(producao);
        apontamento.setItemProducao(itemProducao);
        apontamento.setFuncionarioId(request.funcionarioId());
        apontamento.setHorasTrabalhadas(request.horasTrabalhadas());
        apontamento.setQuantidadeProduzida(request.quantidadeProduzida());
        apontamento.setQuantidadeRefugo(request.quantidadeRefugo());
        apontamento.setObservacoes(request.observacoes());
        apontamento.setMaquinaEquipamentoId(request.maquinaEquipamentoId());
        apontamento.setTurno(request.turno());
        apontamento.setStatus("INICIADO");

        ApontamentoProducao salvo = apontamentoRepository.save(apontamento);
        // a coluna espelho e read-only no banco: recem-salvo, ela ainda vem nula
        // e a resposta do POST saia com producaoId null, diferente do mesmo
        // registro lido depois. Preenchida aqui, os dois caminhos concordam.
        salvo.setProducaoIdRef(request.producaoId());
        salvo.setItemProducaoIdRef(request.itemProducaoId());
        return salvo;
    }

    @Override
    @Transactional
    public ApontamentoProducao atualizar(Long empresaId, Long id, ApontamentoProducaoRequest request) {
        ApontamentoProducao apontamento = apontamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apontamento nao encontrado"));

        if (!apontamento.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("Apontamento nao pertence a esta empresa");
        }

        Producao producao = producaoRepository.findById(request.producaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producao nao encontrada"));

        ItemProducao itemProducao = null;
        if (request.itemProducaoId() != null) {
            itemProducao = itemRepository.findById(request.itemProducaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item de producao nao encontrado"));
        }

        apontamento.setProducao(producao);
        apontamento.setItemProducao(itemProducao);
        apontamento.setFuncionarioId(request.funcionarioId());
        apontamento.setHorasTrabalhadas(request.horasTrabalhadas());
        apontamento.setQuantidadeProduzida(request.quantidadeProduzida());
        apontamento.setQuantidadeRefugo(request.quantidadeRefugo());
        apontamento.setObservacoes(request.observacoes());
        apontamento.setMaquinaEquipamentoId(request.maquinaEquipamentoId());
        apontamento.setTurno(request.turno());

        return apontamentoRepository.save(apontamento);
    }

    @Override
    @Transactional
    public ApontamentoProducao finalizar(Long empresaId, Long id) {
        ApontamentoProducao apontamento = apontamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apontamento nao encontrado"));

        if (!apontamento.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("Apontamento nao pertence a esta empresa");
        }

        if ("FINALIZADO".equals(apontamento.getStatus()) || "CANCELADO".equals(apontamento.getStatus())) {
            throw new BusinessException("Apontamento ja esta finalizado ou cancelado");
        }

        apontamento.setStatus("FINALIZADO");
        return apontamentoRepository.save(apontamento);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        ApontamentoProducao apontamento = apontamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apontamento nao encontrado"));

        if (!apontamento.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("Apontamento nao pertence a esta empresa");
        }

        if ("FINALIZADO".equals(apontamento.getStatus())) {
            throw new BusinessException("Nao e possivel excluir um apontamento finalizado");
        }

        apontamentoRepository.delete(apontamento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApontamentoProducao> listarPorProducao(Long empresaId, Long producaoId) {
        return apontamentoRepository.findByEmpresaIdAndProducaoId(empresaId, producaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApontamentoProducao> listarPorFuncionario(Long empresaId, Long funcionarioId) {
        return apontamentoRepository.findByEmpresaIdAndFuncionarioId(empresaId, funcionarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApontamentoProducao> listarPorPeriodo(Long empresaId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        return apontamentoRepository.findByEmpresaIdAndDataApontamentoBetween(empresaId, dataInicio, dataFim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApontamentoProducao> listarPorStatus(Long empresaId, String status) {
        return apontamentoRepository.findByEmpresaIdAndStatus(empresaId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticas(Long empresaId, Long producaoId) {
        Map<String, Object> stats = new HashMap<>();
        
        BigDecimal totalHoras = apontamentoRepository.sumHorasTrabalhadasByProducao(empresaId, producaoId);
        BigDecimal totalProduzido = apontamentoRepository.sumQuantidadeProduzidaByProducao(empresaId, producaoId);
        BigDecimal totalRefugo = apontamentoRepository.sumQuantidadeRefugoByProducao(empresaId, producaoId);
        
        List<ApontamentoProducao> apontamentos = listarPorProducao(empresaId, producaoId);
        
        stats.put("totalApontamentos", apontamentos.size());
        stats.put("totalHorasTrabalhadas", totalHoras);
        stats.put("totalQuantidadeProduzida", totalProduzido);
        stats.put("totalQuantidadeRefugo", totalRefugo);
        stats.put("percentualRefugo", 
            totalProduzido.compareTo(BigDecimal.ZERO) > 0 ? 
                totalRefugo.multiply(BigDecimal.valueOf(100)).divide(totalProduzido, 2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO);
        
        return stats;
    }
}
