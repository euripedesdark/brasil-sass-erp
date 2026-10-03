package br.com.brasil_saas.qualidade.repository;
import br.com.brasil_saas.qualidade.model.NaoConformidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade,Long> {
    List<NaoConformidade> findAllByEmpresaIdAndDeletedAtIsNullOrderByPrazoAsc(Long empresaId);
}
