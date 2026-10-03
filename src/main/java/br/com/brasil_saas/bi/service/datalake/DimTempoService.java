package br.com.brasil_saas.bi.service.datalake;

import br.com.brasil_saas.bi.model.datalake.DimTempo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DimTempoService {

    // CRUD operations
    DimTempo save(DimTempo dimTempo);
    
    Optional<DimTempo> findById(Long id);
    
    Optional<DimTempo> findByData(LocalDate data);
    
    List<DimTempo> findAll();
    
    List<DimTempo> findByAno(Integer ano);
    
    List<DimTempo> findByAnoAndMes(Integer ano, Integer mes);
    
    List<DimTempo> findByDataBetween(LocalDate start, LocalDate end);
    
    // Business methods
    void populateTimeDimension(LocalDate startDate, LocalDate endDate);
    
    void populateTimeDimensionForYear(Integer year);
    
    void populateTimeDimensionForCurrentYear();
    
    void populateTimeDimensionForLastYear();
    
    void populateTimeDimensionForRange(Integer startYear, Integer endYear);
    
    List<DimTempo> getLast30Days();
    
    List<DimTempo> getLast7Days();
    
    List<DimTempo> getCurrentYear();
    
    List<DimTempo> getCurrentMonth();
    
    List<DimTempo> getCurrentQuarter();
    
    List<DimTempo> getLast12Months();
    
    long count();
    
    void deleteAll();
}
