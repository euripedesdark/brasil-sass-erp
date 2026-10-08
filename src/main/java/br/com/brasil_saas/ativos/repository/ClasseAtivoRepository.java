package br.com.brasil_saas.ativos.repository;

import br.com.brasil_saas.ativos.model.ClasseAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClasseAtivoRepository extends JpaRepository<ClasseAtivo, Long> {
    List<ClasseAtivo> findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(Long empresaId);
}
