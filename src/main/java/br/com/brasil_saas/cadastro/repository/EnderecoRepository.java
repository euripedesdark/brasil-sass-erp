package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> findByPessoaIdAndDeletedAtIsNull(Long pessoaId);
}
