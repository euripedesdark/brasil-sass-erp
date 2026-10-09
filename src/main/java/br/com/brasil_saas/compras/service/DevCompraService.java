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
import java.util.LinkedHashMap;
import java.util.Map;
@Service @RequiredArgsConstructor
public class DevCompraService {
    private final DevCompraRepository devolucoes;
    private final DevCompraItemRepository itens;
    private final PedidoCompraRepository pedidos;
    private final SaldoEstoqueRepository saldos;
    private final MovimentacaoEstoqueRepository movimentacoes;
    private final br.com.brasil_saas.estoque.repository.DepositoRepository depositos;
    private final br.com.brasil_saas.estoque.repository.ReservaEstoqueRepository reservas;
    @Transactional(readOnly = true)
    public List<DevCompra> listar(Long empresaId) { return devolucoes.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId); }
    @Transactional(readOnly = true)
    public List<DevCompraItem> itensDe(Long empresaId, Long id) { porEmpresa(empresaId, id); return itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(empresaId, id); }
    @Transactional
    public DevCompra solicitar(Long empresaId, Long pedidoId, String motivo, List<Map<String, Object>> itensReq) {
        PedidoCompra pedido = pedidos.findByIdForUpdateAndEmpresaId(pedidoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        if (!"RECEBIDO".equals(pedido.getStatus()) && !"PARCIAL".equals(pedido.getStatus()))
            throw new BusinessException("Somente pedidos recebidos podem ser devolvidos");
        if (motivo == null || motivo.isBlank() || motivo.trim().length() > 500)
            throw new BusinessException("Motivo obrigatorio, com no maximo 500 caracteres");
        if (itensReq == null || itensReq.isEmpty()) throw new BusinessException("Informe ao menos um item");
        Map<Long, BigDecimal> solicitadas = new LinkedHashMap<>();
        for (var r : itensReq) {
            try {
                if (r == null || r.get("produtoId") == null || r.get("quantidade") == null)
                    throw new BusinessException("Item invalido");
                Long produtoId = Long.valueOf(String.valueOf(r.get("produtoId")));
                BigDecimal qtd = quantidade(new BigDecimal(String.valueOf(r.get("quantidade"))));
                solicitadas.merge(produtoId, qtd, BigDecimal::add);
            } catch (NumberFormatException e) { throw new BusinessException("Item invalido"); }
        }
        Map<Long, BigDecimal> recebidas = new LinkedHashMap<>();
        if (pedido.getItens() != null) for (ItemPedidoCompra ip : pedido.getItens())
            if (ip.getProdutoId() != null) recebidas.merge(ip.getProdutoId(),
                    ip.getQuantidadeRecebida() == null ? BigDecimal.ZERO : ip.getQuantidadeRecebida(), BigDecimal::add);
        Map<Long, BigDecimal> comprometidas = new LinkedHashMap<>();
        for (DevCompra anterior : devolucoes.findByPedidoIdAndEmpresaIdAndDeletedAtIsNull(pedidoId, empresaId)) {
            if ("CANCELADA".equals(anterior.getStatus())) continue;
            for (DevCompraItem it : itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(empresaId, anterior.getId()))
                comprometidas.merge(it.getProdutoId(), quantidade(it.getQuantidade()), BigDecimal::add);
        }
        for (var e : solicitadas.entrySet())
            if (e.getValue().add(comprometidas.getOrDefault(e.getKey(), BigDecimal.ZERO))
                    .compareTo(recebidas.getOrDefault(e.getKey(), BigDecimal.ZERO)) > 0)
                throw new BusinessException("Quantidade acima da recebida");
        DevCompra d = new DevCompra(); d.setEmpresaId(empresaId); d.setPedidoId(pedidoId);
        d.setMotivo(motivo.trim()); d = devolucoes.save(d);
        for (var e : solicitadas.entrySet()) {
            DevCompraItem it = new DevCompraItem(); it.setEmpresaId(empresaId); it.setDevolucaoId(d.getId());
            it.setProdutoId(e.getKey()); it.setQuantidade(e.getValue()); itens.save(it);
        }
        return d;
    }
    @Transactional
    public DevCompra devolver(Long empresaId, Long id) {
        DevCompra d = bloqueada(empresaId, id);
        if (!"SOLICITADA".equals(d.getStatus())) throw new BusinessException("Somente devolucao SOLICITADA pode ser devolvida");
        var linhas = itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(empresaId, id);
        if (linhas.isEmpty()) throw new BusinessException("Devolucao sem itens");
        Map<Long, BigDecimal> quantidades = new java.util.TreeMap<>();
        for (var it : linhas) {
            if (it.getProdutoId() == null) throw new BusinessException("Item sem produto");
            quantidades.merge(it.getProdutoId(), quantidade(it.getQuantidade()), BigDecimal::add);
        }
        Long depositoId = depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(empresaId, "PADRAO")
                .or(() -> depositos.findFirstByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(empresaId))
                .orElseThrow(() -> new BusinessException("Nenhum deposito ativo para devolver")).getId();
        depositos.findAtivoForUpdate(depositoId, empresaId)
                .orElseThrow(() -> new BusinessException("Deposito indisponivel"));
        Map<Long, SaldoEstoque> bloqueados = new LinkedHashMap<>();
        for (var e : quantidades.entrySet()) {
            var saldo = saldos.findForUpdate(empresaId, depositoId, e.getKey())
                    .orElseThrow(() -> new BusinessException("Estoque insuficiente para devolver"));
            var reservada = reservas.sumAtivas(empresaId, depositoId, e.getKey());
            var disponivel = (saldo.getQuantidade() == null ? BigDecimal.ZERO : saldo.getQuantidade())
                    .subtract(reservada == null ? BigDecimal.ZERO : reservada);
            if (disponivel.compareTo(e.getValue()) < 0) throw new BusinessException("Estoque livre insuficiente para devolver");
            bloqueados.put(e.getKey(), saldo);
        }
        for (var e : quantidades.entrySet()) {
            var saldo = bloqueados.get(e.getKey()); saldo.setQuantidade(saldo.getQuantidade().subtract(e.getValue()));
            saldos.save(saldo);
            MovimentacaoEstoque mov = new MovimentacaoEstoque(); mov.setEmpresaId(empresaId);
            mov.setProdutoId(e.getKey()); mov.setDepositoId(depositoId); mov.setTipo("SAIDA");
            mov.setOrigem("DEVOLUCAO_COMPRA"); mov.setOrigemId(id); mov.setQuantidade(e.getValue().negate());
            mov.setSaldoApos(saldo.getQuantidade()); mov.setObservacao("Devolucao ao fornecedor"); movimentacoes.save(mov);
        }
        d.setStatus("DEVOLVIDA");
        d.setDevolvidaEm(LocalDateTime.now());
        return devolucoes.save(d);
    }
    @Transactional
    public DevCompra cancelar(Long empresaId, Long id) {
        DevCompra d = bloqueada(empresaId, id);
        if ("SOLICITADA".equals(d.getStatus()) == false) throw new BusinessException("Somente devolucao SOLICITADA pode ser cancelada");
        d.setStatus("CANCELADA");
        return devolucoes.save(d);
    }
    private BigDecimal quantidade(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 3)
            throw new BusinessException("Quantidade deve ser positiva, com no maximo 3 casas decimais");
        return valor;
    }
    private DevCompra bloqueada(Long empresaId, Long id) {
        return devolucoes.findForUpdate(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Devolucao nao encontrada"));
    }
    private DevCompra porEmpresa(Long empresaId, Long id) {
        return devolucoes.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Devolucao nao encontrada"));
    }
}
