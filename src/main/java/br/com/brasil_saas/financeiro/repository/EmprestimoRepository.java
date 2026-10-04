package br.com.brasil_saas.financeiro.repository;
import br.com.brasil_saas.financeiro.model.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    Optional<Emprestimo> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<Emprestimo> findByEmpresaIdAndDeletedAtIsNullOrderByDataContratacaoDesc(Long empresaId);
}
