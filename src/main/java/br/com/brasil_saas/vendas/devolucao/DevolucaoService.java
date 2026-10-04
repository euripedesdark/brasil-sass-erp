package br.com.brasil_saas.vendas.devolucao;
import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Service @RequiredArgsConstructor
public class DevolucaoService {
    private final VenDevolucaoRepository devolucoes;
    private final VenDevolucaoItemRepository itens;
    private final PedidoVendaRepository pedidos;
    private final SaldoEstoqueRepository saldos;
    private final MovimentacaoEstoqueRepository movimentacoes;
    private final DepositoRepository depositos;
    private <T> T exigir(Optional<T> o, String msg) {
        return o.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, msg));
    }
    public List<VenDevolucao> listar(Long empresaId) { return devolucoes.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId); }
    public List<VenDevolucaoItem> itens(Long empresaId, Long id) {
        exigir(devolucoes.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Devolucao inexistente");
        return itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId);
    }
    @Transactional public VenDevolucao solicitar(Long empresaId, Long pedidoId, String motivo, Map<Long, BigDecimal> itensQtd) {
        PedidoVenda p = exigir(pedidos.findByIdAndEmpresaId(pedidoId, empresaId), "Pedido inexistente");
        if (!"FATURADO".equals(p.getStatus())) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Somente pedido faturado");
        if (motivo == null || motivo.isBlank()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Motivo obrigatorio");
        if (itensQtd == null || itensQtd.isEmpty()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Informe ao menos um item");
        VenDevolucao d = new VenDevolucao();
        d.setEmpresaId(empresaId);
        d.setPedidoId(pedidoId);
        d.setMotivo(motivo);
        d.setStatus("SOLICITADA");
        d = devolucoes.save(d);
        Map<Long, BigDecimal> vendidas = new LinkedHashMap<>();
        if (p.getItens() != null) for (var it : p.getItens()) if (it.getProdutoId() != null) vendidas.merge(it.getProdutoId(), it.getQuantidade() == null ? BigDecimal.ZERO : it.getQuantidade(), BigDecimal::add);
        boolean tem = false;
        for (var e : itensQtd.entrySet()) {
            BigDecimal vendida = vendidas.getOrDefault(e.getKey(), BigDecimal.ZERO);
            BigDecimal qtd = e.getValue() == null ? BigDecimal.ZERO : e.getValue();
            if (qtd.signum() <= 0 || qtd.compareTo(vendida) > 0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Quantidade invalida para o produto " + e.getKey());
            VenDevolucaoItem i = new VenDevolucaoItem();
            i.setDevolucaoId(d.getId());
            i.setProdutoId(e.getKey());
            i.setQuantidade(qtd);
            i.setQtdRecebida(BigDecimal.ZERO);
            itens.save(i);
            tem = true;
        }
        if (tem == false) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Informe ao menos um item");
        return d;
    }
    @Transactional public VenDevolucao decidir(Long empresaId, Long userId, Long id, boolean aprovar) {
        VenDevolucao d = exigir(devolucoes.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Devolucao inexistente");
        if ("SOLICITADA".equals(d.getStatus()) == false) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Devolucao ja decidida");
        d.setStatus(aprovar ? "APROVADA" : "REJEITADA");
        d.setDecididaPor(userId);
        d.setDecididaEm(LocalDateTime.now());
        return devolucoes.save(d);
    }
    @Transactional public VenDevolucao receber(Long empresaId, Long id) {
        VenDevolucao d = exigir(devolucoes.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId), "Devolucao inexistente");
        if ("APROVADA".equals(d.getStatus()) == false) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Aprove antes de receber");
        Long depId = depositos.findByEmpresaIdAndCodigoAndAtivoTrue(empresaId, "PADRAO").orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Deposito PADRAO inexistente")).getId();
        for (VenDevolucaoItem i : itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)) {
            SaldoEstoque s = saldos.findByEmpresaIdAndProdutoIdForUpdate(empresaId, i.getProdutoId()).orElseGet(() -> {
                SaldoEstoque n = new SaldoEstoque();
                n.setEmpresaId(empresaId);
                n.setDepositoId(depId);
                n.setProdutoId(i.getProdutoId());
                n.setQuantidade(BigDecimal.ZERO);
                return n; });
            s.setQuantidade((s.getQuantidade() == null ? BigDecimal.ZERO : s.getQuantidade()).add(i.getQuantidade()));
            saldos.save(s);
            MovimentacaoEstoque m = new MovimentacaoEstoque();
            m.setEmpresaId(empresaId);
            m.setProdutoId(i.getProdutoId());
            m.setDepositoId(depId);
            m.setTipo("ENTRADA");
            m.setOrigem("DEVOLUCAO");
            m.setOrigemId(id);
            m.setQuantidade(i.getQuantidade());
            m.setSaldoApos(s.getQuantidade());
            m.setObservacao("Devolucao de venda #" + d.getPedidoId());
            movimentacoes.save(m);
            i.setQtdRecebida(i.getQuantidade());
            itens.save(i);
        }
        d.setStatus("RECEBIDA");
        d.setRecebidaEm(LocalDateTime.now());
        return devolucoes.save(d);
    }
}
