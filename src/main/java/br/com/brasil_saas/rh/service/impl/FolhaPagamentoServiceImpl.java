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
