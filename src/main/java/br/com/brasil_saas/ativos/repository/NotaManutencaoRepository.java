package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.NotaManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotaManutencaoRepository extends JpaRepository<NotaManutencao, Long> {
    List<NotaManutencao> findAllByEmpresaIdAndDeletedAtIsNullOrderByDataNotaDescIdDesc(Long empresaId);
}
