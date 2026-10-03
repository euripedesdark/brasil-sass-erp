package br.com.brasil_saas.financeiro.repository;
import br.com.brasil_saas.financeiro.model.Renegociacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface RenegociacaoRepository extends JpaRepository<Renegociacao, Long> {
    Optional<Renegociacao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<Renegociacao> findByEmpresaIdAndDeletedAtIsNullOrderByDataRenegociacaoDesc(Long empresaId);
}
