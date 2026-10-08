package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.*;
import br.com.brasil_saas.financeiro.model.*;
import br.com.brasil_saas.financeiro.repository.*;
import br.com.brasil_saas.financeiro.service.TituloService;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
public class TituloServiceImpl implements TituloService {
    private final TituloRepository tituloRepository;
    private final TituloParcelaRepository parcelaRepository;
    private final BaixaRepository baixaRepository;
    private final CondicaoPagamentoRepository condicaoRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final ExtratoRepository extratoRepository;
    private final ConferenciaFaturaCompraRepository conferenciaCompraRepository;

    @Override @Transactional(readOnly = true)
    public List<TituloResponse> listar(Long empresaId, String status) {
        List<Titulo> titulos = (status == null || status.isBlank())
            ? tituloRepository.findByEmpresaIdAndDeletedAtIsNullOrderByDataVencimento(empresaId)
            : tituloRepository.findByEmpresaIdAndStatusAndDeletedAtIsNullOrderByDataVencimento(empresaId, status);
        return titulos.stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public TituloResponse buscar(Long empresaId, Long id) {
        return toResponse(obterTitulo(empresaId, id));
    }

    @Override @Transactional(readOnly = true)
    public List<ParcelaResponse> parcelas(Long empresaId, Long tituloId) {
        obterTitulo(empresaId, tituloId);
        return parcelaRepository.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(tituloId)
            .stream().map(this::toParcela).toList();
    }

    @Override @Transactional
    public List<ParcelaResponse> gerarParcelas(Long empresaId, Long tituloId, Long condicaoPagamentoId) {
        Titulo titulo = obterTitulo(empresaId, tituloId);
        if (!parcelaRepository.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(tituloId).isEmpty()) {
            throw new BusinessException("Título já possui parcelas geradas");
        }
        List<Integer> dias = new ArrayList<>();
        if (condicaoPagamentoId != null) {
            CondicaoPagamento cond = condicaoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(condicaoPagamentoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Condição de pagamento não encontrada"));
            if (cond.getDias() != null && !cond.getDias().isBlank()) {
                for (String d : cond.getDias().split("[,;/\\s]+")) {
                    if (!d.isBlank()) dias.add(Integer.parseInt(d.trim()));
                }
            }
        }
        if (dias.isEmpty()) dias.add(0);
        int n = dias.size();
        BigDecimal valorBase = titulo.getValorSaldo();
        BigDecimal porParcela = valorBase.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal acumulado = BigDecimal.ZERO;
        List<TituloParcela> criadas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            BigDecimal valor = (i == n - 1) ? valorBase.subtract(acumulado) : porParcela;
            acumulado = acumulado.add(valor);
            TituloParcela p = new TituloParcela();
            p.setEmpresaId(empresaId);
            p.setTituloId(tituloId);
            p.setNumeroParcela(i + 1);
            p.setValorParcela(valor);
            p.setValorSaldo(valor);
            p.setDataVencimento(titulo.getDataEmissao().plusDays(dias.get(i)));
            p.setStatus("ABERTO");
            criadas.add(parcelaRepository.save(p));
        }
        return criadas.stream().map(this::toParcela).toList();
    }

    @Override @Transactional
    public BaixaResponse baixar(Long empresaId, Long tituloId, BaixaRequest r) {
        Titulo titulo = tituloRepository.findForUpdate(tituloId, empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Título não encontrado"));

        if (conferenciaCompraRepository.findVigentesParaTitulo(empresaId, tituloId).stream()
                .anyMatch(c -> !"APROVADA".equals(c.getStatus()))) {
            throw new BusinessException("Título bloqueado por conferência de compra pendente ou divergente");
        }

        if (r.valorBaixa() == null || r.valorBaixa().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor da baixa deve ser maior que zero");
        }

        BigDecimal desconto = r.valorDesconto() == null ? BigDecimal.ZERO : r.valorDesconto();
        BigDecimal juros = r.valorJuro() == null ? BigDecimal.ZERO : r.valorJuro();
        BigDecimal multa = r.valorMulta() == null ? BigDecimal.ZERO : r.valorMulta();
        if (desconto.compareTo(BigDecimal.ZERO) < 0 || juros.compareTo(BigDecimal.ZERO) < 0 || multa.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Desconto, juros e multa não podem ser negativos");
        }

        TituloParcela parcela = null;
        if (r.parcelaId() != null) {
            parcela = parcelaRepository.findForUpdate(r.parcelaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela não encontrada"));
            if (!tituloId.equals(parcela.getTituloId())) {
                throw new BusinessException("Parcela não pertence ao título");
            }
            if (parcela.getValorSaldo().compareTo(r.valorBaixa().add(desconto)) < 0) {
                throw new BusinessException("Valor da baixa excede o saldo da parcela");
            }
        }

        if ("CANCELADO".equalsIgnoreCase(titulo.getStatus())) throw new BusinessException("Título cancelado não pode receber baixa");
        if ("PENDENTE_APROVACAO".equalsIgnoreCase(titulo.getStatus())) throw new BusinessException("Título pendente de aprovação não pode receber baixa");
        if (titulo.getValorSaldo() == null || titulo.getValorSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Título já está totalmente baixado");
        }

        BigDecimal reducao = r.valorBaixa().add(desconto);
        if (reducao.compareTo(titulo.getValorSaldo()) > 0) {
            throw new BusinessException("Valor da baixa + desconto excede o saldo do título");
        }

        if (r.dataBaixa() != null && r.dataBaixa().isBefore(titulo.getDataEmissao())) throw new BusinessException("Data da baixa não pode ser anterior à emissão");
        if (r.contaBancariaId() != null) {
            ContaBancaria conta = contaBancariaRepository.findForUpdate(r.contaBancariaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada ou inativa"));

            BigDecimal movimento = r.valorBaixa().add(juros).add(multa).subtract(desconto);
            if (movimento.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Valor efetivamente movimentado na conta deve ser maior que zero");
            }

            registrarMovimentoConta(empresaId, titulo, conta, r, movimento);
        }

        Baixa baixa = new Baixa();
        baixa.setEmpresaId(empresaId);
        baixa.setTituloId(tituloId);
        baixa.setParcelaId(r.parcelaId());
        baixa.setContaBancariaId(r.contaBancariaId());
        baixa.setTipoPagamentoId(r.tipoPagamentoId());
        baixa.setDataBaixa(r.dataBaixa() == null ? LocalDate.now() : r.dataBaixa());
        baixa.setValorBaixa(r.valorBaixa());
        baixa.setValorDesconto(desconto);
        baixa.setValorJuro(juros);
        baixa.setValorMulta(multa);
        baixa.setObservacao(r.observacao());
        baixa = baixaRepository.save(baixa);

        titulo.setValorSaldo(titulo.getValorSaldo().subtract(reducao));
        if (titulo.getValorSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            titulo.setValorSaldo(BigDecimal.ZERO);
            titulo.setStatus("BAIXADO");
        }
        tituloRepository.save(titulo);

        BigDecimal saldoRestante = titulo.getValorSaldo();
        if (parcela != null) {
            parcela.setValorSaldo(parcela.getValorSaldo().subtract(reducao));
            if (parcela.getValorSaldo().compareTo(BigDecimal.ZERO) <= 0) {
                parcela.setValorSaldo(BigDecimal.ZERO);
                parcela.setStatus("BAIXADA");
            }
            parcelaRepository.save(parcela);
            saldoRestante = parcela.getValorSaldo();
        }

        return new BaixaResponse(baixa.getId(), tituloId, r.parcelaId(), baixa.getValorBaixa(),
            baixa.getValorDesconto(), baixa.getValorJuro(), baixa.getValorMulta(),
            baixa.getDataBaixa(), saldoRestante, titulo.getStatus());
    }

    private void registrarMovimentoConta(Long empresaId, Titulo titulo, ContaBancaria conta,
                                         BaixaRequest r, BigDecimal valorMovimento) {
        Extrato e = new Extrato();
        e.setEmpresaId(empresaId);
        e.setContaBancariaId(conta.getId());
        e.setDataMovimento(r.dataBaixa() == null ? LocalDate.now() : r.dataBaixa());
        e.setDescricao("Baixa título " + (titulo.getNumeroDocumento() == null ? titulo.getId() : titulo.getNumeroDocumento()));
        e.setValor(valorMovimento);
        e.setTipo("R".equalsIgnoreCase(titulo.getTipo()) ? "C" : "D");

        BigDecimal saldoAnterior = conta.getSaldoInicial();
        extratoRepository.findTopByContaBancariaIdAndDeletedAtIsNullOrderByDataMovimentoDescIdDesc(conta.getId())
            .ifPresent(ultimo -> {
                if (ultimo.getSaldoAtual() != null) {
                    e.setSaldoAnterior(ultimo.getSaldoAtual());
                }
            });
        if (e.getSaldoAnterior() == null) {
            e.setSaldoAnterior(saldoAnterior);
        }
        e.setSaldoAtual("C".equals(e.getTipo())
            ? e.getSaldoAnterior().add(valorMovimento)
            : e.getSaldoAnterior().subtract(valorMovimento));
        e.setConciliado(false);
        extratoRepository.save(e);
    }

    @Override @Transactional(readOnly = true)
    public List<ParcelaResponse> vencimentos(Long empresaId, LocalDate inicio, LocalDate fim) {
        return parcelaRepository
            .findByEmpresaIdAndStatusAndDataVencimentoBetweenAndDeletedAtIsNullOrderByDataVencimento(
                empresaId, "ABERTO", inicio, fim)
            .stream().map(this::toParcela).toList();
    }

    private Titulo obterTitulo(Long empresaId, Long id) {
        return tituloRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Título não encontrado"));
    }

    private TituloResponse toResponse(Titulo t) {
        return new TituloResponse(t.getId(), t.getTipo(), t.getNumeroDocumento(), t.getDescricao(),
            t.getPessoaId(), t.getValorOriginal(), t.getValorSaldo(), t.getDataEmissao(),
            t.getDataVencimento(), t.getStatus(), t.getCentroCustoId(), t.getPlanoContasId());
    }

    private ParcelaResponse toParcela(TituloParcela p) {
        return new ParcelaResponse(p.getId(), p.getNumeroParcela(), p.getValorParcela(),
            p.getValorSaldo(), p.getDataVencimento(), p.getStatus());
    }
}
