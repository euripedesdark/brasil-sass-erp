package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.Comissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface ComissaoRepository extends JpaRepository<Comissao, Long> {
    List<Comissao> findByFuncionarioIdAndStatus(Long funcionarioId, String status);
    List<Comissao> findByEmpresaIdAndStatus(Long empresaId, String status);

    /**
     * Operacoes por empresa.
     *
     * O repository so tinha busca por status; o controller nunca existiu, e
     * sem filtro por empresa um id de comissao de outra empresa seria
     * pagavel. O padrao e o mesmo do PedidoVendaRepository.
     */
    Optional<Comissao> findByIdAndEmpresaId(Long id, Long empresaId);

    List<Comissao> findByEmpresaIdAndFuncionarioId(Long empresaId, Long funcionarioId);

    List<Comissao> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);


    @Query("""
            select coalesce(sum(c.valorVenda), 0)
            from Comissao c
            where c.empresaId = :empresaId
              and c.funcionarioId = :funcionarioId
              and c.createdAt >= :inicio
              and c.createdAt < :fim
            """)
    BigDecimal sumVendasPeriodo(@Param("empresaId") Long empresaId,
                                @Param("funcionarioId") Long funcionarioId,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fim") LocalDateTime fim);
}
