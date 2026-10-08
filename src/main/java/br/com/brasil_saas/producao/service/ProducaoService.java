package br.com.brasil_saas.producao.service;

import br.com.brasil_saas.producao.model.Producao;
import java.util.List;

public interface ProducaoService {
    Producao criarOrdem(Long empresaId, ProducaoRequest request);
    /** ABERTO → EM_PROCESSO */
    Producao iniciarProducao(Long empresaId, Long producaoId);
    /** ABERTO|EM_PROCESSO → FINALIZADO (baixa insumos, entra produto) */
    Producao finalizarProducao(Long empresaId, Long producaoId);
    /** ABERTO → CANCELADO (sem movimento de estoque) */
    Producao cancelarProducao(Long empresaId, Long producaoId);
    List<Producao> listar(Long empresaId);
}
