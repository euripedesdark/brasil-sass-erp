package br.com.brasil_saas.producao.repository;

import br.com.brasil_saas.producao.model.RomaneioProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RomaneioProducaoRepository extends JpaRepository<RomaneioProducao, Long> {
    List<RomaneioProducao> findByEmpresaIdAndDeletedAtIsNullOrderByDataRomaneioDesc(Long empresaId);
}
