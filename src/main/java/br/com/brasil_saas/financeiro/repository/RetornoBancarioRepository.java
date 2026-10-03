package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.RetornoBancario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetornoBancarioRepository extends JpaRepository<RetornoBancario, Long> {

    List<RetornoBancario> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId);
}
