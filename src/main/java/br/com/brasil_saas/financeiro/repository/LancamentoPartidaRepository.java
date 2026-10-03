package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.LancamentoPartida;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoPartidaRepository extends JpaRepository<LancamentoPartida, Long> {
    List<LancamentoPartida> findByLancamentoIdAndDeletedAtIsNull(Long lancamentoId);
}
