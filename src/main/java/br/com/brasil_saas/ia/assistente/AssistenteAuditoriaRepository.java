package br.com.brasil_saas.ia.assistente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistenteAuditoriaRepository extends JpaRepository<AssistenteAuditoria, Long> {

    List<AssistenteAuditoria> findByEmpresaIdOrderByIdDesc(Long empresaId);
}
