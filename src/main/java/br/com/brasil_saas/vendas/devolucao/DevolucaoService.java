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
        // A trava do pedido serializa solicitacoes concorrentes sobre o mesmo pedido.
        PedidoVenda p = exigir(pedidos.findByIdForUpdateAndEmpresaId(pedidoId, empresaId), "Pedido inexistente");
        if (!"FATURADO".equals(p.getStatus())) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Somente pedido faturado");
        if (motivo == null || motivo.isBlank()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Motivo obrigatorio");
        if (itensQtd == null || itensQtd.isEmpty()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Informe ao menos um item");
        if (motivo.length() > 100) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Motivo deve ter no maximo 100 caracteres");
        // Quantidade ja comprometida por outras devolucoes (solicitadas, aprovadas ou recebidas).
        Map<Long, BigDecimal> jaDevolvido = new HashMap<>();
        for (VenDevolucao anterior : devolucoes.findByPedidoIdAndEmpresaIdAndStatusInAndDeletedAtIsNull(
                pedidoId, empresaId, List.of("SOLICITADA", "APROVADA", "RECEBIDA")))
            for (VenDevolucaoItem x : itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(anterior.getId(), empresaId))
                jaDevolvido.merge(x.getProdutoId(), x.getQuantidade() == null ? BigDecimal.ZERO : x.getQuantidade(), BigDecimal::add);
        Map<Long, BigDecimal> vendidas = new LinkedHashMap<>();
        if (p.getItens() != null) for (var it : p.getItens()) if (it.getDeletedAt() == null && it.getProdutoId() != null)
            vendidas.merge(it.getProdutoId(), it.getQuantidade() == null ? BigDecimal.ZERO : it.getQuantidade(), BigDecimal::add);
        for (var e : itensQtd.entrySet()) {
            BigDecimal saldo = vendidas.getOrDefault(e.getKey(), BigDecimal.ZERO)
                    .subtract(jaDevolvido.getOrDefault(e.getKey(), BigDecimal.ZERO));
            if (e.getKey() == null || e.getValue() == null || e.getValue().signum() <= 0
                    || e.getValue().stripTrailingZeros().scale() > 3 || e.getValue().compareTo(saldo) > 0)
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Quantidade invalida, acima do saldo ou com mais de 3 casas decimais");
        }
        VenDevolucao d = new VenDevolucao(); d.setEmpresaId(empresaId); d.setPedidoId(pedidoId);
        d.setMotivo(motivo); d.setStatus("SOLICITADA"); d = devolucoes.save(d);
        for (var e : itensQtd.entrySet()) {
            VenDevolucaoItem i = new VenDevolucaoItem(); i.setEmpresaId(empresaId); i.setDevolucaoId(d.getId());
            i.setProdutoId(e.getKey()); i.setQuantidade(e.getValue()); i.setQtdRecebida(BigDecimal.ZERO); itens.save(i);
        }
        return d;
    }
    @Transactional public VenDevolucao decidir(Long empresaId, Long userId, Long id, boolean aprovar) {
        VenDevolucao d = exigir(devolucoes.findByIdForUpdate(id, empresaId), "Devolucao inexistente");
        if ("SOLICITADA".equals(d.getStatus()) == false) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Devolucao ja decidida");
        d.setStatus(aprovar ? "APROVADA" : "REJEITADA");
        d.setDecididaPor(userId);
        d.setDecididaEm(LocalDateTime.now());
        return devolucoes.save(d);
    }
    @Transactional public VenDevolucao receber(Long empresaId, Long id) {
        VenDevolucao d = exigir(devolucoes.findByIdForUpdate(id, empresaId), "Devolucao inexistente");
        if ("APROVADA".equals(d.getStatus()) == false) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Aprove antes de receber");
        Long depId = depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(empresaId, "PADRAO")
                .or(() -> depositos.findFirstByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Nenhum deposito ativo para receber devolucao")).getId();
        depositos.findAtivoForUpdate(depId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Deposito indisponivel"));
        var linhas = new ArrayList<>(itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId));
        if (linhas.isEmpty()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Devolucao sem itens");
        for (var i : linhas) {
            if (i.getProdutoId() == null || i.getQuantidade() == null || i.getQuantidade().signum() <= 0
                    || i.getQuantidade().stripTrailingZeros().scale() > 3
                    || (i.getQtdRecebida() != null && i.getQtdRecebida().signum() != 0))
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Item de devolucao inconsistente");
        }
        linhas.sort(Comparator.comparing(VenDevolucaoItem::getProdutoId));
        for (VenDevolucaoItem i : linhas) {
            SaldoEstoque s = saldos.findForUpdate(empresaId, depId, i.getProdutoId()).orElseGet(() -> {
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
