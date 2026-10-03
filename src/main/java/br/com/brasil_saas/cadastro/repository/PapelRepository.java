package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PapelRepository extends JpaRepository<Papel, Long> {
    List<Papel> findByDeletedAtIsNullOrderByNome();
    boolean existsByNomeIgnoreCaseAndDeletedAtIsNull(String nome);
}
