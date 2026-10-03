package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.PessoaJuridica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PessoaJuridicaRepository extends JpaRepository<PessoaJuridica, Long> {
    Optional<PessoaJuridica> findByPessoaId(Long pessoaId);
}
