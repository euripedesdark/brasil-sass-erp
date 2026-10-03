package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Issqn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssqnRepository extends JpaRepository<Issqn, Long> {
    Optional<Issqn> findByCodIbge(String codIbge);
    List<Issqn> findByUfOrderByMunicipio(String uf);
}
