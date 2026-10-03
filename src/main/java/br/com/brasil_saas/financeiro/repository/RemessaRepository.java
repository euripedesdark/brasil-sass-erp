package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Remessa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemessaRepository extends JpaRepository<Remessa, Long> {

    List<Remessa> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId);

    @Modifying
    @Query("update Remessa r set r.status = 'ENVIADA', r.atualizadoEm = CURRENT_TIMESTAMP where r.id = :id")
    void marcarEnviada(@Param("id") Long id);
}
