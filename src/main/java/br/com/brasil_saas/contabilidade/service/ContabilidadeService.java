package br.com.brasil_saas.contabilidade.service;
import br.com.brasil_saas.contabilidade.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
public interface ContabilidadeService {
    List<CtbLancamento> lancamentos(Long empresaId, String periodo, String status);
    CtbLancamento salvar(Long empresaId, CtbLancamento l);
    CtbPartida addPartida(Long empresaId, Long lancamentoId, CtbPartida p);
    List<CtbPartida> partidas(Long empresaId, Long lancamentoId);
    void removerPartida(Long empresaId, Long lancamentoId, Long partidaId);
    CtbLancamento lancar(Long empresaId, Long id);
    CtbLancamento estornar(Long empresaId, Long id, String motivo);
    List<Map<String, Object>> razao(Long empresaId, Long contaId, LocalDate de, LocalDate ate);
    List<Map<String, Object>> balancete(Long empresaId, LocalDate de, LocalDate ate);
    Map<String, Object> balanco(Long empresaId, int exercicio);
    CtbLancamento gerarDeTitulo(Long empresaId, Long userId, Long tituloId, Long contaDebitoId, Long contaCreditoId);
    /**
     * Espelha proporcionalmente os lancamentos LANCADOS de um titulo,
     * invertendo as partidas, para ajuste de devolucao. Reusa as contas
     * do lancamento original: nao inventa plano de contas.
     */
    EspelhoContabil espelharAjusteDevolucao(Long empresaId, Long tituloOrigemId, Long tituloDestinoId, BigDecimal proporcao, String historico);

    /** Resultado do espelho: APLICADO, SEM_LANCAMENTO_ORIGINAL ou PERIODO_FECHADO. */
    record EspelhoContabil(java.util.List<CtbLancamento> lancamentos, String situacao) {
    }

    List<CtbFechamento> fechamentos(Long empresaId);
    CtbFechamento fechar(Long empresaId, Long userId, String periodo);
    void reabrir(Long empresaId, String periodo);
    BigDecimal[] totais(Long empresaId, Long lancamentoId);
    java.util.List<java.util.Map<String, Object>> dre(Long empresaId, int exercicio);
}
