package br.com.brasil_saas.financeiro.service;
import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
@Service @RequiredArgsConstructor
public class CreditoService {
    private final ClienteRepository clientes;
    private final TituloRepository titulos;
    private final PedidoVendaRepository pedidos;
    @Transactional(readOnly = true) public Map<String, Object> analisar(Long empresaId, Long clienteId) {
        Cliente c = clientes.findById(clienteId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente inexistente"));
        Long pessoaId = c.getPessoa() == null ? null : c.getPessoa().getId();
        BigDecimal emAberto = BigDecimal.ZERO;
        BigDecimal vencido = BigDecimal.ZERO;
        java.time.LocalDate hoje = java.time.LocalDate.now();
        if (pessoaId != null) for (Titulo t : titulos.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(empresaId, pessoaId)) {
            if ("ABERTO".equals(t.getStatus()) == false && "PARCIAL".equals(t.getStatus()) == false) continue;
            BigDecimal s = t.getValorSaldo() == null ? BigDecimal.ZERO : t.getValorSaldo();
            emAberto = emAberto.add(s);
            if (t.getDataVencimento() != null && t.getDataVencimento().isBefore(hoje)) vencido = vencido.add(s);
        }
        BigDecimal emPedidos = BigDecimal.ZERO;
        for (PedidoVenda p : pedidos.findByEmpresaIdAndClienteId(empresaId, clienteId)) {
            if ("ABERTO".equals(p.getStatus()) == false) continue;
            emPedidos = emPedidos.add(p.getValorTotal() == null ? BigDecimal.ZERO : p.getValorTotal());
        }
        BigDecimal limite = c.getLimiteCredito() == null ? BigDecimal.ZERO : c.getLimiteCredito();
        BigDecimal comprometido = emAberto.add(emPedidos);
        BigDecimal disponivel = limite.subtract(comprometido);
        String situacao = disponivel.signum() < 0 ? "ESTOURADO" : vencido.signum() > 0 ? "INADIMPLENTE" : "OK";
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("clienteId", clienteId);
        m.put("limite", limite);
        m.put("emAberto", emAberto);
        m.put("vencido", vencido);
        m.put("emPedidos", emPedidos);
        m.put("comprometido", comprometido);
        m.put("disponivel", disponivel);
        m.put("situacao", situacao);
        return m;
    }
}
