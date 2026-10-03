package br.com.brasil_saas.producao.service;

import br.com.brasil_saas.producao.model.Producao;
import java.util.List;

public interface ProducaoService {
    Producao criarOrdem(Long empresaId, ProducaoRequest request);
    Producao finalizarProducao(Long empresaId, Long producaoId);
    List<Producao> listar(Long empresaId);
}
