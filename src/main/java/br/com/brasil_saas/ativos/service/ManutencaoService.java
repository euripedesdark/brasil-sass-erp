package br.com.brasil_saas.ativos.service;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.repository.*;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.rh.repository.FuncionarioRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manutencao (PM): notas de avaria/solicitacao, ordens com materiais e horas,
 * planos preventivos por tempo ou contador e medicoes de contador.
 *
 * <p>Ciclo da ordem: ABERTA -> LIBERADA -> EM_EXECUCAO -> CONCLUIDA, com
 * CANCELADA possivel antes da conclusao. Custo da ordem = materiais + mao de
 * obra apontada + servicos de terceiros.
 */
@Service
@RequiredArgsConstructor
public class ManutencaoService {

    public static final Set<String> TIPOS_ORDEM = Set.of("CORRETIVA", "PREVENTIVA", "PREDITIVA", "INSPECAO", "MELHORIA");
    public static final Set<String> PRIORIDADES = Set.of("BAIXA", "MEDIA", "ALTA", "URGENTE");
    private static final Set<String> FINALIZADAS = Set.of("CONCLUIDA", "CANCELADA");

    private final AtivoImobilizadoRepository ativos;
    private final ManutencaoRepository ordens;
    private final ManutencaoMaterialRepository materiais;
    private final ManutencaoApontamentoRepository apontamentos;
    private final PlanoManutencaoRepository planos;
    private final NotaManutencaoRepository notas;
    private final MedicaoAtivoRepository medicoes;
    private final ProdutoRepository produtos;
    private final FuncionarioRepository funcionarios;
    private final SaldoEstoqueRepository saldoEstoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final DepositoRepository depositoRepository;

    public record ConclusaoReq(LocalDate data, String causa, String solucao, BigDecimal horasParada, BigDecimal custoServico) {}

    // ----------------------------------------------------------------- ordens

    public List<Manutencao> ordens(Long empresaId) {
        return ordens.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataProgramadaAsc(empresaId);
    }

