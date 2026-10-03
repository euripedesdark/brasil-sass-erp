package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.ReportParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportParameterRepository extends JpaRepository<ReportParameter, Long> {
    List<ReportParameter> findByReportId(Long reportId);
    void deleteByReportId(Long reportId);
}
