package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Page<Cliente> findByDeletedAtIsNull(Pageable pageable);
    Page<Cliente> findByEmpresaIdAndDeletedAtIsNull(Long empresaId, Pageable pageable);
    Optional<Cliente> findByPessoaIdAndDeletedAtIsNull(Long pessoaId);
    Optional<Cliente> findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(Long empresaId, Long pessoaId);
\n    @Query("select c from Cliente c join fetch c.pessoa p where c.id = :id and c.empresaId = :empresaId and c.deletedAt is null")\n    Optional<Cliente> findByIdAndEmpresaIdAndDeletedAtIsNullWithPessoa(@Param("id") Long id, @Param("empresaId") Long empresaId);\n    boolean existsByPessoaIdAndDeletedAtIsNull(Long pessoaId);

    /**
     * Nome do cliente direto do banco, em uma consulta so.
     *
     * O controller de OS imprimia o nome percorrendo c.getPessoa().getNome() fora
     * de transacao: o findById fecha a sessao ao devolver e o proxy lazy estourava
     * LazyInitializationException, entao a impressao da OS devolvia 500. Traz o
     * nome ja resolvido evita montar o grafo inteiro so para mostrar quem e o
     * cliente.
     */
    @Query("select p.nome from Cliente c join c.pessoa p where c.id = :id and c.deletedAt is null")
    Optional<String> nomeDaPessoaPorClienteId(@Param("id") Long id);
}
