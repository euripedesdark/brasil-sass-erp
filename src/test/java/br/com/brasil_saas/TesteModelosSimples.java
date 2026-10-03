package br.com.brasil_saas;

import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Fornecedor;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.rh.model.Funcionario;
import br.com.brasil_saas.rh.model.Cargo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TesteModelosSimples {

    @Test
    public void testarModelosPrincipais() {
        // Testar modelo de pessoa
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("João Silva");
        pessoa.setDocumento("12345678901");
        assertEquals("João Silva", pessoa.getNome());
        
        // Testar modelo de cliente
        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        cliente.setLimiteCredito(new BigDecimal("10000.00"));
        assertTrue(cliente.getLimiteCredito().compareTo(new BigDecimal("0")) > 0);
        
        // Testar modelo de fornecedor
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setPessoa(pessoa);
        fornecedor.setPrazoMedioDias(30);
        assertEquals(30, fornecedor.getPrazoMedioDias());
        
        // Testar modelo de título financeiro
        Titulo titulo = new Titulo();
        titulo.setTipo("R");
        titulo.setDescricao("Venda de produtos");
        titulo.setValorOriginal(new BigDecimal("5000.00"));
        titulo.setDataEmissao(LocalDate.now());
        titulo.setDataVencimento(LocalDate.now().plusDays(30));
        assertEquals("R", titulo.getTipo());
        
        // Testar modelo de cargo
        Cargo cargo = new Cargo();
        cargo.setNome("Gerente");
        cargo.setSalarioBase(new BigDecimal("8000.00"));
        assertEquals("Gerente", cargo.getNome());
        
        // Testar modelo de funcionário
        Funcionario funcionario = new Funcionario();
        funcionario.setPessoaId(1L);
        funcionario.setCargo(cargo);
        funcionario.setSalario(new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("5000.00"), funcionario.getSalario());
        
        System.out.println("✓ Todos os modelos principais estão funcionando corretamente!");
    }
}