package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.dto.CaixaDtos.CaixaRequest;
import br.com.brasil_saas.financeiro.dto.CaixaDtos.MovimentoRequest;
import br.com.brasil_saas.financeiro.model.Caixa;
import br.com.brasil_saas.financeiro.model.MovimentoCaixa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CaixaService {

    Page<Caixa> listar(Long empresaId, Pageable pageable, String termo);

    Caixa buscarPorId(Long id, Long empresaId);

    Caixa criar(CaixaRequest request, Long empresaId);

    Caixa atualizar(Long id, CaixaRequest request, Long empresaId);

    void desativar(Long id, Long empresaId);

    /** SANGRIA ou SUPRIMENTO — atualiza saldo e grava histórico. */
    MovimentoCaixa movimentar(Long empresaId, Long caixaId, MovimentoRequest request);

    List<MovimentoCaixa> listarMovimentos(Long empresaId, Long caixaId);
}
