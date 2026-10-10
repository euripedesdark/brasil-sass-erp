package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.ToleranciaConferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToleranciaConferenciaRepository extends JpaRepository<ToleranciaConferencia, Long> {
    List<ToleranciaConferencia> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderById(Long empresaId);
}
