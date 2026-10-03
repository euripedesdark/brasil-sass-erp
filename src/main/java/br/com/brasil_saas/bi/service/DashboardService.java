package br.com.brasil_saas.bi.service;

import br.com.brasil_saas.bi.dto.DashboardRequest;
import br.com.brasil_saas.bi.model.Dashboard;

import java.util.List;

public interface DashboardService {

    Dashboard criar(Long empresaId, DashboardRequest request);

    Dashboard atualizar(Long empresaId, Long id, DashboardRequest request);

    Dashboard buscarPorId(Long empresaId, Long id);

    List<Dashboard> listarPorEmpresa(Long empresaId);

    List<Dashboard> listarPublicos(Long empresaId);

    List<Dashboard> listarPorTipo(Long empresaId, String tipo);

    List<Dashboard> listarPorUsuario(Long empresaId, Long usuarioId);

    void excluir(Long empresaId, Long id);

    Dashboard duplicar(Long empresaId, Long id, String novoNome);
}
