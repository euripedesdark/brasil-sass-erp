package br.com.brasil_saas.producao.repository;

import br.com.brasil_saas.producao.model.ApontamentoProducao;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApontamentoProducaoRepository extends TenantRepository<ApontamentoProducao, Long> {

    List<ApontamentoProducao> findByEmpresaIdAndProducaoId(Long empresaId, Long producaoId);

    List<ApontamentoProducao> findByEmpresaIdAndFuncionarioId(Long empresaId, Long funcionarioId);

    List<ApontamentoProducao> findByEmpresaIdAndDataApontamentoBetween(
            Long empresaId, LocalDateTime dataInicio, LocalDateTime dataFim);

    List<ApontamentoProducao> findByEmpresaIdAndStatus(Long empresaId, String status);

    @Query("SELECT COALESCE(SUM(a.horasTrabalhadas), 0) FROM ApontamentoProducao a " +
           "WHERE a.empresaId = :empresaId AND a.producao.id = :producaoId")
    BigDecimal sumHorasTrabalhadasByProducao(Long empresaId, Long producaoId);

    @Query("SELECT COALESCE(SUM(a.quantidadeProduzida), 0) FROM ApontamentoProducao a " +
           "WHERE a.empresaId = :empresaId AND a.producao.id = :producaoId")
    BigDecimal sumQuantidadeProduzidaByProducao(Long empresaId, Long producaoId);

    @Query("SELECT COALESCE(SUM(a.quantidadeRefugo), 0) FROM ApontamentoProducao a " +
           "WHERE a.empresaId = :empresaId AND a.producao.id = :producaoId")
    BigDecimal sumQuantidadeRefugoByProducao(Long empresaId, Long producaoId);
}
