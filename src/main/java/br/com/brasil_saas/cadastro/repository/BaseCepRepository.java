package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.BaseCep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BaseCepRepository extends JpaRepository<BaseCep, Long> {
    List<BaseCep> findByCep(String cep);
}
