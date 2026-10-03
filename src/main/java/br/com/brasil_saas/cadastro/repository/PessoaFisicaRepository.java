package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.PessoaFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PessoaFisicaRepository extends JpaRepository<PessoaFisica, Long> {
    Optional<PessoaFisica> findByPessoaId(Long pessoaId);
}
