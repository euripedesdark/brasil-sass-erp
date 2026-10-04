package br.com.brasil_saas.compras.service;
import br.com.brasil_saas.compras.model.DevCompra;
import br.com.brasil_saas.compras.model.DevCompraItem;
import br.com.brasil_saas.compras.model.ItemPedidoCompra;
import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.repository.DevCompraItemRepository;
import br.com.brasil_saas.compras.repository.DevCompraRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Service @RequiredArgsConstructor
public class DevCompraService {
    private final DevCompraRepository devolucoes;
    private final DevCompraItemRepository itens;
    private final PedidoCompraRepository pedidos;
    private final SaldoEstoqueRepository saldos;
    private final MovimentacaoEstoqueRepository movimentacoes;
    @Transactional(readOnly = true)
    public List<DevCompra> listar(Long empresaId) { return devolucoes.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId); }
    @Transactional(readOnly = true)
    public List<DevCompraItem> itensDe(Long empresaId, Long id) { porEmpresa(empresaId, id); return itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(empresaId, id); }
    @Transactional
    public DevCompra solicitar(Long empresaId, Long pedidoId, String motivo, List<Map<String, Object>> itensReq) {
        PedidoCompra pedido = pedidos.findByIdForUpdate(pedidoId).orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        if (empresaId.equals(pedido.getEmpresaId()) == false) throw new ResourceNotFoundException("Pedido de compra nao encontrado");
        if ("RECEBIDO".equals(pedido.getStatus()) == false && "PARCIAL".equals(pedido.getStatus()) == false) throw new BusinessException("Somente pedidos recebidos podem ser devolvidos");
        if (motivo == null || motivo.isBlank()) throw new BusinessException("Motivo obrigatorio");
        if (itensReq == null || itensReq.isEmpty()) throw new BusinessException("Informe ao menos um item");
        DevCompra d = new DevCompra();
        d.setEmpresaId(empresaId);
        d.setPedidoId(pedidoId);
        d.setMotivo(motivo.trim());
        d = devolucoes.save(d);
        for (Map<String, Object> r : itensReq) {
            Long produtoId = r.get("produtoId") == null ? null : Long.valueOf(String.valueOf(r.get("produtoId")));
            BigDecimal qtd = r.get("quantidade") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(r.get("quantidade")));
            if (produtoId == null || qtd.signum() <= 0) throw new BusinessException("Item invalido");
            BigDecimal recebida = BigDecimal.ZERO;
            for (ItemPedidoCompra ip : pedido.getItens()) {
                if (produtoId.equals(ip.getProdutoId())) recebida = recebida.add(ip.getQuantidadeRecebida() == null ? BigDecimal.ZERO : ip.getQuantidadeRecebida());
            }
            BigDecimal jaDevolvida = BigDecimal.ZERO;
            for (DevCompra e : devolucoes.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId)) {
                if (pedidoId.equals(e.getPedidoId()) == false || "CANCELADA".equals(e.getStatus())) continue;
                for (DevCompraItem ei : itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(empresaId, e.getId())) {
                    if (produtoId.equals(ei.getProdutoId())) jaDevolvida = jaDevolvida.add(ei.getQuantidade());
                }
            }
            if (qtd.add(jaDevolvida).compareTo(recebida) > 0) throw new BusinessException("Quantidade acima da recebida");
            DevCompraItem it = new DevCompraItem();
            it.setEmpresaId(empresaId);
            it.setDevolucaoId(d.getId());
            it.setProdutoId(produtoId);
            it.setQuantidade(qtd);
            itens.save(it);
        }
        return d;
    }
    @Transactional
    public DevCompra devolver(Long empresaId, Long id) {
        DevCompra d = porEmpresa(empresaId, id);
        if ("SOLICITADA".equals(d.getStatus()) == false) throw new BusinessException("Somente devolucao SOLICITADA pode ser devolvida");
        for (DevCompraItem it : itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(empresaId, id)) {
            if (it.getProdutoId() == null) continue;
            SaldoEstoque saldo = saldos.findByEmpresaIdAndProdutoIdForUpdate(empresaId, it.getProdutoId()).orElse(null);
            BigDecimal atual = saldo == null || saldo.getQuantidade() == null ? BigDecimal.ZERO : saldo.getQuantidade();
            if (atual.compareTo(it.getQuantidade()) < 0) throw new BusinessException("Estoque insuficiente para devolver");
            if (saldo != null) { saldo.setQuantidade(atual.subtract(it.getQuantidade())); saldos.save(saldo); }
            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setEmpresaId(empresaId);
            mov.setProdutoId(it.getProdutoId());
            mov.setTipo("SAIDA");
            mov.setOrigem("DEVOLUCAO_COMPRA");
            mov.setOrigemId(id);
            mov.setQuantidade(it.getQuantidade());
            mov.setSaldoApos(saldo == null ? BigDecimal.ZERO : saldo.getQuantidade());
            mov.setObservacao("Devolucao ao fornecedor");
            movimentacoes.save(mov);
        }
        d.setStatus("DEVOLVIDA");
        d.setDevolvidaEm(LocalDateTime.now());
        return devolucoes.save(d);
    }
    @Transactional
    public DevCompra cancelar(Long empresaId, Long id) {
        DevCompra d = porEmpresa(empresaId, id);
        if ("SOLICITADA".equals(d.getStatus()) == false) throw new BusinessException("Somente devolucao SOLICITADA pode ser cancelada");
        d.setStatus("CANCELADA");
        return devolucoes.save(d);
    }
    private DevCompra porEmpresa(Long empresaId, Long id) {
        return devolucoes.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Devolucao nao encontrada"));
    }
}
