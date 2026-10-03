package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CustoProducaoService {

    /** Resultado do custeio de uma OP. Valores monetarios com 4 casas. */
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

    /**
     * Custeia a OP sem gravar nada.
     * Materiais: quantidade do item x (custo_unitario do item, se preenchido; senao preco_custo do produto).
     * Mao de obra: horas apontadas x valor_hora do funcionario.
     * Quantidade boa: soma(produzido) - soma(refugo) dos apontamentos; sem apontamento de
     * producao, usa a quantidade planejada. O refugo nao vira estoque: seu custo e
     * rateado sobre as unidades boas (custo unitario = custo total / quantidade boa).
     */
    Resultado calcular(Long empresaId, Long producaoId);
}
