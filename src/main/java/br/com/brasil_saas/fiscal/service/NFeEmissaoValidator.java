package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.fiscal.model.RegraTributaria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NFeEmissaoValidator {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RegraTributariaService regraTributariaService;

    public void validar(Empresa empresa, PedidoVenda pedido) {
        List<String> erros = new ArrayList<>();

        if (empresa.getCnpj() == null || !somenteDigitos(empresa.getCnpj(), 14)) {
            erros.add("CNPJ da empresa emitente deve conter 14 dígitos");
        }
        if (empresa.getRazaoSocial() == null || empresa.getRazaoSocial().isBlank()) {
            erros.add("Razão social da empresa emitente não cadastrada");
        }
        if (empresa.getInscricaoEstadual() == null || empresa.getInscricaoEstadual().isBlank()) {
            erros.add("Inscrição estadual da empresa emitente não cadastrada");
        }
        if (empresa.getUf() == null || empresa.getUf().isBlank()) {
            erros.add("UF da empresa emitente não cadastrada");
        }

        if (pedido.getClienteId() == null) {
            erros.add("Pedido sem cliente");
        } else {
            Cliente cliente = clienteRepository.findByIdAndEmpresaIdAndDeletedAtIsNullWithPessoa(
                    pedido.getClienteId(), empresa.getId()).orElse(null);
            if (cliente == null) {
                erros.add("Cliente do pedido não pertence à empresa ou foi removido");
            }
            if (cliente != null && cliente.getPessoa() != null) {
                Pessoa pessoa = cliente.getPessoa();
                if (pessoa.getDocumento() == null || !somenteDigitos(pessoa.getDocumento(), 11, 14)) {
                    erros.add("Documento do destinatário deve conter CPF ou CNPJ válido em quantidade de dígitos");
                }
                if (pessoa.getNome() == null || pessoa.getNome().isBlank()) {
                    erros.add("Nome do destinatário não cadastrado");
                }
            }
        }

        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            erros.add("Pedido sem itens para emissão da NF-e");
        } else {
            for (ItemPedidoVenda item : pedido.getItens()) {
                if (item.getProdutoId() == null) {
                    erros.add("Item " + item.getNumeroItem() + " sem produto");
                    continue;
                }

                Produto produto = produtoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(
                        item.getProdutoId(), empresa.getId()).orElse(null);
                if (produto == null) {
                    erros.add("Produto do item " + item.getNumeroItem() + " não pertence à empresa ou foi removido");
                    continue;
                }

                if (produto.getNcm() == null || !produto.getNcm().replaceAll("\\D", "").matches("\\d{8}")) {
                    erros.add("Produto " + produto.getCodigo() + " sem NCM de 8 dígitos");
                }
                String cfop = produto.getCfopPadrao();
                if (cfop == null || !cfop.matches("\\d{4}")) {
                    erros.add("Produto " + produto.getCodigo() + " sem CFOP padrão de 4 dígitos");
                }
                try {
                    RegraTributaria regra = regraTributariaService.resolver(empresa, produto);
                    if (regra.getCstIcms() == null || regra.getCstIcms().isBlank()) {
                        erros.add("Produto " + produto.getCodigo() + " sem CST de ICMS na regra tributária");
                    }
                } catch (IllegalStateException ex) {
                    erros.add(ex.getMessage());
                }
                if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                    erros.add("Item " + item.getNumeroItem() + " com quantidade inválida");
                }
                if (item.getValorUnitario() == null || item.getValorUnitario().compareTo(BigDecimal.ZERO) < 0) {
                    erros.add("Item " + item.getNumeroItem() + " com valor unitário inválido");
                }
            }
        }

        if (pedido.getValorTotal() == null || pedido.getValorTotal().compareTo(BigDecimal.ZERO) < 0) {
            erros.add("Valor total do pedido inválido");
        }

        if (!erros.isEmpty()) {
            throw new IllegalStateException("Pedido não está apto para emissão de NF-e: " + String.join("; ", erros));
        }
    }

    private boolean somenteDigitos(String valor, int... tamanhos) {
        if (valor == null) return false;
        String digitos = valor.replaceAll("\\D", "");
        for (int tamanho : tamanhos) {
            if (digitos.length() == tamanho) return true;
        }
        return false;
    }
}
