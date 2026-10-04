package br.com.brasil_saas.producao.service.impl;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.producao.model.ApontamentoProducao;
import br.com.brasil_saas.producao.model.ItemProducao;
import br.com.brasil_saas.producao.model.Producao;
import br.com.brasil_saas.producao.repository.ApontamentoProducaoRepository;
import br.com.brasil_saas.producao.repository.ProducaoRepository;
import br.com.brasil_saas.producao.service.CustoProducaoService;
import br.com.brasil_saas.rh.model.Funcionario;
import br.com.brasil_saas.rh.repository.FuncionarioRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustoProducaoServiceImpl implements CustoProducaoService {
    private static final int ESC = 4;

    private final ProducaoRepository producoes;
    private final ApontamentoProducaoRepository apontamentos;
    private final ProdutoRepository produtos;
    private final FuncionarioRepository funcionarios;

    @Override
    @Transactional(readOnly = true)
    public Resultado calcular(Long empresaId, Long producaoId) {
        Producao op = producoes.findById(producaoId)
                .filter(x -> empresaId.equals(x.getEmpresaId()))
                .orElseThrow(() -> new BusinessException("Ordem de produção não encontrada"));

        List<String> alertas = new ArrayList<>();
        List<Map<String, Object>> mats = new ArrayList<>();
        BigDecimal custoMat = BigDecimal.ZERO;

        for (ItemProducao it : op.getItens()) {
            BigDecimal unit = it.getCustoUnitario();
            String origem = "ITEM";
            if (unit == null) {
                Produto pr = produtos.findByIdAndEmpresaIdAndDeletedAtIsNull(it.getProdutoId(), empresaId).orElse(null);
                unit = pr == null ? null : pr.getPrecoCusto();
                origem = "PRODUTO";
            }
            if (unit == null || unit.signum() <= 0) {
                alertas.add("Material " + it.getProdutoId() + " sem custo: custo subestimado.");
                unit = BigDecimal.ZERO;
            }
            BigDecimal total = nz(it.getQuantidade()).multiply(unit).setScale(ESC, RoundingMode.HALF_UP);
            custoMat = custoMat.add(total);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("produtoId", it.getProdutoId());
            m.put("quantidade", it.getQuantidade());
            m.put("custoUnitario", unit);
            m.put("origemCusto", origem);
            m.put("custoTotal", total);
            mats.add(m);
        }

        List<ApontamentoProducao> aps = apontamentos.findByEmpresaIdAndProducaoId(empresaId, producaoId).stream()
                .filter(a -> !"CANCELADO".equals(a.getStatus()))
                .toList();

        Map<Long, BigDecimal> horasPorFunc = new LinkedHashMap<>();
        BigDecimal produzido = BigDecimal.ZERO;
        BigDecimal refugo = BigDecimal.ZERO;
        boolean semFuncionario = false;

        for (ApontamentoProducao a : aps) {
            produzido = produzido.add(nz(a.getQuantidadeProduzida()));
            refugo = refugo.add(nz(a.getQuantidadeRefugo()));
            if (nz(a.getHorasTrabalhadas()).signum() > 0) {
                if (a.getFuncionarioId() == null) semFuncionario = true;
                else horasPorFunc.merge(a.getFuncionarioId(), a.getHorasTrabalhadas(), BigDecimal::add);
            }
        }
        if (semFuncionario) alertas.add("Há horas apontadas sem funcionário: mão de obra não custeada nessas horas.");

        List<Map<String, Object>> mo = new ArrayList<>();
        BigDecimal custoMo = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> e : horasPorFunc.entrySet()) {
            Funcionario f = funcionarios.findById(e.getKey())
                    .filter(x -> empresaId.equals(x.getEmpresaId())).orElse(null);
            BigDecimal vh = f == null ? BigDecimal.ZERO : nz(f.getValorHora());
            if (vh.signum() <= 0) {
                alertas.add("Funcionário " + e.getKey() + " sem valor/hora: mão de obra não custeada.");
            }
            BigDecimal total = e.getValue().multiply(vh).setScale(ESC, RoundingMode.HALF_UP);
            custoMo = custoMo.add(total);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("funcionarioId", e.getKey());
            m.put("horas", e.getValue());
            m.put("valorHora", vh);
            m.put("custoTotal", total);
            mo.add(m);
        }

        BigDecimal boa;
        if (produzido.signum() > 0) {
            boa = produzido.subtract(refugo).max(BigDecimal.ZERO);
        } else {
            boa = nz(op.getQuantidadePlanejada());
            alertas.add("Sem apontamento de produção: usando a quantidade planejada como produzida.");
        }

        BigDecimal total = custoMat.add(custoMo).setScale(ESC, RoundingMode.HALF_UP);
        BigDecimal unit = boa.signum() > 0 ? total.divide(boa, ESC, RoundingMode.HALF_UP) : null;
        if (unit == null) alertas.add("Nenhuma unidade boa: custo unitário indefinido.");

        return new Resultado(custoMat.setScale(ESC, RoundingMode.HALF_UP),
                custoMo.setScale(ESC, RoundingMode.HALF_UP), total,
                produzido, refugo, boa, unit, mats, mo, alertas);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
