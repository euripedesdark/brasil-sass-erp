package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.AlcadaAprovacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlcadaAprovacaoRepository extends JpaRepository<AlcadaAprovacao, Long> {
    List<AlcadaAprovacao> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByValorLimiteAsc(Long empresaId);
}
