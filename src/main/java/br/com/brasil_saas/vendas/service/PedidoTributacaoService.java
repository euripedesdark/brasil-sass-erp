package br.com.brasil_saas.vendas.service;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.fiscal.service.TributacaoSimuladorService;
import br.com.brasil_saas.vendas.dto.ItemPedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prévia de impostos de um pedido de venda (não grava NFe).
 * Usa NCM/CFOP do produto + UF da empresa e UF destino informada.
 */
@Service
@RequiredArgsConstructor
public class PedidoTributacaoService {

    private final TributacaoSimuladorService tributacao;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;

    public Map<String, Object> prever(Long empresaId, PedidoVendaRequest req, String ufDestino,
                                      Boolean consumidorFinal, Boolean contribuinte) {
        Empresa emp = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));
        String ufOrigem = emp.getUf();
        if (ufDestino == null || ufDestino.isBlank()) {
            ufDestino = ufOrigem;
        }
        List<Map<String, Object>> itens = new ArrayList<>();
        int i = 0;
        if (req.itens() != null) {
            for (ItemPedidoVendaRequest it : req.itens()) {
                i++;
                BigDecimal qtd = it.quantidade() == null ? BigDecimal.ONE : it.quantidade();
                BigDecimal vu = it.valorUnitario() == null ? BigDecimal.ZERO : it.valorUnitario();
                BigDecimal base = qtd.multiply(vu).setScale(2, RoundingMode.HALF_UP);
                String ncm = null;
                String cfop = null;
                if (it.produtoId() != null) {
                    Produto p = produtoRepository.findById(it.produtoId()).orElse(null);
                    if (p != null && (p.getEmpresaId() == null || p.getEmpresaId().equals(empresaId))) {
                        ncm = p.getNcm();
                        cfop = p.getCfopPadrao();
                    }
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("ref", i);
                row.put("produtoId", it.produtoId());
                row.put("base", base);
                row.put("ncm", ncm);
                row.put("cfop", cfop != null ? cfop : "5102");
                itens.add(row);
            }
        }
        Map<String, Object> out = tributacao.simularItens(empresaId, ufOrigem, ufDestino,
                consumidorFinal, contribuinte, itens);
        out.put("ufOrigem", ufOrigem);
        out.put("ufDestino", ufDestino);
        return out;
    }
}
