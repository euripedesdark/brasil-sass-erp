package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.DocumentoFiscal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentoFiscalRepository extends JpaRepository<DocumentoFiscal, Long> {
    Optional<DocumentoFiscal> findByChaveAcessoAndDeletedAtIsNull(String chaveAcesso);
    List<DocumentoFiscal> findByPessoaIdAndDeletedAtIsNull(Long pessoaId);
    Page<DocumentoFiscal> findByEmpresaIdAndDeletedAtIsNull(Long empresaId, Pageable pageable);
}
