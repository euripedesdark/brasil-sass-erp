package br.com.brasil_saas.producao.service;

import br.com.brasil_saas.producao.model.RomaneioProducao;

import java.util.List;

public interface RomaneioProducaoService {
    RomaneioProducao criar(Long empresaId, RomaneioProducaoRequest request);
    List<RomaneioProducao> listar(Long empresaId);
    /** ABERTO → CONFERIDO */
    RomaneioProducao conferir(Long empresaId, Long id);
    /** CONFERIDO → LIBERADO */
    RomaneioProducao liberar(Long empresaId, Long id);
    /** ABERTO → CANCELADO */
    RomaneioProducao cancelar(Long empresaId, Long id);
}
