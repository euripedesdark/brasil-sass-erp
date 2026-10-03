package br.com.brasil_saas.fiscal.service;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public interface EntradaNotaService {
    record ItemIn(Long produtoId, BigDecimal quantidade, BigDecimal valorUnitario, String ncm, String cfop) {}
    record EntradaReq(String chaveAcesso, Long pessoaId, String naturezaOperacao, String cfop,
                      LocalDateTime dataEmissao, BigDecimal valorTotal, Boolean gerarEstoque, List<ItemIn> itens) {}
    record ManifReq(String tipo, String justificativa) {}
    record EntradaResp(Long id, String chaveAcesso, Long pessoaId, String status, BigDecimal valorTotal, LocalDateTime dataEmissao) {}
    EntradaResp registrar(Long empresaId, EntradaReq r);
    EntradaResp manifestar(Long empresaId, Long entradaId, ManifReq r);
    EntradaResp porChave(Long empresaId, String chave);
    PageResponse<EntradaResp> listar(Long empresaId, Pageable p);
}
