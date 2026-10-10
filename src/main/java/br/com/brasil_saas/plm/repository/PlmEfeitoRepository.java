package br.com.brasil_saas.plm.repository;

import br.com.brasil_saas.plm.model.PlmEfeito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlmEfeitoRepository extends JpaRepository<PlmEfeito, Long> {
    List<PlmEfeito> findByEmpresaIdAndMudancaIdOrderByOrdemExecucaoAscIdAsc(Long empresaId, Long mudancaId);
}
