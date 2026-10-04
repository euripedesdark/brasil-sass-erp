package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Apuracao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApuracaoRepository extends JpaRepository<Apuracao, Long> {
    Optional<Apuracao> findByEmpresaIdAndImpostoIdAndCompetencia(Long empresaId, Long impostoId, String competencia);
    java.util.List<Apuracao> findByEmpresaIdOrderByCompetenciaDesc(Long empresaId);
}
