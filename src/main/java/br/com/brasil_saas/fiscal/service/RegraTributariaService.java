package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.fiscal.model.RegraTributaria;
import br.com.brasil_saas.fiscal.repository.RegraTributariaRepository;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.model.Endereco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegraTributariaService {

    private final RegraTributariaRepository repository;

    public RegraTributaria resolver(Empresa empresa, Produto produto) {
        if (empresa == null || empresa.getId() == null) {
            throw new IllegalArgumentException("Empresa obrigatória para resolver tributação");
        }
        if (produto == null) {
            throw new IllegalArgumentException("Produto obrigatório para resolver tributação");
        }

        String ncm = normalizar(produto.getNcm());
        String cfop = normalizar(produto.getCfopPadrao());

        return repository.buscarRegra(empresa.getId(), ncm, cfop, normalizar(empresa.getUf()), null)
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma regra tributária ativa encontrada para produto "
                                + produto.getCodigo() + " (NCM=" + ncm + ", CFOP=" + cfop + ")"));
    }

    public RegraTributaria resolver(Empresa empresa, Produto produto, String ufDestino) {
        if (empresa == null || empresa.getId() == null || produto == null) {
            throw new IllegalArgumentException("Empresa e produto obrigatórios para resolver tributação");
        }
        String ncm = normalizar(produto.getNcm());
        String cfop = normalizar(produto.getCfopPadrao());
        return repository.buscarRegra(empresa.getId(), ncm, cfop, normalizar(empresa.getUf()), normalizar(ufDestino))
                .orElseThrow(() -> new IllegalStateException("Nenhuma regra tributária ativa encontrada para NCM=" + ncm + ", CFOP=" + cfop + ", UF destino=" + ufDestino));
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }
}
