package br.com.brasil_saas.bi.service.impl;

import br.com.brasil_saas.bi.dto.DashboardRequest;
import br.com.brasil_saas.bi.model.Dashboard;
import br.com.brasil_saas.bi.repository.DashboardRepository;
import br.com.brasil_saas.bi.service.DashboardService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    @Override
    @Transactional
    public Dashboard criar(Long empresaId, DashboardRequest request) {
        Dashboard dashboard = new Dashboard();
        dashboard.setEmpresaId(empresaId);
        dashboard.setNome(request.nome());
        dashboard.setDescricao(request.descricao());
        dashboard.setTipo(request.tipo());
        dashboard.setLayout(request.layout());
        dashboard.setFiltros(request.filtros());
        dashboard.setAtivo(request.ativo());
        dashboard.setPublico(request.publico());
        dashboard.setDataCriacao(LocalDate.now());
        dashboard.setDataAtualizacao(LocalDate.now());

        return dashboardRepository.save(dashboard);
    }

    @Override
    @Transactional
    public Dashboard atualizar(Long empresaId, Long id, DashboardRequest request) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard nao encontrado"));

        if (!dashboard.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Dashboard nao pertence a esta empresa");
        }

        dashboard.setNome(request.nome());
        dashboard.setDescricao(request.descricao());
        dashboard.setTipo(request.tipo());
        dashboard.setLayout(request.layout());
        dashboard.setFiltros(request.filtros());
        dashboard.setAtivo(request.ativo());
        dashboard.setPublico(request.publico());
        dashboard.setDataAtualizacao(LocalDate.now());

        return dashboardRepository.save(dashboard);
    }

    @Override
    @Transactional(readOnly = true)
    public Dashboard buscarPorId(Long empresaId, Long id) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard nao encontrado"));

        if (!dashboard.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Dashboard nao pertence a esta empresa");
        }

        return dashboard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dashboard> listarPorEmpresa(Long empresaId) {
        return dashboardRepository.findByEmpresaIdAndAtivoTrue(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dashboard> listarPublicos(Long empresaId) {
        return dashboardRepository.findByEmpresaIdAndPublicoTrue(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dashboard> listarPorTipo(Long empresaId, String tipo) {
        return dashboardRepository.findByEmpresaIdAndTipo(empresaId, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dashboard> listarPorUsuario(Long empresaId, Long usuarioId) {
        return dashboardRepository.findByEmpresaIdAndCriadoPor(empresaId, usuarioId);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard nao encontrado"));

        if (!dashboard.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Dashboard nao pertence a esta empresa");
        }

        dashboardRepository.delete(dashboard);
    }

    @Override
    @Transactional
    public Dashboard duplicar(Long empresaId, Long id, String novoNome) {
        Dashboard original = dashboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard nao encontrado"));

        if (!original.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Dashboard nao pertence a esta empresa");
        }

        Dashboard copia = new Dashboard();
        copia.setEmpresaId(empresaId);
        copia.setNome(novoNome);
        copia.setDescricao(original.getDescricao() + " (Copia)");
        copia.setTipo(original.getTipo());
        copia.setLayout(original.getLayout());
        copia.setFiltros(original.getFiltros());
        copia.setAtivo(false);
        copia.setPublico(original.getPublico());
        copia.setDataCriacao(LocalDate.now());
        copia.setDataAtualizacao(LocalDate.now());

        return dashboardRepository.save(copia);
    }
}
