package br.com.brasil_saas.contabilidade.repository;
import br.com.brasil_saas.contabilidade.model.CtbPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CtbPartidaRepository extends JpaRepository<CtbPartida, Long> {
    Optional<CtbPartida> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<CtbPartida> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<CtbPartida> findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(Long lancamentoId, Long empresaId);
    List<CtbPartida> findByEmpresaIdAndContaIdAndDeletedAtIsNull(Long empresaId, Long contaId);
}
