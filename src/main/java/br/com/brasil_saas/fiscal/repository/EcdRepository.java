package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Ecd;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcdRepository extends JpaRepository<Ecd, Long> {
}
