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
     * Apura o resultado do exercicio: zera as contas de resultado
     * (codigo 3*) contra lucros acumulados, em 31/12. Exige jan-nov
     * fechados e dezembro aberto; recusa se ja apurado.
     */
    CtbLancamento apurarResultado(Long empresaId, Long userId, int exercicio, Long contaLucrosId);

    List<CtbFechamento> fechamentos(Long empresaId);
    CtbFechamento fechar(Long empresaId, Long userId, String periodo);
    void reabrir(Long empresaId, String periodo);
    BigDecimal[] totais(Long empresaId, Long lancamentoId);
    java.util.List<java.util.Map<String, Object>> dre(Long empresaId, int exercicio);
}
