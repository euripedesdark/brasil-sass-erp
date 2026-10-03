package br.com.brasil_saas.bi.repository.datalake;

import br.com.brasil_saas.bi.model.datalake.DimTempo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DimTempoRepository extends JpaRepository<DimTempo, Long> {

    Optional<DimTempo> findByData(LocalDate data);

    List<DimTempo> findByAno(Integer ano);

    List<DimTempo> findByAnoAndMes(Integer ano, Integer mes);

    List<DimTempo> findByDataBetween(LocalDate start, LocalDate end);

    List<DimTempo> findByAnoAndTrimestre(Integer ano, Integer trimestre);

    List<DimTempo> findBySemanaDoAno(Integer semana);

    List<DimTempo> findByEhFimDeSemana(Boolean ehFimDeSemana);

    List<DimTempo> findByEhFeriado(Boolean ehFeriado);

    List<DimTempo> findByDataGreaterThanEqualOrderByDataAsc(LocalDate data);

    List<DimTempo> findByAnoOrderByDataAsc(Integer ano);

    @Query("SELECT COUNT(t) FROM DimTempo t")
    long countAll();
}
