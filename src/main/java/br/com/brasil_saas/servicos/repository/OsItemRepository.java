package br.com.brasil_saas.servicos.repository;
import br.com.brasil_saas.servicos.model.OsItem;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface OsItemRepository extends JpaRepository<OsItem, Long> {
    List<OsItem> findByOsIdAndDeletedAtIsNullOrderByIdAsc(Long osId);

    List<OsItem> findByOsIdAndDeletedAtIsNull(Long osId);
}
