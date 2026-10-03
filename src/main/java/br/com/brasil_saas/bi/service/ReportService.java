package br.com.brasil_saas.bi.service;

import br.com.brasil_saas.bi.dto.ReportRequest;
import br.com.brasil_saas.bi.model.Report;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface ReportService {
    
    Report create(ReportRequest request, Long empresaId);
    
    Report update(Long id, ReportRequest request, Long empresaId);
    
    Report getById(Long id, Long empresaId);
    
    PageResponse<Report> listAll(Long empresaId, Pageable pageable);
    
    List<Report> listByCategory(String category, Long empresaId);
    
    void delete(Long id, Long empresaId);
    
    byte[] generateReport(Long reportId, Map<String, Object> parameters, String outputType, Long empresaId);
    
    void scheduleReport(Long reportId, Long empresaId);
    
    void unscheduleReport(Long reportId, Long empresaId);
}
