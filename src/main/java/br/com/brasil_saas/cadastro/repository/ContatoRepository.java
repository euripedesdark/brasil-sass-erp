package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContatoRepository extends JpaRepository<Contato, Long> {
    List<Contato> findByPessoaIdAndDeletedAtIsNull(Long pessoaId);
}
