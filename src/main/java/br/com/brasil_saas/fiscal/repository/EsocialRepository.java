package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Esocial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EsocialRepository extends JpaRepository<Esocial, Long> {
    List<Esocial> findByEmpresaIdAndCompetencia(Long empresaId, String competencia);
}
