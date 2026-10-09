package br.com.brasil_saas.helpdesk.repository;

import br.com.brasil_saas.helpdesk.model.ChamadoComentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoComentarioRepository extends JpaRepository<ChamadoComentario, Long> {
    List<ChamadoComentario> findByEmpresaIdAndChamadoIdAndDeletedAtIsNullOrderByIdAsc(Long empresaId, Long chamadoId);
}
