package br.com.brasil_saas.servicos.service;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public interface OrdemServicoService {
    record AberturaReq(Long clienteId, String equipamento, String descricao, LocalDateTime previsaoAt) {}
    record EdicaoReq(Long clienteId, String equipamento, String descricao, LocalDateTime previsaoAt) {}
    record ItemReq(Long produtoId, Long servicoId, BigDecimal quantidade, BigDecimal valorUnitario) {}
    record ApontReq(BigDecimal horas, String descricao) {}
    record OsResp(Long id, String numero, Long clienteId, String equipamento, String status,
                  BigDecimal valorTotal, LocalDateTime aberturaAt, LocalDateTime fechamentoAt) {}
    record OsItemResp(Long id, Long produtoId, Long servicoId, BigDecimal quantidade,
                      BigDecimal valorUnitario, BigDecimal valorTotal) {}
    OsResp abrir(Long empresaId, Long usuarioId, AberturaReq req);
    OsResp addItem(Long empresaId, Long osId, ItemReq req);
    OsResp apontar(Long empresaId, Long usuarioId, Long osId, ApontReq req);
    OsResp fechar(Long empresaId, Long osId, String laudo);
    OsResp buscar(Long empresaId, Long osId);
    OsResp atualizar(Long empresaId, Long osId, EdicaoReq req);
    void excluir(Long empresaId, Long osId);
    List<OsItemResp> listarItens(Long empresaId, Long osId);
    PageResponse<OsResp> listar(Long empresaId, String status, Pageable pageable);
}
