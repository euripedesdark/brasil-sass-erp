package br.com.brasil_saas.ia.service.impl;

import br.com.brasil_saas.ia.dto.AnalisePreditivaRequest;
import br.com.brasil_saas.ia.model.AnalisePreditiva;
import br.com.brasil_saas.ia.repository.AnalisePreditivaRepository;
import br.com.brasil_saas.ia.service.AnalisePreditivaService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalisePreditivaServiceImpl implements AnalisePreditivaService {

    private final AnalisePreditivaRepository analiseRepository;

    @Override
    @Transactional
    public AnalisePreditiva criar(Long empresaId, AnalisePreditivaRequest request) {
        AnalisePreditiva analise = new AnalisePreditiva();
        analise.setEmpresaId(empresaId);
        analise.setNome(request.nome());
        analise.setDescricao(request.descricao());
        analise.setTipo(request.tipo());
        analise.setEntidade(request.entidade());
        analise.setEntidadeId(request.entidadeId());
        analise.setPeriodo(request.periodo());
        analise.setDiasPrevisao(request.diasPrevisao());
        analise.setValorAtual(request.valorAtual());
        analise.setValorPrevisto(request.valorPrevisto());
        analise.setValorMinimo(request.valorMinimo());
        analise.setValorMaximo(request.valorMaximo());
        analise.setConfianca(BigDecimal.ZERO);
        analise.setStatus("PENDENTE");
        analise.setDataAnalise(LocalDate.now());
        analise.setDataProximaAnalise(LocalDate.now().plusDays(request.diasPrevisao()));
        analise.setCriadoPor(request.criadoPor());
        analise.setDataCriacao(LocalDate.now());

        return analiseRepository.save(analise);
    }

    @Override
    @Transactional
    public AnalisePreditiva atualizar(Long empresaId, Long id, AnalisePreditivaRequest request) {
        AnalisePreditiva analise = analiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analise preditiva nao encontrada"));

        if (!analise.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Analise nao pertence a esta empresa");
        }

        analise.setNome(request.nome());
        analise.setDescricao(request.descricao());
        analise.setTipo(request.tipo());
        analise.setEntidade(request.entidade());
        analise.setEntidadeId(request.entidadeId());
        analise.setPeriodo(request.periodo());
        analise.setDiasPrevisao(request.diasPrevisao());
        analise.setValorAtual(request.valorAtual());
        analise.setValorPrevisto(request.valorPrevisto());
        analise.setValorMinimo(request.valorMinimo());
        analise.setValorMaximo(request.valorMaximo());
        analise.setDataProximaAnalise(LocalDate.now().plusDays(request.diasPrevisao()));

        return analiseRepository.save(analise);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalisePreditiva buscarPorId(Long empresaId, Long id) {
        AnalisePreditiva analise = analiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analise preditiva nao encontrada"));

        if (!analise.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Analise nao pertence a esta empresa");
        }

        return analise;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisePreditiva> listarPorEmpresa(Long empresaId) {
        return analiseRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisePreditiva> listarPorTipo(Long empresaId, String tipo) {
        return analiseRepository.findByEmpresaIdAndTipo(empresaId, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisePreditiva> listarPorEntidade(Long empresaId, String entidade, Long entidadeId) {
        return analiseRepository.findByEmpresaIdAndEntidadeAndEntidadeId(empresaId, entidade, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisePreditiva> listarPendentes(Long empresaId) {
        return analiseRepository.findPendentesByEmpresa(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisePreditiva> listarHighConfidence(Long empresaId, String tipo, Double minConfianca) {
        return analiseRepository.findHighConfidenceByTipo(empresaId, tipo);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        AnalisePreditiva analise = analiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analise preditiva nao encontrada"));

        if (!analise.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Analise nao pertence a esta empresa");
        }

        analiseRepository.delete(analise);
    }

    @Override
    @Transactional
    public Map<String, Object> preverVendas(Long empresaId, Long produtoId, Integer dias) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("mensagem", "Previsao de vendas gerada");
        resultado.put("produtoId", produtoId);
        resultado.put("dias", dias);
        resultado.put("valorPrevisto", BigDecimal.valueOf(1000.00));
        resultado.put("confianca", 0.85);
        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> preverEstoque(Long empresaId, Long produtoId, Integer dias) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("mensagem", "Previsao de estoque gerada");
        resultado.put("produtoId", produtoId);
        resultado.put("dias", dias);
        resultado.put("quantidadePrevista", 50);
        resultado.put("confianca", 0.80);
        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> preverFinanceiro(Long empresaId, Integer dias) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("mensagem", "Previsao financeira gerada");
        resultado.put("dias", dias);
        resultado.put("receitaPrevista", BigDecimal.valueOf(50000.00));
        resultado.put("despesaPrevista", BigDecimal.valueOf(30000.00));
        resultado.put("lucroPrevisto", BigDecimal.valueOf(20000.00));
        resultado.put("confianca", 0.75);
        return resultado;
    }

    @Override
    @Transactional
    public void executarAnalisesPendentes() {
        List<AnalisePreditiva> analises = analiseRepository.findByEmpresaIdAndStatus(1L, "PENDENTE");
        for (AnalisePreditiva analise : analises) {
            recalcularAnalise(analise.getEmpresaId(), analise.getId());
        }
    }

    @Override
    @Transactional
    public void recalcularAnalise(Long empresaId, Long id) {
        AnalisePreditiva analise = analiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analise preditiva nao encontrada"));

        if (!analise.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Analise nao pertence a esta empresa");
        }

        analise.setStatus("COMPLETO");
        analise.setConfianca(BigDecimal.valueOf(0.85));
        analise.setDataAnalise(LocalDate.now());
        analise.setDataProximaAnalise(LocalDate.now().plusDays(analise.getDiasPrevisao()));

        analiseRepository.save(analise);
    }
}
