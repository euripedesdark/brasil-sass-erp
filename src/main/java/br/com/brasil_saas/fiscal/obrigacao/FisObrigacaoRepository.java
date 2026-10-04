package br.com.brasil_saas.fiscal.obrigacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FisObrigacaoRepository extends JpaRepository<FisObrigacao, Long> {
    Optional<FisObrigacao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<FisObrigacao> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
}
