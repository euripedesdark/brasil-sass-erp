package br.com.brasil_saas.producao.service;

import br.com.brasil_saas.producao.model.RomaneioProducao;

import java.util.List;

public interface RomaneioProducaoService {
    RomaneioProducao criar(Long empresaId, RomaneioProducaoRequest request);
    List<RomaneioProducao> listar(Long empresaId);
}
