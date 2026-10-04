package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CustoProducaoService {
    record Resultado(
            BigDecimal custoMateriais,
            BigDecimal custoMaoDeObra,
            BigDecimal custoTotal,
            BigDecimal quantidadeApontada,
            BigDecimal quantidadeRefugo,
            BigDecimal quantidadeBoa,
            BigDecimal custoUnitario,
            List<Map<String, Object>> materiais,
            List<Map<String, Object>> maoDeObra,
            List<String> alertas) {}

    Resultado calcular(Long empresaId, Long producaoId);
}
