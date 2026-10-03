package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    List<Marca> findByDeletedAtIsNullOrderByNome();
    boolean existsByNomeIgnoreCaseAndDeletedAtIsNull(String nome);
}
