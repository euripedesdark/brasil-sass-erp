package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.CertificadoDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CertificadoDigitalRepository extends JpaRepository<CertificadoDigital, Long> {
    Optional<CertificadoDigital> findByEmpresaIdAndAtivoTrue(Long empresaId);
    java.util.Optional<br.com.brasil_saas.fiscal.model.CertificadoDigital> findFirstByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByValidadeAtDesc(Long empresaId);
}
