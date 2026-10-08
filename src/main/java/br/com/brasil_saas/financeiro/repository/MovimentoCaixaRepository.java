package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.MovimentoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentoCaixaRepository extends JpaRepository<MovimentoCaixa, Long> {
    List<MovimentoCaixa> findByEmpresaIdAndCaixaIdOrderByCreatedAtDesc(Long empresaId, Long caixaId);
}