    public Manutencao ordem(Long empresaId, Long id) {
        return ordens.findById(id)
                .filter(m -> empresaId.equals(m.getEmpresaId()) && m.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Ordem de manutenção não encontrada"));
    }

    @Transactional
    public Manutencao criarOrdem(Long empresaId, Manutencao m) {
        AtivoImobilizado a = ativoEmUso(empresaId, m.getAtivoId());
        m.setId(null);
        m.setEmpresaId(empresaId);
        m.setDeletedAt(null);
        if (m.getNumero() == null || m.getNumero().isBlank()) m.setNumero(proximoNumero("OM", ordens(empresaId).stream().map(Manutencao::getNumero).toList()));
        if (m.getDescricao() == null || m.getDescricao().isBlank()) throw new BusinessException("Descrição da ordem é obrigatória");
        if (m.getTipo() == null) m.setTipo("CORRETIVA");
        if (!TIPOS_ORDEM.contains(m.getTipo())) throw new BusinessException("Tipo de ordem inválido: " + m.getTipo());
        if (m.getPrioridade() == null) m.setPrioridade("MEDIA");
        if (!PRIORIDADES.contains(m.getPrioridade())) throw new BusinessException("Prioridade inválida: " + m.getPrioridade());
        if (m.getCusto() != null && m.getCusto().signum() < 0) throw new BusinessException("Custo da manutenção não pode ser negativo");
        if (m.getCustoServico() != null && m.getCustoServico().signum() < 0) throw new BusinessException("Custo de serviço não pode ser negativo");
        m.setStatus("ABERTA");
        if (m.getCentroCustoId() == null) m.setCentroCustoId(a.getCentroCustoId());
        zerarNulos(m);
        // custo informado manualmente na criacao (fluxo antigo) vira custo de servico
        if (m.getCusto().signum() > 0 && m.getCustoServico().signum() == 0) m.setCustoServico(m.getCusto());
        recalcularCusto(m);
        return ordens.save(m);
    }

    @Transactional
    public Manutencao liberar(Long empresaId, Long id) {
        Manutencao m = ordem(empresaId, id);
        exigirStatus(m, "ABERTA");
        m.setStatus("LIBERADA");
        return ordens.save(m);
    }

    @Transactional
    public Manutencao iniciar(Long empresaId, Long id) {
        Manutencao m = ordem(empresaId, id);
        exigirStatus(m, "ABERTA", "LIBERADA");
        m.setStatus("EM_EXECUCAO");
        if (m.getDataInicio() == null) m.setDataInicio(LocalDate.now());
        return ordens.save(m);
    }

    @Transactional
    public Manutencao concluir(Long empresaId, Long id, ConclusaoReq r) {
        Manutencao m = ordem(empresaId, id);
        if ("CONCLUIDA".equals(m.getStatus())) throw new BusinessException("Manutenção já está concluída");
        if ("CANCELADA".equals(m.getStatus())) throw new BusinessException("Manutenção cancelada não pode ser concluída");
        LocalDate data = r == null || r.data() == null ? LocalDate.now() : r.data();
        if (r != null) {
            if (r.causa() != null) m.setCausa(r.causa());
            if (r.solucao() != null) m.setSolucao(r.solucao());
            if (r.horasParada() != null) {
                if (r.horasParada().signum() < 0) throw new BusinessException("Horas de parada não podem ser negativas");
                m.setHorasParada(r.horasParada());
            }
            if (r.custoServico() != null) {
                if (r.custoServico().signum() < 0) throw new BusinessException("Custo de serviço não pode ser negativo");
                m.setCustoServico(r.custoServico());
            }
        }
        m.setStatus("CONCLUIDA");
        m.setDataConclusao(data);
        if (m.getDataInicio() == null) m.setDataInicio(m.getDataProgramada() != null && !m.getDataProgramada().isAfter(data) ? m.getDataProgramada() : data);
        zerarNulos(m);
        recalcularCusto(m);
        baixarPecasEstoque(empresaId, m, data);
        Manutencao salva = ordens.save(m);

        if (m.getPlanoId() != null) planos.findById(m.getPlanoId()).filter(p -> empresaId.equals(p.getEmpresaId())).ifPresent(p -> {
            p.setUltimaExecucao(data);
            ativos.findById(p.getAtivoId()).ifPresent(a -> p.setContadorUltimaExecucao(a.getContadorAtual()));
            if (p.getIntervaloDias() != null) p.setProximaData(data.plusDays(p.getIntervaloDias()));
            planos.save(p);
        });
        if (m.getNotaId() != null) notas.findById(m.getNotaId()).filter(n -> empresaId.equals(n.getEmpresaId())).ifPresent(n -> {
            n.setStatus("ENCERRADA");
            if (n.getCausa() == null) n.setCausa(m.getCausa());
            if (Boolean.TRUE.equals(n.getEquipamentoParado()) && n.getFimParada() == null) n.setFimParada(LocalDateTime.now());
            notas.save(n);
        });
        return salva;
    }


    private void baixarPecasEstoque(Long empresaId, Manutencao m, LocalDate data) {
        List<ManutencaoMaterial> mats = materiais.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderById(empresaId, m.getId());
        for (ManutencaoMaterial mat : mats) {
            if (mat.getProdutoId() == null || mat.getQuantidade() == null || mat.getQuantidade().signum() <= 0) continue;
            SaldoEstoque saldo = saldoEstoqueRepository.findByEmpresaIdAndProdutoIdForUpdate(empresaId, mat.getProdutoId())
                    .orElseGet(() -> {
                        SaldoEstoque novo = new SaldoEstoque();
                        novo.setEmpresaId(empresaId);
                        novo.setDepositoId(depositoRepository.findByEmpresaIdAndCodigoAndAtivoTrue(empresaId, "PADRAO")
                                .orElseThrow(() -> new BusinessException("Deposito PADRAO nao encontrado para a empresa " + empresaId))
                                .getId());
                        novo.setProdutoId(mat.getProdutoId());
                        novo.setQuantidade(BigDecimal.ZERO);
                        return novo;
                    });
            BigDecimal atual = saldo.getQuantidade() == null ? BigDecimal.ZERO : saldo.getQuantidade();
            if (atual.compareTo(mat.getQuantidade()) < 0)
                throw new BusinessException("Estoque insuficiente para a peca " + mat.getDescricao() + ": saldo " + atual + ", necessario " + mat.getQuantidade());
            saldo.setQuantidade(atual.subtract(mat.getQuantidade()));
            saldoEstoqueRepository.save(saldo);
            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setEmpresaId(empresaId);
            mov.setProdutoId(mat.getProdutoId());
            mov.setTipo("SAIDA");
            mov.setOrigem("MANUTENCAO");
            mov.setOrigemId(m.getId());
            mov.setQuantidade(mat.getQuantidade());
            mov.setSaldoApos(saldo.getQuantidade());
            mov.setDataMovimento(data.atStartOfDay());
            mov.setObservacao("Saida para ordem de manutencao " + m.getNumero());
            movimentacaoRepository.save(mov);
        }
    }

    @Transactional
    public Manutencao cancelar(Long empresaId, Long id, String motivo) {
        Manutencao m = ordem(empresaId, id);
        if (FINALIZADAS.contains(m.getStatus())) throw new BusinessException("Ordem já finalizada");
        m.setStatus("CANCELADA");
        if (motivo != null && !motivo.isBlank()) m.setSolucao("Cancelada: " + motivo);
        if (m.getNotaId() != null) notas.findById(m.getNotaId()).filter(n -> empresaId.equals(n.getEmpresaId())).ifPresent(n -> {
            n.setStatus("ABERTA");
            n.setManutencaoId(null);
            notas.save(n);
        });
        return ordens.save(m);
    }

    // ------------------------------------------------- materiais e apontamentos

    public List<ManutencaoMaterial> materiais(Long empresaId, Long ordemId) {
        ordem(empresaId, ordemId);
        return materiais.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderById(empresaId, ordemId);
    }

    @Transactional
    public ManutencaoMaterial adicionarMaterial(Long empresaId, Long ordemId, ManutencaoMaterial mat) {
        Manutencao m = ordemEditavel(empresaId, ordemId);
        if (mat.getDescricao() == null || mat.getDescricao().isBlank()) throw new BusinessException("Descrição do material é obrigatória");
        if (mat.getQuantidade() == null || mat.getQuantidade().signum() <= 0) throw new BusinessException("Quantidade deve ser maior que zero");
        if (mat.getCustoUnitario() == null) mat.setCustoUnitario(BigDecimal.ZERO);
        if (mat.getCustoUnitario().signum() < 0) throw new BusinessException("Custo unitário não pode ser negativo");
        if (mat.getProdutoId() != null && produtos.findByIdAndEmpresaIdAndDeletedAtIsNull(mat.getProdutoId(), empresaId).isEmpty())
            throw new BusinessException("Produto não encontrado nesta empresa");
        mat.setId(null);
        mat.setEmpresaId(empresaId);
        mat.setManutencaoId(ordemId);
        mat.setDeletedAt(null);
        mat.setCustoTotal(mat.getQuantidade().multiply(mat.getCustoUnitario()).setScale(2, RoundingMode.HALF_UP));
        ManutencaoMaterial salvo = materiais.save(mat);
        atualizarTotais(m);
        return salvo;
    }

    @Transactional
    public void removerMaterial(Long empresaId, Long ordemId, Long materialId) {
        Manutencao m = ordemEditavel(empresaId, ordemId);
        ManutencaoMaterial mat = materiais.findById(materialId)
                .filter(x -> empresaId.equals(x.getEmpresaId()) && ordemId.equals(x.getManutencaoId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Material não encontrado"));
        mat.setDeletedAt(LocalDateTime.now());
        materiais.save(mat);
        atualizarTotais(m);
    }

    public List<ManutencaoApontamento> apontamentos(Long empresaId, Long ordemId) {
        ordem(empresaId, ordemId);
        return apontamentos.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderByDataApontamentoAscIdAsc(empresaId, ordemId);
    }

    @Transactional
    public ManutencaoApontamento apontar(Long empresaId, Long ordemId, ManutencaoApontamento ap) {
        Manutencao m = ordemEditavel(empresaId, ordemId);
        if (ap.getHoras() == null || ap.getHoras().signum() <= 0) throw new BusinessException("Horas devem ser maiores que zero");
        if (ap.getCustoHora() == null) ap.setCustoHora(BigDecimal.ZERO);
        if (ap.getCustoHora().signum() < 0) throw new BusinessException("Custo por hora não pode ser negativo");
        if (ap.getFuncionarioId() != null && funcionarios.findById(ap.getFuncionarioId())
                .filter(f -> empresaId.equals(f.getEmpresaId()) && f.getDeletedAt() == null).isEmpty())
            throw new BusinessException("Funcionário não encontrado nesta empresa");
        ap.setId(null);
        ap.setEmpresaId(empresaId);
        ap.setManutencaoId(ordemId);
        ap.setDeletedAt(null);
        if (ap.getDataApontamento() == null) ap.setDataApontamento(LocalDate.now());
        ap.setCustoTotal(ap.getHoras().multiply(ap.getCustoHora()).setScale(2, RoundingMode.HALF_UP));
        ManutencaoApontamento salvo = apontamentos.save(ap);
        if ("ABERTA".equals(m.getStatus()) || "LIBERADA".equals(m.getStatus())) {
            m.setStatus("EM_EXECUCAO");
            if (m.getDataInicio() == null) m.setDataInicio(ap.getDataApontamento());
        }
        atualizarTotais(m);
        return salvo;
    }

    @Transactional
    public void removerApontamento(Long empresaId, Long ordemId, Long apontamentoId) {
        Manutencao m = ordemEditavel(empresaId, ordemId);
        ManutencaoApontamento ap = apontamentos.findById(apontamentoId)
                .filter(x -> empresaId.equals(x.getEmpresaId()) && ordemId.equals(x.getManutencaoId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Apontamento não encontrado"));
        ap.setDeletedAt(LocalDateTime.now());
        apontamentos.save(ap);
        atualizarTotais(m);
    }

    private void atualizarTotais(Manutencao m) {
        Long e = m.getEmpresaId();
        m.setCustoMaterial(materiais.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderById(e, m.getId()).stream()
                .map(ManutencaoMaterial::getCustoTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        List<ManutencaoApontamento> aps = apontamentos.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderByDataApontamentoAscIdAsc(e, m.getId());
        m.setHorasTrabalhadas(aps.stream().map(ManutencaoApontamento::getHoras).reduce(BigDecimal.ZERO, BigDecimal::add));
        m.setCustoMaoObra(aps.stream().map(ManutencaoApontamento::getCustoTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        zerarNulos(m);
        recalcularCusto(m);
        ordens.save(m);
    }

    static void recalcularCusto(Manutencao m) {
        m.setCusto(m.getCustoMaterial().add(m.getCustoMaoObra()).add(m.getCustoServico()).setScale(2, RoundingMode.HALF_UP));
    }

    private static void zerarNulos(Manutencao m) {
        if (m.getCusto() == null) m.setCusto(BigDecimal.ZERO);
        if (m.getCustoMaterial() == null) m.setCustoMaterial(BigDecimal.ZERO);
        if (m.getCustoMaoObra() == null) m.setCustoMaoObra(BigDecimal.ZERO);
        if (m.getCustoServico() == null) m.setCustoServico(BigDecimal.ZERO);
        if (m.getHorasTrabalhadas() == null) m.setHorasTrabalhadas(BigDecimal.ZERO);
        if (m.getHorasParada() == null) m.setHorasParada(BigDecimal.ZERO);
    }

    private Manutencao ordemEditavel(Long empresaId, Long id) {
        Manutencao m = ordem(empresaId, id);
        if (FINALIZADAS.contains(m.getStatus())) throw new BusinessException("Ordem finalizada não pode ser alterada");
        return m;
    }

    private static void exigirStatus(Manutencao m, String... permitidos) {
        if (!Arrays.asList(permitidos).contains(m.getStatus()))
            throw new BusinessException("Operação não permitida com a ordem em " + m.getStatus());
    }

    // ------------------------------------------------------------------ notas

    public List<NotaManutencao> notas(Long empresaId) {
        return notas.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataNotaDescIdDesc(empresaId);
    }

    @Transactional
    public NotaManutencao criarNota(Long empresaId, Long usuarioId, NotaManutencao n) {
        ativoEmUso(empresaId, n.getAtivoId());
        if (n.getDescricao() == null || n.getDescricao().isBlank()) throw new BusinessException("Descrição da nota é obrigatória");
        if (n.getTipo() == null) n.setTipo("AVARIA");
        if (!Set.of("AVARIA", "SOLICITACAO", "ATIVIDADE").contains(n.getTipo())) throw new BusinessException("Tipo de nota inválido: " + n.getTipo());
        if (n.getPrioridade() == null) n.setPrioridade("MEDIA");
        if (!PRIORIDADES.contains(n.getPrioridade())) throw new BusinessException("Prioridade inválida: " + n.getPrioridade());
        n.setId(null);
        n.setEmpresaId(empresaId);
        n.setDeletedAt(null);
        n.setStatus("ABERTA");
        n.setManutencaoId(null);
        if (n.getNumero() == null || n.getNumero().isBlank()) n.setNumero(proximoNumero("NM", notas(empresaId).stream().map(NotaManutencao::getNumero).toList()));
        if (n.getDataNota() == null) n.setDataNota(LocalDate.now());
        if (n.getEquipamentoParado() == null) n.setEquipamentoParado(Boolean.FALSE);
        if (n.getEquipamentoParado() && n.getInicioParada() == null) n.setInicioParada(LocalDateTime.now());
        if (n.getSolicitanteId() == null) n.setSolicitanteId(usuarioId);
        return notas.save(n);
    }

    /** Converte a nota em ordem (corretiva para avaria). */
    @Transactional
    public Manutencao gerarOrdemDaNota(Long empresaId, Long notaId) {
        NotaManutencao n = nota(empresaId, notaId);
        if (!"ABERTA".equals(n.getStatus())) throw new BusinessException("Nota já possui ordem ou está encerrada");
        Manutencao m = new Manutencao();
        m.setAtivoId(n.getAtivoId());
        m.setNotaId(n.getId());
        m.setTipo("SOLICITACAO".equals(n.getTipo()) ? "MELHORIA" : "CORRETIVA");
        m.setPrioridade(n.getPrioridade());
        m.setDescricao(n.getNumero() + " - " + n.getDescricao());
        m.setDataProgramada(LocalDate.now());
        Manutencao criada = criarOrdem(empresaId, m);
        n.setStatus("EM_PROCESSAMENTO");
        n.setManutencaoId(criada.getId());
        notas.save(n);
        return criada;
    }

    @Transactional
    public NotaManutencao encerrarNota(Long empresaId, Long notaId, String causa) {
        NotaManutencao n = nota(empresaId, notaId);
        if ("ENCERRADA".equals(n.getStatus())) throw new BusinessException("Nota já encerrada");
        if (n.getManutencaoId() != null) {
            Manutencao m = ordem(empresaId, n.getManutencaoId());
            if (!FINALIZADAS.contains(m.getStatus())) throw new BusinessException("Conclua ou cancele a ordem " + m.getNumero() + " antes de encerrar a nota");
        }
        n.setStatus("ENCERRADA");
        if (causa != null && !causa.isBlank()) n.setCausa(causa);
        if (Boolean.TRUE.equals(n.getEquipamentoParado()) && n.getFimParada() == null) n.setFimParada(LocalDateTime.now());
        return notas.save(n);
    }

    private NotaManutencao nota(Long empresaId, Long id) {
        return notas.findById(id)
                .filter(n -> empresaId.equals(n.getEmpresaId()) && n.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Nota de manutenção não encontrada"));
    }

    // ----------------------------------------------------------------- planos

    public List<PlanoManutencao> planos(Long empresaId) {
        return planos.findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(empresaId);
    }

    @Transactional
    public PlanoManutencao salvarPlano(Long empresaId, Long id, PlanoManutencao p) {
        ativoEmUso(empresaId, p.getAtivoId());
        if (p.getCodigo() == null || p.getCodigo().isBlank()) throw new BusinessException("Código do plano é obrigatório");
        if (p.getDescricao() == null || p.getDescricao().isBlank()) throw new BusinessException("Descrição do plano é obrigatória");
        if (p.getTipoCiclo() == null) p.setTipoCiclo("TEMPO");
        switch (p.getTipoCiclo()) {
            case "TEMPO" -> {
                if (p.getIntervaloDias() == null || p.getIntervaloDias() <= 0) throw new BusinessException("Intervalo em dias deve ser maior que zero");
            }
            case "CONTADOR" -> {
                if (p.getIntervaloContador() == null || p.getIntervaloContador().signum() <= 0) throw new BusinessException("Intervalo do contador deve ser maior que zero");
            }
            default -> throw new BusinessException("Tipo de ciclo inválido: " + p.getTipoCiclo());
        }
        if (p.getAntecedenciaDias() == null || p.getAntecedenciaDias() < 0) p.setAntecedenciaDias(0);
        if (p.getPrioridade() == null) p.setPrioridade("MEDIA");
        if (!PRIORIDADES.contains(p.getPrioridade())) throw new BusinessException("Prioridade inválida: " + p.getPrioridade());
        PlanoManutencao atual = id == null ? null : plano(empresaId, id);
        boolean reprogramar = atual != null && Objects.equals(atual.getProximaData(), p.getProximaData())
                && (!Objects.equals(atual.getIntervaloDias(), p.getIntervaloDias()) || !Objects.equals(atual.getTipoCiclo(), p.getTipoCiclo()));
        if (atual != null) {
            p.setId(atual.getId());
            p.setUuid(atual.getUuid());
            p.setCreatedAt(atual.getCreatedAt());
            if (p.getUltimaExecucao() == null) p.setUltimaExecucao(atual.getUltimaExecucao());
            if (p.getContadorUltimaExecucao() == null) p.setContadorUltimaExecucao(atual.getContadorUltimaExecucao());
        } else {
            p.setId(null);
        }
        p.setEmpresaId(empresaId);
        p.setDeletedAt(null);
        if (p.getAtivo() == null) p.setAtivo(Boolean.TRUE);
        if ("TEMPO".equals(p.getTipoCiclo()) && (p.getProximaData() == null || reprogramar))
            p.setProximaData((p.getUltimaExecucao() == null ? LocalDate.now() : p.getUltimaExecucao()).plusDays(p.getIntervaloDias()));
        if ("CONTADOR".equals(p.getTipoCiclo()) && p.getContadorUltimaExecucao() == null)
            ativos.findById(p.getAtivoId()).ifPresent(a -> p.setContadorUltimaExecucao(a.getContadorAtual() == null ? BigDecimal.ZERO : a.getContadorAtual()));
        return planos.save(p);
    }

    @Transactional
    public void excluirPlano(Long empresaId, Long id) {
        PlanoManutencao p = plano(empresaId, id);
        p.setDeletedAt(LocalDateTime.now());
        planos.save(p);
    }

    /** Planos vencidos (ou dentro da antecedencia) que ainda nao possuem ordem em aberto. */
    public List<PlanoManutencao> planosVencidos(Long empresaId, LocalDate referencia) {
        Set<Long> comOrdemAberta = new HashSet<>();
        for (Manutencao m : ordens(empresaId))
            if (m.getPlanoId() != null && !FINALIZADAS.contains(m.getStatus())) comOrdemAberta.add(m.getPlanoId());
        List<PlanoManutencao> out = new ArrayList<>();
        for (PlanoManutencao p : planos(empresaId)) {
            if (!Boolean.TRUE.equals(p.getAtivo()) || comOrdemAberta.contains(p.getId())) continue;
            AtivoImobilizado a = ativos.findById(p.getAtivoId()).orElse(null);
            if (a == null || "BAIXADO".equals(a.getStatus())) continue;
            if (vencido(p, a, referencia)) out.add(p);
        }
        return out;
    }

    static boolean vencido(PlanoManutencao p, AtivoImobilizado a, LocalDate referencia) {
        if ("CONTADOR".equals(p.getTipoCiclo())) {
            if (a.getContadorAtual() == null || p.getIntervaloContador() == null) return false;
            BigDecimal ultimo = p.getContadorUltimaExecucao() == null ? BigDecimal.ZERO : p.getContadorUltimaExecucao();
            return a.getContadorAtual().subtract(ultimo).compareTo(p.getIntervaloContador()) >= 0;
        }
        if (p.getProximaData() == null) return false;
        int antecedencia = p.getAntecedenciaDias() == null ? 0 : p.getAntecedenciaDias();
        return !p.getProximaData().minusDays(antecedencia).isAfter(referencia);
    }

    @Transactional
    public List<Manutencao> gerarOrdensDosPlanos(Long empresaId, LocalDate referencia) {
        List<Manutencao> criadas = new ArrayList<>();
        for (PlanoManutencao p : planosVencidos(empresaId, referencia)) {
            Manutencao m = new Manutencao();
            m.setAtivoId(p.getAtivoId());
            m.setPlanoId(p.getId());
            m.setTipo("PREVENTIVA");
            m.setPrioridade(p.getPrioridade());
            m.setDescricao(p.getCodigo() + " - " + p.getDescricao());
            m.setChecklist(p.getChecklist());
            m.setResponsavelId(p.getResponsavelId());
            m.setDataProgramada(p.getProximaData() != null && "TEMPO".equals(p.getTipoCiclo()) ? p.getProximaData() : referencia);
            criadas.add(criarOrdem(empresaId, m));
        }
        return criadas;
    }

    private PlanoManutencao plano(Long empresaId, Long id) {
        return planos.findById(id)
                .filter(p -> empresaId.equals(p.getEmpresaId()) && p.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Plano de manutenção não encontrado"));
    }

    // --------------------------------------------------------------- medicoes

    public List<MedicaoAtivo> medicoes(Long empresaId, Long ativoId) {
        return ativoId == null
                ? medicoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataMedicaoDescIdDesc(empresaId)
                : medicoes.findAllByEmpresaIdAndAtivoIdAndDeletedAtIsNullOrderByDataMedicaoDescIdDesc(empresaId, ativoId);
    }

    @Transactional
    public MedicaoAtivo registrarMedicao(Long empresaId, MedicaoAtivo med) {
        AtivoImobilizado a = ativoEmUso(empresaId, med.getAtivoId());
        if (med.getValor() == null || med.getValor().signum() < 0) throw new BusinessException("Valor da medição inválido");
        if (a.getContadorAtual() != null && med.getValor().compareTo(a.getContadorAtual()) < 0)
            throw new BusinessException("Medição menor que o contador atual (" + a.getContadorAtual() + ")");
        med.setId(null);
        med.setEmpresaId(empresaId);
        med.setDeletedAt(null);
        if (med.getDataMedicao() == null) med.setDataMedicao(LocalDate.now());
        if (med.getUnidade() == null) med.setUnidade(a.getUnidadeContador());
        a.setContadorAtual(med.getValor());
        if (a.getUnidadeContador() == null) a.setUnidadeContador(med.getUnidade());
        ativos.save(a);
        return medicoes.save(med);
    }

    // -------------------------------------------------------------- historico

    public Map<String, Object> historico(Long empresaId, Long ativoId) {
        AtivoImobilizado a = ativos.findById(ativoId)
                .filter(x -> empresaId.equals(x.getEmpresaId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Ativo não encontrado"));
        List<Manutencao> os = ordens(empresaId).stream().filter(m -> ativoId.equals(m.getAtivoId())).toList();
        List<NotaManutencao> ns = notas(empresaId).stream().filter(n -> ativoId.equals(n.getAtivoId())).toList();
        List<Manutencao> concluidas = os.stream().filter(m -> "CONCLUIDA".equals(m.getStatus())).toList();
        BigDecimal horasParadaNotas = ns.stream()
                .filter(n -> n.getInicioParada() != null && n.getFimParada() != null)
                .map(n -> BigDecimal.valueOf(Duration.between(n.getInicioParada(), n.getFimParada()).toMinutes()).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ativoId", a.getId());
        out.put("codigo", a.getCodigo());
        out.put("descricao", a.getDescricao());
        out.put("contadorAtual", a.getContadorAtual());
        out.put("ordens", os);
        out.put("notas", ns);
        out.put("ordensAbertas", os.stream().filter(m -> !FINALIZADAS.contains(m.getStatus())).count());
        out.put("ordensConcluidas", concluidas.size());
        out.put("custoMaterial", soma(concluidas, Manutencao::getCustoMaterial));
        out.put("custoMaoObra", soma(concluidas, Manutencao::getCustoMaoObra));
        out.put("custoServico", soma(concluidas, Manutencao::getCustoServico));
        out.put("custoTotal", soma(concluidas, Manutencao::getCusto));
        out.put("horasTrabalhadas", soma(concluidas, Manutencao::getHorasTrabalhadas));
        out.put("horasParada", soma(concluidas, Manutencao::getHorasParada).add(horasParadaNotas));
        return out;
    }

    private static BigDecimal soma(List<Manutencao> ms, java.util.function.Function<Manutencao, BigDecimal> f) {
        return ms.stream().map(f).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ------------------------------------------------------------------ apoio

    private AtivoImobilizado ativoEmUso(Long empresaId, Long ativoId) {
        if (ativoId == null) throw new BusinessException("Ativo é obrigatório");
        return ativos.findById(ativoId)
                .filter(a -> empresaId.equals(a.getEmpresaId()) && a.getDeletedAt() == null && !"BAIXADO".equals(a.getStatus()))
                .orElseThrow(() -> new BusinessException("Ativo inexistente, de outra empresa ou baixado"));
    }

    static String proximoNumero(String prefixo, Collection<String> existentes) {
        int max = 0;
        for (String n : existentes) {
            if (n == null || !n.startsWith(prefixo + "-")) continue;
            try {
                max = Math.max(max, Integer.parseInt(n.substring(prefixo.length() + 1)));
            } catch (NumberFormatException ignored) {
                // numero informado manualmente fora do padrao
            }
        }
        return String.format("%s-%06d", prefixo, max + 1);
    }
}
