package br.com.brasil_saas.vendas.repository;

import br.com.brasil_saas.vendas.model.RegraComissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegraComissaoRepository extends JpaRepository<RegraComissao, Long> {

    List<RegraComissao> findByEmpresaIdAndAtivoTrueOrderByFaixaValorMinAsc(Long empresaId);

    List<RegraComissao> findByEmpresaIdAndVendedorIdAndAtivoTrueOrderByFaixaValorMinAsc(Long empresaId, Long vendedorId);

    List<RegraComissao> findByEmpresaIdAndVendedorIdIsNullAndAtivoTrueOrderByFaixaValorMinAsc(Long empresaId);

    /**
     * Inclui as inativas, para a tela de regras poder mostrar e reativar.
     * Sem empresa no filtro, editar ou excluir pela URL tocaria a regra de
     * outra empresa — mesmo problema que foi corrigido no PedidoVendaRepository.
     */
    List<RegraComissao> findByEmpresaIdOrderByFaixaValorMinAsc(Long empresaId);

    Optional<RegraComissao> findByIdAndEmpresaId(Long id, Long empresaId);
}
