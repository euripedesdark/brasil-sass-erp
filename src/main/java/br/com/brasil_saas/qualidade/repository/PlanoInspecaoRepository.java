package br.com.brasil_saas.qualidade.repository;
import br.com.brasil_saas.qualidade.model.PlanoInspecao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlanoInspecaoRepository extends JpaRepository<PlanoInspecao,Long> {
    List<PlanoInspecao> findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(Long empresaId);
}
