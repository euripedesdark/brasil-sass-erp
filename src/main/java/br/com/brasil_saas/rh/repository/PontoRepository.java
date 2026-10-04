package br.com.brasil_saas.rh.repository;
import br.com.brasil_saas.rh.model.Ponto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface PontoRepository extends JpaRepository<Ponto, Long> {
    Optional<Ponto> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    Optional<Ponto> findByEmpresaIdAndFuncionarioIdAndDataAndDeletedAtIsNull(Long empresaId, Long funcionarioId, LocalDate data);
    List<Ponto> findByEmpresaIdAndFuncionarioIdAndDeletedAtIsNullOrderByDataDesc(Long empresaId, Long funcionarioId);
}
