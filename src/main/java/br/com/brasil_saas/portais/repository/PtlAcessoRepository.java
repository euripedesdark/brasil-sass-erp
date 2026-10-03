package br.com.brasil_saas.portais.repository;
import br.com.brasil_saas.portais.model.PtlAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PtlAcessoRepository extends JpaRepository<PtlAcesso, Long> {
    Optional<PtlAcesso> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<PtlAcesso> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    Optional<PtlAcesso> findByTokenAndDeletedAtIsNull(String token);
    List<PtlAcesso> findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(Long empresaId, Long pessoaId);
}
