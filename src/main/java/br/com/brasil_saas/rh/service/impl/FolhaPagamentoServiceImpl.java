package br.com.brasil_saas.rh.service.impl;

import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.rh.dto.FolhaPagamentoRequest;
import br.com.brasil_saas.rh.dto.FolhaPagamentoResponse;
import br.com.brasil_saas.rh.model.FolhaPagamento;
import br.com.brasil_saas.rh.model.ItemFolhaPagamento;
import br.com.brasil_saas.rh.repository.FolhaPagamentoRepository;
import br.com.brasil_saas.rh.service.FolhaPagamentoService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolhaPagamentoServiceImpl implements FolhaPagamentoService {

    private final FolhaPagamentoRepository folhaRepository;
    private final TituloRepository tituloRepository;
    private final br.com.brasil_saas.rh.repository.PontoRepository pontoRepository;
    private final br.com.brasil_saas.rh.repository.FuncionarioRepository funcionarioRepository;

    @Override
    @Transactional
    public FolhaPagamentoResponse criar(Long empresaId, FolhaPagamentoRequest request) {
        FolhaPagamento folha = new FolhaPagamento();
        // empresa do token, nunca do corpo
        folha.setEmpresaId(empresaId);
        folha.setCompetencia(request.competencia());
        folha.setStatus(request.status() != null ? request.status() : "ABERTA");

        List<ItemFolhaPagamento> itens = new ArrayList<>();
        BigDecimal totalProventos = BigDecimal.ZERO;
        BigDecimal totalDescontos = BigDecimal.ZERO;

        if (request.itens() != null) {
            for (var itemReq : request.itens()) {
                ItemFolhaPagamento item = new ItemFolhaPagamento();
                item.setFolha(folha);
                item.setEmpresaId(folha.getEmpresaId());
                item.setFuncionarioId(itemReq.funcionarioId());
                item.setTipo(itemReq.tipo());
                item.setDescricao(itemReq.descricao());
                item.setValor(itemReq.valor());
                itens.add(item);

                if ("PROVENTO".equals(itemReq.tipo())) {
                    totalProventos = totalProventos.add(itemReq.valor());
                } else if ("DESCONTO".equals(itemReq.tipo())) {
                    totalDescontos = totalDescontos.add(itemReq.valor());
                }
            }
        }

        folha.setItens(itens);
        folha.setValorTotal(totalProventos.subtract(totalDescontos));

        FolhaPagamento salva = folhaRepository.save(folha);
        return FolhaPagamentoResponse.from(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public FolhaPagamentoResponse buscarPorId(Long empresaId, Long id) {
        FolhaPagamento folha = folhaRepository.findById(id)
            .filter(f -> f.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new ResourceNotFoundException("Folha de pagamento não encontrada"));
        return FolhaPagamentoResponse.from(folha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolhaPagamentoResponse> listarPorEmpresa(Long empresaId) {
        return folhaRepository.findByEmpresaIdOrderByCompetenciaDesc(empresaId).stream()
            .map(FolhaPagamentoResponse::from).toList();
    }

    @Override
    @Transactional
    public FolhaPagamentoResponse atualizar(Long empresaId, Long id, FolhaPagamentoRequest request) {
        FolhaPagamento folha = folhaRepository.findById(id)
            .filter(f -> f.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new ResourceNotFoundException("Folha de pagamento não encontrada"));

        if ("PAGA".equals(folha.getStatus()) || "CANCELADA".equals(folha.getStatus())) {
            throw new BusinessException("Folhas PAGA ou CANCELADA não podem ser editadas");
        }
        if (request.competencia() != null && !request.competencia().isBlank()) {
            folha.setCompetencia(request.competencia());
        }
        if (request.status() != null && !request.status().isBlank()) {
            folha.setStatus(request.status());
        }

        if (request.itens() != null) {
            List<ItemFolhaPagamento> itens = new ArrayList<>();
            BigDecimal totalProventos = BigDecimal.ZERO;
            BigDecimal totalDescontos = BigDecimal.ZERO;

            for (var itemReq : request.itens()) {
                ItemFolhaPagamento item = new ItemFolhaPagamento();
                item.setFolha(folha);
                item.setEmpresaId(folha.getEmpresaId());
                item.setFuncionarioId(itemReq.funcionarioId());
                item.setTipo(itemReq.tipo());
                item.setDescricao(itemReq.descricao());
                item.setValor(itemReq.valor());
                itens.add(item);

                if ("PROVENTO".equals(itemReq.tipo())) {
                    totalProventos = totalProventos.add(itemReq.valor());
                } else if ("DESCONTO".equals(itemReq.tipo())) {
                    totalDescontos = totalDescontos.add(itemReq.valor());
                }
            }

            folha.setItens(itens);
            folha.setValorTotal(totalProventos.subtract(totalDescontos));
        }

        return FolhaPagamentoResponse.from(folhaRepository.save(folha));
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        FolhaPagamento folha = folhaRepository.findById(id)
            .filter(f -> f.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new ResourceNotFoundException("Folha de pagamento não encontrada"));

        if ("PAGA".equals(folha.getStatus())) {
            throw new BusinessException("Folhas PAGAS não podem ser excluídas");
        }

        folhaRepository.delete(folha);
    }

    @Override
    @Transactional
    public void processar(Long empresaId, Long id) {
        FolhaPagamento folha = folhaRepository.findById(id)
            .filter(f -> f.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new ResourceNotFoundException("Folha de pagamento não encontrada"));

        if (!"ABERTA".equals(folha.getStatus())) {
            throw new BusinessException("Apenas folhas ABERTAS podem ser processadas");
        }

        // Gerar título a pagar no financeiro
        Titulo titulo = new Titulo();
        titulo.setEmpresaId(folha.getEmpresaId());
        titulo.setTipo("P"); // Pagar
        titulo.setNumeroDocumento("FOLHA-" + folha.getCompetencia());
        titulo.setDescricao("Folha de Pagamento - " + folha.getCompetencia());
        titulo.setValorOriginal(folha.getValorTotal());
        titulo.setValorSaldo(folha.getValorTotal());
        titulo.setDataEmissao(LocalDate.now());
        titulo.setDataVencimento(LocalDate.now().plusDays(5)); // Pagamento até dia 5
        titulo.setStatus("ABERTO");
        
        Titulo tituloSalvo = tituloRepository.save(titulo);
        folha.setTituloId(tituloSalvo.getId());
        folha.setStatus("PROCESSADA");
        folhaRepository.save(folha);
    }

    @Override
    @Transactional
    public br.com.brasil_saas.rh.dto.FolhaPagamentoResponse importarPonto(Long empresaId, Long id, Long funcionarioId, Integer ano, Integer mes) {
        FolhaPagamento folha = folhaRepository.findById(id).filter(f -> empresaId.equals(f.getEmpresaId())).orElseThrow(() -> new br.com.brasil_saas.shared.exception.ResourceNotFoundException("Folha inexistente"));
        if ("PAGA".equals(folha.getStatus()) || "CANCELADA".equals(folha.getStatus())) throw new br.com.brasil_saas.shared.exception.BusinessException("Folha fechada");
        var func = funcionarioRepository.findById(funcionarioId).orElseThrow(() -> new br.com.brasil_saas.shared.exception.ResourceNotFoundException("Funcionario inexistente"));
        java.math.BigDecimal horas = java.math.BigDecimal.ZERO;
        for (var pt : pontoRepository.findByEmpresaIdAndFuncionarioIdAndDeletedAtIsNullOrderByDataDesc(empresaId, funcionarioId)) {
            if (Boolean.TRUE.equals(pt.getFalta())) continue;
            if (pt.getData() == null) continue;
            if (ano != null && (pt.getData().getYear() != ano || pt.getData().getMonthValue() != mes)) continue;
            horas = horas.add(pt.getHorasTrabalhadas() == null ? java.math.BigDecimal.ZERO : pt.getHorasTrabalhadas());
        }
        java.math.BigDecimal sal = func.getSalario() == null ? java.math.BigDecimal.ZERO : func.getSalario();
        java.math.BigDecimal valorHora = sal.divide(new java.math.BigDecimal(220), 4, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal valor = horas.multiply(valorHora).setScale(2, java.math.RoundingMode.HALF_UP);
        ItemFolhaPagamento item = new ItemFolhaPagamento();
        item.setFolha(folha);
        item.setEmpresaId(empresaId);
        item.setFuncionarioId(funcionarioId);
        item.setTipo("PROVENTO");
        item.setDescricao("Horas ponto");
        item.setValor(valor);
        folha.getItens().add(item);
        java.math.BigDecimal tot = folha.getValorTotal() == null ? java.math.BigDecimal.ZERO : folha.getValorTotal();
        folha.setValorTotal(tot.add(valor));
        return br.com.brasil_saas.rh.dto.FolhaPagamentoResponse.from(folhaRepository.save(folha));
    }
    public void cancelar(Long empresaId, Long id) {
        FolhaPagamento folha = folhaRepository.findById(id)
            .filter(f -> f.getEmpresaId().equals(empresaId))
            .orElseThrow(() -> new ResourceNotFoundException("Folha de pagamento não encontrada"));

        if ("PAGA".equals(folha.getStatus())) {
            throw new BusinessException("Folhas PAGAS não podem ser canceladas");
        }

        folha.setStatus("CANCELADA");
        folhaRepository.save(folha);
    }
}
