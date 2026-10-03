package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.FluxoAprovacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FluxoAprovacaoRepository extends JpaRepository<FluxoAprovacao, Long> {
    List<FluxoAprovacao> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    Optional<FluxoAprovacao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<FluxoAprovacao> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNull(Long empresaId);
}
