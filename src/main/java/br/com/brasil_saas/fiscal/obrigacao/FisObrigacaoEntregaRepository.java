package br.com.brasil_saas.fiscal.obrigacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FisObrigacaoEntregaRepository extends JpaRepository<FisObrigacaoEntrega, Long> {
    Optional<FisObrigacaoEntrega> findByObrigacaoIdAndEmpresaIdAndCompetenciaAndDeletedAtIsNull(Long o, Long e, String c);
    List<FisObrigacaoEntrega> findByEmpresaIdAndCompetenciaAndDeletedAtIsNull(Long e, String c);
}
