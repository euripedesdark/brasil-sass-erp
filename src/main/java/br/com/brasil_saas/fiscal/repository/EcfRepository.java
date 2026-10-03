package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Ecf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcfRepository extends JpaRepository<Ecf, Long> {
}
